/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
 * This class is defined to represent a Default Display Resolution Box 
 * of JPEG JP2 file format. A Resolution Box has a length, and a fixed type of 
 * "resd" (default display resolution).
 */
@SuppressWarnings("serial")
public class DefaultDisplayResolutionBox extends ResolutionBox {

    public final static int BOX_TYPE_DEFAULT_DISPLAY = 0x72657364;

    public final static String DEF_NAME = "resd";

    public final static String JP2_MD_DEF_DISP_NAME = "JP2KDefaultDisplayResolutionBox";

    /**
     * Constructs a <code>ResolutionBox</code> from the provided type and
     * content data array.
     */
    public DefaultDisplayResolutionBox(byte[] data) {
        super(BOX_TYPE_DEFAULT_DISPLAY, data);
    }
}
