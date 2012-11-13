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


/**
 * This class is defined to represent a Capture Resolution Box of JPEG JP2 file format.
 * A Resolution Box has a length, and a fixed type of "resc" (capture
 * resolution) 
 * 
 */
@SuppressWarnings("serial")
public class CaptureResolutionBox extends ResolutionBox {

    public final static int BOX_TYPE_CAPTURE = 0x72657363;

    public final static String CAP_NAME = "resc";

    public final static String JP2_MD_CAP_NAME = "JP2KCaptureResolutionBox";

    /**
     * Constructs a <code>ResolutionBox</code> from the provided type and
     * content data array.
     */
    public CaptureResolutionBox(byte[] data) {
        super(BOX_TYPE_CAPTURE, data);
    }
}
