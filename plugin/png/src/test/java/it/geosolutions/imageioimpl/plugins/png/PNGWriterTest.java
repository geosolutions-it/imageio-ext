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
import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Test;

import ar.com.hjg.pngj.FilterType;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.chunks.ChunksList;
import ar.com.hjg.pngj.chunks.PngMetadata;
import it.geosolutions.imageio.plugins.png.PNGWriter;
import it.geosolutions.resources.TestData;

/**
 * Unit test for simple App.
 */
public class PNGWriterTest {

    @Test
    public void testWriter() {
        PNGWriter writer = new PNGWriter();
        OutputStream out = null;
        try {

            // read test image
            BufferedImage read = ImageIO.read(TestData.file(this, "sample.jpeg"));

            File pngOut = TestData.temp(this, "test.png", true);
            out = new FileOutputStream(pngOut);

            writer.writePNG(read, out, 1, FilterType.FILTER_NONE);
            BufferedImage test = ImageIO.read(pngOut);
            assertNotNull(test);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }
                out = null;
            }
        }
    }

    @Test
    public void testTeXt() throws Exception {
        PNGWriter writer = new PNGWriter();
        OutputStream out = null;
        File pngOut = null;
        final String title = "Title";
        final String description = "Sample Description";
        final String software = "ImageIO-Ext";
        final String author = "Me";
        try {

            // read test image
            BufferedImage read = ImageIO.read(TestData.file(this, "sample.jpeg"));

            pngOut = TestData.temp(this, "test.png", true);
            out = new FileOutputStream(pngOut);

            Map<String, String> textMetadata = new HashMap<String, String>();
            textMetadata.put("Title", title);
            textMetadata.put("Author", author);
            textMetadata.put("Software", software);
            textMetadata.put("Description", description);

            writer.writePNG(read, out, 1, FilterType.FILTER_NONE, textMetadata);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }

            }
        }

        BufferedImage test = ImageIO.read(pngOut);
        assertNotNull(test);
        PngReader reader = null;
        try {
            reader = new PngReader(pngOut);
            reader.readSkippingAllRows();
            PngMetadata metadata = reader.getMetadata();
            assertNotNull(metadata);
            assertEquals(title, metadata.getTxtForKey("Title"));
            assertEquals(description, metadata.getTxtForKey("Description"));
            assertEquals(author, metadata.getTxtForKey("Author"));
            assertEquals(software, metadata.getTxtForKey("Software"));
        } finally {
            if (reader != null) {
                reader.close();

            }
        }
    }
}
