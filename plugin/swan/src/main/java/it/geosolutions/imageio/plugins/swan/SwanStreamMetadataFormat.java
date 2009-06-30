/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.swan;

import java.util.Arrays;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class SwanStreamMetadataFormat extends IIOMetadataFormatImpl {
    private static IIOMetadataFormat instance = null;

    protected SwanStreamMetadataFormat() {
        super(SwanStreamMetadata.nativeMetadataFormatName,
            IIOMetadataFormatImpl.CHILD_POLICY_ALL);
        
        /*
         * root
         *   +-- general (datasetNumber, tauNumber, baseTime, zone)
         *   |     +-- envelope (xll, yll, xur, yur, rasterSpace)
         *   |     +-- datasetNames 
         *   |     |     +-- datasetName[0] (name)
         *   |     |     +-- datasetName[1] (name)
         *   |     |     +-- ...etc...
         *   |     +-- tau (time, unitOfMeasure)
         *   +-- raster (nColumns, nRows, precision)  
         */
  
        // root -> general
        addElement("general",
            SwanStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_SEQUENCE);
        addAttribute("general", "datasetNumber", DATATYPE_INTEGER, true, null);
        addAttribute("general", "tauNumber", DATATYPE_INTEGER, true, null);
        addAttribute("general", "baseTime", DATATYPE_STRING, true, null);
        addAttribute("general", "zone", DATATYPE_STRING, true, null);

        // general -> envelope 
        addElement("envelope","general",CHILD_POLICY_EMPTY);
        addAttribute("envelope", "xll", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "yll", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "xur", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "yur", DATATYPE_DOUBLE, true, null);
        addAttribute("envelope", "rasterSpace", DATATYPE_STRING,
                true, null, Arrays.asList(SwanStreamMetadata.rasterSpaceTypes));

        // general -> datasetNames
        addElement("datasetNames" , "general", CHILD_POLICY_REPEAT);
        
        // datasetNames -> datasetName
        addElement("datasetName" , "datasetNames", CHILD_POLICY_EMPTY);
        addAttribute("datasetName", "name", DATATYPE_STRING, true, null);

        
        // general -> tau 
        addElement("tau","general",CHILD_POLICY_EMPTY);
        addAttribute("tau", "time", DATATYPE_INTEGER, true, null);
        addAttribute("tau", "unitOfMeasure", DATATYPE_STRING, true, null);
               
        // root -> raster
        addElement("raster",
            SwanStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("raster", "nColumns", DATATYPE_INTEGER, true, null);
        addAttribute("raster", "nRows", DATATYPE_INTEGER, true, null);
        addAttribute("raster", "precision", DATATYPE_INTEGER, true, null);
        }

    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) 
            instance = new SwanStreamMetadataFormat();
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
