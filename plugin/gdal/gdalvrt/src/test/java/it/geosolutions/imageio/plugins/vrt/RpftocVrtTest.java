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
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Testing reading capabilities for RPFTOC with {@link VRTImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class RpftocVrtTest extends  AbstractGDALTest {
    public final static String fileName = "A.TOC.vrt";

    @Test
    public void read() throws FileNotFoundException, IOException {
    	if (!isGDALAvailable) {
            return;
        }
        TestData.unzipFile(this, "rpftoc.zip");
        File file = TestData.file(this, fileName);

        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();

        // subsample by 2 on both dimensions
        final int xSubSampling = 2;
        final int ySubSampling = 2;
        final int xSubSamplingOffset = 0;
        final int ySubSamplingOffset = 0;
        irp.setSourceSubsampling(xSubSampling, ySubSampling,
                xSubSamplingOffset, ySubSamplingOffset);
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);

        // get a RenderedImage
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, "Subsampling Read");
        else
        {
            Assert.assertTrue(image.getWidth() == 768);
            Assert.assertTrue(image.getHeight() == 768);
        }
        ImageIOUtilities.disposeImage(image);
    }
}
