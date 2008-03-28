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
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a HDF annotation.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4Annotation extends AbstractHObject implements IH4ReferencedObject, IHObject{

	/** Annotation types as Strings */
	/** Data Object Label */
	public final static String AN_DATA_LABEL = "Data Object Label";

	/** Data Object Description */
	public final static String AN_DATA_DESC = "Data Object Description";

	/** File Label */
	public final static String AN_FILE_LABEL = "File Label";

	/** File Description */
	public final static String AN_FILE_DESC = "File Description";

	/** Unrecognized Annotation Type*/
	public final static String AN_UNDEFINED = "WARNING!!!";

	/**
	 * Returns a <code>String</code> representing the type of annotation
	 * specified as input.
	 * 
	 * @param annotationType
	 *            the annotation type
	 * @return the Annotation Type as <code>String</code>
	 */
	public static String getAnnotationTypeString(final int annotationType) {
		switch (annotationType) {
		case HDFConstants.AN_DATA_LABEL:
			return AN_DATA_LABEL;
		case HDFConstants.AN_DATA_DESC:
			return AN_DATA_DESC;
		case HDFConstants.AN_FILE_LABEL:
			return AN_FILE_LABEL;
		case HDFConstants.AN_FILE_DESC:
			return AN_FILE_DESC;
		default:
			return AN_UNDEFINED;
		}
	}

	/**
	 * The <code>String</code> holding the content of this annotations
	 * 
	 * @uml.property name="content"
	 */
	private String content;

	/**
	 * the type of this annotation which is one of:<BR>
	 * <code>HDFConstants.AN_DATA_LABEL</code><BR>
	 * <code>HDFConstants.AN_DATA_DESC</code><BR>
	 * <code>HDFConstants.AN_FILE_LABEL</code><BR>
	 * <code>HDFConstants.AN_FILE_DESC</code><BR>
	 * 
	 * @uml.property name="type"
	 */
	private int type;

	/**
	 * the tag of this annotation
	 * 
	 * @uml.property name="tag"
	 */
	private int tag;

	/**
	 * the reference of this annotation
	 * 
	 * @uml.property name="reference"
	 */
	private H4ReferencedObject reference;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Getter of the property <code>reference</code>
	 * 
	 * @return the reference of this annotation.
	 * @uml.property name="reference"
	 */
	public int getReference() {
		return reference.getReference();
	}

	/**
	 * Getter of the property <code>tag</code>
	 * 
	 * @return the tag of this anntoation.
	 * @uml.property name="tag"
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * Getter of the property <code>content</code>
	 * 
	 * @return the content of this annotation.
	 * @uml.property name="content"
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Getter of the property <code>type</code>
	 * 
	 * @return the type of this annotation.
	 * @uml.property name="type"
	 */
	public int getType() {
		return type;
	}

	/**
	 * End access to this annotation.
	 */
	public void close() {
		try {
			if (identifier != HDFConstants.FAIL){
				HDFLibrary.ANendaccess(identifier);
				identifier=HDFConstants.FAIL;
			}
		} catch (HDFException e) {
			// XXX
		}
	}

	/**
	 * Constructor. Builds a {@link H4Annotation} given the input annotation
	 * identifier.
	 * 
	 * @param anIdentifier
	 *            the identifier of the required annotation.
	 * @throws HDFException
	 */
	public H4Annotation(int anIdentifier) throws HDFException {
		identifier = anIdentifier;
		short tagRef[] = new short[] { -1, -1 };
		HDFLibrary.ANid2tagref(identifier, tagRef);
		tag = tagRef[0];
		reference = new H4ReferencedObject(tagRef[1]);
		type = HDFLibrary.ANtag2atype((short) tag);
		final int annLength = HDFLibrary.ANannlen(identifier);
		String annBuf[] = new String[] { "" };
		HDFLibrary.ANreadann(identifier, annBuf, annLength);
		content = annBuf[0];
	}
}
