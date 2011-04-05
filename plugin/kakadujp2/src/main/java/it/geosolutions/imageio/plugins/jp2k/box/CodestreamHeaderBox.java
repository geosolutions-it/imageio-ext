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
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 * 
 * @TODO Actually, this implementation does nothing since this box may be
 *       ignored by a JP2 compatible reader.
 */
@SuppressWarnings("serial")
public class CodestreamHeaderBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x6A706368;

    public final static String NAME = "jpch";

    public final static String JP2K_MD_NAME = "JP2KCodestreamHeaderBox";

    public CodestreamHeaderBox(int length, int type, byte[] data) {
        super(length, type, data);
        // TODO Auto-generated constructor stub
    }
    
    public CodestreamHeaderBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }
    

    @Override
    protected byte[] compose() {
        return null;
    }

    @Override
    protected void parse(byte[] data) {
    }
    
    public IIOMetadataNode getNativeNode() {
        String name = BoxUtilities.getName(getType());
        IIOMetadataNode node = new IIOMetadataNode(name);
        return node;
    }

}
