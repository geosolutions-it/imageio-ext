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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ar.com.hjg.pngj.FilterType;

@RunWith(Parameterized.class)
public class CustomUShortImageTypesTest {
    
    private int nbits;
    private int size;

    public CustomUShortImageTypesTest(int nbits, int size) {
        this.nbits = nbits;
        this.size = size;
    }
    
    @Parameters(name = "bits{0}/size{1}")
    public static Collection<Object[]> parameters() {
        List<Object[]> result = new ArrayList<Object[]>();
        for(int nbits : new int[] {1, 2, 4, 8, 16}) {
            for(int size = 1; size <= 32; size++) {
                result.add(new Object[] {nbits, size});
            }
        }

        return result;
    }

    @Test
    public void testCustomUShortImage() throws Exception {
        BufferedImage bi = ImageTypeSpecifier.createGrayscale(nbits, DataBuffer.TYPE_USHORT, false)
                .createBufferedImage(size, size);
        Graphics2D graphics = bi.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 16, 32);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(16, 0, 16, 32);
        graphics.dispose();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float quality = 5f/9 - 1;
        new PNGWriter().writePNG(bi, bos, -quality, FilterType.FILTER_NONE);
        
        BufferedImage read = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        ImageAssert.assertImagesEqual(bi, read);
    }
}
