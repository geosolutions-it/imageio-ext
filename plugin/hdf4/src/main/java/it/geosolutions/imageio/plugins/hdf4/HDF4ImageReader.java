package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;

import org.gdal.gdal.Dataset;

public class HDF4ImageReader extends GDALImageReader {

	private static final Logger logger = Logger.getLogger(HDF4ImageReader.class
			.toString());

	public HDF4ImageReader(HDF4ImageReaderSpi originatingProvider) {
		super(originatingProvider);
//		if (LoggerController.enableLoggerReader)
//			logger.info("HDF4ImageReader Constructor");
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		logger.info("This method actually returns null. Use getGDALImageMetadata.");
		return null;
	}


	public IIOMetadata getStreamMetadata() throws IOException {
		logger.info("This method actually returns. Use getGDALStreamMetadata.");
		return null;
	}

	protected GDALDatasetWrapper createDataSetWrapper(String string) {
		// TODO implement me
		return null;
	}

	protected GDALDatasetWrapper createDataSetWrapper(Dataset mainDataset, String mainDatasetFileName) {
		// TODO implement me
		return null;
	}

	protected IIOMetadata getIIOImageMetadata(GDALDatasetWrapper wrapper) {
		// TODO implement me
		return null;
	}
	

}
