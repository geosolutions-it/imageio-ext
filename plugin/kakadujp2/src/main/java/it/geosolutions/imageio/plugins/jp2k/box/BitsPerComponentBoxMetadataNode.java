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

import javax.imageio.metadata.IIOMetadataNode;

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * This class is defined to represent a Bits Per Component Box of JPEG JP2 file
 * format. A Bits Per Component box has a length, and a fixed type of "bpcc".
 * Its content is a byte array containing the bit depths of the color
 * components.
 * 
 * This box is necessary only when the bit depth are not identical for all the
 * components.
 *
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
@SuppressWarnings("serial")
public class BitsPerComponentBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String bitDepth;

    BitsPerComponentBoxMetadataNode(final BitsPerComponentBox box) {
        super(box);
        final byte[] bd = box.getBitDepth();
        bitDepth = ImageUtil.convertObjectToString(bd);
        try {
            IIOMetadataNode child = new IIOMetadataNode("BitDepth");
            child.setUserObject(bd);
            child.setNodeValue(bitDepth);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }

    public String getBitDepth() {
        return bitDepth;
    }
}
