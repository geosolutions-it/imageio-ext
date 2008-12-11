/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.swan;

import java.util.Arrays;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class SwanImageMetadataFormat extends IIOMetadataFormatImpl {
    private static IIOMetadataFormat instance = null;

    protected SwanImageMetadataFormat() {
        super(SwanImageMetadata.nativeMetadataFormatName,
            IIOMetadataFormatImpl.CHILD_POLICY_ALL);
        /*
         * root
         *   +-- dataset (shortName, longName, unitOfMeasure, noDataValue)
         *   +-- raster (nColumns, nRows, precision)
         *   +-- envelope (xll ,yll, xur, yur, rasterSpace)
         */
        
        // root -> dataset
        addElement("dataset",
            SwanImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("dataset", "shortName", DATATYPE_STRING, true, null);
        addAttribute("dataset", "longName", DATATYPE_STRING, true, null);
        addAttribute("dataset", "unitOfMeasure", DATATYPE_STRING, true, null);
        addAttribute("dataset", "noDataValue", DATATYPE_DOUBLE, true, null);

        // root -> raster
        addElement("raster",
            SwanImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("raster", "nColumns", DATATYPE_INTEGER, true, null);
        addAttribute("raster", "nRows", DATATYPE_INTEGER, true, null);
        addAttribute("raster", "precision", DATATYPE_INTEGER, true, null);

        // root -> envelope 
        addElement("envelope",
                SwanImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("envelope", "xll", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "yll", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "xur", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "yur", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "rasterSpace", DATATYPE_STRING,
                true, null, Arrays.asList(SwanBaseMetadata.rasterSpaceTypes));

    }

    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) {
            instance = new SwanImageMetadataFormat();
        }

        return instance;
    }

    /**
     * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
     *      javax.imageio.ImageTypeSpecifier)
     */
    public boolean canNodeAppear(String elementName,
        ImageTypeSpecifier imageType) {
    	
    	//@todo @task TODO
        return true;
    }
}
