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

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

/**
 * The abstract service provider interface (SPI) for {@link GDALImageReader}s.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */

public abstract class GDALImageReaderSpi extends ImageReaderSpi {

	protected static boolean available;

	/** <code>true</code> if the specific format supports subdatasets */
	protected boolean supportsSubDataSets;

	/**
	 * Some formats use tiles having size N*1 (tiles are simple rows). To avoid
	 * a wasting number of read operations (a read for each row), it should be
	 * better to refine tile sizes. This field will be <code>true</code> if
	 * the specific format needs tile tuning.
	 */
	protected boolean needsTileTuning;

	/**
	 * Methods returning the formats which are supported by a plugin.
	 * 
	 * The right value to be returned may be found using the GDAL command:
	 * <code> gdalinfo --formats</code> which lists all the supported formats.
	 * 
	 * As an instance, the result of this command may be:
	 * 
	 * VRT (rw+): Virtual Raster GTiff (rw+): GeoTIFF NITF (rw+): National
	 * Imagery Transmission Format HFA (rw+): Erdas Imagine Images (.img)
	 * SAR_CEOS (ro): CEOS SAR Image CEOS (ro): CEOS Image
	 * .........................................
	 * 
	 * You need to set the String returned as the first word (as an instance:
	 * "HFA", if you are building a plugin for the Erdas Image Images)
	 * 
	 * In some circumstances, GDAL provides more than 1 driver to manage a
	 * specific format. As an instance, in order to handle HDF4 files, GDAL
	 * provides two drivers: HDF4 and HDF4Image (which supports Dataset
	 * creation). The HDF4ImageReader will be capable of manage both formats.
	 * 
	 * To specify different formats, just separate them by the ";" symbol. As an
	 * instance: "HDF4;HDF4Image"
	 * 
	 */
	protected abstract String getSupportedFormats();

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.gdalframework");

	static {
		try {
			System.loadLibrary("gdaljni");
			gdal.AllRegister();
			GDALImageReaderSpi.available = true;
		} catch (UnsatisfiedLinkError e) {

			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.severe(new StringBuffer("Native library load failed.")
						.append(e.toString()).toString());
			GDALImageReaderSpi.available = false;
		}
	}

	public GDALImageReaderSpi(String vendorName, String version,
			String[] names, String[] suffixes, String[] MIMETypes,
			String readerClassName, Class[] inputTypes,
			String[] writerSpiNames,
			boolean supportsStandardStreamMetadataFormat,
			String nativeStreamMetadataFormatName,
			String nativeStreamMetadataFormatClassName,
			String[] extraStreamMetadataFormatNames,
			String[] extraStreamMetadataFormatClassNames,
			boolean supportsStandardImageMetadataFormat,
			String nativeImageMetadataFormatName,
			String nativeImageMetadataFormatClassName,
			String[] extraImageMetadataFormatNames,
			String[] extraImageMetadataFormatClassNames) {

		super(
				vendorName,
				version,
				names,
				suffixes,
				MIMETypes,
				readerClassName, // readerClassName
				new Class[] { File.class, FileImageInputStreamExt.class },
				writerSpiNames, // writer Spi Names
				supportsStandardStreamMetadataFormat,
				nativeStreamMetadataFormatName,
				nativeStreamMetadataFormatClassName,
				extraStreamMetadataFormatNames,
				extraStreamMetadataFormatClassNames,
				supportsStandardImageMetadataFormat,
				nativeImageMetadataFormatName,
				nativeImageMetadataFormatClassName,
				extraImageMetadataFormatNames,
				extraImageMetadataFormatClassNames);
	}

	/**
	 * In case the specific {@link GDALImageReader}'s implementation supports
	 * subdatasets, this method return <code>true</code>.
	 * 
	 * @return <code>true</code> in case the specific format supports
	 *         subdatasets
	 * 
	 * NOTE: When defining a specific {@link GDALImageReaderSpi} implementation,
	 * be sure you properly initialize this field in the SPI constructor,
	 * depending on the capabilities of the format for which you are
	 * implementing the new class.
	 */
	public boolean supportsSubdatasets() {
		return supportsSubDataSets;
	}

	/**
	 * In case the {@link GDALImageReader}'s implementation for a specific
	 * format requires tile tuning, this method return <code>true</code>.
	 * 
	 * @return <code>true</code> in case the {@link GDALImageReader}'s
	 *         implementation for a specific format requires tile tuning.
	 * 
	 * NOTE: When defining a specific {@link GDALImageReaderSpi} implementation,
	 * be sure you properly initialize this field in the SPI constructor,
	 * depending on the capabilities of the format for which you are
	 * implementing the new class.
	 */
	public boolean needsTilesTuning() {
		return needsTileTuning;
	}

