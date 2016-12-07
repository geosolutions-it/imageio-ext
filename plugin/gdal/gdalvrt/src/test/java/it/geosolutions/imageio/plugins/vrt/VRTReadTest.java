/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2016, GeoSolutions
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

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.media.jai.ImageLayout;

import org.junit.Assert;
import org.junit.Before;

import it.geosolutions.imageio.core.GCP;
import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.plugins.vrt.VRTImageReaderSpi;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

public class VRTReadTest extends AbstractGDALTest {
	
	public final static String fileName = "3timesutmFloat32.vrt";
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        File file = TestData.file(this, "test-data.zip");
        Assert.assertTrue(file.exists());

        // unzip it
        TestData.unzipFile(this, "test-data.zip");
    }
	
	/**
     * Test reading a vrt format
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
            warningMessage();
            return;
        }

        // get a RenderedImage
        ImageReader reader= new VRTImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        RenderedImage image = reader.read(0);
        if (TestData.isInteractiveTest()) {
            ImageIOUtilities.visualize(image, "test", true);
        } else {
            Assert.assertNotNull(image.getData());
        }
        Assert.assertEquals(256, image.getWidth());
        Assert.assertEquals(256, image.getHeight());
        
        // check for expected datatype
        Assert.assertEquals(DataBuffer.TYPE_FLOAT, image.getSampleModel().getDataType());      
        
        reader.dispose();
    }
	

}
