package it.geosolutions.imageio.plugins;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * @author Daniele Romagnoli
 */
public abstract class CoverageMetadata extends IIOMetadata {

	//TODO:Set it properly
	protected String name="WIND";

	protected String description;

	
	//TODO: should be an universal method?
	protected abstract IIOMetadataNode buildCoverageDescriptionsNode(
			final IIOMetadataNode coverageDescriptionsNode);

	// TODO: Change this name
	public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_coveragemetadata_1.0";

	public Node getAsTree(String formatName) {
		if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
			return getCommonNativeTree();
		throw new UnsupportedOperationException(formatName
				+ " is not a supported format name");
	}

	private Node getCommonNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);
		final IIOMetadataNode coverageMetadataNode = new IIOMetadataNode(
				"CoverageMetaData");

		// Setting name node
		IIOMetadataNode dummyNode = new IIOMetadataNode("name");
		dummyNode.setNodeValue(name);
		coverageMetadataNode.appendChild(dummyNode);

		// Setting description node
		dummyNode = new IIOMetadataNode("description");
		dummyNode.setNodeValue(description);
		coverageMetadataNode.appendChild(dummyNode);

		// TODO: Add DiscoveryMetaData node

		IIOMetadataNode coverageDescriptionsNode = new IIOMetadataNode(
				"coverageDescriptions");
		
		coverageDescriptionsNode = buildCoverageDescriptionsNode(coverageDescriptionsNode);		
		coverageMetadataNode.appendChild(coverageDescriptionsNode);

		// TODO: add Statistics
		root.appendChild(coverageMetadataNode);

		return root;
	}
}
