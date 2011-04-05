/*
 * $RCSfile: SignatureBox.java,v $
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


import org.w3c.dom.Node;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;
/**
 * This class is defined to represent a Signature Box of JPEG JP2 file format.
 * This type of box has a fixed length of 12, a type of "jP " and a four byte
 * content of 0x0D0A870A, which is used to detects of the common file
 * transmission errors which substitutes <CR><LF> with <LF> or vice versa.
 * 
 * <p>
 * <strong>It must be the first box in a JP2 file.</strong>
 * </p>
 */
@SuppressWarnings("serial")
public class SignatureBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x6A502020;

    public final static String NAME = "jP  ";

    public final static String JP2K_MD_NAME = "JP2KSignatureBox";

    public static final byte []LOCAL_DATA=new byte[] { (byte) 0x0D, (byte) 0x0A, (byte) 0x87, (byte) 0x0A };
    
    public static final String SIGNATURE = "0x0D0A870A";
    
    /** Constructs a <code>SignatureBox</code>. */
    public SignatureBox() {
        super(12, BOX_TYPE, null);
    }

    /**
     * Constructs a <code>SignatureBox</code> based on the provided
     * <code>org.w3c.dom.Node</code>.
     */
    public SignatureBox(Node node) throws IIOInvalidTreeException {
        super(node);
    }

    /**
     * Constructs a <code>SignatureBox</code> based on the provided byte
     * array.
     */
    public SignatureBox(byte[] data) throws IIOInvalidTreeException {
        super(12, BOX_TYPE, new byte[]{data[0],data[1],data[2],data[3]});
    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this signature box. The
     * format of this node is defined in the XML dtd and xsd for the JP2 image
     * file.
     */
    public IIOMetadataNode getNativeNode() {
       return new SignatureBoxMetadataNode(this);
    }

    protected byte[] compose() {
       return LOCAL_DATA.clone();
    }

    /**
     * Checks contents of the signature box.
     */
    protected void parse(byte[] data) {
        if (data == null)
            throw new IllegalArgumentException();
        if (data.length != 4)
            throw new IllegalArgumentException();
        if (data[0] != (byte) 0x0D)
            throw new IllegalArgumentException();
        if (data[1] != (byte) 0x0A)
            throw new IllegalArgumentException();
        if (data[2] != (byte) 0x87)
            throw new IllegalArgumentException();
        if (data[3] != (byte) 0x0A)
            throw new IllegalArgumentException();
    }
}
