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
package it.geosolutions.hdf.object;

import ncsa.hdf.hdflib.HDFConstants;

/**
 * Main abstract class representing a HDF Object.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class AbstractHObject implements IHObject {

    /**
     * The numeric identifier associated to this <code>AbstractHObject</code>
     */
    private volatile int identifier = HDFConstants.FAIL;

    /**
     * getter of <code>identifier</code>
     * 
     * @return the numeric identifier associated to this
     *         <code>AbstractHObject</code>
     */
    public int getIdentifier() {
        return identifier;
    }

    protected void setIdentifier(int identifier) {
        if (identifier == HDFConstants.FAIL)
            throw new IllegalArgumentException("HDF identifier cannot be negative! Found " + Integer.toString(identifier));
        if (this.identifier != HDFConstants.FAIL)
            throw new IllegalStateException("Identifier cannot be changed");
        this.identifier = identifier;
    }

    /**
     * Disposes this {@link AbstractHObject}
     */
    public void dispose() {
        this.identifier = HDFConstants.FAIL;
    }
}
