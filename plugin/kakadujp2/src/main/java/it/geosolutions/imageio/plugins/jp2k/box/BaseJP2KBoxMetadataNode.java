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
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class BaseJP2KBoxMetadataNode extends IIOMetadataNode {

//    private final String boxLength;

    private final String boxType;

//    private final String extraLength;
    
    BaseJP2KBoxMetadataNode(final JP2KBox box) {
        super(BoxUtilities.getName(box.getType()));
//        final int l = box.getLength();
//        boxLength = Integer.toString(l);
        final int t = box.getType();
        boxType = BoxUtilities.getTypeString(t);
//        final long exl = box.getExtraLength();
//        extraLength = Long.toString(exl);
//        setAttribute("Length", this.boxLength);
        setAttribute("Type", this.boxType);
//        if (l == 1) {
//            setAttribute("ExtraLength", this.extraLength);
//        }
    }

//    public String getBoxLength() {
//        return boxLength;
//    }

    public String getBoxType() {
        return boxType;
    }

//    public String getExtraLength() {
//        return extraLength;
//    }

}
