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

/**
 * Interface for HDF Objects.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public interface IHObject {
	
	/** 
	 * Close method to end access to the underlying HDF4 object which could
	 * represent, as an instance, a GR Image, a SDS, a GR Images interface 
	 * (The object which allows to get access to several GR Images contained
	 * within a HDF data source). 
	 */
	public void close();

	/**
	 * Returns the idenfitier related to an underlying HDF element (a SDS, a 
	 * GR Image, a SDS Interface ...) 
	 * 
	 * @return the identifier related to the underlying HDF element. 
	 */
	public int getIdentifier();
}
