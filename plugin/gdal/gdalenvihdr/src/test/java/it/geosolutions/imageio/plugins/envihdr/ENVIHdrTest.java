/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageio.plugins.envihdr;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReader;

import org.junit.Assert;
import org.junit.Test;
public class ENVIHdrTest extends AbstractGDALTest {

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
        final String fileName = "small_world.img";
        TestData.unzipFile(this, "test.zip");
        final File file = TestData.file(this, fileName);
        ImageReader reader = new ENVIHdrImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        final RenderedImage image = reader.read(0);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image,fileName);
        else
            Assert.assertNotNull(image);
        reader.dispose();
    }
}
