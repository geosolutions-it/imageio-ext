/*
* JImageIO-extension - OpenSource Java Image translation Library
* http://www.geo-solutions.it/
* (C) 2007, GeoSolutions
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation;
* version 2.1 of the License.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/
package it.geosolutions.imageio.plugins.jpeg;

import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;

/**
 * {@link JpegGDALImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from JPEG files.
 * 
 * @author Simone Giannecchini
 * @author Daniele Romagnoli
 * 
 */
public class JpegGDALImageReader extends GDALImageReader {

	private static final Logger LOGGER = Logger
			.getLogger("javax.imageio.plugins");

	public JpegGDALImageReader(JpegGDALImageReaderSpi originatingProvider) {
		super(originatingProvider);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("JpegGDALImageReader Constructor");
		nSubdatasets = 0;
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		if (LOGGER.isLoggable(Level.INFO))
			LOGGER
					.info("Unsupported operation, please use getGDALImageMetadata(int imageIndex)  instead.");
		throw new UnsupportedOperationException(
				"Unsupported operation, please use getGDALImageMetadata(int imageIndex)  instead.");
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		if (LOGGER.isLoggable(Level.INFO))
			LOGGER
					.info("This method actually returns null. Use getGDALImageMetadata.");
		throw new UnsupportedOperationException(
				"This method actually returns null. Use getGDALStreamMetadata.");
	}

	protected boolean isSupportingSubdatasets() {
		return false;
	}

	protected boolean needsTilesTuning() {
		return true;
	}
}
