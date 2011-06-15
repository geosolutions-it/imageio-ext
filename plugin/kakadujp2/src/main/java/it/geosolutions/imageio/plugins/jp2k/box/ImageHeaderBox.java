/*
 * $RCSfile: HeaderBox.java,v $
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
 * $Date: 2005/02/11 05:01:32 $
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.metadata.IIOMetadataNode;

/**
 * This class is defined to represent an Image Header Box of JPEG JP2 file
 * format. An Image Header Box has a length, and a fixed type of "ihdr". The
 * content of an image header box contains the width/height, number of image
 * components, the bit depth (coded with sign/unsign information), the
 * compression type (7 for JP2 file), the flag to indicate the color space is
 * known or not, and a flag to indicate whether the intellectual property
 * information included in this file.
 */
@SuppressWarnings("serial")
public class ImageHeaderBox extends BaseJP2KBox {

    public static final int BOX_TYPE = 0x69686472;

    public final static String NAME = "ihdr";

    public final static String JP2K_MD_NAME = "JP2KImageHeaderBox";

    public static final int COMPRESSION_TYPE = 7;

    /**
     * Cache the element names for this box's xml definition
     * 
     * @uml.property name="elementNames"
     */
    private static final List<String> elementNames = Collections
            .unmodifiableList(Arrays.asList(new String[] { "Height", "Width",
                    "NumComponents", "BitDepth", "CompressionType",
                    "UnknownColorspace", "IntellectualProperty" }));

    /**
     * This method will be called by the getNativeNodeForSimpleBox of the class
     * Box to get the element names.
     * 
     * @uml.property name="elementNames"
     */
    public static String[] getElementNames() {
        return (String[]) elementNames.toArray();
    }

    /**
     * The element values.
     * 
     * @uml.property name="width"
     */
    private int width;

    /**
     * @uml.property name="height"
     */
    private int height;

    private short numComp;

    /**
     * @uml.property name="bitDepth"
     */
    private byte bitDepth;
    
    private byte bitDepthInterpretation;

    /**
     * @uml.property name="compressionType"
     */
    private byte compressionType;

    private byte unknownColor;

    private byte intelProp;

    private byte[] localData;

    /** Create an Image Header Box from the element values. */
    public ImageHeaderBox(int height, int width, int numComp, int bitDepth,
            int compressionType, int unknownColor, int intelProp) {
        super(22, BOX_TYPE, null);
        this.height = height;
        this.width = width;
        this.numComp = (short) numComp;
        this.bitDepth = (byte) bitDepth;
        this.compressionType = (byte) compressionType;
        this.unknownColor = (byte) unknownColor;
        this.intelProp = (byte) intelProp;
    }

    /** Create an Image Header Box using the content data. */
    public ImageHeaderBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

//    /** Constructs an Image Header Box from a Node. */
//    public ImageHeaderBox(Node node) throws IIOInvalidTreeException {
//        super(node);
//        NodeList children = node.getChildNodes();
//
//        for (int i = 0; i < children.getLength(); i++) {
//            Node child = children.item(i);
//            String name = child.getNodeName();
//
//            if ("Height".equals(name)) {
//                height = BoxUtilities.getIntElementValue(child);
//            }
//
//            if ("Width".equals(name)) {
//                width = BoxUtilities.getIntElementValue(child);
//            }
//
//            if ("NumComponents".equals(name)) {
//                numComp = BoxUtilities.getShortElementValue(child);
//            }
//
//            if ("BitDepth".equals(name)) {
//                bitDepth = BoxUtilities.getByteElementValue(child);
//            }
//
//            if ("CompressionType".equals(name)) {
//                compressionType = BoxUtilities.getByteElementValue(child);
//            }
//
//            if ("UnknownColorspace".equals(name)) {
//                unknownColor = BoxUtilities.getByteElementValue(child);
//            }
//
//            if ("IntellectualProperty".equals(name)) {
//                intelProp = BoxUtilities.getByteElementValue(child);
//            }
//        }
//    }

    /** Parse the data elements from the byte array of the content. */
    protected void parse(byte[] data) {
        height = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16)
                | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        width = ((data[4] & 0xFF) << 24) | ((data[5] & 0xFF) << 16)
                | ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
        numComp = (short) (((data[8] & 0xFF) << 8) | (data[9] & 0xFF));
        bitDepth = data[10];
        bitDepthInterpretation = (byte)(1 + (bitDepth & 0x7F));
        compressionType = data[11];
        if (compressionType != COMPRESSION_TYPE)
            throw new IllegalArgumentException(
                    "Illegal value for compression type.");
        unknownColor = data[12];
        intelProp = data[13];
    }

    /**
     * Returns the height of the image.
     * 
     * @uml.property name="height"
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width of the image.
     * 
     * @uml.property name="width"
     */
    public int getWidth() {
        return width;
    }

    /** Returns the number of image components. */
    public short getNumComponents() {
        return numComp;
    }

    /**
     * Returns the compression type.
     * 
     * @uml.property name="compressionType"
     */
    public byte getCompressionType() {
        return compressionType;
    }

    /**
     * Returns the bit depth for all the image components.
     * 
     * @uml.property name="bitDepth"
     */
    public byte getBitDepth() {
        return bitDepth;
    }

    /** Returns the <code>UnknowColorspace</code> flag. */
    public byte getUnknownColorspace() {
        return unknownColor;
    }

    /** Returns the <code>IntellectualProperty</code> flag. */
    public byte getIntellectualProperty() {
        return intelProp;
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this image header box. The
     * format of this node is defined in the XML dtd and xsd for the JP2 image
     * file.
     */
    public IIOMetadataNode getNativeNode() {
        return new ImageHeaderBoxMetadataNode(this);
    }

    protected synchronized byte[] compose() {
        if (localData != null)
            return localData;
        localData = new byte[14];
        BoxUtilities.copyInt(localData, 0, height);
        BoxUtilities.copyInt(localData, 4, width);

        localData[8] = (byte) (numComp >> 8);
        localData[9] = (byte) (numComp & 0xFF);
        localData[10] = bitDepth;
        localData[11] = compressionType;
        localData[12] = unknownColor;
        localData[13] = intelProp;
        return localData;
    }

    public int getLength() {
        return 22;
    }

    public void setLength(int length) {
        if (length != 22)
            throw new IllegalArgumentException(
                    "length for the ImageHeader JP2 box is fixed at 22");
        super.setLength(length);
    }

    public byte getBitDepthInterpretation() {
        return bitDepthInterpretation;
    }
}
