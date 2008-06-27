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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a HDF VGroup.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4VGroup extends H4Variable implements IHObject {

	/**
	 * The list of TAG/REF couples referred by this VGroup
	 */
	private List tagRefList;

	/**
	 * the number of pairs TAG/REF referred by this group
	 */
	private int numObjects;

	/**
	 * the tag of this group
	 */
	private int tag;

	/**
	 * the reference of this group
	 */
	private H4ReferencedObject reference;

	/**
	 * the class name of this group
	 */
	private String className = "";

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * getter of <code>reference</code>
	 * 
	 * @return the reference of this group.
	 */
	int getReference() {
		return reference.getReference();
	}

	/**
	 * getter of <code>tag</code>
	 * 
	 * @return the tag of this group
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * getter of <code>className</code>
	 * 
	 * @return the class name of this group
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * getter of <code>numObjects</code>
	 * 
	 * @return the number of pairs TAG/REF referred by this group
	 */
	public int getNumObjects() {
		return numObjects;
	}

	/**
	 * getter of <code>h4VGroupCollectionOwner</code>
	 * 
	 * @return the {@link H4VGroupCollection} to which this {@link H4VGroup}
	 *         belongs.
	 */
	H4VGroupCollection getH4VGroupCollectionOwner() {
		return h4VGroupCollectionOwner;
	}

	/**
	 * The {@link H4VGroupCollection} to which this {@link H4VGroup} belongs.
	 * 
	 * @uml.associationEnd inverse="H4VGroupCollection:it.geosolutions.hdf.object.h4.H4VGroupCollection"
	 */
	H4VGroupCollection h4VGroupCollectionOwner = null;

	/**
	 * Main Constructor which builds a <code>H4Vgroup</code> given its ref.
	 * This constructor is called by the {@link H4VGroupCollection} during the
	 * initialization fase. After this call, the {@link H4VGroupCollection}
	 * check if the built object is a real VGroup. If affermative, it call the
	 * {@link #init()} method.
	 * 
	 * @param h4VgroupCollection
	 *            the parent collection
	 * @param ref
	 *            the reference of this VGroup
	 */
	public H4VGroup(H4VGroupCollection h4VgroupCollection, final int ref) {
		h4VGroupCollectionOwner = h4VgroupCollection;
		final int fileID = h4VGroupCollectionOwner.getH4File().getIdentifier();
		try {
			reference = new H4ReferencedObject(ref);
			int identifier = HDFLibrary.Vattach(fileID, ref, "r");
			if (identifier != HDFConstants.FAIL) {
				setIdentifier(identifier);
				final String[] vgroupClass = { "" };
				HDFLibrary.Vgetclass(identifier, vgroupClass);
				className = vgroupClass[0];
				// NOTE that this version does not call the init method since we
				// need to check if the just built object is a vgroup
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new RuntimeException(
					"HDFException occurred while creating a new H4VGroup instance ",
					e);
		}
	}

	/**
	 * Constructor which builds a new <code>H4Vgroup</code> starting from a
	 * parent one.
	 * 
	 * @param parentGroup
	 *            the parent VGroup
	 * @param ref
	 *            the reference of the needed VGroup
	 * 
	 */
	public H4VGroup(H4VGroup parentGroup, final int ref) {
		try {
			this.h4VGroupCollectionOwner = parentGroup.h4VGroupCollectionOwner;
			final int fileID = h4VGroupCollectionOwner.getH4File()
					.getIdentifier();
			reference = new H4ReferencedObject(ref);
			int identifier = HDFLibrary.Vattach(fileID, ref, "r");
			if (identifier != HDFConstants.FAIL) {
				setIdentifier(identifier);
				final String[] vgroupClass = { "" };
				HDFLibrary.Vgetclass(identifier, vgroupClass);
				className = vgroupClass[0];
				initialize();
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new RuntimeException(
					"HDFException occurred while creating a new H4VGroup instance ",
					e);
		}
	}

	/**
	 * Initializes the VGroup
	 * 
	 * @throws HDFException
	 */
	public void initialize() throws HDFException {
		final String[] vgroupName = { "" };
		int identifier=getIdentifier();
		HDFLibrary.Vgetname(identifier, vgroupName);
		setName(vgroupName[0]);
		tag = HDFLibrary.VQuerytag(identifier);
		numObjects = HDFLibrary.Vntagrefs(identifier);
		numAttributes = HDFLibrary.Vnattrs(identifier);
		init();
	}

	protected void finalize() throws Throwable {
		try {
			dispose();
		} catch (Throwable t) {
			// TODO: handle exception
		}
	}

	/**
	 * close this {@link H4VGroup} and dispose allocated objects.
	 */
	public synchronized void dispose() {
		try {
			int identifier=getIdentifier();
			if (identifier != HDFConstants.FAIL) {
				HDFLibrary.Vdetach(identifier);
			}
		} catch (HDFException e) {
			// XXX
		}
		super.dispose();		
	}


	/**
	 * Returns a <code>List</code> containing TAG/REF couples referred by this
	 * VGroup. A TAG/REF couple is stored as an int array having 2 as size.
	 * 
	 * @return a <code>List</code> of TAG/REF couples.
	 */
	public synchronized List getTagRefList() throws HDFException {
		if (tagRefList == null) {
			tagRefList = new ArrayList(numObjects);
			for (int i = 0; i < numObjects; i++) {
				final int tagRef[] = { 0, 0 };
				HDFLibrary.Vgettagref(getIdentifier(), i, tagRef);
				tagRefList.add(i, tagRef);
			}
		}
		return Collections.unmodifiableList(tagRefList);
	}

}
