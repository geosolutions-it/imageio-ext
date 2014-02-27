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


import static org.junit.Assert.assertEquals;

import it.geosolutions.imageio.plugins.png.PNGWriter;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.FormatDescriptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ar.com.hjg.pngj.FilterType;

@RunWith(Parameterized.class)
public class PngSuiteImagesTest {

    private File sourceFile;


    public PngSuiteImagesTest(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        List<Object[]> result = new ArrayList<Object[]>();
        File source = new File("./src/test/resources/pngsuite");
        File[] files = source.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });
        Arrays.sort(files);
        for (File file : files) {
            result.add(new Object[] { file });
        }

        return result;
    }

    @Test
    public void testRoundTripFilterNone() throws Exception {
        BufferedImage input = ImageIO.read(sourceFile);

        roundTripPNGJ(input, input);
    }

    @Test
    public void testRoundTripTiledImage() throws Exception {
        BufferedImage input = ImageIO.read(sourceFile);

        // prepare a tiled image layout
        ImageLayout il = new ImageLayout(input);
        il.setTileWidth(8);
        il.setTileHeight(8);

        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
        RenderedOp tiled = FormatDescriptor.create(input, input.getSampleModel().getDataType(),
                hints);
        assertEquals(8, tiled.getTileWidth());
        assertEquals(8, tiled.getTileHeight());

        roundTripPNGJ(input, tiled);
    }

    private void roundTripPNGJ(BufferedImage original, RenderedImage source) throws Exception {
        // write the PNG
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float quality = 4f/9 - 1;
        new PNGWriter().writePNG(original, bos, -quality, FilterType.FILTER_NONE);

        // write the output to file for eventual visual comparison
        byte[] bytes = bos.toByteArray();
        writeToFile(new File("./target/roundTripNone", sourceFile.getName()), bytes);

        // read it back
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(bis);

        ImageAssert.assertImagesEqual(original, image);
    }

    private void writeToFile(File file, byte[] bytes) throws IOException {
        File parent = file.getParentFile();
        parent.mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

}
