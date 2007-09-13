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
package it.geosolutions.imageio.stream.output;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * @author Simone Giannecchini
 * 
 * Supporting marking is a big issue I should overline this somehow
 * 
 */
public class ImageOutputStreamAdapter extends ImageOutputStreamImpl {

	private OutputStream os;

	/**
	 * 
	 */
	public ImageOutputStreamAdapter(OutputStream os) {
		this.os = os;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.imageio.stream.ImageOutputStreamImpl#write(int)
	 */
	public void write(int b) throws IOException {
		os.write(b);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.imageio.stream.ImageOutputStreamImpl#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b,off,len);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.imageio.stream.ImageInputStreamImpl#read()
	 */
	public int read() throws IOException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.imageio.stream.ImageInputStreamImpl#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see javax.imageio.stream.ImageInputStreamImpl#flush()
	 */
	public void flush() throws IOException {
		os.flush();
	}

	/* (non-Javadoc)
	 * @see javax.imageio.stream.ImageInputStreamImpl#close()
	 */
	public void close() throws IOException {
		super.close();
		os.close();
	}

	

}
