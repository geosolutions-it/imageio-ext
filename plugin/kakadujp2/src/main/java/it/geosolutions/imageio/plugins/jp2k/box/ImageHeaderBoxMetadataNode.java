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
 * This class is defined to represent an Image Header Box of JPEG JP2 file
 * format. An Image Header Box has a length, and a fixed type of "ihdr". The
 * content of an image header box contains the width/height, number of image
 * components, the bit depth (coded with sign/unsign information), the
 * compression type (7 for JP2 file), the flag to indicate the color space is
 * known or not, and a flag to indicate whether the intellectual property
 * information included in this file.
 */
@SuppressWarnings("serial")
public class ImageHeaderBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private String height;

    private String width;

    private String numComponents;

    private String bitDepth;
    
    private String bitDepthInterpretation;

    private String compressionType;

    private String intellectualProperty;

    private String unknownColorspace;

    ImageHeaderBoxMetadataNode(final ImageHeaderBox box) {
        super(box);
        final short nc = box.getNumComponents();
        numComponents = Short.toString(nc);
        final byte bdepth = box.getBitDepth();
        bitDepth = Byte.toString(bdepth);
        final byte bdepthInterp = box.getBitDepthInterpretation();
        bitDepthInterpretation =  Byte.toString(bdepthInterp);
        final byte ct = box.getCompressionType();
        compressionType = Byte.toString(ct);
        final int h = box.getHeight();
        this.height = Integer.toString(h);
        final int w = box.getWidth();
        this.width = Integer.toString(w);
        final byte ip = box.getIntellectualProperty();
        intellectualProperty = Byte.toString(ip);
        final byte ucs = box.getUnknownColorspace();
        this.unknownColorspace = Byte.toString(ucs);

        try {
            IIOMetadataNode child = new IIOMetadataNode("Height");
            child.setUserObject(Integer.valueOf(h));
            child.setNodeValue(height);
            appendChild(child);
            child = new IIOMetadataNode("Width");
            child.setUserObject(Integer.valueOf(w));
            child.setNodeValue(width);
            appendChild(child);
            child = new IIOMetadataNode("NumComponents");
            child.setUserObject(Short.valueOf(nc));
            child.setNodeValue(numComponents);
            appendChild(child);
            child = new IIOMetadataNode("BitDepth");
            child.setUserObject(Byte.valueOf(bdepth));
            child.setNodeValue(bitDepth);
            appendChild(child);
            child = new IIOMetadataNode("BitDepthInterpretation");
            child.setUserObject(Byte.valueOf(bdepthInterp));
            child.setNodeValue(bitDepthInterpretation);
            appendChild(child);
            child = new IIOMetadataNode("CompressionType");
            child.setUserObject(Byte.valueOf(ct));
            child.setNodeValue(compressionType);
            appendChild(child);
            child = new IIOMetadataNode("UnknownColorspace");
            child.setUserObject(Byte.valueOf(ucs));
            child.setNodeValue(unknownColorspace);
            appendChild(child);
            child = new IIOMetadataNode("IntellectualProperty");
            child.setUserObject(Byte.valueOf(ip));
            child.setNodeValue(intellectualProperty);
            appendChild(child);
        } catch (Exception e) {
            throw new IllegalArgumentException("BoxMetadataNode0");
        }
    }

    public String getHeight() {
        return height;
    }

    public String getWidth() {
        return width;
    }

    public String getNumComponents() {
        return numComponents;
    }

    public String getBitDepth() {
        return bitDepth;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public String getIntellectualProperty() {
        return intellectualProperty;
    }

    public String getUnknownColorspace() {
        return unknownColorspace;
    }

    public String getBitDepthInterpretation() {
        return bitDepthInterpretation;
    }
}
