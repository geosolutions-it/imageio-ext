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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;

/**
 * Class representing an HDF attribute.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4Attribute {

    /**
     * The data contained within the attribute
     */
    private Object values;

    /**
     * The name of the attribute
     */
    private String name = "";

    /**
     * the size of the attribute
     */
    private int size;

    /**
     * The {@link AbstractH4Object} to which this attribute belongs.
     */
    private AbstractH4Object attributeOwner;

    /**
     * The index of the attribute.
     */
    private int index;

    /**
     * The datatype of the attribute.
     */
    private int datatype;

    /** This field is available only for attributes attached to VGroups */
    private int numValues = -1;

    /**
     * Builds a {@link H4Attribute}
     * 
     * @param object
     *                the {@link AbstractHObject} owner
     * @param i
     *                the index of the attribute
     * @param attrName
     *                the name of the attribute
     * @param attrInfo
     *                the array containing attribute info such as datatype and
     *                size. Attribute information retrieved by means of the
     *                VGroup interface, also returns the number of values.
     */
    public H4Attribute(AbstractH4Object object, int i, String attrName,int[] attrInfo) {
        this(object, i, attrName, attrInfo, null);
    }

    /**
     * Builds a {@link H4Attribute}
     * 
     * @param object
     *                the {@link AbstractHObject} owner
     * @param i
     *                the index of the attribute
     * @param attrName
     *                the name of the attribute
     * @param attrInfo
     *                the array containing attribute info such as datatype and
     *                size. Attribute information retrieved by means of the
     *                VGroup interface, also returns the number of values.
     * @param data
     *                an Object containing datavalues of this attribute.
     */
    public H4Attribute(AbstractH4Object object, int i, String attrName,int[] attrInfo, Object data) {
        int attrDatatype;
        if (attrInfo.length == 2) {
            attrDatatype = attrInfo[0];
            size = attrInfo[1];
            numValues = 1;
        } else {
            attrDatatype = attrInfo[0];
            numValues = attrInfo[1];
            size = attrInfo[2];
        }
        attributeOwner = object;
        index = i;
        name = attrName;
        datatype = attrDatatype & (~HDFConstants.DFNT_LITEND);
        values = data;
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////

    /**
     * getter of <code>name</code>
     * 
     * @return the name of the attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * getter of <code>index</code>
     * 
     * @return the index of the attribute.
     */
    public int getIndex() {
        return index;
    }

    /**
     * getter of <code>datatype</code>
     * 
     * @return the datatype of the attribute.
     */
    public int getDatatype() {
        return datatype;
    }

    /**
     * getter of <code>numValues</code>
     * 
     * @return the numValues.
     */
    public int getNumValues() {
        return numValues;
    }

    /**
     * getter of <code>size</code>
     * 
     * @return the size of the attribute.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns a proper <code>Object</code> containing attribute values. The
     * type of the returned object depends on the datatype of this attribute. As
     * an instance, for an attribute having <code>HDFConstants.DFNT_INT32</code>
     * as datatype, returned object is an <code>int</code> array. See
     * {@link H4Utilities#allocateArray(int, int)} to retrieve information about
     * the returned type.
     * 
     * @return an <code>Object</code> containing attribute values.
     * @throws HDFException
     */
    public synchronized Object getValues() throws HDFException {
        if (values == null) {
            values = H4Utilities.allocateArray(datatype, size);
            // //
            //
            // Reading the attribute, using the proper interface, depending on
            // the subclass of the object to which the attribute is attached
            //
            // //
            boolean done = attributeOwner.readAttribute(index, values);
            if (!done)
                values = null;
        }
        return values;
    }

    /**
     * Return attribute values as a <code>String</code>.
     * 
     * @return a <code>String</code> containing attribute values.
     */
    public String getValuesAsString() throws HDFException {
        return H4Utilities.getValuesAsString(datatype, getValues());
    }

    public synchronized void dispose() {
        numValues = -1;
        attributeOwner = null;
        name = null;
        index = -1;
        datatype = HDFConstants.FAIL;
        values = null;
    }
}
