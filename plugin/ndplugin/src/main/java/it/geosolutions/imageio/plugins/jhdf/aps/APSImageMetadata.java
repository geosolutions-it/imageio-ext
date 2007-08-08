package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageMetadata;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import ncsa.hdf.object.Dataset;

import org.w3c.dom.Node;

public class APSImageMetadata extends BaseHDFImageMetadata{
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSImageMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	protected final int[] mapMutex = new int[1];
	
	/**
	 * private fields for metadata node building
	 */

//	protected Map attributesMap = Collections.synchronizedMap(new HashMap(19));

	public Node getAsTree(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return getNativeTree();
		} else {
			throw new IllegalArgumentException("Not a recognized format!");
		}
	}

	private Node getNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);
		/**
		 * Setting Dataset Properties common to any sub-format
		 */
		
		root.appendChild(getCommonDatasetNode());
		return root;
	}
	
	public APSImageMetadata(){
		super(
				false,
				nativeMetadataFormatName,
				null,
				null, null);
	}

	public APSImageMetadata(Dataset dataset) {
		this();
		initializeFromDataset(dataset);
	}

	public APSImageMetadata(SubDatasetInfo sdInfo) {
		this();
		initializeFromDataset(sdInfo);
	}

	private void initializeFromDataset(SubDatasetInfo sdInfo) {
		initializeCommonDatasetProperties(sdInfo);
		//TODO: Add further initializations APS specific?
	}

	/**
	 * Initialize Metadata from a raster
	 * 
	 * @param raster
	 *            the <code>SwanRaster</code> from which retrieve data
	 * @param imageIndex
	 *            the imageIndex relying the required subdataset
	 */
	private void initializeFromDataset(final Dataset dataset) {
		//Initializing common properties to each HDF dataset.
		initializeCommonDatasetProperties(dataset);
		//TODO: Add further initializations APS specific?
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		// TODO Auto-generated method stub

	}

	public void reset() {
		// TODO Auto-generated method stub

	}

}
