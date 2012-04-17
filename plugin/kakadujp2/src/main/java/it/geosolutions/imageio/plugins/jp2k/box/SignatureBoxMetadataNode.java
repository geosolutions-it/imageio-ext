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

/**
 * This class is defined to represent a Signature Box of JPEG JP2 file format.
 * This type of box has a fixed length of 12, a type of "jP " and a four byte
 * content of 0x0D0A870A, which is used to detects of the common file
 * transmission errors which substitutes <CR><LF> with <LF> or vice versa.
 * 
 * <p>
 * <strong>It must be the first box in a JP2 file.</strong>
 * </p>
 */
@SuppressWarnings("serial")
public class SignatureBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String signature;
    
    SignatureBoxMetadataNode(final SignatureBox box) {
        super(box);
        signature = SignatureBox.SIGNATURE;
        try {
            IIOMetadataNode child = new IIOMetadataNode("Content");
            child.setUserObject(box.getContent());
            child.setNodeValue(signature);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }

    public String getSignature() {
        return signature;
    }
}
