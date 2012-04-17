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


/**
 * This class is defined to create the contiguous codestream box of JP2 file
 * format.
 * 
 * <p>
 * NOTE: this box can be used to extract the entire codestram from within a jp2k file, 
 * however I would not expect to expose this inside metadata :-)
 */
@SuppressWarnings("serial")
public class ContiguousCodestreamBox extends DefaultJP2KBox {

    /**
     * Cache the element names for this box's xml definition
     * 
     * @uml.property name="elementNames"
     */
    private static String[] elementNames = { "Content" };

    /**
     * This method will be called by the getNativeNodeForSimpleBox of the class
     * Box to get the element names.
     * 
     * @uml.property name="elementNames"
     */
    public static String[] getElementNames() {
        return elementNames;
    }
    
    public ContiguousCodestreamBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    public final static int BOX_TYPE = 0x6a703263;

    public final static String NAME = "jp2c";

    public final static String JP2K_MD_NAME = "JPEG2000ContiguousCodestreamBox";

}
