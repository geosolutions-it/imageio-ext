/* JP2K Kakadu Image Writer V. 1.0 
 * 
 * (c) 2008, 2018 OnePacs, LLC, info@onepacs.com
 *
 * Produced by GeoSolutions, Eng. Daniele Romagnoli and Eng. Simone Giannecchini
 * GeoSolutions S.A.S. ---  Via Carignoni 51, 55041 Camaiore (LU) Italy
 * Contact: info@geo-solutions.it
 *
 * Released under the Gnu Lesser General Public License version 3. 
 * All rights otherwise reserved. 
 *
 * JP2K Kakadu Image Writer is distributed on an "AS IS" basis, 
 * WITHOUT ANY WARRANTY, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  
 *
 * See the GNU Lesser General Public License version 3 for more details. 
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package it.geosolutions.imageio.plugins.jp2k;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class JP2KKakaduImageWriterSpi extends ImageWriterSpi {

    static final String[] suffixes = { "JP2", "J2C" };

    static final String[] formatNames = { "jpeg2000", "jpeg 2000", "JPEG2000", "JPEG 2000", "JP2", "JP2K" };

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
    
    static final Class[] OUTPUT_TYPE =  { File.class, ImageOutputStream.class };

    /**
     * Default {@link ImageWriterSpi} constructor for JP2K writers.
     */
    public JP2KKakaduImageWriterSpi() {
        super(vendorName, version, formatNames, suffixes, MIMETypes, writerCN,
                OUTPUT_TYPE, readerSpiName,
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
    public ImageWriter createWriterInstance(Object extension)
            throws IOException {
        return new JP2KKakaduImageWriter(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return "SPI for JPEG 2000 ImageWriter based on KDU JNI";
    }

    /**
     * Refine the check if needed.
     */
    public boolean canEncodeImage(ImageTypeSpecifier type) {
//        final int numBands = type.getNumBands();
//        final int numBits = type.getBitsPerBand(0);
        return true;
    }

}
