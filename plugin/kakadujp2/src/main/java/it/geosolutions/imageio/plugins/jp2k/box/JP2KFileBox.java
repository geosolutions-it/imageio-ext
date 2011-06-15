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

import javax.imageio.metadata.IIOMetadataNode;

/**
 * Fake node to represent that a JPEG2000 file is a superbox itself.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
public class JP2KFileBox extends DefaultJP2KBox implements JP2KBox {

    /**
     * 
     */
    private static final long serialVersionUID = -2615305045686220671L;

    public static final int BOX_TYPE = 0x00000001;

    public final static String JP2K_MD_NAME = "JP2KFileBoxes";

    public final static String NAME = "jp2k";

    @Override
    public synchronized byte[] getContent() {
        return null;
    }

    public JP2KFileBox() {
        super(-1, BOX_TYPE, null);
    }
    
    public IIOMetadataNode getNativeNode() {
        String name = BoxUtilities.getName(getType());
        IIOMetadataNode node = new IIOMetadataNode(name);
        return node;
    }
}
