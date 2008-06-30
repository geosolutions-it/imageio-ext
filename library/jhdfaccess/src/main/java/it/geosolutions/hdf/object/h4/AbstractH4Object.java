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

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;

/**
 * Class which need to be extended by any object which may have attached
 * attributes.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class AbstractH4Object extends AbstractHObject implements
        IHObject {

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    /**
     * The map of attributes related to this object
     */
    protected Map attributes;

    /**
     * An index to attributes map
     */
    protected Map indexToAttributesMap;

    /**
     * The number of attributes related to this object.
     */
    protected int numAttributes;

    /**
     * getter of <code>numAttributes</code>
     * 
     * @return the number of attributes associated to this object
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
    protected abstract int findAttributeIndexByName(String attributeName)
            throws HDFException;

    /**
     * Initialize attributes properties without attributes creation. 
     * Attributes will be lazily built when needed.
     */
    protected synchronized void init() {
        if (numAttributes != 0) {
            indexToAttributesMap = new HashMap(numAttributes);
            attributes = new HashMap(numAttributes);
        } else {
            attributes = null;
            indexToAttributesMap = null;
        }
    }

    /**
     * Returns a specific attribute of this object, given its name.
     * 
     * @param attributeName
     *                the name of the required attribute
     * @return the {@link H4Attribute} related to the specified name.
     * @throws HDFException
     */
    public synchronized H4Attribute getAttribute(final String attributeName)
            throws HDFException {
        H4Attribute attribute = null;

        // Here, the initialization has already occurred.
        if (attributes != null) {

            // Looking for the specified attribute name within the map
            if (!attributes.containsKey(attributeName)) {
                // The required attribute is not present. Searching for
                // this attribute by querying the interface
                int attributeIndex = findAttributeIndexByName(attributeName);

                if (attributeIndex != HDFConstants.FAIL) {
                    // I found a valid index for this Attribute.
                    // I build a new attribute
                    attribute = H4Attribute
                            .buildAttribute(this, attributeIndex);
                    if (attribute != null) {
                        // Putting the attribute in the maps
                        attributes.put(attributeName, attribute);
                        indexToAttributesMap.put(Integer
                                .valueOf(attributeIndex), attribute);
                    }
                }
            } else
                attribute = (H4Attribute) attributes.get(attributeName);
        }
        return attribute;
    }

    /**
     * returns a <code>Map</code> containing all attributes associated to this
     * object
     * 
     * @return the map of attributes.
     * @throws HDFException
     */
    synchronized Map getAttributes() throws HDFException {
        if (attributes != null && attributes.size() < numAttributes) {
            buildAttributesMaps();
        }
        return attributes;
    }

    /**
     * Builds the attributes maps by adding all not yet loaded attributes.
     * 
     * @throws HDFException
     */
    private void buildAttributesMaps() throws HDFException {
        assert Thread.holdsLock(this);
        // Fill the attributes map with the missing attributes
        for (int index = 0; index < numAttributes; index++) {
            if (!indexToAttributesMap.containsKey(Integer.valueOf(index))) {
                // An attribute with this index has not been found
                // in the map
                H4Attribute attribute = H4Attribute.buildAttribute(this, index);
                if (attribute != null) {
                    // Adding the new attribute to the map
                    indexToAttributesMap.put(Integer.valueOf(index), attribute);
                    attributes.put(attribute.getName(), attribute);
                }
            }
        }
    }

    /**
     * Returns a specific attribute of this object, given its index.
     * 
     * @param attributeIndex
     *                the index of the required attribute
     * @return the {@link H4Attribute} related to the specified index.
     * @throws HDFException
     */
    public synchronized H4Attribute getAttribute(final int attributeIndex)
            throws HDFException {
        checkAttributeIndex(attributeIndex);
        if (indexToAttributesMap != null
                && !indexToAttributesMap.containsKey(Integer
                        .valueOf(attributeIndex))) {
            // Usually, attributes will be accessed by name.
            // Attributes are instead accessed by index during an attribute
            // list scan. For this reason, if the required attribute is not
            // in the map, we provide to loading all the attributes.
            buildAttributesMaps();
        }
        return (H4Attribute) indexToAttributesMap.get(Integer
                .valueOf(attributeIndex));
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
                    "Specified index is not valid. It should be greater than zero and belower than "
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
            indexToAttributesMap.clear();
            indexToAttributesMap = null;
        }
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
