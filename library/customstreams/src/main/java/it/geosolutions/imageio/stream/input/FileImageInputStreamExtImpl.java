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
package it.geosolutions.imageio.stream.input;

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * An implementation of {@link ImageInputStream} that gets its input from a
 * {@link File}. The eraf contents are assumed to be stable during the lifetime
 * of the object.
 * 
 * @author Simone Giannecchini
 */
public final class FileImageInputStreamExtImpl extends ImageInputStreamImpl
		implements FileImageInputStreamExt {

	protected File file;

	protected EnhancedRandomAccessFile eraf;

	public byte readByte() throws IOException {

		return eraf.readByte();
	}

	public char readChar() throws IOException {

		return eraf.readChar();
	}

	public double readDouble() throws IOException {

		return eraf.readDouble();
	}

	public float readFloat() throws IOException {

		return eraf.readFloat();
	}

	public void readFully(byte[] b, int off, int len) throws IOException {

		eraf.readFully(b, off, len);
	}

	public void readFully(byte[] b) throws IOException {

		eraf.readFully(b);
	}

	public int readInt() throws IOException {

		return eraf.readInt();
	}

	public String readLine() throws IOException {

		return eraf.readLine();
	}

	public ByteOrder getByteOrder() {

		return eraf.getByteOrder();
	}

	public long getStreamPosition() throws IOException {
		return eraf.getFilePointer();
	}

	public boolean isCached() {
		return eraf.isCached();
	}

	public int read(byte[] b) throws IOException {
		return eraf.read(b);
	}

	public long skipBytes(long n) throws IOException {

		return eraf.skipBytes(n);
	}

	public long readLong() throws IOException {

		return eraf.readLong();
	}

	public short readShort() throws IOException {

		return eraf.readShort();
	}

	public int readUnsignedByte() throws IOException {

		return eraf.readUnsignedByte();
	}

	public long readUnsignedInt() throws IOException {

		return eraf.readUnsignedInt();
	}

	public int readUnsignedShort() throws IOException {

		return eraf.readUnsignedShort();
	}

	public String readUTF() throws IOException {

		return eraf.readUTF();
	}

	public void setByteOrder(ByteOrder byteOrder) {

		eraf.setByteOrder(byteOrder);
	}

	public int skipBytes(int n) throws IOException {

		return eraf.skipBytes(n);
	}

	/**
	 * Constructs a {@link FileImageInputStreamExtImpl} that will read from a
	 * given {@link File}.
	 * 
	 * <p>
	 * The eraf contents must not change between the time this object is
	 * constructed and the time of the last call to a read method.
	 * 
	 * @param f
	 *            a {@link File} to read from.
	 * 
	 * @exception NullPointerException
	 *                if <code>f</code> is <code>null</code>.
	 * @exception SecurityException
	 *                if a security manager exists and does not allow read
	 *                access to the eraf.
	 * @exception FileNotFoundException
	 *                if <code>f</code> is a directory or cannot be opened for
	 *                reading for any other reason.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public FileImageInputStreamExtImpl(File f) throws FileNotFoundException,
			IOException {
		if (f == null) {
			throw new NullPointerException("f == null!");
		}
		this.file = f;
		this.eraf = new EnhancedRandomAccessFile(f, "r");
		this.eraf.setByteOrder(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Constructs a {@link FileImageInputStreamExtImpl} that will read from a
	 * given {@link File}.
	 * 
	 * <p>
	 * The eraf contents must not change between the time this object is
	 * constructed and the time of the last call to a read method.
	 * 
	 * @param f
	 *            a {@link File} to read from.
	 * @param bufferSize
	 *            size of the underlying buffer.
	 * 
	 * @exception NullPointerException
	 *                if <code>f</code> is <code>null</code>.
	 * @exception SecurityException
	 *                if a security manager exists and does not allow read
	 *                access to the eraf.
	 * @exception FileNotFoundException
	 *                if <code>f</code> is a directory or cannot be opened for
	 *                reading for any other reason.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public FileImageInputStreamExtImpl(File f, int bufferSize)
			throws FileNotFoundException, IOException {
		if (f == null) {
			throw new NullPointerException("Input eraf was null!");
		}
		this.file = f;
		this.eraf = new EnhancedRandomAccessFile(f, "r", bufferSize);
		// NOTE: this must be done accordingly to what ImageInputStreamImpl
		// does, otherwise some ImageREader subclasses might not work.
		this.eraf.setByteOrder(ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Reads an int from the underlying {@link EnhancedRandomAccessFile}.
	 */
	public int read() throws IOException {
		checkClosed();
		bitOffset = 0;
		int val = eraf.read();
		if (val != -1) {
			++streamPos;
		}
		return val;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		checkClosed();
		bitOffset = 0;
		int nbytes = eraf.read(b, off, len);
		if (nbytes != -1) {
			streamPos += nbytes;
		}
		return nbytes;
	}

	/**
	 * Returns the length of the underlying eraf, or <code>-1</code> if it is
	 * unknown.
	 * 
	 * @return the eraf length as a <code>long</code>, or <code>-1</code>.
	 */
	public long length() {
		try {
			checkClosed();
			return eraf.length();
		} catch (IOException e) {
			return -1L;
		}
	}

	/**
	 * Seeks the current position to pos.
	 */
	public void seek(long pos) throws IOException {
		checkClosed();
		if (pos < flushedPos) {
			throw new IllegalArgumentException("pos < flushedPos!");
		}
		bitOffset = 0;
		eraf.seek(pos);
		streamPos = eraf.getFilePointer();
	}

	/**
	 * Closes the underlying {@link EnhancedRandomAccessFile}.
	 * 
	 * @throws IOException
	 *             in case something bad happens.
	 */
	public void close() throws IOException {
		super.close();
		eraf.close();
	}

	/**
	 * Retrieves the {@link File} we are connected to.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Disposes this {@link FileImageInputStreamExtImpl} by closing its
	 * underlying {@link EnhancedRandomAccessFile}.
	 * 
	 */
	public void dispose() {
		try {
			close();
		} catch (IOException e) {

		}

	}

}
