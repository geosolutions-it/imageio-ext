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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ar.com.hjg.pngj.FilterType;

@RunWith(Parameterized.class)
public class BufferedImageTypesTest {

    static final int WIDTH = 1024;

    static final int HEIGTH = 1024;
    
    static final int STROKE_WIDTH = 30;

    static final int LINES = 200;
    
    BufferedImage image;

    String name;

    public BufferedImageTypesTest(String name, int imageType) {
        this.name = name;
        image = new BufferedImage(WIDTH, HEIGTH, imageType);
        new SampleImagePainter().paintImage(image);
    }

    @Parameters(name = "{0}")
    public static Collection parameters() throws Exception {
        String[] types = new String[] { "4BYTE_ABGR", "INT_ARGB", "3BYTE_BGR", "INT_BGR", 
                "INT_RGB", "BYTE_INDEXED", "BYTE_GRAY" };
        
        List<Object[]> parameters = new ArrayList<Object[]>();
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            Field field = BufferedImage.class.getDeclaredField("TYPE_" + type);
            int imageType = (Integer) field.get(null);
            parameters.add(new Object[] {type.toLowerCase(), imageType});
        }
        
        return parameters;
    }


    @Test
    public void compareImage() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float quality = 4f/9 - 1;
        new PNGWriter().writePNG(image, bos, -quality, FilterType.FILTER_NONE);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BufferedImage readBack = ImageIO.read(bis);
        
        boolean success = false;
        try {
            ImageAssert.assertImagesEqual(image, readBack);
            success = true;
        } finally {
            if(!success) {
                ImageIO.write(image, "PNG", new File("./target/" + name + "_expected.png"));
                ImageIO.write(readBack, "PNG", new File("./target/" + name + "_actual.png"));
            }
        }
    }

}
