package it.geosolutions.imageio.plugins.jhdf;

import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Simple abstract class used to build a String representing the structure
 * of a <code>IIOMetadataNode</coded> tree.
 * 
 * @author Daniele Romagnoli
 *
 */
public abstract class MetadataDisplay {

	private static int depth=0;

	public static String buildMetadataFromNode(IIOMetadataNode node) {
		String localMetadata=""; 
		final String name = node.getNodeName();
		final StringBuffer sb = new StringBuffer("<").append(name);
		NamedNodeMap attributes = node.getAttributes();
		if (attributes!=null){
			final int nAttributes = attributes.getLength();
			if (nAttributes>0){
				sb.append(" ");
				int i=0;
				for (;i<nAttributes;i++){
					Node attribNode = attributes.item(i);
					final String attribName = attribNode.getNodeName();
					final String attribValue = attribNode.getNodeValue();
					sb.append("\"").append(attribName).append("\"=\"").append(attribValue).append("\"");
					if (i!=nAttributes-1)
						sb.append(" ");
				}
			}
		}
		sb.append(">");
		localMetadata = sb.toString();
		if (node.hasChildNodes()){
			depth++;
			localMetadata = localMetadata + "\n" + setTab(depth) + buildMetadataFromNode((IIOMetadataNode)node.getFirstChild());
		}
		IIOMetadataNode sibling = (IIOMetadataNode) node.getNextSibling();
		if (sibling!=null){
				localMetadata = localMetadata + "\n" + setTab(depth) + buildMetadataFromNode(sibling);
		}
		else 
			depth--;
		
		return localMetadata;
	}

	private static String setTab(int depth) {
		final StringBuffer sb = new StringBuffer();
		for (int i=0;i<depth;i++)
			sb.append("\t");
		return sb.toString();
	}
}
