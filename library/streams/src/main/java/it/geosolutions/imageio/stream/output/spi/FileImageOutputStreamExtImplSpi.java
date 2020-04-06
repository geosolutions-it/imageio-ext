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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;

/**
 * A special ImageOutputStream Service Provider Interface which is able to
 * provide a {@link FileImageOutputStreamExt}
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileImageOutputStreamExtImplSpi extends ImageOutputStreamSpi {

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.stream.output.spi");

    private static final String vendorName = "GeoSolutions";

    private static final String version = "1.0";

    private static final Class<?> outputClass = File.class;

    public FileImageOutputStreamExtImplSpi() {
        super(vendorName, version, outputClass);
    }

    public String getDescription(Locale locale) {
        return "Service provider that wraps a FileImageOutputStream";
    }

    /**
     * Upon registration, this method ensures that this SPI will be invoked
     * before the default FileImageOutputStreamSpi
     * 
     * @param registry
     *                ServiceRegistry where this object has been registered.
     * @param category
     *                a Class object indicating the registry category under
     *                which this object has been registered.
     */
    @SuppressWarnings("unchecked")
	public void onRegistration(ServiceRegistry registry, Class category) {
        super.onRegistration(registry, category);
        Object other;
        final Class targetClass = ImageOutputStreamSpi.class;
        for (Iterator i = registry.getServiceProviders(targetClass, true); i.hasNext();) {
            other = i.next();

            // using class name to avoid warnings in JDK 11
            if (other != null && other.getClass().getName().equals("com.sun.imageio.spi.FileImageOutputStreamSpi"))
                registry.deregisterServiceProvider(other);
            if (this != other) {
                registry.setOrdering(targetClass, this, other);
            }
        }
    }

    /**
     * Returns an instance of the ImageOutputStream implementation associated
     * with this service provider.
     * 
     * @return an ImageOutputStream instance.
     * 
     * @throws IllegalArgumentException
     *                 if input is not an instance of the correct class or is
     *                 null.
     */
    public ImageOutputStream createOutputStreamInstance(Object output,
            boolean useCache, File cacheDir) {

        try {
            return new FileImageOutputStreamExtImpl((File) output);
        } catch (FileNotFoundException e) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            return null;
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
