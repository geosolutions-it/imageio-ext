/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jpx_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_compositor_buf;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_message_formatter;
import kdu_jni.Kdu_region_compositor;
import kdu_jni.Kdu_simple_file_source;

/**
 * {@link JP2KakaduImageReader} is a <code>ImageReader</code> able to create
 * {@link RenderedImage} from JP2 files, leveraging on kdu_jni bindings.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class JP2KakaduImageReader extends ImageReader {

	private static Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jp2k");

	/** Auxiliary String Buffer. It is used for messages logging */
	private StringBuffer sb;

	private int dataBufferType;


	/** The source resolution levels. */
	private int sourceDWTLevels;

	/** It is simply 2^sourceDWTLevels */
	private int maxSupportedSubSamplingFactor;

	/**
	 * Represents the max number of available quality layers (only after a
	 * getMaxQualityLayers method call)
	 */
	private int maxAvailableQualityLayers = -1;

	private final static int[] BITMASKS_RGB = new int[] { 0xff0000, 0xff00,
			0xff, };

	/** The dataset input source */
	private File fileSource = null;

	/** sample model for the whole image */
	private SampleModel sm = null;

	/** color model */
	private ColorModel cm = null;

	/** Number of components of the source. */
	private int nBands;

	/** the bitDepth */
	private int bitDepth;

	/** the whole image width */
	private int width;

	/** the whole image height */
	private int height;

	private boolean isRawSource;

	private Kdu_codestream codestream; // Needs `destroy'

	private Kdu_dims tileSize;

	private int tileWidth;

	private int tileHeight;

	private Jp2_family_src familySource;

	private Jpx_source wrappedSource;

	/** The ImageInputStream */
	private ImageInputStream imageInputStream;

	protected JP2KakaduImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
		initializeKakaduMessagesManagement();
	}

	/**
	 * Initializing kakadu messages as stated in the KduRender.java example
	 */
	private void initializeKakaduMessagesManagement() {
		try {

			// ////
			// Customize error and warning services
			// ////

			// Non-throwing message printer
			Kdu_sysout_message sysout = new Kdu_sysout_message(false);

			// Exception-throwing message printer
			Kdu_sysout_message syserr = new Kdu_sysout_message(true);

			// /////
			// Initialize formatted message printer
			// ////

			// Non-throwing printer
			Kdu_message_formatter pretty_sysout = new Kdu_message_formatter(
					sysout);
			// Throwing printer
			Kdu_message_formatter pretty_syserr = new Kdu_message_formatter(
					syserr);
			Kdu_global.Kdu_customize_warnings(pretty_sysout);
			Kdu_global.Kdu_customize_errors(pretty_syserr);

		} catch (KduException e) { // Quoting from KduRender.java example: "See
			// END NOTE 2"
			throw new RuntimeException(
					"Error caused by a Kakadu exception during creation of key objects! ",
					e);
		}
	}

	/**
	 * Returns the height in pixel of the image
	 */
	public int getHeight(int imageIndex) throws IOException {
		return height;
	}

	/**
	 * Returns the width in pixel of the image
	 */
	public int getWidth(int imageIndex) throws IOException {
		return width;
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		return null;
	}

	public Iterator getImageTypes(int imageIndex) throws IOException {
		final List l = new java.util.ArrayList(5);

		// Setting SampleModel and ColorModel for the whole image
		if (cm == null || sm == null)
			setSampleModelAndColorModel();

		ImageTypeSpecifier imageType = new ImageTypeSpecifier(cm, sm);
		l.add(imageType);
		return l.iterator();
	}

	/**
	 * Returns the number of images contained in the source.
	 */
	public int getNumImages(boolean allowSearch) throws IOException {
		return 1;
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		return null;
	}

	/**
	 * Read the image and returns it as a complete <code>BufferedImage</code>,
	 * using a supplied <code>ImageReadParam</code>.
	 */

	public BufferedImage read(int imageIndex, ImageReadParam param)
			throws IOException {

		// TODO: Maybe need a sort of checkImageIndex(imageIndex)

		Kdu_simple_file_source localRawSource = null;
		Jp2_family_src localFamilySource = null;
		Jpx_source localWrappedSource = null;

		// get a default set of ImageReadParam if needed.
		if (param == null)
			param = getDefaultReadParam();

		// ///////////////////////////////////////////////////////////
		//
		// STEP 1.
		// -------
		// local variables initialization
		//
		// ///////////////////////////////////////////////////////////
		// The destination image properties
		int dstWidth = -1;
		int dstHeight = -1;
		int dstXOffset = 0;
		int dstYOffset = 0;

		// The source region image properties
		int srcRegionWidth = -1;
		int srcRegionHeight = -1;
		int srcRegionXOffset = 0;
		int srcRegionYOffset = 0;

		// The properties of the image we need to load from Kakadu
		int requiredRegionWidth = -1;
		int requiredRegionHeight = -1;
		int requiredRegionXOffset = 0;
		int requiredRegionYOffset = 0;

		// Subsampling Factors */
		int xSubsamplingFactor = -1;
		int ySubsamplingFactor = -1;

		// In some cases, like, as an instance, having xSS != ySS or subsampling
		// factor very high, we need to find a proper subsampling factor prior
		// to read data from kakadu
		int newSubSamplingFactor = 1;

		// boolean used to specify when resampling is required (as an instance,
		// different subsampling factors (xSS != ySS) require resampling)
		boolean resamplingIsRequired = false;

		// ///////////////////////////////////////////////////////////
		//
		// STEP 2.
		// -------
		// parameters management (retrieve user defined readParam and
		// futher initializations)
		//
		// ///////////////////////////////////////////////////////////
		if (!(param instanceof JP2KakaduImageReadParam)) {
			// The parameter is not of JP2KakaduImageReadParam type but
			// simply an ImageReadParam instance (the superclass)
			// we need to build a proper JP2KakaduImageReadParam prior
			// to start parameters parsing.
			JP2KakaduImageReadParam jp2kParam = (JP2KakaduImageReadParam) getDefaultReadParam();
			jp2kParam.intialize(param);
			param = jp2kParam;
		}

		// selected interpolation type
		final int interpolationType = ((JP2KakaduImageReadParam) param)
				.getInterpolationType();

		// //
		//
		// specified quality layers
		//
		// //
		int qualityLayers = ((JP2KakaduImageReadParam) param)
				.getQualityLayers();
		if (qualityLayers != -1) {
			// qualityLayers != -1 means that the user have specified that value
			if (qualityLayers > maxAvailableQualityLayers)
				// correct the user defined qualityLayers parameter if this
				// exceed the
				// max number of available quality layers
				qualityLayers = maxAvailableQualityLayers;
		}

		// //
		// Retrieving Information about Source Region and doing additional
		// intialization operations.
		// //
		final Rectangle srcRegion = param.getSourceRegion();
		if (srcRegion != null) {
			srcRegionWidth = (int) srcRegion.getWidth();
			srcRegionHeight = (int) srcRegion.getHeight();
			srcRegionXOffset = (int) srcRegion.getX();
			srcRegionYOffset = (int) srcRegion.getY();

			// ////////////////////////////////////////////////////////////////
			//
			// Minimum correction for wrong source regions
			//
			// When you do subsampling or source subsetting it might happen that
			// the given source region in the read param is uncorrect, which
			// means it can be or a bit larger than the original file or can
			// begin a bit before original limits.
			//
			// We got to be prepared to handle such case in order to avoid
			// generating ArrayIndexOutOfBoundsException later in the code.
			//
			// ////////////////////////////////////////////////////////////////
			if (srcRegionXOffset < 0)
				srcRegionXOffset = 0;
			if (srcRegionYOffset < 0)
				srcRegionYOffset = 0;

			// initializing destination image properties
			dstWidth = srcRegionWidth;
			dstXOffset = srcRegionXOffset;
			if ((srcRegionXOffset + srcRegionWidth) > width) {
				srcRegionWidth = width - srcRegionXOffset;
			}
			dstHeight = srcRegionHeight;
			dstYOffset = srcRegionYOffset;
			if ((srcRegionYOffset + srcRegionHeight) > height) {
				srcRegionHeight = height - srcRegionYOffset;
			}

		} else {

			// Source Region not specified.
			// Assuming Source Region Dimension equal to Source Image Dimension
			dstWidth = width;
			dstHeight = height;
			dstXOffset = dstYOffset = 0;
			srcRegionWidth = width;
			srcRegionHeight = height;
			srcRegionXOffset = srcRegionYOffset = 0;
		}

		// SubSampling variables initialization
		xSubsamplingFactor = param.getSourceXSubsampling();
		ySubsamplingFactor = param.getSourceYSubsampling();

		// ////////////////////////////////////////////////////////////////////
		//
		// Updating the destination size in compliance with the subSampling
		// parameters
		//
		// ////////////////////////////////////////////////////////////////////
		dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
		dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

		if (srcRegionXOffset != 0) {
			dstXOffset = ((srcRegionXOffset - 1) / xSubsamplingFactor) + 1;
		}
		if (srcRegionYOffset != 0) {
			dstYOffset = ((srcRegionYOffset - 1) / xSubsamplingFactor) + 1;
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// STEP 3.
		// -------
		// check if the image need to be resampled/rescaled and find the proper
		// scale factor as well as the size of the required region we need to
		// provide to kakadu.
		//
		// ////////////////////////////////////////////////////////////////////

		WritableRaster raster;
		try {

			/**
			 * Finding a valid scale factor
			 * 
			 * REMEMBER: xSSF means xSubSamplingFactor. ySSF means
			 * ySubSamplingFactor
			 * 
			 * xSubSamplingFactor (xSSF) and ySubSamplingFactor (ySSF) set as
			 * imageReadParam may be differents. Furthermore, they may be
			 * greater than 2^(DWT_levels of the coded image)
			 * 
			 * For this reason, we need to provide a proper value to the
			 * SetScale method.
			 */
			// Preliminar check: Are xSSF and ySSF different?
			final boolean subSamplingFactorsAreDifferent = (xSubsamplingFactor != ySubsamplingFactor);
			// Let be nSSF the minimum of xSSF and ySSF (They may be equals).
			newSubSamplingFactor = (xSubsamplingFactor <= ySubsamplingFactor) ? xSubsamplingFactor
					: ySubsamplingFactor;
			// if nSSF is greater than the maxSupportedSubSamplingFactor
			// (MaxSSF), it need to be adjusted.
			final boolean changedSubSamplingFactors = (newSubSamplingFactor > maxSupportedSubSamplingFactor);
			if (newSubSamplingFactor > maxSupportedSubSamplingFactor)
				newSubSamplingFactor = maxSupportedSubSamplingFactor;
			resamplingIsRequired = subSamplingFactorsAreDifferent
					|| changedSubSamplingFactors;
			if (!resamplingIsRequired) {
				// xSSF and ySSF are equal and they are not greater than the max
				// supported subsampling factor
				requiredRegionWidth = dstWidth;
				requiredRegionHeight = dstHeight;
				requiredRegionXOffset = dstXOffset;
				requiredRegionYOffset = dstYOffset;
			} else {
				// x and y subsampling factors are different or they are
				// greater than the max supported subsampling factor.
				// We need to find a new subsampling factor to load
				// a proper region.
				newSubSamplingFactor = findOptimalSubSamplingFactor(newSubSamplingFactor);
				requiredRegionWidth = ((srcRegionWidth - 1) / newSubSamplingFactor) + 1;
				requiredRegionHeight = ((srcRegionHeight - 1) / newSubSamplingFactor) + 1;
				if (srcRegionXOffset != 0) {
					requiredRegionXOffset = ((srcRegionXOffset - 1) / newSubSamplingFactor) + 1;
				}
				if (srcRegionYOffset != 0) {
					requiredRegionYOffset = ((srcRegionYOffset - 1) / newSubSamplingFactor) + 1;
				}
			}
			final float scale = 1.0f / newSubSamplingFactor;

			// ////////////////////////////////////////////////////////////////
			//
			// STEP 4.
			// -------
			// Kakadu region compositor initialization:
			// scale setting and required region's size setting.
			//
			// ////////////////////////////////////////////////////////////////
			// Setting required region dimensions
			final Kdu_dims requiredRegionDims = new Kdu_dims();
			requiredRegionDims.Access_size().Set_x(requiredRegionWidth);
			requiredRegionDims.Access_size().Set_y(requiredRegionHeight);
			requiredRegionDims.Access_pos().Set_x(requiredRegionXOffset);
			requiredRegionDims.Access_pos().Set_y(requiredRegionYOffset);

			// ////
			//
			// Raster-related Objects initialization
			//
			// ////
			final int databufferSize = requiredRegionWidth
					* requiredRegionHeight;
			final int[] destinationBuffer = new int[databufferSize];

			// //
			//
			// Create and configure the compositor objects
			//
			// //

			final Kdu_region_compositor compositor = new Kdu_region_compositor();
			final String fileName = fileSource.getAbsolutePath();
			if (!isRawSource) {
				localFamilySource = new Jp2_family_src();
				localWrappedSource = new Jpx_source();

				localFamilySource.Open(fileName);
				final int success = localWrappedSource.Open(localFamilySource,
						true);

				if (success < 0) { // Must open as raw file
					localFamilySource.Close();
					localWrappedSource.Close();
					localRawSource = new Kdu_simple_file_source(fileName);
					compositor.Create(localRawSource);
				} else
					compositor.Create(localWrappedSource);

			} else {
				localRawSource = new Kdu_simple_file_source(fileName);
				compositor.Create(localRawSource);
			}
			compositor.Add_compositing_layer(0, new Kdu_dims(), new Kdu_dims());
			compositor.Set_scale(false, false, false, scale);
			if (qualityLayers != -1) {
				compositor.Set_max_quality_layers(qualityLayers);

			}
			// Setting the required region.
			compositor.Set_buffer_surface(requiredRegionDims);
			final Kdu_compositor_buf compositor_buf = compositor
					.Get_composition_buffer(requiredRegionDims);
			compositor.Refresh();

			// ////////////////////////////////////////////////////////////////
			//
			// STEP 5.
			// -------
			// Data Loading loop
			//
			// ////////////////////////////////////////////////////////////////
			final Kdu_dims decodedRegion = new Kdu_dims();
			while (compositor.Process(databufferSize, decodedRegion)) {

				// System.out.println("pos +" +
				// decodedRegion.Access_pos().Get_x()
				// + " " + decodedRegion.Access_pos().Get_y() + " w "
				// + decodedRegion.Access_size().Get_x() + " h "
				// + decodedRegion.Access_size().Get_y());
				// SinglePixelPackedSampleModel innerSampleModel = new
				// SinglePixelPackedSampleModel(
				// DataBuffer.TYPE_INT, decodedRegion.Access_size()
				// .Get_x(), decodedRegion.Access_size().Get_y(),
				// BITMASKS_RGB);
				//
				// final int new_pixels = decodedRegion.Access_size().Get_x()
				// * decodedRegion.Access_size().Get_y();
				// if (new_pixels == 0)
				// continue;
				// final int[] b = new int[new_pixels];
				// compositor_buf.Get_region(decodedRegion, b);
				//
				// DataBufferInt ib = new DataBufferInt(b, b.length);
				// WritableRaster r = Raster.createWritableRaster(
				// innerSampleModel, ib, null);
				// final BufferedImage bi = new BufferedImage(cm, r, false,
				// null);
				// // processImageUpdate(bi, decodedRegion.Access_pos().Get_x(),
				// // decodedRegion.Access_pos().Get_y(), decodedRegion
				// // .Access_size().Get_x(), decodedRegion
				// // .Access_size().Get_y(), 1, 1, null);
				//
				// ImageIO.write(bi, "jpeg", new File("c:\\zz\\"
				// + decodedRegion.Access_pos().Get_x() + "_"
				// + decodedRegion.Access_pos().Get_y() + "_"
				// + decodedRegion.Access_size().Get_x() + "_"
				// + decodedRegion.Access_size().Get_y() + ".jpeg"));

			}

			if (!compositor.Is_processing_complete())
				throw new RuntimeException("Unable to get the requested region");

			// ////////////////////////////////////////////////////////////////
			//
			// STEP 6.
			// -------
			// Data acquisition and raster creation
			//
			// ////////////////////////////////////////////////////////////////
			requiredRegionDims.Access_pos().Set_x(0);
			requiredRegionDims.Access_pos().Set_y(0);
			compositor_buf.Get_region(requiredRegionDims, destinationBuffer);
			compositor.Delete_buffer(compositor_buf);
			compositor.Native_destroy();
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(sb.toString());

			SampleModel innerSampleModel = null;
			DataBuffer imageBuffer = null;
			switch (nBands) {
			case 3:
				innerSampleModel = new SinglePixelPackedSampleModel(
						DataBuffer.TYPE_INT, requiredRegionWidth,
						requiredRegionHeight, BITMASKS_RGB);
				imageBuffer = new DataBufferInt(destinationBuffer,
						databufferSize);
				break;
			case 1:
				innerSampleModel = cm.createCompatibleSampleModel(
						tileWidth, tileHeight);

//				switch (dataBufferType) {
//				case DataBuffer.TYPE_INT:
					imageBuffer = new DataBufferInt(destinationBuffer,
							databufferSize);
					break;
//				}
			}

			try {
				raster = Raster.createWritableRaster(innerSampleModel,
						imageBuffer, null);
			} catch (RasterFormatException rfe) {
				throw new RuntimeException("Error during raster creation", rfe);
			}

		} catch (KduException e) {
			throw new RuntimeException(
					"Error caused by a Kakadu exception during creation of key objects! ",
					e);
		}

		// ////////////////////////////////////////////////////////////////
		//
		// STEP 7.
		// -------
		// BufferedImage Instantiation
		//
		// ////////////////////////////////////////////////////////////////
		final BufferedImage bi = new BufferedImage(cm, raster, false, null);

		// ////////////////////////////////////////////////////////////////
		//
		// STEP 8 (optional).
		// -------
		// Image resampling if needed (See STEP 3 for additional info)
		//
		// ////////////////////////////////////////////////////////////////

		if (!isRawSource) {
			localWrappedSource.Native_destroy();
			localFamilySource.Native_destroy();

		} else
			localRawSource.Native_destroy();

		if (resamplingIsRequired)
			return warpImage(bi, dstWidth, dstHeight, interpolationType);

		// ImageIO.write(bi, "tiff", new File("c:\\sample\\tile"
		// + param.getSourceXSubsampling() + "_"
		// + param.getSourceYSubsampling() + "_" + param.getSourceRegion()
		// + ".tiff"));

		return bi;
	}

	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		///////////////////////////////////////////////////////////////////////
		//
		// Reset this reader as needed.
		//
		///////////////////////////////////////////////////////////////////////
		if (this.imageInputStream != null) {
			reset();
			imageInputStream = null;
		}
		
		
		///////////////////////////////////////////////////////////////////////
		//
		// Checks on input object as a valid file
		//
		///////////////////////////////////////////////////////////////////////
		if (input == null)
			throw new NullPointerException("The provided input is null!");
		
		if (input instanceof File) {
			fileSource = (File) input;
		} else 
			// Checking if the provided input is a FileImageInputStreamExt
			if (input instanceof FileImageInputStreamExt) {
				fileSource = ((FileImageInputStreamExt) input).getFile();
			
		} else 
			// Checking if the provided input is a URL
			if (input instanceof URL) {
				final URL tempURL = (URL) input;
				if (tempURL.getProtocol().equalsIgnoreCase("file")) {
	
					try {
						fileSource = new File(URLDecoder.decode(tempURL.getFile(),
								"UTF-8"));
					} catch (IOException e) {
						throw new RuntimeException("Not a Valid Input", e);
					}
			}
		}
			else
				throw new IllegalArgumentException("Provided input object is null or invalid");
		////
		//
		// Check that the input file is a valid file
		//
		////
		if(!fileSource.exists()||fileSource.isDirectory()||!fileSource.canRead()){
			final StringBuilder buff= new StringBuilder("Invalid input file provided");
			buff.append("exists: ").append(fileSource.exists()).append("\n");
			buff.append("isDirectory: ").append(fileSource.isDirectory()).append("\n");
			buff.append("canRead: ").append(fileSource.canRead()).append("\n");
			throw new IllegalArgumentException();
		}	
		Kdu_simple_file_source rawSource = null; // Must be disposed last
		familySource = new Jp2_family_src(); // Dispose last
		wrappedSource = new Jpx_source(); // Dispose in the middle
		Kdu_region_compositor compositor = null; // Must be disposed first

		try {
			if (fileSource != null) {

				// Open input file as raw codestream or a JP2/JPX file
				final String fileName = fileSource.getAbsolutePath();
				familySource.Open(fileName); // Generates an error if file
				// doesn't exist
				final int success = wrappedSource.Open(familySource, true);

				if (success < 0) { // Must open as raw file
					familySource.Close();
					wrappedSource.Close();
					rawSource = new Kdu_simple_file_source(fileName);
				}

				// Create and configure the compositor object
				compositor = new Kdu_region_compositor();
				if (rawSource != null) {
					compositor.Create(rawSource);
					isRawSource = true;
				} else
					compositor.Create(wrappedSource);

				compositor.Add_compositing_layer(0, new Kdu_dims(),
						new Kdu_dims());
				compositor.Set_scale(false, false, false, 1.0F);
				if (codestream == null) {
					long activeCodeStream = compositor.Get_next_codestream(0,
							true, true);
					codestream = compositor.Access_codestream(activeCodeStream);
				}

				// Initializing size-related properties (Image dims, Tiles)
				final Kdu_dims imageDims = new Kdu_dims();
				codestream.Access_siz().Finalize_all();
				codestream.Get_dims(-1, imageDims, false);
				tileSize = new Kdu_dims();
				codestream.Get_tile_dims(new Kdu_coords(0, 0), -1, tileSize);

//				tileWidth = tileSize.Access_size().Get_x();
//				tileHeight = tileSize.Access_size().Get_y();
				tileWidth = 512;
				tileHeight = 512;
				width = imageDims.Access_size().Get_x();
				height = imageDims.Access_size().Get_y();
				// codestream.Set_persistent();

				// Initializing Resolution levels and Quality Layers
				sourceDWTLevels = codestream.Get_min_dwt_levels();
				maxSupportedSubSamplingFactor = 1 << sourceDWTLevels;
				maxAvailableQualityLayers = retrieveMaxQualityLayers();
				try {
					setSampleModelAndColorModel();
				} catch (IOException ioe) {
					throw new RuntimeException(
							"Error during setting SampleModel and ColorModel",
							ioe);
				}
			} else
				throw new NullPointerException("The provided input is null!");

		} catch (KduException e) {
			throw new RuntimeException(
					"Error caused by a Kakadu exception during creation of key objects! ",
					e);
		}

		
		if (isRawSource)
			if (rawSource != null)
				rawSource.Native_destroy();
		
		super.setInput(input, seekForwardOnly, ignoreMetadata);
	}

	/**
	 * Disposes all the resources, native and non, used by this
	 * {@link ImageReader} subclass.
	 */
	public void dispose() {
		// it actually does nothing but it might turn out to be iseful in future
		// releases of ImageIO
		super.dispose();

		// /////////////////////////////////////////////////////////////////////
		//
		// Freeing up native resources.
		//
		// /////////////////////////////////////////////////////////////////////
		wrappedSource.Native_destroy();
		familySource.Native_destroy();

		// /////////////////////////////////////////////////////////////////////
		//
		// Closing the passes stream (it was mostly unused.)
		//
		// /////////////////////////////////////////////////////////////////////
		if (imageInputStream != null) {
			try {
				imageInputStream.close();
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);

			}
		}

	}

	/**
	 * Returns the height of a tile
	 */
	public int getTileHeight(int imageIndex) throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("tileHeight:").append(
					Integer.toString(tileHeight)).toString());
		return tileHeight;
	}

	/**
	 * Returns the width of a tile
	 */
	public int getTileWidth(int imageIndex) throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("tileWidth:").append(
					Integer.toString(tileWidth)).toString());
		return tileWidth;
	}

	private void setSampleModelAndColorModel() throws IOException {
		try {
			if (sm != null && cm != null)
				return;
			// Getting dataset main properties
			nBands = codestream.Get_num_components();
			bitDepth = codestream.Get_bit_depth(0);
			final int xSize = getWidth(0);
			final int ySize = getHeight(0);

			// Setting SampleModel and ColorModel
			switch (nBands) {
			case 1:
				final ColorSpace cs = ColorSpace
						.getInstance(ColorSpace.CS_GRAY);
				int bits[] = new int[] { bitDepth };

				// TODO: Handle more cases
				switch (bitDepth) {
				case 8:
					dataBufferType = DataBuffer.TYPE_BYTE;
					break;
				case 16:
					dataBufferType = DataBuffer.TYPE_USHORT;
					break;
				case 14:
					dataBufferType = DataBuffer.TYPE_USHORT;
					break;
				case 32:
					dataBufferType = DataBuffer.TYPE_INT;
					break;
				default:
					// TODO: Need to change this case
					dataBufferType = DataBuffer.TYPE_INT;
					break;
				}
				cm = new ComponentColorModel(cs, bits, false, false,
						Transparency.OPAQUE, dataBufferType);
				sm = cm.createCompatibleSampleModel(tileWidth, tileHeight);
				break;
			case 3:
				sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,
						xSize, ySize, BITMASKS_RGB);
				cm = new DirectColorModel(32, BITMASKS_RGB[0], BITMASKS_RGB[1],
						BITMASKS_RGB[2]);
				break;
			default:
				// TODO: Add more bands management
				throw new UnsupportedOperationException("Insupported image type");
			}

		} catch (KduException e) {
			throw new RuntimeException(
					"Error caused by a Kakadu exception during codestream properties retrieval! ",
					e);
		}
	}

	/**
	 * Transforms the provided <code>BufferedImage</code> and returns a new
	 * one in compliance with the required destination image properties,
	 * adopting the specified interpolation algorithm
	 * 
	 * @param bi
	 *            the original BufferedImage
	 * @param dstWidth
	 *            the required destination image width
	 * @param dstHeight
	 *            the required destination image heigth
	 * @param interpolationType
	 *            the specified interpolation type
	 * @return a <code>BufferedImage</code> having size = dstWidth*dstHeight
	 *         which is the result of the WarpAffineresu.
	 */
	private BufferedImage warpImage(BufferedImage bi, final int dstWidth,
			final int dstHeight, final int interpolationType) {

		final WritableRaster raster = cm.createCompatibleWritableRaster(
				dstWidth, dstHeight);

		final BufferedImage finalImage = new BufferedImage(cm, raster, false,
				null);
		final Graphics2D gc2D = finalImage.createGraphics();
		gc2D.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		gc2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		gc2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_SPEED);
		gc2D
				.setRenderingHint(
						RenderingHints.KEY_INTERPOLATION,
						interpolationType == JP2KakaduImageReadParam.INTERPOLATION_NEAREST ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
								: RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gc2D.drawImage(bi, 0, 0, dstWidth, dstHeight, 0, 0, bi.getWidth(), bi
				.getHeight(), null);
		gc2D.dispose();
		bi.flush();
		bi = null;

		return finalImage;

	}

	/**
	 * Provides to retrieve the optimal subsampling factor, given a specified
	 * subsampling factor as input parameter. Let iSS be the input subsampling
	 * factor and let L be the number of available resolution levels
	 * 
	 * The optimal subsampling factor is oSS = 2^level where level is not
	 * greater than L and oSS is not greater than iSS
	 * 
	 * @param newSubSamplingFactor
	 *            the specified subsampling factor for which we need to find an
	 *            optimal subsampling factor
	 * @return the optimal subsampling factor
	 */
	private int findOptimalSubSamplingFactor(final int newSubSamplingFactor) {
		// Within a loop, using a local variable instead of an instance field
		// is preferred to improve performances.
		final int levels = sourceDWTLevels;
		int optimalSubSamplingFactor = 1;

		// finding the available subsampling factors from the number of
		// resolution levels
		for (int i = 0; i < levels; i++) {
			// double the subSamplingFactor until it is belower than the
			// input subSamplingFactor
			if (optimalSubSamplingFactor < newSubSamplingFactor)
				optimalSubSamplingFactor = 1 << i;
			// if the calculated subSamplingFactor is greater than the input
			// subSamplingFactor, we need to step back by halving it.
			else if (optimalSubSamplingFactor > newSubSamplingFactor) {
				optimalSubSamplingFactor = optimalSubSamplingFactor >> 1;
				break;
			} else if (optimalSubSamplingFactor == newSubSamplingFactor)
				break;
		}
		return optimalSubSamplingFactor;
	}

	/**
	 * Build a default {@link JP2KakaduImageReadParam}
	 */
	public ImageReadParam getDefaultReadParam() {
		return new JP2KakaduImageReadParam();
	}

	/**
	 * provides to retrieve the number of max quality layers contained in the
	 * source. This method is executed only during a <code>setInput</code>
	 * call which also provides to set the
	 * <code>maxAvailableQualityLayers</code> field which can be queried
	 * afterwards using the <code>getMaxAvailableQualityLayers</code> getter
	 * 
	 * @return the number of max available quality layers
	 * 
	 */
	private int retrieveMaxQualityLayers() {
		// To retrieve the number of quality layers,we need to access to a tile.
		// To do this, we use a commonly used trick: we read a tiny region of
		// the source image.

		Kdu_simple_file_source localRawSource = null;
		Jp2_family_src localFamilySource = null;
		Jpx_source localWrappedSource = null;

		Kdu_dims smallDims = new Kdu_dims();
		int qualityLayers = 1;

		try {

			// Initializing the properties of the required region of the source
			// image
			smallDims.Access_size().Set_x(2);
			smallDims.Access_size().Set_y(2);
			smallDims.Access_pos().Set_x(0);
			smallDims.Access_pos().Set_y(0);

			// setting the required region
			// Create and configure the compositor object
			Kdu_region_compositor compositor = new Kdu_region_compositor();

			if (isRawSource) {
				localFamilySource = new Jp2_family_src();
				localWrappedSource = new Jpx_source();
				final String fileName = fileSource.getAbsolutePath();
				localFamilySource.Open(fileName);
				final int success = localWrappedSource.Open(localFamilySource,
						true);

				if (success < 0) { // Must open as raw file
					localFamilySource.Close();
					localWrappedSource.Close();
					localRawSource = new Kdu_simple_file_source(fileName);
				}

				if (localRawSource != null)
					compositor.Create(localRawSource);
			} else
				compositor.Create(wrappedSource);

			compositor.Add_compositing_layer(0, new Kdu_dims(), new Kdu_dims());
			compositor.Set_scale(false, false, false, 1.0F);
			compositor.Set_buffer_surface(smallDims);
			Kdu_dims decodedRegion = new Kdu_dims();
			int newPixels = -1;

			// pixels loading
			while (compositor.Process(4, decodedRegion)) {
				newPixels = decodedRegion.Access_size().Get_x()
						* decodedRegion.Access_size().Get_y();
				if (newPixels == 0)
					continue;

				// At least a pixel has been read. Thus we can get the
				// number of max available quality layers.
				qualityLayers = compositor.Get_max_available_quality_layers();
				break;
			}
			compositor.Native_destroy();
		} catch (KduException e) {
			// TODO better management?
			qualityLayers = -1;
		}

		if (isRawSource) {
			localWrappedSource.Native_destroy();
			localFamilySource.Native_destroy();
			if (localRawSource != null)
				localRawSource.Native_destroy();
		}

		return qualityLayers;
	}

	/**
	 * returns the number of max available quality layers
	 * 
	 * @return the number of max available quality layers.
	 */
	public int getMaxAvailableQualityLayers() {
		return maxAvailableQualityLayers;
	}

	public int getSourceDWTLevels() {
		return sourceDWTLevels;
	}
}