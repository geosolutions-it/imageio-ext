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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF Lone Vgroups.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4VGroupCollection extends AbstractHObject implements IHObject,
        List {

    private class H4VGroupCollectionIterator implements Iterator{

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

        public H4VGroupCollectionIterator(Iterator it) {
                this.it = it;
        }
    }
    
    private class H4VGroupCollectionListIterator implements ListIterator{

        private ListIterator listIt;

        public H4VGroupCollectionListIterator(ListIterator listIt) {
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
    
    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");
    
    /**
     * the number of lone vgroups of this Group collection
     */
    private int numLoneVgroups = 0;

    /**
     * The list of lone Vgroups of this Group collection
     */
    private List loneVgroupsList;

    /**
     * {@link H4File} to which this collection is attached
     * 
     * @uml.associationEnd inverse="h4VgroupCollection:it.geosolutions.hdf.object.h4.H4File"
     */
    private H4File h4File;

    /**
     * Returns the number of lone VGroups available in this collection
     */
    public int size() {
        return numLoneVgroups;
    }

    /**
     * getter of <code>h4File</code>
     * 
     * @return the {@link H4File} to which this collection is attached.
     */
    H4File getH4File() {
        return h4File;
    }

    /**
     * Constructor which builds and initialize a <code>H4VGroupCollection</code>
     * given an input {@link H4File}.
     * 
     * @param h4file
     *                the input {@link H4File}
     * 
     * @throws IllegalArgumentException
     *                 in case some error occurs when accessing the File or when
     *                 accessing the VGroup interface
     */
    public H4VGroupCollection(H4File h4file) {
        h4File = h4file;
        if (h4file == null)
            throw new IllegalArgumentException("Null file provided");
        final int fileID = h4File.getIdentifier();
        if (fileID == HDFConstants.FAIL)
            throw new IllegalArgumentException("Invalid file identifier");
        try {
            if (HDFLibrary.Vstart(fileID)) {
                final int nLoneVgroups = HDFLibrary.Vlone(fileID, null, 0);

                loneVgroupsList = new ArrayList(nLoneVgroups);
                final int[] referencesArray = new int[nLoneVgroups];
                HDFLibrary.Vlone(fileID, referencesArray, nLoneVgroups);
                for (int loneVgroupIndex = 0; loneVgroupIndex < nLoneVgroups; loneVgroupIndex++) {
                    // Attach to the current vgroup then get and display its
                    // name and class. Only Real VGroups will be attached

                    // Note: the current vgroup must be detached
                    // before moving to the next.
                    H4VGroup vgroup = new H4VGroup(this,referencesArray[loneVgroupIndex]);
                    if (H4Utilities.isAVGroupClass(vgroup.getClassName())) {
                        vgroup.initialize();
                        loneVgroupsList.add(numLoneVgroups++, vgroup);
                    } else
                        vgroup.dispose();
                }
            } else {
                throw new IllegalStateException(
                "Failing to get access to the VGroup routines");
            }
        } catch (HDFException e) {
            throw new IllegalStateException(
                    "HDFException occurred while accessing VGroup routines with file "
                            + h4file.getFilePath(), e);
        }
    }

    /**
     * Returns the {@link H4VGroup} related to the index-TH group. Prior to call
     * this method, be sure that some lone vgroups are available from this
     * {@link H4VGroupCollection} by querying the
     * {@link H4VGroupCollection#size()} method. otherwise an
     * <code>IndexOutOfBoundsException</code> will be thrown.
     * 
     * @param index
     *                the index of the required vgroup.
     * @return the requested {@link H4VGroup}
     * @throws IndexOutOfBoundsException
     */
    public Object get(int index) {
        if (index > numLoneVgroups || index < 0)
            throw new IndexOutOfBoundsException(
                    "Specified index is not valid. It should be greater than zero and belower than "
                            + numLoneVgroups);
        H4VGroup group = (H4VGroup) loneVgroupsList.get(index);
        return group;
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during dimension finalization: "
                                + e.getLocalizedMessage());
        }
    }


    /**
     * End access to the underlying VGroup interface and end access to the owned
     * {@link AbstractHObject}s
     */
    public synchronized void dispose() {
        try {
            if (loneVgroupsList != null) {
                for (int i = 0; i < numLoneVgroups; i++) {
                    H4VGroup group = (H4VGroup) loneVgroupsList.get(i);
                    group.dispose();
                }
            }
            int identifier=getIdentifier();
	    if (identifier != HDFConstants.FAIL) {
	        if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE,
                            "disposing VGroup collection with ID = ");
                HDFLibrary.Vend(identifier);
            }
        } catch (HDFException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Error Closing access to the VGroup Interface with ID = "
                                + getIdentifier());
        }
        super.dispose();
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
     * Returns <code>true</code> if this list contains the specified lone
     * VGroup.
     */
    public boolean contains(Object vgroup) {
        return loneVgroupsList.contains(vgroup);
    }

    /**
     * Returns <code>true</code> if this list contains all of the elements of
     * the specified collection.
     */
    public boolean containsAll(Collection collection) {
        return loneVgroupsList.containsAll(collection);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * lone VGroup, or -1 if this list does not contain it.
     */
    public int indexOf(Object vgroup) {
        return loneVgroupsList.indexOf(vgroup);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * lone VGroup, or -1 if this list does not contain it.
     */
    public int lastIndexOf(Object vgroup) {
        return loneVgroupsList.lastIndexOf(vgroup);
    }

    /**
     * Returns <code>true</code> if this list contains no lone VGroups.
     * 
     * @return <code>true</code> if this list contains no lone VGroups.
     */
    public boolean isEmpty() {
        return size() > 0 ? false : true;
    }

    /**
     * Returns an iterator over the lone VGroups in this list.
     * 
     * @return an iterator.
     */
    public Iterator iterator() {
        return new H4VGroupCollectionIterator(loneVgroupsList.iterator());
    }

    /**
     * Returns a list iterator of the lone VGroups in this list.
     * 
     * @return a list iterator.
     */
    public ListIterator listIterator() {
        return new H4VGroupCollectionListIterator(loneVgroupsList.listIterator());
    }

    /**
     * Returns a list iterator of the lone VGroups in this list, starting at the
     * specified position in this list
     * 
     * @return a list iterator.
     */
    public ListIterator listIterator(int index) {
        return new H4VGroupCollectionListIterator(loneVgroupsList.listIterator(index));
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
     * Removes from the list all the elements that are not contained in the
     * specified collection. Since this method is actually unsupported, an
     * <code>UnsupportedOperationException</code> will be thrown.
     * 
     * @throws UnsupportedOperationException
     */
    public boolean retainAll(Collection arg0) {
        throw new UnsupportedOperationException();
    }
}
