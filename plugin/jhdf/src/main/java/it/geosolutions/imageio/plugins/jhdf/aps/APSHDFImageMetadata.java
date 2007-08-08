package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;

import org.w3c.dom.Node;

public class APSHDFImageMetadata extends BaseHDFImageMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	/**
	 * private fields for metadata node building
	 */

	protected Map attributesMap = Collections.synchronizedMap(new HashMap(19));

	//	
	// private String createSoftware = "";
	//
	// private String createTime = "";
	//
	// private String createPlatform = "";
	//
	// private String productName = "";
	//
	// private String productAlgorithm = "";
	//
	// private String productUnits = "";
	//
	// private String productVersion = "";
	//
	// private String productType = "";
	//
	// private String additionalUnits = "";
	//
	// private String productStatus = "";
	//
	// private String validRange = "";
	//
	// private String invalid = "";
	//
	// private String productScaling = "";
	//
	// private String scalingSlope = "";
	//
	// private String scalingIntercept = "";
	//
	// private String browseFunc = "";
	//
	// private String browseRanges = "";

	private String name = "";

	private String fullName = "";

	private String rank = "";

	private String dims = "";

	private String chunkSize = "";

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
		 * Setting Product Dataset Attributes
		 */

		IIOMetadataNode pdsaNode = new IIOMetadataNode(
				"ProductDataSetAttributes");
		pdsaNode.setAttribute(APSAttributes.PDSA_CREATESOFTWARE,
				(String) attributesMap.get(APSAttributes.PDSA_CREATESOFTWARE));
		pdsaNode.setAttribute(APSAttributes.PDSA_CREATETIME, (String) attributesMap.get(APSAttributes.PDSA_CREATETIME));
		pdsaNode
				.setAttribute(APSAttributes.PDSA_CREATEPLATFORM,(String) attributesMap.get(APSAttributes.PDSA_CREATEPLATFORM));
		pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTNAME, (String) attributesMap.get(APSAttributes.PDSA_PRODUCTNAME));
		pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTALGORITHM,(String) attributesMap.get(APSAttributes.PDSA_PRODUCTALGORITHM));
		pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTUNITS,(String) attributesMap.get(APSAttributes.PDSA_PRODUCTUNITS));
		pdsaNode
				.setAttribute(APSAttributes.PDSA_PRODUCTVERSION, (String) attributesMap.get(APSAttributes.PDSA_PRODUCTVERSION));
		pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTTYPE, (String) attributesMap.get(APSAttributes.PDSA_PRODUCTTYPE));
		pdsaNode.setAttribute(APSAttributes.PDSA_ADDITIONALUNITS,(String) attributesMap.get(APSAttributes.PDSA_ADDITIONALUNITS));
		pdsaNode.setAttribute(APSAttributes.PDSA_PRODUCTSTATUS,(String) attributesMap.get(APSAttributes.PDSA_PRODUCTSTATUS));
		pdsaNode.setAttribute(APSAttributes.PDSA_VALIDRANGE, (String) attributesMap.get(APSAttributes.PDSA_VALIDRANGE));
		pdsaNode.setAttribute(APSAttributes.PDSA_INVALID, (String) attributesMap.get(APSAttributes.PDSA_INVALID));
		pdsaNode
				.setAttribute(APSAttributes.PDSA_PRODUCTSCALING,(String) attributesMap.get(APSAttributes.PDSA_PRODUCTSCALING));
		pdsaNode.setAttribute(APSAttributes.PDSA_SCALINGSLOPE, (String) attributesMap.get(APSAttributes.PDSA_SCALINGSLOPE));
		pdsaNode.setAttribute(APSAttributes.PDSA_SCALINGINTERCEPT,(String) attributesMap.get(APSAttributes.PDSA_SCALINGINTERCEPT));
		pdsaNode.setAttribute(APSAttributes.PDSA_BROWSEFUNC, (String) attributesMap.get(APSAttributes.PDSA_BROWSEFUNC));
		pdsaNode.setAttribute(APSAttributes.PDSA_BROWSERANGES, (String) attributesMap.get(APSAttributes.PDSA_BROWSERANGES));
		root.appendChild(pdsaNode);

		/**
		 * Setting Raster Properties
		 */

		IIOMetadataNode datasetNode = new IIOMetadataNode("DatasetProperties");
		datasetNode.setAttribute("Name", name);
		datasetNode.setAttribute("FullName", fullName);
		datasetNode.setAttribute("Rank", rank);
		datasetNode.setAttribute("Dims", dims);
		datasetNode.setAttribute("ChunkSize", chunkSize);
		root.appendChild(datasetNode);

		return root;
	}

	public APSHDFImageMetadata(Dataset dataset) {
		this();
		initializeFromDataset(dataset);
	}

	public APSHDFImageMetadata() {
		super(
				false,
				nativeMetadataFormatName,
				"it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadataFormat",
				null, null);
	}

	/**
	 * Initialize Metadata from a raster
	 * 
	 * @param raster
	 *            the <code>SwanRaster</code> from which retrieve data
	 * @param imageIndex
	 *            the imageIndex relying the required subdataset
	 */
	private void initializeFromDataset(Dataset dataset) {
		if (dataset == null)
			return;

		// TODO: Add syncronization

		Iterator metadataIt;
		try {
			metadataIt = dataset.getMetadata().iterator();
			
			//number of supported attributes
			final int attribNumber = APSAttributes.PDSA_ATTRIB.length;
			while (metadataIt.hasNext()) {

				// get Attributes
				final Attribute att = (Attribute) metadataIt.next();

				// get Attribute Name
				final String attribName = att.getName();
				
				// checks if the attribute name matches one of the supported
				// attributes
				for (int k=0; k<attribNumber;k++){
					//if matched
					if (attribName.equals(APSAttributes.PDSA_ATTRIB[k])){
						
						//getting values
						Object valuesList = att.getValue();
						String attribValue="";
						if (valuesList != null) {
							final String[] values = (String[]) valuesList;
							final int numValues = values.length;
							final StringBuffer sb = new StringBuffer();
							int i = 0;
							for (; i < numValues - 1; i++) {
								sb.append(values[i]).append(",");
							}
							//Setting a period separated values string
							sb.append(values[i]);
							attribValue = sb.toString();
						}
						
						//putting the <attribut Name, attribute value> couple
						//in the map
						attributesMap.put((String)attribName, attribValue);
						break;
					}
				}
			}

			// Setting Dataset Properties
			// Setting dims
			final long dims[] = dataset.getDims();
			if (dims != null) {
				final int dimsLength = dims.length;
				final StringBuffer sb = new StringBuffer();
				for (int i = 0; i < dimsLength; i++) {
					sb.append(Long.toString(dims[i])).append(",");
				}
				this.dims = sb.toString().trim();
			}

			// setting chunkSize
			final long chunkSize[] = dataset.getChunkSize();

			if (chunkSize != null) {
				final int chunkSizeLength = chunkSize.length;
				final StringBuffer sb = new StringBuffer();
				for (int i = 0; i < chunkSizeLength; i++) {
					sb.append(Long.toString(chunkSize[i])).append(",");
				}
				this.chunkSize = sb.toString().trim();
			}

			final int rank = dataset.getRank();
			this.rank = Integer.toString(rank);
			this.name = dataset.getName();
			this.fullName = dataset.getFullName();
		} catch (Exception e) {
			// TODO Auto-generated catch block

		}

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
