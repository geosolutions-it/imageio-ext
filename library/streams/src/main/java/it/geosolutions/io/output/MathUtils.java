/**
 *   MathUtils.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 * (c) simone giannecchini 

 */
package it.geosolutions.io.output;


/**A class that contains several static utility methods.
 *
 * A class that contains several static methods for converting multiple bytes into
 * one float or integer.
 *
 * @author  Benjamin Stark
 * @author  Simone Giannecchini
 * @version 1.1
 */
public final class MathUtils {
    //bitMask used to Mask data received
    static final int[] bitMask = { 0, 1, 3, 7, 15, 31, 63, 127, 255 };

    /**Convert two bytes into a signed integer.
     *
     * Convert two bytes into a signed integer.
     *
     * @param a higher byte
     * @param b lower byte
     *
     * @return integer value
     */
    public static int int2(final int a,final  int b) {
        return (1 - ((a & 128) >> 6)) * ((((a & 255) & 127) << 8) | (b & 255));
    }

    /**Convert three bytes into a signed integer.
     *
     * Convert three bytes into a signed integer.
     *
     * @param a higher byte
     * @param b middle part byte
     * @param c lower byte
     *
     * @return integer value
     */
    public static int int3(final int a, final int b,final  int c) {
        return (1 - ((a & 128) >> 6)) * ((((a & 255) & 127) << 16)
        | ((b & 255) << 8) | (c & 255));
    }

    /**Convert four bytes into a signed integer.
     *
     * Convert four bytes into a signed integer.
     *
     * @param a highest byte
     * @param b higher middle byte
     * @param c lower middle byte
     * @param d lowest byte
     *
     * @return integer value
     */
    public static int int4(final int a, final int b, final int c, final int d) {
        return (1 - ((a & 128) >> 6)) * ((((a & 255) & 127) << 24)
        | ((b & 255) << 16) | ((c & 255) << 8) | (d & 255));
    }

    /**Convert two bytes into an unsigned integer.
     *
     * Convert two bytes into an unsigned integer.
     *
     * @param a higher byte
     * @param b lower byte
     *
     * @return integer value
     */
    public static int uint2(final int a,final  int b) {
        return ((a & 255) << 8) | (b & 255);
    }

    /**Convert three bytes into an unsigned integer.
     *
     * Convert three bytes into an unsigned integer.
     *
     * @param a higher byte
     * @param b middle byte
     * @param c lower byte
     *
     * @return integer value
     */
    public static int uint3(final int a,final  int b, final int c) {
        return ((a & 255) << 16) | ((b & 255) << 8) | (c & 255);
    }

    /**Convert four bytes into a float value.
     *
     * Convert four bytes into a float value.
     *
     * @param a highest byte
     * @param b higher byte
     * @param c lower byte
     * @param d lowest byte
     *
     * @return float value
     */
    public static double IBM2FLoat(final int a,final  int b,final  int c,final  int d) {
        boolean positive = true;
        int power = 0;
        int abspower = 0;
        int mant = 0;

        double value = 0.0;
        double exp = 0.0;

        positive = (a & 0x80) == 0;
        mant = MathUtils.uint3(b, c, d);
        power = (a & 0x7f) - 64;
        abspower = (power > 0) ? power : (-power);

        /* calc exp */
        exp = 16.0;
        value = 1.0;

        while (abspower != 0) {
            if ((abspower & 1) != 0) {
                value *= exp;
            }

            exp = exp * exp;
            abspower >>= 1;
        }

        if (power < 0) {
            value = 1.0 / value;
        }

        value = (value * mant) / 16777216.0;

        if (!positive) {
            value = -value;
        }

        return value;
    }

    /**Converts a double to the standard IBM representation for a single precision real floating point number.
     *
     * Converts a float to the standard IBM representation for a single precision real floating point number.
     * This code is heavily based on code from gribw a c utility to encode
     * grib files (see <A HREF="http://www.cpc.ncep.noaa.gov/products/wesley/gribw.html">
     * gribw(riter)</A> )}.
     * Many thanks to the author.
     */
    public static byte[] Float2IBM(double fVal) {
        //ret value
    	final byte[] ibm = new byte[4];
        int sign = 0;
        int exp = 0;
        double mant = 0.0d;
        int imant = 0;

        //do we need to proceed
        if (fVal == 0.0) {
            return ibm;
        }

        //setting sign
        if (fVal < 0.0) {
            sign = 128;
            fVal = -fVal;
        }
        else {
            sign = 0;
        }

        mant = MathUtils.frexpMant(fVal);
        exp = MathUtils.frexpExp(fVal);

        if (mant >= 1.0) {
            mant = 0.5;
            exp++;
        }

        while ((exp & 3) != 0) {
            mant *= 0.5;
            exp++;
        }

        imant = (int) (Math.floor((mant * 256.0 * 256.0 * 256.0) + 0.5));

        if (imant >= (256 * 256 * 256)) {
            imant = (int) (Math.floor((mant * 16.0 * 256.0 * 256.0) + 0.5));
            exp -= 4;
        }

        exp = (exp / 4) + 64;

        if (exp < 0) {
            System.err.println("underflow in flt2ibm");

            return null;
        }

        if (exp > 127) {
            System.err.println("overflow in flt2ibm");

            //ibm[0] = (byte)(sign | 127);
            //ibm[1] = ibm[2] = ibm[3]=255;
            return null;
        }

        ibm[0] = (byte) (sign | exp);
        ibm[3] = (byte) (imant & 255);
        ibm[2] = (byte) ((imant >> 8) & 255);
        ibm[1] = (byte) ((imant >> 16) & 255);

        return ibm;
    }

