/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import org.eclipse.imagen.ImageLayout;
import org.junit.Assert;
import org.junit.Test;

import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.ParameterBlockImageN;
import org.eclipse.imagen.RenderedOp;

import javax.imageio.ImageReadParam;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Testing reading capabilities for ERDAS Imagine with {@link VRTImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class ErdasImgVrtTest extends AbstractGDALTest {
    public final static String fileName = "sample-erdas.img.vrt";

    /**
     * Test read exploiting common ImageN operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void jaiOperations() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file = TestData.file(this, fileName);

        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockImageN pbjImageRead;
        pbjImageRead = new ParameterBlockImageN("ImageRead");
        pbjImageRead.setParameter("Input", file);

        // get a RenderedImage
        RenderedOp image = ImageN.create("ImageRead", pbjImageRead);

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, "Read");
        else
            Assert.assertNotNull(image.getTiles());
        ImageIOUtilities.disposeImage(image);
    }

    @Test
    public void bandSelection() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file = TestData.file(this, fileName);

        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockImageN pbjImageRead;
        pbjImageRead = new ParameterBlockImageN("ImageRead");
        pbjImageRead.setParameter("Input", file);
        ImageReadParam param = new ImageReadParam();
        param.setSourceBands(new int[] { 2 });

        SampleModel originalSampleModel = new ComponentSampleModel(
                DataBuffer.TYPE_FLOAT, 256, 256, 3, 256 * 3, new int[] {0,1,2});
        SampleModel sm = new ComponentSampleModel(
                DataBuffer.TYPE_FLOAT, 256, 256, 1, 256, new int[] {0});

        ColorModel cm = GDALUtilities.extractColorModel(null, originalSampleModel, 1);

        ImageLayout layout = new ImageLayout().setTileHeight(256).setTileWidth(256);
        layout.setColorModel(cm);
        layout.setSampleModel(sm);
        RenderingHints hints = new RenderingHints(ImageN.KEY_IMAGE_LAYOUT, layout);

        // get a RenderedImage
        RenderedOp image = ImageN.create("ImageRead", pbjImageRead, hints);

        sm = image.getSampleModel();
        cm = image.getColorModel();
        ColorSpace colorSpace = cm.getColorSpace();
        // Original image is RGB: 400x200 pixels, 3 bands
        // Let's verify we only get one band image with
        // the databuffer only containing one band pixels
        Assert.assertEquals(1, sm.getNumBands());
        Assert.assertEquals(1, cm.getNumComponents());
        Assert.assertEquals(ColorSpace.TYPE_GRAY, colorSpace.getType());
        Assert.assertEquals(400*200*1, image.getData().getDataBuffer().getSize());

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, "Read");
        else
            Assert.assertNotNull(image.getTiles());
        ImageIOUtilities.disposeImage(image);
    }

}
