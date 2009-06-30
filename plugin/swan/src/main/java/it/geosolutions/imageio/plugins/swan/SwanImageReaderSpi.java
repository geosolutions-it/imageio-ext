/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.swan;

import it.geosolutions.imageio.plugins.swan.raster.SwanRaster;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.apache.xmlbeans.XmlException;

/**
 * Class which provides a specialized Service Provider Interface which
 * instantiates a {@link SwanImageReader} if it is able to decode the input provided.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class SwanImageReaderSpi extends ImageReaderSpi {
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.swan");

	// TODO: select the proper extension
	static final String[] suffixes = { "swo" };

	static final String[] formatNames = { "SWAN", "SWAN output files" };

	static final String[] MIMETypes = { "image/swo" };

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.swan.SwanImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = null;

	/** { "it.geosolutions.imageio.plugins.swan.SwanImageWriterSpi" } */

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = "it.geosolutions.imageio.plugins.swan.SwanStreamMetadata_1.0";

	static final String nativeStreamMetadataFormatClassName = "it.geosolutions.imageio.plugins.swan.SwanStreamMetadataFormat";

	static final String[] extraStreamMetadataFormatNames = null;

	static final String[] extraStreamMetadataFormatClassNames = null;

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = true;

	static final String nativeImageMetadataFormatName = "it.geosolutions.imageio.plugins.swan.SwanImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = "it.geosolutions.imageio.plugins.swan.SwanImageMetadataFormat";;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public SwanImageReaderSpi() {
		super(
				vendorName,
				version,
				formatNames,
				suffixes,
				MIMETypes,
				readerCN, // readerClassName
				STANDARD_INPUT_TYPE,
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
	 * This method check if the input source can be decoded by the reader
	 * provided by this specific subclass of ImageReaderSpi. Return true if the
	 * check was successfully passed. input source type accepted and handled are
	 * String, File, Url, InputStream and ImageInputStream.
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#canDecodeInput(java.lang.Object)
	 */
	public boolean canDecodeInput(Object input) throws IOException {

		if (input instanceof ImageInputStream)
			((ImageInputStream) input).mark();

		/**
		 * Checking input source types and creating an ImageInputStream
		 */

		// temp vars
//		ImageInputStream spiImageInputStream;
//		boolean closeMe = false;// if the stream is opened here we need to close
		// it before leaving

		// if input source is a string,
		// convert input from String to File
		if (input instanceof String) {
			input = new File((String) input);
//			closeMe = true;
		}

		// if input source is an URL, open an InputStream
		if (input instanceof URL) {
			final URL tempURL = (URL) input;
			if (tempURL.getProtocol().equalsIgnoreCase("file"))
				input = new File(URLDecoder.decode(tempURL.getFile(), "UTF8"));
			else
				input = ((URL) input).openStream();
//			closeMe = true;
		}

		// if input source is a File,
		// convert input from File to FileInputStream
		if (input instanceof File) {
			input = new FileImageInputStreamExtImpl((File) input);
//			closeMe = true;
		}

		if (input instanceof ImageInputStream) {
			((ImageInputStream) input).mark();
//			spiImageInputStream = (ImageInputStream) input;
		} else {
			return false;
		}

		// Checking if this specific SPI can decode the provided input
		try {
			FileCoherencyCheck((FileImageInputStreamExtImpl) input);
		} catch (XmlException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			((ImageInputStream) input).reset();
//			if (closeMe)
//				spiImageInputStream.close();
			return false;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			((ImageInputStream) input).reset();
//			if (closeMe)
//				spiImageInputStream.close();
			return false;
		}
		if (input instanceof ImageInputStream)
			((ImageInputStream) input).reset();
//		if (closeMe)
//			spiImageInputStream.close();
		return true;
	}

	/**
	 * 
	 * @param swanFile
	 *            the file containing data generated by SWAN
	 * @throws IOException
	 * @throws XmlException
	 * 
	 * 
	 */
	public void FileCoherencyCheck(ImageInputStream input) throws IOException,
			XmlException {

		SwanRaster swanRaster = new SwanRaster(input);
		swanRaster.parseHeader();
	}

	/**
	 * Returns an instance of the SwanImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new SwanImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return "SWAN Image Reader, version " + version;
	}

	public void dispose() {

	}
}
