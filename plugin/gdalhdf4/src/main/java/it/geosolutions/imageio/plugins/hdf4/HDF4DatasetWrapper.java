package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.GDALDatasetWrapper;

import org.gdal.gdal.Dataset;

public class HDF4DatasetWrapper extends GDALDatasetWrapper {

	// TODO: Check for subdatasettings
	public HDF4DatasetWrapper(String sDatasetName) {
		super(sDatasetName);
	}

	public HDF4DatasetWrapper(Dataset ds, String name) {
		super(ds, name, false);
	}
}