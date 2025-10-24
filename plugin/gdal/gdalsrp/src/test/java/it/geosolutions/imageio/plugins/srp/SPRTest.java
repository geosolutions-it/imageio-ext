/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2018, GeoSolutions
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
package it.geosolutions.imageio.plugins.srp;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.ParameterBlockImageN;
import org.eclipse.imagen.RenderedOp;
import org.junit.Assert;
import org.junit.Test;

public class SPRTest extends AbstractGDALTest {

    public static final String fileName = "FKUSRP01.IMG";

    @Test
    public void read() throws FileNotFoundException, IOException {
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
        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockImageN pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();

        // subsample by 2 on both dimensions
        final int xSubSampling = 2;
        final int ySubSampling = 2;
        final int xSubSamplingOffset = 0;
        final int ySubSamplingOffset = 0;
        irp.setSourceSubsampling(xSubSampling, ySubSampling, xSubSamplingOffset, ySubSamplingOffset);
        pbjImageRead = new ParameterBlockImageN("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);

        // get a RenderedImage
        RenderedOp image = ImageN.create("ImageRead", pbjImageRead);

        if (TestData.isInteractiveTest()) Viewer.visualizeAllInformation(image, "Subsampling Read");
        else {
            Raster[] io = image.getTiles();
            Assert.assertNotNull(io);
            Assert.assertEquals(64, image.getWidth());
            Assert.assertEquals(64, image.getHeight());
        }
        ImageIOUtilities.disposeImage(image);
    }
}
