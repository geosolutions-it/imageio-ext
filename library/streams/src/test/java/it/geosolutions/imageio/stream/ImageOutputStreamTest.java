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
package it.geosolutions.imageio.stream;

import it.geosolutions.imageio.stream.output.ImageOutputStreamAdapter;
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Simone Giannecchini, GeoSolutions
 */
public class ImageOutputStreamTest {
    private final static Logger LOGGER = Logger.getLogger(ImageOutputStreamTest.class.toString());


    public ImageOutputStreamTest() {
        super();
    }
    @Before
    public void setUp() throws Exception {
        ImageIO.setUseCache(true);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws FileNotFoundException, IOException {


        // read test image
        RenderedImage image = JAI.create("ImageRead", TestData.file(this, "sample.jpeg"));

        // try to encode a jpeg
        BufferedImage test=null;
        ImageOutputStream out=null;
        try{
            File jpegOut = TestData.temp(this, "test.jpeg",true);
            out = new ImageOutputStreamAdapter(
                    new FileOutputStream(jpegOut));
            ImageIO.write(image, "JPEG", out);
            test = ImageIO.read(jpegOut);
            Assert.assertNotNull(test);
        }finally{
            if(test!=null){
                test.flush();
                test=null;
            }

        }

        // try to encode a png
        test=null;
        out=null;
        try{
            File pngO = TestData.temp(this, "test.png",true);
            out = new ImageOutputStreamAdapter(
                    new FileOutputStream(pngO));
            ImageIO.write(image, "PNG", out);
            Assert.assertNotNull(null);// we should not get here!!!
        }catch(Exception e){

        }finally{
            if(test!=null){
                test.flush();
                test=null;
            }

        }

        // try to encode a tiff
        test=null;
        out=null;
        try{
            File tiffO = TestData.temp(this, "test.tiff",true);
            out = new ImageOutputStreamAdapter(
                    new FileOutputStream(tiffO));
            ImageIO.write(image, "tiff", out);
            Assert.assertNotNull(null);// we should not get here!!!
        }catch(Exception e){

        }finally{
            if(test!=null){
                test.flush();
                test=null;
            }

        }

        // try to encode a bmp
        test=null;
        out=null;
        try{
            File bmpO = TestData.temp(this, "test.bmp",true);
            out = new ImageOutputStreamAdapter(
                    new FileOutputStream(bmpO));
            ImageIO.write(image, "bmp", out);
            Assert.assertNotNull(null);// we should not get here!!!
        }catch(Exception e){

        }finally{
            if(test!=null){
                test.flush();
                test=null;
            }

        }

        // try to encode a gif
        test=null;
        out=null;
        try{
            File gifO = TestData.temp(this, "test.gif",true);
            out = new ImageOutputStreamAdapter(
                    new FileOutputStream(gifO));
            ImageIO.write(image, "gif", out);
            test = ImageIO.read(gifO);
            Assert.assertNotNull(test);
        }finally{
            if(test!=null){
                test.flush();
                test=null;
            }

        }



    }
}
