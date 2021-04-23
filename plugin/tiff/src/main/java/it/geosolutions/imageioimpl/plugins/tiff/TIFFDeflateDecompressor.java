/*
 * $RCSfile: TIFFDeflateDecompressor.java,v $
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
 * $Date: 2005/02/11 05:01:45 $
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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOException;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFDecompressor;

public class TIFFDeflateDecompressor extends TIFFDecompressor {

    private static final boolean DEBUG = false;

    Inflater inflater = null;
    int predictor;

    public TIFFDeflateDecompressor(int predictor) throws IIOException {
        inflater = new Inflater();

        if (predictor != BaselineTIFFTagSet.PREDICTOR_NONE &&
            predictor != BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING &&
            predictor != BaselineTIFFTagSet.PREDICTOR_FLOATING_POINT) {
            throw new IIOException("Illegal value for Predictor in TIFF file");
        }

        if(DEBUG) {
            System.out.println("Using horizontal differencing predictor");
        }

        this.predictor = predictor;
    }

    public synchronized void decodeRaw(byte[] b,
                                       int dstOffset,
                                       int bitsPerPixel,
                                       int scanlineStride) throws IOException {

        // Check bitsPerSample.
        if (predictor == BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            int len = bitsPerSample.length;
            final int bps = bitsPerSample[0];
            if (bps != 8 && bps != 16 && bps != 32) {
                throw new IIOException
                    (bps + "-bit samples " +
                     "are not supported for Horizontal " +
                     "differencing Predictor");
            }
            for (int i = 0; i < len; i++) {
                if (bitsPerSample[i] != bps) {
                    throw new IIOException
                        ("Varying sample width is not " +
                         "supported for Horizontal " +
                         "differencing Predictor (first: " +
                         bps + ", unexpected:" + bitsPerSample[i] + ")");
                }
            }
        } else if (predictor == BaselineTIFFTagSet.PREDICTOR_FLOATING_POINT) {
            int len = bitsPerSample.length;
            final int bps = bitsPerSample[0];
            if (bps != 16 && bps != 24 && bps != 32 && bps != 64) {
                throw new IIOException
                    (bps + "-bit samples " +
                     "are not supported for Floating " +
                     "point Predictor");
            }
            for (int i = 0; i < len; i++) {
                if (bitsPerSample[i] != bps) {
                    throw new IIOException
                        ("Varying sample width is not " +
                         "supported for Floating " +
                         "point Predictor (first: " +
                         bps + ", unexpected:" + bitsPerSample[i] + ")");
                }
            }
            for (int sf : sampleFormat) {
                if (sf != BaselineTIFFTagSet.SAMPLE_FORMAT_FLOATING_POINT) {
                    throw new IIOException
                        ("Floating point Predictor not supported" +
                         "with " + sf + " data format");
                }
            }
        }

        // Seek to current tile data offset.
        stream.seek(offset);

        // Read the deflated data.
        byte[] srcData = new byte[byteCount];
        stream.readFully(srcData);

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

        // Set the input to the Inflater.
        inflater.setInput(srcData);

        // Inflate the data.
        try {
            inflater.inflate(buf, bufOffset, bytesPerRow*srcHeight);
        } catch(DataFormatException dfe) {
            throw new IIOException(I18N.getString("TIFFDeflateDecompressor0"),
                                   dfe);
        }

        // Reset the Inflater.
        inflater.reset();

        if (predictor == BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            if (bitsPerSample[0] == 8) {
                for (int j = 0; j < srcHeight; j++) {
                    int count = bufOffset + samplesPerPixel * (j * srcWidth + 1);
                    for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                        buf[count] += buf[count - samplesPerPixel];
                        count++;
                    }
                }
            }
            else if(bitsPerSample[0]==16) {
                if (stream.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 2;
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr=(((int)buf[count]) & 0xFF) | (buf[count+1]<<8);
                            int prev=(((int)buf[count-samplesPerPixel*2]) & 0xFF) | (buf[count+1-samplesPerPixel*2]<<8);
                            curr+=prev;
                            buf[count]=(byte)curr;
                            buf[count+1]=(byte)(curr>>8);
                            count+=2;
                        }
                    }
                }
                else
                {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 2;
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr=(((int)buf[count+1]) & 0xFF) | (buf[count]<<8);
                            int prev=(((int)buf[count+1-samplesPerPixel*2]) & 0xFF) | (buf[count-samplesPerPixel*2]<<8);
                            curr+=prev;
                            buf[count+1]=(byte)curr;
                            buf[count]=(byte)(curr>>8);
                            count+=2;
                        }
                    }
                }
            }
            else if(bitsPerSample[0]==32) {
                if (stream.getByteOrder() == ByteOrder.LITTLE_ENDIAN) {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 4;
                        int pbase = count - samplesPerPixel * 4;
                        int prev = readIntegerFromBuffer(buf, pbase, pbase + 1, pbase + 2, pbase + 3);
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr = readIntegerFromBuffer(buf, count, count +1, count +2, count + 3);
                            int sum = curr + prev;
                            buf[count] = (byte) (sum & 0xFF);
                            buf[count + 1] = (byte) ((sum >> 8) & 0xFF);
                            buf[count + 2] = (byte) ((sum >> 16) & 0xFF);
                            buf[count + 3] = (byte) ((sum >> 24) & 0xFF) ;
                            count += 4;
                            prev = sum;
                        }
                    }
                }
                else
                {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 4;
                        int pbase = count - samplesPerPixel * 4;
                        int prev = readIntegerFromBuffer(buf, pbase + 3, pbase + 2, pbase + 1, pbase);
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr = readIntegerFromBuffer(buf, count + 3, count + 2, count + 1, count);
                            int sum = curr + prev;
                            buf[count + 3] = (byte) (sum & 0xFF);
                            buf[count + 2] = (byte) (sum >> 8 & 0xFF);
                            buf[count + 1] = (byte) (sum >> 16 & 0xFF);
                            buf[count] = (byte) (sum >> 24 & 0xFF);
                            count += 4;
                            prev = sum;
                        }
                    }
                }
            }
            else throw new IIOException("Unexpected branch of Horizontal differencing Predictor, bps="+bitsPerSample[0]);
        } else if (predictor == BaselineTIFFTagSet.PREDICTOR_FLOATING_POINT) {
            int bytesPerSample = bitsPerSample[0] / 8;
            if (bytesPerRow % (bytesPerSample * samplesPerPixel) != 0) {
                throw new IIOException
                    ("The number of bytes in a row (" + bytesPerRow + ") is not divisible" +
                     "by the number of bytes per pixel (" + bytesPerSample * samplesPerPixel + ")");
            }

            for (int j = 0; j < srcHeight; j++) {
                int offset = bufOffset + j * bytesPerRow;
                int count = offset + samplesPerPixel;
                for (int i = samplesPerPixel; i < bytesPerRow; i++) {
                    buf[count] += buf[count - samplesPerPixel];
                    count++;
                }

                // Reorder the semi-BigEndian bytes.
                byte[] tmp = Arrays.copyOfRange(buf, offset, offset + bytesPerRow);
                int samplesPerRow = srcWidth * samplesPerPixel;
                if (stream.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                    for (int i = 0; i < samplesPerRow; i++) {
                        for (int k = 0; k < bytesPerSample; k++) {
                            buf[offset + i * bytesPerSample + k] = tmp[k * samplesPerRow + i];
                        }
                    }
                } else {
                    for (int i = 0; i < samplesPerRow; i++) {
                        for (int k = 0; k < bytesPerSample; k++) {
                            buf[offset + i * bytesPerSample + k] = tmp[(bytesPerSample - k - 1) * samplesPerRow + i];
                        }
                    }
                }
            }
        }

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

    private final int readIntegerFromBuffer(byte[] buf, int offset1, int offset2, int offset3, int offset4) {
        return (buf[offset1] & 0xFF)
                | ((buf[offset2] & 0xFF) << 8)
                | ((buf[offset3] & 0xFF) << 16)
                | ((buf[offset4] & 0xFF) << 24);
    }
}
