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
package it.geosolutions.imageio.gdalframework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.spi.ImageWriterSpi;

/**
 * The abstract service provider interface (SPI) for {@link GDALImageWriter}s.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class GDALImageWriterSpi extends ImageWriterSpi {

    static {
        GDALUtilities.loadGDAL();
    }

    /**
     * <code>List</code> of gdal formats supported by this plugin.
     */
    private List supportedFormats;

    public GDALImageWriterSpi(String vendorName, String version,
            String[] names, String[] suffixes, String[] MIMETypes,
            String writerClassName, Class[] outputTypes,
            String[] readerSpiNames,
            boolean supportsStandardStreamMetadataFormat,
            String nativeStreamMetadataFormatName,
            String nativeStreamMetadataFormatClassName,
            String[] extraStreamMetadataFormatNames,
            String[] extraStreamMetadataFormatClassNames,
            boolean supportsStandardImageMetadataFormat,
            String nativeImageMetadataFormatName,
            String nativeImageMetadataFormatClassName,
            String[] extraImageMetadataFormatNames,
            String[] extraImageMetadataFormatClassNames, List supportedFormats) {

        super(
                vendorName,
                version,
                names,
                suffixes,
                MIMETypes,
                writerClassName, // writer class name
                STANDARD_OUTPUT_TYPE,
                readerSpiNames, // reader spi names
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
        this.supportedFormats = new ArrayList(supportedFormats);
    }

    /**
     * Methods returning the formats which are supported by a plugin.
     * 
     * The right value to be returned may be found using the GDAL command:
     * <code> gdalinfo --formats</code> which lists all the supported formats.
     * 
     * As an instance, the result of this command may be:
     * 
     * VRT (rw+): Virtual Raster GTiff (rw+): GeoTIFF NITF (rw+): National
     * Imagery Transmission Format HFA (rw+): Erdas Imagine Images (.img)
     * SAR_CEOS (ro): CEOS SAR Image CEOS (ro): CEOS Image
     * .........................................
     * 
     * You need to set the String returned as the first word (as an instance:
     * "HFA", if you are building a plugin for the Erdas Image datasets)
     * 
     * In some circumstances, GDAL provides more than 1 driver to manage a
     * specific format. As an instance, in order to handle HDF4 files, GDAL
     * provides two drivers: HDF4 and HDF4Image (which supports Dataset
     * creation). The HDF4ImageReader will be capable of manage both formats.
     * 
     */
    public List getSupportedFormats() {
        return Collections.unmodifiableList(this.supportedFormats);
    }

}
