/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        https://imageio-ext.dev.java.net/
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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;
import ncsa.hdf.hdflib.HDFException;

/**
 * Interface which should be implemented by H4Objects having attributes.
 * 
 * @todo in the future we may want to refactor this interface and merge it with 
 * the {@link IHObject} interface in order to also merge the respective abstract classes providing
 * skeletal implementations.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public interface IH4Object extends IHObject {

    /**
     * Return the number of attributes available for the H4Object
     */
    int getNumAttributes();

    /**
     * Return a H4Attribute given a specified index.
     * 
     * @param attributeIndex
     *                the index of the requested attribute. In case
     *                getNumAttributes returns 0, meaning no Attributes, calling
     *                this method will result in an
     *                {@link IllegalArgumentException}. In case of available
     *                attributes, the index shall be in the range [0,
     *                getNumAttributes()-1], otherwise an
     *                {@code IndexOutOfBoundsException} will be thrown.
     * @return the specified attribute or {@code null} in case of no attribute
     *         found.
     * @throws HDFException
     */
    H4Attribute getAttribute(final int attributeIndex) throws HDFException;

    /**
     * Return a H4Attribute given a specified name.
     * 
     * @param attributeName
     *                the name of the requested attribute.
     * @return the specified attribute or {@code null} in case of no attribute
     *         found.
     * @throws HDFException
     */
    H4Attribute getAttribute(final String attributeName) throws HDFException;

}