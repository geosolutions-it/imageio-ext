/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.doq2;

import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import it.geosolutions.imageio.gdalframework.Viewer;

import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link DOQ2ImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class DOQ2Test extends AbstractDOQ2TestCase {

    public DOQ2Test(String name) {
        super(name);
    }
 
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test read exploiting common JAI operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testImageRead() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file;
        try {
            file = TestData.file(this, fileName);
        } catch (FileNotFoundException fnfe) {
            super.warningMessage();
            return;
        }
        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();

        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(32).setTileWidth(32);

        // get a RenderedImage
        RenderedOp image = JAI.create("ImageRead", pbjImageRead,new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest()) {
            Viewer.visualizeAllInformation(image, "test");
        } else
            Assert.assertNotNull(image.getTiles());
        Assert.assertEquals(500, image.getWidth());
        Assert.assertEquals(7631, image.getHeight());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test read without exploiting JAI
        suite.addTest(new DOQ2Test("testImageRead"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
