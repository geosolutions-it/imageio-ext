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

import com.sun.media.jai.codecimpl.util.RasterFactory;
import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Testing reading capabilities for JPEG with {@link VRTImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JpegVrtTest extends AbstractGDALTest {

    /**
     * Simple test read
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void read() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        final ParameterBlockJAI pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();
        final String fileName = "bw_sample.jpg.vrt";
        final File file = TestData.file(this, fileName);
        irp.setSourceSubsampling(1, 2, 0, 0);
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("readParam", irp);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
                .setTileWidth(512);

        RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image,fileName);
        else
            Assert.assertNotNull(image.getTiles());
        ImageIOUtilities.disposeImage(image);
    }

    /**
     * Test sourceBands management capabilities.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void sourceBands() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final File inputFile = TestData.file(this, "small_world.jpg.vrt");

        // //
        //
        // Preparing srcRegion constants
        //
        // //
        final int srcRegionX = 40;
        final int srcRegionY = 50;
        final int srcRegionWidth = 400;
        final int srcRegionHeight = 300;
        final int subSamplingX = 2;
        final int subSamplingY = 1;

        // //
        //
        // Setting source settings parameters
        //
        // //
        ImageReadParam rparam = new ImageReadParam();
        rparam.setSourceRegion(new Rectangle(srcRegionX, srcRegionY,
                srcRegionWidth, srcRegionHeight));
        rparam.setSourceSubsampling(subSamplingX, subSamplingY, 0, 0);
        rparam.setSourceBands(new int[] { 0 });

        // //
        //
        // Setting destination settings parameters
        //
        // //
        rparam.setDestinationBands(new int[] { 0 });

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel cm = RasterFactory.createComponentColorModel(
                DataBuffer.TYPE_BYTE, // dataType
                cs, // color space
                false, // has alpha
                false, // is alphaPremultiplied
                Transparency.OPAQUE); // transparency

        final int destWidth = srcRegionWidth / subSamplingX;
        final int destHeight = srcRegionHeight / subSamplingY;
        Assert.assertEquals(destWidth, 200);
        Assert.assertEquals(destHeight, 300);

        final SampleModel sm = cm.createCompatibleSampleModel(destWidth,
                destHeight);
        rparam.setDestinationType(new ImageTypeSpecifier(cm, sm));

        // //
        //
        // Preparing for image read operation
        //
        // //
        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                "ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        pbjImageRead.setParameter("readParam", rparam);
        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(128)
                .setTileWidth(128);

        RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image,"imageread");
        else
        	Assert.assertNotNull(image.getTiles());
        ImageIOUtilities.disposeImage(image);
    }

}
