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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;

import org.gdal.gdal.Dataset;

/**
 * {@link HDF4ImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from ECW files.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class HDF4ImageReader extends GDALImageReader {

	public class HDF4DataSetWrapper extends GDALDatasetWrapper {

		// TODO: Check for subdatasettings
		public HDF4DataSetWrapper(String sDatasetName) {
			super(sDatasetName);
		}

		public HDF4DataSetWrapper(Dataset ds, String name) {
			super(ds, name, false);
		}
	}

	private static final Logger logger = Logger.getLogger(HDF4ImageReader.class
			.toString());

	public HDF4ImageReader(HDF4ImageReaderSpi originatingProvider) {
		super(originatingProvider);		
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		return getDataSetWrapper(imageIndex).getImageIOMetadata();
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		logger.info("This method actually returns. Use getGDALStreamMetadata.");
		return null;
	}

	protected GDALDatasetWrapper createDataSetWrapper(Dataset mainDataset,
			String mainDatasetFileName) {
		return new HDF4DataSetWrapper(mainDataset, mainDatasetFileName);
	}

	protected GDALDatasetWrapper createDataSetWrapper(String string) {
		return new HDF4DataSetWrapper(string);
	}
	
	protected IIOMetadata getIIOImageMetadata(GDALDatasetWrapper wrapper) {
		//TODO: in the future, we could define a specific HDF4 metadata format
		return new GDALCommonIIOImageMetadata(wrapper);
	}

}
