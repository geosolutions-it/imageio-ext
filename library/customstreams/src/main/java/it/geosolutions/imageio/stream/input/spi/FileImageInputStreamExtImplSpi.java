/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
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

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

import com.sun.imageio.spi.FileImageInputStreamSpi;
import com.sun.media.imageio.stream.FileChannelImageInputStream;

/**
 * Implementation of an {@link ImageInputStreamSpi} for instantiating an
 * {@link ImageInputStream} capable of connecting to a {@link File} by means of
 * an {@link EnhancedRandomAccessFile} which gives buffering capabilities.
 * 
 * <p>
 * It is worth to point out that {@link ImageIO} already provide the
 * {@link FileChannelImageInputStream} in order to efficiently access images
 * with buffering. despite to this I have ran into many problems with
 * {@link FileChannel}s especially on Windows machines, hence I came up with
 * this {@link ImageInputStream} subclass and this {@link ImageInputStreamSpi}
 * which gives similar performances for most uses but far less problems.
 * 
 * <p>
 * A special thank to Alex McManus and Russ Rewfor the
 * {@link EnhancedRandomAccessFile} class.
 * 
 * @see ImageInputStream
 * @see ImageInputStreamSpi
 * @see ImageIO#createImageInputStream(Object)
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileImageInputStreamExtImplSpi extends ImageInputStreamSpi {

	/** Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.stream.input");

	private static final String vendorName = "GeoSolutions";

	private static final String version = "1.0-RC1";

	private static final Class inputClass = File.class;

	/**
	 * Constructs a blank {@link ImageInputStreamSpi}. It is up to the subclass
	 * to initialize instance variables and/or override method implementations
	 * in order to provide working versions of all methods.
	 * 
	 */
	public FileImageInputStreamExtImplSpi() {
		super(vendorName, version, inputClass);
	}

	/**
	 * @see ImageInputStreamSpi#getDescription(Locale).
	 */
	public String getDescription(Locale locale) {
		return "Service provider that wraps a FileImageInputStream";
	}

	/**
	 * Upon registration, this method ensures that this SPI is listed at the top
	 * of the ImageInputStreamSpi items, so that it will be invoked before the
	 * default FileImageInputStreamSpi
	 * 
	 * @param registry
	 *            ServiceRegistry where this object has been registered.
	 * @param category
	 *            a Class object indicating the registry category under which
	 *            this object has been registered.
	 */
	public void onRegistration(ServiceRegistry registry, Class category) {
		super.onRegistration(registry, category);
		Object other;
		Class targetClass = ImageInputStreamSpi.class;
		for (Iterator i = registry.getServiceProviders(targetClass, true); i
				.hasNext();) {
			other = i.next();

			if (other instanceof FileImageInputStreamSpi)
				registry.deregisterServiceProvider(other);
			if (this != other)
				registry.setOrdering(targetClass, this, other);

		}
	}

	/**
	 * Returns an instance of the ImageInputStream implementation associated
	 * with this service provider.
	 * 
	 * @param input
	 *            an object of the class type returned by getInputClass.
	 * @param useCache
	 *            a boolean indicating whether a cache eraf should be used, in
	 *            cases where it is optional.
	 * 
	 * @param cacheDir
	 *            a File indicating where the cache eraf should be created, or
	 *            null to use the system directory.
	 * 
	 * 
	 * @return an ImageInputStream instance.
	 * 
	 * @throws IllegalArgumentException
	 *             if input is not an instance of the correct class or is null.
	 */
	public ImageInputStream createInputStreamInstance(Object input,
			boolean useCache, File cacheDir) {
		if (!(input instanceof File)) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("THe provided input is not a eraf.");
			return null;
		}

		try {
			return new FileImageInputStreamExtImpl((File) input);
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
