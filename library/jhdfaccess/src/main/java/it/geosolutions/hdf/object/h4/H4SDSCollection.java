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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF Scientific Data Sets.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4SDSCollection extends H4DecoratedObject implements IHObject,
		List {

	// /**
	// * A list containing sds sizes where sds size is the product of the sizes
	// of
	// * the dimensions different from x and y
	// */
	// private List sdsSizesList;

//	private int[] mutex = new int[1];

	/**
	 * A map which allows to retrieve the index of a SDS within the SDS
	 * Collection, given its name. <BR>
	 * Couples of this map are <key=name, value=indexInList>.<BR>
	 * 
	 * It is worth to point out that SDS names do not have to be unique within a
	 * file. For this reason, if you need to retrieve a SDS with a name equals
	 * to the name of another SDS, you should specify the required SDS by index
	 * since keys in map are all different. <BR>
	 */
	private Map sdsNamesToIndexes;

	/**
	 * The list of {@link H4SDS} available by mean of this SDS collection
	 * @uml.associationEnd inverse="h4SdsCollectionOwner:it.geosolutions.hdf.object.h4.H4SDS"
	 */
	private List sdsList;

	/**
	 * the number of Scientific DataSets available by mean of this SDS
	 * collection
	 */
	private int numSDS = 0;

	/** 
	 * the {@link H4File} to which this collection is attached
	 * @uml.associationEnd inverse="h4SdsCollection:it.geosolutions.hdf.object.h4.H4File"
	 */
	private H4File h4File;

	/**
	 * Returns the number of SDS available in this collection
	 */
	public int size() {
		return numSDS;
	}

	// public List getSdsSizesList() {
	// return sdsSizesList;
	// }

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////
	
	/** 
	 * getter of <code>h4File</code>
	 * @return Returns the h4File.
	 */
	public H4File getH4File() {
		return h4File;
	}

	/**
	 * Returns the {@link H4SDS} related to the index-TH sds available in this
	 * collection and open it. Prior to call this method, be sure that some sds
	 * are available from this {@link H4SDSCollection} by querying the
	 * {@link H4SDSCollection#size()} method, otherwise an
	 * <code>IndexOutOfBoundsException</code> will be thrown.
	 * 
	 * @param index
	 *            the index of the requested sds.
	 * @return the requested {@link H4SDS}
	 * @throws IndexOutOfBoundsException
	 */
	public Object get(final int index) {
		if (index > numSDS || index < 0)
			throw new IndexOutOfBoundsException(
					"Specified index is not valid. It should be greater than zero and belower than "
							+ numSDS);
		H4SDS sds = (H4SDS) sdsList.get(index);
		// if (sds != null) {
		sds.open();
		return sds;
		// }
		// else
		// throw new IllegalArgumentException ("The requested object is
		// unavailable");
	}

	/**
	 * Returns the {@link H4SDS} having the name specified as input parameter,
	 * and open it. Prior to call this method, be sure that some sds are
	 * available from this {@link H4SDSCollection} by querying the
	 * {@link H4SDSCollection#size()} method.
	 * 
	 * @param sName
	 *            the name of the requested sds.
	 * @return the requested {@link H4SDS} or <code>null</code> if the
	 *         specified sds does not exist
	 */
	public H4SDS get(final String sName) {
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
					// retrieving the total # of SDS. It is worth to point out
					// that this number includes the SDS related to dimension
					// scales which will not treated as SDS. For this reason,
					// the effective number of SDS could be lower
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
						// // SDS will be created and immediately closed.
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
	public synchronized void dispose() {
//		synchronized (mutex) {
			super.dispose();
			if (sdsNamesToIndexes != null)
				sdsNamesToIndexes.clear();
			close();
//		}
	}

	/**
	 * End access to the underlying SD interface and end access to the owned
	 * {@link AbstractHObject}s
	 */
	public synchronized void close() {
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

	/**
	 * Appends the specified element to the end of this list. Since this method
	 * is actually unsupported, an <code>UnsupportedOperationException</code>
	 * will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean add(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Since this method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void add(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Appends all of the elements contained in the specified collection to the
	 * end of this list. Since this method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Inserts all of the elements contained in the specified collection at the
	 * specified position in this list. Since this method is actually
	 * unsupported, an <code>UnsupportedOperationException</code> will be
	 * thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(int arg0, Collection arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns <code>true</code> if this list contains the specified SDS.
	 */
	public boolean contains(Object sds) {
		return sdsList.contains(sds);
	}

	/**
	 * Returns <code>true</code> if this list contains all of the elements of
	 * the specified collection.
	 */
	public boolean containsAll(Collection collection) {
		return sdsList.containsAll(collection);
	}

	/**
	 * Returns the index in this list of the first occurrence of the specified
	 * SDS, or -1 if this list does not contain it.
	 */
	public int indexOf(Object sds) {
		return sdsList.indexOf(sds);
	}

	/**
	 * Returns the index in this list of the last occurrence of the specified
     * SDS, or -1 if this list does not contain it.
	 */
	public int lastIndexOf(Object sds) {
		return sdsList.lastIndexOf(sds);
	}
	
	/**
	 * Returns <code>true</code> if this list contains no SDS.
	 * 
	 * @return <code>true</code> if this list contains no SDS.
	 */
	public boolean isEmpty() {
		return size() > 0 ? false : true;
	}
	
	/**
	 * Returns an iterator over the SDSs in this list.
	 * 
	 * @return an iterator.
	 */
	public Iterator iterator() {
		return Collections.unmodifiableCollection(sdsList).iterator();
	}

	/**
	 * Returns a list iterator of the SDSs in this list.
	 * 
	 * @return a list iterator.
	 */
	public ListIterator listIterator() {
		return Collections.unmodifiableList(sdsList).listIterator();
	}

	/**
	 * Returns a list iterator of the SDSs in this list, starting at the
	 * specified position in this list
	 * 
	 * @return a list iterator.
	 */
	public ListIterator listIterator(int index) {
		return Collections.unmodifiableList(sdsList).listIterator(index);
	}

	/**
	 * Removes the first occurrence in this list of the specified element. Since
	 * this method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes the element at the specified position in this list. Since this
	 * method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes from the list all the elements. Since this method is actually
	 * unsupported, an <code>UnsupportedOperationException</code> will be
	 * thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Object set(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}
	
	/** 
	 * This method is actually unsupported: an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
	 */
	public void clear() {
		throw new UnsupportedOperationException();	
	}
	
	/** 
	 * This method is actually unsupported: an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
	 */
	public List subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is actually unsupported, an
	 * <code>UnsupportedOperationException</code> will be thrown.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public Object[] toArray(Object[] arg0) {
		throw new UnsupportedOperationException();
	}
	
	/** 
	 * Removes from the list all the elements that are not contained in the
	 * specified collection.
	 * Since this method is actually unsupported, an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
     */ 
	public boolean retainAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}
}
