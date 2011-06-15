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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageWriterSpi;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates a {@link JP2GDALKakaduImageWriter} if it is able to decode the
 * input provided.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class JP2GDALKakaduImageWriterSpi extends GDALImageWriterSpi {
    private static final String[] formatNames = {"jpeg 2000", "JPEG 2000", "jpeg2000", "JPEG2000"};
    
    private static final String[] extensions = {"jp2"}; // Should add jpx or jpm
    
    private static final String[] mimeTypes = {"image/jp2", "image/jpeg2000"};

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

    static final String nativeImageMetadataFormatName = null;

    static final String nativeImageMetadataFormatClassName = null;

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    /**
     * 
     */
    public JP2GDALKakaduImageWriterSpi() {
        super(vendorName, version, formatNames, extensions, mimeTypes, writerCN,
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
                extraImageMetadataFormatClassNames, Collections
                        .singletonList("JP2KAK"));
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
        return true;
    }
}
