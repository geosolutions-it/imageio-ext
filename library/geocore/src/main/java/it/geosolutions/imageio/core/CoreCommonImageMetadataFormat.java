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
package it.geosolutions.imageio.core;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Class defining the structure of metadata documents describing common image
 * metadata returned from <code>getAsTree</code> method.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class CoreCommonImageMetadataFormat extends IIOMetadataFormatImpl implements IIOMetadataFormat {

    /**
     * A single instance of the <code>CoreCommonImageMetadataFormat</code>
     * class.
     */
    private static CoreCommonImageMetadataFormat commonInstance;

    /**
     * Returns an instance of the <code>CoreCommonImageMetadataFormat</code>
     * class. We build only a single instance and we cache it for future uses.
     * 
     * @return an instance of the <code>CoreCommonImageMetadataFormat</code>
     *         class.
     */
    public static synchronized IIOMetadataFormat getInstance() {
        if (commonInstance == null) {
            commonInstance = new CoreCommonImageMetadataFormat();
        }
        return commonInstance;
    }

    /**
     * Constructs a <code>CoreCommonImageMetadataFormat</code> instance.
     */
    private CoreCommonImageMetadataFormat() {
        super(CoreCommonImageMetadata.nativeMetadataFormatName,CHILD_POLICY_SOME);
        
        // //
        //
        // root -> DatasetDescriptor
        //
        // //
        addElement("DatasetDescriptor",CoreCommonImageMetadata.nativeMetadataFormatName,CHILD_POLICY_EMPTY);
        addAttribute("DatasetDescriptor", "name", DATATYPE_STRING, true, null);
        addAttribute("DatasetDescriptor", "description", DATATYPE_STRING, true,null);
        addAttribute("DatasetDescriptor", "driverDescription", DATATYPE_STRING,true, null);
        addAttribute("DatasetDescriptor", "driverName", DATATYPE_STRING, true,null);
        addAttribute("DatasetDescriptor", "projection", DATATYPE_STRING, true,"");
        addAttribute("DatasetDescriptor", "numGCPs", DATATYPE_INTEGER, true,"0", "0", null, true, false);
        addAttribute("DatasetDescriptor", "gcpProjection", DATATYPE_STRING,true, "");

        // //
        //
        // root -> RasterDimensions
        //
        // //
        addElement("RasterDimensions",CoreCommonImageMetadata.nativeMetadataFormatName,CHILD_POLICY_EMPTY);
        addAttribute("RasterDimensions", "width", DATATYPE_INTEGER, true, null,"0", null, false, false);
        addAttribute("RasterDimensions", "height", DATATYPE_INTEGER, true,null, "0", null, false, false);
        addAttribute("RasterDimensions", "tileWidth", DATATYPE_INTEGER, true,null, "1", null, true, false);
        addAttribute("RasterDimensions", "tileHeight", DATATYPE_INTEGER, true,null, "1", null, true, false);
        addAttribute("RasterDimensions", "numBands", DATATYPE_INTEGER, true,"1", "1", null, true, false);

        // //
        //
        // root -> GeoTransform
        //
        // //
        addElement("GeoTransform",CoreCommonImageMetadata.nativeMetadataFormatName,CHILD_POLICY_EMPTY);
        addAttribute("GeoTransform", "m0", DATATYPE_DOUBLE, false, null);
        addAttribute("GeoTransform", "m1", DATATYPE_DOUBLE, false, null);
        addAttribute("GeoTransform", "m2", DATATYPE_DOUBLE, false, null);
        addAttribute("GeoTransform", "m3", DATATYPE_DOUBLE, false, null);
        addAttribute("GeoTransform", "m4", DATATYPE_DOUBLE, false, null);
        addAttribute("GeoTransform", "m5", DATATYPE_DOUBLE, false, null);

        // //
        //
        // root -> GCPS
        //
        // //
        addElement("GCPS", CoreCommonImageMetadata.nativeMetadataFormatName,CHILD_POLICY_REPEAT);
        addElement("GCP", "GCPS", CHILD_POLICY_EMPTY);
        addAttribute("GCP", "x", DATATYPE_INTEGER, true, null, "0", null, true,false);
        addAttribute("GCP", "y", DATATYPE_INTEGER, true, null, "0", null, true,false);
        addAttribute("GCP", "id", DATATYPE_STRING, true, "");
        addAttribute("GCP", "info", DATATYPE_STRING, false, "");
        addAttribute("GCP", "lon", DATATYPE_DOUBLE, true, null);
        addAttribute("GCP", "lat", DATATYPE_DOUBLE, true, null);
        addAttribute("GCP", "elevation", DATATYPE_DOUBLE, true, null);

        addElement("BandsInfo",CoreCommonImageMetadata.nativeMetadataFormatName,CHILD_POLICY_REPEAT);
        // //
        //
        // root -> BandsInfo -> BandInfo
        //
        // //       
        addElement("BandInfo", "BandsInfo", CHILD_POLICY_SOME);
        addAttribute("BandInfo", "index", DATATYPE_INTEGER, true, null, null,null, false, false);
        addAttribute("BandInfo", "colorInterpretation", DATATYPE_INTEGER, true,null, null, null, false, false);
        addAttribute("BandInfo", "noData", DATATYPE_DOUBLE, false, Double.toString(Double.NaN), null, null, false, false);
        addAttribute("BandInfo", "scale", DATATYPE_DOUBLE, false, "1.0", "0.0",null, false, false);
        addAttribute("BandInfo", "offset", DATATYPE_DOUBLE, false, "0.0", null,null, false, false);
        addAttribute("BandInfo", "minimum", DATATYPE_DOUBLE, false, null, null,null, false, false);
        addAttribute("BandInfo", "maximum", DATATYPE_DOUBLE, false, null, null,null, false, false);
        addAttribute("BandInfo", "numOverviews", DATATYPE_INTEGER, false, null,null, null, false, false);
        addAttribute("BandInfo", "unit", DATATYPE_STRING, false, "");

        // //
        //
        // root -> BandsInfo ->BandInfo -> ColorTable
        //
        // //
        addElement("ColorTable", "BandInfo", 0, 256);
        addAttribute("ColorTable", "sizeOfLocalColorTable", DATATYPE_INTEGER,true, null);
        // //
        //
        // root -> BandsInfo -> BandInfo -> ColorTable -> ColorTableEntry
        //
        // //
        addElement("ColorTableEntry", "ColorTable", CHILD_POLICY_EMPTY);
        addAttribute("ColorTableEntry", "index", DATATYPE_INTEGER, true, null,"0", "255", true, true);
        addAttribute("ColorTableEntry", "red", DATATYPE_INTEGER, true, null, "0", "255", true, true);
        addAttribute("ColorTableEntry", "green", DATATYPE_INTEGER, true, null, "0", "255", true, true);
        addAttribute("ColorTableEntry", "blue", DATATYPE_INTEGER, true, null,"0", "255", true, true);
        addAttribute("ColorTableEntry", "alpha", DATATYPE_INTEGER, true, null,"0", "255", true, true);
    }

    /**
     * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
     *      javax.imageio.ImageTypeSpecifier)
     * TODO we should implement this  
     */
    public boolean canNodeAppear(String elementName,ImageTypeSpecifier imageType) {
    	throw new UnsupportedOperationException("Implement me");
    }
}
