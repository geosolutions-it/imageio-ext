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

import java.io.ByteArrayInputStream;

import it.geosolutions.imageio.plugins.jp2k.JP2KBox;

import it.geosolutions.util.KakaduUtilities;
import org.w3c.dom.DOMException;

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * This class is defined to represent a XML box of JPEG JP2 file format. This
 * type of box has a length, a type of "xml ". Its content is a text string of a
 * XML instance.
 */
public class XMLBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private JP2KBox wrappedBox;

    private byte[] content = null;

    private synchronized byte[] getContent() {
        if (content == null) {
            byte[] b = wrappedBox.getContent();
            if (b != null)
                content = b.clone();
        }
        return content;

    }

    public XMLBoxMetadataNode(final JP2KBox box) {
        super(box);
        wrappedBox = box;
    }

    public String getXml() {
        return KakaduUtilities.readTerminatedString(getContent());
    }

    public ByteArrayInputStream getRawXml() {
        return new ByteArrayInputStream(getContent());
    }

    @Override
    public String getNodeValue() throws DOMException {
        return getXml();
    }

    @Override
    public Object getUserObject() {
        return getContent();
    }
}
