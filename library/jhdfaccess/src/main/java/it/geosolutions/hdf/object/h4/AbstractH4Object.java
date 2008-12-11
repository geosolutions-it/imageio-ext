/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFException;

/**
 * Class which need to be extended by any object which may have attached
 * attributes.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class AbstractH4Object extends AbstractHObject implements
        IHObject, IH4Object {

    protected AbstractH4Object(final int identifier, final int numAttributes) {
        super(identifier);
        this.numAttributes = numAttributes;
        attributes = new HashMap(numAttributes);
        indexToAttributesMap = new HashMap(numAttributes);
    }

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    /**
     * The map of attributes related to this object
     */
    private Map attributes;

    /**
     * An index to attributes map
     */
    private Map indexToAttributesMap;

    /**
     * The number of attributes related to this object.
     */
    private int numAttributes;

    /**
     * @see it.geosolutions.hdf.object.h4.IH4Object#getNumAttributes()
     */
    public int getNumAttributes() {
        return numAttributes;
    }

    /**
     * Get the attribute info for the attribute related to the specified index
     * and store its name in the attrName parameter.
     * 
     * @param index
     *                the index of the required attribute
     * @param attrName
     *                will contains the name of the required attribute
     * @return an array of attribute info (attribute datatype, size and values)
     *         or <code>null</code> if getting info is impossible.
     * @throws HDFException
     */
    protected abstract int[] getAttributeInfo(int index, String[] attrName)
            throws HDFException;

    /**
     * Get the values of the attribute specified by the <code>index</code>
     * parameter and store them in <code>values</code> parameter which need to
     * be preemptively build with a proper datatype array. see
     * {@link H4Utilities#allocateArray(int, int)}
     * 
     * @param index
     *                the index of the required attribute
     * @param values
     *                the object to be filled with the attribute values.
     * @return <code>true</code> in case of successfully attribute read.
     * @throws HDFException
     */
    protected abstract boolean readAttribute(int index, Object values)
            throws HDFException;

    /**
     * Return the attribute index given the required attribute name.
     * 
     * @param attributeName
     *                the attribute name
     * @return the index of the required attribute
     */
    protected abstract int getAttributeIndexByName(String attributeName)
            throws HDFException;

    /**
     * @see it.geosolutions.hdf.object.h4.IH4Object#getAttribute(java.lang.String)
     */
    public synchronized H4Attribute getAttribute(final String attributeName)
            throws HDFException {
        if (attributeName == null)
            throw new IllegalArgumentException("Null attribute name provided");
        H4Attribute attribute = null;
        if (attributes != null) {
            // Looking for the specified attribute name within the map
            if (!attributes.containsKey(attributeName)) {
                // The required attribute is not present. Searching for
                // this attribute by querying the interface
                int attributeIndex = getAttributeIndexByName(attributeName);
                if (attributeIndex!=-1)
                    return getAttribute(attributeIndex);
            } else
                attribute = (H4Attribute) attributes.get(attributeName);
        }
        return attribute;
    }

    /**
     * @see it.geosolutions.hdf.object.h4.IH4Object#getAttribute(int)
     */
    public synchronized H4Attribute getAttribute(final int attributeIndex)
            throws HDFException {
        checkAttributeIndex(attributeIndex);
        H4Attribute attribute = null;
        Integer index = Integer.valueOf(attributeIndex);
        if (!indexToAttributesMap.containsKey(index)) {
            // Usually, attributes will be accessed by name.
            // Attributes are instead accessed by index during an attribute
            // list scan. For this reason, if the required attribute is not
            // in the map, we provide to loading all the attributes.
            attribute = getAttributeByIndex(attributeIndex);
            if (attribute != null) {
                // Adding the new attribute to the map
                indexToAttributesMap.put(index, attribute);
                attributes.put(attribute.getName(), attribute);
            }
        }
        if (indexToAttributesMap.containsKey(index)) {
            attribute = (H4Attribute) indexToAttributesMap.get(index);
        }
        return attribute;
    }

    protected H4Attribute getAttributeByIndex(int attributeIndex)
            throws HDFException {
        return H4Utilities.buildAttribute(this, attributeIndex);
    }

    /**
     * checks if the specified attribute index is valid. An attribute index is
     * not valid if there are no attributes <BR>
     * (numAttributes = 0) or if the specified index exceed the number of
     * attributes-1.
     * 
     * @param index
     *                the index to be checked
     * @param numAttributes
     *                the number of attributes
     * 
     */
    private void checkAttributeIndex(final int index) {
        assert Thread.holdsLock(this);
        if (numAttributes == 0)
            throw new IllegalArgumentException("No available attributes");
        else if (index >= numAttributes || index < 0)
            throw new IndexOutOfBoundsException(
                    "Specified index is not valid: "
                            + index
                            + "\nIt should be greater than zero and belower than "
                            + numAttributes);
    }

    /**
     * Clear the attributes mappings.
     */
    public synchronized void dispose() {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.log(Level.FINE, "disposing object");
        if (attributes != null) {
            attributes.clear();
            attributes = null;
        }
        if (indexToAttributesMap != null) {
            for (int i = 0; i < numAttributes; i++) {
                Integer intIndex = Integer.valueOf(i);
                if (indexToAttributesMap.containsKey(intIndex)) {
                    H4Attribute attrib = (H4Attribute) indexToAttributesMap
                            .get(intIndex);
                    if (attrib != null) {
                        attrib.dispose();
                        attrib = null;
                    }
                }
            }
            indexToAttributesMap.clear();
            indexToAttributesMap = null;
        }
        numAttributes = -1;
        super.dispose();
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during finalization: "
                                + e.getLocalizedMessage());
        }
    }
}
