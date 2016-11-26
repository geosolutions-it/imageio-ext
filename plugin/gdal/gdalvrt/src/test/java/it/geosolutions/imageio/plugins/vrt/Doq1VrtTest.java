/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
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
package it.geosolutions.imageio.plugins.vrt;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Testing reading capabilities for First Generation USGS DOQ
 * with {@link VRTImageReader}.
 *
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class Doq1VrtTest extends AbstractGDALTest {
    public final static String fileName = "fakedoq1.doq.vrt";

    /**
     * Test read exploiting common JAI operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void imageRead() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file = TestData.file(this, fileName);

        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();

        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);
        
        //NOTE that the actual sample data (fakedoq1.doq) only contains a row.
        //Therefore, we need to force the read on that reduced area.
        //Requesting a bigger image height will result in a GDAL ReadBlock error. 
        irp.setSourceRegion(new Rectangle(0,0,500,1));
        
        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(32).setTileWidth(32);

        // get a RenderedImage
        RenderedOp image = JAI.create("ImageRead", pbjImageRead,new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest()) {
            Viewer.visualizeAllInformation(image, "test");
        } else {
            Assert.assertNotNull(image.getTiles());
        }
        Assert.assertEquals(500, image.getWidth());
        Assert.assertEquals(1, image.getHeight());
        ImageIOUtilities.disposeImage(image);
    }

}
