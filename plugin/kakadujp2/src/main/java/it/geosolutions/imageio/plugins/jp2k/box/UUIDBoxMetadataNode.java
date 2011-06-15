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

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * This class is defined to represent a UUID Box of JPEG JP2 file format. This
 * type of box has a length, a type of "uuid". Its content is a 16-byte UUID
 * followed with a various-length data.
 */
public class UUIDBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private JP2KBox wrappedBox;

    private byte[] content = null;

    private byte[] data = null;

    private byte[] uuid = null;

    private synchronized byte[] getContent() {
        if (content == null) {
            byte[] b = wrappedBox.getContent();
            if (b != null) {
                content = b;
                if (uuid == null) {
                    uuid = new byte[16];
                    System.arraycopy(content, 0, uuid, 0, 16);
                }
                if (data == null) {
                    data = new byte[content.length - 16];
                    System.arraycopy(content, 16, data, 0, data.length);
                }

            }
        }
        return content;
    }

    public UUIDBoxMetadataNode(final JP2KBox box) {
        super(box);
        wrappedBox = box;
    }

    public byte[] getUuid() {
        getContent();
        if (uuid!=null)
            return uuid.clone();
        return uuid;
    }

    public byte[] getData() {
        getContent();
        if (data!=null)
            return data.clone();
        return data;
    }
    
    @Override
    public String getNodeValue() {
        return ImageUtil.convertObjectToString(getContent());
    }

    @Override
    public Object getUserObject() {
        getContent();
        if (content!=null)
            return content.clone();
        return null;
    }
}
