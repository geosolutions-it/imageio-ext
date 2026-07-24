/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2026, GeoSolutions
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
 * EXPRESS OR IMPLIED WARRANTIES ARE DISCLAIMED. IN NO EVENT SHALL GeoSolutions
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import static org.junit.Assert.assertArrayEquals;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import java.nio.ByteOrder;
import java.util.Random;
import org.junit.Test;

/**
 * Checks that the optimized 8-bit horizontal-differencing reversal produces exactly the same bytes as the original
 * straightforward loop, for single and multi band data across several row sizes. {@link PredictorDecompressorBenchmark}
 * measures the two against each other.
 */
public class PredictorDecompressorTest {

    @Test
    public void testEightBitMatchesReference() {
        int[] sppValues = {1, 2, 3, 4, 5};
        int[] widths = {1, 2, 7, 512};
        int[] heights = {1, 3, 33};
        Random rnd = new Random(20260724);
        for (int spp : sppValues) {
            for (int w : widths) {
                for (int h : heights) {
                    byte[] input = new byte[w * spp * h];
                    rnd.nextBytes(input);

                    byte[] expected = input.clone();
                    reference(expected, w, h, spp);

                    byte[] actual = input.clone();
                    int[] bps = new int[spp];
                    int[] sf = new int[spp];
                    for (int b = 0; b < spp; b++) {
                        bps[b] = 8;
                        sf[b] = BaselineTIFFTagSet.SAMPLE_FORMAT_UNSIGNED_INTEGER;
                    }
                    PredictorDecompressor pd = new PredictorDecompressor(
                            BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING,
                            bps,
                            sf,
                            spp,
                            ByteOrder.LITTLE_ENDIAN);
                    try {
                        pd.decompress(actual, 0, 0, h, w, w * spp);
                    } catch (Exception e) {
                        throw new AssertionError("decompress threw for spp=" + spp + " w=" + w + " h=" + h, e);
                    }

                    assertArrayEquals("spp=" + spp + " w=" + w + " h=" + h, expected, actual);
                }
            }
        }
    }

    /** The original straightforward 8-bit reversal, kept here as the reference. */
    private static void reference(byte[] buf, int srcWidth, int srcHeight, int samplesPerPixel) {
        for (int j = 0; j < srcHeight; j++) {
            int count = samplesPerPixel * (j * srcWidth + 1);
            for (int i = samplesPerPixel; i < srcWidth * samplesPerPixel; i++) {
                buf[count] += buf[count - samplesPerPixel];
                count++;
            }
        }
    }
}
