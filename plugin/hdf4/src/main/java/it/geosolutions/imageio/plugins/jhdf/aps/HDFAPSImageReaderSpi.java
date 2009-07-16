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
package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.ndplugin.BaseImageReaderSpi;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;

import ncsa.hdf.hdflib.HDFException;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Service provider interface for the APS-HDF Image
 * 
 * @author Daniele Romagnoli
 */
public class HDFAPSImageReaderSpi extends BaseImageReaderSpi {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jhdf.aps");

    static final String[] suffixes = { "hdf", "hdf4" };

    static final String[] formatNames = { "HDF-APS" };

    static final String[] MIMETypes = { "image/hdf", "image/hdf4" };

    static final String version = "1.0";

    static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.aps.HDFAPSImageReader";

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

    public HDFAPSImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
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

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("HDFAPSImageReaderSpi Constructor");
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
                	final String attrib = NetCDFUtilities.getGlobalAttributeAsString(dataset, HDFAPSProperties.STD_FA_CREATESOFTWARE); 
                    if (attrib != null && attrib.length()>0 && attrib.startsWith("APS"))
                                    found = true;
                    dataset.close();
                }
            } catch (IllegalArgumentException e) {
                found = false;
            }
        }
        return found;
    }

    public ImageReader createReaderInstance(Object input) throws IOException {
        return new HDFAPSImageReader(this);
    }

    public String getDescription(Locale locale) {
        return "HDF-APS Image Reader, version " + version;
    }
}
