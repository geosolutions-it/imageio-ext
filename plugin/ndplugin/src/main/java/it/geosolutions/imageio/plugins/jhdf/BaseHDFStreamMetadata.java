package it.geosolutions.imageio.plugins.jhdf;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

public abstract class BaseHDFStreamMetadata extends IIOMetadata {

	public BaseHDFStreamMetadata(boolean standardMetadataFormatSupported,
			String nativeMetadataFormatName,
			String nativeMetadataFormatClassName,
			String[] extraMetadataFormatNames,
			String[] extraMetadataFormatClassNames) {
		super(standardMetadataFormatSupported, nativeMetadataFormatName,
				nativeMetadataFormatClassName, extraMetadataFormatNames,
				extraMetadataFormatClassNames);
	}
	
	/**
	 * Build a node having name <code>nodeName</code> and attributes retrieved
	 * from the provided <code>attribMap</code>. 
	 * 
	 * @param attribMap
	 * 		A <code>Map</code> containing couples (attrib name, attrib value) 
	 * @param nodeName
	 * 		the name which need to be set for the node.
	 * @return
	 * 		the built node.
	 */
	protected IIOMetadataNode buildAttributesNodeFromMap(final Map attribMap, final String nodeName){
		final IIOMetadataNode node = new IIOMetadataNode(nodeName);
		synchronized (attribMap) {
			if (attribMap!=null){
				final Set set = attribMap.keySet();
				final Iterator iter = set.iterator();
				while (iter.hasNext()) {
					final String key = (String)iter.next();
					final String attribValue = (String) attribMap.get(key);
					node.setAttribute(key, attribValue);
				}
			}
		}
		return node;
	}

}
