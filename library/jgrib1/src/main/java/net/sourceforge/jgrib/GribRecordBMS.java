/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * GribRecordBMS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 */
package net.sourceforge.jgrib;

import it.geosolutions.io.output.BitOutputStream;
import it.geosolutions.io.output.MathUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.stream.ImageInputStream;


/**
 * A class that represents the bitmap section (BMS) of a GRIB record. It
 * indicates grid points where no parameter value is defined.
 *
 * @author Benjamin Stark
 * @version 1.0
 */
public final class GribRecordBMS {

	/** Logger for the GribFile class. */
	private final static Logger LOGGER = Logger.getLogger(GribRecordBMS.class.toString());
	
    private static final int[] bitmask = { 128, 64, 32, 16, 8, 4, 2, 1 };

    /** Length in bytes of this section. */
    private int length;

    /** The bit map. */
    private boolean[] bitmap;
    private int zero_fill;
    private int size;
    private long pos;
    private int numBits;
    private ImageInputStream inStream;


    /**
     * GribRecordBMS
     *
     * @param bitmap String
     */
    public GribRecordBMS(final boolean[] bitmap) {
        GribFileUtilities.ensureNotNull("bitmap", bitmap);

        this.bitmap = bitmap.clone();

        /**
         * STEP 1 check the length and see if we need to zerofill the bitmap to
         * reach number of bits which is a multiple of 8.
         */
        zero_fill = 0;

        if ((this.bitmap.length % 8) > 0) {
            zero_fill = 8 - (this.bitmap.length % 8);
        }

        /**
         * STEP 2 start writing things we need.
         */
        //length of section 1 (3 octets)
        this.length = ((this.bitmap.length + zero_fill) / 8) + 6;

        this.setLength();
    }

    // *** public methods *********************************************************

    /**
     * DOCUMENT ME!
     *
     * @param in
     *
     * @throws IOException
     * @throws NoValidGribException
     */
    public GribRecordBMS(final ImageInputStream in)
        throws IOException {
        // octets 1-3 (length of section)
        this.length = MathUtils.uint3(in.read(), in.read(), in.read());

        //octet 4 -unused bits in order to zero fill to an even number of bytes
        this.zero_fill = in.read();

        // octets 5-6  bit map follows 
        //        otherwise - the numeric refers to a predefined bit map provided by the center 
        if ((in.read() != 0) || (in.read() != 0)) {
            throw new IllegalStateException(
                "GribRecordBMS: No bit map defined here.");
        }

        //pos and size
        this.pos = in.getStreamPosition();
        this.inStream = in;
        this.size = length - 6; //header of BMS is 6 octects long
        this.numBits = ((this.length - 6) * 8) - zero_fill;
        in.skipBytes(size);
     
    }

    // *** constructors *******************************************************

    /**
     * Constructs a <tt>GribRecordBMS</tt> object from a bit input stream.
     *
     * @throws IOException if stream can not be opened etc.
     */
    private void parseBMS() throws IOException {
        final byte[] data = new byte[this.size];
        this.inStream.seek(this.pos);
        this.inStream.read(data);

        // create new bit map, octet 4 contains number of unused bits at the end
        this.bitmap = new boolean[this.numBits];

        // fill bit map
        for (int i = 0; i < numBits; i++) {
            this.bitmap[i] = (data[(i / 8)] & bitmask[i % 8]) != 0;
        }
    }

    /**
     * Get length in bytes of this section.
     *
     * @return length in bytes
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Get bit map.
     *
     * @return bit map as array of boolean values
     *
     * @throws IOException
     */
    public boolean[] getBitmap() throws IOException {
        if (this.bitmap == null) {
            parseBMS();
        }

        return this.bitmap;
    }

    /**
     * Get a string representation of this BMS.
     *
     * @return string representation of this BMS
     */
    public String toString() {
        return "    BMS section:" + '\n' + "        bitmap length: "
        + this.bitmap;
    }

    /**
     * writeTo
     *
     * @param out OutputStream
     *
     * @throws IOException DOCUMENT ME!
     */
    public void writeTo(OutputStream out) throws IOException {
    	//setting length
        this.setLength();
        
        //for output purposes
    	ByteArrayOutputStream outComm = new ByteArrayOutputStream(this.length);

        //bit wise output stream
    	BitOutputStream bitOut = new BitOutputStream(outComm);



        //length
        outComm.write(new byte[] {
                (byte) (this.length >> 16), (byte) (this.length >> 8),
                (byte) (this.length)
            });

        //zero filling bits (1 octets)
        outComm.write(new byte[] { (byte) zero_fill });

        //bitmap follows (2 octets)
        outComm.write(new byte[] { 0, 0 });

        //bitmap
        final int bLength =this.bitmap.length;
        final int totalLength=bLength + this.zero_fill;
        for (int i = 0; i < totalLength; i++) {
            if (i < bLength) {
                bitOut.write(this.bitmap[i]);
            } else {
                bitOut.write(false); //FILLING
            }
        }

        bitOut.close();

        //write out
        out.write(outComm.toByteArray(),0,outComm.size());
    }

    private void setLength() {
        //length and filling padding
        //total bits used
        int unusedBits = 0;

        //how many values?
        int totalNumberOfValues = this.bitmap.length;

        //computing octets used to reach an integer number of bytes.
        int byteUsed = (int) Math.ceil(totalNumberOfValues / 8.0);

        //we need an even number of octests for the bitmap cause we hae 3 bytes for the
        //length 1 for the unused bits number and 2 for the flags --> Total of N+6 octects
        //where N are used byt the bitmap itself.
        if ((byteUsed % 2) == 1) {
            byteUsed++;
            unusedBits = 8;
        }

        int partialUnusedBits = totalNumberOfValues
            - ((int) Math.floor(totalNumberOfValues / 8.0) * 8);

        if (partialUnusedBits > 0) {
            unusedBits += (8 - partialUnusedBits);
        }

        //setting length
        this.length = byteUsed + 3 + 1 + 2;

        //setting zero filling
        this.zero_fill = unusedBits;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GribRecordBMS)) {
            return false;
        }

        if (this == obj) {
            // Same object
            return true;
        }

        GribRecordBMS bms = (GribRecordBMS) obj;
        boolean[] bms1 = null;
        boolean[] bms2 = null;

        try {
            bms1 = bms.getBitmap();
            bms2 = this.getBitmap();
        } catch (IOException e) {
            if(LOGGER.isLoggable(Level.WARNING))
            	LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
            return false;
        }

        if (bms1.length != bms2.length) {
            return false;
        }

        if (this.zero_fill != bms.zero_fill) {
            return false;
        }

        for (int i = 0; i < bms1.length; i++) {
            if (bms1[i] != bms2[i]) {
                return false;
            }
        }

        return true;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
