/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        https://imageio-ext.dev.java.net/
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
package it.geosolutions.hdf.object.h4;

import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * A package private class which allows to handle Attributes using the SDS APIs.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
class H4SDSFamilyObjectsWithAttributes  extends AbstractH4ObjectWithAttributes{

    public H4SDSFamilyObjectsWithAttributes(final int identifier, final int numAttributes) {
        super(identifier, numAttributes);
    }
    
    /**
     * @see {@link AbstractH4ObjectWithAttributes#readAttribute(int, Object)}
     */
    protected boolean readAttribute(int index, Object values)
            throws HDFException {
        return HDFLibrary.SDreadattr(getIdentifier(), index, values);
    }

    /**
     * @see {@link AbstractH4ObjectWithAttributes#getAttributeInfo(int, String[])}
     */
    protected int[] getAttributeInfo(int index, String[] attrName)
            throws HDFException {
        int[] attrInfo = new int[] { 0, 0 };
        boolean done = HDFLibrary.SDattrinfo(getIdentifier(), index, attrName,
                attrInfo);
        if (done)
            return attrInfo;
        else
            return null;
    }

    /**
     * @see {@link AbstractH4ObjectWithAttributes#findAttributeIndexByName(String)}
     */
    protected int findAttributeIndexByName(String attributeName)
            throws HDFException {
        return HDFLibrary.SDfindattr(getIdentifier(), attributeName);
    }
}
