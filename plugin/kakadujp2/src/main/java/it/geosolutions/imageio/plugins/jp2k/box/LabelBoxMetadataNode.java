/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2k.box;

import javax.imageio.metadata.IIOMetadataNode;

/**
 * This class is defined to represent a Label Box of JPEG JP2 file format.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
@SuppressWarnings("serial")
public class LabelBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    public final static String GML_DATA = "gml.data";
    
    private String text;

    public String getText() {
        return text;
    }

    LabelBoxMetadataNode(final LabelBox box) {
        super(box);
        text = box.getText();
        try {
            IIOMetadataNode child = new IIOMetadataNode("text");
            child.setNodeValue(text);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }
}
