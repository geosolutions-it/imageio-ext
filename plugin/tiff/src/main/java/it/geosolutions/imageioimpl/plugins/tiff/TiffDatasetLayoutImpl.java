/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2015, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.tiff;

import it.geosolutions.imageio.maskband.DatasetLayout;
import it.geosolutions.imageio.maskband.DefaultDatasetLayoutImpl;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFStreamMetadata.MetadataNode;

import java.io.File;
import java.io.IOException;

import javax.imageio.metadata.IIOMetadata;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link DatasetLayout} implementation which is able to parse {@link TIFFStreamMetadata} in order to get information about TIFF image structure
 * 
 * @author Nicola Lagomarsini
 */
public class TiffDatasetLayoutImpl extends DefaultDatasetLayoutImpl implements DatasetLayout {

    public TiffDatasetLayoutImpl() {
        super();
    }

    /** File containing external TIFF Masks */
    private File externalMasks;

    /** File containing external TIFF Overview */
    private File externalOverviews;

    /** File containing external TIFF Mask Overview */
    private File externalMaskOverviews;

    /** Value indicating how many overviews are contained inside the TIFF file */
    private int numInternalOverviews = -1;

    /** Value indicating how many masks are contained inside the TIFF file */
    private int numInternalMasks = -1;

    /** Value indicating how many masks are contained inside the external Mask TIFF file */
    private int numExternalMasks = -1;

    /** Value indicating how many overviews are contained inside the external Overview TIFF file */
    private int numExternalOverviews = -1;

    /** Value indicating how many mask overviews are contained inside the external Mask Overview TIFF file */
    private int numExternalMaskOverviews = -1;

    public int getNumInternalOverviews() {
        return numInternalOverviews;
    }

    public int getNumInternalMasks() {
        return numInternalMasks;
    }

    public int getNumExternalMasks() {
        return numExternalMasks;
    }

    public int getInternalOverviewImageIndex(int overviewIndex) {
        // 0 index means the image itself
        if (overviewIndex == 0) {
            return overviewIndex;
        }
        // Check if the index is inside the number of overviews
        if (numInternalOverviews < 0 || overviewIndex > numInternalOverviews) {
            return -1;
        }
        // Initial value set to 0
        int index = 0;
        // TIFF internal structure sets the index 1 to a mask if present
        // And then the other index are related to the Overviews
        if (numInternalMasks > 0) {
            // So we have to increase the index by one
            index++;
        }
        // Now we have the right imageindex
        return index + overviewIndex;
    }

    public int getInternalMaskImageIndex(int maskIndex) {
        // Checks on the Mask Size
        if (numInternalMasks <= 0 || maskIndex >= numInternalMasks) {
            return -1;
        }
        // Index 0 means that we must take the image 1 which is the first mask
        if (maskIndex == 0) {
            return 1;
        }
        // Index 0 and 1 means the first image and masks, then the further indexes
        // are related to the image overviews and then to mask overviews
        return numInternalOverviews + 2 + maskIndex;
    }

    public int getNumExternalOverviews() {
        return numExternalOverviews;
    }

    public int getNumExternalMaskOverviews() {
        return numExternalMaskOverviews;
    }

    public File getExternalMaskOverviews() {
        return externalMaskOverviews;
    }

    public File getExternalMasks() {
        return externalMasks;
    }

    public File getExternalOverviews() {
        return externalOverviews;
    }

    // Parameter setter
    public void setNumInternalOverviews(int numOverviews) {
        this.numInternalOverviews = numOverviews;
    }

    public void setNumInternalMasks(int numInternalMasks) {
        this.numInternalMasks = numInternalMasks;
    }

    public void setExternalMasks(File externalMasks) {
        this.externalMasks = externalMasks;
    }

    public void setNumExternalMasks(int numExternalMasks) {
        this.numExternalMasks = numExternalMasks;
    }

    public void setExternalOverviews(File externalOverviews) {
        this.externalOverviews = externalOverviews;
    }

    public void setExternalMaskOverviews(File externalMaskOverviews) {
        this.externalMaskOverviews = externalMaskOverviews;
    }

    public void setNumExternalOverviews(int numExternalOverviews) {
        this.numExternalOverviews = numExternalOverviews;
    }

    public void setNumExternalMaskOverviews(int numExternalMaskOverviews) {
        this.numExternalMaskOverviews = numExternalMaskOverviews;
    }

    /**
     * Creates a new {@link DatasetLayout} instance created from parsing input reader Stream Metadata.
     * 
     * @param metadata {@link IIOMetadata} object to parse
     * 
     * @return a {@link DatasetLayout} instance parsed from input StreamMetadata
     */
    public static DatasetLayout parseLayout(IIOMetadata metadata) throws IOException {
        // Init the layout to null
        TiffDatasetLayoutImpl layout = null;
        // Getting input parameters
        Node tree = metadata.getAsTree("com_sun_media_imageio_plugins_tiff_stream_1.0");
        // Ensuring not null
        if (tree == null) {
            return layout;
        } else {
            layout = new TiffDatasetLayoutImpl();
        }

        // Checking Childs
        NodeList list = tree.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            Node node = list.item(i);
            // Ensuring not null
            if (node == null) {
                continue;
            }
            // Getting the name
            String nodeName = node.getNodeName();
            // Getting Attribute Value
            String value = node.getAttributes().item(0).getNodeValue();
            // Getting the Enum related to the input Element
            MetadataNode mnode = MetadataNode.getFromName(nodeName);
            // Setting Attribute value
            switch (mnode) {
            case N_INT_MASK:
                layout.setNumInternalMasks(Integer.parseInt(value));
                break;
            case N_EXT_MASK:
                layout.setNumExternalMasks(Integer.parseInt(value));
                break;
            case N_INT_OVR:
                layout.setNumInternalOverviews(Integer.parseInt(value));
                break;
            case N_EXT_OVR:
                layout.setNumExternalOverviews(Integer.parseInt(value));
                break;
            case N_EXT_OVR_MASK:
                layout.setNumExternalMaskOverviews(Integer.parseInt(value));
                break;
            case EXT_MASK_FILE:
                layout.setExternalMasks((value != null && !value.isEmpty()) ? new File(value)
                        : null);
                break;
            case EXT_OVR_FILE:
                layout.setExternalOverviews((value != null && !value.isEmpty()) ? new File(value)
                        : null);
                break;
            case EXT_OVR_MASK_FILE:
                layout.setExternalMaskOverviews((value != null && !value.isEmpty()) ? new File(
                        value) : null);
                break;
            default:
                break;
            }
        }
        return layout;
    }
}