    /**Convert an integer containing length bits into a vector of bytes.
     *
     * Convert an integer containing length bits into a vector of bytes. The most signifiant byte is returned
     * firs. The last byte contains the less signifiant part of the bitvector.
     *
     * @param bitVector int The bit vector to convert.
     * @param length int The number of bits in the vector.
     *
     * @return byte[] Byte vector.
     */
    public static byte[] bitVector2ByteVector(final int bitVector,final int length) {
        if ((length <= 0) || (length > 32)) {
            return null;
        }

        //how many octets?
        final byte octetsNumber = (byte) Math.ceil(length / 8.0);

        //last octet bits number to read
        final byte lastOctetNumBits = (byte) (((length % 8) != 0) ? (length % 8) : 8);

        //return value
        final byte[] retVal = new byte[(byte) Math.ceil(length / 8.0)];

        for (byte i = octetsNumber; i > 0; i--) {
            if (i == octetsNumber) {
                retVal[octetsNumber - i] = (byte) ((bitVector >> ((i - 1) * 8))
                    & MathUtils.bitMask[lastOctetNumBits]);
            }
            else {
                retVal[octetsNumber - i] = (byte) ((bitVector >> ((i - 1) * 8))
                    & MathUtils.bitMask[8]);
            }
        }

        //sign
        return retVal;
    }

    /**log2.
     *
     * log2.
     *
     * @param aFloat float
     */
    static public double log2(final double val) {
        return (Math.log(val) / Math.log(2));
    }

    /**exp2.
     *
     * exp2.
     *
     * @param val double
     */
    static public double exp2(final double val) {
        return Math.exp(val * Math.log(2));
    }

    /**signedInt2Bytes, converts a signed integer to a vector of bytes.
     *
     * signedInt2Bytes, converts a signed integer to a vector of bytes.
     */
    static public byte[] signedInt2Bytes(int val, final int numBytes) {
        //sign
        boolean negative = false;

        if (val < 0) {
            negative = true;
            val = -val;
        }

        final byte[] retVal = new byte[numBytes];

        switch (numBytes) {
        case 4:
            retVal[0] = ((byte) ((val >> 24) & 255));
            retVal[1] = ((byte) ((val >> 16) & 255));
            retVal[2] = ((byte) ((val >> 8) & 255));
            retVal[3] = ((byte) ((val) & 255));

            break;

        case 3:
            retVal[0] = ((byte) ((val >> 16) & 255));
            retVal[1] = ((byte) ((val >> 8) & 255));
            retVal[2] = ((byte) ((val) & 255));

            break;

        case 2:
            retVal[0] = ((byte) ((val >> 8) & 255));
            retVal[1] = ((byte) ((val) & 255));

            break;

        case 1:
            retVal[0] = ((byte) ((val) & 255));

            break;

        default:
            return null;
        }

        //SIGN
        retVal[0] |= (negative ? (1 << 7) : (0 << 7));

        return retVal;
    }

    /**frexpMant, builds and gives the mantissa base 2 for the floating point representation of a real number.
     *
     * frexpMant, builds and gives the mantissa base 2 for the floating point representation of a real number.
     */
    public static double frexpMant(final double val) {
        //getting ieee 754 representation
        int intBits = Float.floatToIntBits((float) val);

        //getting the mantissa out of it
        intBits &= ((int) Math.pow(2, 23) - 1);

        double mantissa = (intBits * Math.pow(2, -23)) + 1; //adding hidden bit

        //natissa should stay in between 1/2 and 1
        mantissa /= 2;

        return mantissa;
    }

    /**frexpExp, builds and  gives the exponent base 2 for the floating point representation of a real number.
     *
     * frexpExp, builds give the exponent base 2 for the floating point representation of a real number.
     */
    public static byte frexpExp(final double val) {
        //getting ieee 754 representation
        int intBits = Float.floatToIntBits((float) val);

        //removing sign bit
        intBits >>>= 23;

        //shifting to get the sign
        intBits &= 0xff;

        //getting the sign
        intBits &= 255;

        //removing the bias
        intBits -= 127;

        //adding 1 because i have to take into account that ieee 754 single precision
        //deals with normalized mantissa using a hidden bit
        //terefore at the end the mantissa is 1.something
        //we need mantissa to be in between 1 and 1/2
        return (byte) ++intBits;
    }
}
