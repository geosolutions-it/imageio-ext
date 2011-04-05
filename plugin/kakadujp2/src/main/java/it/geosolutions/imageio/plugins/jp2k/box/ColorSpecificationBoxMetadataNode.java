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

import java.awt.color.ICC_Profile;

import javax.imageio.metadata.IIOMetadataNode;

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * This class is defined to represent a Color Specification Box of JPEG JP2 file format. 
 * A Color Specification Box has a length, and a fixed type of "colr". 
 * Its content contains the method to define the color space, the precedence and 
 * approximation accuracy (0 for JP2 files), the enumerated color space, and 
 * the ICC color profile if any.
 */
@SuppressWarnings("serial")
public class ColorSpecificationBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String method;

    private String precedence;

    private String approximationAccuracy;

    private String enumeratedColorSpace;

    private String iccProfile;
    
    private ICC_Profile profile;

    public ColorSpecificationBoxMetadataNode(final ColorSpecificationBox box) {
        super(box);
        final byte m = box.getMethod();
        final byte approx = box.getApproximationAccuracy();
        final byte prec = box.getPrecedence();
        final int enumCS = box.getEnumeratedColorSpace();
        profile = box.getICCProfile();
        
        try {
            IIOMetadataNode child = new IIOMetadataNode("Method");
            child.setUserObject(Byte.valueOf(m));
            method = Byte.toString(m);
            child.setNodeValue(method);
            appendChild(child);
            
            child = new IIOMetadataNode("Precedence");
            child.setUserObject(Byte.valueOf(prec));
            precedence = Byte.toString(prec);
            child.setNodeValue(precedence);
            appendChild(child);
            
            child = new IIOMetadataNode("ApproximationAccuracy");
            child.setUserObject(Byte.valueOf(approx));
            approximationAccuracy = Byte.toString(approx);
            child.setNodeValue(approximationAccuracy);
            appendChild(child);
            
            child = new IIOMetadataNode("EnumeratedColorSpace");
            child.setUserObject(Integer.valueOf(enumCS));
            enumeratedColorSpace = Integer.toString(enumCS);
            child.setNodeValue(enumeratedColorSpace);
            appendChild(child);
           
            child = new IIOMetadataNode("ICCProfile");
            child.setUserObject(profile);
            child.setNodeValue(ImageUtil.convertObjectToString(profile));
            appendChild(child);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPrecedence() {
        return precedence;
    }

    public String getApproximationAccuracy() {
        return approximationAccuracy;
    }

    public String getEnumeratedColorSpace() {
        return enumeratedColorSpace;
    }

    public String getICCProfile() {
        return iccProfile;
    }
    
}
