package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageMetadata;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

public class TOVSImageMetadata extends BaseHDFImageMetadata {
		public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSImageMetadata_1.0";

		public static final String[] metadataFormatNames = { nativeMetadataFormatName };

		protected final int[] mapMutex = new int[1];
		
		/**
		 * private fields for metadata node building
		 */

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

		public TOVSImageMetadata(){
			super(
					false,
					nativeMetadataFormatName,
					null,
					null, null);
		}
		
		public TOVSImageMetadata(SubDatasetInfo sdInfo) {
			this();
			initializeFromDataset(sdInfo);
		}

		/**
		 * Initialize Metadata from a raster
		 * 
		 * @param raster
		 *            the <code>SwanRaster</code> from which retrieve data
		 * @param imageIndex
		 *            the imageIndex relying the required subdataset
		 */
		private void initializeFromDataset(final SubDatasetInfo sdInfo) {
			//Initializing common properties to each HDF dataset.
			initializeCommonDatasetProperties(sdInfo);
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
