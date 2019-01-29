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
package it.geosolutions.imageio.plugins.arcgrid.raster;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageReader;
import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageWriter;
import it.geosolutions.imageio.utilities.StringToDouble;

import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.TileFactory;
import javax.media.jai.iterator.RectIter;

/**
 * Abstract base class to handle ASCII ArcGrid/GRASS formats
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class AsciiGridRaster {

	protected final static Logger LOGGER = Logger.getLogger(AsciiGridRaster.class.toString());
	

	public static enum AsciiGridRasterType{
		UNDEFINED {
			@Override
			public AsciiGridRaster createAsciiGridRaster(
					ImageInputStream inStream) {
				throw new UnsupportedOperationException("Operation not supported by the UNDEFINED raster type");
			}

			@Override
			public AsciiGridRaster createAsciiGridRaster(
					ImageOutputStream oStream, AsciiGridsImageWriter writer) {
				throw new UnsupportedOperationException("Operation not supported by the UNDEFINED raster type");
			}

			@Override
			public AsciiGridRaster createAsciiGridRaster(
					ImageOutputStream oStream) {
				throw new UnsupportedOperationException("Operation not supported by the UNDEFINED raster type");
			}

			@Override
			public AsciiGridRaster createAsciiGridRaster(
					ImageInputStream inStream, AsciiGridsImageReader reader) {
				throw new UnsupportedOperationException("Operation not supported by the UNDEFINED raster type");
			}
		},
		ESRI {
			@Override
			public EsriAsciiGridRaster createAsciiGridRaster(
				final ImageInputStream inStream) {
				return new EsriAsciiGridRaster(inStream);
			}

			@Override
			public EsriAsciiGridRaster createAsciiGridRaster(
					ImageOutputStream oStream, AsciiGridsImageWriter writer) {
				return new EsriAsciiGridRaster(oStream, writer);
			}

			@Override
			public EsriAsciiGridRaster createAsciiGridRaster(
					ImageOutputStream oStream) {
				return new EsriAsciiGridRaster(oStream);
			}

			@Override
			public EsriAsciiGridRaster createAsciiGridRaster(
					ImageInputStream inStream, AsciiGridsImageReader reader) {
				return new EsriAsciiGridRaster(inStream,reader);
			}
		},
		GRASS {
			@Override
			public GrassAsciiGridRaster createAsciiGridRaster(
					ImageInputStream inStream) {
				return new GrassAsciiGridRaster(inStream);
			}

			@Override
			public GrassAsciiGridRaster createAsciiGridRaster(
					ImageOutputStream oStream, AsciiGridsImageWriter writer) {
				return new GrassAsciiGridRaster(oStream,writer);
			}

			@Override
			public GrassAsciiGridRaster createAsciiGridRaster(
					ImageOutputStream oStream) {
				return new GrassAsciiGridRaster(oStream);
			}

			@Override
			public GrassAsciiGridRaster createAsciiGridRaster(
					ImageInputStream inStream, AsciiGridsImageReader reader) {
				return new GrassAsciiGridRaster(inStream,reader);
			}
		};
		
		public abstract AsciiGridRaster createAsciiGridRaster(ImageInputStream inStream);
		public abstract AsciiGridRaster createAsciiGridRaster(ImageOutputStream oStream, AsciiGridsImageWriter writer);
		public abstract AsciiGridRaster createAsciiGridRaster(ImageOutputStream oStream);
		public abstract AsciiGridRaster createAsciiGridRaster(ImageInputStream inStream, AsciiGridsImageReader reader);
		public static AsciiGridRasterType getDefaultRasterType() {
			return UNDEFINED;
		}
	}


	/** The OS-dependent line separator */
	public static final String newline = System.getProperty("line.separator");

	protected final int[] tileTreeMutex = new int[1];

	protected volatile boolean abortRequired = false;

	private final static int MAX_BYTES_TO_READ = 70;

	private final static int MAX_VALUE_LENGTH = 40;

	/** max value found in the file */
	protected double maxValue = Double.MIN_VALUE;

	/** min value found in the file */
	protected double minValue = Double.MAX_VALUE;

	/**
	 * x coordinate of the grid origin (the lower left corner) in compliance
	 * with the <code>isCorner</code> value
	 */
	protected double xllCellCoordinate = Double.NaN;

	/**
	 * y coordinate of the grid origin (the lower left corner) in compliance
	 * with the <code>isCorner</code> value
	 */
	protected double yllCellCoordinate = Double.NaN;

	/** horizontal subsampling */
	protected int sourceXSubsampling = 1;

	/** vertical subsampling */
	protected int sourceYSubsampling = 1;

	/**
	 * The grid's origin (the lower left corner of the grid) may be specified
	 * using the coordinates of either its lower-left corner (by providing
	 * XLLCORNER and YLLCORNER) or the center of the lower-left grid cell
	 * (XLLCENTER and YLLCENTER)
	 */
	/**
	 * If <code>isCorner</code> is <code>true</code> then
	 * <code>xllCellCoordinate</code> is the coordinate of the lower-left
	 * corner of the grid. If <code>isCorner</code> is <code>false</code>
	 * then <code>xllCellCoordinate</code> is the coordinate of the center of
	 * the lower-left gridcell of the grid.
	 */
	protected boolean isCorner;

	/** The size of a single cell of the grid along X */
	protected double cellSizeX = Double.NaN;

	/** The size of a single cell of the grid along Y */
	protected double cellSizeY = Double.NaN;

	/** The number of columns of the raster */
	protected int nCols = -1;

	/** The number of rows of the raster */
	protected int nRows = -1;

	/** <code>true</code> if the file related to this raster is compressed */
	protected boolean compress;

	/**
	 * A kind of Bookmark that point at the first byte in the stream after the
	 * header
	 */
	protected long dataStartAt = -1;

	/**
	 * Used to remember the locations (in bytes offset from 0) of samples at tile boundaries within the data.
	 * These locations speed up random access to tile data by allowing us to seek to the closest-lower known position in
	 * the file to start reading from there.  Without this, due to the irregular layout (on disk) of the data, random tile
	 * access would require a large amount of data to be needlessly skipped through before the right place in the file can
	 * be found.
	 * </p>
	 * Example:  If we had ascii data that was laid out in the stream like so...
	 * <pre>
	 * 0          1          2          3          4          5
	 * 01233456789012334567890123345678901233456789012334567890
	 *
	 * 0 0.1 3 33 4 1 0.9 5434 5 5 234 09 2 55432 3 0.87 3 588
	 * </pre>
	 *
	 * Then a fully populated tileMarker index for a tile size is 2x2 would be
	 *
	 * <pre>
	 * 3: 7
	 * 7: 17
	 * 11: 29
	 * 15: 41
	 * </pre>
	 *
	 */
	protected TreeMap<Long,Long> tileMarker = new TreeMap<Long,Long>();

	/** the width of a tile */
	protected int tileWidth = -1;

	/** the height of a tile */
	protected int tileHeight = -1;

	/** ImageInputStream used to read the source that contain data */
	protected ImageInputStream imageIS = null;

	/**
	 * ImageOutputStream used to write the raster to the device
	 * (file,stream,...)
	 */
	protected ImageOutputStream imageOS = null;

	/** the value used to represent noData for an element of the raster */
	protected double noData = Double.NaN;

	/** the {@link AsciiGridsImageReader} to be used for read operations */
	protected AsciiGridsImageReader reader;

	/** the {@link AsciiGridsImageWriter} to be used for write operations */
	protected AsciiGridsImageWriter writer;

	/** A constructor b */
	protected AsciiGridRaster(ImageInputStream iis) {
		imageIS = iis;
		abortRequired = false;
	}

	/**
	 * A constructor to build an {@link AsciiGridRaster} given an
	 * <code>ImageInputStream</code> and an {@link AsciiGridsImageReader}
	 * 
	 * @param iis
	 *            <code>ImageInputStream</code> needed to read the raster.
	 * @param reader
	 *            {@link AsciiGridsImageReader} used to read the raster.
	 */
	protected AsciiGridRaster(ImageInputStream iis, AsciiGridsImageReader reader) {
		this(iis);
		this.reader = reader;
	}

	/**
	 * A base constructor to write {@link AsciiGridRaster}s.
	 * 
	 * @param ios
	 *            <code>ImageOutputStream</code> needed to write the raster.
	 */

	protected AsciiGridRaster(ImageOutputStream ios) {
		imageOS = ios;
		abortRequired = false;
	}

	/**
	 * A constructor to build an {@link AsciiGridRaster} given an
	 * <code>ImageOutputStream</code> and an {@link AsciiGridsImageWriter}
	 * 
	 * @param iis
	 *            <code>ImageOutputStream</code> needed to read the raster.
	 * @param writer
	 *            {@link AsciiGridsImageWriter} used to read the raster.
	 */
	protected AsciiGridRaster(
			ImageOutputStream ios,
			AsciiGridsImageWriter writer) {
		this(ios);
		this.writer = writer;
	}

	/**
	 * The header structure of an ASCII GRASS file is different from the one of
	 * an ASCII ArcGrid file.<BR>
	 * 
	 * <pre>
	 *         A GRASS Header has the following form:
	 *         ---------------------------------------
	 *         NORTH: XX
	 *         SOUTH: XX
	 *         EAST: XX
	 *         WEST: XX
	 *         ROWS: XX
	 *         COLS: XX
	 *                                  
	 *                                  
	 *         An ArcGrid header has the following form:
	 *         -----------------------------------------
	 *         nrows XX
	 *         ncols XX
	 *         xllcorner (OR xllcenter) XX
	 *         yllcorner (OR yllcenter) XX
	 *         cellsize XX
	 *         NODATA_value XX (Optional) 	
	 *         
	 *                                 
	 *         (note: XX represents the value of the specific field)
	 * </pre>
	 * 
	 * For this reason, a specific implementation is required.
	 */
	public abstract void parseHeader() throws IOException;

	/**
	 * As stated for {@link parseHeader}, the differences betweens the
	 * structure of the header of an ASCII ArcGrid and the one of an ASCII GRASS
	 * require a different management also during the writing process. For this
	 * reason, a specific implementation is required.
	 */
	public abstract void writeHeader(String columns, String rows, String xll,
			String yll, String cellsizeX, String cellsizeY,
			String rasterSpaceType, String noDataValue) throws IOException;

	/**
	 * Return the <code>String</code> representing noData. <BR>
	 * GRASS use the '*' sign to represent noData, while ArcGrid use a numeric
	 * representation (A commonly used value is -9999 which is also used as
	 * default value)
	 */
	public abstract String getNoDataMarker();

	/**
	 * Max value.
	 * 
	 * @return the max value contained in the data file
	 */
	final public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Min value.
	 * 
	 * @return the min value contained in the data file
	 */
	final public double getMinValue() {
		return minValue;
	}

	/**
	 * NoData value.
	 * 
	 * @return the value representing noData
	 */

	final public double getNoData() {
		return noData;
	}

	/**
	 * Number of rows.
	 * 
	 * @return the number of rows contained in the file.
	 */
	final public int getNRows() {
		return nRows;
	}
	
	/** Retrieves the type of raster we are serving, GRASS or ESRI.*/
	abstract public AsciiGridRasterType getRasterType();

	/**
	 * Number of columns.
	 * 
	 * @return the number of columns contained in the file.
	 */
	final public int getNCols() {
		return nCols;
	}

	/**
	 * XSubSampling factor
	 * 
	 * @return the subSampling factor along X
	 */
	public int getSourceXSubsampling() {
		return sourceXSubsampling;
	}

	/**
	 * YSubSampling factor
	 * 
	 * @return the subSampling factor along Y
	 */
	public int getSourceYSubsampling() {
		return sourceYSubsampling;
	}

	/**
	 * Cell Size X value
	 * 
	 * @return the size of a single cell of the grid along X
	 */
	final public double getCellSizeX() {
		return cellSizeX;
	}

	/**
	 * Cell Size Y value
	 * 
	 * @return the size of a single cell of the grid along X
	 */
	final public double getCellSizeY() {
		return cellSizeY;
	}

	/**
	 * Lower-Left Cell: X coordinate
	 * 
	 * @return the X coordinate of the lower left grid cell.
	 * @see #isCorner
	 */
	final public double getXllCellCoordinate() {
		return xllCellCoordinate;
	}

	/**
	 * Lower-Left Cell: Y coordinate
	 * 
	 * @return the Y coordinate of the lower left grid cell.
	 * @see #isCorner
	 */
	final public double getYllCellCoordinate() {
		return yllCellCoordinate;
	}

	/**
	 * TileHeight value
	 * 
	 * @return the height of the tile
	 */
	final public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * TileWidth value
	 * 
	 * @return the width of the tile
	 */
	final public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * Initializes tile sizes
	 * 
	 * @param tileWidth
	 *            the width of the tile
	 * @param tileHeight
	 *            the height of the tile
	 */
	final public void setTilesSize(final int tileWidth, final int tileHeight) {
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;
	}

	/**
	 * Returns the <code>isCorner</code> field.<BR>
	 * It is <code>true</code> if <code>xllCellCoordinate</code> and
	 * <code>yllCellCoordinate</code> represent the coordinates of the
	 * lower-left corner of the lower-left cell of the grid. It is
	 * <code>false</code> if they represent the coordinates of the center of
	 * the lower-left cell of the grid.
	 * 
	 * @return isCorner.
	 */
	final public boolean isCorner() {
		return isCorner;
	}

	/**
	 * Returns the stream position where useful data starts (just after the end
	 * of the header)
	 * 
	 * @return dataStartAt
	 */
	final long getDataStartAt() {
		return dataStartAt;
	}

	/**
	 * This method reads data values from the ImageInputStream and returns a
	 * raster having these data values as samples. When image is tiled or
	 * reading is executed only on a specific part of the ASCII source, I need
	 * to determine which values must be loaded and which must be skipped.
	 * Within an ASCII source, I can't know how many digits compose a value.
	 * Thus, I need to scan and check every byte stored on the input source and
	 * retrieve the value as well as I need to skip values if they are useless.
	 * 
	 * @param param
	 *            an ImageReadParam which specifies source region properties as
	 *            width, height, x and y offsets.
	 * 
	 * @return WritableRaster the Raster composed by reading data values
	 * 
	 * @throws IOException
	 * @throws NumberFormatException
	 *             TODO we ignore destination region, destinationOffset etc...
	 */
	public WritableRaster readRaster(ImageReadParam param) throws IOException {
		final WritableRaster raster;

		final boolean hasListeners = reader.isHasListeners();

		int perc = 0;
		int iPerc = 1;
		int dstWidth = -1;
		int dstHeight = -1;
		int srcRegionWidth = -1;
		int srcRegionHeight = -1;
		int srcRegionXOffset = -1;
		int srcRegionYOffset = -1;
		int xSubsamplingFactor = -1;
		int ySubsamplingFactor = -1;
		boolean doSubsampling = false;

		// //////////////////////////////////////////////////////////////////////
		//
		//
		// STEP 1
		//
		// Retrieving Information about Source Region and doing
		// additional intialization operations.
		//
		//
		//
		// /////////////////////////////////////////////////////////////////////
		Rectangle srcRegion = param.getSourceRegion();
		if (srcRegion != null) {
			srcRegionWidth = (int) srcRegion.getWidth();
			srcRegionHeight = (int) srcRegion.getHeight();
			srcRegionXOffset = (int) srcRegion.getX();
			srcRegionYOffset = (int) srcRegion.getY();

			// //
			//
			// Minimum correction for wrong source regions
			//
			// When you do subsampling or source subsetting it might happen that
			// the given source region in the read param is uncorrect, which
			// means it can be or a bit larger than the original file or can
			// begin a bit before original limits.
			//
			// We got to be prepared to handle such case in order to avoid
			// generating ArrayIndexOutOFboundsException later in the code.
			//
			// //
			if (srcRegionXOffset < 0)
				srcRegionXOffset = 0;
			if (srcRegionYOffset < 0)
				srcRegionYOffset = 0;
			if ((srcRegionXOffset + srcRegionWidth) > nCols) {
				srcRegionWidth = nCols - srcRegionXOffset;
			}
			// initializing destWidth
			dstWidth = srcRegionWidth;

			if ((srcRegionYOffset + srcRegionHeight) > nRows) {
				srcRegionHeight = nRows - srcRegionYOffset;
			}
			// initializing dstHeight
			dstHeight = srcRegionHeight;

		} else {
			// Source Region not specified.
			// Assuming Source Region Dimension equal to Source Image Dimension
			dstWidth = nCols;
			dstHeight = nRows;
			srcRegionXOffset = srcRegionYOffset = 0;
			srcRegionWidth = nCols;
			srcRegionHeight = nRows;
		}

		// SubSampling variables initialization
		xSubsamplingFactor = param.getSourceXSubsampling();
		ySubsamplingFactor = param.getSourceYSubsampling();
		if ((xSubsamplingFactor > nCols) || (ySubsamplingFactor > nRows)) {
			throw new IOException(
					"The subSamplingFactor cannot be greater than image size!");
		}
		if (xSubsamplingFactor > 1 || ySubsamplingFactor > 1)
			doSubsampling = true;
		// ////////////////////////////////////////////////////////////////////////////
		//
		// I'm loading data to create a Raster needed for a Tile.
		// Thus, if the samples needed for the tile are not located immediatly
		// after the header, I need to find (and count) a defined number of
		// whitespaces (a withespace could be one of
		// {' ' , '\n' , '\r' , '\t' , "\r\n"})
		//
		// ////////////////////////////////////////////////////////////////////////////
		// total samples to scan
		final int samplesToLoad = srcRegionHeight * nCols;

		// //
		//
		// Updating the destination size in compliance with
		// the subSampling parameters
		//
		// //
		dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
		dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

		// Number of samples to count before I find useful data
		final long tileBeginsAtSampleIndex = (nCols * srcRegionYOffset);

		final TileFactory factory = (TileFactory) JAI.getDefaultInstance().getRenderingHint(JAI.KEY_TILE_FACTORY);
		if (factory != null)
			raster = factory.createTile(RasterFactory.createBandedSampleModel(
					java.awt.image.DataBuffer.TYPE_DOUBLE, dstWidth, dstHeight,
					1), null);
		else
			raster = RasterFactory.createBandedRaster(
					java.awt.image.DataBuffer.TYPE_DOUBLE, dstWidth, dstHeight,
					1, null);

		int ch = -1;
		int prevCh = -1;
		long streamPosition = 0;
		// /////////////////////////////////////////////////////////////////////
		//
		//
		//
		// STEP 2
		//
		// Searching Start of useful (for this Tile) data Values
		//
		//
		//
		//
		// /////////////////////////////////////////////////////////////////////


		// we set these two based on the best information we can find in the tile index
		final long samplesToSkip;
		final long indexedSeekPos;

		// this number is the zero-based position of the next sample that will be read from the input stream.  It gets
		// updated as this method proceeds so that it agrees with the current state of imageIS
		long nextSampleIndex;

		if (tileBeginsAtSampleIndex > 0) {
			// we can't start reading samples from the beginning - use the index to see if there's a stream position
			// that can accelerate our search
			synchronized (tileTreeMutex) {
				Long exactPosition = tileMarker.get(tileBeginsAtSampleIndex);

				// Case 1: Exact key
				if (exactPosition != null) {
					if (LOGGER.getLevel() == Level.FINEST) {
						LOGGER.finest(String.format(
								"[lookup] Found exact position %d for sample %d%n", 
								exactPosition, 
								tileBeginsAtSampleIndex));
					}
					// this is an exact match - no sample skipping will be required, just set the stream position and we can start
					// reading data in to the raster
					samplesToSkip = 0;
					indexedSeekPos = exactPosition;
					nextSampleIndex = tileBeginsAtSampleIndex;
				} else {
					// Case 2: Nearest(Lower) Key
					final SortedMap<Long,Long> partitionedIndex = tileMarker.headMap(Long.valueOf(tileBeginsAtSampleIndex));

					if (!partitionedIndex.entrySet().isEmpty()) {
						// the highest/last key in the partitioned index will give us the best possible position to start from
						// as it will be the closest to where we need to start reading from (given we read from left to right)
						final long nearestSampleIndex = partitionedIndex.lastKey();
						final long nearestPosition = tileMarker.get(partitionedIndex.lastKey());

					  samplesToSkip = tileBeginsAtSampleIndex - nearestSampleIndex;
					  if (LOGGER.getLevel() == Level.FINEST) {
							LOGGER.finest(String.format("[lookup] Found nearest (%d) indexed stream position %d for sample %d%n",
								tileBeginsAtSampleIndex,
								nearestPosition,
								nearestSampleIndex
							));
					  }

							indexedSeekPos = nearestPosition;
							nextSampleIndex = nearestSampleIndex;
					} else {
						// NB pretty sure this can't ever happen, given that the index is populated from l->r, but let's leave it
						// here as the test coverage doesn't give me much confidence that it's not needed for obscure reasons
						indexedSeekPos = dataStartAt;
						samplesToSkip = tileBeginsAtSampleIndex;
						nextSampleIndex = 0;
					}
				}
			}
		} else {
			indexedSeekPos = dataStartAt;
			nextSampleIndex = 0;
			samplesToSkip = 0;
		}

		// nextSampleIndex should now agree with seek position, this means the the next sample
		// to be read will be the value with index position nextSampleIndex
		imageIS.seek(indexedSeekPos);
		streamPosition = imageIS.getStreamPosition();

		if (LOGGER.getLevel() == Level.FINEST) {
			LOGGER.finest(String.format("[lookup] After index lookup : indexedSeekPos=%d nextSampleIndex=%d%n",
					indexedSeekPos, nextSampleIndex));
		}

		// //
		//
		// Check abort request
		//
		// //
		if (hasListeners && abortRequired) {
			return raster;
		}

		// if the seek position we found wasn't exact, then we need to skip through the ascii values
		// until we find the data for the tile
		int numSamplesSkipped = 0;
		if (samplesToSkip > 0) {
			final int tileH = getTileHeight();
			final int tileW = getTileWidth();
			Long key;
			Long val;

			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.finest(String.format("[skipping] About to skip %d samples to go from %d to %d%n",
						samplesToSkip, nextSampleIndex, tileBeginsAtSampleIndex));
			}

			while (numSamplesSkipped < samplesToSkip) {

				ch = imageIS.read(); // Filling the Buffer
				if (ch == -1)
					throw new EOFException(
							"EOF found while looking for valid input");
				streamPosition++;

				// if the last character was whitespace and this isn't, then we've hit a sample
				if (!isWhitespace(ch)) {
					if (isWhitespace(prevCh)) {
						numSamplesSkipped++;
						nextSampleIndex++;

						// we've reached a tile boundary - mark this in the index to speed up future random access
						if (((nextSampleIndex) % (tileH * tileW)) == 0) {
							key = Long.valueOf(nextSampleIndex);
							// the actual index is one character back - we have consumed the first
							// character to get here, so we set the indexed stream position be such that
							// the index lookup will place the stream in the correct place for reading to start
							// to this remembered value
							val = Long.valueOf(streamPosition) - 1;
							synchronized (tileTreeMutex) {
								if (!tileMarker.containsKey(key)) {
									if (LOGGER.getLevel() == Level.FINEST) {
										LOGGER.finest(String.format("[skipping] Reached tile boundary, marking sample position %d will be found at stream position %d%n", key,
												val));
									}

									tileMarker.put(key, val);
								}
							}
						}
					}
				}

				prevCh = ch;

				if (hasListeners) {
					// //
					//
					// Check abort request at every 10%
					//
					// //
					perc = (int) (((nextSampleIndex * 1.0f) / tileBeginsAtSampleIndex) * 100);
					if ((perc % (10 * iPerc) == 0) && (int) perc > 0) {
						if (abortRequired)
							return raster;
						iPerc++;
					}
				}
			}
		}

		// sample skipping consumes the first character from the next sample - let's rewind
		// by a byte so we can read it again
	  if (numSamplesSkipped > 0) {
	  	if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.finest(String.format("[skipping] Finished, numSamplesSkipped=%d, nextSampleIndex=%d streamPosition=%d%n",
						numSamplesSkipped, nextSampleIndex, imageIS.getStreamPosition()));
	  	}
	  	imageIS.seek(imageIS.getStreamPosition() - 1);
	  } else {
	  	if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.finest(String.format("[skipping] No skipping required, nextSampleIndex=%d streamPosition=%d%n",
						nextSampleIndex, imageIS.getStreamPosition()));
	  	}
	  }

		// /////////////////////////////////////////////////////////////////////
		//
		//
		// STEP 3
		//
		//
		// Here starts the real samples loading. It's Time to read data
		// and convert Ascii bytes in floating numbers.
		//
		//
		// /////////////////////////////////////////////////////////////////////

		// //
		//
		// 3.B: Variables initialization
		//
		// //
	  if (LOGGER.getLevel() == Level.FINEST) {
			LOGGER.finest(String.format("[reading] starting read of %d samples %d..%d stream position %d%n",
			samplesToLoad, nextSampleIndex, nextSampleIndex + samplesToLoad, imageIS.getStreamPosition()));
	  }
		prevCh = -1;
		ch = -1;

		// //
		// variables for arithmetic operations
		// //
		double value = 0.0;

		// //
		// variables for raster setting
		// //
		long rasterX = 0;
		long rasterY = 0;
		long tempCol = 0, tempRow = 0;
		long samplesRead = 0;

		final double noDataValue = getNoData();
		final StringToDouble doubleConverter = StringToDouble.acquire();
		// If I need to load 10 samples, I need to count 9 spaces
		while (samplesRead < samplesToLoad) {
			value = getValue(imageIS, MAX_BYTES_TO_READ, MAX_VALUE_LENGTH,
					doubleConverter);
			// // //
			// //
			// // Does subsampling allow to add this value?
			// //
			// // //
			tempCol = samplesRead % nCols;
			tempRow = samplesRead / nCols;

			if (!((tempCol < srcRegionXOffset || tempCol >= srcRegionXOffset
					+ srcRegionWidth))) {

				if ((!doSubsampling)
						|| (doSubsampling && (((tempRow) % ySubsamplingFactor == 0) && ((tempCol)
								% xSubsamplingFactor == 0)))) {
					// If there is an exponent, I update the value
					// no data management
					if (Double.isInfinite(value)) {
						if ((samplesRead != samplesToLoad)) {
							StringToDouble.release(doubleConverter);
							throw new IOException(
									"Error on reading data due to an END of File or invalid data find");
						}
					}

					if ((value != noDataValue) && !Double.isNaN(value)
							&& !Double.isInfinite(value)) {
						synchronized (tileTreeMutex) {
							minValue = Math.min(minValue, value);
							maxValue = Math.max(maxValue, value);
						}
					}

					// //
					//
					// We found a value, let's give it to the raster.
					//
					// //
					rasterY = (tempRow) / ySubsamplingFactor;
					rasterX = (tempCol - srcRegionXOffset) / xSubsamplingFactor;
					raster.setSample((int)rasterX, (int)rasterY, 0, value);
				}
			}
			// sample found
			samplesRead++;

			if (hasListeners) {
				// //
				//
				// Check abort request at every 10%
				//
				// //
				perc = (int) (((samplesRead * 1.0f) / samplesToLoad) * 100);
				if ((perc % (10 * iPerc) == 0) && (int) perc > 0) {
					if (abortRequired)
						return raster;
					reader.processImageProgress(perc);
					iPerc++;
				}
			}
			// Resetting Values
			value = 0;
		}

		// as with skipping samples, we put an entry in to the index to speed up future random access
		synchronized (tileTreeMutex) {
			// scan to beginning of next non-whitespace character - this needs to be exact so that skipping routine
			// works correctly...
			while ((ch = imageIS.read()) != -1 && isWhitespace(ch)) {
			}
			// ... now remember the position *before* the non w/s character
			long streamPositionToCache = imageIS.getStreamPosition() - 1;
			long samplePositionToCache = samplesRead + tileBeginsAtSampleIndex;

			if (LOGGER.getLevel() == Level.FINEST) {
				LOGGER.finest(String.format("[reading] Finished reading, marking sample position %d will begin at stream position %d%n",
					samplePositionToCache,
					streamPositionToCache));
			}

			tileMarker.put(samplePositionToCache, streamPositionToCache);
		}
		StringToDouble.release(doubleConverter);
		return raster;
	}

	/**
	 * Writes the raster
	 * 
	 * @param iterator
	 *            A <code>RectIterator</code> built on Lines and Pixels of the
	 *            Raster which need to be written.
	 * @param noDataDouble
	 *            the value representing noData.
	 * @param noDataMarker
	 *            a <code>String</code> which need to be printed when founding
	 *            a noData value
	 * @throws IOException
	 */

	public void writeRaster(RectIter iterator, Double noDataDouble,
			String noDataMarker) throws IOException {

		final boolean hasListeners = writer.isHasListeners();
		final int pixelsToWrite = hasListeners ? (writer.getNColumns() * writer
				.getNRows()) : 0;
		int pixelsWritten = 0;
		int perc = 0;
		int iPerc = 1;

		if (hasListeners && abortRequired) {
			return;
		}
		String sampleString;
		double sample;
		while (!iterator.finishedLines()) {
			while (!iterator.finishedPixels()) {
				sample = iterator.getSampleDouble();
				sampleString = Double.toString(sample);
				pixelsWritten++;
				if (hasListeners) {
					perc = (int) (((pixelsWritten * 1.0f) / pixelsToWrite) * 1000);

					if ((perc % (25 * iPerc) == 0) && (int) perc > 0) {
						if (abortRequired) {
							return;
						}
						writer.processImageProgress(perc / 10f);
						iPerc++;
					}
				}

				// writing the sample
				if ((noDataDouble.compareTo(new Double(sample)) != 0)
						&& !Double.isNaN(sample) && !Double.isInfinite(sample)) {
					imageOS.writeBytes(sampleString);
				} else {
					imageOS.writeBytes(noDataMarker);
				}

				iterator.nextPixel();

				// space
				if (!iterator.finishedPixels())
					imageOS.write(32);

			}

			imageOS.writeBytes(newline);
			iterator.nextLine();
			iterator.startPixels();
		}
	}

	public void abort() {
		abortRequired = true;
	}

	public boolean isAborting() {
		return abortRequired;
	}

	public void clearAbort() {
		abortRequired = false;
	}

	/**
	 * Retrieves a value from the ascii grid header using the provided
	 * {@link ImageInputStream} and by reading at most
	 * <code>maxBytesToRead</code> from the provided input stream.
	 * 
	 * <p>
	 * The maximum length of the value we are looking for is
	 * <code>maxValueLength</code>.
	 * 
	 * <p>
	 * The header of an ascii grid file, ESRI or GRASS is composed by a certain
	 * number of lines of the form:
	 * 
	 * key value _new_line_ key value _new_line_
	 * 
	 * Purpose of this method is to look for the header's keys.
	 * 
	 * @param inStream
	 *            is the {@link ImageInputStream} to read from.
	 * @param maxBytesToRead
	 *            is maximum number of bytes to consume from the provided
	 *            {@link ImageInputStream}.
	 * @param maxValueLength
	 *            indicates the maximum expected length in input ascii
	 *            characters of the value we are looking for.
	 * @return a {@link double} containing either a valid value or Negative
	 *         Infinity if something bad happened.
	 * @throws IOException
	 */
	double getValue(final ImageInputStream inStream, int maxBytesToRead,
			int maxValueLength, StringToDouble doubleConverter)
			throws IOException {

		byte b;
		double retVal = Double.NaN;
		boolean started = false;
		int bytesRead = 0, validBytesRead = 0;
		// final StringToDouble doubleConverter = StringToDouble.acquire();
		while (true) {
			b = (byte) (inStream.read() & 0xff);

			// /////////////////////////////////////////////////////////////////
			//
			// Is it something to use or not.
			//
			// /////////////////////////////////////////////////////////////////
			if (b == -1)
				// send error on end of file
				return Double.NEGATIVE_INFINITY;
			bytesRead++;
			if (bytesRead > maxBytesToRead)
				break;

			// ///////////////////////////////////////////////////////////////////
			//
			// Eat spaces, tabs carriage return, line feeds.
			//
			// ///////////////////////////////////////////////////////////////////
			if (isWhitespace(b)) {
				if (started) {
					break;
				} else {
					continue;
				}
			}
			// only digits, '+', 'e', 'E', '*', '.' and ',' are allowed
			if ((b < 48 || b > 57) && b != 43 && b != 45 && b != 69 && b != 101
					&& b != 46 && b != 42 && b != 44)
				return Double.NEGATIVE_INFINITY;
			validBytesRead++;

			// ///////////////////////////////////////////////////////////////////
			//
			// Process this character
			//
			// ///////////////////////////////////////////////////////////////////
			if (!doubleConverter.pushChar(b))
				return Double.NEGATIVE_INFINITY;
			started = true;
			if (validBytesRead > maxValueLength)
				break;

		}

		// ///////////////////////////////////////////////////////////////////
		//
		// Retrieve the value
		//
		// ///////////////////////////////////////////////////////////////////
		retVal = doubleConverter.compute();
		doubleConverter.reset();
		return retVal;
	}

	/**
	 * @return true if ch is a space, a tab, new line or carriage return
	 */
	private boolean isWhitespace(int ch) {
		return ch == 32 || ch == 10 || ch == 13 || ch == 9;
	}

	/**
	 * Retrieves a key from the ascii grid header using the provided
	 * {@link ImageInputStream} and by reading at most
	 * <code>maxBytesToRead</code> from the provided input stream.
	 * 
	 * <p>
	 * The maximum length of the key we are looking for is
	 * <code>maxKeyLength</code>.
	 * 
	 * <p>
	 * The <code>specialChar</code> input params can be used to recognize
	 * special input character beyond the usual ones [A-Z], [a-z], _.
	 * 
	 * <p>
	 * The header of an ascii grid file, ESRI or GRASS is composed by a certain
	 * number of lines of the form:
	 * 
	 * key value _new_line_ key value _new_line_
	 * 
	 * Purpose of this method is to look for the header's keys.
	 * 
	 * @param inStream
	 *            is the {@link ImageInputStream} to read from.
	 * @param maxBytesToRead
	 *            is maximum number of bytes to consume from the provided
	 *            {@link ImageInputStream}.
	 * @param maxKeyLength
	 *            indicates the maximum expected length of they key we are
	 *            looking for.
	 * @param specialChar
	 *            can be used to indicate a special character to decode (see
	 *            above).
	 * @return a {@link String} containing either a key or an empty string.
	 * @throws IOException
	 */
	String getKey(final ImageInputStream inStream, int maxBytesToRead,
			int maxKeyLength, byte specialChar) throws IOException {

		byte b;
		StringBuffer buffer = new StringBuffer(10);
		boolean started = false;
		int bytesRead = 0, validBytesRead = 0;
		while (true) {
			b = (byte) (inStream.read() & 0xff);

			// //
			//
			// Is it something to use or not.
			//
			// //
			if (b == -1)
				// send error on end of file
				break;
			bytesRead++;
			if (bytesRead >= maxBytesToRead)
				break;

			// //
			//
			// Eat spaces, tabs carriage return, line feeds.
			//
			// //
			if (b == 32 || b == 10 || b == 13 || b == 9) {
				if (!started)
					continue;

				else {
					break;
				}
			}
			// //
			//
			// Valid chars are a-z A-Z, _
			//
			// //
			if (specialChar > 127) {
				if (!(b == 95 || (b >= 97 && b <= 122) || (b >= 64 && b <= 90)))
					break;
			} else if (!(b == 95 || b == specialChar || (b >= 97 && b <= 122) || (b >= 64 && b <= 90)))
				break;

			// //
			//
			// Append this character
			//
			// //
			validBytesRead++;
			buffer.append(new String(new byte[] { b }, "ascii"));
			started = true;
			if (validBytesRead >= maxKeyLength)
				break;
		}

		return buffer.toString();
	}
}
