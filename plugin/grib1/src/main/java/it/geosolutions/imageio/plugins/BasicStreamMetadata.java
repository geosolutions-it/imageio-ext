package it.geosolutions.imageio.plugins;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * @author Daniele Romagnoli
 */
public abstract class BasicStreamMetadata extends IIOMetadata {

//	 TODO: Change this name
	public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_basicstreammetadata_1.0";

	protected String format;

	protected String description;

	protected long numImages;

	protected abstract IIOMetadataNode buildImagesNode(IIOMetadataNode imagesNode);
	
	public BasicStreamMetadata(final String format, final String description,
			final long numImages) {
		this.format = format;
		this.description = description;
		this.numImages = numImages;
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
		final IIOMetadataNode basicStreamMetadataNode = new IIOMetadataNode("BasicStreamMetadata");
		
		IIOMetadataNode formatNode = new IIOMetadataNode("format");
		formatNode.setNodeValue(format);
		basicStreamMetadataNode.appendChild(formatNode);
		
		IIOMetadataNode descriptionNode = new IIOMetadataNode("description");
		descriptionNode.setNodeValue(description);
		basicStreamMetadataNode.appendChild(descriptionNode);
		
		IIOMetadataNode numImagesNode = new IIOMetadataNode("numImages");
		numImagesNode.setNodeValue(Long.toString(numImages));
		basicStreamMetadataNode.appendChild(numImagesNode);
		
		IIOMetadataNode imagesNode = new IIOMetadataNode(
		"images");
		
		imagesNode = buildImagesNode(imagesNode);
				
		basicStreamMetadataNode.appendChild(imagesNode);
		root.appendChild(basicStreamMetadataNode);
		
		return root;
	}
	
	public boolean isReadOnly() {
		return false;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
	}

	public void reset() {

	}

}
