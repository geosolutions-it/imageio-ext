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
package it.geosolutions.imageio.plugins.jp2k.box;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is defined to represent a Label Box of JPEG JP2 file
 * format. 
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
@SuppressWarnings("serial")
public class LabelBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x6c626c20;

    public final static String NAME = "lbl\040";

    public final static String JP2K_MD_NAME = "JP2KLabelBox";

    /**
     * Cache the element names for this box's xml definition
     * 
     * @uml.property name="elementNames"
     */
    private static String[] elementNames = { "Text" };

    /**
     * This method will be called by the getNativeNodeForSimpleBox of the class
     * Box to get the element names.
     * 
     * @uml.property name="elementNames"
     */
    public static String[] getElementNames() {
        return elementNames;
    }

    private byte[] localData;

    private String text;

    /** Constructs a <code>LabelBox</code> from its content data. */
    public LabelBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    /** Constructs a <code>LabelBox</code> from its data elements. */
    public LabelBox(final String text) {
        super(2 * text.length(), BOX_TYPE, text.getBytes());
        this.text = text;
    }

    /** Constructs a <code>LabelBox</code> from a Node. */
    public LabelBox(Node node) throws IIOInvalidTreeException {
        super(node);
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();

            if ("Text".equals(name)) {
                text = BoxUtilities.getStringElementValue(child);
            }
        }
    }

    /** Parses the content of this box from its content byte array. */
    protected void parse(byte[] data) {
        text = new String(data);
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this Label box.
     * The format of this node is defined in the XML dtd and xsd for the JP2
     * image file.
     */
    public IIOMetadataNode getNativeNode() {
        return getNativeNodeForSimpleBox();
    }

    protected synchronized byte[] compose() {
        if (localData != null)
            return localData;
        localData = text.getBytes();
        return localData;
    }

    public String getText() {
        return text;
    }
}
