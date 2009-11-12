/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.hdf4.terascan;

import it.geosolutions.imageio.ndplugin.BaseImageReaderSpi;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;

import ucar.nc2.dataset.NetcdfDataset;

public class HDF4TeraScanImageReaderSpi extends BaseImageReaderSpi {

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

    static final String nativeStreamMetadataFormatName = HDF4TeraScanStreamMetadata.nativeMetadataFormatName;

    static final String nativeStreamMetadataFormatClassName = null;

    static final String[] extraStreamMetadataFormatNames = { null };

    static final String[] extraStreamMetadataFormatClassNames = { null };

    // ImageMetadataFormatNames and ImageMetadataFormatClassNames
    static final boolean supportsStandardImageMetadataFormat = false;

    static final String nativeImageMetadataFormatName = HDF4TeraScanImageMetadata.nativeMetadataFormatName;

    static final String nativeImageMetadataFormatClassName = null;

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    public HDF4TeraScanImageReaderSpi() {
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

        if (input instanceof FileImageInputStreamExtImpl) {
            input = ((FileImageInputStreamExtImpl) input).getFile();
        }

        if (input instanceof File) {
            try {
                final NetcdfDataset dataset = NetCDFUtilities.getDataset(input);
                if (dataset != null) {
                	final int productsNum = HDF4TeraScanProperties.avhrrProducts.getNProducts();
                	for (int i = 0; i < productsNum; i++) {
                		if (dataset.findVariable(HDF4TeraScanProperties.avhrrProducts
                                .get(i).getProductName())!=null){
                			found = true;
                			break;
                		}
                			
                	}
                	dataset.close();
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
        return new HDF4TeraScanImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return new StringBuffer("AVHRR Compliant HDF Image Reader, version ")
                .append(version).toString();
    }

}