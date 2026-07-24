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

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Measures the 8-bit horizontal differencing reversal in {@link PredictorDecompressor} against the
 * straightforward loop it replaced, for one to four bands.
 *
 * <p>Ignored because it takes about three minutes and the numbers are machine specific. Surefire will skip
 * it, so run it on an otherwise idle machine from IDE or by removing the
 * annotation. JMH is needed rather than hand written timing loops: it forks a JVM per benchmark, so the
 * two loops cannot pollute each other's inlining decisions, it feeds the image dimensions in as
 * state so they are not constant folded, and it reports the error alongside the score.
 *
 * <p>One operation reverses {@link #TOTAL_BYTES} of samples, so 48 ms/op is 1000 MB/s. Scores on an
 * AMD Ryzen AI 9 HX 370 with OpenJDK 17, ms/op, reference vs stridedPerBand vs current (reference, strided, current):
 *
 * <ul>
 *   <li>1 band: 199.9 +- 69.9 vs 71.7 +- 2.2 vs 22.3 +- 0.4, so 240, 669 and 2155 MB/s
 *   <li>2 bands: 158.1 +- 72.6 vs 70.9 +- 0.8 vs 32.2 +- 1.3, so 304, 677 and 1489 MB/s
 *   <li>3 bands: 154.7 +- 71.1 vs 72.0 +- 2.6 vs 37.8 +- 0.4, so 310, 667 and 1270 MB/s
 *   <li>4 bands: 174.7 +- 23.2 vs 70.9 +- 0.6 vs 37.4 +- 1.5, so 275, 677 and 1283 MB/s
 * </ul>
 *
 * <p>The reference scores are the unstable ones, up to 46% error against under 4% for the other two.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class PredictorDecompressorBenchmark {

    /** Held constant across band counts so the scores are comparable. */
    static final int TOTAL_BYTES = 48 << 20;

    @Param({"1", "2", "3", "4"})
    public int samplesPerPixel;

    private byte[] buf;
    private int width;
    private int height;
    private PredictorDecompressor decompressor;

    /**
     * The buffer is decoded in place and never reset: the sums wrap and the values drift, but the
     * work done per operation is exactly the same either way.
     */
    @Setup(Level.Trial)
    public void setup() {
        width = 4096;
        height = TOTAL_BYTES / (width * samplesPerPixel);
        buf = new byte[width * samplesPerPixel * height];
        new Random(20260724).nextBytes(buf);

        int[] bitsPerSample = new int[samplesPerPixel];
        int[] sampleFormat = new int[samplesPerPixel];
        for (int band = 0; band < samplesPerPixel; band++) {
            bitsPerSample[band] = 8;
            sampleFormat[band] = BaselineTIFFTagSet.SAMPLE_FORMAT_UNSIGNED_INTEGER;
        }
        decompressor = new PredictorDecompressor(BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING,
                bitsPerSample, sampleFormat, samplesPerPixel, ByteOrder.LITTLE_ENDIAN);
    }

    @Benchmark
    public byte[] current() throws Exception {
        decompressor.decompress(buf, 0, 0, height, width, width * samplesPerPixel);
        return buf;
    }

    /**
     * The first optimized version: one strided pass per band, carrying the running value in a local.
     * Kept as an arm because it is the shape that was validated end to end on three band data, so a
     * change to the 8-bit branch has to be checked against it and not only against the reference.
     */
    @Benchmark
    public byte[] stridedPerBand() {
        int rowSamples = width * samplesPerPixel;
        for (int j = 0; j < height; j++) {
            int rowStart = j * rowSamples;
            int rowEnd = rowStart + rowSamples;
            for (int band = 0; band < samplesPerPixel; band++) {
                byte acc = buf[rowStart + band];
                for (int p = rowStart + band + samplesPerPixel; p < rowEnd; p += samplesPerPixel) {
                    acc += buf[p];
                    buf[p] = acc;
                }
            }
        }
        return buf;
    }

    @Benchmark
    public byte[] reference() {
        for (int j = 0; j < height; j++) {
            int count = samplesPerPixel * (j * width + 1);
            for (int i = samplesPerPixel; i < width * samplesPerPixel; i++) {
                buf[count] += buf[count - samplesPerPixel];
                count++;
            }
        }
        return buf;
    }

    @Test
    @Ignore
    public void runBenchmarks() throws Exception {
        new Runner(new OptionsBuilder().include(getClass().getName()).build()).run();
    }
}
