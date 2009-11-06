/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.swan;

import it.geosolutions.imageio.plugins.swan.raster.SwanRaster;
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RasterFactory;

import org.apache.xmlbeans.XmlException;

import com.sun.media.imageioimpl.common.ImageUtil;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class SwanImageReader extends ImageReader {
	/** Logger. */
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.swan");

	/** Minimum size of a certain file source that neds tiling. */
	private static final int MIN_SIZE_NEED_TILING = 5242880; // 5 MByte

	/** Defaul tile size. */
	private static final int DEFAULT_TILE_SIZE = 1048576 / 2; // 1 MByte

	/** Image Dimensions */
	private int width = -1;

	/** Image Dimensions */
	private int height = -1;

	private SwanRaster rasterReader = null;

	/** The {@link imageInputStream} associated to this reader. */
	private ImageInputStream imageInputStream = null;

	/** the tileWidth of the image */
	private int tileWidth;

	/** the tileHeight of the image */
	private int tileHeight;

	/** the number of images belonging the current source */
	private int numImages;

	/** The {@link SwanStreamMetadata} associated to this reader. */
	private SwanStreamMetadata streamMetadata;

	/**
	 * Constructor.
	 * 
	 * It builts up an {@link SwanImageReader} by providing an
	 * {@link SwanImageReaderSpi}
	 * 
	 * @param originatingProvider
	 */
	public SwanImageReader(SwanImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	public int getHeight(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return height;
	}

	public Iterator getImageTypes(int imageIndex) throws IOException {
		final List l = new java.util.ArrayList();
		
		final boolean isBiComponentQuantity = rasterReader.isBiComponentQuantity(imageIndex);
	
		final SampleModel sm = getSampleModel(isBiComponentQuantity);
		final ColorModel cm = retrieveColorModel(sm);
				
		ImageTypeSpecifier imageType = new ImageTypeSpecifier(cm, sm);
		l.add(imageType);

		return l.iterator();
	}

	private SampleModel getSampleModel(boolean isBiComponentQuantity) {
		int nBands = isBiComponentQuantity?2:1; 
		
		// bands variables
		final int[] banks = new int[nBands];
		final int[] offsets = new int[nBands];
		for (int band = 0; band < nBands; band++) {
			banks[band] = band;
			offsets[band] = 0;
		}

		// Variable used to specify the data type for the storing samples
		// of the SampleModel
		final SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height,
				width, banks, offsets);
		return sm;
	}

	public static ColorModel retrieveColorModel(final SampleModel sm) {
		final int nBands = sm.getNumBands();
		ColorModel cm = null;
		ColorSpace cs = null;
		if (nBands > 1) {
			// Number of Bands > 1.
			// ImageUtil.createColorModel provides to Creates a
			// ColorModel that may be used with the specified
			// SampleModel
			cm = ImageUtil.createColorModel(sm);
			if (cm == null)
				LOGGER.info("There are no ColorModels found");

		} else {

			// Just one band. Using the built-in Gray Scale Color Space
			cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			cm = RasterFactory.createComponentColorModel(DataBuffer.TYPE_FLOAT, // dataType
					cs, // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency
		}
		return cm;
	}
	
	public int getNumImages(boolean allowSearch) throws IOException {
		return numImages;
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		if (streamMetadata == null)
			streamMetadata = new SwanStreamMetadata(this.rasterReader);
		return streamMetadata;
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return new SwanImageMetadata(this.rasterReader, imageIndex);
	}

	public int getWidth(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return width;
	}

	public BufferedImage read(int imageIndex, ImageReadParam param)
			throws IOException {
		checkImageIndex(imageIndex);
		if (param == null) {
			param = getDefaultReadParam();
		}
		// The max allowed long value is 9.223.372.036.854.775.807
		// Should be sufficient :)
		final long imageStartAt = retrieveImageStart(imageIndex);
		final boolean isBicomponent = rasterReader.isBiComponentQuantity(imageIndex);
		return rasterReader.readRaster(imageStartAt, param, isBicomponent);
	}

	/**
	 * Given an imageIndex, returns the position within the stream where related
	 * data starts
	 * 
	 * @param imageIndex
	 *            The index of the required raster
	 * @return The position within the stream where required data starts.
	 */
	private long retrieveImageStart(final int imageIndex) {
		checkImageIndex(imageIndex);
		final int basicStep = rasterReader.getDatasetForecastSize();
		long imageStartAt = 0;
		for (int i=0;i<imageIndex;i++){
			imageStartAt+=basicStep;
			if (rasterReader.isBiComponentQuantity(i))
				imageStartAt+=basicStep;
		}
		return imageStartAt;
// return rasterReader.getDatasetForecastSize() * imageIndex;
	}

	/**
	 * This auxiliary method provide to retrieve a proper imageIndex to access
	 * the required raster contained within the input file which may have data
	 * for several forecast and several SWAN output quantities.
	 * 
	 * @param requiredForecast
	 *            the forecast for which we need data. (starts from zero)
	 * @param requiredQuantity
	 *            the quantity for which we need data. (starts from zero)
	 * @return an imageIndex needed to access the required raster.
	 */
	public int getImageIndexFromTauAndDatasets(final int requiredForecast,
			final int requiredQuantity) {
		// Output file contains data stored in this order:
		// 1st forecast: 1st output quantity, 2nd output quantity, ...
		// 2nd forecast: 1st output quantity, 2nd output quantity, ...
		// ...
		// Nth forecast: 1st output quantity, 2nd output quantity, ...
		if (rasterReader == null) {
			throw new IllegalArgumentException("You should set a proper input");
		}

		final int nDatasets = rasterReader.getNDatasets();
		final int nTau = rasterReader.getNTaus();

		if (requiredForecast < 0 || requiredQuantity < 0
				|| requiredForecast > (nTau - 1)
				|| requiredQuantity > (nDatasets - 1)) {
			// TODO: maybe we need to specify a better error message
			throw new IllegalArgumentException("specified index are not valid");
		}

		return ((nDatasets * requiredForecast) + requiredQuantity);
	}

	/**
	 * Return the index related to a specified SWAN quantity 
	 * 
	 * @param quantityName
	 *            the name of the required SWAN quantity
	 * @return
	 */
	public int getIndexFromQuantityName(final String quantityName) {
		if (rasterReader != null) 
			return rasterReader.getIndexFromQuantityName(quantityName);
		return -1;
	}

	public void setInput(Object input) {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Setting Input");
		// ////////////////////////////////////////////////////////////////////
		//
		// Reset the state of this reader
		//
		// Prior to set a new input, I need to do a pre-emptive reset in order
		// to clear any value-object related to the previous input.
		// ////////////////////////////////////////////////////////////////////
		if (this.imageInputStream != null) {
			reset();
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Resetting old stream");

		}

		// ////////////////////////////////////////////////////////////////////
		//
		// ImageInputStream?
		// 
		// ////////////////////////////////////////////////////////////////////
		if (input instanceof ImageInputStream)
			imageInputStream = (ImageInputStream) input;
		else

		// ////////////////////////////////////////////////////////////////////
		//
		// URL?
		// 
		// ////////////////////////////////////////////////////////////////////
		if (input instanceof URL) {
			final URL testUrl = (URL) input;
			if (!testUrl.getProtocol().equalsIgnoreCase("file")) {
				// is not a file let's reject it
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Unsupported URL provided as input!");
				throw new IllegalArgumentException(
						"Unsupported URL provided as input!");
			}
			// now we know it is pointing to a file
			// let's see if it exists
			final File inFile = Utilities.urlToFile(testUrl);
			if (!inFile.exists()) {
				// is not a file let's reject it
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Input file does not exists!");
				throw new IllegalArgumentException(
						"Input file does not exists!");
			}
		} else

		// ////////////////////////////////////////////////////////////////////
		//
		// FILE?
		// 
		// ////////////////////////////////////////////////////////////////////
		if (input instanceof File) {
			final File inFile = (File) input;
			if (!inFile.exists()) {
				// is not a file let's reject it
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Input file does not exists!");
				throw new IllegalArgumentException(
						"Input file does not exists!");
			}
		} else {
			// is not something we can decode
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.severe("Input is not decodable!");
			throw new IllegalArgumentException("Input is not decodable!");
		}

		if (imageInputStream == null)
			try {
				imageInputStream = ImageIO.createImageInputStream(input);
				if (imageInputStream == null) {
					throw new IllegalArgumentException(
							"Unsupported object provided as input!");
				}
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				final IllegalArgumentException ex = new IllegalArgumentException();
				ex.initCause(e);
				throw ex;
			}

		// /////////////////////////////////////////////////////////////////////
		// Now, I have an ImageInputStream and I can try to see if input can
		// be decoded by doing header parsing
		// /////////////////////////////////////////////////////////////////////
		try {
			imageInputStream.reset();
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			final IllegalArgumentException ex = new IllegalArgumentException(
					"Unable to read the provided input");
			ex.initCause(e);
			throw ex;
		}

		try {
			rasterReader = new SwanRaster(imageInputStream, this);
			rasterReader.parseHeader();
		} catch (IOException e1) {
			// Input cannot be decoded
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			final IllegalArgumentException ex = new IllegalArgumentException(
					"The header of the provided input is not valid");
			ex.initCause(e1);
			throw ex;
		} catch (XmlException e1) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			final IllegalArgumentException ex = new IllegalArgumentException(
					"The header of the provided input is not well formed");

			ex.initCause(e1);
			throw ex;
		}

		// setting input on superclass
		super.setInput(imageInputStream, true, false);

		// reading information
		initializeReader();
	}

	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		this.setInput(input);
	}

	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input);
	}

	/**
	 * This method initializes the {@link SwanImageReader} (if it has already
	 * decoded an input source) by setting some fields, like the
	 * imageInputStream, the {@link ColorModel} and the {@link SampleModel},
	 * the image dimensions and so on.
	 * 
	 */
	private void initializeReader() {

		// Image dimensions initialization
		width = rasterReader.getNCols();
		height = rasterReader.getNRows();
		
		// calculating the imageSize. Its value is given by
		// nRows*nCols*sampleSizeByte (if DataType is Float
		// the size of each sample is 32 bit = 4 Byte)
		
		final int sampleSizeBit = DataBuffer.TYPE_FLOAT;
		final int sampleSizeByte = (sampleSizeBit + 7) / 8;

		int singleDatasetSize = width * height * sampleSizeByte;

		/**
		 * Setting Tile Dimensions (If Tiling is supported)
		 */

		// if the Image Size is greater than a certain dimension
		// (MIN_SIZE_NEED_TILING), the image needs to be tiled
		if (singleDatasetSize >= MIN_SIZE_NEED_TILING) {

			// This implementation supposes that tileWidth is equal to the width
			// of the whole image
			tileWidth = width;

			// actually (need improvements) tileHeight is given by
			// the default tile size divided by the tileWidth multiplied by the
			// sample size (in byte)
			tileHeight = DEFAULT_TILE_SIZE / (tileWidth * sampleSizeByte);

			// if computed tileHeight is zero, it is setted to 1 as precaution
			if (tileHeight < 1) {
				tileHeight = 1;
			}

		} else {
			// If no Tiling needed, I set the tile sizes equal to the image
			// sizes
			tileWidth = width;
			tileHeight = height;
		}
		numImages = rasterReader.getNImages();
	}

	public BufferedImage read(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return read(imageIndex, null);
	}

	public RenderedImage readAsRenderedImage(final int imageIndex,
			ImageReadParam param) throws IOException {
		checkImageIndex(imageIndex);
		return read(imageIndex, param);
	}

	public BufferedImage readTile(final int imageIndex, final int tileX,
			final int tileY) throws IOException {
		checkImageIndex(imageIndex);

		final int w = getWidth(imageIndex);
		final int h = getHeight(imageIndex);
		int tw = getTileWidth(imageIndex);
		int th = getTileHeight(imageIndex);

		int x = tw * tileX;
		int y = th * tileY;

		if ((tileX < 0) || (tileY < 0) || (x >= w) || (y >= h)) {
			throw new IllegalArgumentException(
					"Tile indices are out of bounds!");
		}

		// if tile overcomes the rightern image bound
		// tile will be resized
		if ((x + tw) > w) {
			tw = w - x;
		}

		// if tile overcomes the vertical image bound
		// tile will be resized
		if ((y + th) > h) {
			th = h - y;
		}

		ImageReadParam param = getDefaultReadParam();
		Rectangle tileRect = new Rectangle(x, y, tw, th);
		param.setSourceRegion(tileRect);

		return read(imageIndex, param);
	}

	public Raster readTileRaster(final int imageIndex, int tileX, int tileY)
			throws IOException {
		return readTile(imageIndex, tileX, tileY).getRaster();
	}

	public boolean canReadRaster() {
		return true;
	}

	/**
	 * This method check if the ImageIndex indicated is valid In the
	 * SwanImageReader.
	 * 
	 * @param imageIndex
	 *            the specified imageIndex
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if imageIndex is greater than 0.
	 */
	private void checkImageIndex(final int imageIndex) {
		if (imageIndex > numImages) {
			throw new IndexOutOfBoundsException("illegal Index");
		}
	}

	/**
	 * Resets this {@link SwanImageReader}.
	 * 
	 * @see javax.imageio.ImageReader#reset()
	 */
	public void reset() {
		dispose();
		super.setInput(null, false, false);
		rasterReader = null;
		tileHeight = tileWidth = -1;
		width = height = -1;
		numImages = -1;
		streamMetadata = null;
		
	}

	public void dispose() {
		if (imageInputStream != null)
			try {
				imageInputStream.close();
			} catch (IOException ioe) {

			}
		imageInputStream = null;
		final ImageInputStream headerIS = rasterReader.getHeaderIS();
		if (headerIS != null)
			try {
				headerIS.close();
			} catch (IOException e) {

			}

		super.dispose();
	}

	public int getTileHeight(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return tileHeight;
	}

	public int getTileWidth(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return tileWidth;
	}

}