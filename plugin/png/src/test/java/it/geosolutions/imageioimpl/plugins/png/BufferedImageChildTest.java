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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.Test;

import ar.com.hjg.pngj.FilterType;

public class BufferedImageChildTest {

    BufferedImage getSample() {
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics graphics = bi.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 25, 25);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(25, 0, 25, 25);
        graphics.setColor(Color.YELLOW);
        graphics.fillRect(0, 25, 25, 25);
        graphics.setColor(Color.RED);
        graphics.fillRect(25, 25, 25, 25);
        graphics.dispose();
        return bi;
    }
    
    @Test
    public void testSmallerSameOrigin() throws Exception {
        testSubImage(0, 0, 25, 25);
    }
    
    @Test
    public void testSmallerTranslateX() throws Exception {
        testSubImage(25, 0, 25, 25);
    }
    
    @Test
    public void testSmallerTranslateY() throws Exception {
        testSubImage(0, 25, 25, 25);
    }
    
    @Test
    public void testSmallerTranslateXY() throws Exception {
        testSubImage(25, 25, 25, 25);
    }

    private void testSubImage(int x, int y, int w, int h) throws Exception {
        BufferedImage bi = getSample();
        // ImageAssert.showImage("Original", 2000, bi);
        BufferedImage subimage = bi.getSubimage(x, y, w, h);
        // ImageAssert.showImage("Subimage", 2000, subimage);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float quality = 4f/9 - 1;
        new PNGWriter().writePNG(subimage, bos, -quality, FilterType.FILTER_NONE);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BufferedImage readBack = ImageIO.read(bis);
        // ImageAssert.showImage("ReadBack", 2000, readBack);
        
        ImageAssert.assertImagesEqual(subimage, readBack);
    }
    
    
}
