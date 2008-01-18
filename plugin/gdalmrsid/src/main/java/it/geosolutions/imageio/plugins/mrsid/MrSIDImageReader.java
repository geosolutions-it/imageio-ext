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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link MrSIDImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from MrSID files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class MrSIDImageReader extends GDALImageReader {

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.mrsid");

	public MrSIDImageReader(MrSIDImageReaderSpi originatingProvider) {
		super(originatingProvider, 0);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("MrSIDImageReader Constructor");
	}
	
	/**
	 * Build a proper {@link GDALCommonIIOImageMetadata} given the name of a
	 * dataset
	 * 
	 * @param datasetName
	 *            the name of the dataset
	 */
	protected GDALCommonIIOImageMetadata createDatasetMetadata(
			String datasetName) {
		return new MrSIDIIOImageMetadata(datasetName);
	}

}
