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
package it.geosolutions.imageio.gdalframework;

import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.media.jai.JAI;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
/**
 * Utility class providing a set of static utility methods 
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class GDALUtilities {
	
	/** private constructor to prevent instantiation */
	private GDALUtilities(){}
	
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.gdalframework");

	static {
		try {
			System.loadLibrary("gdaljni");
			gdal.AllRegister();
		} catch (UnsatisfiedLinkError e) {

			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, new StringBuffer(
						"Native library load failed. ").append(
						e.getLocalizedMessage()).toString(), e);
		}
	}

	/**
	 * Simply provides to retrieve the corresponding <code>GDALDataType</code>
	 * for the specified <code>dataBufferType</code>
	 * 
	 * @param dataBufferType
	 *            the <code>DataBuffer</code> type for which we need to
	 *            retrieve the proper <code>GDALDataType</code>
	 * 
	 * @return the proper <code>GDALDataType</code>
	 */
	public final static int retrieveGDALDataBufferType(final int dataBufferType) {
		switch (dataBufferType) {
		case DataBuffer.TYPE_BYTE:
			return gdalconstConstants.GDT_Byte;
		case DataBuffer.TYPE_USHORT:
			return gdalconstConstants.GDT_UInt16;
		case DataBuffer.TYPE_SHORT:
			return gdalconstConstants.GDT_Int16;
		case DataBuffer.TYPE_INT:
			return gdalconstConstants.GDT_Int32;
		case DataBuffer.TYPE_FLOAT:
			return gdalconstConstants.GDT_Float32;
		case DataBuffer.TYPE_DOUBLE:
			return gdalconstConstants.GDT_Float64;
		default:
			return gdalconstConstants.GDT_Unknown;
		}

	}

	/**
	 * Returns the maximum amount of memory available for GDAL caching
	 * mechanism.
	 * 
	 * @return the maximum amount in bytes of memory available.
	 */
	public final static int getCacheMax() {
		return gdal.GetCacheMax();
	}

	/**
	 * Returns the amount of GDAL cache used.
	 * 
	 * @return the amount (bytes) of memory currently in use by the GDAL memory
	 *         caching mechanism.
	 */
	public final static int getCacheUsed() {
		return gdal.GetCacheUsed();
	}

	public final static List getJDKImageReaderWriterSPI(
			ServiceRegistry registry, String formatName, boolean isReader) {

		IIORegistry iioRegistry = (IIORegistry) registry;

		Class spiClass;
		if (isReader)
			spiClass = ImageReaderSpi.class;
		else
			spiClass = ImageWriterSpi.class;

		Iterator iter = iioRegistry.getServiceProviders(spiClass, true); // useOrdering

		String formatNames[];
		ImageReaderWriterSpi provider;

		ArrayList list = new ArrayList();
		while (iter.hasNext()) {
			provider = (ImageReaderWriterSpi) iter.next();

			// Get the formatNames supported by this Spi
			formatNames = provider.getFormatNames();
			final int length = formatNames.length;
			for (int i = 0; i < length; i++) {
				if (formatNames[i].equalsIgnoreCase(formatName)) {
					// Must be a JDK provided ImageReader/ImageWriter
					list.add(provider);
					break;
				}
			}
		}
		return list;
	}

	/**
	 * Sets the GDAL Max Cache Size. You can do this only if caching is enabled.
	 * 
	 * @param maxCacheSize
	 */
	public final static void setCacheMax(int maxCacheSize) {
		gdal.SetCacheMax(maxCacheSize);
	}

	/**
	 * Allows to enable/disable GDAL caching mechanism.
	 * 
	 * @param useCaching
	 *            <code>true</code> to enable GDAL caching. <code>false</code>
	 *            to disable GDAL caching.
	 */
	public final static void setGdalCaching(boolean useCaching) {
		final String sOption = useCaching ? "YES" : "NO";
		gdal.SetConfigOption("GDAL_FORCE_CACHING", sOption);
	}

	public final static synchronized Dataset acquireDataSet(String name, int accessType) {
		return gdal.Open(name, accessType);
	}

	/**
	 * Returns any metadata related to the specified image. The SUBDATASETS
	 * domain is not returned since it is related to the whole stream instead of
	 * a single image.
	 * 
	 * @param dataSetName
	 *            the name of the dataset for which we need to retrieve
	 *            imageMetadata
	 * 
	 * @return a <code>List</code> containing any metadata found.
	 */
	public final static List getGDALImageMetadata(String dataSetName) {

		final Dataset ds = acquireDataSet(dataSetName,
				gdalconst.GA_ReadOnly);
		final List gdalImageMetadata = ds.GetMetadata_List("");
		closeDataSet(ds);
		return gdalImageMetadata;
	}

	public final static synchronized void closeDataSet(Dataset ds) {
		ds.delete();
	}

	/**
	 * Returns the value of a specific metadata item related to the stream. As
	 * an instance, it may be used to find the name or the description of a
	 * specific subdataset.
	 * 
	 * @param metadataName
	 *            the name of the specified metadata item.
	 * @param datasetName
	 * @return the value of the required metadata item.
	 */
	public final static String getStreamMetadataItem(String metadataName,
			String datasetName) {
		return getMetadataItem(getGDALStreamMetadata(datasetName), metadataName);
	}

	/**
	 * Returns the value of a specific metadata item contained in the metadata
	 * given as first input parameter
	 * 
	 * @param gdalImageMetadata
	 *            the required metadata <code>List</code>
	 *            (gdalStreamMetadata or gdalImageMetadata)
	 * @param metadataName
	 *            the name of the specified metadata item
	 * @return the value of the specified metadata item
	 */
	public final static String getMetadataItem(List imageMetadata,
			String metadataName) {
		final Iterator it = imageMetadata.iterator();
		// Metadata items scanning
		while (it.hasNext()) {
			String s = (String) it.next();
			int indexOfEqualSymbol = s.indexOf('=');
			String sName = s.substring(0, indexOfEqualSymbol);
			if (sName.equals(metadataName))
				return s.substring(indexOfEqualSymbol + 1, s.length());
		}
		return null;
	}

	/**
	 * Returns any metadata which is not related to a specific image. The actual
	 * implementation provide to return only the SUBDATASETS domain metadata but
	 * it may be changed in future.
	 * 
	 * @param datasetName
	 * 
	 * @return a <code>List</code> containing metadata related to the
	 *         stream.
	 */
	public final static List getGDALStreamMetadata(String datasetName) {

		final Dataset ds = acquireDataSet(datasetName,
				gdalconst.GA_ReadOnly);
		List gdalStreamMetadata = ds.GetMetadata_List("SUBDATASETS");
		closeDataSet(ds);
		return gdalStreamMetadata;

	}

	/**
	 * Set the value of a specific attribute of a specific
	 * <code>IIOMetadataNode</code>
	 * 
	 * @param name
	 *            The name of the attribute which need to be set
	 * @param val
	 *            The value we want to set
	 * @param node
	 *            The <code>IIOMetadataNode</code> having the attribute we are
	 *            going to set
	 * @param attributeType
	 *            The type of the attribute we are going to set
	 */
	public final static void setNodeAttribute(String name, Object val,
			IIOMetadataNode node, int attributeType) {

		try {
			if (val != null && val instanceof String) {
				final String value = (String) val;
				if (value != null && value.length() > 0) {
					switch (attributeType) {
					case IIOMetadataFormat.DATATYPE_DOUBLE:
						// check that we can parse it
						Double.parseDouble(value);
						break;
					case IIOMetadataFormat.DATATYPE_FLOAT:
						// check that we can parse it
						Float.parseFloat(value);
						break;
					case IIOMetadataFormat.DATATYPE_INTEGER:
						// check that we can parse it
						Integer.parseInt(value);
						break;
					case IIOMetadataFormat.DATATYPE_BOOLEAN:
						// check that we can parse it
						Boolean.valueOf(value);
						break;
					case IIOMetadataFormat.DATATYPE_STRING:
						// do nothing
						break;
					default:
						// prevent the code from setting the value
						throw new RuntimeException();
					}
					node.setAttribute(name, value);
					return;
				}
			}
		} catch (Exception e) {
			// do nothing simply proceed.
		}
		// DEFAULT VALUE FOR ATTRIBUTES IS ""
		node.setAttribute(name, "");
	}

	/**
	 * The default tile size. This default tile size can be overriden with a
	 * call to {@link JAI#setDefaultTileSize}.
	 */
	public static final Dimension DEFAULT_TILE_SIZE = new Dimension(512, 512);

	/**
	 * The minimums tile size.
	 */
	public static final int MIN_TILE_SIZE = 256;

	/**
	 * Line separator String. "\r\n" or "\r" or "\n" depending on the Operating
	 * System
	 */
	final static String NEWLINE = System.getProperty("line.separator");

	/**
	 * This set of constants may be used to perform some optmization on tile
	 * sizing
	 */
	public final static int optimalTileMemorySize = 1024 * 1024;

	public final static int optimalTileSize = 512;

	/**
	 * Suggests a tile size for the specified image size. On input, {@code size}
	 * is the image's size. On output, it is the tile size. This method write
	 * the result directly in the supplied object and returns {@code size} for
	 * convenience.
	 * <p>
	 * This method it aimed to computing a tile size such that the tile grid
	 * would have overlapped the image bound in order to avoid having tiles
	 * crossing the image bounds and being therefore partially empty. This
	 * method will never returns a tile size smaller than
	 * {@value #MIN_TILE_SIZE}. If this method can't suggest a size, then it
	 * left the corresponding {@code size} field ({@link Dimension#width width}
	 * or {@link Dimension#height height}) unchanged.
	 * <p>
	 * The {@link Dimension#width width} and {@link Dimension#height height}
	 * fields are processed independently in the same way. The following
	 * discussion use the {@code width} field as an example.
	 * <p>
	 * This method inspects different tile sizes close to the
	 * {@linkplain JAI#getDefaultTileSize() default tile size}. Lets
	 * {@code width} be the default tile width. Values are tried in the
	 * following order: {@code width}, {@code width+1}, {@code width-1},
	 * {@code width+2}, {@code width-2}, {@code width+3}, {@code width-3},
	 * <cite>etc.</cite> until one of the following happen:
	 * <p>
	 * <ul>
	 * <li>A suitable tile size is found. More specifically, a size is found
	 * which is a dividor of the specified image size, and is the closest one of
	 * the default tile size. The {@link Dimension} field ({@code width} or
	 * {@code height}) is set to this value.</li>
	 * 
	 * <li>An arbitrary limit (both a minimums and a maximums tile size) is
	 * reached. In this case, this method <strong>may</strong> set the
	 * {@link Dimension} field to a value that maximize the remainder of
	 * <var>image size</var> / <var>tile size</var> (in other words, the size
	 * that left as few empty pixels as possible).</li>
	 * </ul>
	 */
	public final static Dimension toTileSize(final Dimension size) {
		Dimension defaultSize = JAI.getDefaultTileSize();
		if (defaultSize == null) {
			defaultSize = DEFAULT_TILE_SIZE;
		}
		int s;
		if ((s = toTileSize(size.width, defaultSize.width)) != 0)
			size.width = s;
		if ((s = toTileSize(size.height, defaultSize.height)) != 0)
			size.height = s;
		return size;
	}

	/**
	 * Suggests a tile size close to {@code tileSize} for the specified
	 * {@code imageSize}. This method it aimed to computing a tile size such
	 * that the tile grid would have overlapped the image bound in order to
	 * avoid having tiles crossing the image bounds and being therefore
	 * partially empty. This method will never returns a tile size smaller than
	 * {@value #MIN_TILE_SIZE}. If this method can't suggest a size, then it
	 * returns 0.
	 * 
	 * @param imageSize
	 *            The image size.
	 * @param tileSize
	 *            The preferred tile size, which is often
	 *            {@value #DEFAULT_TILE_SIZE}.
	 */
	public final static int toTileSize(final int imageSize, final int tileSize) {
		final int MAX_TILE_SIZE = Math.min(tileSize * 2, imageSize);
		final int stop = Math.max(tileSize - MIN_TILE_SIZE, MAX_TILE_SIZE
				- tileSize);
		int sopt = 0; // An "optimal" tile size, to be used if no exact
		// dividor is found.
		int rmax = 0; // The remainder of 'imageSize / sopt'. We will try to
		// maximize this value.
		/*
		 * Inspects all tile sizes in the range [MIN_TILE_SIZE ..
		 * MAX_TIME_SIZE]. We will begin with a tile size equals to the
		 * specified 'tileSize'. Next we will try tile sizes of 'tileSize+1',
		 * 'tileSize-1', 'tileSize+2', 'tileSize-2', 'tileSize+3', 'tileSize-3',
		 * etc. until a tile size if found suitable.
		 * 
		 * More generally, the loop below tests the 'tileSize+i' and
		 * 'tileSize-i' values. The 'stop' constant was computed assuming that
		 * MIN_TIME_SIZE < tileSize < MAX_TILE_SIZE. If a tile size is found
		 * which is a dividor of the image size, than that tile size (the
		 * closest one to 'tileSize') is returned. Otherwise, the loop continue
		 * until all values in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE] were
		 * tested. In this process, we remind the tile size that gave the
		 * greatest reminder (rmax). In other words, this is the tile size with
		 * the smallest amount of empty pixels.
		 */
		for (int i = 0; i <= stop; i++) {
			int s;
			if ((s = tileSize + i) <= MAX_TILE_SIZE) {
				final int r = imageSize % s;
				if (r == 0) {
					// Found a size >= to 'tileSize' which is a dividor of image
					// size.
					return s;
				}
				if (r > rmax) {
					rmax = r;
					sopt = s;
				}
			}
			if ((s = tileSize - i) >= MIN_TILE_SIZE) {
				final int r = imageSize % s;
				if (r == 0) {
					// Found a size <= to 'tileSize' which is a dividor of image
					// size.
					return s;
				}
				if (r > rmax) {
					rmax = r;
					sopt = s;
				}
			}
		}
		/*
		 * No dividor were found in the range [MIN_TILE_SIZE .. MAX_TIME_SIZE].
		 * At this point 'sopt' is an "optimal" tile size (the one that left as
		 * few empty pixel as possible), and 'rmax' is the amount of non-empty
		 * pixels using this tile size. We will use this "optimal" tile size
		 * only if it fill at least 75% of the tile. Otherwise, we arbitrarily
		 * consider that it doesn't worth to use a "non-standard" tile size. The
		 * purpose of this arbitrary test is again to avoid too many small tiles
		 * (assuming that
		 */
		return (rmax >= tileSize - tileSize / 4) ? sopt : 0;
	}
}
