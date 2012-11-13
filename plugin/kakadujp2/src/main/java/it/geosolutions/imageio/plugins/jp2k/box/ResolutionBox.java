/*
 * $RCSfile: ResolutionBox.java,v $
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
 * This class is defined to represent a Resolution Box of JPEG JP2 file format.
 * A Resolution Box has a length, and a fixed type of "resc" (capture
 * resolution) or "resd" (default display resolution).
 * 
 * Its contents includes the resolution numerators, denominator, and the
 * exponents for both horizontal and vertical directions.
 */
@SuppressWarnings("serial")
public class ResolutionBox extends BaseJP2KBox {
    /** The data elements in this box. */
    private short numV;

    private short numH;

    private short denomV;

    private short denomH;

    private byte expV;

    private byte expH;

    public final static int BOX_TYPE = 0x72657320;

    public final static int BOX_TYPE_CAPTURE = 0x72657363;

    public final static int BOX_TYPE_DEFAULT_DISPLAY = 0x72657364;

    public final static String NAME = "res ";

    public final static String CAP_NAME = "resc";

    public final static String DEF_NAME = "resd";

    public final static String JP2K_MD_NAME = "JP2KResolutionBox";

    public final static String JP2_MD_CAP_NAME = "JP2KCaptureResolutionBox";

    public final static String JP2_MD_DEF_DISP_NAME = "JP2KDefaultDisplayResolutionBox";

    /** The cached horizontal/vertical resolutions. */
    private float hRes;

    private float vRes;

    private byte[] localData;

    /**
     * Constructs a <code>ResolutionBox</code> from the provided type and
     * content data array.
     */
    public ResolutionBox(int type, byte[] data) {
        super(8 + data.length, type, data);
    }

    public ResolutionBox(byte[] data) {
        this(BOX_TYPE, data);
    }

    /**
     * Constructs a <code>ResolutionBox</code> from the provided type and
     * horizontal/vertical resolutions.
     */
    public ResolutionBox(int type, float hRes, float vRes) {
        super(8 + 18, type, null);
        this.hRes = hRes;
        this.vRes = vRes;
        denomH = denomV = 1;

        expV = 0;
        if (vRes >= 32768) {
            int temp = (int) vRes;
            while (temp >= 32768) {
                expV++;
                temp /= 10;
            }
            numV = (short) (temp & 0xFFFF);
        } else {
            numV = (short) vRes;
        }

        expH = 0;
        if (hRes >= 32768) {
            int temp = (int) hRes;
            while (temp >= 32768) {
                expH++;
                temp /= 10;
            }
            numH = (short) (temp & 0xFFFF);
        } else {
            numH = (short) hRes;
        }
    }

    /**
     * Constructs a <code>ResolutionBox</code> based on the provided
     * <code>org.w3c.dom.Node</code>.
     */
    public ResolutionBox(Node node) throws IIOInvalidTreeException {
        super(node);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();

            if ("VerticalResolutionNumerator".equals(name)) {
                numV = BoxUtilities.getShortElementValue(child);
            }

            if ("VerticalResolutionDenominator".equals(name)) {
                denomV = BoxUtilities.getShortElementValue(child);
            }

            if ("HorizontalResolutionNumerator".equals(name)) {
                numH = BoxUtilities.getShortElementValue(child);
            }

            if ("HorizontalResolutionDenominator".equals(name)) {
                denomH = BoxUtilities.getShortElementValue(child);
            }

            if ("VerticalResolutionExponent".equals(name)) {
                expV = BoxUtilities.getByteElementValue(child);
            }

            if ("HorizontalResolutionExponent".equals(name)) {
                expH = BoxUtilities.getByteElementValue(child);
            }
        }
    }

    /** Return the horizontal resolution. */
    public float getHorizontalResolution() {
        return hRes;
    }

    /** Return the vertical resolution. */
    public float getVerticalResolution() {
        return vRes;
    }

    /** Parse the data elements from the provided content data array. */
    protected void parse(byte[] data) {
        numV = (short) (((data[0] & 0xFF) << 8) | (data[1] & 0xFF));
        denomV = (short) (((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
        numH = (short) (((data[4] & 0xFF) << 8) | (data[5] & 0xFF));
        denomH = (short) (((data[6] & 0xFF) << 8) | (data[7] & 0xFF));
        expV = data[8];
        expH = data[9];
        vRes = (float) ((numV & 0xFFFF) * Math.pow(10, expV) / (denomV & 0xFFFF));
        hRes = (float) ((numH & 0xFFFF) * Math.pow(10, expH) / (denomH & 0xFFFF));
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this resolution box. The
     * format of this node is defined in the XML dtd and xsd for the JP2 image
     * file.
     */
    public IIOMetadataNode getNativeNode() {
       return new ResolutionBoxMetadataNode(this);
    }

    protected synchronized byte[] compose() {
        if (localData != null)
            return localData;
        localData = new byte[10];
        localData[0] = (byte) (numV >> 8);
        localData[1] = (byte) (numV & 0xFF);
        localData[2] = (byte) (denomV >> 8);
        localData[3] = (byte) (denomV & 0xFF);

        localData[4] = (byte) (numH >> 8);
        localData[5] = (byte) (numH & 0xFF);
        localData[6] = (byte) (denomH >> 8);
        localData[7] = (byte) (denomH & 0xFF);

        localData[8] = expV;
        localData[9] = expH;
        return localData;
    }
    
    public short getVerticalResolutionNumerator() {
        return numV;
    }

    public short getHorizontalResolutionNumerator() {
        return numH;
    }

    public short getVerticalResolutionDenominator() {
        return denomV;
    }

    public short getHorizontalResolutionDenominator() {
        return denomH;
    }

    public byte getVerticalResolutionExponent() {
        return expV;
    }

    public byte getHorizontalResolutionExponent() {
        return expH;
    }
}
