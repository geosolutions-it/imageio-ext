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
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

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
    public void jaiReadFromFile() throws IOException {
        final File file = new File("c:/work/data/tiff/tm.tiff");//TestData.file(this, "test.tif");
        
        final ImageReadParam param= new ImageReadParam();
        param.setSourceRegion(new Rectangle(10,10,800,800));
        final TIFFImageReader reader =(TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
        reader.setInput(new FileImageInputStream(file));
        double sum=0;
        final long num = 10000l;
        for(long i=0;i<num;i++){
            final double time= System.nanoTime();
            BufferedImage image = reader.read(0,param);
//            if (TestData.isInteractiveTest())
                ImageIOUtilities.visualize(image, "testManualRead");
//            else
                Assert.assertNotNull(image.getData());
                sum+=System.nanoTime()-time;
            Assert.assertEquals(120, image.getWidth());
            Assert.assertEquals(107, image.getHeight());
            
            image.flush();
            image=null;
        }
        
        System.out.println("tempo in ms "+sum/num/1E6);
        reader.dispose();
    }
        
//        final long num = 10000l;
//        
//        final ImageReadParam param= new ImageReadParam();
//        param.setSourceRegion(new Rectangle(10,10,800,800));
//        for (long i = 0; i < num; i++) {
//            final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
////            ImageLayout l = new ImageLayout();
////            l.setTileHeight(16);
////            l.setTileWidth(16);        
//            pbjImageRead.setParameter("Input", file);
//            pbjImageRead.setParameter("imageChoice", 0);
//            pbjImageRead.setParameter("ReadParam", param);
//            pbjImageRead.setParameter("Reader", reader);
//            final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
////            , new RenderingHints(
////                    JAI.KEY_IMAGE_LAYOUT, l));
//            assertNotNull(image.getTiles());
////            assertEquals(image.getWidth(), 120);
////            assertEquals(image.getHeight(), 107);
//            image.dispose();
//            
////            ImageIOUtilities.visualize(image);
//        }
//    }

    //
    // @org.junit.Test
    // public void inputsTest() throws IOException {
    // if (!runTests)
    // return;
    //
    // // //
    // //
    // // Testing base reader methods
    // //
    // // //
    // final File file = TestData.file(this, "CB_TM432.jp2");
    // final ImageReader reader = new JP2KKakaduImageReaderSpi().createReaderInstance();
    // reader.setInput(file);
    // Assert.assertEquals(1,reader.getNumImages(false));
    // Assert.assertEquals(488, reader.getTileHeight(0));
    // Assert.assertEquals(361, reader.getTileWidth(0));
    // Assert.assertEquals(488, reader.getHeight(0));
    // Assert.assertEquals(361, reader.getWidth(0));
    // Assert.assertNotNull(reader.getStreamMetadata());
    // Assert.assertNotNull(reader.getImageMetadata(0));
    // Assert.assertNotNull(reader.getImageTypes(0));
    //
    // // //
    // //
    // // Quick Test on wrong image index
    // //
    // // //
    // boolean isValidImageIndex = false;
    // try{
    // reader.getWidth(99);
    // isValidImageIndex = true;
    // } catch (IndexOutOfBoundsException e){
    // Assert.assertFalse(isValidImageIndex);
    // }
    //
    // // //
    // //
    // // Testing raw jp2 file
    // //
    // // //
    // final File rawfile = TestData.file(this, "raw.j2c");
    // final ImageReader rawreader = new JP2KKakaduImageReaderSpi().createReaderInstance();
    // rawreader.setInput(rawfile);
    // rawreader.read(0);
    // boolean hasStreamMetadata = false;
    // try{
    // rawreader.getStreamMetadata();
    // hasStreamMetadata = true;
    // } catch (UnsupportedOperationException e){
    // Assert.assertFalse(hasStreamMetadata);
    // }
    //
    // // //
    // //
    // // Testing a file which isn't a jp2 one
    // //
    // // //
    // boolean isValidInput = false;
    // final File badfile = File.createTempFile("bad", ".jp2");
    // badfile.deleteOnExit();
    // final FileImageOutputStream fios = new FileImageOutputStream(badfile);
    // fios.writeChars("BAD");
    // fios.close();
    //
    // final ImageReader badFileReader = new JP2KKakaduImageReaderSpi().createReaderInstance();
    // try{
    // badFileReader.setInput(badfile);
    // isValidInput = true;
    // } catch (Throwable t){
    // Assert.assertFalse(isValidInput);
    // }
    // }
    //
    @org.junit.Test
    @Ignore
    public void manualRead() throws IOException {

        final File file = TestData.file(this, "test.tif");
        final TIFFImageReader reader =(TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();

        reader.setInput(new FileImageInputStream(file));
        double sum=0;
        final long num = 10000l;
        for(long i=0;i<num;i++){
            final double time= System.nanoTime();
            BufferedImage image = reader.read(0);
//            if (TestData.isInteractiveTest())
//                ImageIOUtilities.visualize(image, "testManualRead");
//            else
                Assert.assertNotNull(image.getData());
                sum+=System.nanoTime()-time;
            Assert.assertEquals(120, image.getWidth());
            Assert.assertEquals(107, image.getHeight());
            
            image.flush();
            image=null;
        }
        
        System.out.println("tempo in ms "+sum/num/1E6);
        reader.dispose();
    }
    //
    // public static void displayStatistics(boolean b, RenderedImage source) {
    // PlanarImage img = JAI.create("extrema", source, null);
    // double[] maximum = (double[]) img.getProperty("maximum");
    // double[] minimum = (double[]) img.getProperty("minimum");
    //
    // ParameterBlock pb = (new ParameterBlock()).addSource(source);
    // pb.add(null).add(1).add(1).add(new int[] { 65536 });
    // pb.add(new double[] { minimum[0] }).add(new double[] { maximum[0] });
    //
    // PlanarImage dst = JAI.create("histogram", pb);
    // Histogram h = (Histogram) dst.getProperty("hiStOgRam");
    // JFrame frame = new HistogramFrame(h, b);
    // frame.pack();
    // frame.show();
    // }

}
