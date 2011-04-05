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
 * This class is defined to represent a UUID list Box of JPEG JP2 file format.
 * This type of box has a length, a type of "ulst". Its contents include the
 * number of UUID entry and a list of 16-byte UUIDs.
 */
@SuppressWarnings("serial")
public class UUIDListBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String numberUUID;

    private short nUid;

    private String[] uuid;

    UUIDListBoxMetadataNode(final UUIDListBox box) {
        super(box);
        nUid = box.getNum();
        numberUUID = Short.toString(nUid);
        final byte[][] uuids = box.getUuids();
        uuid = new String[nUid];
        IIOMetadataNode child = new IIOMetadataNode("NumberUUID");
        child.setUserObject(new Short(nUid));
        child.setNodeValue(numberUUID);
        appendChild(child);

        for (int i = 0; i < nUid; i++) {
            child = new IIOMetadataNode("UUID");
            if (uuids[i]!=null)
                child.setUserObject(uuids[i].clone());
            uuid[i]=ImageUtil.convertObjectToString(uuids[i]);
            child.setNodeValue(uuid[i]);
            appendChild(child);
        }
    }

    public String getNumberUUID() {
        return numberUUID;
    }

    public String getUuid(final int index) {
        if (index > nUid - 1)
            throw new IllegalArgumentException("Number of UUID is "
                    + numberUUID);
        return uuid[index];
    }
}
