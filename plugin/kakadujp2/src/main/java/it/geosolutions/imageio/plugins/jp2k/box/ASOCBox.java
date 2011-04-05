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

import org.w3c.dom.Node;

/**
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
@SuppressWarnings("serial")
public class ASOCBox extends DefaultJP2KBox {

    public final static int BOX_TYPE = 0x61736F63;

    public final static String NAME = "asoc";

    public final static String JP2K_MD_NAME = "JP2KAsocBox";

    public ASOCBox(Node node) throws IIOInvalidTreeException {
        super(node);
    }
    
    public ASOCBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    @Override
    protected byte[] compose() {
        return null;
    }

    @Override
    protected void parse(byte[] data) {
        
    }
}
