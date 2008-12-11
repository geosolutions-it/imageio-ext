/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import javax.imageio.ImageReadParam;

/**
 * A class to be used to allows {@link ImageReadParam}s to be cloned.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class CloneableImageReadParam extends ImageReadParam implements
		Cloneable {

	public abstract Object clone() throws CloneNotSupportedException;

}
 