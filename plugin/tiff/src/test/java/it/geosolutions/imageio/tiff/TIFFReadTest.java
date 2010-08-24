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
    public void readFromFileDirect() throws IOException {
        final File file = TestData.file(this, "test.tif");

        // double sum=0;
        // final long num = 10000l;
        // for(long i=0;i<num;i++){
        // final double time= System.nanoTime();
        
        // IMAGE 0
        RenderedImage image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(0), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(120, image.getWidth());
        Assert.assertEquals(107, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        // }
        
        // IMAGE 2
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(2), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(40, image.getWidth());
        Assert.assertEquals(36, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;



        // IMAGE 4
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(4), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(24, image.getWidth());
        Assert.assertEquals(22, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        

        // IMAGE 6
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(6), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(18, image.getWidth());
        Assert.assertEquals(16, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        


        // IMAGE 1
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(1), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(60, image.getWidth());
        Assert.assertEquals(54, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;


        // IMAGE 3
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(3), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(30, image.getWidth());
        Assert.assertEquals(27, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        
        // IMAGE 5
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(5), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(20, image.getWidth());
        Assert.assertEquals(18, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        
        
        // IMAGE 7
        image = ImageReadDescriptor.create(
        		new FileImageInputStream(file), 
        		Integer.valueOf(7), 
        		false, 
        		false,
        		false,
        		null,
        		null, 
        		null, 
        		new TIFFImageReaderSpi().createReaderInstance(),
        		null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(15, image.getWidth());
        Assert.assertEquals(14, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        

    }
    
    
    
    @Test
    public void readFromFileJAI() throws IOException {
        final File file = TestData.file(this, "test.tif");

        final ImageReadParam param = new ImageReadParam();
        // param.setSourceRegion(new Rectangle(10,10,800,800));
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        reader.setInput(new FileImageInputStream(file));
        // double sum=0;
        // final long num = 10000l;
        // for(long i=0;i<num;i++){
        // final double time= System.nanoTime();
        
        // IMAGE 0
        BufferedImage image = reader.read(0, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(120, image.getWidth());
        Assert.assertEquals(107, image.getHeight());

        image.flush();
        image = null;
        // }
        
        // IMAGE 2
        image = reader.read(2, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(40, image.getWidth());
        Assert.assertEquals(36, image.getHeight());

        image.flush();
        image = null;



        // IMAGE 4
        image = reader.read(4, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(24, image.getWidth());
        Assert.assertEquals(22, image.getHeight());

        image.flush();
        image = null;
        

        // IMAGE 6
        image = reader.read(6, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(18, image.getWidth());
        Assert.assertEquals(16, image.getHeight());

        image.flush();
        image = null;
        


        // IMAGE 1
        image = reader.read(1, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(60, image.getWidth());
        Assert.assertEquals(54, image.getHeight());

        image.flush();
        image = null;


        // IMAGE 3
        image = reader.read(3, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(30, image.getWidth());
        Assert.assertEquals(27, image.getHeight());

        image.flush();
        image = null;

        
        // IMAGE 5
        image = reader.read(5, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(20, image.getWidth());
        Assert.assertEquals(18, image.getHeight());

        image.flush();
        image = null;
        
        
        // IMAGE 7
        image = reader.read(7, param);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(15, image.getWidth());
        Assert.assertEquals(14, image.getHeight());

        image.flush();
        image = null;
        
        // System.out.println("time (ms) "+sum/num/1E6);
        reader.dispose();
    }
}
