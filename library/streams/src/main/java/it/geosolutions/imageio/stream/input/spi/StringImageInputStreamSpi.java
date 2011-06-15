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
package it.geosolutions.imageio.stream.input.spi;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * Implementation of an {@link ImageInputStreamSpi} for instantiating an
 * {@link ImageInputStream} capable of connecting to a {@link String}.
 * 
 * <p>
 * I basically rely on the existence of something to read from a {@link File} in
 * case this {@link URL} points to a {@link File}, otherwise I try to open up
 * an {@link InputStream} and I ask the
 * {@link ImageIO#createImageInputStream(Object)} to create an
 * {@link ImageInputStream} for it.
 * 
 * 
 * @see ImageInputStream
 * @see ImageInputStreamSpi
 * @see ImageIO#createImageInputStream(Object)
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class StringImageInputStreamSpi extends ImageInputStreamSpi {
    /* Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.stream.input");

    private static final String vendorName = "GeoSolutions";

    private static final String version = "1.0";

    private static final Class<String> inputClass = String.class;

    /**
     * Default constructor for a {@link StringImageInputStreamSpi};
     */
    public StringImageInputStreamSpi() {
        super(vendorName, version, inputClass);
    }

    /**
     * @see javax.imageio.spi.ImageInputStreamSpi#createInputStreamInstance(java.lang.Object,
     *      boolean, java.io.File)
     */
    public ImageInputStream createInputStreamInstance(Object input,
            boolean useCache, File cacheDir) throws IOException {
        // is it a String?
        if (!(input instanceof String)) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("The provided input is not a valid String.");
            return null;
        }

        final String sourceString = ((String) input);
        
        //
        // as a URL
        //
        try{
        	// the needed checks are done inside the constructor
        	final URL tempURL = new URL(sourceString);
        	return new URLImageInputStreamSpi().createInputStreamInstance(tempURL,ImageIO.getUseCache(),ImageIO.getCacheDirectory());
        }catch (Throwable e) {
			if(LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
		}
       

        //
        // as a FILE
        //
        final File tempFile = new File(sourceString);
        try{
        	// the needed checks are done inside the constructor
        	return new FileImageInputStreamExtImpl(tempFile);
        }catch (Throwable e) {
			throw new IllegalArgumentException(e);
		}
    }

    /**
     * @see ImageInputStreamSpi#getDescription(Locale).
     */
    public String getDescription(Locale locale) {
        return "Service provider that helps connecting to the object pointed by a String";
    }

}
