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
 * This class is defined to represent a File Type Box of JPEG JP2 file format. 
 * A File Type Box has a length, and a fixed type of "ftyp". 
 * The content of a file type box contains the brand ("jp2 " for JP2 file", 
 * the minor version (0 for JP2 file format), and a compatibility list 
 * (one of which should be "jp2 " if brand is not "jp2 ".)
 */
@SuppressWarnings("serial")
public class FileTypeBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private final String brand;
    
    private final String minorVersion;
    
    private final String compatibilityList;
    
    FileTypeBoxMetadataNode(final FileTypeBox box) {
        super(box);
        brand = ImageUtil.convertObjectToString(box.getBrand());
        minorVersion = Integer.toString(box.getMinorVersion());
        compatibilityList= ImageUtil.convertObjectToString(box.getCompatibilitySet());
        
        try {
            IIOMetadataNode child = new IIOMetadataNode("Brand");
            child.setNodeValue(brand);
            appendChild(child);
            child = new IIOMetadataNode("MinorVersion");
            child.setNodeValue(minorVersion);
            appendChild(child);
            child = new IIOMetadataNode("CompatibilityList");
            child.setNodeValue(compatibilityList);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }

    public String getBrand() {
        return brand;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public String getCompatibilityList() {
        return compatibilityList;
    }

}
