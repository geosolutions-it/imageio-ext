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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link JP2GDALKakaduImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from JP2K files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JP2GDALKakaduImageReader extends GDALImageReader {

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jp2kakadu");

	/**
	 * Constructs a
	 * <code>JP2GDALKakaduImageReader<code> using a 
	 * {@link JP2GDALKakaduImageReaderSpi}.
	 * 
	 * @param originatingProvider
	 *            The {@link JP2GDALKakaduImageReaderSpi} to use for building 
	 *            this <code>JP2GDALKakaduImageReader<code>.
	 */
	public JP2GDALKakaduImageReader(
			JP2GDALKakaduImageReaderSpi originatingProvider) {
		super(originatingProvider, 0);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("JP2GDALKakaduImageReader Constructor");
	}
}
