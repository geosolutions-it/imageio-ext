/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.jp2k;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

public class JP2KKakaduImageWriterSpi extends ImageWriterSpi {

    static final String[] suffixes = { "JP2", "J2C" };

    static final String[] formatNames = { "JP2", "JPEG 2000", "JP2K" };

    static final String[] MIMETypes = { "image/jp2" };

    static final String version = "1.0";

    static final String writerCN = "it.geosolutions.imageio.plugins.jp2k.JP2kKakaduImageWriter";

    static final String vendorName = "GeoSolutions";

    // ReaderSpiNames
    static final String[] readerSpiName = { "it.geosolutions.imageio.plugins.jp2k.JP2kKakaduImageReaderSpi" };

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
    public JP2KKakaduImageWriterSpi() {
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
    }

    /**
     * 
     * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
     */
    public ImageWriter createWriterInstance(Object extension)
            throws IOException {
        return new JP2KKakaduImageWriter(this);
    }

    /**
     * 
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return "SPI for JPEG 2000 ImageWriter based on KDU JNI";
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
        final int numBands = type.getNumBands();
        final int numBits = type.getBitsPerBand(0);
        return true;
    }

}
