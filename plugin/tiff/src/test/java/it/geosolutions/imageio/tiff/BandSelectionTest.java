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

import it.geosolutions.imageio.plugins.tiff.TIFFImageReadParam;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.resources.TestData;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import org.eclipse.imagen.PlanarImage;
import org.eclipse.imagen.RenderedOp;
import org.eclipse.imagen.media.imageread.ImageReadDescriptor;
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

    private static BufferedImage REFERENCE_IMAGE;

    @BeforeClass
    public static void readReference() throws IOException {
        REFERENCE_IMAGE = TIFFReadTest.readTiff(TestData.file(BandSelectionTest.class, "sampleRGBA.tif"));
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
        File file = TestData.file(this, "sampleRGBA.tif");
        assertBandSelections(file);
    }

    @Test
    public void testBandSelectPixelInterleavedLZW() throws Exception {
        File original = TestData.file(this, "sampleRGBA_LZW.tif");
        assertBandSelections(original);
    }

    @Test
    public void testBandSelectPixelInterleavedDeflate() throws Exception {
        File original = TestData.file(this, "sampleRGBA_DEFLATE.tif");
        assertBandSelections(original);
    }

    @Test
    public void testBandSelectPixelInterleavedZSTD() throws Exception {
        File original = TestData.file(this, "sampleRGBA_ZSTD.tif");
        assertBandSelections(original);
    }

    @Test
    public void testBandSelectBandInterleavedNoCompression() throws Exception {
        File file = TestData.file(this, "sampleRGBA_bi.tif");
        assertBandSelections(file);
    }

    @Test
    public void testBandSelectBandInterleavedLZW() throws Exception {
        File original = TestData.file(this, "sampleRGBA_LZW_bi.tif");
        assertBandSelections(original);
    }

    @Test
    public void testBandSelectBandInterleavedDeflate() throws Exception {
        File original = TestData.file(this, "sampleRGBA_DEFLATE_bi.tif");
        assertBandSelections(original);
    }

    @Test
    public void testBandSelectBandInterleavedZSTD() throws Exception {
        File original = TestData.file(this, "sampleRGBA_ZSTD_bi.tif");
        assertBandSelections(original);
    }

    private void assertBandSelections(File file) throws Exception {
        // test assorted band selection combinations
        assertBandSelection(file, new int[] {0});
        assertBandSelection(file, new int[] {1});
        assertBandSelection(file, new int[] {2});
        assertBandSelection(file, new int[] {3});
        assertBandSelection(file, new int[] {0, 3});
        assertBandSelection(file, new int[] {3, 0});
        assertBandSelection(file, new int[] {0, 1, 2});
        assertBandSelection(file, new int[] {1, 2, 3});
        assertBandSelection(file, new int[] {3, 2, 1, 0});
        // add some more tests with duplicated bands, if enhanced selection is used
        if (enhancedBandSelection) {
            assertBandSelection(file, new int[] {1, 2, 2});
            assertBandSelection(file, new int[] {3, 3});
            // triky one, more output bands than input
            assertBandSelection(file, new int[] {3, 3, 2, 2, 1, 1, 0, 0});
        }
    }

    private void assertBandSelection(File file, int[] srcBands) throws Exception {
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

    private BufferedImage readWithBandSelect(File file, int[] bands) throws Exception {
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();

        try (FileImageInputStream is = new FileImageInputStream(file)) {
            TIFFImageReadParam param = (TIFFImageReadParam) reader.getDefaultReadParam();
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
                reader.setInput(is);
                return reader.read(0, param);
            }

            // deferred read, to go through the ImageN machinery figuring out the deferred image layout
            // (and then making it a BufferedImage so that we can dispose the reader safely)
            RenderedOp op = ImageReadDescriptor.create(is, 0, false, false, false, null, null, param, reader, null);
            return PlanarImage.wrapRenderedImage(op).getAsBufferedImage();
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }
}
