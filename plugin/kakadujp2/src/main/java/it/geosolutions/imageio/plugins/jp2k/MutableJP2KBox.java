/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.jp2k;

import javax.swing.tree.MutableTreeNode;

/**
 * @author Simone Giannecchini, GeoSolutions
 */
public interface MutableJP2KBox extends JP2KBox, MutableTreeNode {

    /** Returns the box content in byte array. */
    public void setContent(final byte[] content);

    public void setlength(final int length);

    public void setExtraLength(final long extraLength);

    public void setType(final int type);

    public void setIsLeaf(final boolean isLeaf);

}
