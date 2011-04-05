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
 * Default implementation of a BaseJP2KBox.
 * 
 * <p>
 * This class actually does nothing.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
class DefaultJP2KBox extends BaseJP2KBox {

    private static final long serialVersionUID = 5452395379686282653L;

    public DefaultJP2KBox(int length, int type, byte[] data) {
        super(length, type, data);
    }

    public DefaultJP2KBox(int length, int type, long extraLength, byte[] data) {
        super(length, type, extraLength, data);
    }

    public DefaultJP2KBox(Node node) throws IIOInvalidTreeException {
        super(node);
    }
    
    @Override
    protected void parse(byte[] data) {
        // do nothing
    }

    @Override
    protected byte[] compose() {
        return null;
    }
}
