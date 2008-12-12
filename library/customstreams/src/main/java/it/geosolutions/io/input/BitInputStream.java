/*
 * Copyright (c) Andrey Kuznetsov. All Rights Reserved.
 *
 * http://uio.imagero.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of imagero Andrei Kouznetsov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.io.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * adds ability to read streams bitewise and also to read predefined amount of
 * bits every read() call
 * 
 * @author Andrey Kuznetsov
 */
public class BitInputStream extends FilterInputStream {

    int vbits = 0;

    int bitbuf = 0;

    private int bitsToRead = 8;

    public BitInputStream(InputStream in) {
        super(in);
    }

    /**
     * how much bits is read every read() call (default - 8)
     */
    public int getBitsToRead() {
        return bitsToRead;
    }

    /**
     * set how much bits is read every read() call (max 8)
     * 
     * @param bitsToRead
     */
    public void setBitsToRead(int bitsToRead) {
        if (bitsToRead > 8) {
            throw new IllegalArgumentException();
        }
        this.bitsToRead = bitsToRead;
    }

    public int read() throws IOException {
        return read(bitsToRead);
    }

    public int read(int nbits) throws IOException {
        int ret;
        // nothing to read
        if (nbits == 0) {
            return 0;
        }
        // too many bits requested
        if (nbits > 8) {
            throw new IllegalArgumentException("max 8 bits can be read at once");
        }
        // not anough bits in buffer
        if (nbits > vbits) {
            fillBuffer(nbits);
        }
        // buffer still empty => we are reached EOF
        if (vbits == 0) {
            return -1;
        }
        ret = bitbuf << (32 - vbits) >>> (32 - nbits);
        vbits -= nbits;

        if (vbits < 0) {
            vbits = 0;
        }

        return ret;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads data from input stream into an byte array.
     * 
     * @param b
     *                the buffer into which the data is read.
     * @param off
     *                the start offset of the data.
     * @param len
     *                the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or -1 if the EOF
     *         has been reached.
     * @exception IOException
     *                    if an I/O error occurs.
     * @exception NullPointerException
     *                    if supplied byte array is null
     */
    public int read(byte b[], int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;

        int i = 1;
        for (; i < len; ++i) {
            c = read();
            if (c == -1) {
                break;
            }
            b[off + i] = (byte) c;
        }
        return i;
    }

    /**
     * empties bit buffer.
     */
    public void resetBuffer() {
        vbits = 0;
        bitbuf = 0;
    }

    /**
     * Skips some bytes from the input stream. If bit buffer is not empty, n -
     * (vbits + 8) / 8 bytes skipped, then buffer is resetted and filled with
     * same amount of bits as it has before skipping.
     * 
     * @param n
     *                the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @exception IOException
     *                    if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        if (vbits == 0) {
            return in.skip(n);
        } else {
            int b = (vbits + 8) / 8;
            in.skip(n - b);
            int vbits = this.vbits;
            resetBuffer();
            fillBuffer(vbits);
            return n;
        }
    }

    private void fillBuffer(int nbits) throws IOException {
        int c;
        while (vbits < nbits) {
            c = in.read();
            if (c == -1) {
                break;
            }
            bitbuf = (bitbuf << 8) + (c & 0xFF);
            vbits += 8;
        }
    }

    public int getBitOffset() {
        return 7 - (vbits % 8);
    }
}
