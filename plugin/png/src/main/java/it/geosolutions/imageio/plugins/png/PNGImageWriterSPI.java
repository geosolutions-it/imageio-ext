/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    
 *    (C) 2007 - 2015, GeoSolutions
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
package it.geosolutions.imageio.plugins.png;

import it.geosolutions.imageio.stream.output.ImageOutputStreamAdapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

/**
 * {@link ImageWriterSpi} implementation for the high performance PNG encoder.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class PNGImageWriterSPI extends ImageWriterSpi {

    static final String[] suffixes = { "PNG", "png"};
    
    static final String[] formatNames = { "png", };
    
    static final String[] MIMETypes = { "image/png" };
    
    static final String version = "1.0";
    
    static final String writerCN = "it.geosolutions.imageio.plugins.turbojpeg.PNGImageWriter";
    
    static final String vendorName = "GeoSolutions";
    
    // ReaderSpiNames
    static final String[] readerSpiName = { "it.geosolutions.imageio.plugins.turbojpeg.PNGImageReaderSpi" };
    
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
     * Default Constructor
     */
    public PNGImageWriterSPI() {
        super(vendorName, version, formatNames, suffixes, MIMETypes, writerCN,
                new Class[]{ImageOutputStreamAdapter.class, OutputStream.class, File.class}, readerSpiName,
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
     * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
     */
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new PNGImageWriter(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return "SPI for PNG ImageWriter based on GeoSolutions PNGWriter";
    }

    /**
     * TODO: Refine the check before releasing.
     */
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(type.createBufferedImage(2, 2));
        return scanlines != null;
    }

}
