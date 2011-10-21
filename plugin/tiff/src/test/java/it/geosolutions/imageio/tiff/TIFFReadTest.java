/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.tiff;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.PlanarImage;

import org.junit.Assert;
import org.junit.Test;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class TIFFReadTest extends Assert {
    @Test
    public void readFromFileJAI() throws IOException {
        final File file = TestData.file(this, "test.tif");

        // double sum = 0;
        // final long num = 10000l;

        // for (long i = 0; i < num; i++) {
        // final double time = System.nanoTime();

        // IMAGE 0
        RenderedImage image = ImageReadDescriptor.create(new FileImageInputStream(file),
                Integer.valueOf(0), false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(PlanarImage.wrapRenderedImage(image).getTiles());
        // sum += System.nanoTime() - time;
        Assert.assertEquals(30, image.getWidth());
        Assert.assertEquals(26, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        // }

        // IMAGE 2
        final ImageReadParam readParam = new ImageReadParam();
        readParam.setSourceRegion(new Rectangle(0, 0, 10, 10));
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(2),
                false, false, false, null, null, readParam,
                new TIFFImageReaderSpi().createReaderInstance(), null);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(8, image.getWidth());
        Assert.assertEquals(7, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 4
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(4),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 5
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(5),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(1, image.getWidth());
        Assert.assertEquals(1, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 1
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(1),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(15, image.getWidth());
        Assert.assertEquals(13, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 3
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(3),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(4, image.getWidth());
        Assert.assertEquals(4, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 5
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(5),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(1, image.getWidth());
        Assert.assertEquals(1, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;



    }

    @Test
    public void readFromFileDirect() throws IOException {

        final File file = TestData.file(this, "test.tif");

        final ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(0, 0, 2, 2));

        // double sum=0;
        // final long num = 10000l;

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        reader.setInput(new FileImageInputStream(file));
        // System.out.println(new IIOMetadataDumper(
        // reader.getImageMetadata(0),TIFFImageMetadata.nativeMetadataFormatName).getMetadata());

        // for(long i=0;i<num;i++){
        // final double time= System.nanoTime();
        // IMAGE 0
        BufferedImage image = reader.read(0, param);
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());
        image.flush();
        image = null;

        image = reader.read(1, param);
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());
        image.flush();
        image = null;

        image = reader.read(2, param);
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());
        image.flush();
        image = null;

        image = reader.read(1, param);
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());
        image.flush();
        image = null;

        image = reader.read(3, param);
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());
        image.flush();
        image = null;

        image = reader.read(0, param);
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());
        image.flush();
        image = null;


        // sum+=System.nanoTime()-time;
        // Assert.assertEquals(120, image.getWidth());
        // Assert.assertEquals(107, image.getHeight());

        // System.out.println("test "+i);
        //
        // }
        // System.out.println(sum/num);
        reader.dispose();
    }
}
