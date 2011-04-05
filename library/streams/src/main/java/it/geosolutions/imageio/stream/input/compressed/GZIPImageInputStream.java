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

import it.geosolutions.io.input.adapter.InputStreamAdapter;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Inflater;

import javax.imageio.stream.ImageInputStream;

/**
 * This class implements a stream filter for reading compressed data in the GZIP
 * format.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class GZIPImageInputStream extends InflaterImageInputStream {
    private byte[] tmpbuf = new byte[128];

    /**
     * GZIP header magic number.
     */
    public final static int GZIP_MAGIC = 0x8b1f;

    /**
     * File header flags.
     */
    private final static int FHCRC = 2; // Header CRC

    private final static int FEXTRA = 4; // Extra field

    private final static int FNAME = 8; // File name

    private final static int FCOMMENT = 16; // File comment

    /**
     * CRC-32 for uncompressed data.
     */
    protected CRC32 crc = new CRC32();

    /**
     * Indicates end of input stream.
     */
    protected boolean eos;

    private boolean closed = false;

    /**
     * Creates a new input stream with the specified buffer size.
     * 
     * @param in
     *                the input stream
     * @param size
     *                the input buffer size
     * @exception IOException
     *                    if an I/O error has occurred
     * @exception IllegalArgumentException
     *                    if size is <= 0
     */
    public GZIPImageInputStream(ImageInputStream iis) throws IOException {
        super(iis, new Inflater(true), 8192);
        usesDefaultInflater = true;
        readHeader();
        crc.reset();
    }

    /**
     * Reads GZIP member header.
     */
    private void readHeader() throws IOException {
        CheckedInputStream in = new CheckedInputStream(new InputStreamAdapter(
                this.iis), crc);
        crc.reset();
        // Check header magic
        if (readUShort(in) != GZIP_MAGIC) {
            throw new IOException("Not in GZIP format");
        }
        // Check compression method
        if (readUByte(in) != 8) {
            throw new IOException("Unsupported compression method");
        }
        // Read flags
        int flg = readUByte(in);
        // Skip MTIME, XFL, and OS fields
        skipBytes(in, 6);
        // Skip optional extra field
        if ((flg & FEXTRA) == FEXTRA) {
            skipBytes(in, readUShort(in));
        }
        // Skip optional eraf name
        if ((flg & FNAME) == FNAME) {
            while (readUByte(in) != 0)
                ;
        }
        // Skip optional eraf comment
        if ((flg & FCOMMENT) == FCOMMENT) {
            while (readUByte(in) != 0)
                ;
        }
        // Check optional header CRC
        if ((flg & FHCRC) == FHCRC) {
            int v = (int) crc.getValue() & 0xffff;
            if (readUShort(in) != v) {
                throw new IOException("Corrupt GZIP header");
            }
        }
    }

    /**
     * Reads GZIP member trailer.
     */
    private void readTrailer() throws IOException {
        InputStream in = new InputStreamAdapter(this.iis);
        int n = inf.getRemaining();
        if (n > 0) {
            in = new SequenceInputStream(new ByteArrayInputStream(buf, len - n,
                    n), in);
        }
        // Uses left-to-right evaluation order
        if ((readUInt(in) != crc.getValue()) ||
        // rfc1952; ISIZE is the input size modulo 2^32
                // TODO: improve this test (getBytesWritten() method from 1.5
                // should be preferred)
                (readUInt(in) != (inf.getTotalOut() & 0xffffffffL)))
            throw new IOException("Corrupt GZIP trailer");
    }

    /**
     * Reads uncompressed data into an array of bytes. Blocks until enough input
     * is available for decompression.
     * 
     * @param buf
     *                the buffer into which the data is read
     * @param off
     *                the start offset of the data
     * @param len
     *                the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     *         compressed input stream is reached
     * @exception IOException
     *                    if an I/O error has occurred or the compressed input
     *                    data is corrupt
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        checkClosed();
        if (eos) {
            return -1;
        }
        len = super.read(buf, off, len);
        if (len == -1) {
            readTrailer();
            eos = true;
        } else {
            crc.update(buf, off, len);
        }
        return len;
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
            super.close();
            eos = true;
            closed = true;
        }
    }

    /**
     * Reads unsigned integer in Intel byte order.
     */
    private long readUInt(InputStream in) throws IOException {
        long a = readUShort(in);
        long b = readUShort(in);
        return ((b & 0xffff) << 16) | (a & 0xffff);
    }

    /**
     * Reads unsigned short in Intel byte order.
     */
    private int readUShort(InputStream in) throws IOException {
        int a = readUByte(in);
        int b = readUByte(in);
        return ((b & 0xff) << 8) | (a & 0xff);
    }

    /**
     * Reads unsigned byte.
     */
    private int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b & 0xff;
    }

    /**
     * Skips bytes of input data blocking until all bytes are skipped. Does not
     * assume that the input stream is capable of seeking.
     */
    private void skipBytes(InputStream in, int n) throws IOException {
        while (n > 0) {
            int len = in.read(tmpbuf, 0, n < tmpbuf.length ? n : tmpbuf.length);
            if (len == -1) {
                throw new EOFException();
            }
            n -= len;
        }
    }
}
