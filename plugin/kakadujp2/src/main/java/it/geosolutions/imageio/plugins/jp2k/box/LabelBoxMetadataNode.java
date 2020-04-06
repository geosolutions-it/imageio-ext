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

import it.geosolutions.imageio.plugins.jp2k.JP2KBox;
import it.geosolutions.util.KakaduUtilities;

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

    public LabelBoxMetadataNode(final JP2KBox box) {
        super(box);
        text = KakaduUtilities.readTerminatedString(box.getContent());
        try {
            IIOMetadataNode child = new IIOMetadataNode("text");
            child.setNodeValue(text);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }
}
