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

import it.geosolutions.imageio.gdalframework.GDALImageWriterSpi;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

import org.gdal.gdal.gdal;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates an {@link AsciiGridsImageReader} if it is able to decode the input
 * provided.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public final class JP2GDALKakaduImageWriterSpi extends GDALImageWriterSpi {
	static final String[] suffixes = { "JP2", "J2C" };

	static final String[] formatNames = { "JP2", "JPEG 2000", "JP2K"};

	static final String[] MIMETypes = { "image/jp2" };

	static final String version = "1.0";

	static final String writerCN = "it.geosolutions.imageio.plugins.jp2kakadu.JP2GDALKakaduImageWriter";

	static final String vendorName = "GeoSolutions";

	// ReaderSpiNames
	static final String[] readerSpiName = { "it.geosolutions.imageio.plugins.jp2kakadu.JP2GDALKakaduImageReaderSpi" };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = null;

	static final String[] extraStreamMetadataFormatClassNames = null;

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = null; //"javax.imageio.plugins.jp2k.JP2KImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = null; //"javax.imageio.plugins.jp2k.JP2KImageMetadataFormat";

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	private boolean registered;
	
	/**
	 * 
	 */
	public JP2GDALKakaduImageWriterSpi() {
		super(vendorName, version, formatNames, suffixes, MIMETypes, writerCN, 
				STANDARD_OUTPUT_TYPE, readerSpiName, 
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
		isSupportingCreateCopy=true;
	}



	/**
	 * 
	 * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
	 */
	public ImageWriter createWriterInstance(Object extension)
			throws IOException {
		return new JP2GDALKakaduImageWriter(this);
	}

	/**
	 * 
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return "SPI for JPEG 2000 ImageWriter";
	}



	public boolean canEncodeImage(ImageTypeSpecifier type) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void onRegistration(ServiceRegistry registry, Class category) {
		super.onRegistration(registry, category);
		if (registered) {
			return;
		}

		registered = true;
		Iterator writers = GDALUtilities.getJDKImageReaderWriterSPI(registry, "JPEG2000",
				false).iterator();

		ImageWriterSpi spi;
		while (writers.hasNext()) {
			spi = (ImageWriterSpi) writers.next();
			if(spi==this)
				continue;
			registry.deregisterServiceProvider(spi);
			registry.setOrdering(category, this, spi);

		}
	}
	
	public final static void setWriteMultithreadingLevel(
			final int threadsNum) {
		if (threadsNum>0)
			gdal.SetConfigOption("KAKADU_WRITE_MULTITHREADING_LEVEL", Integer.toString(threadsNum));
		else
			throw new IllegalArgumentException(
				"KAKADU_WRITE_MULTITHREADING_LEVEL must be positive!");
	
	}
}



