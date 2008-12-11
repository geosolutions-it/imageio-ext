/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.jp2k;

import javax.swing.tree.MutableTreeNode;

/**
 * Basic interface for a {@link JP2KBox}.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 */
public interface JP2KBox extends MutableTreeNode, Cloneable{

    /** Returns the box length. */
    public abstract int getLength();

    /** Returns the box type. */
    public abstract int getType();

    /** Returns the box extra length. */
    public abstract long getExtraLength();

    /**
     * Returns the box content in byte array.
     * 
     * @return a byte array with the box content or null if this is a super box.
     */
    public byte[] getContent();
    
    public abstract Object clone();
}