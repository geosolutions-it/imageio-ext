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
package it.geosolutions.imageio.plugins.jpeg;

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
 * Service provider interface for jpeg images
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JpegGDALImageReaderSpi extends GDALImageReaderSpi {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jpeg");

    static final String[] suffixes = { "jpg", "jpeg" };

    static final String[] formatNames = { "JPEG" };

    static final String[] MIMETypes = { "image/jpeg", "image/jpg" };

    static final String version = "1.0";

    static final String description = "JPEG Image Reader, version " + version;
    
    static final String readerCN = "it.geosolutions.imageio.plugins.jpeg.JpegGDALImageReader";

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

    static final String nativeImageMetadataFormatName = null;

    static final String nativeImageMetadataFormatClassName = null;

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    // private boolean registered;

    public JpegGDALImageReaderSpi() {
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
                        .singletonList("JPEG"));

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("JpegGDALImageReaderSpi Constructor");

    }

    /**
     * This method checks if the provided input can be decoded from this SPI
     */
    public boolean canDecodeInput(Object input) throws IOException {
        return super.canDecodeInput(input);
    }

    /**
     * Returns an instance of the JpegGDALImageReader
     * 
     * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
     */
    public ImageReader createReaderInstance(Object source) throws IOException {
        return new JpegGDALImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return description;
    }

    // /**
    // * Upon registration, this method ensures that this SPI is listed at the
    // top
    // * of the ImageReaderSpi items, so that it will be invoked before the
    // * default ImageReaderSpi
    // *
    // * @param registry
    // * ServiceRegistry where this object has been registered.
    // * @param category
    // * a Class object indicating the registry category under which
    // * this object has been registered.
    // */
    // public void onRegistration(ServiceRegistry registry, Class category) {
    // super.onRegistration(registry, category);
    // if (registered) {
    // return;
    // }
    // registered = true;
    // Iterator readers = com.sun.media.imageioimpl.common.ImageUtil
    // .getJDKImageReaderWriterSPI(registry, "JPEG", true).iterator();
    // ImageReaderSpi spi;
    // while (readers.hasNext()) {
    // spi = (ImageReaderSpi) readers.next();
    // if (spi == this)
    // continue;
    // registry.deregisterServiceProvider(spi);
    // registry.setOrdering(category, this, spi);
    // }
    // }
}
