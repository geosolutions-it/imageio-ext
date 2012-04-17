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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

/**
 * Abstract class representing a HDF variable
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class H4Variable extends AbstractHObject implements IHObject {

    /**
     * The name of this Variable
     */
    private String name = null;

    /**
     * getter of <code>name</code>
     * 
     * @return the name of this Variable.
     */
    public String getName() {
        return name;
    }

    protected synchronized void setName(String name) {
        if (this.name != null)
            throw new IllegalStateException("Name cannot be changed");
        this.name = name;
    }
}
