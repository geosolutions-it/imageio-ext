/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import javax.imageio.metadata.IIOMetadataNode;

/**
 * This class is defined to represent a Bits Per Component Box of JPEG JP2 file format. A Bits Per Component box has a
 * length, and a fixed type of "bpcc". Its content is a byte array containing the bit depths of the color components.
 *
 * <p>This box is necessary only when the bit depth are not identical for all the components.
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
        bitDepth = ImageIOUtilities.convertObjectToString(bd);
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
