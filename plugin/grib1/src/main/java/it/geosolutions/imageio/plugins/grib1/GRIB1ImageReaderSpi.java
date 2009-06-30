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
package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.ndplugin.BaseImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;

import javax.imageio.ImageReader;
import javax.imageio.spi.ServiceRegistry;

import net.sourceforge.jgrib.GribFile;

/**
 * Service provider interface for the GRIB1 Image
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class GRIB1ImageReaderSpi extends BaseImageReaderSpi {
    static final String[] suffixes = { "grib", "grb" };

    static final String[] formatNames = { "GRIB1" };

    static final String[] MIMETypes = { "image/grib", "image/grb" };

    static final String version = "1.0";

    static final String readerCN = "it.geosolutions.imageio.plugins.grib1.GRIB1ImageReader";

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

    public GRIB1ImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN, // readerClassName
                FLAT_STANDARD_INPUT_TYPES,
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

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("GRIB1ImageReaderSpi Constructor");
        }
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (input instanceof URI) {
            input = ((URI) input).toURL();
        }

        if (input instanceof File) {
            return GribFile.canDecodeInput((File) input);
        } else if (input instanceof String) {
            File file = new File((String) input);
            return GribFile.canDecodeInput(file);
        } else if (input instanceof URL) {
            return GribFile.canDecodeInput((URL) input);
        } else if (input instanceof FileImageInputStreamExt) {
            return GribFile.canDecodeInput(((FileImageInputStreamExt) input)
                    .getFile());

        }
        return false;
    }

    public ImageReader createReaderInstance(Object input) throws IOException {
        return new GRIB1ImageReader(this);
    }

    public String getDescription(Locale locale) {
        return "GRIB1 Image Reader, version " + version;
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        try {
            Class.forName("net.sourceforge.jgrib.GribCollection");
        } catch (ClassNotFoundException e) {
            registry.deregisterServiceProvider(this);
        }
        super.onRegistration(registry, category);
    }
}
