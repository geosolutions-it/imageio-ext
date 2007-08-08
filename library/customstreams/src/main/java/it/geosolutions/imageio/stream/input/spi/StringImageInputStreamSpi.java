/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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

import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
 * {@link ImageInputStream} capable of connecting to a {@link Strin}.
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
 * @author Simone Giannecchini(Simboss)
 */
public class StringImageInputStreamSpi extends ImageInputStreamSpi {
	/* Logger. */
	private final static Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.stream.input");

	private static final String vendorName = "GeoSolutions";

	private static final String version = "1.0-RC1";

	private static final Class inputClass = String.class;

	/**
	 * Default constructor for a {@link StringImageInputStreamSpi};
	 */
	public StringImageInputStreamSpi() {
		super(vendorName, version, inputClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.imageio.spi.ImageInputStreamSpi#createInputStreamInstance(java.lang.Object,
	 *      boolean, java.io.File)
	 */
	public ImageInputStream createInputStreamInstance(Object input,
			boolean useCache, File cacheDir) throws IOException {
		// is it a URL?
		if (!(input instanceof String))
		{
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("The provided input is not a valid String.");
			return null;
		}

		final String sourceString = ((String) input);
		// /////////////////////////////////////////////////////////////////////
		//
		// URL
		//
		// /////////////////////////////////////////////////////////////////////
		final URL tempURL = new URL(sourceString);
		if (tempURL.getProtocol().compareToIgnoreCase("file") == 0) {
			final File tempFile = new File(URLDecoder.decode(tempURL.getFile(),
					"UTF-8"));
			if (!tempFile.exists())
			{
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("The provided input file does not exist.");
				return null;
			}

			return new FileImageInputStreamExtImpl(tempFile);

		} else {
			// is it a valid InputStream
			InputStream inStream;
			try {
				inStream = tempURL.openStream();

				if (useCache)
					return new MemoryCacheImageInputStream(inStream);
				else
					return new FileCacheImageInputStream(inStream, cacheDir);
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
				
			}

		}
		// /////////////////////////////////////////////////////////////////////
		//
		// FILE
		//
		// /////////////////////////////////////////////////////////////////////
		final File tempFile = new File(sourceString);
		if (!tempFile.exists())
		{
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("The provided input file does not exist.");
			return null;
		}
		else
			return new FileImageInputStreamExtImpl(tempFile);


	}

	/**
	 * @see ImageInputStreamSpi#getDescription(Locale).
	 */
	public String getDescription(Locale locale) {
		return "Service provider that helps connecting to the object pointed by a String";
	}

}
