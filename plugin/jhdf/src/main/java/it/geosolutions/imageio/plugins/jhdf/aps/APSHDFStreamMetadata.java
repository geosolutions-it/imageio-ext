package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFStreamMetadata;

import javax.imageio.metadata.IIOInvalidTreeException;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.HObject;

import org.w3c.dom.Node;

public class APSHDFStreamMetadata extends BaseHDFStreamMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFStreamMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	public Node getAsTree(String formatName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
		// TODO Auto-generated method stub
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		}
	
	public APSHDFStreamMetadata(HObject root) {
		this();
		initializeFromRoot(root);
	}

	private void initializeFromRoot(HObject root) {

		
	}

	public APSHDFStreamMetadata() {
		super(
				false,
				nativeMetadataFormatName,
				"it.geosolutions.imageio.plugins.jhdf.aps.APSHDFStreamMetadataFormat",
				null, null);
	}
}
