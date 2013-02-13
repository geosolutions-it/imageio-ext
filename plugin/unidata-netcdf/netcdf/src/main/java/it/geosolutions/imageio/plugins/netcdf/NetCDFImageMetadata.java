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
package it.geosolutions.imageio.plugins.netcdf;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * Class for the NetCDF Image Metadata representation.
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
public class NetCDFImageMetadata extends CoreCommonImageMetadata {

    private final static String driverName = "netCDF";

    private final static String driverDescription = "Network Common Data Format";

    public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_nectdf_netcdfImageMetadata_1.0";

    private HashMap<String, String> additionalMetadata;

    private IIOMetadataNode nativeTree;
    
    public final static String ATTRIBUTES_NODE = "Attributes";

    protected final NetCDFImageReader imageReader;

    private int imageIndex;

    private Node commonNativeTree;

    /**
     * <code>BaseImageMetadata</code> constructor.
     * 
     * @param reader
     *                the reader used to obtain metadata.
     * @param imageIndex
     *                the image index for which to get the metadata
     */
    protected NetCDFImageMetadata(NetCDFImageReader reader, int imageIndex) {
        super(false, nativeMetadataFormatName, nativeMetadataFormatClassName,null, null);
        if (reader == null)
            throw new NullPointerException("The provided reader is null");
        
        this.imageIndex = imageIndex;
        imageReader = reader;

        // //
        //
        // Initializing member if needed
        //
        // //
        try {
            setMembers(imageReader);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void setMembers( NetCDFImageReader imageReader ) throws IOException {
        // Retrieving raster properties
        setWidth(imageReader.getWidth(imageIndex));
        setHeight(imageReader.getHeight(imageIndex));
        setTileWidth(imageReader.getTileWidth(imageIndex));
        setTileHeight(imageReader.getTileHeight(imageIndex));

        // TODO: Should I expose a getNumBands method instead of leveraging
        // on imageTypeSpecifier?
        Iterator<ImageTypeSpecifier> it = imageReader.getImageTypes(imageIndex);
        if (it != null && it.hasNext()) {
            final ImageTypeSpecifier its = it.next();
            setNumBands(its.getSampleModel().getNumBands());
        }

        // set metadata
        final int imageIndex = getImageIndex();
        final NetCDFImageReader reader = (NetCDFImageReader) imageReader;
        setDriverName(driverName);
        setDriverDescription(driverDescription);
        setDatasetName(reader.getVariableName(imageIndex));
        final double scale = reader.getScale(imageIndex);
        if (!Double.isNaN(scale))
            setScales(new Double[]{Double.valueOf(scale)});
        final double offset = reader.getOffset(imageIndex);
        if (!Double.isNaN(offset))
            setOffsets(new Double[]{Double.valueOf(offset)});
        final double fillValue = reader.getFillValue(imageIndex);
        if (!Double.isNaN(fillValue))
            setNoDataValues(new Double[]{Double.valueOf(fillValue)});

        // TODO: Setting valid range as max min is ok?
        final double[] validRange = reader.getValidRange(imageIndex);
        if (validRange != null && validRange.length == 2 && !Double.isNaN(validRange[0]) && !Double.isNaN(validRange[1])) {
            setMinimums(new Double[]{Double.valueOf(validRange[0])});
            setMaximums(new Double[]{Double.valueOf(validRange[1])});
        }

        // overviews are always absent
        setNumOverviews(new int[]{0});

        final int numAttributes = imageReader.getNumAttributes(imageIndex);
        this.additionalMetadata = new HashMap<String, String>(numAttributes);
        for( int i = 0; i < numAttributes; i++ ) {
            final KeyValuePair attributePair = imageReader.getAttribute(imageIndex, i);
            final String attributeName = attributePair.getKey();
            final String attributeValue = attributePair.getValue();
            additionalMetadata.put(attributeName, attributeValue);
        }

    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     */
    public Node getAsTree(String formatName) {
        if (NetCDFImageMetadata.nativeMetadataFormatName .equalsIgnoreCase(formatName))
            return createNativeTree();
        else if (CoreCommonImageMetadata.nativeMetadataFormatName.equalsIgnoreCase(formatName))
            return super.createCommonNativeTree();
        throw new IllegalArgumentException(formatName+ " is not a supported format name");
    }

    private synchronized Node createNativeTree() {
        if (this.nativeTree != null)
            return this.nativeTree;
        nativeTree = new IIOMetadataNode(NetCDFImageMetadata.nativeMetadataFormatName);

        // ////////////////////////////////////////////////////////////////////
        //
        // DatasetDescriptor
        //
        // ////////////////////////////////////////////////////////////////////
        if (this.additionalMetadata != null) {
            IIOMetadataNode node = new IIOMetadataNode(ATTRIBUTES_NODE);

            for (String key : this.additionalMetadata.keySet()) {
                final String attributeValue = additionalMetadata.get(key);
                node.setAttribute(key, attributeValue);
            }

            nativeTree.appendChild(node);
        }

        return nativeTree;
    }

    protected int getImageIndex() {
        return imageIndex;
    }

    @Override
    protected synchronized Node createCommonNativeTree() {
        if (this.commonNativeTree == null)
            commonNativeTree = super.createCommonNativeTree();
        return commonNativeTree;

    }

    public NetCDFImageReader getImageReader() {
        return imageReader;
    }
}