/*
 * $RCSfile: TIFFStreamMetadata.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 05:01:50 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2015, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import it.geosolutions.imageio.maskband.DatasetLayout;

import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.metadata.IIOInvalidTreeException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IIOMetadata} subclass containing Metadata associated to the whole TIFF file.
 */
public class TIFFStreamMetadata extends IIOMetadata {

    // package scope
    protected static final String nativeMetadataFormatName =
        "com_sun_media_imageio_plugins_tiff_stream_1.0";

    protected static final String nativeMetadataFormatClassName =
        "it.geosolutions.imageioimpl.plugins.tiff.TIFFStreamMetadataFormat";

    /** Node name associated to the External mask File */
    public static final String EXTERNAL_MASK_FILE = "externalMaskFile";

    /** Node name associated to the External masks overview File */
    public static final String EXTERNAL_MASK_OVERVIEW_FILE = "externalMaskOverviewMaskFile";

    /** Node name associated to the External Overview File */
    public static final String EXTERNAL_OVERVIEW_FILE = "externalOverviewFile";

    /** Node name associated to the number of internal overviews */
    public static final String NUM_INTERNAL_OVERVIEWS = "numInternalOverviews";

    /** Node name associated to the number of external overviews */
    public static final String NUM_EXTERNAL_OVERVIEWS = "numExternalOverviews";

    /** Node name associated to the number of external mask overview */
    public static final String NUM_EXTERNAL_MASK_OVERVIEWS = "numExternalMaskOverviews";

    /** Node name associated to the number of external masks */
    public static final String NUM_EXTERNAL_MASKS = "numExternalMasks";

    /** Node name associated to the number of masks */
    public static final String NUM_INTERNAL_MASKS = "numInternalMasks";

    /** Node name associated to the file ByteOrder */
    public static final String BYTE_ORDER = "ByteOrder";

