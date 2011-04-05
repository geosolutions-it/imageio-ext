/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jpeg;

import it.geosolutions.imageio.imageioimpl.EnhancedImageReadParam;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

/**
 * {@link JpegJMagickImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from JPEG files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JpegJMagickImageReader extends ImageReader {

	protected void finalize() throws Throwable {
		dispose();
	}

	public ImageReadParam getDefaultReadParam() {
		return new JpegJMagickImageReaderReadParam();
	}

	/**
	 * Implementation of {@link ImageReadParam} for this
	 * {@link JpegJMagickImageReader}. Actually we are using
	 * {@link CloneableImageReadParam} since ImageMagick guys claim that their
	 * library is thread safe but the moment I am getting nasty errors if I try
	 * to use multithreading hence I am locking the reader up.
	 * 
	 * @author Simone Giannecchini, GeoSolutions.
	 * @author Daniele Romagnoli, GeoSolutions.
	 */
	public static class JpegJMagickImageReaderReadParam extends EnhancedImageReadParam {

		public String toString() {
			final StringBuilder buff = new StringBuilder();
			buff.append("JpegJMagickImageReaderReadParam={").append("\n");
			if (this.getSourceRegion() != null)
				buff
						.append(
								"SourceRegion"
										+ this.getSourceMaxProgressivePass())
						.append("\n");
			buff.append("sx=").append(this.getSourceXSubsampling()).append(
					" sy=").append(this.getSourceYSubsampling());
			buff.append("}");
			return buff.toString();
		}

		/**
		 * Deep copy this instance of {@link JpegJMagickImageReaderReadParam};
		 */
		public Object clone() throws CloneNotSupportedException {
			final JpegJMagickImageReaderReadParam retVal = new JpegJMagickImageReaderReadParam();
			retVal.setInterpolationType(this.getInterpolationType());
			retVal.setController(this.getController());
			retVal.setDestination(getDestination());
			retVal.setDestinationBands(getDestinationBands());
			retVal.setDestinationOffset(getDestinationOffset());
			retVal.setDestinationType(getDestinationType());
			retVal.setSourceBands(getSourceBands());
			retVal.setSourceProgressivePasses(getSourceMinProgressivePass(),
					getSourceNumProgressivePasses());
			retVal.setSourceRegion(getSourceRegion());
			retVal.setSourceSubsampling(getSourceXSubsampling(),
					getSourceYSubsampling(), getSubsamplingXOffset(),
					getSubsamplingYOffset());
			return retVal;
		}

		public static final int INTERPOLATION_NEAREST = 1;

		public static final int INTERPOLATION_BILINEAR = 2;

		private int interpolationType;

		/** Constructs a default instance of <code>JP2KakaduImageReadParam</code>. */
		public JpegJMagickImageReaderReadParam() {
			super();
			interpolationType = INTERPOLATION_NEAREST;
		}

		/**
		 * Gets <code>InterpolationType</code>.
		 * 
		 * @return the interpolation algorithm which will be used when
		 *         imageMagick need to be warped
		 */
		public final int getInterpolationType() {
			return interpolationType;
		}

		/**
		 * Sets <code>InterpolationType</code>.
		 * 
		 * @param interpolationType
		 *            the interpolation type used during <code>WarpAffine</code>
		 *            operation
		 * 
		 * interpolationType should be one of: -<em>INTERPOLATION_NEAREST</em> -<em>INTERPOLATION_BILINEAR</em> -<em>INTERPOLATION_BICUBIC</em> -<em>INTERPOLATION_BICUBIC2</em>
		 */
		public final void setInterpolationType(int interpolationType) {
			this.interpolationType = interpolationType;
		}

		/** Initilize this JpegJMagickImageReaderReadParam */
		protected void intialize(ImageReadParam param) {
			if (param.hasController()) {
				setController(param.getController());
			}
			setSourceRegion(param.getSourceRegion());
			setSourceBands(param.getSourceBands());
			setDestinationBands(param.getDestinationBands());
			setDestination(param.getDestination());
			setDestinationOffset(param.getDestinationOffset());
			setSourceSubsampling(param.getSourceXSubsampling(), param
					.getSourceYSubsampling(), param.getSubsamplingXOffset(),
					param.getSubsamplingYOffset());
			setDestinationType(param.getDestinationType());

		}
	}

	/**
	 * {@link MagickImageAdapter} containes code to adapt a {@link MagickImage}
	 * to a {@link BufferedImage}.
	 * 
	 * @author Simone Giannecchini, GeoSolutions.
	 * 
	 */
	public static class MagickImageAdapter {
		/**
		 * Caches the layout for the underlying {@link #imageMagick}.
		 */
		private final ImageLayout layout;

		/**
		 * {@link MagickImage} to adapt to the {@link BufferedImage} interface.
		 */
		private final MagickImage imageMagick;

		/**
		 * Constructor. Let us build a {@link MagickImageAdapter} adapter for
		 * the specified {@link ImageInfo} object.
		 * 
		 * @param info
		 * @throws MagickException
		 */
		public MagickImageAdapter(final ImageInfo info) throws MagickException {

			// TODO if we invert them JVM crashes, not nice, not at all.
			imageMagick = new MagickImage(info);
			final Dimension dim = imageMagick.getDimension();
			final int w = dim.width;
			final int h = dim.height;
			final ImageLayout layout = new ImageLayout();
			layout.setWidth(w).setHeight(h).setSampleModel(
					new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, h, h,
							3, 3 * dim.width, new int[] { 0, 1, 2 }))
					.setColorModel(
							new ComponentColorModel(ColorSpace
									.getInstance(ColorSpace.CS_LINEAR_RGB),
									false, false, Transparency.OPAQUE,
									DataBuffer.TYPE_BYTE));
			this.layout = layout;

		}

		public ImageLayout getLayout() {
			return layout;
		}

		public void dispose() {

			this.imageMagick.destroyImages();
		}

		public String toString() {
			final StringBuilder buff = new StringBuilder();
			buff.append("MagickImageAdapter={").append("\n");
			if (this.layout != null)
				buff.append("ImageLayout " + this.layout.toString()).append(
						"\n");
			buff.append("sx=").append(this.imageMagick.toString());
			buff.append("}");
			return buff.toString();
		}

		/**
		 * @param magickImage
		 * @return
		 * @throws MagickException
		 */
		public static WritableRaster JMagickToWritableRaster(
				MagickImage magickImage) throws IOException {
			try {

				final Dimension dim = magickImage.getDimension();
				final int size = dim.width * dim.height;
				byte[] pixxels = new byte[size * 3];
				magickImage.dispatchImage(0, 0, dim.width, dim.height, "RGB",
						pixxels);

				final SampleModel sm = new PixelInterleavedSampleModel(
						DataBuffer.TYPE_BYTE, dim.width, dim.height, 3,
						3 * dim.width, new int[] { 0, 1, 2 });

				final WritableRaster raster = Raster.createWritableRaster(sm,
						new DataBufferByte(pixxels, size, 0), null);
				return raster;
			} catch (MagickException e) {
				final IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}

		/**
		 * Get a <code>BufferedImage</code> from a {@link MagickImage}
		 * 
		 * @param magickImage
		 *            The source {@link MagickImage}
		 * @return a <code>BufferedImage</code> from a {@link MagickImage}
		 * 
		 * @throws IOException
		 */
		public static BufferedImage magickImageToBufferedImage(
				MagickImage magickImage) throws IOException {

			final WritableRaster raster = MagickImageAdapter
					.JMagickToWritableRaster(magickImage);
			ColorModel cm = new ComponentColorModel(ColorSpace
					.getInstance(ColorSpace.CS_LINEAR_RGB), false, false,
					Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
			return new BufferedImage(cm, raster, false, null);

		}

		public static BufferedImage magickImageToBufferedImage(
				MagickImageAdapter image, Rectangle srcRegion, int dstWidth,
				int dstHeight) throws IOException {
			try {
				MagickImage im = srcRegion != null ? image.imageMagick
						.cropImage(srcRegion) : image.imageMagick;
				final Dimension dim = im.getDimension();
				im = (dim.width != dstWidth || dim.height != dstHeight) ? im
						.sampleImage(dstWidth, dstHeight).sampleImage(dstWidth,
								dstHeight) : im;
				return magickImageToBufferedImage(im);
			} catch (MagickException e) {
				final IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jpeg");

	/** The source of the image*/
	private File sourceFile;

	public JpegJMagickImageReader(JpegJMagickImageReaderSpi originatingProvider) {
		super(originatingProvider);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("JpegJMagickImageReader Constructor");

	}

	/**
	 * List of the {@link ImageLayout}s of the various images contained in
	 * {@link #sourceFile}.
	 */
	private final List<MagickImageAdapter> imagesLayouts =Collections.synchronizedList(new ArrayList<MagickImageAdapter>());

	/**
	 * Returns the height in pixels of the given image within the input source.
	 * 
	 * @param imageIndex
	 *            the index of the image to be queried.
	 * @return the height of the image, as an int.
	 */
	public int getHeight(int imageIndex) throws IOException {
		synchronized (imagesLayouts) {
			checkImageIndex(imageIndex);
			return imagesLayouts.get(imageIndex).getLayout().getHeight(null);
		}
	}

	/**
	 * Returns the width in pixels of the given image within the input source.
	 * 
	 * @param imageIndex
	 *            the index of the image to be queried.
	 * @return the width of the image, as an int.
	 */
	public int getWidth(int imageIndex) throws IOException {
		synchronized (imagesLayouts) {
			checkImageIndex(imageIndex);
			return  imagesLayouts.get(imageIndex).getLayout().getWidth(null);
		}
	}

	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
		synchronized (imagesLayouts) {
			checkImageIndex(imageIndex);
			final ImageLayout layout = (imagesLayouts.get(imageIndex)).getLayout();
			return Collections.singletonList(
					new ImageTypeSpecifier(layout.getColorModel(null), layout.getSampleModel(null))).iterator();
		}

	}

	public int getNumImages(boolean allowSearch) throws IOException {
		// @todo we need to change this
		return 1;
	}

	/**
	 * Actually, this method is not supported and it throws an
	 * <code>UnsupportedOperationException</code>
	 */
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Actually, this method is not supported and it throws an
	 * <code>UnsupportedOperationException</code>
	 */
	public IIOMetadata getStreamMetadata() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Read the imageMagick and returns it as a complete
	 * <code>BufferedImage</code>, using a supplied
	 * <code>ImageReadParam</code>.
	 * 
	 * @param imageIndex
	 *            the index of the image to be retrieved.
	 * @param param
	 *            an <code>ImageReadParam</code> used to control the reading
	 *            process, or null.
	 */
	public BufferedImage read(int imageIndex, ImageReadParam param)
			throws IOException {
		synchronized (imagesLayouts) {
			checkImageIndex(imageIndex);
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Requesting imageMagick at index " + imageIndex
						+ " with  ImageReadParam=" + param.toString());

			// ///////////////////////////////////////////////////////////
			//
			// STEP 1.
			// -------
			// local variables initialization
			//
			// ///////////////////////////////////////////////////////////
			// width and height for this imageMagick
			int width = 0;
			int height = 0;
			final MagickImageAdapter im = ((MagickImageAdapter) imagesLayouts.get(imageIndex));
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Selected imageMagick adapter " + im.toString());
			final ImageLayout layout = im.getLayout();

			width = layout.getWidth(null);
			height = layout.getHeight(null);

			// get a default set of ImageReadParam if needed.
			if (param == null)
				param = getDefaultReadParam();

			// The destination imageMagick properties
			int dstWidth = -1;
			int dstHeight = -1;
			// int dstXOffset = 0;
			// int dstYOffset = 0;

			// The source region imageMagick properties
			int srcRegionWidth = -1;
			int srcRegionHeight = -1;
			int srcRegionXOffset = 0;
			int srcRegionYOffset = 0;

			// Subsampling Factors */
			int xSubsamplingFactor = -1;
			int ySubsamplingFactor = -1;

			// ///////////////////////////////////////////////////////////
			//
			// STEP 2.
			// -------
			// parameters management (retrieve user defined readParam and
			// futher initializations)
			//
			// ///////////////////////////////////////////////////////////
			// //
			// Retrieving Information about Source Region and doing additional
			// intialization operations.
			// //
			Rectangle srcRegion = param.getSourceRegion();
			if (srcRegion != null) {
				srcRegionWidth = (int) srcRegion.getWidth();
				srcRegionHeight = (int) srcRegion.getHeight();
				srcRegionXOffset = (int) srcRegion.getX();
				srcRegionYOffset = (int) srcRegion.getY();

				// ////////////////////////////////////////////////////////////////
				//
				// Minimum correction for wrong source regions
				//
				// When you do subsampling or source subsetting it might happen
				// that
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

				// initializing destination imageMagick properties
				dstWidth = srcRegionWidth;
				if ((srcRegionXOffset + srcRegionWidth) > width) {
					srcRegionWidth = width - srcRegionXOffset;
				}
				dstHeight = srcRegionHeight;
				if ((srcRegionYOffset + srcRegionHeight) > height) {
					srcRegionHeight = height - srcRegionYOffset;
				}
				// creating a correct source region
				srcRegion = new Rectangle(srcRegionXOffset, srcRegionYOffset,
						srcRegionWidth, srcRegionHeight);
			} else {

				// Source Region not specified.
				// Assuming Source Region Dimension equal to Source Image
				// Dimension
				dstWidth = width;
				dstHeight = height;
				// dstXOffset = dstYOffset = 0;
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

			// ////////////////////////////////////////////////////////////////
			//
			// STEP 3.
			// -------
			// BufferedImage creation
			//
			// ////////////////////////////////////////////////////////////////
			return MagickImageAdapter.magickImageToBufferedImage(im, srcRegion,dstWidth, dstHeight);
		}
	}

	/**
	 * Check if the provided imageIndex is valid
	 * 
	 * @param imageIndex
	 *            the image index to be checked
	 * @throws IOException
	 */
	private void checkImageIndex(int imageIndex) throws IOException {
		if (imageIndex > 0)
			throw new IndexOutOfBoundsException();
		assert Thread.holdsLock(imagesLayouts);
		if (imagesLayouts.size() - 1 < imageIndex) {
			try {
				imagesLayouts
						.add(new JpegJMagickImageReader.MagickImageAdapter(
								new ImageInfo(sourceFile.getAbsolutePath())));

			} catch (MagickException e) {
				final IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
	}

	/**
	 * Sets the input source to use to the given <code>Object</code>, usually
	 * a <code>File</code> or a <code>FileImageInputStreamExt</code>
	 */
	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		setInput(input);
		super.setInput(sourceFile, seekForwardOnly, ignoreMetadata);
	}

	/**
	 * Sets the input source to use to the given <code>Object</code>, usually
	 * a <code>File</code> or a <code>FileImageInputStreamExt</code>
	 */
	public void setInput(Object input, boolean seekForwardOnly) {
		setInput(input);
		super.setInput(sourceFile, seekForwardOnly);
	}

	/**
	 * Sets the input source to use to the given <code>Object</code>, usually
	 * a <code>File</code> or a <code>FileImageInputStreamExt</code>
	 */
	public void setInput(Object input) {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("setInput on object " + input.toString());
		if (input instanceof File) {
			sourceFile = (File) input;
		} else if (input instanceof FileImageInputStreamExt) {
			FileImageInputStreamExt imageIn = (FileImageInputStreamExt) input;
			sourceFile = imageIn.getFile();
		} else
			throw new IllegalArgumentException("The input type provided is not supported");
		
	}

	/**
	 * Allows any resources held by this object to be released.
	 */
	public void dispose() {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Disposing JpegJMagickImageReader");
		synchronized (imagesLayouts) {
			final Iterator<MagickImageAdapter> it = imagesLayouts.iterator();
			while (it.hasNext()) {
				final MagickImageAdapter img = (MagickImageAdapter) it.next();
				img.dispose();
			}
		}
		super.dispose();
	}
}
