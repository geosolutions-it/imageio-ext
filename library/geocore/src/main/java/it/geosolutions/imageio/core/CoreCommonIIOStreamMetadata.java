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

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * Class representing common stream metadata 
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class CoreCommonIIOStreamMetadata extends IIOMetadata {

    /**
     * The name of the native metadata format for this object.
     */
    public static final String nativeMetadataFormatName = "it.geosolutions.imageio.core.commonStreamMetadata_1.0";

    /**
     * The name of the class implementing <code>IIOMetadataFormat</code> and
     * representing the native metadata format for this object.
     */
    public static final String nativeMetadataFormatClassName = "it.geosolutions.imageio.core.CoreCommonIIOStreamMetadata";

    /** A list of the dataset names available within the underlying stream */
    private final String datasetNames[];

    /**
     * Public constructor for common stream metadata object. It builds a proper
     * {@link CoreCommonIIOStreamMetadata} object given the dataset names
     * available within the underlying stream.
     * 
     * @param datasetNames
     *                the dataset names available within the datasource.
     */
    public CoreCommonIIOStreamMetadata(String datasetNames[]) {
        super(false, nativeMetadataFormatName, nativeMetadataFormatClassName,null, null);
        this.datasetNames = datasetNames;
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     * 
     * @param formatName
     *                the name of the requested metadata format. Note that
     *                actually, the only supported format name is the
     *                {@link CoreCommonIIOStreamMetadata#nativeMetadataFormatName}.
     *                Requesting other format names will result in an
     *                <code>IllegalArgumentException</code>
     */
    public Node getAsTree(String formatName) {
        if (!nativeMetadataFormatName.equalsIgnoreCase(formatName))
            throw new IllegalArgumentException(formatName + " is not a recognized format name for gdal stream metadata.");
        final IIOMetadataNode root = new IIOMetadataNode( nativeMetadataFormatName);
        final IIOMetadataNode dataSetsNode = new IIOMetadataNode("DataSets");
        root.appendChild(dataSetsNode);
        // we need to take into account that when subdatasets are supported we
        // have to remove 1 from the number of datasets we declare
        int length = datasetNames.length;
        if (length > 1)
            length--;
        dataSetsNode.setAttribute("number", Integer.toString(length));
        for (int i = 0; i < length; i++) {
            final IIOMetadataNode dataSetNode = new IIOMetadataNode("DataSet");
            dataSetNode.setAttribute("name", datasetNames[i]);
            dataSetsNode.appendChild(dataSetNode);
        }
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
    @Override
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
    @Override
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
        throw new UnsupportedOperationException("mergeTree operation is not allowed");
    }

    /**
     * Method unsupported. Calling this method will throws an
     * <code>UnsupportedOperationException</code>
     * 
     * @see javax.imageio.metadata.IIOMetadata#reset()
     * 
     * @see #isReadOnly()
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException("reset operation is not allowed");
    }

}
