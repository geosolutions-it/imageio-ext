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
package it.geosolutions.imageio.plugins.hdf4.terascan;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import it.geosolutions.imageio.utilities.Utilities;

import java.io.IOException;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

public class HDF4TeraScanStreamMetadata extends IIOMetadata {
    /**
     * The name of the native metadata format for this object.
     */
    public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_jhdf_hdf_avhrr_streamMetadata_1.0";

    private BaseImageReader reader;

    public final static String GLOBAL_ATTRIBUTES = "GlobalAttributes";

    public HDF4TeraScanStreamMetadata(final BaseImageReader reader) {
        this.reader = reader;
    }

    /**
     * Returns the XML DOM <code>Node</code> object that represents the root
     * of a tree of metadata contained within this object on its native format.
     * 
     * @return a root node containing common metadata exposed on its native
     *         format.
     */
    protected Node createCommonNativeTree() {
        // Create root node
        final IIOMetadataNode root = new IIOMetadataNode(
                nativeMetadataFormatName);

        // ////////////////////////////////////////////////////////////////////
        //
        // GlobalAttributes
        //
        // ////////////////////////////////////////////////////////////////////
        final IIOMetadataNode node = new IIOMetadataNode(GLOBAL_ATTRIBUTES);
        if (reader instanceof HDF4TeraScanImageReader) {
            final HDF4TeraScanImageReader directReader = (HDF4TeraScanImageReader) reader;
            final int numAttributes = directReader.getNumGlobalAttributes();
            try {
                for (int i = 0; i < numAttributes; i++) {
                	 final KeyValuePair keyValuePair = directReader.getGlobalAttribute(i);
                     String attribName = keyValuePair.getKey();
                     final String attribValue = keyValuePair.getValue();
                    // //
                    // Note: IIOMetadata doesn't allow to set attribute name
                    // containing "\\". Therefore we replace that char
                    // //
                    if (attribName.contains("\\"))
                    	attribName = Utilities
                                .adjustAttributeName(attribName);
                    node.setAttribute(attribName, attribValue);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to parse attribute",
                        e);
            }

            root.appendChild(node);
        }
        return root;
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     * 
     * @param formatName
     *                the name of the requested metadata format. Note that
     *                actually, the only supported format name is the
     *                {@link CoreCommonImageMetadata#nativeMetadataFormatName}.
     *                Requesting other format names will result in an
     *                <code>IllegalArgumentException</code>
     */
    public Node getAsTree(String formatName) {
        if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
            return createCommonNativeTree();
        throw new IllegalArgumentException(formatName
                + " is not a supported format name");
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
        // TODO: add message
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
    }

}
