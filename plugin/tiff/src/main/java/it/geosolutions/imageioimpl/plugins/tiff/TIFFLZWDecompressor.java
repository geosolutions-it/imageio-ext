/*
 * $RCSfile: TIFFLZWDecompressor.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 05:01:48 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFDecompressor;
import it.geosolutions.imageio.plugins.tiff.TIFFTag;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;


public class TIFFLZWDecompressor extends TIFFDecompressor {

    private static final boolean DEBUG = false;

    private static final int andTable[] = {
	511, 
	1023,
	2047,
	4095
    };

    int predictor;

    byte[] srcData;
    byte[] dstData;

    int srcIndex;
    int dstIndex;

    byte stringTable[][];
    int tableIndex, bitsToGet = 9;

    int nextData = 0;
    int nextBits = 0;

    public TIFFLZWDecompressor(int predictor) throws IIOException {
        super();

        if (predictor != BaselineTIFFTagSet.PREDICTOR_NONE && 
            predictor != 
            BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            throw new IIOException("Illegal value for Predictor in " +
                                   "TIFF file");
        }

        if(DEBUG) {
            System.out.println("Using horizontal differencing predictor");
        }

        this.predictor = predictor;
    }

    public void decodeRaw(byte[] b,
                          int dstOffset,
                          int bitsPerPixel,
                          int scanlineStride) throws IOException {

        // Check bitsPerSample.
        if (predictor == 
            BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            int len = bitsPerSample.length;
            final int bps=bitsPerSample[0];
            if (bps != 8 && bps != 16) {
                throw new IIOException
                    (bps + "-bit samples "+
                     "are not supported for Horizontal "+
                     "differencing Predictor");
            }
            for(int i=1;i<len;i++) {
                if(bitsPerSample[i]!=bps) {
                    throw new IIOException
                        ("Varying sample width is not "+
                         "supported for Horizontal "+
                         "differencing Predictor (first: "+
                         bps+", unexpected:"+bitsPerSample[i]+")");
                }
            }
        }

        stream.seek(offset);

        byte[] sdata = new byte[byteCount];
        stream.readFully(sdata);

        int bytesPerRow = (srcWidth*bitsPerPixel + 7)/8;
        byte[] buf;
        int bufOffset;
        if(bytesPerRow == scanlineStride) {
            buf = b;
            bufOffset = dstOffset;
        } else {
            buf = new byte[bytesPerRow*srcHeight];
            bufOffset = 0;
        }

        int numBytesDecoded = decode(sdata, 0, buf, bufOffset);

        if(bytesPerRow != scanlineStride) {
            if(DEBUG) {
                System.out.println("bytesPerRow != scanlineStride");
            }
            int off = 0;
            for (int y = 0; y < srcHeight; y++) {
                System.arraycopy(buf, off, b, dstOffset, bytesPerRow);
                off += bytesPerRow;
                dstOffset += scanlineStride;
            }
        }
    }

    public int decode(byte[] sdata, int srcOffset,
                      byte[] ddata, int dstOffset)
        throws IOException {
        if (sdata[0] == (byte)0x00 && sdata[1] == (byte)0x01) {
            throw new IIOException
                ("TIFF 5.0-style LZW compression is not supported!");
        }

        this.srcData = sdata;
        this.dstData = ddata;

        this.srcIndex = srcOffset;
        this.dstIndex = dstOffset;

	this.nextData = 0;
	this.nextBits = 0;

        initializeStringTable();

	int code, oldCode = 0;
	byte[] string;

	while ((code = getNextCode()) != 257) {
	    if (code == 256) {
		initializeStringTable();
		code = getNextCode();
		if (code == 257) {
		    break;
		}

		writeString(stringTable[code]);
		oldCode = code;
	    } else {
		if (code < tableIndex) {
		    string = stringTable[code];

		    writeString(string);
		    addStringToTable(stringTable[oldCode], string[0]); 
		    oldCode = code;
		} else {
		    string = stringTable[oldCode];
		    string = composeString(string, string[0]);
		    writeString(string);
		    addStringToTable(string);
		    oldCode = code;
		}
	    }
	}

        if (predictor == BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            if(bitsPerSample[0]==8) {
                for (int j = 0; j < srcHeight; j++) {
                    int count = dstOffset + samplesPerPixel * (j * srcWidth + 1);
                    for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                        dstData[count] += dstData[count - samplesPerPixel];
                        count++;
                    }
                }
            }
            else if(bitsPerSample[0]==16) {
                if(stream.getByteOrder()==ByteOrder.LITTLE_ENDIAN) {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 2;
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr=(((int)dstData[count]) & 0xFF) + (dstData[count+1]<<8);
                            int prev=(((int)dstData[count-samplesPerPixel*2]) & 0xFF)+(dstData[count+1-samplesPerPixel*2]<<8);
                            curr+=prev;
                            dstData[count]=(byte)curr;
                            dstData[count+1]=(byte)(curr>>8);
                            count+=2;
                        }
                    }
                }
                else
                {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 2;
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr=(((int)dstData[count+1]) & 0xFF) + (dstData[count]<<8);
                            int prev=(((int)dstData[count+1-samplesPerPixel*2]) & 0xFF)+(dstData[count-samplesPerPixel*2]<<8);
                            curr+=prev;
                            dstData[count+1]=(byte)curr;
                            dstData[count]=(byte)(curr>>8);
                            count+=2;
                        }
                    }
                }
            }
            else throw new IIOException("Unexpected branch of Horizontal differencing Predictor, bps="+bitsPerSample[0]);
        }

        return dstIndex - dstOffset;
    }

    /**
     * Initialize the string table.
     */
    public void initializeStringTable() {
	stringTable = new byte[4096][];
	
	for (int i = 0; i < 256; i++) {
	    stringTable[i] = new byte[1];
	    stringTable[i][0] = (byte)i;
	}
	
	tableIndex = 258;
	bitsToGet = 9;
    }

    /**
     * Write out the string just uncompressed.
     */
    public void writeString(byte string[]) {
        if(dstIndex < dstData.length) {
            int maxIndex = Math.min(string.length,
				    dstData.length - dstIndex);

            for (int i=0; i < maxIndex; i++) {
                dstData[dstIndex++] = string[i];
            }
        }
    }
    
    /**
     * Add a new string to the string table.
     */
    public void addStringToTable(byte oldString[], byte newString) {
	int length = oldString.length;
	byte string[] = new byte[length + 1];
	System.arraycopy(oldString, 0, string, 0, length);
	string[length] = newString;
	
	// Add this new String to the table
	stringTable[tableIndex++] = string;
	
	if (tableIndex == 511) {
	    bitsToGet = 10;
	} else if (tableIndex == 1023) {
	    bitsToGet = 11;
	} else if (tableIndex == 2047) {
	    bitsToGet = 12;
	} 
    }

    /**
     * Add a new string to the string table.
     */
    public void addStringToTable(byte string[]) {
	// Add this new String to the table
	stringTable[tableIndex++] = string;
	
	if (tableIndex == 511) {
	    bitsToGet = 10;
	} else if (tableIndex == 1023) {
	    bitsToGet = 11;
	} else if (tableIndex == 2047) {
	    bitsToGet = 12;
	} 
    }

    /**
     * Append <code>newString</code> to the end of <code>oldString</code>.
     */
    public byte[] composeString(byte oldString[], byte newString) {
	int length = oldString.length;
	byte string[] = new byte[length + 1];
	System.arraycopy(oldString, 0, string, 0, length);
	string[length] = newString;

	return string;
    }

    // Returns the next 9, 10, 11 or 12 bits
    public int getNextCode() {
        // Attempt to get the next code. The exception is caught to make
        // this robust to cases wherein the EndOfInformation code has been
        // omitted from a strip. Examples of such cases have been observed
        // in practice.

        try {
            nextData = (nextData << 8) | (srcData[srcIndex++] & 0xff);
            nextBits += 8;

            if (nextBits < bitsToGet) {
                nextData = (nextData << 8) | (srcData[srcIndex++] & 0xff);
                nextBits += 8;
            }

            int code =
                (nextData >> (nextBits - bitsToGet)) & andTable[bitsToGet - 9];
            nextBits -= bitsToGet;

            return code;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Strip not terminated as expected: return EndOfInformation code.
            return 257;
        }
    }
}

