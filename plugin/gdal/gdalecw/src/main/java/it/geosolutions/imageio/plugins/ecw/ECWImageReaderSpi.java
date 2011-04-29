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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;
import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

/**
 * Service provider interface for the ECW Image
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class ECWImageReaderSpi extends GDALImageReaderSpi {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.ecw");

    static final String[] suffixes = { "ecw" };

    static final String[] formatNames = { "ECW", "ECWP" };

    static final String[] MIMETypes = { "image/ecw" };

    static final String version = "1.0";
    
    static final String description = "ECW Image Reader, version " + version;

    static final String readerCN = "it.geosolutions.imageio.plugins.ecw.ECWImageReader";

    static final String vendorName = "GeoSolutions";

    // writerSpiNames
    static final String[] wSN = {/* "javax.imageio.plugins.ecw.ECWImageWriterSpi" */null };

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

    public ECWImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN, // readerClassName
                new Class[] { File.class, FileImageInputStreamExt.class, 
                        ECWPImageInputStream.class },
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
                        .singletonList("ECW"));

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("ECWImageReaderSpi Constructor");
    }

    /**
     * This method checks if the provided input can be decoded from this SPI
     */
    public boolean canDecodeInput(Object input) throws IOException {
        if (input instanceof ECWPImageInputStream) {
            String ecwp = ((ECWPImageInputStream) input).getECWPLink();
            boolean isDecodeable = false;
            if (ecwp != null) {
                final Dataset ds = GDALUtilities.acquireDataSet(ecwp,
                        gdalconst.GA_ReadOnly);
                if (ds != null)
                    isDecodeable = isDecodable(ds);
            }
            return isDecodeable;
        } else
            return super.canDecodeInput(input);

    }

    /**
     * Returns an instance of the ECWImageReader
     * 
     * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
     */
    public ImageReader createReaderInstance(Object source) throws IOException {
        return new ECWImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return description;
    }

}
