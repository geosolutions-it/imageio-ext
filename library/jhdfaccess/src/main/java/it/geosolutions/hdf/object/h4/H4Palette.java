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
 * Class representing a Palette.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4Palette extends AbstractHObject implements IH4ReferencedObject,
		IHObject {

	/**
	 * the datavalues of this palette. They will be loaded only when required.
	 */
	private byte[] values = null;

	/**
	 * the datatype of this palette
	 */
	private int datatype;

	/**
	 * The index of this palette within the parent image.
	 */
	private int index;

	/**
	 * The number of entries of this palette.
	 */

	private int numEntries;

	/**
	 * the interlace Mode associated to this palette <BR>
	 * Available values are:<BR>
	 * 
	 * HDFConstants.MFGR_INTERLACE_PIXEL<BR>
	 * HDFConstants.MFGR_INTERLACE_LINE<BR>
	 * HDFConstants.MFGR_INTERLACE_COMPONENT<BR>
	 */
	private int interlaceMode;

	/**
	 * the reference of this palette
	 */
	private H4ReferencedObject reference;

	/**
	 * the number of components of this palette
	 */
	private int numComponents;

	/**
	 * The {@link H4GRImage} owner.
	 */
	private H4GRImage grImage;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * getter of <code>index</code>
	 * 
	 * @return the index of this palette within the parent image.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * getter of <code>reference</code>
	 * 
	 * @return the reference of this palette
	 */
	public int getReference() {
		return reference.getReference();
	}

	/**
	 * getter of <code>numComponents</code>
	 * 
	 * @return the number of components of this palette
	 */
	public int getNumComponents() {
		return numComponents;
	}

	/**
	 * getter of <code>datatype</code>
	 * 
	 * @return the datatype of this palette
	 */
	public int getDatatype() {
		return datatype;
	}

	/**
	 * getter of <code>interlaceMode</code>
	 * 
	 * @return the interlace Mode associated to this palette
	 */
	public int getInterlaceMode() {
		return interlaceMode;
	}

	/**
	 * getter of <code>numEntries</code>
	 * 
	 * @return the number of entries of this palette.
	 */
	public int getNumEntries() {
		return numEntries;
	}

	/**
	 * getter of <code>grImage</code>
	 * 
	 * @return the {@link H4GRImage} owner.
	 */
	public H4GRImage getGrImage() {
		return grImage;
	}

	/**
	 * Constructor which builds a new <code>H4Palette</code> given its index
	 * in the image.<BR>
	 * 
	 * @param image
	 *            the parent image
	 * @param index
	 *            the index of the required palette
	 * 
	 */
	public H4Palette(H4GRImage image, final int index) {
		grImage = image;
		final int grID = grImage.getIdentifier();
		try {
			identifier = HDFLibrary.GRgetlutid(grID, index);
			if (identifier != HDFConstants.FAIL) {
				int lutInfo[] = new int[] { 0, 0, 0, 0 };

				// Getting palette information
				HDFLibrary.GRgetlutinfo(identifier, lutInfo);
				reference = new H4ReferencedObject(HDFLibrary
						.GRluttoref(identifier));
				numComponents = lutInfo[0];

				// palette datatype
				datatype = lutInfo[1] & (~HDFConstants.DFNT_LITEND);

				// palette interlaceMode
				interlaceMode = lutInfo[2];

				numEntries = lutInfo[3];
			}else{
				// XXX
			}

		} catch (HDFException e) {
			throw new RuntimeException ("HDFException occurred while creating a new H4Palette", e);
		}
	}

	/**
	 * return a byte array containing palette values.
	 * 
	 * @return
	 * @throws HDFException
	 */
	public byte[] getValues() throws HDFException {
		if (values == null) {
			HDFLibrary.GRreqlutil(identifier, interlaceMode);
			values = new byte[3 * numEntries];
			HDFLibrary.GRreadlut(identifier, values);
		}
		return values;

	}

	/**
	 * Method inherited from {@link AbstractHObject}.
	 */
	public void close() {
		if (identifier != HDFConstants.FAIL)
			identifier = HDFConstants.FAIL;
	}

}
