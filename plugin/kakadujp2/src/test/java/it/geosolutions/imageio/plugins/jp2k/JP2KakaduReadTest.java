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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.Histogram;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on
 * JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class JP2KakaduReadTest extends AbstractJP2KakaduTestCase {

//    static FileCache fileCache = new FileCache();

    public JP2KakaduReadTest(String name) {
        super(name);

    }

//    /**
//     * Test Read exploiting Linear Interpolation
//     * 
//     * @throws IOException
//     */
//    public void testJaiReadFromUrl() throws IOException {
//        final URL url = new URL(
//                "http://www.microimages.com/gallery/jp2/CB_TM432.jp2");
//        final File file = fileCache.getFile(url);
//        final ParameterBlockJAI pbjImageRead;
//        pbjImageRead = new ParameterBlockJAI("ImageRead");
//        pbjImageRead.setParameter("Input", file);
//        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
//        if (TestData.isInteractiveTest())
//            visualize(image,
//                    "http://www.microimages.com/gallery/jp2/CB_TM432.jp2");
//        else
//            assertNotNull(image.getTiles());
//    }

    public void testJaiReadFromFile() throws IOException {

        final File file = TestData.file(this, "example.jp2");
        ImageReadDescriptorMT.register(JAI.getDefaultInstance());

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                "ImageRead");
        ImageLayout l = new ImageLayout();
        l.setTileHeight(256);
        l.setTileWidth(256);

        ImageReadParam rp = new JP2KKakaduImageReadParam();
        rp.setSourceSubsampling(1, 1, 0, 0);
        pbjImageRead.setParameter("ReadParam", rp);
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("imageChoice", 0);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
        if (TestData.isInteractiveTest())
            visualize(image, 800, 600);
        else
            assertNotNull(image.getTiles());
    }

    /**
     * Test Read without exploiting JAI-ImageIO Tools
     * 
     * @throws IOException
     */
    // public void testManualRead() throws IOException {
    // final File file = new File(sampleFileForDebug);
    //
    // ImageReader reader = new JP2KakaduImageReader(
    // new JP2KakaduImageReaderSpi());
    // reader.setInput(file);
    // final int numImages = reader.getNumImages(true);
    // for (int i = 0; i < numImages; i++) {
    // ImageReadParam param = new ImageReadParam();
    // param.setSourceSubsampling(8, 8, 0, 0);
    // // param.setSourceRegion(new Rectangle(0, 2000, 2000, 700));
    // RenderedImage image = reader.read(i, param);
    // if (TestData.isInteractiveTest())
    // visualize(image, "testManualRead");
    // else
    // assertNotNull(PlanarImage.wrapRenderedImage(image).getTiles());
    // // displayStatistics(false, image);
    //
    // reader.reset();
    // }
    // reader.dispose();
    //
    // }
    public void testManualRead() throws IOException {
        final File file = TestData.file(this, "example.jp2");
        JP2KKakaduImageReader reader = new JP2KKakaduImageReader(
                new JP2KKakaduImageReaderSpi());

        reader.setInput(file);
        ImageReadParam param = new ImageReadParam();
         param.setSourceSubsampling(4, 4, 0, 0);
        RenderedImage image = reader.read(0, param);
        if (TestData.isInteractiveTest())
            visualize(image, "testManualRead");
        else
            assertNotNull(image.getData());
//        assertEquals(361, image.getWidth());
//        assertEquals(488, image.getHeight());
    }

    public static void displayStatistics(boolean b, RenderedImage source) {
        PlanarImage img = JAI.create("extrema", source, null);
        double[] maximum = (double[]) img.getProperty("maximum");
        double[] minimum = (double[]) img.getProperty("minimum");

        ParameterBlock pb = (new ParameterBlock()).addSource(source);
        pb.add(null).add(1).add(1).add(new int[] { 65536 });
        pb.add(new double[] { minimum[0] }).add(new double[] { maximum[0] });

        PlanarImage dst = JAI.create("histogram", pb);
        Histogram h = (Histogram) dst.getProperty("hiStOgRam");
        JFrame frame = new HistogramFrame(h, b);
        frame.pack();
        frame.show();
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(new JP2KakaduReadTest("testJaiReadFromFile"));

//        suite.addTest(new JP2KakaduReadTest("testManualRead"));
//
//        suite.addTest(new JP2KakaduReadTest("testJaiReadFromUrl"));

        return suite;
    }
}
