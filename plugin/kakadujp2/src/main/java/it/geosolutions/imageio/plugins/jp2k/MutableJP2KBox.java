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

public interface MutableJP2KBox extends JP2KBox, MutableTreeNode {

	/** Returns the box content in byte array. */
	public void setContent(final byte [] content);
	
	public void setlength(final int length);
	
	public void setExtraLength(final long extraLength);
	
	public void setType(final int type);
	
	public void setIsLeaf(final boolean isLeaf);

}
