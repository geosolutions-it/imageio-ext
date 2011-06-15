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
package it.geosolutions.imageio.plugins.arcgrid.spi;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageReader;
import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageWriter;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates an {@link AsciiGridsImageReader} if it is able to decode the input
 * provided.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public final class AsciiGridsImageWriterSpi extends ImageWriterSpi {
	static final String[] suffixes = { "asc", "gz" };

	static final String[] formatNames = { "Ascii ArcInfo", "Ascii GRASS",
			"arcGrid" };

	static final String[] MIMETypes = { "image/asc" };

	static final String version = "1.0";

	static final String writerCN = "it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageWriter";

	static final String vendorName = "GeoSolutions";

	// ReaderSpiNames
	static final String[] rSN = { "it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageReaderSpi" };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = null;

	static final String[] extraStreamMetadataFormatClassNames = null;

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = "it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = "it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadataFormat";

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	/**
	 * 
	 */
	public AsciiGridsImageWriterSpi() {
		super(vendorName, version, formatNames, suffixes, MIMETypes, writerCN, // writer
				// class
				// name
				STANDARD_OUTPUT_TYPE, rSN, // reader spi names
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
	 * 
	 * @see javax.imageio.spi.ImageWriterSpi#canEncodeImage(javax.imageio.ImageTypeSpecifier)
	 */
	public boolean canEncodeImage(ImageTypeSpecifier its) {

		 int bands = its.getNumBands();
		 return bands == 1;
	}

	/**
	 * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
	 */
	public ImageWriter createWriterInstance(Object extension)
			throws IOException {
		return new AsciiGridsImageWriter(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return "SPI for AsciiIMageWriter";
	}
}
