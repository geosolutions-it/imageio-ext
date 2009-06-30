/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.media.jai.operator.ImageWriteDescriptor;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AsciiGridTest extends TestCase {
    public AsciiGridTest(String name) {
        super(name);
    }

    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.arcgrid");

    protected void setUp() throws Exception {
        super.setUp();
        File file = TestData.file(this, "arcgrid.zip");
        assertTrue(file.exists());

        // unzip it
        TestData.unzipFile(this, "arcgrid.zip");

    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Read a file using subSampling and sourceRegion settings
        suite.addTest(new AsciiGridTest("testReadRegion"));

        // Read a GRASS, compressed (GZ) file
        suite.addTest(new AsciiGridTest("testReadGrassGZ"));

        // Read an ArcGrid file and write it back to another file
        suite.addTest(new AsciiGridTest("testReadWrite"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Read an ArcGrid file and write it back to another file
     */
    public void testReadWrite() throws FileNotFoundException, IOException {
        String title = new String("Simple JAI ImageRead operation test");
        LOGGER.info("\n\n " + title + " \n");
        File inputFile = TestData.file(this, "095b_dem_90m.asc");
        ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, title, true);
        else
            image.getTiles();
        assertEquals(351, image.getWidth());
        assertEquals(350, image.getHeight());

        // //
        //
        // Writing it out
        //
        // //
        final File foutput = TestData.temp(this, "095b_dem_90m.asc", false);
        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
                "ImageWrite");
        pbjImageWrite.setParameter("Output", foutput);
        pbjImageWrite.addSource(image);

        // //
        //
        // What I am doing here is crucial, that is getting the used writer and
        // disposing it. This will force the underlying stream to write data on
        // disk.
        //
        // //
        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
        final ImageWriter writer = (ImageWriter) op
                .getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
        writer.dispose();

        // //
        //
        // Reading it back
        //
        // //
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", foutput);
        RenderedOp image2 = JAI.create("ImageRead", pbjImageRead);
        title = new String("Read Back the just written image");
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, title, true);
        else
            image2.getTiles();

        final String error[] = new String[1];
        final boolean result = compare(image, image2, error);
        assertTrue(error[0], result);
    }

    /**
     * Read a GRASS, compressed (GZ) file
     */
    public void testReadGrassGZ() throws FileNotFoundException, IOException {
        // This test may require 20 seconds to be executed. Therefore it will
        // be run only when extensive tests are requested.
        if (TestData.isExtensiveTest()) {
            String title = new String("JAI ImageRead on a GRASS GZipped file ");
            LOGGER.info("\n\n " + title + " \n");
            File inputFile = TestData.file(this, "spearfish.asc.gz");
            final GZIPInputStream stream = new GZIPInputStream(
                    new FileInputStream(inputFile));
            ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
            pbjImageRead.setParameter("Input", stream);
            RenderedOp image = JAI.create("ImageRead", pbjImageRead);
            if (TestData.isInteractiveTest())
                ImageIOUtilities.visualize(image, title, true);
            else
                assertNotNull(image.getTiles());
        }

    }

    /**
     * Read a file using subSampling and sourceRegion settings
     */
    public void testReadRegion() throws FileNotFoundException, IOException {
        String title = new String(
                "JAI ImageRead using subSampling and sourceRegion ");
        LOGGER.info("\n\n " + title + " \n");

        // //
        //
        // Preparing ImageRead parameters
        //
        // //
        File inputFile = TestData.file(this, "dem.asc");
        ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        final ImageReadParam irp = new ImageReadParam();

        // Setting sourceRegion on the original image
        irp.setSourceRegion(new Rectangle(200, 300, 1000, 1000));

        // Setting subSampling factors
        irp.setSourceSubsampling(2, 2, 0, 0);
        pbjImageRead.setParameter("ReadParam", irp);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, title, true);
        else
            assertNotNull(image.getTiles());
    }

    /**
     * Compare images by testing each pixel of the first image equals the pixel
     * of the second image. Return <code>true</code> if compare is
     * successfully.
     * 
     * @param image
     *                the first image to be compared
     * @param image2
     *                the first image to be compared
     * @param error
     *                a container for error messages in case of differences.
     * @return <code>true</code> if everything is ok.
     */
    private boolean compare(final RenderedOp image, final RenderedOp image2,
            final String error[]) {
        final int minTileX1 = image.getMinTileX();
        final int minTileY1 = image.getMinTileY();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int maxTileX1 = minTileX1 + image.getNumXTiles();
        final int maxTileY1 = minTileY1 + image.getNumYTiles();
        double value1 = 0, value2 = 0;

        for (int tileIndexX = minTileX1; tileIndexX < maxTileX1; tileIndexX++)
            for (int tileIndexY = minTileY1; tileIndexY < maxTileY1; tileIndexY++) {

                final Raster r1 = image.getTile(tileIndexX, tileIndexY);
                final Raster r2 = image2.getTile(tileIndexX, tileIndexY);

                for (int i = r1.getMinX(); i < width; i++) {
                    for (int j = r1.getMinY(); j < height; j++) {
                        value1 = r1.getSampleDouble(i, j, 0);
                        value2 = r2.getSampleDouble(i, j, 0);

                        if (value1 != value2) {
                            error[0] = new StringBuffer(
                                    "Written back image is not equal to the original one: ")
                                    .append(value1).append(", ").append(value2)
                                    .toString();
                            return false;
                        }
                    }
                }
            }
        return true;
    }
}
