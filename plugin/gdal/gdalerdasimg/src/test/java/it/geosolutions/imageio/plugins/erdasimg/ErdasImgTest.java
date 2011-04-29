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
package it.geosolutions.imageio.plugins.erdasimg;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Testing reading capabilities for {@link ErdasImgImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class ErdasImgTest extends AbstractGDALTest {
    public final static String fileName = "sample.img";



    /**
     * Test read exploiting common JAI operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void jaiOperations() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file;
        try {
            TestData.unzipFile(this, "test.zip");
            file = TestData.file(this, fileName);
            
        } catch (FileNotFoundException fnfe) {
            warningMessage();
            return;
        }
        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageRead;
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);

        // get a RenderedImage
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, "Read");
        else
            Assert.assertNotNull(image.getTiles());
        ImageIOUtilities.disposeImage(image);
    }

}
