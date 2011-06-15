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
 * This class is defined to represent an Image Header Box of JPEG JP2 file
 * format. 
 */
@SuppressWarnings("serial")
public class JP2HeaderBox extends DefaultJP2KBox {

    public static final int BOX_TYPE = 0x6A703268;

    public final static String NAME = "jp2h";

    public final static String JP2K_MD_NAME = "JP2KJP2HeaderBox";

    /** Create a JP2 Header Box using the content data. */
    public JP2HeaderBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }
    
    public IIOMetadataNode getNativeNode() {
        String name = BoxUtilities.getName(getType());
        IIOMetadataNode node = new IIOMetadataNode(name);
        return node;
    }
}
