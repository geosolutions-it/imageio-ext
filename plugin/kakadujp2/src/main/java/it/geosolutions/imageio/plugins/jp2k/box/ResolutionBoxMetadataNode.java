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

import javax.imageio.metadata.IIOMetadataNode;

/**
 * This class is defined to represent a Resolution Box of JPEG JP2 file format.
 * 
 * Its contents includes the resolution numerators, denominator, and the
 * exponents for both horizontal and vertical directions.
 */
public class ResolutionBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String verticalResolutionNumerator;

    private String horizontalResolutionNumerator;

    private String verticalResolutionDenominator;

    private String horizontalResolutionDenominator;

    private String verticalResolutionExponent;

    private String horizontalResolutionExponent;

    private String horizontalResolution;

    private String verticalResolution;

    ResolutionBoxMetadataNode(final ResolutionBox box) {
        super(box);
        final float vRes = box.getVerticalResolution();
        final short vDen = box.getVerticalResolutionDenominator();
        final short vNum = box.getVerticalResolutionNumerator();
        final byte vExp = box.getVerticalResolutionExponent();

        final float hRes = box.getHorizontalResolution();
        final short hDen = box.getHorizontalResolutionDenominator();
        final short hNum = box.getHorizontalResolutionNumerator();
        final byte hExp = box.getHorizontalResolutionExponent();

        IIOMetadataNode child = new IIOMetadataNode(
                "VerticalResolutionNumerator");
        child.setUserObject(new Short(vNum));
        verticalResolutionNumerator = Short.toString(vNum);
        child.setNodeValue(verticalResolutionNumerator);
        appendChild(child);

        child = new IIOMetadataNode("VerticalResolutionDenominator");
        child.setUserObject(new Short(vDen));
        verticalResolutionDenominator = Short.toString(vDen);
        child.setNodeValue(verticalResolutionDenominator);
        appendChild(child);

        child = new IIOMetadataNode("HorizontalResolutionNumerator");
        child.setUserObject(new Short(hNum));
        horizontalResolutionNumerator = Short.toString(hNum);
        child.setNodeValue(horizontalResolutionNumerator);
        appendChild(child);

        child = new IIOMetadataNode("HorizontalResolutionDenominator");
        child.setUserObject(new Short(hDen));
        horizontalResolutionDenominator = Short.toString(hDen);
        child.setNodeValue(horizontalResolutionDenominator);
        appendChild(child);

        child = new IIOMetadataNode("VerticalResolutionExponent");
        child.setUserObject(new Byte(vExp));
        verticalResolutionExponent = Byte.toString(vExp);
        child.setNodeValue(verticalResolutionExponent);
        appendChild(child);

        child = new IIOMetadataNode("HorizontalResolutionExponent");
        child.setUserObject(new Byte(hExp));
        horizontalResolutionExponent = Byte.toString(hExp);
        child.setNodeValue(horizontalResolutionExponent);
        appendChild(child);

        child = new IIOMetadataNode("VerticalResolution");
        child.setUserObject(new Float(vRes));
        verticalResolution = Float.toString(vRes);
        child.setNodeValue(verticalResolution);
        appendChild(child);

        child = new IIOMetadataNode("HorizontalResolution");
        child.setUserObject(new Float(hRes));
        horizontalResolution = Float.toString(hRes);
        child.setNodeValue(horizontalResolution);
        appendChild(child);
    }

    public String getVerticalResolutionNumerator() {
        return verticalResolutionNumerator;
    }

    public String getHorizontalResolutionNumerator() {
        return horizontalResolutionNumerator;
    }

    public String getVerticalResolutionDenominator() {
        return verticalResolutionDenominator;
    }

    public String getHorizontalResolutionDenominator() {
        return horizontalResolutionDenominator;
    }

    public String getVerticalResolutionExponent() {
        return verticalResolutionExponent;
    }

    public String getHorizontalResolutionExponent() {
        return horizontalResolutionExponent;
    }

    public String getHorizontalResolution() {
        return horizontalResolution;
    }

    public String getVerticalResolution() {
        return verticalResolution;
    }

}
