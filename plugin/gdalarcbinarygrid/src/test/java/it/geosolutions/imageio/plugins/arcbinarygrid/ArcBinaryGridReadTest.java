/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.arcbinarygrid;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link ArcBinaryGridImageReader} leveraging
 * on JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 * 
 */
public class ArcBinaryGridReadTest extends AbstractArcBinaryGridTestCase {
    public ArcBinaryGridReadTest(String name) {
        super(name);
    }

    /**
     * To run this test, you need to get the content of the 
     * whole nzdem500 folder available at:
     * http://download.osgeo.org/gdal/data/aig/nzdem/nzdem500/
     */
    private final static String fileName = "nzdem500/vat.adf";

    /**
     * Simple test read through JAI - ImageIO
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testReadJAI() throws IOException {
        if (!isGDALAvailable) {
            return;
        }
        final ParameterBlockJAI pbjImageRead;
        File file = null;
        try {
            file = TestData.file(this, fileName);
        } catch (FileNotFoundException fnfe) {
            LOGGER.warning("test-data not found: " + fileName
                    + "\nTests are skipped");
            return;
        }
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        ImageReadParam rp = new ImageReadParam();
        rp.setSourceSubsampling(8, 8, 0, 0);
        pbjImageRead.setParameter("readParam", rp);
        pbjImageRead.setParameter("Input", file);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, fileName, true);
        else
            image.getTiles();
        assertEquals(251, image.getWidth());
        assertEquals(369, image.getHeight());
    }

    /**
     * Simple test read through ImageIO
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testReadImageIO() throws IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file = null;
        try {
            file = TestData.file(this, fileName);
        } catch (FileNotFoundException fnfe) {
            LOGGER.warning("test-data not found: " + fileName
                    + "\nTests are skipped");
            return;
        }

        // //
        //
        // Try to get a reader for this raster data
        //
        // //
        final Iterator it = ImageIO.getImageReaders(file);
        assertTrue(it.hasNext());

        // //
        //
        // read some data from it using subsampling
        //
        // //
        final ImageReader reader = (ImageReader) it.next();
        assertTrue(reader instanceof ArcBinaryGridImageReader);
        ImageReadParam rp = reader.getDefaultReadParam();
        rp.setSourceSubsampling(4, 4, 0, 0);
        reader.setInput(file);
        RenderedImage image = reader.read(0, rp);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "subsample read " + file.getName(), true);
        reader.reset();
        reader.dispose();
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite();

        // Test reading of a simple image
        suite.addTest(new ArcBinaryGridReadTest("testReadJAI"));

        // Test reading of a simple image
        suite.addTest(new ArcBinaryGridReadTest("testReadImageIO"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