	/**
	 * Checks if the provided input can be decoded by the specific SPI. When
	 * building a new plugin, remember to implement the
	 * <code>getSupportedFormat</coded> abstract method.
	 * 
	 * @return 
	 * 		true if the input can be successfully decoded.
	 *
	 */
	public boolean canDecodeInput(Object input) throws IOException {
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Can Decode Input");

		if (input instanceof ImageInputStream)
			((ImageInputStream) input).mark();

		/**
		 * Checking input source types and creating an ImageInputStream
		 */

		// if input source is a string,
		// convert input from String to File
		if (input instanceof String)
			input = new File((String) input);

		// if input source is an URL, open an InputStream
		if (input instanceof URL) {
			final URL tempURL = (URL) input;
			if (tempURL.getProtocol().equalsIgnoreCase("file"))
				input = new File(URLDecoder.decode(tempURL.getFile(), "UTF8"));
			else
				input = ((URL) input).openStream();
		}

		// if input source is a File,
		// convert input from File to FileInputStream
		if (input instanceof File)
			input = new FileImageInputStreamExtImpl((File) input);

		boolean isInputDecodable = false;
		// Checking if this specific SPI can decode the provided input
		try {
			String s = ((FileImageInputStreamExtImpl) input).getFile()
					.getAbsolutePath();

			Dataset ds = GDALUtilities.acquireDataSet(s, gdalconst.GA_ReadOnly);
			isInputDecodable = isDecodable(ds);

			// Closing the dataset
			GDALUtilities.closeDataSet(ds);
		} catch (Exception e) {

		}
		return isInputDecodable;
	}

	/**
	 * Checks if the provided Dataset was opened by a Driver supporting the same
	 * formats which are supported by the specific ImageReaderSpi.
	 * 
	 * There is a trivial example: Suppose we are implementing a plugin for HDF4
	 * format and suppose we are testing the <code>canDecodeInput</code> with
	 * a NITF file as input. GDAL will successfully open the NITF file. However,
	 * it will use the NITF driver instead of the HDF4 driver. Since NITF is not
	 * supported by the HDF4ImageReaderSpi, this method will return return
	 * <code>false</code>.
	 * 
	 * @param dataset
	 *            The input dataset
	 * 
	 * @return <code>true</code> if the format is supported.
	 *         <code>false</code> otherwise.
	 */
	protected boolean isDecodable(Dataset dataset) {
		if (dataset != null) {
			Driver driver = dataset.GetDriver();

			// retrieving the format of the provided input.
			// We use the "Description" of the driver which has opened the
			// input.
			final String sDriver = driver.GetDescription();

			// ////////////////////////////////////////////////////////////////
			// checking if this format is supported by the specific SPI */
			// ////////////////////////////////////////////////////////////////

			// Some plugins may support more than 1 Format (Driver).
			// As an Instance, the HDF4ImageReader supports HDF4 and HDF4Image
			// Formats
			String formats = getSupportedFormats();
			int hasManyFormats = formats.indexOf(';');
			if (hasManyFormats == -1) {
				// The plugin supports only one format
				if (sDriver.equals(formats))
					return true;
			} else {
				// The plugin supports different formats
				while (hasManyFormats != -1) {
					if (sDriver.equals(formats.substring(0, hasManyFormats)))
						return true;
					formats = formats.substring(hasManyFormats + 1, formats
							.length());
					hasManyFormats = formats.indexOf(';');
				}
				// I need to check the last string available
				if (sDriver.equals(formats))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the native library has been loaded.
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the native library has been loaded.
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isAvailable() {
		return available;
	}

	/**
	 * Returns <code>true</code> if a driver for the specific format is
	 * available. <code>false</code> otherwise.<BR>
	 * It is worth to point out that a successfull loading of the native library
	 * is not sufficient to grant the support for a specific format. We should
	 * also check if the proper driver is available.
	 * 
	 * @return <code>true</code> if a driver for the specific format is
	 *         available. <code>false</code> otherwise.<BR>
	 */
	public boolean isDriverAvailable() {
		String formats = getSupportedFormats();
		int hasManyFormats = formats.indexOf(';');
		if (hasManyFormats == -1) {
			// The plugin supports only one format
			Driver driver = gdal.GetDriverByName(formats);
			if (driver != null)
				return true;
		} else {
			// The plugin supports different formats
			while (hasManyFormats != -1) {
				Driver driver = gdal.GetDriverByName(formats.substring(0,
						hasManyFormats));
				if (driver != null)
					return true;
				formats = formats.substring(hasManyFormats + 1, formats
						.length());
				hasManyFormats = formats.indexOf(';');
			}
			// I need to check the last string available
			Driver driver = gdal.GetDriverByName(formats);
			if (driver != null)
				return true;
		}
		return false;
	}

}
