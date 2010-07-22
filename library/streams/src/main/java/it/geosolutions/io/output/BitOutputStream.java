/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 *    JGriB1 - OpenSource Java library for GriB files edition 1
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package it.geosolutions.io.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Providing ability to write decorate outputstream with a bit oriented
 * interface.
 *
 * @author Simone Gianecchini
 */
public final class BitOutputStream extends FilterOutputStream {
    //buffersize
    private static final byte bufferSize = 32;

    /**
     * Buffer, it is 32 bits long. We cannot use a long because the underlying
     * interface is working at most with integers.
     */
    int bitbuffer;

    //number of bits written
    int count;

    public BitOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes some bits (max 8) from the specified byte to stream.
     *
     * @param b int which should be written
     * @param nbits bit count to write
     *
     * @throws IOException if an I/O error occurs
     */
    public void write(byte b, int nbits) throws IOException {
        if ((nbits <= 0) || (nbits > 8)) {
            return;
        }

        byte remainingBits = 0;
        byte bits2Write = b;

        //do we have enough room for this byte?
        //if not flush everything
        if ((this.count + nbits) > BitOutputStream.bufferSize) {
            //getting space available to write bits
            remainingBits = (byte) (BitOutputStream.bufferSize - this.count);

            //getting the buts to write
            //from the leftmost
            bits2Write >>>= (nbits - remainingBits);

            //making room in bitbuffer
            bitbuffer <<= remainingBits;

            //writing
            bitbuffer |= (((int) bits2Write) & MathUtils.bitMask[remainingBits]);
            count += remainingBits; //32

            this.flush();

            //decreasing bits to write
            nbits -= remainingBits;

            //bits left to be written
            bits2Write = (byte) (b & MathUtils.bitMask[nbits]);
        }

        //write to the buffer
        bitbuffer <<= nbits;
        bitbuffer |= (bits2Write & MathUtils.bitMask[nbits]);
        count += nbits;
    }

    /**
     * writes bits from buffer to output stream
     *
     * @throws IOException if I/O error occurs
     */
    public void flush() throws IOException {
        byte[] byte2Write = MathUtils.bitVector2ByteVector(bitbuffer, count);

        if (byte2Write != null) {
            out.write(byte2Write);
        }

        count = 0;
        bitbuffer = 0;

        //out .flush();
    }

    /**
     * Write a bit to this bit oriented stream.
     *
     * @param bit boolean
     *
     * @throws IOException DOCUMENT ME!
     */
    public void write(boolean bit) throws IOException {
        final byte b = bit ? (byte) 1 : 0;

        this.write(b, 1);
    }

    /**
     * Write an integer as a bit vector of the specified length.
     *
     * @param bitVect int
     * @param length DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void write(int bitVect, int length) throws IOException {
        //convert the bit vector
        byte[] byteVect = MathUtils.bitVector2ByteVector(bitVect, length);

        //check it
        if (byteVect == null) {
            return;
        }

        //everything's fine so far
        if (length <= 8) {
            this.write(byteVect[0], length);

            return;
        }

        //we need to know how many bits to write in the last octet
        byte lastOctetNumBits = (byte) (length
            - ((byte) Math.floor(length / 8.0) * 8));

        if (lastOctetNumBits == 0) {
            lastOctetNumBits = 8;
        }

        for (int i = 0; i < byteVect.length; i++) {
            if (i == 0) {
                this.write(byteVect[i], lastOctetNumBits);
            } else {
                this.write(byteVect[i], 8);
            }
        }
    }

    /**
     * Writes the specified <code>byte</code> to this bit-wise output stream.
     * 
     * <p>
     * The <code>write</code> method of <code>BitOutputStream</code> does nto
     * always cals the <code>write</code> method of its underlying output
     * stream.
     * </p>
     * 
     * <p>
     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.
     * </p>
     *
     * @param b the <code>byte</code>.
     *
     * @exception IOException if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        this.write(b, 8);
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream.
     * 
     * <p>
     * The <code>write</code> method of <code>FilterOutputStream</code> calls
     * its <code>write</code> method of one argument with the argument
     * <code>b</code>.
     * </p>
     * 
     * <p>
     * Note that this method does not call the one-argument <code>write</code>
     * method of its underlying stream with the single argument
     * <code>b</code>.
     * </p>
     *
     * @param b the data to be written.
     *
     * @exception IOException if an I/O error occurs.
     *
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    public void write(byte[] b) throws IOException {
        flush();
        out.write(b, 0, b.length);
    }

    /**
     * Writes <code>len</code> bytes from the specified <code>byte</code> array
     * starting at offset <code>off</code> to this output stream.
     * 
     * <p>
     * The <code>write</code> method of <code>FilterOutputStream</code> calls
     * the <code>write</code> method of one argument on each <code>byte</code>
     * to output.
     * </p>
     * 
     * <p>
     * Note that this method does not call the <code>write</code> method of its
     * underlying input stream with the same arguments.
     * </p>
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     *
     * @exception IOException if an I/O error occurs.
     *
     * @see java.io.FilterOutputStream#write(int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        flush();
        out.write(b, off, len);
    }
}
