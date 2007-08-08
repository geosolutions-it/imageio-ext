/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.stream.input;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * Decorator class for the {@link ImageInputStream} subclasses.
 * 
 * @author Simone Giannecchini
 * 
 */
public abstract class FilterImageInputStream extends ImageInputStreamImpl
		implements ImageInputStream {

	protected ImageInputStream iis;

	/**
	 * 
	 */
	public FilterImageInputStream(ImageInputStream iis) {

		this.iis = iis;
	}

	public FilterImageInputStream() {

		this.iis = null;
	}

	public boolean isCached() {

		return iis.isCached();
	}

	public boolean isCachedFile() {

		return iis.isCachedFile();
	}

	public boolean isCachedMemory() {

		return iis.isCachedMemory();
	}

	public int skipBytes(int n) throws IOException {

		return iis.skipBytes(n);
	}

	public void setBitOffset(int bitOffset) throws IOException {
		iis.setBitOffset(bitOffset);

	}

	public ByteOrder getByteOrder() {

		return iis.getByteOrder();
	}

	public void setByteOrder(ByteOrder byteOrder) {
		iis.setByteOrder(byteOrder);

	}

}
