/*
 * $RCSfile: UUIDBox.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 05:01:37 $
 * $State: Exp $
 */
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
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is defined to represent a UUID Box of JPEG JP2 file format. This
 * type of box has a length, a type of "uuid". Its content is a 16-byte UUID
 * followed with a various-length data.
 */
@SuppressWarnings("serial")
public class UUIDBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x75756964;

    public final static String NAME = "uuid";

    public final static String JP2K_MD_NAME = "JP2KUUIDBox";

    /**
     * Cache the element names for this box's xml definition
     * 
     * @uml.property name="elementNames"
     */
    private static String[] elementNames = { "UUID", "Data" };

    /**
     * This method will be called by the getNativeNodeForSimpleBox of the class
     * Box to get the element names.
     * 
     * @uml.property name="elementNames"
     */
    public static String[] getElementNames() {
        return elementNames;
    }

    /** The data elements in this UUID box. */
    private byte[] uuid;

    private byte[] udata;

    private byte[] localData;

    /** Constructs a <code>UUIDBox</code> from its content data array. */
    public UUIDBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    /**
     * Constructs a <code>UUIDBox</code> based on the provided
     * <code>org.w3c.dom.Node</code>.
     */
    public UUIDBox(Node node) throws IIOInvalidTreeException {
        super(node);
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();

            if ("UUID".equals(name)) {
                uuid = BoxUtilities.getByteArrayElementValue(child);
            }

            if ("Data".equals(name)) {
                udata = BoxUtilities.getByteArrayElementValue(child);
            }
        }
    }

    /** Parses the data elements from the provided data array. */
    protected void parse(byte[] data) {
        uuid = new byte[16];
        System.arraycopy(data, 0, uuid, 0, 16);
        udata = new byte[data.length - 16];
        System.arraycopy(data, 16, udata, 0, udata.length);
    }

    /** Returns the UUID of this box. */
    public byte[] getUUID() {
        return uuid;
    }

    /** Returns the UUID data of this box. */
    public byte[] getData() {
        return udata;
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this UUID box. The format
     * of this node is defined in the XML dtd and xsd for the JP2 image file.
     */
    public IIOMetadataNode getNativeNode() {
        return new UUIDBoxMetadataNode(this);
    }

    protected synchronized byte[] compose() {
        if (localData != null)
            return localData;
        localData = new byte[16 + udata.length];
        System.arraycopy(uuid, 0, localData, 0, 16);
        System.arraycopy(udata, 0, localData, 16, udata.length);
        return localData;
    }
}
