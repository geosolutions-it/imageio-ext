/*
 * $RCSfile: FileTypeBox.java,v $
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.metadata.IIOMetadataNode;

/**
 * This class is defined to represent a File Type Box of JPEG JP2 file format. 
 * A File Type Box has a length, and a fixed type of "ftyp". 
 * The content of a file type box contains the brand ("jp2 " for JP2 file", 
 * the minor version (0 for JP2 file format), and a compatibility list 
 * (one of which should be "jp2 " if brand is not "jp2 ".)
 */
@SuppressWarnings("serial")
public class FileTypeBox extends BaseJP2KBox {
    /**
     * Define possible compatibilites for JPEG2000 files
     * 
     * @author Simone Giannecchini, GeoSolutions
     * 
     */
    public static enum JPEG2000FileType {
        JP2, JPX, JPXB, UNSPECIFIED;

        public static JPEG2000FileType valueOf(final int compatibility) {
            switch (compatibility) {
            case TYPE_JP2:
                return JPEG2000FileType.JP2;
            case TYPE_JPX:
                return JPEG2000FileType.JPX;
            case TYPE_JPXB:
                return JPEG2000FileType.JPXB;
            default:
                return UNSPECIFIED;
            }
        }

        public int toInt() {
            switch (this) {
            case JP2:
                return TYPE_JP2;
            case JPX:
                return TYPE_JPX;
            case JPXB:
                return TYPE_JPXB;
            default:
                return -1;
            }
        }
    }

    private final static int TYPE_JP2 = 0x6a703220;
    
    private final static int TYPE_JPX = 0x6a707820;
    
    private final static int TYPE_JPXB = 0x6a707862;
    
    public final static int BOX_TYPE = 0x66747970;

    public final static String NAME = "ftyp";

    public final static String JP2K_MD_NAME = "JP2KFileTypeBox";

    /**
     * Cache the element names for this box's xml definition
     * 
     * @uml.property name="elementNames"
     */
    private static final List<String> elementNames = Collections
            .unmodifiableList(Arrays.asList(new String[] { "Brand",
                    "MinorVersion", "CompatibilitySet" }));

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
     * @uml.property name="brand"
     */
    private JPEG2000FileType brand;

    /**
     * @uml.property name="minorVersion"
     */
    private int minorVersion;

    private Set<JPEG2000FileType> compatibility;

    private byte[] localData;

    /**
     * Constructs a <code>FileTypeBox</code> from the provided brand, minor
     * version and compatibility list.
     */
    public FileTypeBox(final int br, final int minorVersion, final int[] comp) {
        super(16 + (comp == null ? 0 : (comp.length << 2)), BOX_TYPE, null);
        this.brand = JPEG2000FileType.valueOf(br);
        this.minorVersion = minorVersion;
        for (int c : comp)
            this.compatibility.add(JPEG2000FileType.valueOf(c));
    }

    /**
     * Constructs a <code>FileTypeBox</code> from the provided byte array.
     */
    public FileTypeBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

//    /**
//     * Constructs a <code>FileTypeBox</code> from
//     * <code>org.w3c.dom.Node</code>.
//     */
//    public FileTypeBox(Node node) throws IIOInvalidTreeException {
//        super(node);
//        NodeList children = node.getChildNodes();
//
//        for (int i = 0; i < children.getLength(); i++) {
//            Node child = children.item(i);
//            String name = child.getNodeName();
//
//            if ("Brand".equals(name)) {
//                brand = JPEG2000FileType.valueOf(BoxUtilities.getIntElementValue(child));
//            }
//
//            if ("MinorVersion".equals(name)) {
//                minorVersion = BoxUtilities.getIntElementValue(child);
//            }
//
//            if ("CompatibilityList".equals(name)) {
//                final int[] compatibilities = BoxUtilities.getIntArrayElementValue(child);
//                for(int c:compatibilities)
//                	this.compatibility.add(JPEG2000FileType.valueOf(c));
//            }
//        }
//    }

    /**
     * Returns the brand of this file type box.
     * 
     * @uml.property name="brand"
     */
    public JPEG2000FileType getBrand() {
        return brand;
    }

    /**
     * Returns the minor version of this file type box.
     * 
     * @uml.property name="minorVersion"
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /** Returns the compatibility list of this file type box. */
    public Set<JPEG2000FileType> getCompatibilitySet() {
        return new HashSet<JPEG2000FileType>(compatibility);
    }
    
    public boolean isCompatibleWith(final JPEG2000FileType c){
    	return this.compatibility.contains(c);
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this file type box. The
     * format of this node is defined in the XML dtd and xsd for the JP2 image
     * file.
     */
    public IIOMetadataNode getNativeNode() {
        return new FileTypeBoxMetadataNode(this);
    }

    protected synchronized void parse(byte[] data) {
        if (data == null)
            return;
        compatibility = new HashSet<JPEG2000FileType>();
        brand = JPEG2000FileType.valueOf(((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16)
                | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF));

        minorVersion = ((data[4] & 0xFF) << 24) | ((data[5] & 0xFF) << 16)
                | ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);

        int len = (data.length - 8) / 4;
        if (len > 0) {
            for (int i = 0, j = 8; i < len; i++, j += 4)
                compatibility.add(JPEG2000FileType.valueOf(((data[j] & 0xFF) << 24)
                        | ((data[j + 1] & 0xFF) << 16)
                        | ((data[j + 2] & 0xFF) << 8) | (data[j + 3] & 0xFF)));
        }
    }

    protected byte[] compose() {
    	assert Thread.holdsLock(this);
        if (localData != null)
            return localData;

        localData = new byte[8 + (compatibility != null ? (compatibility.size() << 2) : 0)];

        BoxUtilities.copyInt(localData, 0, brand.toInt());
        BoxUtilities.copyInt(localData, 4, minorVersion);
        int i = 0;
        for (JPEG2000FileType c : compatibility)
            localData[i++] = (byte) (c.toInt() & 0xff);

        return localData;
    }
}
