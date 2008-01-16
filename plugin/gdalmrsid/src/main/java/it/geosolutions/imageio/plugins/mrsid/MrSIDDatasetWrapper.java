package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALDatasetWrapper;

import javax.imageio.metadata.IIOMetadata;

import org.gdal.gdal.Dataset;

public class MrSIDDatasetWrapper extends GDALDatasetWrapper {

	public MrSIDDatasetWrapper(String sDatasetName) {
		super(sDatasetName);
	}

	public MrSIDDatasetWrapper(Dataset ds, String name) {
		super(ds, name);
	}

	/**
	 * Provide a proper <code>IIOMetadata</code> object for the specified
	 * {@link GDALDatasetWrapper}.
	 * 
	 * The default implementation is overridden to return a specific
	 * {@link MrSIDIIOImageMetadata} instance.
	 */
	protected IIOMetadata getIIOImageMetadata() {
		return new MrSIDIIOImageMetadata(this);
	}
}
