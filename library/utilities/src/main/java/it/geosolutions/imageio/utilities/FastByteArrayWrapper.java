/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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
package it.geosolutions.imageio.utilities;

import java.io.IOException;

/**
 * This class implements a byteArray wrapper to speed up working with byte
 * array. The buffer automatically grows as data is written to it. The data can
 * be retrieved using <code>toByteArray()</code> and <code>toString()</code>.
 * <p>
 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in this
 * class can be called after the stream has been closed without generating an
 * <tt>IOException</tt>.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Giannecchini Simone, GeoSolutions
 */
public class FastByteArrayWrapper {

    /**
     * The buffer where data is stored.
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;

    /**
     * Default constructor<BR>
     * Creates a new byte array output stream. The buffer capacity is initially
     * 32 bytes, though its size increases if necessary.
     */
    public FastByteArrayWrapper() {
        this(32);
    }

    /**
     * Build a {@link FastByteArrayWrapper} with the inner buffer specified as
     * argument
     * 
     * @param buffer
     *                the inner buffer used by this {@link FastByteArrayWrapper}
     */
    public FastByteArrayWrapper(byte[] buffer) {
        this(buffer, 0);
    }

    /**
     * Build a {@link FastByteArrayWrapper} with the inner buffer specified as
     * argument.
     * 
     * @param buffer
     *                the inner buffer used by this {@link FastByteArrayWrapper}
     * @param count
     *                the number of valid bytes in the provided buffer
     */
    public FastByteArrayWrapper(byte[] buffer, int count) {
        if (count < 0 || (buffer != null && count > buffer.length))
            throw new IllegalArgumentException(
                    "The specified count is invalid. It can't be lower than zero or greater than the length of the provided buffer");
        this.buf = buffer;
        this.count = count;
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of the
     * specified size, in bytes.
     * 
     * @param size
     *                the initial size.
     * @exception IllegalArgumentException
     *                    if size is negative.
     */
    public FastByteArrayWrapper(int size) {
        if (size < 0)
            throw new IllegalArgumentException("Negative initial size: " + size);
        buf = new byte[size];
    }

    /**
     * Writes the specified byte to this byte array wrapper.
     * 
     * @param b
     *                the byte to be written.
     */
    public void write(int b) {
        int newcount = count + 1;
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        buf[count] = (byte) b;
        count = newcount;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at
     * offset <code>off</code> to this byte array wrapper.
     * 
     * @param b
     *                the data.
     * @param off
     *                the start offset in the data.
     * @param len
     *                the number of bytes to write.
     */
    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0)
            return;

        int newcount = count + len;
        if (newcount > buf.length) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * starting at offset 0 to this byte array wrapper.
     * 
     * @param b
     *                the data.
     */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Resets the <code>count</code> field of this byte array wrapper to zero,
     * so that all currently accumulated values in the byte array is discarded.
     */
    public void reset() {
        count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current size of
     * this byte array wrapper and the valid contents of the buffer have been
     * copied into it.
     * 
     * @return the current contents of this output stream, as a byte array.
     * @see java.io.ByteArrayOutputStream#size()
     */
    public byte getByteArray()[] {
        return this.buf;
    }

    /**
     * Returns the current size of the buffer.
     * 
     * @return the value of the <code>count</code> field, which is the number
     *         of valid bytes in the buffer.
     * @see java.io.ByteArrayOutputStream#count
     */
    public int size() {
        return count;
    }

    /**
     * Converts the buffer's contents into a string, translating bytes into
     * characters according to the platform's default character encoding.
     * 
     * @return String translated from the buffer's contents.
     */
    public String toString() {
        return new String(buf, 0, count);
    }
}
