package it.geosolutions.imageio.plugins;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * @author Daniele Romagnoli
 */
public abstract class ImageMetadata extends IIOMetadata {

	protected int imageIndex;

	protected String sourceURI;

	protected abstract IIOMetadataNode appendBandsNode();
	
	// TODO: Change this name
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.imagemetadata";

	public boolean isReadOnly() {
		// XXX
		return false;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		// XXX
	}

	public void reset() {
		// XXX
	}

	public Node getAsTree(String formatName) {
		if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
			return getCommonNativeTree();
		throw new UnsupportedOperationException(formatName
				+ " is not a supported format name");
	}

	private Node getCommonNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);
		final IIOMetadataNode imageMetadataNode = new IIOMetadataNode(
				"ImageMetadata");
		imageMetadataNode.setAttribute("imageIndex", Integer
				.toString(imageIndex));
		imageMetadataNode.setAttribute("sourceURI", sourceURI);

		IIOMetadataNode rasterGeometryNode = new IIOMetadataNode("RasterGeometry");
		imageMetadataNode.appendChild(rasterGeometryNode);

		IIOMetadataNode bandsNode = appendBandsNode();
		
		imageMetadataNode.appendChild(bandsNode);
		
		//TODO: add Statistics
		root.appendChild(imageMetadataNode);

		return root;
	}

}
