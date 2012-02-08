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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;

/**
 * Service provider interface for MrSid image reader
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class MrSIDImageReaderSpi extends GDALImageReaderSpi {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.mrsid");

    static final String[] suffixes = { "sid", "sdw" };

    static final String[] formatNames = { "MrSID" };

    static final String[] MIMETypes = { "image/sid" };

    static final String version = "1.0";
    
    static final String description = "MrSID Image Reader, version " + version;

    static final String readerCN = "it.geosolutions.imageio.plugins.mrsid.MrSIDImageReader";

    static final String vendorName = "GeoSolutions";

    // writerSpiNames
    static final String[] wSN = { null };

    // StreamMetadataFormatNames and StreamMetadataFormatClassNames
    static final boolean supportsStandardStreamMetadataFormat = false;

    static final String nativeStreamMetadataFormatName = null;

    static final String nativeStreamMetadataFormatClassName = null;

    static final String[] extraStreamMetadataFormatNames = { null };

    static final String[] extraStreamMetadataFormatClassNames = { null };

    // ImageMetadataFormatNames and ImageMetadataFormatClassNames
    static final boolean supportsStandardImageMetadataFormat = false;

    static final String nativeImageMetadataFormatName = MrSIDIIOImageMetadata.mrsidImageMetadataName;

    static final String nativeImageMetadataFormatClassName = MrSIDIIOImageMetadataFormat.class
            .toString();

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    public MrSIDImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN, // readerClassName
                new Class[] { File.class, FileImageInputStreamExt.class },
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
                extraImageMetadataFormatClassNames, Collections
                        .singletonList("MrSID"));
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("MrSIDImageReaderSpi Constructor");

    }

    /**
     * Returns an instance of the MrSIDImageReader
     * 
     * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
     */
    public ImageReader createReaderInstance(Object source) throws IOException {
        return new MrSIDImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return description;
    }
}
