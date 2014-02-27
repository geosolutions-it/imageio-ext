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
package it.geosolutions.imageioimpl.plugins.png;

import it.geosolutions.imageio.plugins.png.PNGWriter;
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.junit.Assert;

import ar.com.hjg.pngj.FilterType;

/**
 * Unit test for simple App.
 */
public class PNGWriterTest extends TestCase {

    public void testWriter(){
        PNGWriter writer = new PNGWriter();
        OutputStream out = null;
        try{
            
        // read test image
        BufferedImage read = ImageIO.read(TestData.file(this, "sample.jpeg"));
        
        File pngOut = TestData.temp(this, "test.png",true);
        out = new FileOutputStream(pngOut);
        
        writer.writePNG(read, out, 1, FilterType.FILTER_NONE);
        BufferedImage test = ImageIO.read(pngOut);
        Assert.assertNotNull(test);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    
                }
                out=null;
            }
        }
    }
}
