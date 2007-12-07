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

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF Lone Vgroups.
 * 
 * @author Romagnoli Daniele
 */
public class H4VGroupCollection extends AbstractHObject implements IHObject {

	/**
	 * the number of lone vgroups of this Group collection
	 * 
	 * @uml.property name="numLoneVgroups"
	 */
	private int numLoneVgroups = 0;

	/**
	 * The list of lone Vgroups of this Group collection
	 */
	private List loneVgroups;

	/**
	 * {@link H4File} to which this collection is attached
	 * 
	 * @uml.property name="h4File"
	 * @uml.associationEnd inverse="h4VgroupCollection:it.geosolutions.hdf.object.h4.H4File"
	 */
	private H4File h4File;

	/**
	 * Getter of the property <code>numLoneVgroups</code>
	 * 
	 * @return the number of lone vgroups of this Group collection.
	 * @uml.property name="numLoneVgroups"
	 */
	public int getNumLoneVgroups() {
		return numLoneVgroups;
	}

	/**
	 * Getter of the property <code>h4File</code>
	 * 
	 * @return the {@link H4File} to which this collection is attached.
	 * @uml.property name="h4File"
	 */
	public H4File getH4File() {
		return h4File;
	}

	/**
	 * Constructor which builds and initialize a <code>H4VGroupCollection</code>
	 * given an input {@link H4File}.
	 * 
	 * @param h4file
	 *            the input {@link H4File}
	 * 
	 * @throws IllegalArgumentException
	 *             in case some error occurs when accessing the File or when
	 *             accessing the VGroup interface
	 */
	public H4VGroupCollection(H4File h4file) {
		h4File = h4file;
		final int fileID = h4File.getIdentifier();
		try {
			if (HDFLibrary.Vstart(fileID)) {
				final int nLoneVgroups = HDFLibrary.Vlone(fileID, null, 0);

				loneVgroups = new ArrayList(nLoneVgroups);
				final int[] referencesArray = new int[nLoneVgroups];
				HDFLibrary.Vlone(fileID, referencesArray, nLoneVgroups);
				for (int loneVgroupIndex = 0; loneVgroupIndex < nLoneVgroups; loneVgroupIndex++) {
					// Attach to the current vgroup then get and display its
					// name and class. Only Real VGroups will be attached

					// Note: the current vgroup must be detached
					// before moving to the next.
					H4VGroup vgroup = new H4VGroup(this,
							referencesArray[loneVgroupIndex]);
					if (vgroup.isAVGroupClass()) {
						vgroup.init();
						loneVgroups.add(numLoneVgroups++, vgroup);
					} else
						vgroup.close();
				}
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new IllegalArgumentException(
					"HDFException occurred while accessing VGroup routines with file "
							+ h4file.getFilePath(), e);
		}
	}

	/**
	 * Returns the {@link H4VGroup} related to the index-TH group. Prior to call
	 * this method, be sure that some lone vgroups are available from this
	 * {@link H4VGroupCollection} by querying the
	 * {@link H4VGroupCollection#getNumLoneVgroups()} method.
	 * 
	 * @param index
	 *            the index of the required vgroup.
	 * @return the requested {@link H4VGroup}
	 */
	public H4VGroup getH4VGroup(int index) {
		if (index > numLoneVgroups || index < 0)
			throw new IndexOutOfBoundsException(
					"Specified index is not valid. It should be greater than zero and belower than "
							+ numLoneVgroups);
		H4VGroup group = (H4VGroup) loneVgroups.get(index);
		return group;
	}

	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * close this {@link H4VGroupCollection} and dispose allocated objects.
	 */
	public void dispose() {
		close();
	}

	/**
	 * End access to the underlying VGroup interface and end access to the owned
	 * {@link AbstractHObject}s
	 */
	public void close() {
		try {
			if (loneVgroups != null) {
				for (int i = 0; i < numLoneVgroups; i++) {
					H4VGroup group = (H4VGroup) loneVgroups.get(i);
					group.dispose();
				}
			}
			if (identifier != HDFConstants.FAIL) {
				HDFLibrary.Vend(identifier);
				identifier = HDFConstants.FAIL;
			}

		} catch (HDFException e) {
			// XXX
		}
	}
}
