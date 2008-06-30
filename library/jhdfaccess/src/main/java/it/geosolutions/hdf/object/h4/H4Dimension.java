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
public class H4Dimension extends H4Variable implements IHObject {

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    /** predefined attributes */
    public static String PREDEF_ATTR_LABEL = "long_name";

    public static String PREDEF_ATTR_UNIT = "units";

    public static String PREDEF_ATTR_FORMAT = "format";

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
     *                 in case of wrong specified input parameters or in case
     *                 some initialization fails due to wrong identifiers or
     *                 related errors.
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
                numAttributes = dimInfo[2];
                init();

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
                throw new IllegalArgumentException(
                        "Failing to get an identifier for the Dimension");
            }

        } catch (HDFException e) {
            throw new IllegalArgumentException(
                    "HDFException occurred while creating a new H4Dimension", e);
        }
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
    public void dispose() {
        try {
            if (hasDimensionScaleSet) {
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
            }
        } catch (HDFException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Error closing access to the dimension with ID = "
                                + getIdentifier());
        }
        super.dispose();
    }

    /**
     * returns a <code>Map</code> containing all attributes associated to this
     * Dimension
     * 
     * @return the map of attributes.
     * @throws HDFException
     */
    synchronized Map getAttributes() throws HDFException {

        // Checking if I need to initialize attributes map
        if (attributes != null && attributes.size() < numAttributes) {
            // user defined attributes
            final String[] dimAttrName = new String[1];
            int nAttr = 0;
            for (int i = 0; i < numAttributes; i++) {
                dimAttrName[0] = "";
                // get various info about this attribute
                int[] dimAttrInfo = getAttributeInfo(i, dimAttrName);
                final String attrName = dimAttrName[0];

                // //
                //
                // The HDF user guide explicitly states that
                // Predefined Attributes for Dimensions need to be
                // read using specialized routines (SDgetdimstrs).
                // So, I will skip a found attribute if it is a
                // predefined one.
                //
                // //
                boolean isPredef = false;
                if (attrName.equals(PREDEF_ATTR_LABEL)
                        || attrName.equals(PREDEF_ATTR_UNIT)
                        || attrName.equals(PREDEF_ATTR_FORMAT))
                    isPredef = true;
                if (!isPredef) {
                    H4Attribute attrib =null;
                    if (dimAttrInfo != null) 
                        attrib = new H4Attribute(this, nAttr, attrName, dimAttrInfo);
                    if (attrib != null) {
                        attributes.put(attrName, attrib);
                        indexToAttributesMap.put(Integer.valueOf(nAttr),
                                attrib);
                        nAttr++;
                    } else {
                        throw new RuntimeException("Error while setting dimension attribute");
                    }
                }
            }

            // retrieving predefined attributes
            final String predefAttributesValues[] = { "NONE", "NONE", "NONE" };
            HDFLibrary.SDgetdimstrs(getIdentifier(), predefAttributesValues,
                    HDFConstants.DFS_MAXLEN);
            final String predefinedStrings[] = { PREDEF_ATTR_LABEL,
                    PREDEF_ATTR_UNIT, PREDEF_ATTR_FORMAT };

            for (int k = 0; k < 3; k++) {
                // getting predefined attribute value
                final String value = predefAttributesValues[k];
                if (value != null && value.trim().length() != 0) {

                    // predefined attribute found. Building a new
                    // H4Attribute
                    H4Attribute attrib = new H4Attribute(this, nAttr,
                            predefinedStrings[k], new int[] {
                                    HDFConstants.DFNT_CHAR8, value.length() },
                            value.getBytes());
                    if (attrib != null) {
                        attributes.put(predefinedStrings[k], attrib);
                        indexToAttributesMap
                                .put(Integer.valueOf(nAttr), attrib);
                        nAttr++;
                    }
                }
            }
        }
        return attributes;
    }

    /**
     * Returns a specific attribute of this Dimension, given its name.
     * 
     * @param attributeName
     *                the name of the required attribute
     * @return the {@link H4Attribute} related to the specified name.
     * @throws HDFException
     */
    public synchronized H4Attribute getAttribute(final String attributeName)
            throws HDFException {
        H4Attribute attribute = null;
        getAttributes();
        if (attributes != null && attributes.containsKey(attributeName))
            attribute = (H4Attribute) attributes.get(attributeName);
        return attribute;
    }

    /**
     * Returns a specific attribute of this Dimension, given its index.
     * 
     * @param attributeIndex
     *                the index of the required attribute
     * @return the {@link H4Attribute} related to the specified index.
     * @throws HDFException
     */
    public synchronized H4Attribute getAttribute(final int attributeIndex)
            throws HDFException {
        H4Attribute attribute = null;
        getAttributes();
        if (indexToAttributesMap != null
                && indexToAttributesMap.containsKey(Integer
                        .valueOf(attributeIndex)))
            attribute = (H4Attribute) indexToAttributesMap.get(Integer
                    .valueOf(attributeIndex));

        return attribute;
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

    protected boolean readAttribute(int index, Object values)
            throws HDFException {
        return HDFLibrary.SDreadattr(getIdentifier(), index, values);
    }

    protected int[] getAttributeInfo(int index, String[] attrName)
            throws HDFException {
        final int[] dimAttrInfo = { 0, 0 };
        boolean done = HDFLibrary.SDattrinfo(getIdentifier(), index, attrName,
                dimAttrInfo);
        if (done)
            return dimAttrInfo;
        else
            return null;

    }

    /**
     * Dimension has a different management of attributes. Therefore, this method isn't needed.
     */
    protected int findAttributeIndexByName(String attributeName)
            throws HDFException {
        throw new UnsupportedOperationException();
    }
}
