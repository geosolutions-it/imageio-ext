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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.ndplugin.BaseImageReaderSpi;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.iosp.hdf4.H4iosp;

/**
 * Service provider interface for the APS-HDF Image
 * 
 * @author Daniele Romagnoli
 */
public abstract class HDF4ImageReaderSpi extends BaseImageReaderSpi {

	static{
		NetcdfDataset.setDefaultEnhanceMode(EnumSet.of(Enhance.CoordSystems));
	}

    private static final Logger LOGGER = Logger.getLogger(HDF4ImageReaderSpi.class.toString());

    static final String[] suffixes = { "hdf", "hdf4" };

    static final String[] formatNames = { "HDF4" };

    static final String[] MIMETypes = { "image/hdf4" };

    static final String version = "1.0";

    static final String readerCN = "it.geosolutions.imageio.plugins.hdf4.BaseHDF4ImageReader";

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

    public HDF4ImageReaderSpi() {
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
            LOGGER.fine("HDF4TeraScanImageReaderSpi Constructor");
    }

    public HDF4ImageReaderSpi(final String readerCN) {
    	super(vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN, 
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
        // check if this is an 
        if (input instanceof File) {
        	// open up as a netcdf dataset
            final NetcdfDataset dataset = NetCDFUtilities.getDataset(input);            
            if (dataset != null) {
            	
            	// first of all is it an HDF4??
            	// TODO change this when we will be allowed to use >= 4.0.46
            	if(!(dataset.getIosp() instanceof H4iosp))
            		return false;
            	
            	// now, check if we can read it
            	try{
	            	found = isValidDataset(dataset);
            	}
            	finally {
            		try{
            			dataset.close();
            		}
            		catch (Throwable e) {
						if(LOGGER.isLoggable(Level.FINE))
							LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
					}
            	}
        	}
        }
        return found;
    }

    protected boolean isValidDataset(final NetcdfDataset dataset) {
		return false;
	}

	public ImageReader createReaderInstance(Object input) throws IOException {
        return new HDF4ImageReaderProxy(this);
    }

    public String getDescription(Locale locale) {
        return "HDF4 Image Reader, version " + version;
    }
}
