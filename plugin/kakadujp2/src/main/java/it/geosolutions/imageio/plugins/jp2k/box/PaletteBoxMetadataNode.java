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
 *    (C) 2007 - 2009, GeoSolutions
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
 * This class is designed to represent a palette box for JPEG 2000 JP2 file
 * format. A palette box has a length, and a fixed type of "pclr". Its content
 * contains the number of palette entry, the number of color components, the bit
 * depths of the output components, the LUT. Currently, only 8-bit color index
 * is supported.
 */
@SuppressWarnings("serial")
public class PaletteBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private int numC;

    private int numE;

    private String numEntries;

    private String numComps;

    private String[] bitDepth;

    private byte[][] lut;

    public PaletteBoxMetadataNode(final PaletteBox box){
        super(box);
        final byte[] bd = box.getBitDepths();
        final byte[][] lookupt = box.getLUT();
        numC = box.getNumComp();
        numE = box.getNumEntries();

        bitDepth = new String[numC];
        lut = lookupt.clone();

        IIOMetadataNode child = new IIOMetadataNode("NumberEntries");
        child.setUserObject(new Integer(numE));
        numEntries = Integer.toString(numE);
        child.setNodeValue(numEntries);
        appendChild(child);

        child = new IIOMetadataNode("NumberColors");
        child.setUserObject(new Integer(numC));
        numComps = Integer.toString(numC);
        child.setNodeValue(numComps);
        appendChild(child);

        child = new IIOMetadataNode("BitDepth");
        child.setUserObject(bd.clone());
        child.setNodeValue(ImageUtil.convertObjectToString(bd));
        appendChild(child);

        child = new IIOMetadataNode("LUT");
        for (int i = 0; i < numE; i++) {
            IIOMetadataNode child1 = new IIOMetadataNode("LUTRow");
            byte[] row = new byte[numC];
            for (int j = 0; j < numC; j++)
                row[j] = lut[j][i];

            child1.setUserObject(row);
            child1.setNodeValue(ImageUtil.convertObjectToString(row));
            child.appendChild(child1);
        }
        appendChild(child);
    }

    public String getNumEntries() {
        return numEntries;
    }

    /** Return the number of color components. */
    public String getNumComp() {
        return numComps;
    }

    /** Return the bit depths for all the color components. */
    public String getBitDepths(final int index) {
        if (index > numC - 1)
            throw new IllegalArgumentException("Number of BitDepth is " + numC);
        return new String(bitDepth[index]);
    }

    /** Return the Raw LUT. */
    public byte[][] getRawLUT() {
        return lut.clone();
    }
}
