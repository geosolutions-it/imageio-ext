/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.plugins.jp2k.box.ASOCBox;
import it.geosolutions.imageio.plugins.jp2k.box.ASOCBoxMetadataNode;
import it.geosolutions.imageio.plugins.jp2k.box.BoxUtilities;
import it.geosolutions.imageio.plugins.jp2k.box.ContiguousCodestreamBox;
import it.geosolutions.imageio.plugins.jp2k.box.JP2KFileBox;
import it.geosolutions.imageio.plugins.jp2k.box.LabelBox;
import it.geosolutions.imageio.plugins.jp2k.box.LabelBoxMetadataNode;
import it.geosolutions.imageio.plugins.jp2k.box.UUIDBox;
import it.geosolutions.imageio.plugins.jp2k.box.UUIDBoxMetadataNode;
import it.geosolutions.imageio.plugins.jp2k.box.XMLBox;
import it.geosolutions.imageio.plugins.jp2k.box.XMLBoxMetadataNode;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.w3c.dom.Node;

public class JP2KStreamMetadata extends IIOMetadata {

    public final static String NUM_CODESTREAMS = "NumberOfCodestreams";

    public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_jp2k_StreamMetadata_1.0";

    private DefaultTreeModel treeModel;

    private int numCodestreams;

    private SoftReference<Node> nativeTreeNodeRef;

    public JP2KStreamMetadata(final TreeModel treeModel,
            final int numCodestreams) {
        final JP2KFileBox box = (JP2KFileBox) treeModel.getRoot();
        if (box == null)
            throw new IllegalArgumentException("Originating tree is empty");
        this.numCodestreams = numCodestreams;
        final JP2KFileBox root = (JP2KFileBox) box.clone();
        this.treeModel = new DefaultTreeModel(root);
        final int childCount = box.getChildCount();
        int i = 0;
        while (i < childCount) {
            JP2KBox child = (JP2KBox) box.getChildAt(i);
            cloneTree(child, root, i++);
        }
    }

    private void cloneTree(final JP2KBox toBeCloned, final JP2KBox parent,
            int index) {
        final JP2KBox currentBox = (JP2KBox) ((JP2KBox) toBeCloned).clone();
        parent.insert(currentBox, index++);

        final int childCount = toBeCloned.getChildCount();
        int i = 0;
        while (i < childCount) {
            final JP2KBox child = (JP2KBox) toBeCloned.getChildAt(i);
            cloneTree(child, currentBox, i++);
        }
    }

    @Override
    public Node getAsTree(String formatName) {
        if (formatName.equalsIgnoreCase(nativeMetadataFormatName))
            return getNativeTree();
        else
            throw new IllegalArgumentException(formatName
                    + " is not a supported format name");
    }

    private Node getNativeTree() {
        Node nativeTree = this.nativeTreeNodeRef == null ? null
                : this.nativeTreeNodeRef.get();
        if (nativeTree == null) {
            IIOMetadataNode node = new IIOMetadataNode(nativeMetadataFormatName);
            JP2KBoxMetadata root = (JP2KBoxMetadata) treeModel.getRoot();
            nativeTree = buildTree(root);
            node.appendChild(nativeTree);
            this.nativeTreeNodeRef = new SoftReference<Node>(nativeTree);
        }
        return nativeTree;
    }

