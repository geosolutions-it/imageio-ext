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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;

import org.gdal.gdal.Dataset;

/**
 * {@link JP2GDALKakaduImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from JP2K files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class JP2GDALKakaduImageReader extends GDALImageReader {
	
	public class JP2GDALKakaduDataSetWrapper extends GDALDatasetWrapper {

		public JP2GDALKakaduDataSetWrapper(String sDatasetName) {
			super(sDatasetName);
		}

		public JP2GDALKakaduDataSetWrapper(Dataset ds, String name) {
			super(ds, name);
		}
	}
	
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jp2kakadu");

	public JP2GDALKakaduImageReader(JP2GDALKakaduImageReaderSpi originatingProvider) {
		super(originatingProvider);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("JP2GDALKakaduImageReader Constructor");
		nSubdatasets = 0;
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		return getDataSetWrapper(imageIndex).getImageIOMetadata();
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		if (LOGGER.isLoggable(Level.INFO))
			LOGGER
					.info("This method actually returns null. Use getGDALImageMetadata.");
		throw new UnsupportedOperationException(
				"This method actually returns null. Use getGDALStreamMetadata.");
	}

	protected GDALDatasetWrapper createDataSetWrapper(Dataset mainDataset,
			String mainDatasetFileName) {
		return new JP2GDALKakaduDataSetWrapper(mainDataset, mainDatasetFileName);
	}

	protected GDALDatasetWrapper createDataSetWrapper(String string) {
		return new JP2GDALKakaduDataSetWrapper(string);
	}

	protected IIOMetadata getIIOImageMetadata(GDALDatasetWrapper arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
