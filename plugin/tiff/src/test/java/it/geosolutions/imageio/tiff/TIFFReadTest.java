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
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileImageInputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class TIFFReadTest extends Assert {

    @Test
    @Ignore
    public void jaiReadFromFile() throws IOException {
        final File file = TestData.file(this, "test.tif");
        
        final ImageReadParam param= new ImageReadParam();
        param.setSourceRegion(new Rectangle(10,10,800,800));
        final TIFFImageReader reader =(TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
        reader.setInput(new FileImageInputStream(file));
        double sum=0;
        final long num = 10000l;
        for(long i=0;i<num;i++){
            final double time= System.nanoTime();
            BufferedImage image = reader.read(0,param);
            if (TestData.isInteractiveTest())
                ImageIOUtilities.visualize(image, "testManualRead");
            else
                Assert.assertNotNull(image.getData());
            sum+=System.nanoTime()-time;
            Assert.assertEquals(120, image.getWidth());
            Assert.assertEquals(107, image.getHeight());
            
            image.flush();
            image=null;
        }
        
        System.out.println("time (ms) "+sum/num/1E6);
        reader.dispose();
    }
}
