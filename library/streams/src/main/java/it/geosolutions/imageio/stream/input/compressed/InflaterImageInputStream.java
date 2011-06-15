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
package it.geosolutions.imageio.stream.input.compressed;

import it.geosolutions.imageio.stream.input.FilterImageInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

import javax.imageio.stream.ImageInputStream;

/**
 * @author Simone Giannecchini, GeoSolutions
 */
public class InflaterImageInputStream extends FilterImageInputStream {
    /**
     * Length of input buffer.
     */
    protected int len;

    private byte[] b = new byte[512];

    protected boolean closed = false;

    /**
     * Input buffer for decompression.
     */
    protected byte[] buf;

    protected byte[] singleByteBuf = new byte[1];

    protected boolean usesDefaultInflater = false;

    /**
     * Decompressor for this stream.
     */
    protected Inflater inf;

    public InflaterImageInputStream(ImageInputStream iis) {
        this(iis, new Inflater());
        usesDefaultInflater = true;
    }

    public InflaterImageInputStream(ImageInputStream iis, Inflater inflater) {
        super(iis);
        this.inf = inflater;
        if (inf == null) {
            throw new NullPointerException();
        }
        buf = new byte[8192];

    }

    public InflaterImageInputStream(ImageInputStream iis, Inflater inflater,
            int size) {
        super(iis);
        this.inf = inflater;
        if (inf == null) {
            throw new NullPointerException();
        }
        buf = new byte[size];

    }

    /**
     * Reads a byte of uncompressed data. This method will block until enough
     * input is available for decompression.
     * 
     * @return the byte read, or -1 if end of compressed input is reached
     * @exception IOException
     *                    if an I/O error has occurred
     */
    public int read() throws IOException {
        checkClosed();
        return read(singleByteBuf, 0, 1) == -1 ? -1 : singleByteBuf[0] & 0xff;
    }

    /**
     * Reads uncompressed data into an array of bytes. This method will block
     * until some input can be decompressed.
     * 
     * @param b
     *                the buffer into which the data is read
     * @param off
     *                the start offset of the data
     * @param len
     *                the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     *         compressed input is reached or a preset dictionary is needed
     * @exception ZipException
     *                    if a ZIP format error has occurred
     * @exception IOException
     *                    if an I/O error has occurred
     */
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        try {
            int n;
            while ((n = inf.inflate(b, off, len)) == 0) {
                if (inf.finished() || inf.needsDictionary()) {
                    return -1;
                }
                if (inf.needsInput()) {
                    fill();
                }
            }
            return n;
        } catch (DataFormatException e) {
            String s = e.getMessage();
            throw new ZipException(s != null ? s : "Invalid ZLIB data format");
        }
    }

    /**
     * Fills input buffer with more data to decompress.
     * 
     * @exception IOException
     *                    if an I/O error has occurred
     */
    protected void fill() throws IOException {
        checkClosed();
        len = iis.read(buf, 0, buf.length);
        if (len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        }
        inf.setInput(buf, 0, len);
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * 
     * @exception IOException
     *                    if an I/O error has occurred
     */
    public void close() throws IOException {
        if (!closed) {
            if (usesDefaultInflater)
                inf.end();
            super.close();
            closed = true;
        }
    }

    public void mark() {
        throw new UnsupportedOperationException();
    }

    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void seek(long pos) throws IOException {
        throw new UnsupportedOperationException();
    }

    public int skipBytes(int n) throws IOException {
        return skipBytes(n);
    }

    /**
     * Skips specified number of bytes of uncompressed data.
     * 
     * @param n
     *                the number of bytes to skip
     * @return the actual number of bytes skipped.
     * @exception IOException
     *                    if an I/O error has occurred
     * @exception IllegalArgumentException
     *                    if n < 0
     */
    public long skipBytes(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        checkClosed();
        int max = (int) Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > b.length) {
                len = b.length;
            }
            len = read(b, 0, len);
            if (len == -1) {
                break;
            }
            total += len;
        }
        return total;
    }

}
