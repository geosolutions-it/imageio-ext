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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF General Raster Images.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4GRImageCollection extends AbstractH4Object implements IHObject,
        List {

    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    private class H4GRImageCollectionIterator implements Iterator {

        private Iterator it;

        public boolean hasNext() {
            return it.hasNext();
        }

        public Object next() {
            return it.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();

        }

        public H4GRImageCollectionIterator(Iterator it) {
            this.it = it;
        }
    }
    
    private class H4GRImageCollectionListIterator implements ListIterator{

        private ListIterator listIt;

        public H4GRImageCollectionListIterator(ListIterator listIt) {
                this.listIt = listIt;
        }

        public void add(Object item) {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return listIt.hasNext();
        }

        public boolean hasPrevious() {
            return listIt.hasPrevious();
        }

        public Object next() {
            return listIt.next();
        }

        public int nextIndex() {
            return listIt.nextIndex();
        }

        public Object previous() {
            return listIt.previous();
        }

        public int previousIndex() {
            return listIt.previousIndex();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(Object item) {
            throw new UnsupportedOperationException();
        }
    }

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
     *                the input {@link H4File}
     * @throws IllegalArgumentException
     *                 in case of wrong specified input parameters or in case
     *                 some initialization fails due to wrong identifiers or
     *                 related errors.
     */
    public H4GRImageCollection(H4File h4file) {
        h4File = h4file;
        if (h4file == null)
            throw new IllegalArgumentException("Null file provided");
        final int fileID = h4File.getIdentifier();
        if (fileID == HDFConstants.FAIL)
            throw new IllegalArgumentException("Invalid file identifier");
        try {
            int identifier = HDFLibrary.GRstart(fileID);
            if (identifier != HDFConstants.FAIL) {
                setIdentifier(identifier);
                final int[] grFileInfo = new int[2];

                // Retrieving Information
                if (HDFLibrary.GRfileinfo(identifier, grFileInfo)) {
                    numAttributes = grFileInfo[1];
                    init();
                    numImages = grFileInfo[0];
                    grImagesList = new ArrayList(numImages);
                    grImagesNamesToIndexes = new HashMap(numImages);
                    for (int i = 0; i < numImages; i++) {
                        // Initializing image list
                        H4GRImage grImage = new H4GRImage(this, i);
                        grImagesList.add(i, grImage);
                        final String name = grImage.getName();
                        grImagesNamesToIndexes.put(name, new Integer(i));
                    }
                } else {
                    // the GRFileInfo operation has failed
                    numImages = 0;
                    grImagesList = null;
                    grImagesNamesToIndexes = null;
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "Unable to get file info from the GR interface with ID = "
                                        + identifier);
                }
            } else {
                throw new IllegalStateException(
                        "Failing to get an identifier for the GRImage collection");
            }
        } catch (HDFException e) {
            throw new IllegalStateException(
                    "HDFException occurred while accessing General Raster Images routines with file "
                            + h4file.getFilePath(), e);
        }
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during GRImage collection finalization: "
                                + e.getLocalizedMessage());
        }
    }

    /**
     * End access to the underlying general raster interface and end access to
     * the owned {@link AbstractHObject}s
     */
    public synchronized void dispose() {
        if (grImagesNamesToIndexes != null) {
            grImagesNamesToIndexes.clear();
            grImagesNamesToIndexes = null;
        }
        if (grImagesList != null) {
            for (int i = 0; i < numImages; i++) {
                H4GRImage h4grImage = (H4GRImage) grImagesList.get(i);
                h4grImage.dispose();
            }
            grImagesList.clear();
            grImagesList = null;
        }
        try {
            int identifier = getIdentifier();
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE,
                        "disposing GRImage collection with ID = " + identifier);
            if (identifier != HDFConstants.FAIL) {
                boolean closed = HDFLibrary.GRend(identifier);
                if (!closed) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "Unable to close access to GRImage interface with ID = "
                                        + identifier);
                }
            }
        } catch (HDFException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Error closing access to the GRImage interface with ID = "
                                + getIdentifier());
        }
        super.dispose();
    }

    /**
     * Returns the {@link H4GRImage} related to the index-TH image. Prior to
     * call this method, be sure that some images are available from this
     * {@link H4GRImageCollection} by querying the
     * {@link H4GRImageCollection#size()} method. otherwise an
     * <code>IndexOutOfBoundsException</code> will be thrown.
     * 
     * @param index
     *                the index of the requested image.
     * @return the requested {@link H4GRImage}
     * @throws IndexOutOfBoundsException
     */
    public Object get(final int index) {
        if (index > numImages || index < 0)
            throw new IndexOutOfBoundsException(
                    "Specified index is not valid. It should be greater than zero and belower than "
                            + numImages);
        H4GRImage image = (H4GRImage) grImagesList.get(index);
        return image;
    }

    /**
     * Returns the {@link H4GRImage} having the name specified as input
     * parameter, and open it. Prior to call this method, be sure that some
     * images are available from this {@link H4GRImageCollection} by querying
     * the {@link H4GRImageCollection#size()} method.
     * 
     * @param sName
     *                the name of the requested image.
     * @return the requested {@link H4GRImage} or <code>null</code> if the
     *         specified image does not exist
     */
    public H4GRImage get(final String sName) {
        if (sName==null)
            throw new IllegalArgumentException("Null image name provided");
        H4GRImage grImage = null;
        if (grImagesNamesToIndexes.containsKey(sName)) {
            grImage = (H4GRImage) grImagesList
                    .get(((Integer) grImagesNamesToIndexes.get(sName))
                            .intValue());
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
        return new H4GRImageCollectionIterator(grImagesList.iterator());
    }

    /**
     * Returns a list iterator of the GR Images in this list.
     * 
     * @return a list iterator.
     */
    public ListIterator listIterator() {
        return new H4GRImageCollectionListIterator(grImagesList.listIterator());
    }

    /**
     * Returns a list iterator of the GR Images in this list, starting at the
     * specified position in this list
     * 
     * @return a list iterator.
     */
    public ListIterator listIterator(int index) {
        return new H4GRImageCollectionListIterator(grImagesList.listIterator(index));
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
     * Removes from the list all the elements that are not contained in the
     * specified collection. Since this method is actually unsupported, an
     * <code>UnsupportedOperationException</code> will be thrown.
     * 
     * @throws UnsupportedOperationException
     */
    public boolean retainAll(Collection arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is actually unsupported: an
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
     * 
     * @throws UnsupportedOperationException
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is actually unsupported: an
     * <code>UnsupportedOperationException</code> will be thrown.
     * 
     * @throws UnsupportedOperationException
     */
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is actually unsupported: an
     * <code>UnsupportedOperationException</code> will be thrown.
     * 
     * @throws UnsupportedOperationException
     */
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is actually unsupported: an
     * <code>UnsupportedOperationException</code> will be thrown.
     * 
     * @throws UnsupportedOperationException
     */
    public Object[] toArray(Object[] arg0) {
        throw new UnsupportedOperationException();
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
     * Returns <code>true</code> if this list contains the specified GR Image.
     */
    public boolean contains(Object image) {
        return grImagesList.contains(image);
    }

    /**
     * Returns <code>true</code> if this list contains all of the elements of
     * the specified collection.
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
     * Returns the index in this list of the last occurrence of the specified GR
     * Image, or -1 if this list does not contain it.
     */
    public int lastIndexOf(Object image) {
        return grImagesList.lastIndexOf(image);
    }

    /**
     * @see {@link AbstractH4Object#readAttribute(int, Object)}
     */
    protected boolean readAttribute(int index, Object values)
            throws HDFException {
        return HDFLibrary.GRgetattr(getIdentifier(), index, values);
    }

    /**
     * @see {@link AbstractH4Object#getAttributeInfo(int, String[])}
     */
    protected int[] getAttributeInfo(int index, String[] attrName)
            throws HDFException {
        int[] attrInfo = new int[] { 0, 0 };
        boolean done = HDFLibrary.GRattrinfo(getIdentifier(), index, attrName,
                attrInfo);
        if (done)
            return attrInfo;
        else
            return null;
    }

    /**
     * @see {@link AbstractH4Object#findAttributeIndexByName(String)}
     */
    protected int findAttributeIndexByName(String attributeName) throws HDFException {
        return HDFLibrary.GRfindattr(getIdentifier(),attributeName);
    }
}
