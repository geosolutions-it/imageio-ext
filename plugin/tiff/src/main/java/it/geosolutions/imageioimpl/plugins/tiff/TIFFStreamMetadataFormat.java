/*
 * $RCSfile: TIFFStreamMetadataFormat.java,v $
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
 * $Date: 2005/02/11 05:01:50 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2015, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;

/**
 * {@link TIFFMetadataFormat} subclass used for defining {@link TIFFStreamMetadata} structure
 */
public class TIFFStreamMetadataFormat extends TIFFMetadataFormat {

    /** String value indicating "value" attribute for all the nodes*/
    private static final String VALUE = "/value";

    private static TIFFStreamMetadataFormat theInstance = null;

    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {
        return false;
    }

    private TIFFStreamMetadataFormat() {
        this.resourceBaseName =
    "it.geosolutions.imageioimpl.plugins.tiff.TIFFStreamMetadataFormatResources";
        this.rootName = TIFFStreamMetadata.nativeMetadataFormatName;

        TIFFElementInfo einfo;
        TIFFAttrInfo ainfo;
        String[] empty = new String[0];
        String[] childNames;
        String[] attrNames;

        childNames = new String[] { TIFFStreamMetadata.BYTE_ORDER,
                TIFFStreamMetadata.NUM_INTERNAL_MASKS, TIFFStreamMetadata.NUM_EXTERNAL_MASKS,
                TIFFStreamMetadata.NUM_INTERNAL_OVERVIEWS,
                TIFFStreamMetadata.NUM_EXTERNAL_OVERVIEWS,
                TIFFStreamMetadata.NUM_EXTERNAL_MASK_OVERVIEWS,
                TIFFStreamMetadata.EXTERNAL_MASK_FILE, TIFFStreamMetadata.EXTERNAL_OVERVIEW_FILE,
                TIFFStreamMetadata.EXTERNAL_MASK_OVERVIEW_FILE };
        einfo = new TIFFElementInfo(childNames, empty, CHILD_POLICY_ALL);

        elementInfoMap.put(TIFFStreamMetadata.nativeMetadataFormatName,
                           einfo);

        childNames = empty;
        // Defininf the various nodes
        attrNames = new String[] { TIFFStreamMetadata.BYTE_ORDER };
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put("ByteOrder", einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.NUM_INTERNAL_MASKS, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.NUM_EXTERNAL_MASKS, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.NUM_INTERNAL_OVERVIEWS, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.NUM_EXTERNAL_OVERVIEWS, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.NUM_EXTERNAL_MASK_OVERVIEWS, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.EXTERNAL_MASK_FILE, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.EXTERNAL_OVERVIEW_FILE, einfo);
        einfo = new TIFFElementInfo(childNames, attrNames, CHILD_POLICY_EMPTY);
        elementInfoMap.put(TIFFStreamMetadata.EXTERNAL_MASK_OVERVIEW_FILE, einfo);

        // Defining Node Attributes
        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_STRING;
        ainfo.isRequired = true;
        attrInfoMap.put(TIFFStreamMetadata.BYTE_ORDER + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_INTEGER;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.NUM_INTERNAL_MASKS + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_INTEGER;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.NUM_EXTERNAL_MASKS + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_INTEGER;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.NUM_INTERNAL_OVERVIEWS + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_INTEGER;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.NUM_EXTERNAL_OVERVIEWS + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_INTEGER;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.NUM_EXTERNAL_MASK_OVERVIEWS + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_STRING;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.EXTERNAL_MASK_FILE + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_STRING;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.EXTERNAL_OVERVIEW_FILE + VALUE, ainfo);

        ainfo = new TIFFAttrInfo();
        ainfo.dataType = DATATYPE_STRING;
        ainfo.isRequired = false;
        attrInfoMap.put(TIFFStreamMetadata.EXTERNAL_MASK_OVERVIEW_FILE + VALUE, ainfo);
    }

    public static synchronized IIOMetadataFormat getInstance() {
        if (theInstance == null) {
            theInstance = new TIFFStreamMetadataFormat();
        }
        return theInstance;
    }
}
