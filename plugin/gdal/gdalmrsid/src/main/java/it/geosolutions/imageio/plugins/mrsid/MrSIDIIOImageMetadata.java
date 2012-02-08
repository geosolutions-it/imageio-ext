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

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.Map;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;

import org.gdal.gdal.Dataset;
import org.w3c.dom.Node;

/**
 * Specialization of {@link IIOMetadata} specific for the MrSID format. It
 * provides the user with the specific MrSID metadata.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class MrSIDIIOImageMetadata extends GDALCommonIIOImageMetadata {

    /**
     * The name of the metadata format for this object.
     */
    public final static String mrsidImageMetadataName = "org_gdal_imageio_mrsid_metadata";

    /**
     * The name of the class implementing <code>IIOMetadataFormat</code> and
     * representing the metadata format for this object.
     */
    public static final String mrsidImageMetadataFormatClassName = "it.geosolutions.imageio.gdalframework.MrSIDIIOImageMetadataFormat";

    /**
     * <code>MrSIDIIOImageMetadata</code> constructor.
     * 
     * @param dataseName
     *                The name (usually a File path or a subdataset name when
     *                the format supports subdatasets) of the dataset we want to
     *                open.
     */
    public MrSIDIIOImageMetadata(final String datasetName) {
        super(datasetName, mrsidImageMetadataName,
                mrsidImageMetadataFormatClassName);
    }
    
    /**
     * <code>MrSIDIIOImageMetadata</code> constructor.
     * 
     * @param dataset
     *                The name (usually a File path or a subdataset name when
     *                the format supports subdatasets) of the dataset we want to
     *                open.
     */
    public MrSIDIIOImageMetadata(final Dataset dataset,final String datasetName) {
        super(dataset, datasetName, mrsidImageMetadataName,
                mrsidImageMetadataFormatClassName);
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     * 
     * @param formatName
     *                the name of the requested metadata format. Actually
     *                supported format name are {@link #mrsidImageMetadataName}
     *                and
     *                {@link GDALCommonIIOImageMetadata#nativeMetadataFormatName}.
     */
    public Node getAsTree(String formatName) {
        if (formatName.equalsIgnoreCase(mrsidImageMetadataName))
            return getMrSIDMetadataTree();
        return super.getAsTree(formatName);
    }

    /**
     * Returns the XML DOM <code>Node</code> object that represents the root
     * of a tree of metadata contained within this object on its native format,
     * which contains MrSID specific information.
     * 
     * @return a root node containing common metadata exposed on its native
     *         format.
     */
    private Node getMrSIDMetadataTree() {

        // Create root node
        final IIOMetadataNode root = new IIOMetadataNode(mrsidImageMetadataName);
        // final Dataset ds = GDALUtilities.acquireDataSet(getDatasetName(),
        // gdalconst.GA_ReadOnly);
        // final Map gdalMetadataMap = ds.GetMetadata_Dict("");

        final Map defaultDomainMap = getGdalMetadataDomain(GDALUtilities.GDALMetadataDomain.DEFAULT);

        // //
        //
        // ImageDescriptor
        //
        // //
        IIOMetadataNode node = new IIOMetadataNode("ImageDescriptor");
        node.setAttribute("IMAGE__INPUT_NAME", getDatasetName());
        GDALUtilities.setNodeAttribute("IMAGE__INPUT_FILE_SIZE",
                defaultDomainMap.get("IMAGE__INPUT_FILE_SIZE"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__DYNAMIC_RANGE_WINDOW",
                defaultDomainMap.get("IMAGE__DYNAMIC_RANGE_WINDOW"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__DYNAMIC_RANGE_LEVEL",
                defaultDomainMap.get("IMAGE__DYNAMIC_RANGE_LEVEL"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_VERSION",
                defaultDomainMap.get("IMAGE__COMPRESSION_VERSION"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__TARGET_COMPRESSION_RATIO",
                defaultDomainMap.get("IMAGE__TARGET_COMPRESSION_RATIO"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_NLEV",
                defaultDomainMap.get("IMAGE__COMPRESSION_NLEV"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_WEIGHT",
                defaultDomainMap.get("IMAGE__COMPRESSION_WEIGHT"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_GAMMA",
                defaultDomainMap.get("IMAGE__COMPRESSION_GAMMA"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_BLOCK_SIZE",
                defaultDomainMap.get("IMAGE__COMPRESSION_BLOCK_SIZE"), node,
                IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities.setNodeAttribute("IMAGE__CREATION_DATE", defaultDomainMap
                .get("IMAGE__CREATION_DATE"), node,
                IIOMetadataFormat.DATATYPE_STRING);
        GDALUtilities.setNodeAttribute("IMAGE__WIDTH", defaultDomainMap
                .get("IMAGE__WIDTH"), node, IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities
                .setNodeAttribute("IMAGE__HEIGHT", defaultDomainMap
                        .get("IMAGE__HEIGHT"), node,
                        IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities.setNodeAttribute("IMAGE__TRANSPARENT_DATA_VALUE",
                defaultDomainMap.get("IMAGE__TRANSPARENT_DATA_VALUE"), node,
                IIOMetadataFormat.DATATYPE_STRING);
        GDALUtilities.setNodeAttribute("IMAGE__COLOR_SCHEME", defaultDomainMap
                .get("IMAGE__COLOR_SCHEME"), node,
                IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities.setNodeAttribute("IMAGE__DATA_TYPE", defaultDomainMap
                .get("IMAGE__DATA_TYPE"), node,
                IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities.setNodeAttribute("IMAGE__BITS_PER_SAMPLE",
                defaultDomainMap.get("IMAGE__BITS_PER_SAMPLE"), node,
                IIOMetadataFormat.DATATYPE_INTEGER);
        root.appendChild(node);

        // //
        //
        // Georeferencing
        //
        // //
        node = new IIOMetadataNode("Georeferencing");
        GDALUtilities.setNodeAttribute("IMG__HORIZONTAL_UNITS",
                defaultDomainMap.get("IMG__HORIZONTAL_UNITS"), node,
                IIOMetadataFormat.DATATYPE_STRING);
        GDALUtilities.setNodeAttribute("IMG__PROJECTION_TYPE", defaultDomainMap
                .get("IMG__PROJECTION_TYPE"), node,
                IIOMetadataFormat.DATATYPE_STRING);
        GDALUtilities.setNodeAttribute("IMG__PROJECTION_NUMBER",
                defaultDomainMap.get("IMG__PROJECTION_NUMBER"), node,
                IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities.setNodeAttribute("IMG__PROJECTION_ZONE", defaultDomainMap
                .get("IMG__PROJECTION_ZONE"), node,
                IIOMetadataFormat.DATATYPE_INTEGER);
        GDALUtilities.setNodeAttribute("IMG__SPHEROID_NAME", defaultDomainMap
                .get("IMG__SPHEROID_NAME"), node,
                IIOMetadataFormat.DATATYPE_STRING);
        GDALUtilities.setNodeAttribute("IMG__SPHEROID_SEMI_MAJOR_AXIS",
                defaultDomainMap.get("IMG__SPHEROID_SEMI_MAJOR_AXIS"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMG__SPHEROID_SEMI_MINOR_AXIS",
                defaultDomainMap.get("IMG__SPHEROID_SEMI_MINOR_AXIS"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMG__SPHEROID_ECCENTRICITY_SQUARED",
                defaultDomainMap.get("IMG__SPHEROID_ECCENTRICITY_SQUARED"),
                node, IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMG__SPHEROID_RADIUS", defaultDomainMap
                .get("IMG__SPHEROID_RADIUS"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__XY_ORIGIN", defaultDomainMap
                .get("IMAGE__XY_ORIGIN"), node,
                IIOMetadataFormat.DATATYPE_STRING);
        GDALUtilities.setNodeAttribute("IMAGE__X_RESOLUTION", defaultDomainMap
                .get("IMAGE__X_RESOLUTION"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__Y_RESOLUTION", defaultDomainMap
                .get("IMAGE__Y_RESOLUTION"), node,
                IIOMetadataFormat.DATATYPE_DOUBLE);
        GDALUtilities.setNodeAttribute("IMAGE__WKT", defaultDomainMap
                .get("IMAGE__WKT"), node, IIOMetadataFormat.DATATYPE_STRING);
        root.appendChild(node);
        // GDALUtilities.closeDataSet(ds);
        return root;

    }

    /**
     * Returns <code>true</code> since this object does not support the
     * <code>mergeTree</code>, <code>setFromTree</code>, and
     * <code>reset</code> methods.
     * 
     * @return <code>true</code> since this <code>IIOMetadata</code> object
     *         cannot be modified.
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Method unsupported. Calling this method will throws an
     * <code>UnsupportedOperationException</code>
     * 
     * @see javax.imageio.metadata.IIOMetadata#mergeTree()
     * 
     * @see #isReadOnly()
     */
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
        throw new UnsupportedOperationException(
                "mergeTree operation is not allowed");
    }

    /**
     * Method unsupported. Calling this method will throws an
     * <code>UnsupportedOperationException</code>
     * 
     * @see javax.imageio.metadata.IIOMetadata#reset()
     * 
     * @see #isReadOnly()
     */
    public void reset() {
        throw new UnsupportedOperationException(
                "reset operation is not allowed");
    }
}
