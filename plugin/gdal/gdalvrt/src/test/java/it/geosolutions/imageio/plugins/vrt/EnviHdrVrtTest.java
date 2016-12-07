/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
import it.geosolutions.resources.TestData;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageReader;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Testing reading capabilities for ESRI HDR with {@link VRTImageReader}.
 */
public class EnviHdrVrtTest extends AbstractGDALTest {

    /**
     * Test Read without exploiting JAI-ImageIO Tools
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
	@Test
    public void testManualRead() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final String fileName = "small_world.img.vrt";
        TestData.unzipFile(this, "small-world.zip");
        final File file = TestData.file(this, fileName);
        ImageReader reader = new VRTImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        final RenderedImage image = reader.read(0);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image,fileName);
        else
            Assert.assertNotNull(image);
        reader.dispose();
    }
}
