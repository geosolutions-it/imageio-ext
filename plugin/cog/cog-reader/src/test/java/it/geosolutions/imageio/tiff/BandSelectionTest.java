/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
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
package it.geosolutions.imageio.tiff;

import static org.junit.Assert.assertEquals;

import com.sun.media.jai.operator.ImageReadDescriptor;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.DefaultCogImageInputStream;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests band selection with a variety of file layouts (pixel vs band interleaved) and compressions (since decompressors
 * are informed of band selection).
 */
@RunWith(Parameterized.class)
public class BandSelectionTest {

    private static String URL_BASE = "https://gs-cog.s3.eu-central-1.amazonaws.com/sample/";

    private static BufferedImage REFERENCE_IMAGE;

    @BeforeClass
    public static void readReference() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(URL_BASE + "sampleRGBA.tif");
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        CogImageReadParam param = new CogImageReadParam();
        param.setRangeReaderClass(HttpRangeReader.class);

        REFERENCE_IMAGE = reader.read(0, param);
    }

    private final boolean enhancedBandSelection;
    private final boolean deferredLoading;

    @Parameterized.Parameters(name = "Enhanced read: {0} - Deferred load: {1} ")
    public static List<Object[]> useEnhancedBandSelection() {
        return Arrays.asList(new Object[][] {{false, false}, {true, false}, {false, true}, {true, true}});
    }

    public BandSelectionTest(boolean enhancedBandSelection, boolean deferredLoading) {
        this.enhancedBandSelection = enhancedBandSelection;
        this.deferredLoading = deferredLoading;
    }

    @Test
    public void testBandSelectPixelInterleavedNoCompression() throws Exception {
        assertBandSelections("sampleRGBA.tif");
    }

    @Test
    public void testBandSelectPixelInterleavedLZW() throws Exception {
        assertBandSelections("sampleRGBA_LZW.tif");
    }

    @Test
    public void testBandSelectPixelInterleavedDeflate() throws Exception {
        assertBandSelections("sampleRGBA_DEFLATE.tif");
    }

    @Test
    public void testBandSelectPixelInterleavedZSTD() throws Exception {
        assertBandSelections("sampleRGBA_ZSTD.tif");
    }

    @Test
    public void testBandSelectBandInterleavedNoCompression() throws Exception {
        assertBandSelections("sampleRGBA_bi.tif");
    }

    @Test
    public void testBandSelectBandInterleavedLZW() throws Exception {
        assertBandSelections("sampleRGBA_LZW_bi.tif");
    }

    @Test
    public void testBandSelectBandInterleavedDeflate() throws Exception {
        assertBandSelections("sampleRGBA_DEFLATE_bi.tif");
    }

    @Test
    public void testBandSelectBandInterleavedZSTD() throws Exception {
        assertBandSelections("sampleRGBA_ZSTD_bi.tif");
    }

    private void assertBandSelections(String file) throws Exception {
        // test assorted band selection combinations
        assertBandSelection(file, new int[] {0});
        assertBandSelection(file, new int[] {2});
        assertBandSelection(file, new int[] {3, 0});
        assertBandSelection(file, new int[] {1, 2, 3});
        assertBandSelection(file, new int[] {3, 2, 1, 0});
        // add some more tests with duplicated bands, if enhanced selection is used
        if (enhancedBandSelection) {
            assertBandSelection(file, new int[] {1, 2, 2});
            // triky one, more output bands than input
            assertBandSelection(file, new int[] {3, 3, 2, 2, 1, 1, 0, 0});
        }
    }

    private void assertBandSelection(String file, int[] srcBands) throws Exception {
        // original file
        Raster original = REFERENCE_IMAGE.getData();

        // band selection
        RenderedImage image = readWithBandSelect(file, srcBands);
        assertEquals(srcBands.length, image.getSampleModel().getNumBands());
        Raster selected = image.getData();

        // compare read data with original
        int[] source = new int[4];
        int[] destination = new int[srcBands.length];
        for (int r = 0; r < image.getHeight(); r++) {
            for (int c = 0; c < image.getWidth(); c++) {
                original.getPixel(c, r, source);
                selected.getPixel(c, r, destination);
                for (int b = 0; b < srcBands.length; b++) {
                    assertEquals(source[srcBands[b]], destination[b]);
                }
            }
        }
    }

    private BufferedImage readWithBandSelect(String fileName, int[] bands) throws Exception {
        CogImageReader reader = null;
        try (DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(URL_BASE + fileName)) {
            reader = new CogImageReader(new CogImageReaderSpi());
            CogImageReadParam param = new CogImageReadParam();
            param.setRangeReaderClass(HttpRangeReader.class);
            if (enhancedBandSelection) {
                // easy, allows for the same bands to be selected more than once
                param.setBands(bands);
            } else {
                // classic, but with more complex set up
                param.setSourceBands(bands);
                param.setDestinationBands(IntStream.range(0, bands.length).toArray());
            }

            if (deferredLoading || !enhancedBandSelection) {
                ImageTypeSpecifier its =
                        ImageIOUtilities.getBandSelectedType(bands.length, REFERENCE_IMAGE.getSampleModel());
                param.setDestinationType(its);
            }

            // direct read
            if (!deferredLoading) {
                reader.setInput(cogStream);
                return reader.read(0, param);
            }

            // deferred read, to go through the JAI machinery figuring out the deferred image layout
            // (and then making it a BufferedImage so that we can dispose the reader safely)
            cogStream.init(param); // needed to make it load the header
            RenderedOp op =
                    ImageReadDescriptor.create(cogStream, 0, false, false, false, null, null, param, reader, null);
            return PlanarImage.wrapRenderedImage(op).getAsBufferedImage();
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }
}
