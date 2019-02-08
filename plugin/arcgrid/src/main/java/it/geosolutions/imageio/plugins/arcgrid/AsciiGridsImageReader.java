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
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster;
import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster.AsciiGridRasterType;
import it.geosolutions.imageio.plugins.arcgrid.spi.AsciiGridsImageReaderSpi;
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

/**
 * Class used for reading ASCII ArcInfo Grid Format (ArcGrid) and ASCII GRASS
 * Grid Format and to create {@link RenderedImage}s and {@link Raster}s.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public final class AsciiGridsImageReader extends ImageReader {
	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(AsciiGridsImageReader.class.toString());

	/** <code>true</code> if there are some listeners attached to this reader */
	private boolean hasListeners;

	/** Minimum size of a certain file source that neds tiling. */
	private static final int MIN_SIZE_NEED_TILING = 5242880; // 5 MByte

	/** Defaul tile size. */
	private static final int DEFAULT_TILE_SIZE = 1048576 / 2; // 1 MByte

	/** Image Dimensions */
	private int width = -1;

	/** Image Dimensions */
	private int height = -1;

	/** Image Size */
	private int imageSize = -1;

	// if the imageSize is bigger than MIN_SIZE_NEED_TILING
	// we proceed to image tiling
	private boolean isTiled = false;

	/**
	 * Tile width for the underlying raster.
	 */
	private int tileWidth = -1;

	/**
	 * Tile height for the underlying raster.
	 */
	private int tileHeight = -1;

	/**
	 * The thread-safe {@link AsciiGridRaster} to read rasters out of an ascii
	 * grid file.
	 * 
	 * <p>
	 * Every {@link AsciiGridsImageReader} will cache this raster-reader between
	 * differet reads because it will internally save information about the
	 * positions of the tiles on disk.
	 * 
	 */
	private AsciiGridRaster rasterReader = null;

	/**
	 * The Color model for an {@link AsciiGridsImageReader}.
	 * 
	 * The color model is always the same, moreover a {@link ColorModel} in java
	 * is an immutable, therefore it is possible to create it just once for all
	 * the possible {@link AsciiGridsImageReader}.
	 */
	private final static ComponentColorModel cm = RasterFactory
			.createComponentColorModel(DataBuffer.TYPE_DOUBLE,
			// dataType
					ColorSpace.getInstance(ColorSpace.CS_GRAY), // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency;

	/** The <code>SampleModel</code> associated to this reader. */
	private SampleModel sm;

	/** The <code>ImageTypeSpecifier</code> associated to this reader. */
	private ImageTypeSpecifier imageType;

	/** The <code>ImageInputStream</code> associated to this reader. */
	private ImageInputStream imageInputStream = null;

	/** The {@link AsciiGridsImageMetadata} associated to this reader. */
	private AsciiGridsImageMetadata metadata;

	/**
	 * Constructor.
	 * 
	 * It builts up an {@link AsciiGridsImageReader} by providing an
	 * {@link AsciiGridsImageReaderSpi}
	 * 
	 * @param originatingProvider
	 *            the originating service provider interface
	 */
	public AsciiGridsImageReader(AsciiGridsImageReaderSpi originatingProvider) {
		super(originatingProvider);

	}

	/**
	 * Sets the input for this {@link AsciiGridsImageReader}.
	 * 
	 * @param input
	 *            Source the {@link AsciiGridsImageReader} will read from
	 * 
	 * <strong>NOTE: Constrain on GZipped InputStream</strong> If we want to
	 * provide explicitly an InputStream (instead of a File) for a GZipped
	 * source, we MUST provide a proper previously created GZIPInputStream
	 * instead of a simple InputStream.
	 * 
	 * Thus, you need to use Code A) instead of Code B): <blockquote> //as an
	 * instance: File file = new File("example.asc.gz"); //A GZipped Source ...
	 * //Code A) GZIPInputStream stream = new GZIPInputStream(new
	 * FileInputStream(file));
	 * 
	 * //Code B) //InputStream stream = new FileInputStream(file);
	 * 
	 * </blockquote> Otherwise, when calling
	 * {@code ImageIO.getImageReaders(stream)}, (directly or indirectly by a
	 * Jai ImageRead Operation), the proper SPI can't correctly try to read the
	 * Header in order to decode the input.
	 * 
	 */
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
				throw new IllegalArgumentException("Unsupported URL provided as input!");
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
					throw new IllegalArgumentException("Unsupported object provided as input!");
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
			final IllegalArgumentException ex = new IllegalArgumentException("Unable to parse the header for the provided input");
			ex.initCause(e);
			throw ex;
		}
		imageInputStream.mark();

		try {

			// Header Parsing to check if it is an EsriAsciiGridRaster
			rasterReader = AsciiGridRasterType.ESRI.createAsciiGridRaster(imageInputStream, this);
			rasterReader.parseHeader();
		} catch (IOException e) {
			try {

				// Header Parsing to know if it is a GrassAsciiGridRaster
				rasterReader = AsciiGridRasterType.GRASS.createAsciiGridRaster(imageInputStream, this);
				rasterReader.parseHeader();
			} catch (IOException e1) {
				// Input cannot be decoded
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				final IllegalArgumentException ex = new IllegalArgumentException(
						"Unable to parse the header for the provided input");
				ex.initCause(e1);
				throw ex;
			}
		}

		// setting input on superclass
		super.setInput(imageInputStream, true, false);

		hasListeners = (this.progressListeners != null && (!(this.progressListeners
				.isEmpty()))) ? true : false;
		// reading information
		initializeReader();

	}

	/**
	 * This method initializes the {@link AsciiGridsImageReader} (if it has
	 * already decoded an input source) by setting some fields, like the
	 * imageInputStream, the {@link ColorModel} and the {@link SampleModel},
	 * the image dimensions and so on.
	 */
	private void initializeReader() {

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.info("Data Initializing");
			LOGGER.info("\tImageInputStream: \t" + imageInputStream.toString());
			LOGGER.info("\tRasterType:\t\t\t " + rasterReader.getRasterType().toString());
		}

		// Image dimensions initialization
		width = rasterReader.getNCols();
		height = rasterReader.getNRows();

		// calculating the imageSize. Its value is given by
		// nRows*nCols*sampleSizeByte (if DataType is Float
		// the size of each sample is 32 bit = 4 Byte)
		final int sampleSizeBit = cm.getPixelSize();
		final int sampleSizeByte = (sampleSizeBit + 7) / 8;

		imageSize = width * height * sampleSizeByte;

		/**
		 * Setting Tile Dimensions (If Tiling is supported)
		 */

		// if the Image Size is greater than a certain dimension
		// (MIN_SIZE_NEED_TILING), the image needs to be tiled
		if (imageSize >= MIN_SIZE_NEED_TILING) {
			isTiled = true;

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
			////
			//
			// Trick to handle very large rasters
			//
			////
			sm = cm.createCompatibleSampleModel(tileWidth, tileHeight);
			rasterReader.setTilesSize(tileWidth, tileHeight);
		} else {
			// If no Tiling needed, I set the tile sizes equal to the image
			// sizes
			tileWidth = width;
			tileHeight = height;
		}

		// this is a trick to workaround
		sm = cm.createCompatibleSampleModel(tileHeight, tileWidth);
		// image type specifier
		imageType = new ImageTypeSpecifier(cm, sm);
	}

	/**
	 * This method check if the ImageIndex indicated is valid. For
	 * {@link AsciiGridsImageReader} the only legal imageIndex value is 0, since
	 * AsciiGrid sources may contain data for just a single image
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if imageIndex is greater than 0.
	 */
	private void checkImageIndex(final int imageIndex) {
		/* AsciiGrid file format can "contain" only 1 image */
		if (imageIndex != 0)
			throw new IndexOutOfBoundsException("illegal Index: "+imageIndex);
	}

	/**
	 * Returns the height in pixels of the image
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * @return the height in pixels of the image
	 */
	public int getHeight(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return height;
	}

	/**
	 * Returns the width in pixels of the image
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return the width in pixels of the image
	 */
	public int getWidth(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return width;
	}

	/**
	 * Returns the number of images available from the current input source.
	 * Since AsciiGrid input source only contains data for a single image, this
	 * method always return "1". The input parameter is simply ignored.
	 * 
	 * @return 1
	 */
	public int getNumImages(final boolean allowSearch) throws IOException {
		return 1;
	}

	/**
	 * this method provides suggestions for possible image types that will be
	 * used to decode the image. In this case, we are suggesting using a 32 bit
	 * grayscale image with no alpha component.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return an <code>Iterator</code> containing an
	 *         <code>ImageTypeSpecifier</code> suggesting to use a 32 bit
	 *         grayscale image.
	 */
	public synchronized Iterator<ImageTypeSpecifier> getImageTypes(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>();

		if (imageType == null)
			imageType = new ImageTypeSpecifier(cm, sm);
		l.add(imageType);
		return l.iterator();
	}

	/**
	 * Since Ascii Grid format sources may only contain data for a single image,
	 * we return <code>null</code>. We suggest to use
	 * <code>getImageMetadata(0)</code> in order to retrieve valid metadata
	 * 
	 * @return <code>null</code>
	 */
	public IIOMetadata getStreamMetadata() throws IOException {
		return null;
	}

	/**
	 * Returns an <code>IIOMetadata</code> object containing metadata
	 * associated with the image.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return an <code>IIOMetadata</code> object.
	 * 
	 */
	public IIOMetadata getImageMetadata(final int imageIndex)
			throws IOException {
		checkImageIndex(imageIndex);
		if (metadata == null)
			metadata = new AsciiGridsImageMetadata(this.rasterReader);
		return metadata;
	}

	/**
	 * Reads the raster and return it as a complete <code>BufferedImage</code>
	 * using a supplied <code>ImageReadParam</code>
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * @param param
	 *            an <code>ImageReadParam</code> to specify subsampling
	 *            factors, and sourceRegion settings. Other properties are
	 *            actually ignored.
	 * 
	 * @return the desired portion of the image as a <code>BufferedImage</code>
	 * 
	 * @see javax.imageio.ImageReader#read(int, javax.imageio.ImageReadParam)
	 */
	public BufferedImage read(final int imageIndex, ImageReadParam param)
			throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.info("read(final int imageIndex, ImageReadParam param)");

		checkImageIndex((imageIndex));
		if (hasListeners) {
			clearAbortRequest();
			// Broadcast the start of the image read operation
			processImageStarted(0);
		}

		final BufferedImage bi = new BufferedImage(cm,
				(WritableRaster) readRaster(imageIndex, param), false, null);

		if (hasListeners) {
			// Check if there is a request of aborting the read and broadcast
			// the proper event.
			if (rasterReader.isAborting())
				processReadAborted();
			else
				processImageComplete();
		}
		return bi;
	}

	/**
	 * Returns the height of a tile in the image.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return the height of a tile
	 */
	public int getTileHeight(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return tileHeight;
		// If the image is not Tiled, tile Height = Whole Image Height
	}

	/**
	 * Returns the width of a tile in the image.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return the width of a tile
	 */
	public int getTileWidth(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return tileWidth;
		// If the image is not Tiled, tile Width = Whole Image Width
	}

	/**
	 * Returns <code>true</code> if the image has been tiled. All AsciiGrid
	 * sources are untiled. However, when the size of an image is greater than a
	 * threshold value, we introduce a tiling mechanism.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return <code>true</code> if the image has been tiled.
	 */
	public boolean isImageTiled(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return isTiled;
	}

	/**
	 * Returns <code>true</code> if the storage format of the image places no
	 * inherent impediment on random access to pixels. Since each value
	 * contained within an AsciiGrid file may be represented with a different
	 * number of decimal digits, we need to find/count a specific number of
	 * whitespaces before to get a desired pixel value. For this reason, this
	 * method returns <code>false</code>
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return <code>false</code>
	 */
	public boolean isRandomAccessEasy(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return false;
	}

	/**
	 * Returns <code>true</code> since we always call the
	 * <code>setInput</code> method with the <code>seekForwardOnly</code>
	 * argument set to <code>true</code>.
	 * 
	 * @return <code>true</code>
	 */
	public boolean isSeekForwardOnly() {
		return true;
	}

	/**
	 * Simply call the overloaded <code>read</code> method by passing
	 * <code>null</code> as value of the <code>ImageReadParam</code>
	 * argument.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return the whole image as a <code>BufferedImage</code>.
	 * 
	 * @see #read(int, ImageReadParam)
	 */
	public BufferedImage read(final int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.info("read(imageIndex");
		return read(imageIndex, null);
	}

	/**
	 * Returns <code>false</code> since AsciiGrid format does not supports
	 * thumbnail preview images.
	 * 
	 * @return <code>false</code>
	 */
	public boolean readerSupportsThumbnails() {
		return false;
	}

	/**
	 * Returns a new <code>Raster</code> object containing the raw pixel data
	 * from the image stream, without any color conversion applied.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * @param param
	 *            an <code>ImageReadParam</code> used to control the reading
	 *            process, or <code>null</code>.
	 * @return the desired portion of the image as a <code>Raster</code>.
	 */
	public Raster readRaster(final int imageIndex, ImageReadParam param)
			throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER
					.info("readRaster(final int imageIndex, ImageReadParam param)");
		if (param == null)
			param = getDefaultReadParam();
		return rasterReader.readRaster(param);
	}

	/**
	 * 
	 * Reads the tile indicated by the <code>tileX</code> and
	 * <code>tileY</code> arguments, returning it as a
	 * <code>BufferedImage</code>.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * @param tileX
	 *            the column index (starting with 0) of the tile to be
	 *            retrieved.
	 * @param tileY
	 *            the row index (starting with 0) of the tile to be retrieved.
	 * 
	 * @return the tile as a <code>BufferedImage</code>.
	 */
	public BufferedImage readTile(final int imageIndex, final int tileX,
			final int tileY) throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER
					.info("readTile(final int imageIndex, final int tileX, final int tileY)");
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

	/**
	 * Returns a new <code>Raster</code> object containing the raw pixel data
	 * from the tile, without any color conversion applied.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @param tileX
	 *            the column index (starting with 0) of the tile to be
	 *            retrieved.
	 * @param tileY
	 *            the row index (starting with 0) of the tile to be retrieved.
	 * 
	 * @return the tile as a <code>Raster</code>.
	 */

	public Raster readTileRaster(final int imageIndex, int tileX, int tileY)
			throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER
					.info("readTileRaster(final int imageIndex, int tileX, int tileY)");
		return readTile(imageIndex, tileX, tileY).getRaster();
	}

	/**
	 * Returns <code>true</code> since this plug-in supports reading just a
	 * {@link java.awt.image.Raster <code>Raster</code>} of pixel data.
	 * 
	 * @return <code>true</code>
	 */

	public boolean canReadRaster() {
		return true;
	}

	/**
	 * Returns the smallest valid index for reading, 0 for the
	 * {@link AsciiGridsImageReader}.
	 * 
	 * @return 0
	 */

	public int getMinIndex() {
		return 0;
	}

	/**
	 * Returns the number of thumbnail preview images associated with the given
	 * image. Since {@link AsciiGridsImageReader} does not support thumbnails,
	 * this method always returns 0.
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return 0
	 */
	public int getNumThumbnails(final int imageIndex) throws IOException {
		return 0;
	}

	/**
	 * Always return false since the {@link AsciiGridsImageReader} does not
	 * support thumbnails
	 * 
	 * @param imageIndex
	 *            the index of the required image which need to be always 0
	 *            since AsciiGrid format supports only single image.
	 * 
	 * @return <code>false</code>
	 */
	public boolean hasThumbnails(final int imageIndex) throws IOException {
		return false;
	}

	/**
	 * Returns <code>true</code> if the current input source has been marked
	 * as allowing metadata to be ignored by passing <code>true</code> as the
	 * <code>ignoreMetadata</code> argument to the
	 * {@link AsciiGridsImageReader#setInput} method.
	 * 
	 * @return <code>true</code> if the metadata may be ignored.
	 */
	public boolean isIgnoringMetadata() {
		return ignoreMetadata;
	}

	/**
	 * A simple method which returns the proper AsciiGridRaster used to perform
	 * reading operations
	 * 
	 * @return Returns the rasterReader.
	 */
	public AsciiGridRaster getRasterReader() {
		return rasterReader;
	}

	/**
	 * A simple method which returns the imageInputStream used to perform
	 * reading operations
	 * 
	 * @return Returns the imageInputStream.
	 */
	public ImageInputStream getCurrentImageInputStream() {
		return imageInputStream;
	}

	/**
	 * Cleans this {@link AsciiGridsImageReader} up.
	 */
	public void dispose() {
		if (imageInputStream != null)
			try {
				imageInputStream.close();
			} catch (IOException ioe) {

			}

		imageInputStream = null;
		super.dispose();
	}

	/**
	 * Resets this {@link AsciiGridsImageReader}.
	 */
	public void reset() {
		imageInputStream = null;
		super.setInput(null, false, false);
		rasterReader = null;
		tileHeight = tileWidth = -1;
		width = height = -1;
		sm = null;
		isTiled = false;
		imageType = null;
		imageSize = -1;
		metadata = null;
	}

	public void processImageProgress(float percentageDone) {
		super.processImageProgress(percentageDone);
	}

	/**
	 * Request to abort any current read operation.
	 */
	public synchronized void abort() {
		// super.abort();
		if (rasterReader != null)
			rasterReader.abort();
	}

	/**
	 * Checks if a request to abort the current read operation has been made.
	 */
	protected synchronized boolean abortRequested() {
		return rasterReader.isAborting();
		// return super.abortRequested();
	}

	/**
	 * Clear any request to abort.
	 */
	protected synchronized void clearAbortRequest() {
		// super.clearAbortRequest();
		if (rasterReader != null)
			rasterReader.clearAbort();
	}

	/**
	 * A Simple call to the <code>setInput(Object input)</code> method,
	 * ignoring all other parameters.
	 */
	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		this.setInput(input);
	}

	/**
	 * A Simple call to the <code>setInput(Object input)</code> method,
	 * ignoring all other parameters.
	 */
	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input);
	}

	public boolean isHasListeners() {
		return hasListeners;
	}
}
