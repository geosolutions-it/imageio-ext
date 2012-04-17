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
 * This class is defined to represent a Data Entry URL Box of JPEG JP2 file
 * format. A Data Entry URL Box has a length, and a fixed type of "url ". Its
 * content are a one-byte version, a three-byte flags and a URL pertains to the
 * UUID List box within its UUID Info superbox.
 */
@SuppressWarnings("serial")
public class DataEntryURLBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String version;

    private String flags;

    private String url;

    DataEntryURLBoxMetadataNode(final DataEntryURLBox box) {
        super(box);
        final byte v = box.getVersion();
        version = ImageUtil.convertObjectToString(v);
        final byte[] fl = box.getFlags();
        flags = ImageUtil.convertObjectToString(fl);
        url = box.getURL();
        try {
            IIOMetadataNode child = new IIOMetadataNode("Version");
            child.setUserObject(new Byte(v));
            child.setNodeValue(version);
            appendChild(child);
            
            child = new IIOMetadataNode("Flags");
            child.setUserObject(fl);
            child.setNodeValue(flags);
            appendChild(child);
            
            child = new IIOMetadataNode("URL");
            child.setNodeValue(url);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }

    public String getVersion() {
        return version;
    }

    public String getFlags() {
        return flags;
    }

    /** Returns the <code>URL</code> data element. */
    public String getURL() {
        return url;
    }
}
