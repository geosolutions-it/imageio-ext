/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2007 - 2009, GeoSolutions
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
 */
package it.geosolutions.imageioimpl.plugins.png;

import ar.com.hjg.pngj.FilterType;
import it.geosolutions.imageio.plugins.png.PNGWriter;
import it.geosolutions.resources.TestData;
import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@Ignore
public class PNGWriterBenchmarkTest {

    /**
     * This only exists as an easy method to run a profiler on. It isn't actually
     * a valid test, so it'll be ignored.
     */
    @Test
    @Ignore
    public void profileNonSubImageBenchmark() throws Exception {
        PngWriterBenchmark.NonSubImageState state = new PngWriterBenchmark.NonSubImageState();
        state.setup();
        for (int i = 0; i < 2000; i++) {
            PngWriterBenchmark.run(state.bufferedImage);
        }
    }

    /**
     * This only exists as an easy method to run a profiler on. It isn't actually
     * a valid test, so it'll be ignored.
     */
    @Test
    @Ignore
    public void profileSubImageBenchmark() throws Exception {
        PngWriterBenchmark.SubImageState state = new PngWriterBenchmark.SubImageState();
        state.setup();
        for (int i = 0; i < 2000; i++) {
            PngWriterBenchmark.run(state.bufferedImage);
        }
    }

    /**
     * We run a benchmark on non-sub-images and a benchmark on sub-images and
     * check to make sure that the throughput is within 10% of each other.
     */
    @Test
    public void runAllBenchmarksAndCompareResults() throws Exception {
        Options options = new OptionsBuilder()
                .include(PngWriterBenchmark.class.getSimpleName() + ".*")
                .result("./target/benchmark-results.json")
                .resultFormat(ResultFormatType.JSON)
                .build();
        Collection<RunResult> runResults = new Runner(options).run();

        Double previousThroughput = null;
        for (RunResult runResult : runResults) {

            double operationsPerSecond = runResult.getPrimaryResult().getScore();
            if (previousThroughput == null) {
                previousThroughput = operationsPerSecond;
            } else {

                // We are checking to ensure that the throughput of this run
                // is within 10% of the throughput of the previous run.
                assertTrue(isNumberWithinPercentOfNumber(previousThroughput, operationsPerSecond, 10));
            }
        }
    }

    private boolean isNumberWithinPercentOfNumber(double n1, double n2, double percent) {
        if (n2 == 0) {
            return false;
        } else {
            double diff = Math.abs(n1 - n2);
            double actualPercentage = Math.abs(diff / n2) * 100.0;
            return percent > actualPercentage;
        }
    }


    @BenchmarkMode(Mode.Throughput)
    @Fork(1)
    @Threads(1)
    @Warmup(iterations = 2, time = 1)
    @Measurement(time = 1)
    public static class PngWriterBenchmark {


        /**
         * Creates a sub-image of the full one.
         */
        @State(Scope.Thread)
        public static class SubImageState {
            BufferedImage bufferedImage;

            @Setup
            public void setup() throws Exception {
                BufferedImage fullImage = ImageIO.read(TestData.file(PNGWriterBenchmarkTest.class, "sample-large.jpg"));
                bufferedImage = fullImage.getSubimage(0, 0, 512, 512);
            }
        }

        /**
         * Takes a portion of the full image (similar to SubImageState) but clones it into a standalone
         * BufferedImage that explicitly does NOT have a parent; i.e. it is intentionally no longer a sub-image.
         * <p>
         * The reason we crop the original photo is just to make this benchmark comparable to the {@link SubImageState},
         * such that they are encoding the same image size and contents.
         */
        @State(Scope.Thread)
        public static class NonSubImageState {
            BufferedImage bufferedImage;

            @Setup
            public void setup() throws Exception {
                BufferedImage fullImage = ImageIO.read(TestData.file(PNGWriterBenchmarkTest.class, "sample-large.jpg"));
                BufferedImage subImage = fullImage.getSubimage(0, 0, 512, 512);
                bufferedImage = deepCopy(subImage);
            }
        }

        @Benchmark
        public static void subImageBenchmark(SubImageState state) throws Exception {
            run(state.bufferedImage);
        }

        @Benchmark
        public static void nonSubImageBenchmark(NonSubImageState state) throws Exception {
            run(state.bufferedImage);
        }

        public static void run(BufferedImage bufferedImage) throws Exception {
            PNGWriter writer = new PNGWriter();

            if (writer.isScanlineSupported(bufferedImage)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                writer.writePNG(bufferedImage, out, 1, FilterType.FILTER_NONE);
            }
        }

    }

    public static BufferedImage deepCopy(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
