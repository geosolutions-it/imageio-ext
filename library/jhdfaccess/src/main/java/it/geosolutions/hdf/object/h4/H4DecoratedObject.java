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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;
import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

/**
 * Class which need to be extended by any object which may have attached attributes.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class H4DecoratedObject extends AbstractHObject implements
		IHObject {

	private int[] mutex = new int[] { 1 };

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
	 * Initialize attributes properties.
	 */
	public void initDecorated() {
		synchronized (mutex) {
			if (numAttributes != 0) {
				indexToAttributesMap = Collections.synchronizedMap(new HashMap(
						numAttributes));
				attributes = Collections.synchronizedMap(new HashMap(
						numAttributes));
			} else {
				attributes = null;
				indexToAttributesMap = null;
			}
		}
	}

	/**
	 * Returns a specific attribute of this object, given its name.
	 * 
	 * @param attributeName
	 *            the name of the required attribute
	 * @return the {@link H4Attribute} related to the specified name.
	 * @throws HDFException
	 */
	public H4Attribute getAttribute(final String attributeName)
			throws HDFException {
		H4Attribute attribute = null;
		synchronized (mutex) {
			
			//Here, the initialization has already occurred.
			if (attributes != null) {
				
				// Looking for the specified attribute name within the map
				if (!attributes.containsKey(attributeName)) {
					// The required attribute is not present. Searching for
					// this attribute by querying the interface
					int attributeIndex = -1;
					if (this instanceof H4SDS
							|| this instanceof H4SDSCollection) {
						attributeIndex = HDFLibrary.SDfindattr(identifier,
								attributeName);
					} else if (this instanceof H4GRImage
							|| this instanceof H4GRImageCollection) {
						attributeIndex = HDFLibrary.GRfindattr(identifier,
								attributeName);
					} else if (this instanceof H4VGroup) {
						HDFLibrary.Vfindattr(identifier, attributeName);
					}
					if (attributeIndex != HDFConstants.FAIL) {
						// I found a valid index for this Attribute.
						// I build a new attribute
						attribute = H4Attribute.buildAttribute(this,
								attributeIndex);
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
	}

	/**
	 * returns a <code>Map</code> containing all attributes associated to this
	 * object
	 * 
	 * @return the map of attributes.
	 * @throws HDFException
	 */
	public Map getAttributes() throws HDFException {
		synchronized (mutex) {
			if (attributes != null && attributes.size() < numAttributes) {
				buildAttributesMaps();
			}
			return attributes;
		}
	}

	/**
	 * Builds the attributes maps by adding all not yet loaded attributes.
	 * 
	 * @throws HDFException
	 */
	private void buildAttributesMaps() throws HDFException {
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
	 *            the index of the required attribute
	 * @return the {@link H4Attribute} related to the specified index.
	 * @throws HDFException
	 */
	public H4Attribute getAttribute(final int attributeIndex)
			throws HDFException {
		checkAttributeIndex(attributeIndex);
		synchronized (mutex) {
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
	}

	/**
	 * checks if the specified attribute index is valid. An attribute index is
	 * not valid if there are no attributes <BR>
	 * (numAttributes = 0) or if the specified index exceed the number of
	 * attributes-1.
	 * 
	 * @param index
	 *            the index to be checked
	 * @param numAttributes
	 *            the number of attributes
	 * 
	 */
	private void checkAttributeIndex(final int index) {
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
	public void dispose(){
		synchronized (mutex) {
			if (attributes != null)
				attributes.clear();
			if (indexToAttributesMap!=null)
				attributes.clear();
		}
	}
	
	protected void finalize() throws Throwable {
		dispose();
	}
}
