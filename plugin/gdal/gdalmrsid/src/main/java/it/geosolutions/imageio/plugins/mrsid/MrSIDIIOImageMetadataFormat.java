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
package it.geosolutions.imageio.plugins.mrsid;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Class defining the structure of metadata documents describing MrSID specific
 * image metadata returned from <code>getAsTree</code> method.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class MrSIDIIOImageMetadataFormat extends IIOMetadataFormatImpl
        implements IIOMetadataFormat {

    /**
     * A single instance of the <code>MrSIDIIOImageMetadataFormat</code>
     * class.
     */
    protected static MrSIDIIOImageMetadataFormat mrsidMetadataInstance;

    /**
     * Returns an instance of the <code>MrSIDIIOImageMetadataFormat</code>
     * class. We build only a single instance and we cache it for future uses.
     * 
     * @return an instance of the <code>MrSIDIIOImageMetadataFormat</code>
     *         class.
     */
    public static synchronized IIOMetadataFormat getInstance() {
        if (mrsidMetadataInstance == null) {
            mrsidMetadataInstance = new MrSIDIIOImageMetadataFormat();
        }
        return mrsidMetadataInstance;
    }

    /**
     * Constructs a <code>MrSIDIIOImageMetadataFormat</code> instance.
     */
    protected MrSIDIIOImageMetadataFormat() {
        super(MrSIDIIOImageMetadata.mrsidImageMetadataName, CHILD_POLICY_SOME);

        // //
        //
        // root -> ImageDescriptor
        //
        // //
        addElement("ImageDescriptor",
                MrSIDIIOImageMetadata.mrsidImageMetadataName,
                CHILD_POLICY_EMPTY);
        addAttribute("ImageDescriptor", "IMAGE__INPUT_NAME", DATATYPE_STRING,
                true, null);
        addAttribute("ImageDescriptor", "IMAGE__INPUT_FILE_SIZE",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__DYNAMIC_RANGE_WINDOW",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__DYNAMIC_RANGE_LEVEL",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_VERSION",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__TARGET_COMPRESSION_RATIO",
                DATATYPE_INTEGER, true, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_NLEV",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_WEIGHT",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_GAMMA",
                DATATYPE_DOUBLE, false, null);
        addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_BLOCK_SIZE",
                DATATYPE_INTEGER, true, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__CREATION_DATE",
                DATATYPE_STRING, false, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__WIDTH", DATATYPE_INTEGER, true,
                null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__HEIGHT", DATATYPE_INTEGER,
                true, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__TRANSPARENT_DATA_VALUE",
                DATATYPE_STRING, false, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__COLOR_SCHEME",
                DATATYPE_INTEGER, true, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__DATA_TYPE", DATATYPE_INTEGER,
                true, null, null, null, false, false);
        addAttribute("ImageDescriptor", "IMAGE__BITS_PER_SAMPLE",
                DATATYPE_INTEGER, true, null, null, null, false, false);
        // //
        //
        // root -> Georeferencing
        //
        // //
        addElement("Georeferencing",
                MrSIDIIOImageMetadata.mrsidImageMetadataName,
                CHILD_POLICY_EMPTY);
        addAttribute("Georeferencing", "IMG__HORIZONTAL_UNITS",
                DATATYPE_STRING, true, null);
        addAttribute("Georeferencing", "IMG__PROJECTION_TYPE",
                DATATYPE_STRING, true, null);
        addAttribute("Georeferencing", "IMG__PROJECTION_NUMBER",
                DATATYPE_INTEGER, true, null, null, null, false, false);
        addAttribute("Georeferencing", "IMG__PROJECTION_ZONE",
                DATATYPE_INTEGER, true, null, null, null, false, false);
        addAttribute("Georeferencing", "IMG__SPHEROID_NAME", DATATYPE_STRING,
                true, null);
        addAttribute("Georeferencing", "IMG__SPHEROID_SEMI_MAJOR_AXIS",
                DATATYPE_DOUBLE, false, null);
        addAttribute("Georeferencing", "IMG__SPHEROID_SEMI_MINOR_AXIS",
                DATATYPE_DOUBLE, false, null);
        addAttribute("Georeferencing", "IMG__SPHEROID_ECCENTRICITY_SQUARED",
                DATATYPE_DOUBLE, false, null);
        addAttribute("Georeferencing", "IMG__SPHEROID_RADIUS",
                DATATYPE_DOUBLE, false, null);
        addAttribute("Georeferencing", "IMG__PROJECTION_PARAMETERS",
                DATATYPE_STRING, true, null);
        addAttribute("Georeferencing", "IMAGE__XY_ORIGIN", DATATYPE_STRING,
                true, null);
        addAttribute("Georeferencing", "IMAGE__X_RESOLUTION", DATATYPE_DOUBLE,
                false, null);
        addAttribute("Georeferencing", "IMAGE__Y_RESOLUTION", DATATYPE_DOUBLE,
                false, null);
        addAttribute("Georeferencing", "IMAGE__WKT", DATATYPE_STRING, true,
                null);
    }

    /**
     * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
     *      javax.imageio.ImageTypeSpecifier)
     */
    public boolean canNodeAppear(String elementName,
            ImageTypeSpecifier imageType) {
        return false;
    }

}
