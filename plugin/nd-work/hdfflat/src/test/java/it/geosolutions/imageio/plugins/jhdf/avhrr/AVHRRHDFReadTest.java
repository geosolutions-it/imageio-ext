/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
 */
package it.geosolutions.imageio.plugins.jhdf.avhrr;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.plugins.jhdf.JHDFTestCase;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AVHRRHDFReadTest extends JHDFTestCase {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jhdf.avhrr");

    public AVHRRHDFReadTest(String name) {
        super(name);
    }

    private void warningMessage() {
        StringBuffer sb = new StringBuffer(
                "Test file not available. Test are skipped");
        LOGGER.info(sb.toString());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test reading of a simple image
        suite.addTest(new AVHRRHDFReadTest("testRead"));

        // Test reading of a simple image
        suite.addTest(new AVHRRHDFReadTest("testReadCompare"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testRead() throws IOException {
        File file;
        try {
            file = TestData.file(this, "2008.0622.1224.n18.hdf4");
        } catch (FileNotFoundException fnfe) {
            warningMessage();
            return;
        }
        ImageReader reader = new HDFAVHRRImageReaderSpi()
                .createReaderInstance();
        reader.setInput(file);
        final int index = 0;
        if (TestData.isInteractiveTest()) {
            ImageIOUtilities.visualize(reader.read(0), "mcsst", true);
        } else
            assertNotNull(reader.read(index));

        IIOMetadata metadata = reader.getImageMetadata(index);
        ImageIOUtilities.displayImageIOMetadata(metadata
                .getAsTree(CoreCommonImageMetadata.nativeMetadataFormatName));
        ImageIOUtilities.displayImageIOMetadata(metadata
                .getAsTree(HDFAVHRRImageMetadata.nativeMetadataFormatName));
        reader.reset();
        reader.setInput(file);
        reader.dispose();
    }

    /**
     * Test read exploiting common JAI operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testReadCompare() throws FileNotFoundException, IOException {
        try {
            final File file = TestData.file(this, "2008.0904.1116.n18.hdf4");

            // ////////////////////////////////////////////////////////////////
            // preparing the ImageRead
            // ////////////////////////////////////////////////////////////////
            final ParameterBlockJAI pbjImageRead;
            final ImageReadParam irp = new ImageReadParam();

            // subsample by 2 on both dimensions
            final int xSubSampling = 2;
            final int ySubSampling = 2;
            final int xSubSamplingOffset = 0;
            final int ySubSamplingOffset = 0;
            irp.setSourceSubsampling(xSubSampling, ySubSampling,
                    xSubSamplingOffset, ySubSamplingOffset);

            final ImageLayout l = new ImageLayout();
            l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(64)
                    .setTileWidth(64);

            pbjImageRead = new ParameterBlockJAI("ImageRead");
            pbjImageRead.setParameter("Input", file);
            pbjImageRead.setParameter("readParam", irp);

            // get a RenderedImage
            final RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                    new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

            
            // Silly equality test
            final BufferedImage buffImage = image.getAsBufferedImage();
            final ImageReader reader = new HDFAVHRRImageReaderSpi()
                    .createReaderInstance();
            reader.setInput(file);
            final BufferedImage buffImage2 = reader.read(0, irp);
            final int w = buffImage.getWidth();
            final int h = buffImage.getHeight();
            assertEquals(w, buffImage2.getWidth());
            assertEquals(h, buffImage2.getHeight());
            final Raster raster1 = buffImage.getData();
            final Raster raster2 = buffImage2.getData();
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    assertEquals(raster1.getSample(j, i, 0), raster2.getSample(
                            j, i, 0));
                }
            }

        } catch (FileNotFoundException fnfe) {
            warningMessage();
        }
    }
}
