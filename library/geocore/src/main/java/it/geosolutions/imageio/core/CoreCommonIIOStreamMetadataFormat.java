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
 * Class defining the structure of metadata documents describing common stream
 * metadata returned from <code>getAsTree</code> method.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class CoreCommonIIOStreamMetadataFormat extends IIOMetadataFormatImpl {

    /**
     * A single instance of the <code>GDALCommonIIOStreamMetadataFormat</code>
     * class.
     */
    private static CoreCommonIIOStreamMetadataFormat theInstance = null;

    /**
     * Constructs a <code>GDALCommonIIOStreamMetadataFormat</code> instance.
     */
    private CoreCommonIIOStreamMetadataFormat() {
        super(CoreCommonIIOStreamMetadata.nativeMetadataFormatName,CHILD_POLICY_SOME);
        
        // //
        //
        // root -> DataSets
        //
        // //
        addElement("DataSets", CoreCommonIIOStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_REPEAT);
        addAttribute("DataSets", "number", DATATYPE_STRING, true, null);

        // //
        //
        // DataSets -> DataSet
        //
        // //
        addElement("DataSet", "DataSets", CHILD_POLICY_EMPTY);
        addAttribute("DataSet", "name", DATATYPE_STRING, true, null);
    }

    /**
     * Returns an instance of the <code>CoreCommonIIOStreamMetadataFormat</code>
     * class. We build only a single instance and we cache it for future uses.
     * 
     * @return an instance of the <code>CoreCommonIIOStreamMetadataFormat</code>
     *         class.
     */
    public static synchronized IIOMetadataFormat getInstance() {
        if (theInstance == null) 
            theInstance = new CoreCommonIIOStreamMetadataFormat();
        return theInstance;
    }

    /**
     * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
     *      javax.imageio.ImageTypeSpecifier)
     */
    public boolean canNodeAppear(String elementName,
            ImageTypeSpecifier imageType) {
        throw new UnsupportedOperationException("Implement me");
    }

}
