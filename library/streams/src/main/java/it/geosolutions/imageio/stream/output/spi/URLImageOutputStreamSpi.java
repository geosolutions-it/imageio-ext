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
package it.geosolutions.imageio.stream.output.spi;

import it.geosolutions.imageio.stream.output.FileImageOutputStreamExtImpl;
import it.geosolutions.imageio.utilities.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 * A Special {@link ImageOutputStreamSpi} Service Provider Interface which is
 * able to provide provide an {@link ImageOutputStream} object for writing to a
 * {@link URL} in case such a URL is writable of course.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */

public class URLImageOutputStreamSpi extends ImageOutputStreamSpi {

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.stream.output.spi");

    private static final String vendorName = "GeoSolutions";

    private static final String version = "1.0";

    private static final Class outputClass = URL.class;

    public URLImageOutputStreamSpi() {
        super(vendorName, version, outputClass);
    }

    public String getDescription(Locale locale) {
        return "Service provider for writing to a URL";
    }

    /**
     * Returns an instance of the {@link ImageOutputStream} implementation
     * associated with this service provider.
     * 
     * @return an ImageOutputStream instance.
     * 
     * @throws IllegalArgumentException
     *                 if input is not an instance of the correct class or is
     *                 null.
     */
    public ImageOutputStream createOutputStreamInstance(Object output,
            boolean useCache, File cacheDir) {

        // is it a URL?
        if (!(output instanceof URL))
            return null;

        // URL that point to a file
        final URL outputURL = ((URL) output);
        if (outputURL.getProtocol().compareToIgnoreCase("file") == 0) {
            File tempFile;
            try {
                tempFile = Utilities.urlToFile(outputURL);
                return new FileImageOutputStreamExtImpl(tempFile);
            } catch (UnsupportedEncodingException e) {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            } catch (FileNotFoundException e) {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            }

        }
        return null;

    }
}
