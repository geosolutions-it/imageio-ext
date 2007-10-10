/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
 *    (C) 2006, GeoSolutions S.A.S.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A {@link Map} with a fixed maximum size which removes the <cite>least recently used</cite> (LRU)
 * entry if an entry is added when full. This class implements a simple technique for LRU pooling
 * of objects.
 * <p>
 * Note that this cache is based on hard references, not on {@linkplain java.lang.ref.WeakReference
 * weak} or {@linkplain java.lang.ref.SoftReference soft references} because especially for server
 * side applications such caches are often too aggressively cleared.
 * 
 * @version $Id$
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class LRULinkedHashMap extends LinkedHashMap {
    /**
     * Serial number for cross-version compatibility.
     */
    private static final long serialVersionUID = 2082871604196698140L;

    /** Default maximum capacity. */
    private static final int DEFAULT_MAXIMUM_CAPACITY = 100;

    /** Maximum number of entries for this LRU cache. */
    private final int maxEntries;

    /**
     * Constructs a {@code LRULinkedHashMap} with default initial capacity, maximum capacity
     * and load factor.
     */
    public LRULinkedHashMap() {
        super();
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with default maximum capacity and load factor.
     * 
     * @param initialCapacity The initial capacity.
     */
    public LRULinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with default maximum capacity.
     * 
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     */
    public LRULinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with default maximum capacity.
     * 
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @param accessOrder     The ordering mode: {@code true} for access-order,
     *                        {@code false} for insertion-order.
     */
    public LRULinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with the specified maximum capacity.
     * 
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @param accessOrder     The ordering mode: {@code true} for access-order,
     *                        {@code false} for insertion-order.
     * @param maxEntries      Maximum number of entries for this LRU cache.
     */
    public LRULinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder,
            final int maxEntries) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxEntries = maxEntries;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with all entries from the specified map.
     * 
     * @param m the map whose mappings are to be placed in this map.
     */
    private LRULinkedHashMap(final Map m) {
        super(m);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
        removeExtraEntries();
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with all entries from the specified map
     * and maximum number of entries.
     * 
     * @param m the map whose mappings are to be placed in this map.
     * @param maxEntries      Maximum number of entries for this LRU cache.
     */
    private LRULinkedHashMap(final Map m, final int maxEntries) {
        super(m);
        this.maxEntries = maxEntries;
        removeExtraEntries();
    }

    /**
     * If there is more entries than the maximum amount, remove extra entries.
     * <p>
     * <b>Note:</b> Invoking {@code removeExtraEntries()} after adding all entries in the
     * {@code LRULinkedHashMap(Map)} constructor is less efficient than just iterating over
     * the {@code maxEntries} first entries at construction time, but super-class constructor
     * is more efficient for maps with less than {@code maxEntries}. We assume that this is the
     * most typical case. In addition, this method would be needed anyway if we add a
     * {@code setMaximumEntries(int)} method in some future Geotools version.
     */
    private void removeExtraEntries() {
        if (size() > maxEntries) {
            final Iterator it = entrySet().iterator();
            for (int c=0; c<maxEntries; c++) {
                it.next();
            }
            while (it.hasNext()) {
                it.remove();
            }
        }
    }

    /**
     * Returns {@code true} if this map should remove its eldest entry. The default implementation
     * returns {@code true} if the {@linkplain #size number of entries} in this map has reached the
     * maximum number of entries specified at construction time.
     * 
     * @param eldest The least recently inserted entry in the map, or if this is an access-ordered
     *        map, the least recently accessed entry. This is the entry that will be removed it this
     *        method returns {@code true}.
     * @return {@code true} if the eldest entry should be removed from the map;
     *         {@code false} if it should be retained.
     */
    protected boolean removeEldestEntry(final Map.Entry eldest) {
        // /////////////////////////////////////////////////////////////////////
        //
        // Do I have to remove anything?
        //
        // If I still am below the desired threshold I just return false nad
        // that is it.
        //
        // /////////////////////////////////////////////////////////////////////
        return size() > maxEntries;
    }
}
