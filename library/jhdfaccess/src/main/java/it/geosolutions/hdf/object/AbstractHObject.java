/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
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
package it.geosolutions.hdf.object;

import ncsa.hdf.hdflib.HDFConstants;

/**
 * Main abstract class representing a HDF Object.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class AbstractHObject implements IHObject {

    public AbstractHObject(int identifier) {
        setIdentifier(identifier);
    }

    protected AbstractHObject() {
    }

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
            throw new IllegalArgumentException(
                    "HDF identifier cannot be negative! Found "
                            + Integer.toString(identifier));
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