    /**
     * Enum used for defining the various node of the Metadata Tree
     * 
     * @author Nicola Lagomarsini GeoSolutions
     */
    public enum MetadataNode {
        B_ORDER(BYTE_ORDER) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                if (value.equals(bigEndianString)) {
                    metadata.byteOrder = ByteOrder.BIG_ENDIAN;
                } else if (value.equals(littleEndianString)) {
                    metadata.byteOrder = ByteOrder.LITTLE_ENDIAN;
                } else {
                    return "Incorrect value for " + BYTE_ORDER + " \"value\" attribute";
                }
                return null;
            }
        },
        N_INT_MASK(NUM_INTERNAL_MASKS) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                try {
                    // Getting number
                    int num = Integer.parseInt(value);
                    // Setting value
                    metadata.dtLayout.setNumInternalMasks(num);
                } catch (Exception e) {
                    return e.getMessage();
                }
                return null;
            }
        },
        N_EXT_MASK(NUM_EXTERNAL_MASKS) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                try {
                    // Getting number
                    int num = Integer.parseInt(value);
                    // Setting value
                    metadata.dtLayout.setNumExternalMasks(num);
                } catch (Exception e) {
                    return e.getMessage();
                }
                return null;
            }
        },
        N_INT_OVR(NUM_INTERNAL_OVERVIEWS) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                try {
                    // Getting number
                    int num = Integer.parseInt(value);
                    // Setting value
                    metadata.dtLayout.setNumInternalOverviews(num);
                } catch (Exception e) {
                    return e.getMessage();
                }
                return null;
            }
        },
        N_EXT_OVR(NUM_EXTERNAL_OVERVIEWS) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                try {
                    // Getting number
                    int num = Integer.parseInt(value);
                    // Setting value
                    metadata.dtLayout.setNumExternalOverviews(num);
                } catch (Exception e) {
                    return e.getMessage();
                }
                return null;
            }
        },
        N_EXT_OVR_MASK(NUM_EXTERNAL_MASK_OVERVIEWS) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                try {
                    // Getting number
                    int num = Integer.parseInt(value);
                    // Setting value
                    metadata.dtLayout.setNumExternalMaskOverviews(num);
                } catch (Exception e) {
                    return e.getMessage();
                }
                return null;
            }
        },
        EXT_MASK_FILE(EXTERNAL_MASK_FILE) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                metadata.dtLayout.setExternalMasks((value != null && !value.isEmpty()) ? new File(
                        value) : null);
                return null;
            }
        },
        EXT_OVR_FILE(EXTERNAL_OVERVIEW_FILE) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                metadata.dtLayout
                        .setExternalMaskOverviews((value != null && !value.isEmpty()) ? new File(
                                value) : null);
                return null;
            }
        },
        EXT_OVR_MASK_FILE(EXTERNAL_MASK_OVERVIEW_FILE) {
            @Override
            public String handleMetadata(TIFFStreamMetadata metadata, String value) {
                metadata.dtLayout
                        .setExternalOverviews((value != null && !value.isEmpty()) ? new File(value)
                                : null);
                return null;
            }
        };

        /** Node Name*/
        private String name;

        private MetadataNode(String name) {
            this.setName(name);
        }

        /** Getter for the Node Name*/
        public String getName() {
            return name;
        }

        /** Setter for the Node Name*/
        public void setName(String name) {
            this.name = name;
        }

        /**
         * This method allows to handle the input value inside the input {@link TIFFStreamMetadata}.
         * 
         * @param metadata
         * @param value
         * @return
         */
        public abstract String handleMetadata(TIFFStreamMetadata metadata, String value);

        /**
         * Static method which returns a {@link MetadataNode} from the input node name
         * 
         * @param name
         * @return
         */
        public static MetadataNode getFromName(String name) {
            for (MetadataNode node : values()) {
                if (node.getName().equalsIgnoreCase(name)) {
                    return node;
                }
            }
            return null;
        }
    }

    /** List of the allowed Node names */
    private static List<String> names = new ArrayList<String>();

    static {
        // Populating the Node Name list
        names.add(BYTE_ORDER);
        names.add(NUM_INTERNAL_MASKS);
        names.add(NUM_EXTERNAL_MASKS);
        names.add(NUM_INTERNAL_OVERVIEWS);
        names.add(NUM_EXTERNAL_OVERVIEWS);
        names.add(NUM_EXTERNAL_MASK_OVERVIEWS);
        names.add(EXTERNAL_MASK_FILE);
        names.add(EXTERNAL_OVERVIEW_FILE);
        names.add(EXTERNAL_MASK_OVERVIEW_FILE);
    }

    private static final String bigEndianString =
        ByteOrder.BIG_ENDIAN.toString();
    private static final String littleEndianString =
        ByteOrder.LITTLE_ENDIAN.toString();

    public ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    /** {@link DatasetLayout} associated to the Metadata*/
    public TiffDatasetLayoutImpl dtLayout = new TiffDatasetLayoutImpl();

    public TIFFStreamMetadata() {
        super(false,
              nativeMetadataFormatName,
              nativeMetadataFormatClassName,
              null, null);
    }

    public boolean isReadOnly() {
        return false;
    }

    // Shorthand for throwing an IIOInvalidTreeException
    private static void fatal(Node node, String reason)
        throws IIOInvalidTreeException {
        throw new IIOInvalidTreeException(reason, node);
    }

    public Node getAsTree(String formatName) {
        IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);

        IIOMetadataNode byteOrderNode = new IIOMetadataNode(BYTE_ORDER);
        byteOrderNode.setAttribute("value", byteOrder.toString());

        root.appendChild(byteOrderNode);

        // Setting Internal Mask number
        IIOMetadataNode numInternalMasksNode = new IIOMetadataNode(NUM_INTERNAL_MASKS);
        numInternalMasksNode.setAttribute("value", Integer.valueOf(dtLayout.getNumInternalMasks())
                .toString());
        // Setting External Mask number
        IIOMetadataNode numExternalMasksNode = new IIOMetadataNode(NUM_EXTERNAL_MASKS);
        numExternalMasksNode.setAttribute("value", Integer.valueOf(dtLayout.getNumExternalMasks())
                .toString());
        // Setting Internal Overview number
        IIOMetadataNode numInternalOverviewsNode = new IIOMetadataNode(NUM_INTERNAL_OVERVIEWS);
        numInternalOverviewsNode.setAttribute("value",
                Integer.valueOf(dtLayout.getNumInternalOverviews()).toString());
        // Setting Internal Overview number
        IIOMetadataNode numExternalOverviewsNode = new IIOMetadataNode(NUM_EXTERNAL_OVERVIEWS);
        numExternalOverviewsNode.setAttribute("value",
                Integer.valueOf(dtLayout.getNumExternalOverviews()).toString());
        // Setting External Mask Overview number
        IIOMetadataNode numExternalMaskOverviewsNode = new IIOMetadataNode(
                NUM_EXTERNAL_MASK_OVERVIEWS);
        numExternalMaskOverviewsNode.setAttribute("value",
                Integer.valueOf(dtLayout.getNumExternalMaskOverviews()).toString());
        // Setting external file path
        IIOMetadataNode externalMaskFileNode = new IIOMetadataNode(EXTERNAL_MASK_FILE);
        File file = dtLayout.getExternalMasks();
        externalMaskFileNode.setAttribute("value", file != null ? file.getAbsolutePath() : "");
        // Setting external Overview file path
        IIOMetadataNode externalOverviewFileNode = new IIOMetadataNode(EXTERNAL_OVERVIEW_FILE);
        file = dtLayout.getExternalOverviews();
        externalOverviewFileNode.setAttribute("value", file != null ? file.getAbsolutePath() : "");
        // Setting Internal Overview number
        IIOMetadataNode externalMaskOverviewFileNode = new IIOMetadataNode(
                EXTERNAL_MASK_OVERVIEW_FILE);
        file = dtLayout.getExternalMaskOverviews();
        externalMaskOverviewFileNode.setAttribute("value", file != null ? file.getAbsolutePath()
                : "");

        // Setting Child nodes
        root.appendChild(numInternalMasksNode);
        root.appendChild(numExternalMasksNode);
        root.appendChild(numInternalOverviewsNode);
        root.appendChild(numExternalOverviewsNode);
        root.appendChild(numExternalMaskOverviewsNode);
        root.appendChild(externalMaskFileNode);
        root.appendChild(externalOverviewFileNode);
        root.appendChild(externalMaskOverviewFileNode);

        return root;
    }

    private void mergeNativeTree(Node root) throws IIOInvalidTreeException {
        Node node = root;
        if (!node.getNodeName().equals(nativeMetadataFormatName)) {
            fatal(node, "Root must be " + nativeMetadataFormatName);
        }

        // Checking Childs
        if (node.hasChildNodes()) {
            // Check nodes
            NodeList childNodes = root.getChildNodes();
            int size = names.size();
            // Iterate on the nodes
            for (int i = 0; i < size; i++) {
                Node child = childNodes.item(i);
                String message = checkChild(child);
                if (message != null && !message.isEmpty()) {
                    fatal(child, message);
                } else {
                    // Check the childs
                    String nodeName = child.getNodeName();
                    NamedNodeMap attrs = child.getAttributes();
                    String value = (String) attrs.getNamedItem("value").getNodeValue();
                    // Getting Enum related to the Child
                    MetadataNode metadataNode = MetadataNode.getFromName(nodeName);
                    // Check if exists
                    if (metadataNode == null) {
                        fatal(child, "Undefined Node");
                    } else {
                        // Handle the value
                        String exception = metadataNode.handleMetadata(this, value);
                        // If an exception occurs, report it
                        if (exception != null && !exception.isEmpty()) {
                            fatal(child, exception);
                        }
                    }
                }
            }
        } else {
            fatal(node, "Root must have childs");
        }
    }

    /**
     * Method for checking if the {@link Node} object is related to the Metadata Tree
     * 
     * @param child
     * @return
     */
    private String checkChild(Node child) {
        // Null check
        if (child == null) {
            return "Root cannot have null child";
        }
        // Name check
        String nodeName = child.getNodeName();
        if (names.contains(nodeName)) {
            NamedNodeMap attrs = child.getAttributes();
            String value = (String) attrs.getNamedItem("value").getNodeValue();
            // Null value check
            if (value == null) {
                return nodeName + " node must have a \"value\" attribute";
            }
        } else {
            return "Root cannot have \"" + nodeName + "\" child";
        }
        return null;
    }

    public void mergeTree(String formatName, Node root)
        throws IIOInvalidTreeException {
        if (formatName.equals(nativeMetadataFormatName)) {
            if (root == null) {
                throw new IllegalArgumentException("root == null!");
            }
            mergeNativeTree(root);
        } else {
            throw new IllegalArgumentException("Not a recognized format!");
        }
    }

    public void reset() {
        this.byteOrder = ByteOrder.BIG_ENDIAN;
        dtLayout = new TiffDatasetLayoutImpl();
    }
}
