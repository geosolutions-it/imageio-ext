/**
 * GribRecordBMS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 * Simone Giannecchini (simboss@tiscali.it) 2005
 */
package it.geosolutions.io.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an input stream wrapper that can read a specific number of
 * bytes and bits from an input stream.
 *
 * @author  Benjamin Stark
 * @author  Simone Giannecchini
 * @version 1.1
 */
public final class BitInputStream extends FilterInputStream {
    /**
     * Buffer for one byte which will be processed bit by bit.
     */
    private int bitBuf = 0;

    /**
     * Current bit position in <tt>bitBuf</tt>.
     */
    private int bitPos = 0;

    /**
     * Constructs a bit input stream from an <tt>InputStream</tt> object.
     *
     * @param in input stream that will be wrapped
     */
    public BitInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read an unsigned 8 bit value.
     *
     * @return unsigned 8 bit value as integer
     */
    public int readUI8()
        throws IOException {
        int ui8 = in.read();

        if (ui8 < 0) {
            throw new IOException("BitInputStream::readUI8:End of input.");
        }

        return ui8;
    }

    /**
     * Read specific number of unsigned bytes from the input stream.
     *
     * @param length number of bytes to read and return as integers
     *
     * @return unsigned bytes as integer values
     */
    public int[] readUI8(final int length)
        throws IOException {
        int[] data = new int[length];
        int read = 0;

        for (int i = 0; (i < length) && (read >= 0); i++) {
            data[i] = read = this.read();
        }

        if (read < 0) {
            throw new IOException("BitInputStream::readUI8(final int length): of input.");
        }

        return data;
    }

    /**
     * Read specific number of bytes from the input stream.
     *
     * @param length number of bytes to read
     *
     * @return array of read bytes
     */
    public byte[] read(final int length)
        throws IOException {
        final byte[] data = new byte[length];

        final int numRead = this.read(data);

        if (numRead < length) {
            // retry reading
            final int numReadRetry = this.read(data, numRead, data.length - numRead);

            if ((numRead + numReadRetry) < length) {
                throw new IOException("BitInputStream::read(final int length):Unexpected end of input.");
            }
        }

        return data;
    }

    /**
     * Read an unsigned value from the given number of bits.
     *
     * @param numBits number of bits used for the unsigned value
     *
     * @return value read from <tt>numBits</tt> bits as long
     */
    public long readUBits(final int numBits)
        throws IOException {
        if (numBits == 0) {
            return 0;
        }

        int bitsLeft = numBits;
        long result = 0;

        if (this.bitPos == 0) {
            this.bitBuf = in.read();
            this.bitPos = 8;
        }
        int shift=0;
        while (true) {
            shift = bitsLeft - this.bitPos;

            if (shift > 0) {
                // Consume the entire buffer
                result |= (this.bitBuf << shift);
                bitsLeft -= this.bitPos;

                // Get the next byte from the input stream
                this.bitBuf = in.read();
                this.bitPos = 8;
            }
            else {
                // Consume a portion of the buffer
                result |= (this.bitBuf >> -shift);
                this.bitPos -= bitsLeft;
                this.bitBuf &= (0xff >> (8 - this.bitPos)); // mask off consumed bits

                return result;
            }
        }
    }

    /**
     * Read a signed value from the given number of bits
     *
     * @param numBits number of bits used for the signed value
     *
     * @return value read from <tt>numBits</tt> bits as integer
     */
    public int readSBits(final int numBits)
        throws IOException {
        // Get the number as an unsigned value.
    	long uBits = readUBits(numBits);

        // Is the number negative?
        if ((uBits & (1L << (numBits - 1))) != 0) {
            // Yes. Extend the sign.
            uBits |= (-1L << numBits);
        }

        return (int) uBits;
    }
}
