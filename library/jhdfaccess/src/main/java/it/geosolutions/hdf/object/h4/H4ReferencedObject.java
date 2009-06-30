/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.hdf.object.h4;

/**
 * Main class representing an HDF4 Object with a reference.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
class H4ReferencedObject {

    /**
     * the reference of this object
     */
    private int reference;

    public H4ReferencedObject(int ref) {
        reference = ref;
    }

    /**
     * getter of <code>reference</code>
     * 
     * @return the reference of this object.
     */
    public int getReference() {
        return reference;
    }

}
