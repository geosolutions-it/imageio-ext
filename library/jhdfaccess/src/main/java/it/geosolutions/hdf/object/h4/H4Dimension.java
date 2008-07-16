/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a SDS Dimension.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4Dimension extends H4Variable implements IHObject, IH4Object {

    private AbstractH4Object attributesHolder;

    private class H4DimensionAttributesManager extends AbstractH4Object {

        private Map predefAttribsByName = null;

        private Map predefAttribsByIndex = null;

        private Map predefValues = null;

        private boolean hasPredefined;

        public H4DimensionAttributesManager(final int identifier,
                final int numAttributes, String[] predefinedAttributePairs)
                throws HDFException {
            super(identifier, numAttributes);
            if (predefinedAttributePairs != null) {
                hasPredefined = true;
                if (hasPredefined)
                    attributesCheck(predefinedAttributePairs);
            }
        }

        /**
         * returns a <code>Map</code> containing all attributes associated to
         * this Dimension
         * 
         * @return the map of attributes.
         * @throws HDFException
         * @throws HDFException
         */
        private void attributesCheck(final String[] predefinedAttributePairs)
                throws HDFException {
            final int numAttributes = this.getNumAttributes();
            final String[] dimAttrName = new String[1];
            final int numPredef = predefinedAttributePairs.length / 2;
            predefAttribsByIndex = new HashMap(numPredef);
            predefAttribsByName = new HashMap(numPredef);
            predefValues = new HashMap(numPredef);
            for (int i = 0; i < numAttributes; i++) {
                dimAttrName[0] = "";
                final int[] dimAttrInfo = { 0, 0 };
                // get various info about this attribute
                HDFLibrary.SDattrinfo(getIdentifier(), i, dimAttrName,dimAttrInfo);
                final String attrName = dimAttrName[0];

                // //
                //
                // The HDF user guide explicitly states that
                // Predefined Attributes for Dimensions need to be
                // read using specialized routines (SDgetdimstrs).
                // So, I will setup information related to predefined Attributes
                //
                // //
                for (int k = 0; k < numPredef; k++) {
                    if (attrName.equals(predefinedAttributePairs[k * 2])) {
                        Integer index = Integer.valueOf(i);
                        predefAttribsByIndex.put(index, attrName);
                        predefAttribsByName.put(attrName, index);
                        String value = predefinedAttributePairs[(k * 2) + 1];
                        predefValues.put(attrName, value);
                    }
                }
            }
        }

        /**
         * @see {@link AbstractH4Object#readAttribute(int, Object)}
         */
        protected boolean readAttribute(int index, Object values)
                throws HDFException {
            if (hasPredefined) {
                boolean success = false;
                Integer i = Integer.valueOf(index);
                if (predefAttribsByIndex.containsKey(i)) {
                    String name = (String) predefAttribsByIndex.get(i);
                    if (name != null && name.trim().length() > 0
                            && predefValues.containsKey(name)) {
                        // //
                        //
                        // Values have been preemptively allocated.
                        // Note that predefined attributes have values
                        // as a byte array. I simply need to copy bytes
                        //
                        // //
                        final byte[] valueBytes = ((String) predefValues
                                .get(name)).getBytes();

                        // Casting the preallocated values container to
                        // the byte array to be filled
                        byte[] retValues = ((byte[]) values);
                        final int size = retValues.length;
                        for (int k = 0; k < size; k++)
                            retValues[k] = valueBytes[k];
                        success = true;
                    }
                }
                return success;
            } else
                return HDFLibrary.SDreadattr(getIdentifier(), index, values);
        }

        /**
         * @see {@link AbstractH4Object#getAttributeInfo(int, String[])}
         */
        protected int[] getAttributeInfo(int index, String[] attrName)
                throws HDFException {
        	H4Utilities.checkNonNull(attrName, "attrName");
            final int[] dimAttrInfo = { 0, 0 };
            boolean done = false;
            if (hasPredefined) {
                Integer i = Integer.valueOf(index);
                if (predefAttribsByIndex.containsKey(i)) {
                    String name = (String) predefAttribsByIndex.get(i);
                    if (name != null && name.trim().length() > 0&& predefValues.containsKey(name)) {
                        byte[] values = ((String) predefValues.get(name)).getBytes();
                        attrName[0] = name;
                        dimAttrInfo[0] = HDFConstants.DFNT_CHAR8;
                        dimAttrInfo[1] = values.length;
                        done = true;
                    }
                }
            } else {
                done = HDFLibrary.SDattrinfo(getIdentifier(), index, attrName,dimAttrInfo);
            }
            if (done)
                return dimAttrInfo;
            else
                return null;
        }

        /**
         * @see {@link AbstractH4Object#getAttributeIndexByName(String)}
         */
        protected int getAttributeIndexByName(String attributeName)
                throws HDFException {
        	H4Utilities.checkNonNull(attributeName, "attributeName");
            if (hasPredefined) {
                int index = -1;
                if (predefAttribsByName.containsKey(attributeName)) {
                    Integer i = (Integer) predefAttribsByName
                            .get(attributeName);
                    if (i != null) {
                        index = i.intValue();
                    }
                }
                return index;
            } else
                return HDFLibrary.SDfindattr(getIdentifier(), attributeName);
        }

        /**
         * Clear the attributes mappings.
         */
        public synchronized void dispose() {
            if (predefAttribsByIndex != null) {
                predefAttribsByIndex.clear();
                predefAttribsByIndex = null;
            }
            if (predefAttribsByName != null) {
                predefAttribsByName.clear();
                predefAttribsByName = null;
            }
            if (predefValues != null) {
                predefValues.clear();
                predefValues = null;
            }
            super.dispose();
        }
    }

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    /**
     * <code>true</code> if a dimension scale is set for this dimension.
     * <code>false</code> otherwise
     */
    private boolean hasDimensionScaleSet = false;

    /**
     * the ID of the SDS representing the dimension scale (if present) set for
     * this dimension.
     */
    private int sdsDimensionScaleID = HDFConstants.FAIL;

    /**
     * the datatype of the dimension scale, if a dimension scale is set for this
     * dimension. Otherwise, datatype is zero.
     */
    private int datatype;

    /**
     * The index of this dimension along the owner Scientific Dataset.
     */
    private int index;

    /**
     * The size of this dimension.
     */
    private int size;

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////

    /**
     * getter of <code>index</code>
     * 
     * @return the index of this dimension.
     */
    public int getIndex() {
        return index;
    }

    /**
     * getter of <code>size</code>
     * 
     * @return the size of this dimension.
     */
    public int getSize() {
        return size;
    }

    /**
     * getter of <code>datatype</code>.
     * 
     * @return the datatype. If a dimension scale is set for this dimension,
     *         <code>datatype</code> is the data type of the dimension scale.
     *         Otherwise, datatype is zero.
     */
    public int getDatatype() {
        return datatype;
    }

    /**
     * getter of <code>hasDimensionScaleSet</code>.
     * 
     * @return <code>true</code> if a dimension scale is set for this
     *         dimension. <code>false</code> otherwise
     */
    public final boolean isHasDimensionScaleSet() {
        return hasDimensionScaleSet;
    }

    /**
     * Builds a {@link H4Dimension} given the SDS to which the dimension
     * belongs, and the index of the dimension within the SDS.
     * 
     * @param sds
     *                the {@link H4SDS} to which this dimension belongs.
     * @param dimensionIndex
     *                the index of the dimension within the SDS.
     * @throws IllegalArgumentException
     *                 in case of wrong specified input parameters or
     *                 identifiers.
     * @throws IllegalStateException
     *                 in case of problems getting a valid identifier for the
     *                 Annotation APIs
     */
    public H4Dimension(H4SDS sds, final int dimensionIndex) {
        index = dimensionIndex;
        if (sds == null)
            throw new IllegalArgumentException("Null sds provided");
        if (dimensionIndex < 0)
            throw new IllegalArgumentException("Invalid dimension index");
        final int sdsID = sds.getIdentifier();
        if (sdsID == HDFConstants.FAIL)
            throw new IllegalArgumentException("Invalid sds identifier");
        try {

            // get the id of the required dimension of the specified dataset
            int identifier = HDFLibrary.SDgetdimid(sdsID, dimensionIndex);
            if (identifier != HDFConstants.FAIL) {
                setIdentifier(identifier);
                // retrieving dimension information
                final String[] dimName = { "" };
                final int[] dimInfo = { 0, 0, 0 };
                HDFLibrary.SDdiminfo(identifier, dimName, dimInfo);
                setName(dimName[0]);
                size = dimInfo[0];
                datatype = dimInfo[1] & (~HDFConstants.DFNT_LITEND);

                // //
                //
                // Predefined dimension attributes should be differently handled
                // by leveraging on special APIs.
                // The following workaround manages that case.
                //
                // //
                String predefAttributePairs[] = checkForPredefined();
                attributesHolder = new H4DimensionAttributesManager(
                        identifier, dimInfo[2], predefAttributePairs);

                // Retrieving dimension scale
                final int interfaceID = sds.getH4SDSCollectionOwner()
                        .getIdentifier();

                // If set, the dimension scale has the same name of the
                // dimension
                final int dimensionScaleIndex = HDFLibrary.SDnametoindex(
                        interfaceID, getName());
                if (dimensionScaleIndex != HDFConstants.FAIL) {
                    sdsDimensionScaleID = HDFLibrary.SDselect(interfaceID,
                            dimensionScaleIndex);
                    hasDimensionScaleSet = HDFLibrary
                            .SDiscoordvar(sdsDimensionScaleID);
                    if (hasDimensionScaleSet && datatype == 0) {
                        // //////////////
                        //
                        // Sometimes, although dimension scale values exist,
                        // returned datatype is zero. Then, I attempt to
                        // retrieve the datatype from the SDS containing
                        // dimension scale values.
                        //
                        // //////////////
                        final int dummyInfo[] = { 0, 0, 0 };
                        final String dummyName[] = { "" };
                        final int dummyDimSizes[] = new int[HDFConstants.MAX_VAR_DIMS];
                        if (HDFLibrary.SDgetinfo(sdsDimensionScaleID,
                                dummyName, dummyDimSizes, dummyInfo)) {
                            datatype = dummyInfo[1];
                        }
                    }
                    //
                    // final boolean isDimensionScale = HDFLibrary
                    // .SDiscoordvar(sdsDimensionScaleID);
                    // if (isDimensionScale) {
                    // dimensionScaleID = HDFLibrary.SDgetdimid(
                    // sdsDimensionScaleID, 0);
                    // hasDimensionScaleSet = true;
                    // }
                }
            } else {
                throw new IllegalStateException(
                        "Failing to get an identifier for the Dimension");
            }

        } catch (HDFException e) {
            throw new IllegalStateException(
                    "HDFException occurred while creating a new H4Dimension", e);
        }
    }

    private String[] checkForPredefined() throws HDFException {
        String[] predefAttributePairs = null;
        final String predefAttributesValues[] = { "NONE", "NONE", "NONE" };

        if (HDFLibrary.SDgetdimstrs(getIdentifier(), predefAttributesValues,
                HDFConstants.DFS_MAXLEN)) {
            int predefined = 0;
            boolean predefinedElements[] = new boolean[] { false, false, false };
            for (int k = 0; k < 3; k++) {
                if (predefAttributesValues[k] != null) {
                    predefined++;
                    predefinedElements[k] = true;
                }

            }
            if (predefined != 0) {
                final String predefinedStrings[] = { H4Utilities.PREDEF_ATTR_LABEL,
                        H4Utilities.PREDEF_ATTR_UNIT, H4Utilities.PREDEF_ATTR_FORMAT };
                predefAttributePairs = new String[predefined * 2];
                for (int k = 0; k < 3; k++) {
                    if (predefinedElements[k]) {
                        predefAttributePairs[k * 2] = predefinedStrings[k];
                        predefAttributePairs[(k * 2) + 1] = predefAttributesValues[k];
                    }
                }
            }

        }
        return predefAttributePairs;
    }

    /**
     * Returns a proper <code>Object</code> containing the values of the
     * dimension scale set for this dimension. The type of the returned object
     * depends on the datatype of the dimension scale. As an instance, for a
     * dimension scale having <code>HDFConstants.DFNT_INT32</code> as
     * datatype, returned object is an <code>int</code> array. See
     * {@link H4Utilities#allocateArray(int, int)} to retrieve information about
     * the returned type.
     * 
     * @return an <code>Object</code> containing dimension scale values if
     *         this dimension has a dimension scale set. <code>null</code>
     *         otherwise.
     * @throws HDFException
     */

    public Object getDimensionScaleValues() throws HDFException {
        Object dataValues = null;
        if (hasDimensionScaleSet && getIdentifier() != HDFConstants.FAIL) {
            dataValues = H4Utilities.allocateArray(datatype, size);
            if (dataValues != null)
                HDFLibrary.SDgetdimscale(getIdentifier(), dataValues);
        }
        return dataValues;
    }

    /**
     * close this {@link H4Dimension} and dispose allocated objects. if a
     * Dimension Scale is available for this Dimension, I need to close access
     * to the SDS containing dimension scale values
     */
    public synchronized void dispose() {
        final int identifier = getIdentifier();
        if (identifier != HDFConstants.FAIL) {
            if (attributesHolder != null) {
                attributesHolder.dispose();
                attributesHolder = null;
            }
            if (hasDimensionScaleSet) {
                try {
                    // end access to the SDS representing the dimension
                    if (sdsDimensionScaleID != HDFConstants.FAIL) {
                        if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.log(Level.FINE,
                                    "disposing dimension scale with ID = "
                                            + sdsDimensionScaleID);
                        boolean closed = HDFLibrary
                                .SDendaccess(sdsDimensionScaleID);
                        if (!closed) {
                            if (LOGGER.isLoggable(Level.WARNING))
                                LOGGER.log(Level.WARNING,
                                        "Unable to close access to the dimension scale with ID = "
                                                + sdsDimensionScaleID);
                        }
                        sdsDimensionScaleID = HDFConstants.FAIL;
                    }
                } catch (HDFException e) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "Error closing access to the dimension scale with ID = "
                                        + sdsDimensionScaleID);
                }
            }
        }
        super.dispose();
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during dimension finalization: "
                                + e.getLocalizedMessage());
        }
    }

    /**
     * @see {@link IH4Object#getAttribute(int)}
     */
    public H4Attribute getAttribute(int attributeIndex) throws HDFException {
        return attributesHolder.getAttribute(attributeIndex);
    }

    /**
     * @see {@link IH4Object#getAttribute(String)}
     */
    public H4Attribute getAttribute(String attributeName) throws HDFException {
        return attributesHolder.getAttribute(attributeName);
    }

    /**
     * @see {@link IH4Object#getNumAttributes()}
     */
    public int getNumAttributes() {
        return attributesHolder.getNumAttributes();
    }
}
