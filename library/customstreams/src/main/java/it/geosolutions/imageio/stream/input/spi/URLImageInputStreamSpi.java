/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.stream.input.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * Implementation of an {@link ImageInputStreamSpi} for instantiating an
 * {@link ImageInputStream} capable of connecting to a {@link URL}.
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
public class URLImageInputStreamSpi extends ImageInputStreamSpi {
    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.stream.input");

    private static final FileImageInputStreamExtImplSpi fileStreamSPI = new FileImageInputStreamExtImplSpi();

    /**
     * Takes a URL and converts it to a File. The attempts to deal with Windows
     * UNC format specific problems, specifically files located on network
     * shares and different drives.
     * 
     * If the URL.getAuthority() returns null or is empty, then only the url's
     * path property is used to construct the file. Otherwise, the authority is
     * prefixed before the path.
     * 
     * It is assumed that url.getProtocol returns "file".
     * 
     * Authority is the drive or network share the file is located on. Such as
     * "C:", "E:", "\\fooServer"
     * 
     * @param url
     *                a URL object that uses protocol "file"
     * @return a File that corresponds to the URL's location
     */
    public static File urlToFile(URL url) {
        String string = url.toExternalForm();

        try {
            string = URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        String path3;
        String simplePrefix = "file:/";
        String standardPrefix = simplePrefix + "/";

        if (string.startsWith(standardPrefix)) {
            path3 = string.substring(standardPrefix.length());
        } else if (string.startsWith(simplePrefix)) {
            path3 = string.substring(simplePrefix.length() - 1);
        } else {
            String auth = url.getAuthority();
            String path2 = url.getPath().replace("%20", " ");
            if (auth != null && !auth.equals("")) {
                path3 = "//" + auth + path2;
            } else {
                path3 = path2;
            }
        }

        return new File(path3);
    }

    private static final String vendorName = "GeoSolutions";

    private static final String version = "1.0";

    private static final Class inputClass = URL.class;

    /**
     * Default constructor for a {@link URLImageInputStreamSpi};
     */
    public URLImageInputStreamSpi() {
        super(vendorName, version, inputClass);
    }

    /**
     * 
     * @see javax.imageio.spi.ImageInputStreamSpi#createInputStreamInstance(java.lang.Object,
     *      boolean, java.io.File)
     */
    public ImageInputStream createInputStreamInstance(Object input,
            boolean useCache, File cacheDir) {
        // is it a URL?
        if (!(input instanceof URL)) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("The provided input is not a valid URL.");
            return null;
        }

        try {
            // URL that points to a file?
            final URL sourceURL = ((URL) input);
            final File tempFile = urlToFile(sourceURL);
            if (tempFile.exists() && tempFile.isFile() && tempFile.canRead())
                return fileStreamSPI.createInputStreamInstance(tempFile,
                        useCache, cacheDir);

            // URL that does NOT points to a file, let's open up a stream
            if (useCache)
                return new MemoryCacheImageInputStream(sourceURL.openStream());
            else
                return new FileCacheImageInputStream(sourceURL.openStream(),
                        cacheDir);

        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * @see ImageInputStreamSpi#getDescription(Locale).
     */
    public String getDescription(Locale locale) {
        return "Service provider that helps connecting to the onject pointed by a URL";
    }
}
