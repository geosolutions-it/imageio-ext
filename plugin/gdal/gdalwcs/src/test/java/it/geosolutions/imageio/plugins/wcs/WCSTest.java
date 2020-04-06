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
package it.geosolutions.imageio.plugins.wcs;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.imageio.plugins.wcs.WCSImageReader;
import it.geosolutions.imageio.plugins.wcs.WCSImageReaderSpi;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

/**
 * Testing reading capabilities for {@link WCSImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class WCSTest extends AbstractGDALTest {
    public final static String fileName = "wcs.xml";

    private final static boolean isDriverAvailable = isGDALAvailable
            && GDALUtilities.isDriverAvailable("WCS");

    /**
     * Test read exploiting common JAI operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Ignore
    public void read() throws FileNotFoundException, IOException {
        if (!isDriverAvailable) {
            return;
        }
        if (!isGDALDATAEnvSet){
            warningMessage("GDAL_DATA environment variable has not been set. Tests are skipped");
            return;
        }
        File file;
        try {
            file = TestData.file(this, fileName);
        } catch (FileNotFoundException fnfe) {
            warningMessage();
            return;
        }
        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ImageReader mReader = new WCSImageReaderSpi()
                .createReaderInstance();
        mReader.setInput(file);
        final RenderedImage image = mReader.read(0);
 
        if (TestData.isInteractiveTest()) {
            ImageIOUtilities.visualize(image);
        }
        else
            Assert.assertNotNull(image.getData());
    }

}
