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
 * Class providing access to HDF General Raster Images.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4GRImageCollection extends H4DecoratedObject implements IHObject,
		List {

	private int[] mutex = new int[1];

	/**
	 * The list of {@link H4GRImage}s available by mean of this image
	 * collection
	 */
	private List grImagesList;

	/**
	 * Mapping between named images and related indexes in the list.
	 */
	private Map grImagesNamesToIndexes;

	/**
	 * The number of images available by means of this image collection
	 */
	private int numImages;

	/**
	 * the {@link H4File} to which this collection is attached
	 * 
	 * @uml.associationEnd inverse="h4GrImageCollection:it.geosolutions.hdf.object.h4.H4File"
	 */
	private H4File h4File;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the number of GR Images available in this collection
	 */
	public int size() {
		return numImages;
	}

	/**
	 * getter of <code>h4File</code>
	 * 
	 * @return the {@link H4File} to which this collection is attached
	 */
	H4File getH4File() {
		return h4File;
	}

	/**
	 * Constructor which builds and initialize a
	 * <code>H4GRImageCollection</code> given an input {@link H4File}.
	 * 
	 * @param h4file
	 *            the input {@link H4File}
	 * @throws IllegalArgumentException
	 *             in case some error occurs when accessing the File or when
	 *             accessing the GR Interface
	 */
	public H4GRImageCollection(H4File h4file) {
		h4File = h4file;
		final int fileID = h4File.getIdentifier();
		try {
			identifier = HDFLibrary.GRstart(fileID);
			if (identifier != HDFConstants.FAIL) {
				final int[] grFileInfo = new int[2];

				// Retrieving Information
				if (HDFLibrary.GRfileinfo(identifier, grFileInfo)) {
					numAttributes = grFileInfo[1];
					initDecorated();
					numImages = grFileInfo[0];
					grImagesList = new ArrayList(numImages);
					grImagesNamesToIndexes = Collections
							.synchronizedMap(new HashMap(numImages));
					for (int i = 0; i < numImages; i++) {
						// Initializing image list
						H4GRImage grImage = new H4GRImage(this, i);
						grImagesList.add(i, grImage);
						final String name = grImage.getName();
						grImagesNamesToIndexes.put(name, new Integer(i));
						// grImage.close();
					}
				} else {
					// the GRFileInfo operation has failed
					numImages = 0;
					grImagesList = null;
				}
			} else {
				// XXX
			}
		} catch (HDFException e) {
			throw new IllegalArgumentException(
					"HDFException occurred while accessing General Raster Images routines with file "
							+ h4file.getFilePath(), e);
		}
	}

	/**
	 * close this {@link H4GRImageCollection} and dispose allocated objects.
	 */
	public void dispose() {
		synchronized (mutex) {
			super.dispose();
			if (grImagesNamesToIndexes != null)
				grImagesNamesToIndexes.clear();
			close();
		}
	}

	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * End access to the underlying general raster interface and end access to
	 * the owned {@link AbstractHObject}s
	 */
	public void close() {
		synchronized (mutex) {
			try {
				if (grImagesList != null) {
					for (int i = 0; i < numImages; i++) {
						H4GRImage h4grImage = (H4GRImage) grImagesList.get(i);
						h4grImage.dispose();
					}
					grImagesList.clear();
				}
				if (identifier != HDFConstants.FAIL) {
					HDFLibrary.GRend(identifier);
					identifier = HDFConstants.FAIL;
				}
			} catch (HDFException e) {
				// XXX
			}
		}
	}

	/**
	 * Returns the {@link H4GRImage} related to the index-TH image. Prior to
	 * call this method, be sure that some images are available from this
	 * {@link H4GRImageCollection} by querying the
	 * {@link H4GRImageCollection#size()} method. otherwise an 
	 * <code>IndexOutOfBoundsException</code> will be thrown.
	 * 
	 * @param index
	 *            the index of the requested image.
	 * @return the requested {@link H4GRImage}
	 * @throws IndexOutOfBoundsException
	 */
	public Object get(final int index) {
		if (index > numImages || index < 0)
			throw new IndexOutOfBoundsException(
					"Specified index is not valid. It should be greater than zero and belower than "
							+ numImages);
		H4GRImage image = (H4GRImage) grImagesList.get(index);
		// image.open();
		return image;
	}

	/**
	 * Returns the {@link H4GRImage} having the name specified as input
	 * parameter, and open it. Prior to call this method, be sure that some
	 * images are available from this {@link H4GRImageCollection} by querying
	 * the {@link H4GRImageCollection#size()} method.
	 * 
	 * @param sName
	 *            the name of the requested image.
	 * @return the requested {@link H4GRImage} or <code>null</code> if the
	 *         specified image does not exist
	 */
	public H4GRImage get(final String sName) {
		H4GRImage grImage = null;
		if (grImagesNamesToIndexes.containsKey(sName)) {
			grImage = (H4GRImage) grImagesList
					.get(((Integer) grImagesNamesToIndexes.get(sName))
							.intValue());
			// grImage.open();
		}
		return grImage;
	}

	/**
	 * Returns <code>true</code> if this list contains no GR Images.
	 * 
	 * @return <code>true</code> if this list contains no GR Images.
	 */
	public boolean isEmpty() {
		return size() > 0 ? false : true;
	}

	/**
     * Returns an iterator over the GR Images in this list.
     *
     * @return an iterator.
     */
	public Iterator iterator() {
		return Collections.unmodifiableCollection(grImagesList).iterator();
	}
	
	/**
     * Returns a list iterator of the GR Images in this list.
     *
     * @return a list iterator.
     */
	public ListIterator listIterator() {
		return Collections.unmodifiableList(grImagesList).listIterator();
	}

	/**
     * Returns a list iterator of the GR Images in this list, starting at the 
     * specified position in this list
     *
     * @return a list iterator.
     */
	public ListIterator listIterator(int index) {
		return Collections.unmodifiableList(grImagesList).listIterator(index);
	}

	/**
     * Removes the first occurrence in this list of the specified element.
     * Since this method is actually unsupported, an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
     */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

    /**
     * Removes the element at the specified position in this list.
     * Since this method is actually unsupported, an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
     */
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	/** 
	 * Removes from the list all the elements.
	 * Since this method is actually unsupported, an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
     */ 
	public boolean removeAll(Collection arg0) {
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

	/** 
	 * This method is actually unsupported: an 
     * <code>UnsupportedOperationException</code> will be thrown.
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
	 * This method is actually unsupported: an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
	 */
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	/** 
	 * This method is actually unsupported: an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
	 */
	public Object[] toArray(Object[] arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Appends the specified element to the end of this list.
	 * Since this method is actually unsupported, an 
     * <code>UnsupportedOperationException</code> will be thrown.
     * @throws UnsupportedOperationException 
	 */
	public boolean add(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Since this method is actually unsupported, an 
    * <code>UnsupportedOperationException</code> will be thrown.
    * @throws UnsupportedOperationException 
	 */
	public void add(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Appends all of the elements contained in the specified collection to 
	 * the end of this list.
	 * Since this method is actually unsupported, an 
    * <code>UnsupportedOperationException</code> will be thrown.
    * @throws UnsupportedOperationException 
	 */
	public boolean addAll(Collection arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Inserts all of the elements contained in the specified collection at the 
	 * specified position in this list.
	 * Since this method is actually unsupported, an 
    * <code>UnsupportedOperationException</code> will be thrown.
    * @throws UnsupportedOperationException 
	 */
	public boolean addAll(int arg0, Collection arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns <code>true</code> if this list contains the specified GR Image.
	 */
	public boolean contains(Object image) {
		return grImagesList.contains(image);
	}

	/**
	 * Returns <code>true</code> if this list contains all of the elements of the
     * specified collection.
	 */
	public boolean containsAll(Collection collection) {
		return grImagesList.containsAll(collection);
	}

	/**
	 * Returns the index in this list of the first occurrence of the specified
     * GR Image, or -1 if this list does not contain it.
	 */
	public int indexOf(Object image) {
		return grImagesList.indexOf(image);
	}
	
	/**
	 * Returns the index in this list of the last occurrence of the specified
     * GR Image, or -1 if this list does not contain it.
	 */
	public int lastIndexOf(Object image) {
		return grImagesList.lastIndexOf(image);
	}
}
