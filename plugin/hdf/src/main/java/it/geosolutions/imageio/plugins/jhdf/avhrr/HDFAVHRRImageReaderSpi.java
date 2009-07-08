/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.imageio.plugins.jhdf.avhrr;

import it.geosolutions.hdf.object.h4.H4File;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.hdf.object.h4.H4Utilities;
import it.geosolutions.imageio.ndplugin.BaseImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;

public class HDFAVHRRImageReaderSpi extends BaseImageReaderSpi {

    static final String[] suffixes = { "hdf", "hdf4" };

    static final String[] formatNames = { "HDF", "HDF4" };

    static final String[] mimeTypes = { "image/hdf" };

    static final String version = "1.0";

    static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.avhrr.HDFAVHRRImageReader";

    static final String vendorName = "GeoSolutions";

    // writerSpiNames
    static final String[] wSN = { null };

    // StreamMetadataFormatNames and StreamMetadataFormatClassNames
    static final boolean supportsStandardStreamMetadataFormat = false;

    static final String nativeStreamMetadataFormatName = HDFAVHRRStreamMetadata.nativeMetadataFormatName;

    static final String nativeStreamMetadataFormatClassName = null;

    static final String[] extraStreamMetadataFormatNames = { null };

    static final String[] extraStreamMetadataFormatClassNames = { null };

    // ImageMetadataFormatNames and ImageMetadataFormatClassNames
    static final boolean supportsStandardImageMetadataFormat = false;

    static final String nativeImageMetadataFormatName = HDFAVHRRImageMetadata.nativeMetadataFormatName;

    static final String nativeImageMetadataFormatClassName = null;

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    public HDFAVHRRImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                mimeTypes,
                readerCN, // readerClassName
                DIRECT_STANDARD_INPUT_TYPES,
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
                extraImageMetadataFormatClassNames);
    }

    public boolean canDecodeInput(Object input) throws IOException {
        boolean found = false;
        if (!H4Utilities.isJHDFLibAvailable())
            return false;
        if (input instanceof FileImageInputStreamExtImpl) {
            input = ((FileImageInputStreamExtImpl) input).getFile();
        }

        if (input instanceof File) {
            try {
                final String filepath = ((File) input).getPath();
                final H4File h4File = new H4File(filepath);
                if (h4File != null) {
                    final H4SDSCollection sdscoll = h4File.getH4SdsCollection();
                    final int productsNum = HDFAVHRRProperties.avhrrProducts
                            .getNProducts();
                    for (int i = 0; i < productsNum; i++) {
                        if (sdscoll.get(HDFAVHRRProperties.avhrrProducts
                                .get(i).getProductName()) != null) {
                            found = true;
                            break;
                        }

                    }

                    h4File.dispose();
                }
            } catch (IllegalArgumentException e) {
                found = false;
            }
        }
        return found;
    }

    /**
     * Returns an instance of the HDFImageReader
     * 
     * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
     */
    public ImageReader createReaderInstance(Object source) throws IOException {
        return new HDFAVHRRImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return new StringBuffer("AVHRR Compliant HDF Image Reader, version ")
                .append(version).toString();
    }

}