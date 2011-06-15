/*
 * $RCSfile: PaletteBox.java,v $
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
 * $Date: 2005/02/11 05:01:36 $
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

import java.awt.image.IndexColorModel;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is designed to represent a palette box for JPEG 2000 JP2 file
 * format. A palette box has a length, and a fixed type of "pclr". Its content
 * contains the number of palette entry, the number of color components, the bit
 * depths of the output components, the LUT. Currently, only 8-bit color index
 * is supported.
 */
@SuppressWarnings("serial")
public class PaletteBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x70636C72;

    public final static String NAME = "pclr";

    public final static String JP2K_MD_NAME = "JP2KPaletteBox";

    /**
     * The value of the data elements.
     * 
     * @uml.property name="numEntries"
     */
    private int numEntries;

    private int numComps;

    private byte[] bitDepth;

    private byte[][] lut;

    private byte[] localdata;

    /** Compute the length of this box. */
    private static int computeLength(IndexColorModel icm) {
        int size = icm.getMapSize();
        int[] comp = icm.getComponentSize();
        return 11 + comp.length + size * comp.length;
    }

    /**
     * Gets the size of the components or the bit depth for all the color
     * components.
     */
    private static byte[] getCompSize(IndexColorModel icm) {
        int[] comp = icm.getComponentSize();
        int size = comp.length;
        byte[] buf = new byte[size];
        for (int i = 0; i < size; i++)
            buf[i] = (byte) (comp[i] - 1);
        return buf;
    }

    /**
     * Gets the LUT from the <code>IndexColorModel</code> as a two-dimensional
     * byte array.
     */
    private static byte[][] getLUT(IndexColorModel icm) {
        int[] comp = icm.getComponentSize();
        int size = icm.getMapSize();
        byte[][] lut = new byte[comp.length][size];
        icm.getReds(lut[0]);
        icm.getGreens(lut[1]);
        icm.getBlues(lut[2]);
        if (comp.length == 4)
            icm.getAlphas(lut[3]);
        return lut;
    }

    /**
     * Constructs a <code>PaletteBox</code> from an
     * <code>IndexColorModel</code>.
     */
    public PaletteBox(IndexColorModel icm) {
        this(computeLength(icm), getCompSize(icm), getLUT(icm));
    }

    /**
     * Constructs a <code>PlatteBox</code> from a
     * <code>org.w3c.dom.Node</code>.
     */
    public PaletteBox(Node node) throws IIOInvalidTreeException {
        super(node);
        byte[][] tlut = null;
        int index = 0;

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();

            if ("NumberEntries".equals(name)) {
                numEntries = BoxUtilities.getIntElementValue(child);
            }

            if ("NumberColors".equals(name)) {
                numComps = BoxUtilities.getIntElementValue(child);
            }

            if ("BitDepth".equals(name)) {
                bitDepth = BoxUtilities.getByteArrayElementValue(child);
            }

            if ("LUT".equals(name)) {
                tlut = new byte[numEntries][];

                NodeList children1 = child.getChildNodes();

                for (int j = 0; j < children1.getLength(); j++) {
                    Node child1 = children1.item(j);
                    name = child1.getNodeName();
                    if ("LUTRow".equals(name)) {
                        tlut[index++] = BoxUtilities
                                .getByteArrayElementValue(child1);
                    }
                }
            }
        }

        // XXX: currently only 8-bit LUT is supported so no decode is needed
        // For more refer to read palette box section.
        lut = new byte[numComps][numEntries];

        for (int i = 0; i < numComps; i++)
            for (int j = 0; j < numEntries; j++)
                lut[i][j] = tlut[j][i];

    }

    /**
     * Constructs a <code>PaletteBox</code> from the provided length, bit
     * depths of the color components and the LUT.
     */
    public PaletteBox(int length, byte[] comp, byte[][] lut) {
        super(length, BOX_TYPE, null);
        this.bitDepth = comp;
        this.lut = lut;
        this.numEntries = lut[0].length;
        this.numComps = lut.length;
    }

    /**
     * Constructs a <code>PaletteBox</code> from the provided byte array.
     */
    public PaletteBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    /**
     * Return the number of palette entries.
     * 
     * @uml.property name="numEntries"
     */
    public int getNumEntries() {
        return numEntries;
    }

    /** Return the number of color components. */
    public int getNumComp() {
        return numComps;
    }

    /** Return the bit depths for all the color components. */
    public byte[] getBitDepths() {
        return bitDepth;
    }

    /** Return the LUT. */
    public byte[][] getLUT() {
        return lut;
    }

    /**
     * creates an <code>IIOMetadataNode</code> from this palette box. The
     * format of this node is defined in the XML dtd and xsd for the JP2 image
     * file.
     */
    public IIOMetadataNode getNativeNode() {
        return new PaletteBoxMetadataNode(this);
    }

    protected void parse(byte[] data) {
        if (data == null)
            return;
        numEntries = (short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));

        numComps = data[2];
        bitDepth = new byte[numComps];
        System.arraycopy(data, 3, bitDepth, 0, numComps);

        lut = new byte[numComps][numEntries];
        for (int i = 0, k = 3 + numComps; i < numEntries; i++)
            for (int j = 0; j < numComps; j++)
                lut[j][i] = data[k++];
    }

    protected synchronized byte[] compose() {
        if (localdata != null)
            return localdata;
        localdata = new byte[3 + numComps + numEntries * numComps];
        localdata[0] = (byte) (numEntries >> 8);
        localdata[1] = (byte) (numEntries & 0xFF);

        localdata[2] = (byte) numComps;
        System.arraycopy(bitDepth, 0, localdata, 3, numComps);

        for (int i = 0, k = 3 + numComps; i < numEntries; i++)
            for (int j = 0; j < numComps; j++)
                localdata[k++] = lut[j][i];

        return localdata;
    }
}
