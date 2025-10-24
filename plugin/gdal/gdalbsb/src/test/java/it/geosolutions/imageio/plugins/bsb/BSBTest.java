/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.bsb;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import org.eclipse.imagen.ImageLayout;
import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.ParameterBlockImageN;
import org.eclipse.imagen.RenderedOp;
import org.junit.Assert;
import org.junit.Before;

/**
 * Testing reading capabilities for {@link BSBImageReader}.
 *
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class BSBTest extends AbstractGDALTest {
    public static final String fileName = "rgbsmall.kap";

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test read exploiting common ImageN operations (Crop-Translate-Rotate)
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @org.junit.Test
    public void imageRead() throws FileNotFoundException, IOException {
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
        final ParameterBlockImageN pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();

        pbjImageRead = new ParameterBlockImageN("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(32).setTileWidth(32);

        // get a RenderedImage
        RenderedOp image = ImageN.create("ImageRead", pbjImageRead, new RenderingHints(ImageN.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest()) {
            ImageIOUtilities.visualize(image, "test", true);
        } else {
            Assert.assertNotNull(image.getData());
        }
        Assert.assertEquals(50, image.getWidth());
        Assert.assertEquals(50, image.getHeight());
        ImageIOUtilities.disposeImage(image);
    }
}
