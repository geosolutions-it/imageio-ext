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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF Scientific Data Sets.
 * 
 * @author Romagnoli Daniele
 */
public class H4SDSCollection extends H4DecoratedObject implements IHObject {

	// /**
	// * A list containing sds sizes where sds size is the product of the sizes
	// of
	// * the dimensions different from x and y
	// */
	// private List sdsSizesList;

	private int[] mutex = new int[1];

	/**
	 * A map which allows to retrieve the index of a SDS within the SDS
	 * Collection, given its name. <BR>
	 * Couples of this map are <key=name, value=indexInList>.<BR>
	 * 
	 * It is worth to point out that SDS names do not have to be unique within a
	 * file. For this reason, if you need to retrieve a SDS with a name equals
	 * to the name of another SDS, you should specify the required SDS by index
	 * since keys in map are all differents. <BR>
	 * 
	 * @uml.property name="sdsNamesToIndexes"
	 */
	private Map sdsNamesToIndexes;

	/**
	 * The list of {@link H4SDS} available by mean of this SDS collection
	 * 
	 * @uml.property name="sdsList"
	 * @uml.associationEnd inverse="h4SdsCollectionOwner:it.geosolutions.hdf.object.h4.H4SDS"
	 */
	private List sdsList;

	/**
	 * the number of Scientific DataSets available by mean of this SDS
	 * collection
	 * 
	 * @uml.property name="numSDS"
	 */
	private int numSDS = 0;

	/**
	 * the {@link H4File} to which this collection is attached
	 * 
	 * @uml.property name="h4File"
	 * @uml.associationEnd inverse="h4SdsCollection:it.geosolutions.hdf.object.h4.H4File"
	 */
	private H4File h4File;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Getter of the property <code>numSDS</code>
	 * 
	 * @return the number of Scientific DataSets available by mean of this SDS
	 *         collection
	 * @uml.property name="numSDS"
	 */
	public int getNumSDS() {
		return numSDS;
	}

	// public List getSdsSizesList() {
	// return sdsSizesList;
	// }

	public Map getSdsNamesToIndexes() {
		return sdsNamesToIndexes;
	}

	/**
	 * Getter of the property <code>h4File</code>
	 * 
	 * @return Returns the h4File.
	 * @uml.property name="h4File"
	 */
	public H4File getH4File() {
		return h4File;
	}

	/**
	 * Returns the {@link H4SDS} related to the index-TH sds available in this
	 * collection and open it. Prior to call this method, be sure that some sds
	 * are available from this {@link H4SDSCollection} by querying the
	 * {@link H4SDSCollection#getNumSDS()} method.
	 * 
	 * @param index
	 *            the index of the requested sds.
	 * @return the requested {@link H4SDS}
	 */
	public H4SDS getH4SDS(final int index) {
		if (index > numSDS || index < 0)
			throw new IndexOutOfBoundsException(
					"Specified index is not valid. It should be greater than zero and belower than "
							+ numSDS);
		H4SDS sds = (H4SDS) sdsList.get(index);
		if (sds != null) {
			sds.open();
			return sds;
		}
		return null;
	}

	/**
	 * Returns the {@link H4SDS} having the name specified as input parameter,
	 * and open it. Prior to call this method, be sure that some sds are
	 * available from this {@link H4SDSCollection} by querying the
	 * {@link H4SDSCollection#getNumSDS()} method.
	 * 
	 * @param sName
	 *            the name of the requested sds.
	 * @return the requested {@link H4SDS} or <code>null</code> if the
	 *         specified sds does not exist
	 */
	public H4SDS getH4SDS(final String sName) {
		H4SDS sds = null;
		if (sdsNamesToIndexes.containsKey(sName)) {
			sds = (H4SDS) sdsList.get(((Integer) sdsNamesToIndexes.get(sName))
					.intValue());
			sds.open();
		}
		return sds;
	}

	/**
	 * Constructor which builds and initialize a <code>H4SDSCollection</code>
	 * given an input {@link H4File}.
	 * 
	 * @param h4file
	 *            the input {@link H4File}
	 * @throws IllegalArgumentException
	 *             in case some error occurs when accessing the File or when
	 *             accessing the SDS interface           
	 */
	public H4SDSCollection(H4File h4file) {
		this.h4File = h4file;
		final String filePath = h4file.getFilePath();
		try {
			identifier = HDFLibrary.SDstart(filePath, HDFConstants.DFACC_RDONLY
					| HDFConstants.DFACC_PARALLEL);
			if (identifier != HDFConstants.FAIL) {

				final int[] sdsFileInfo = new int[2];
				if (HDFLibrary.SDfileinfo(identifier, sdsFileInfo)) {
					numAttributes = sdsFileInfo[1];
					initDecorated();
					// retrieving the total num of SDS. It is worth to point out
					// that this number includes the SDS related to dimension
					// scales which will not treated as SDS. For this reason,
					// the
					// effective number of SDS could be lower
					final int sdsTotalNum = sdsFileInfo[0];

					sdsList = new ArrayList(sdsTotalNum);
					// sdsSizesList = new ArrayList(sdsTotalNum);
					sdsNamesToIndexes = Collections
							.synchronizedMap(new HashMap(sdsTotalNum));
					for (int i = 0; i < sdsTotalNum; i++) {
						H4SDS candidateSds = H4SDS.buildH4SDS(this, i);
						if (candidateSds != null) {
							sdsList.add(numSDS, candidateSds);
							final String name = candidateSds.getName();
							sdsNamesToIndexes.put(name, new Integer(numSDS));
							numSDS++;
							candidateSds.close();
						}

						// H4SDS sds = new H4SDS(this, i);
						// final boolean isDimensionScale = candidateSds
						// .isDimensionScale();
						// // SDS will be created and immediatly closed.
						// // It will be re-opened only when required.
						// if (!isDimensionScale) {
						// sds.init();
						// sdsList.add(numSDS, sds);
						// final String name = sds.getName();
						// sdsNamesToIndexes.put(name, new Integer(numSDS));
						// // sdsSizesList.add(numSDS, Integer
						// // .valueOf(sds.getDatasetSize()));
						// numSDS++;
						// }

					}
				}
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new IllegalArgumentException(
					"HDFException occurred while accessing SDS routines with file "
							+ h4file.getFilePath(), e);
		}
	}
	
	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * close this {@link H4SDSCollection} and dispose allocated objects.
	 */
	public void dispose() {
		synchronized (mutex) {
			super.dispose();
			if (sdsNamesToIndexes != null)
				sdsNamesToIndexes.clear();
			close();
		}
	}

	/**
	 * End access to the underlying SD interface and end access to the owned
	 * {@link AbstractHObject}s
	 */
	public void close() {
		try {
			if (sdsList != null) {
				for (int i = 0; i < numSDS; i++) {
					H4SDS h4sds = (H4SDS) sdsList.get(i);
					h4sds.dispose();
				}
			}
			if (identifier != HDFConstants.FAIL) {
				HDFLibrary.SDend(identifier);
				identifier = HDFConstants.FAIL;
			}
		} catch (HDFException e) {
			// XXX
		}
	}
}
