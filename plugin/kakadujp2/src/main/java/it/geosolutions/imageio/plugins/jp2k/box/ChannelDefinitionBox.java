/*
 * $RCSfile: ChannelDefinitionBox.java,v $
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
 * $Date: 2005/02/11 05:01:31 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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

import java.awt.image.ColorModel;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is designed to represent a Channel Definition Box of JPEG JP2 file format. 
 * A Channel Definition Box has a length, and a fixed type of "cdef". 
 * Its content defines the type of the image channels: color channel, alpha channel or 
 * premultiplied alpha channel.
 *
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
@SuppressWarnings("serial")
public class ChannelDefinitionBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x63646566;

    public final static String NAME = "cdef";

    public final static String JP2K_MD_NAME = "JP2KChannelDefinitionBox";

    /** The cached data elements. */
    private short num;

    private short[] channels;

    /**
     * @uml.property name="types"
     */
    private int[] types;

    private short[] associations;

    private byte[] localData;

    /**
     * Computes the length of this box from the provided <code>ColorModel</code>.
     */
    private static int computeLength(ColorModel colorModel) {
        int length = colorModel.getComponentSize().length - 1;
        return 10 + (colorModel.isAlphaPremultiplied() ? length * 18
                : length * 12);
    }

    /**
     * Fills the channel definitions into the arrays based on the number of
     * components and isPremultiplied.
     */
    public static void fillBasedOnBands(int numComps, boolean isPremultiplied,
            short[] c, int[] t, short[] a) {
        int num = numComps * (isPremultiplied ? 3 : 2);
        if (isPremultiplied) {
            for (int i = numComps * 2; i < num; i++) {
                c[i] = (short) (i - numComps * 2);
                t[i] = 2; // 2 -- premultiplied
                a[i] = (short) (i + 1 - numComps * 2);
            }
        }

        for (int i = 0; i < numComps; i++) {
            int j = i + numComps;
            c[i] = (short) i;
            t[i] = 0; // The original channel
            a[j] = a[i] = (short) (i + 1);

            c[j] = (short) numComps;
            t[j] = 1; // 1 -- transparency
        }
    }

    /**
     * Constructs a <code>ChannelDefinitionBox</code> based on the provided
     * <code>ColorModel</code>.
     */
    public ChannelDefinitionBox(ColorModel colorModel) {
        super(computeLength(colorModel), BOX_TYPE, null);

        // creates the buffers for the channel definitions.
        short length = (short) (colorModel.getComponentSize().length - 1);
        num = (short) (length * (colorModel.isAlphaPremultiplied() ? 3 : 2));
        channels = new short[num];
        types = new int[num];
        associations = new short[num];

        // fills the arrays.
        fillBasedOnBands(length, colorModel.isAlphaPremultiplied(), channels,
                types, associations);
    }

    /**
     * Constructs a <code>ChannelDefinitionBox</code> based on the provided
     * content in byte array.
     */
    public ChannelDefinitionBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    /**
     * Constructs a <code>ChannelDefinitionBox</code> based on the provided
     * channel definitions.
     */
    public ChannelDefinitionBox(short[] channel, int[] types,
            short[] associations) {
        super(10 + channel.length * 6, BOX_TYPE, null);
        this.num = (short) channel.length;
        this.channels = channel;
        this.types = types;
        this.associations = associations;
    }

    /**
     * Constructs a <code>ChannelDefinitionBox</code> based on the provided
     * <code>org.w3c.dom.Node</code>.
     */
    public ChannelDefinitionBox(Node node) throws IIOInvalidTreeException {
        super(node);
        NodeList children = node.getChildNodes();
        int index = 0;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();

            if ("NumberOfDefinition".equals(name)) {
                num = BoxUtilities.getShortElementValue(child);
            }

            if ("Definitions".equals(name)) {
                channels = new short[num];
                types = new int[num];
                associations = new short[num];

                NodeList children1 = child.getChildNodes();

                for (int j = 0; j < children1.getLength(); j++) {
                    child = children1.item(j);
                    name = child.getNodeName();
                    if ("ChannelNumber".equals(name)) {
                        channels[index] = BoxUtilities
                                .getShortElementValue(child);
                    }

                    if ("ChannelType".equals(name)) {
                        types[index] = BoxUtilities.getShortElementValue(child);
                    }

                    if ("Association".equals(name)) {
                        associations[index++] = BoxUtilities
                                .getShortElementValue(child);
                    }
                }
            }
        }
    }

    /** Parse the channel definitions from the content data array. */
    protected void parse(byte[] data) {
        num = (short) ((data[0] << 8) | data[1]);
        channels = new short[num];
        types = new int[num];
        associations = new short[num];

        for (int i = 0, j = 2; i < num; i++) {
            channels[i] = (short) (((data[j++] & 0xFF) << 8) | (data[j++] & 0xFF));
            types[i] = (int) (((data[j++] & 0xFF) << 8) | (data[j++] & 0xFF));
            associations[i] = (short) (((data[j++] & 0xFF) << 8) | (data[j++] & 0xFF));
        }
    }

    /** Returns the defined channels. */
    public short[] getChannel() {
        return channels;
    }

    /**
	 * Returns the channel types.
	 * @uml.property  name="types"
	 */
    public int[] getTypes() {
        return types;
    }

    /**
     * Returns the association which associates a color channel to a color
     * component in the color space of the image.
     */
    public short[] getAssociation() {
        return associations;
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this channel definition
     * box. The format of this node is defined in the XML dtd and xsd for the
     * JP2 image file.
     */
    public IIOMetadataNode getNativeNode() {
        IIOMetadataNode node = new IIOMetadataNode(BoxUtilities
                .getName(getType()));
        setDefaultAttributes(node);

        IIOMetadataNode child = new IIOMetadataNode("NumberOfDefinition");
        child.setUserObject(new Short(num));
        child.setNodeValue("" + num);
        node.appendChild(child);

        child = new IIOMetadataNode("Definitions");
        node.appendChild(child);

        for (int i = 0; i < num; i++) {
            IIOMetadataNode child1 = new IIOMetadataNode("ChannelNumber");
            child1.setUserObject(new Short(channels[i]));
            child1.setNodeValue("" + channels[i]);
            child.appendChild(child1);

            child1 = new IIOMetadataNode("ChannelType");
            child1.setUserObject(new Integer(types[i]));
            child1.setNodeValue("" + types[i]);
            child.appendChild(child1);

            child1 = new IIOMetadataNode("Association");
            child1.setUserObject(new Short(associations[i]));
            child1.setNodeValue("" + associations[i]);
            child.appendChild(child1);
        }

        return node;
    }

    protected byte[] compose() {
        if (localData != null)
            return localData;
        int len = num * 6 + 2;
        localData = new byte[len];
        localData[0] = (byte) (num >> 8);
        localData[1] = (byte) (num & 0xFF);

        for (int i = 0, j = 2; i < num; i++) {
            localData[j++] = (byte) (channels[i] >> 8);
            localData[j++] = (byte) (channels[i] & 0xFF);

            localData[j++] = (byte) (types[i] >> 8);
            localData[j++] = (byte) (types[i] & 0xFF);

            localData[j++] = (byte) (associations[i] >> 8);
            localData[j++] = (byte) (associations[i] & 0xFF);
        }
        return localData;
    }

    public short getNum() {
        return num;
    }
    
}
