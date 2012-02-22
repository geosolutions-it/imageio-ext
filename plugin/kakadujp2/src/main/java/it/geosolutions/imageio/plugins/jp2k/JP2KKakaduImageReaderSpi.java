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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.utilities.Utilities;
import it.geosolutions.util.KakaduUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ServiceRegistry;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jpx_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_simple_file_source;

/**
 * Service provider interface for the JPEG2000SimpleBox
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 * 
 */
public class JP2KKakaduImageReaderSpi extends ImageReaderSpi {

	static {
        KakaduUtilities.loadKakadu();
    }
    protected boolean registered = false;
    
    static final String[] suffixes = { "jp2", "jp2k", "j2k", "j2c" };
     
    static final String[] formatNames = { "JP2KSimpleBox", "jpeg2000", "jpeg 2000", "JPEG 2000", "JPEG2000" };
     
    static final String[] MIMETypes = { "image/jp2", "image/jp2k", "image/j2k", "image/j2c" };

    static final String version = "1.0";

    static final String readerCN = "it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReader";

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

    public JP2KKakaduImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN, // readerClassName
                new Class[] { File.class, FileImageInputStreamExt.class, 
                		URL.class },
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

    /**
     * This method checks if the provided input can be decoded from this SPI
     */
    public boolean canDecodeInput(Object input) throws IOException {
        boolean isDecodable = true;
        File source = null;

        // Retrieving the File source
        if (input instanceof File) {
            source = (File) input;
        } else if (input instanceof FileImageInputStreamExt) {
            source = ((FileImageInputStreamExt) input).getFile();
        } else if (input instanceof URL) {
            final URL tempURL = (URL) input;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                source = Utilities.urlToFile(tempURL);
            }
        } else
            return false;

        Jp2_family_src familySource = new Jp2_family_src();
        Jpx_source wrappedSource = new Jpx_source();
        Kdu_simple_file_source rawSource = null;
        try {
            String fileName = source.getAbsolutePath();
            familySource.Open(fileName);
            int success = wrappedSource.Open(familySource, true);
            // success is 0 if there isn't sufficient information from the
            // source object to complete the opening operation.
            // success is -1 if the source is not compatible with the JPX or JP2
            // specifications
            if (success < 0) { // Must open as a raw file
                familySource.Close();
                wrappedSource.Close();
                rawSource = new Kdu_simple_file_source(fileName);
                if (rawSource != null){
                    if (fileName != null){
                        FileInputStream fis = new FileInputStream (new File(fileName));
                        byte[] jp2SocMarker = new byte[2];
                        fis.read(jp2SocMarker);
                        if (jp2SocMarker[0] == (byte)0xFF && jp2SocMarker[1] == (byte)0x4F)
                            isDecodable = true;
                        else
                        	isDecodable = false;
                        fis.close();
                    }
                }
                else
                    isDecodable = false;
            }

        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during creation of key objects! ",
                    e);
        }

        // Dispose
        wrappedSource.Native_destroy();
        familySource.Native_destroy();
        if (rawSource != null)
            rawSource.Native_destroy();
        return isDecodable;
    }

    /**
     * Returns an instance of the {@link JP2KKakaduImageReader}
     * 
     * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
     */
    public ImageReader createReaderInstance(Object source) throws IOException {
        return new JP2KKakaduImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return new StringBuffer("JP2K Image Reader, version ").append(version)
                .toString();
    }

    /**
     * Upon registration, this method ensures that this SPI is listed at the top
     * of the ImageReaderSpi items, so that it will be invoked before the
     * default ImageReaderSpi
     * 
     * @param registry
     *                ServiceRegistry where this object has been registered.
     * @param category
     *                a Class object indicating the registry category under
     *                which this object has been registered.
     */
    @SuppressWarnings("unchecked")
	public synchronized void onRegistration(ServiceRegistry registry, Class category) {
        super.onRegistration(registry, category);
        if (registered) {
            return;
        }

        registered = true;
        if (!KakaduUtilities.isKakaduAvailable()) {
            final IIORegistry iioRegistry = (IIORegistry) registry;
            final Class<ImageReaderSpi> spiClass = ImageReaderSpi.class;
            final Iterator<ImageReaderSpi> iter = iioRegistry.getServiceProviders(spiClass,true);
            while (iter.hasNext()) {
                final ImageReaderSpi provider = (ImageReaderSpi) iter.next();
                if (provider instanceof JP2KKakaduImageReaderSpi) {
                    registry.deregisterServiceProvider(provider);
                }
            }
            return;
        }
        
        final List<ImageReaderWriterSpi> readers = KakaduUtilities.getJDKImageReaderWriterSPI(registry,"jpeg2000", true);
        for (ImageReaderWriterSpi elem:readers) {
        	if (elem instanceof ImageReaderSpi){
	            final ImageReaderSpi spi = (ImageReaderSpi) elem;;
	            if (spi == this)
	                continue;
	            registry.deregisterServiceProvider(spi);
	            registry.setOrdering(category, this, spi);
        	}

        }
    }
}
