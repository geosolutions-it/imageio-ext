/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2021, GeoSolutions
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

import javax.imageio.IIOException;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Class applying the Predictor algorithm to restore the data from its
 * compressed form.
 */
public class PredictorDecompressor {

    /** predictor's type */
    private final int predictor;
    private int[] bitsPerSample;
    private int[] sampleFormat;
    private int samplesPerPixel;
    private ByteOrder byteOrder;

    public PredictorDecompressor(int predictor, int[] bitsPerSample,
                                 int[] sampleFormat, int samplesPerPixel,
                                 ByteOrder byteOrder) {
        this.predictor = predictor;
        this.bitsPerSample = bitsPerSample;
        this.sampleFormat = sampleFormat;
        this.samplesPerPixel = samplesPerPixel;
        this.byteOrder = byteOrder;
    }

    /**
     * Decompress the buffer content by applying the proper predictor algorithm
     */
    public void decompress(byte[] buf, int bufOffset, int dstOffset, int srcHeight, int srcWidth, int bytesPerRow) throws IIOException {
        if (predictor == BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            if (bitsPerSample[0] == 8) {
                if (samplesPerPixel == 1) {
                    reverseDifferencing(buf, bufOffset, srcHeight, srcWidth, samplesPerPixel);
                } else {
                    reverseDifferencingInterleaved(buf, bufOffset, srcHeight, srcWidth * samplesPerPixel,
                            samplesPerPixel);
                }
            } else if (bitsPerSample[0] == 16) {
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 2;
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr = (((int) buf[count]) & 0xFF) | (buf[count + 1] << 8);
                            int prev = (((int) buf[count - samplesPerPixel * 2]) & 0xFF) | (buf[count + 1 - samplesPerPixel * 2] << 8);
                            curr += prev;
                            buf[count] = (byte) curr;
                            buf[count + 1] = (byte) (curr >> 8);
                            count += 2;
                        }
                    }
                } else {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 2;
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr = (((int) buf[count + 1]) & 0xFF) | (buf[count] << 8);
                            int prev = (((int) buf[count + 1 - samplesPerPixel * 2]) & 0xFF) | (buf[count - samplesPerPixel * 2] << 8);
                            curr += prev;
                            buf[count + 1] = (byte) curr;
                            buf[count] = (byte) (curr >> 8);
                            count += 2;
                        }
                    }
                }
            } else if (bitsPerSample[0] == 32) {
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 4;
                        int pbase = count - samplesPerPixel * 4;
                        int prev = TIFFDecompressor.readIntegerFromBuffer(buf, pbase, pbase + 1, pbase + 2, pbase + 3);
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr = TIFFDecompressor.readIntegerFromBuffer(buf, count, count + 1, count + 2, count + 3);
                            int sum = curr + prev;
                            buf[count] = (byte) (sum & 0xFF);
                            buf[count + 1] = (byte) ((sum >> 8) & 0xFF);
                            buf[count + 2] = (byte) ((sum >> 16) & 0xFF);
                            buf[count + 3] = (byte) ((sum >> 24) & 0xFF);
                            count += 4;
                            prev = sum;
                        }
                    }
                } else {
                    for (int j = 0; j < srcHeight; j++) {
                        int count = dstOffset + samplesPerPixel * (j * srcWidth + 1) * 4;
                        int pbase = count - samplesPerPixel * 4;
                        int prev = TIFFDecompressor.readIntegerFromBuffer(buf, pbase + 3, pbase + 2, pbase + 1, pbase);
                        for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                            int curr = TIFFDecompressor.readIntegerFromBuffer(buf, count + 3, count + 2, count + 1, count);
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
            } else
                throw new IIOException("Unexpected branch of Horizontal differencing Predictor, bps=" + bitsPerSample[0]);
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
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
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
    }

    /**
     * Reverses 8-bit horizontal differencing on single band rows, in place.
     *
     * <p>The running value stays in acc rather than being read back from the array. stride is always
     * 1 here, but it is taken as an argument on purpose: written as a literal 1 the same loop
     * measured four times slower, the JIT evidently choosing a worse shape for it. byte addition
     * wraps mod 256, which is the predictor's semantics.
     */
    private static void reverseDifferencing(byte[] buf, int bufOffset, int srcHeight, int rowSamples, int stride) {
        for (int j = 0; j < srcHeight; j++) {
            final int rowStart = bufOffset + j * rowSamples;
            final int rowEnd = rowStart + rowSamples;
            byte acc = buf[rowStart];
            for (int p = rowStart + stride; p < rowEnd; p += stride) {
                acc += buf[p];
                buf[p] = acc;
            }
        }
    }

    /**
     * Reverses 8-bit horizontal differencing on band interleaved rows, in place.
     *
     * <p>Each band has its own prefix sum, as in {@link #reverseDifferencing}, but the row is walked
     * once in address order instead of once per band: a walk of stride samplesPerPixel reads every
     * cache line samplesPerPixel times to use 64/samplesPerPixel bytes of it each time, and that
     * measured about twice as slow. Consecutive samples belong to different bands, so the
     * accumulators in acc still form independent dependency chains; acc is samplesPerPixel bytes
     * wide and stays in L1.
     */
    private static void reverseDifferencingInterleaved(byte[] buf, int bufOffset, int srcHeight, int rowSamples,
            int samplesPerPixel) {
        final byte[] acc = new byte[samplesPerPixel];
        for (int j = 0; j < srcHeight; j++) {
            final int rowStart = bufOffset + j * rowSamples;
            final int rowEnd = rowStart + rowSamples;
            System.arraycopy(buf, rowStart, acc, 0, samplesPerPixel);
            int band = 0;
            for (int p = rowStart + samplesPerPixel; p < rowEnd; p++) {
                byte v = (byte) (acc[band] + buf[p]);
                acc[band] = v;
                buf[p] = v;
                if (++band == samplesPerPixel) band = 0;
            }
        }
    }

    /**
     * Validate the current predictor setup
     */
    public void validate() throws IIOException {
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
    }
}