    private Node buildTree(final JP2KBoxMetadata node) {
        if (node == null)
            throw new IllegalArgumentException("Null node provided ");
        IIOMetadataNode mdNode;
        switch (node.getType()) {
        // Using LazyBox in the following 3 cases.
        case XMLBox.BOX_TYPE:
            mdNode = new XMLBoxMetadataNode(node);
            break;
        case UUIDBox.BOX_TYPE:
            mdNode = new UUIDBoxMetadataNode(node);
            break;
        case ASOCBox.BOX_TYPE:
            mdNode = new ASOCBoxMetadataNode(node);
            break;
        case LabelBox.BOX_TYPE:
            mdNode = new LabelBoxMetadataNode(node);
            break;
        case JP2KFileBox.BOX_TYPE:
            mdNode = node.getNativeNode();
            mdNode.setAttribute(NUM_CODESTREAMS, Integer.toString(0));
            break;
        case ContiguousCodestreamBox.BOX_TYPE:
            return null;

        default:
            mdNode = node.getNativeNode();
        }
        final int childCount = node.getChildCount();
        int i = 0;
        while (i < childCount) {
            final Node appendMe = buildTree((JP2KBoxMetadata) node
                    .getChildAt(i++));
            if (appendMe != null)
                mdNode.appendChild(appendMe);
        }
        return mdNode;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    // private ASOCBoxMetadataNode findParentASOCBoxMetadataNode(){
    // final Node rootNode = createNativeTree();
    // final IIOMetadataNode returnedNode[] = new IIOMetadataNode[1];
    // searchFirstOccurrenceNode (rootNode,
    // BoxUtilities.getName(ASOCBox.BOX_TYPE), returnedNode);
    // if (returnedNode[0]!=null)
    // return (ASOCBoxMetadataNode)returnedNode[0];
    // return null;
    // }
    //    

    /**
     * Search the first occurrence of a node related to the specified box type
     * and return it as a {@link IIOMetadataNode} or null in case of not found.
     * Search is performed visiting the children of a node before the brothers.
     */
    public IIOMetadataNode searchFirstOccurrenceNode(final int requestedBoxType) {
        final Node rootNode = getAsTree(nativeMetadataFormatName);
        final List<IIOMetadataNode> returnedNodes = new ArrayList<IIOMetadataNode>(1);
        searchOccurrencesNode(rootNode, BoxUtilities
                .getName(requestedBoxType), returnedNodes, true);
        if (!returnedNodes.isEmpty())
            return returnedNodes.get(0);
        return null;
    }
    
    /**
     * Search any JP2 Box metadata node occurrence of the specified type.
     * @param requestedBoxType 
     * 			the box type to be searched.
     * @return
     * 			a List containing any occurrence found.
     */
    public List<IIOMetadataNode> searchOccurrencesNode(final int requestedBoxType) {
        final Node rootNode = getAsTree(nativeMetadataFormatName);
        final List<IIOMetadataNode> returnedNodes = new ArrayList<IIOMetadataNode>(1);
        searchOccurrencesNode(rootNode, BoxUtilities
                .getName(requestedBoxType), returnedNodes, false);
        return returnedNodes;
    }

    private void searchOccurrencesNode(final Node node, final String requestedBoxType, 
    		final List<IIOMetadataNode> returnedNodes, final boolean exitFirstFound) {
        if (node != null) {
            if (node.getNodeName().equalsIgnoreCase(requestedBoxType)) {
            	boolean sameNode = false;
            	for (IIOMetadataNode foundNode : returnedNodes){
            		if (foundNode == node){
            			sameNode = true;
            			break;
            		}
            	}
            	if (!sameNode){
            		returnedNodes.add((IIOMetadataNode) node);
            		if (exitFirstFound)
            			return;
            	}
                
            }

            if (node.hasChildNodes()) {
                searchOccurrencesNode(node.getFirstChild(),
                        requestedBoxType, returnedNodes, exitFirstFound);
                if (returnedNodes != null && !returnedNodes.isEmpty() && exitFirstFound)
                    return;
                Node sibling = node.getNextSibling();
                while (sibling != null) {
                    searchOccurrencesNode(sibling, requestedBoxType,
                            returnedNodes, exitFirstFound);
                    if (returnedNodes != null && !returnedNodes.isEmpty() && exitFirstFound)
                        return;
                    sibling = sibling.getNextSibling();
                }
            }
        }
        return;
    }

    //    
    //    
    // public void parseGML(){
    // final ASOCBoxMetadataNode mainAsocBoxNode =
    // findParentASOCBoxMetadataNode();
    // if (mainAsocBoxNode!=null){
    // Node firstLabel = mainAsocBoxNode.getFirstChild();
    // if (firstLabel!=null &&
    // ((BaseJP2KBoxMetadataNode)firstLabel).getBoxType().equalsIgnoreCase(BoxUtilities.getBoxName(LabelBox.BOX_TYPE))){
    // String content = ((LabelBoxMetadataNode)firstLabel).getText();
    // if (content.equalsIgnoreCase(LabelBoxMetadataNode.GML_DATA)){
    // Node asocSubNode = firstLabel.getNextSibling();
    // while (asocSubNode!=null){
    // Node labelNode = asocSubNode.getFirstChild();
    // Node xmlNode = labelNode.getNextSibling();
    // if (xmlNode!=null && xmlNode instanceof XMLBoxMetadataNode){
    // XMLBoxMetadataNode xmlBoxNode = (XMLBoxMetadataNode) xmlNode;
    //                            
    // }
    // }
    // }
    // else{
    // if (LOGGER.isLoggable(Level.FINE)){
    // LOGGER.fine("Unable to find " + LabelBoxMetadataNode.GML_DATA + " Label
    // in this ASOC box");
    // }
    // }
    // }
    // }
    // }

    @Override
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
        throw new UnsupportedOperationException("MergeTree is unsupported");
    }

    @Override
    public void reset() {

    }

}
