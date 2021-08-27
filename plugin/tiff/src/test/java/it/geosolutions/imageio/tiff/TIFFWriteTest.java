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

import it.geosolutions.imageio.plugins.tiff.TIFFImageWriteParam;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriter;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriterSpi;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.jai.PlanarImage;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.media.jai.operator.ImageReadDescriptor;


/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class TIFFWriteTest extends Assert {
    
    private final static Logger LOGGER = Logger.getLogger(TIFFWriteTest.class.toString());

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

        // IMAGE 7

    }

    @Test
    public void readWriteFromFileDirect() throws IOException {

//        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(512*1024*1024);
//        final TCTool tc= new TCTool((SunTileCache)JAI.getDefaultInstance().getTileCache());
        final File inputFile =TestData.file(this, "test.tif");// new File("c:\\work\\dem30_final.tiff");
        final File outputFile = TestData.temp(this, "testw.tif",true);
        
        

        final ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(0, 0, 10, 10));



        TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        reader.setInput(new FileImageInputStream(inputFile));

        BufferedImage image = reader.read(0, param);
//        RenderedImage image = ImageReadDescriptor.create(new FileImageInputStream(inputFile),
//                Integer.valueOf(0), false, false, false, null, null, null,
//                reader, new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout().setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512).setTileWidth(512)));
        
        final TIFFImageWriter writer= (TIFFImageWriter) new TIFFImageWriterSpi().createWriterInstance();
        final ImageWriteParam writeParam= new TIFFImageWriteParam(Locale.getDefault());
        writeParam.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setTiling(512, 512,0,0);

        
//        for( int i=0;i<1;i++){
        writer.setOutput(new FileImageOutputStream(outputFile));
        
        // BUG
//        writer.write(metadata, new IIOImage(image, null, metadata), null);
        writer.addIIOWriteProgressListener(new IIOWriteProgressListener() {
            
            public void writeAborted(ImageWriter source) {
                // TODO Auto-generated method stub
                
            }
            
            public void thumbnailStarted(ImageWriter source, int imageIndex, int thumbnailIndex) {
                assertTrue(imageIndex>=0);
                assertTrue(thumbnailIndex==0);
                
            }
            
            public void thumbnailProgress(ImageWriter source, float percentageDone) {
                assertTrue(percentageDone>=0&&percentageDone<=100);
                
            }
            
            public void thumbnailComplete(ImageWriter source) {
                // TODO Auto-generated method stub
                
            }
            
            public void imageStarted(ImageWriter source, int imageIndex) {
                assertTrue(imageIndex>=0);
                LOGGER.info("imageStarted");
                
            }
            
            public void imageProgress(ImageWriter source, float percentageDone) {
                assertTrue(percentageDone>=0&&percentageDone<=100);
                LOGGER.info(percentageDone+"%");
                
            }
            
            public void imageComplete(ImageWriter source) {
                LOGGER.info("imageComplete");
                
            }
        });
        writer.write(null,new IIOImage(image, null,null),writeParam);
        
//        }
        writer.dispose();
        
        
        
        reader.reset();
        reader.setInput(new FileImageInputStream(outputFile));
        image = reader.read(0);
        
               
        Assert.assertEquals(10, image.getWidth());
        Assert.assertEquals(10, image.getHeight());
        image.flush();
        image=null;
        
        reader.dispose();
    }



    @Test
    public void writeZSTD() throws IOException {

        final File inputFile =TestData.file(this, "test.tif");
        final File outputFile = TestData.temp(this, "testw.tif",true);
        TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        reader.setInput(new FileImageInputStream(inputFile));

        BufferedImage image = reader.read(0);
        final TIFFImageWriter writer= (TIFFImageWriter) new TIFFImageWriterSpi().createWriterInstance();
        final ImageWriteParam writeParam= new TIFFImageWriteParam(Locale.getDefault());
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType("ZSTD");
        writer.setOutput(new FileImageOutputStream(outputFile));
        writer.write(image);
        writer.dispose();
        TIFFReadTest.assertImagesEqual(image, TIFFReadTest.readTiff(outputFile));
    }
}
