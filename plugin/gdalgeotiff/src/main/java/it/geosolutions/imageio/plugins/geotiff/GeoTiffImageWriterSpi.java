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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.gdalframework.GDALImageWriterSpi;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates a {@link GeoTiffImageWriter} 
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public final class GeoTiffImageWriterSpi extends GDALImageWriterSpi {
	static final String[] suffixes = { "GeoTiff", "tiff" , "tif" };

	static final String[] formatNames = { "Tiff", "GeoTiff"};

	static final String[] MIMETypes = { "image/tiff" };

	static final String version = "1.0";

	static final String writerCN = "it.geosolutions.imageio.plugins.geotiff.GeoTiffImageWriter";

	static final String vendorName = "GeoSolutions";

	// ReaderSpiNames
	static final String[] readerSpiName = { "it.geosolutions.imageio.plugins.geotiff.GeoTiffImageReaderSpi" };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = null;

	static final String[] extraStreamMetadataFormatClassNames = null;

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = null; //"javax.imageio.plugins.geotiff.GeoTiffImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = null; //"javax.imageio.plugins.geotiff.GeoTiffImageMetadataFormat";

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	private boolean registered;
	
	/**
	 * 
	 */
	public GeoTiffImageWriterSpi() {
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
		isSupportingCreate=true;
		isSupportingCreateCopy=true;
	}

	/**
	 * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
	 */
	public ImageWriter createWriterInstance(Object extension)
			throws IOException {
		return new GeoTiffImageWriter(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return "SPI for GeoTiff ImageWriter";
	}

	public boolean canEncodeImage(ImageTypeSpecifier type) {
		// XXX
		return true;
	}
	
	public void onRegistration(ServiceRegistry registry, Class category) {
		 super.onRegistration(registry, category);
		if (registered) 
			return;

		registered = true;
		Iterator writers = GDALUtilities.getJDKImageReaderWriterSPI(registry, "TIFF",
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
	
	
}



