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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;

import org.gdal.gdal.gdal;

/**
 * Service provider interface for the jp2k image
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class JP2GDALKakaduImageReaderSpi extends GDALImageReaderSpi {

	public static abstract class KakaduErrorManagement {

		public static final int FUSSY = 0;

		public static final int RESILIENT = 1;

		public static final int FAST = 2;
	}

	private static final Logger logger = Logger
			.getLogger("javax.imageio.plugins.jp2k");

	static final String[] suffixes = { "jp2", "jp2" };

	static final String[] formatNames = { "JPEG2000" };

	static final String[] MIMETypes = { "image/jp2", "image/jp2k" };

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.jp2kakadu.JP2GDALKakaduImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = {/* "it.geosolutions.imageio.plugins.jp2kakadu.JP2GDALKakaduImageReaderSpi" */null };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = { null };

	static final String[] extraStreamMetadataFormatClassNames = { null };

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = null;

	static final String nativeImageMetadataFormatClassName = null;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	private boolean registered;

	public JP2GDALKakaduImageReaderSpi() {
		super(
				vendorName,
				version,
				formatNames,
				suffixes,
				MIMETypes,
				readerCN, // readerClassName
				STANDARD_INPUT_TYPE,
				wSN, // writer Spi Names
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

		if (logger.isLoggable(Level.FINE))
			logger.fine("JP2GDALKakaduImageReaderSpi Constructor");

	}

	/**
	 * This method checks if the provided input can be decoded from this SPI
	 */
	public boolean canDecodeInput(Object input) throws IOException {
		return super.canDecodeInput(input);
	}

	/**
	 * Returns an instance of the JP2GDALKakaduImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new JP2GDALKakaduImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return new StringBuffer("JP2K Image Reader, version ").append(version)
				.toString();
	}

	protected String getSupportedFormats() {
		return "JP2KAK";
	}

	/**
	 * Upon registration, this method ensures that this SPI is listed at the top
	 * of the ImageReaderSpi items, so that it will be invoked before the
	 * default ImageReaderSpi
	 * 
	 * @param registry
	 *            ServiceRegistry where this object has been registered.
	 * @param category
	 *            a Class object indicating the registry category under which
	 *            this object has been registered.
	 */
	public void onRegistration(ServiceRegistry registry, Class category) {
		super.onRegistration(registry, category);
		if (registered) {
			return;
		}

		registered = true;

		Iterator readers = GDALUtilities.getJDKImageReaderWriterSPI(registry, "JPEG2000",
				true).iterator();

		ImageReaderSpi spi;
		while (readers.hasNext()) {
			spi = (ImageReaderSpi) readers.next();
			if (spi == this)
				continue;
			registry.deregisterServiceProvider(spi);
			registry.setOrdering(category, this, spi);
		}
	}

	/**
	 * Allows to customize kakadu error management.
	 * 
	 * @param errorManagement
	 * 
	 */
	public final static void setKakaduInputErrorManagement(
			final int errorManagement) {
		switch (errorManagement) {
		case KakaduErrorManagement.FAST:
			gdal.SetConfigOption("KAKADU_ERROR_LEVEL_MANAGEMENT", "FAST");
			break;
		case KakaduErrorManagement.FUSSY:
			gdal.SetConfigOption("KAKADU_ERROR_LEVEL_MANAGEMENT", "FUSSY");
			break;
		case KakaduErrorManagement.RESILIENT:
			gdal.SetConfigOption("KAKADU_ERROR_LEVEL_MANAGEMENT", "RESILIENT");
			break;
		default:
			throw new IllegalArgumentException(
					"KAKADU_ERROR_LEVEL_MANAGEMENT unknown! Provided value is "
							+ errorManagement);

		}
	}

	public final static void setReadMultithreadingLevel(final int threadsNum) {
		if (threadsNum > 0)
			gdal.SetConfigOption("KAKADU_READ_MULTITHREADING_LEVEL", Integer
					.toString(threadsNum));
		else
			throw new IllegalArgumentException(
				"KAKADU_READ_MULTITHREADING_LEVEL must be positive!");
	}

}
