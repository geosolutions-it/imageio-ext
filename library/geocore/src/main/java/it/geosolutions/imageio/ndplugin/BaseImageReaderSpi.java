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
package it.geosolutions.imageio.ndplugin;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public abstract class BaseImageReaderSpi extends ImageReaderSpi {

	protected static final Class<?>[] DIRECT_STANDARD_INPUT_TYPES = new Class[] {FileImageInputStreamExt.class,ImageInputStream.class,File.class,URL.class,URI.class };
	
	protected static final Logger LOGGER = Logger.getLogger(BaseImageReaderSpi.class.toString());
	
	protected static final String vendorName = "GeoSolutions";

	public BaseImageReaderSpi() {
		super();
	}

	public BaseImageReaderSpi(String vendorName, String version,
			String[] names, String[] suffixes, String[] MIMETypes,
			String readerClassName, Class<?>[] inputTypes,
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
		super(vendorName, version, names, suffixes, MIMETypes, readerClassName,
				inputTypes, writerSpiNames,
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

}