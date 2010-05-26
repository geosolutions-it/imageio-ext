/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.imageio.utilities;

import java.io.Serializable;
import java.util.Map;

/**
 * A default implementation of {@link java.util.Map.Entry} which map an arbitrary
 * key-value pairs. This entry is immutable by default.
 * 
 * @author Martin Desruisseaux
 */
public class MapEntry<K,V> implements Map.Entry<K,V>, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 8627698052283756776L;

    /**
     * The key.
     */
    private final K key;

    /**
     * The value.
     */
    private final V value;

    /**
     * Creates a new map entry with the specified key-value pair.
     *
     * @param key The key.
     * @param value The value.
     */
    public MapEntry(final K key, final V value) {
        this.key   = key;
        this.value = value;
    }

    /**
     * Returns the key corresponding to this entry.
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value corresponding to this entry.
     */
    public V getValue() {
        return value;
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation). The default implementation throws an
     * {@link UnsupportedOperationException}.
     */
    public V setValue(final V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Compares the specified object with this entry for equality.
     *
     * @param object The object to compare with this entry for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof Map.Entry) {
            final Map.Entry that = (Map.Entry) object;
            return ImageIOUtilities.equals(this.getKey(),   that.getKey()) &&
                   ImageIOUtilities.equals(this.getValue(), that.getValue());
        }
        return false;
    }

    /**
     * Returns the hash code value for this map entry
     */
    @Override
    public int hashCode() {
        int code = 0;
        if (key   != null) code  =   key.hashCode();
        if (value != null) code ^= value.hashCode();
        return code;
    }
}
