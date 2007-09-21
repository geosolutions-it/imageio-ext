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
 * @author Daniele Romagnoli
 */
public abstract class AbstractHObject implements IHObject{

	/**
	 * The numeric identifier associated to this <code>AbstractHObject</code>
	 * 
	 * @uml.property name="identifier"
	 */
	protected int identifier = HDFConstants.FAIL;

	/**
	 *  Getter of the property <code>identifier</code>
	 * 
	 * @return the numeric identifier associated to this
	 *         <code>AbstractHObject</code>
	 * @uml.property name="identifier"
	 */
	public int getIdentifier() {
		return identifier;
	}
}
