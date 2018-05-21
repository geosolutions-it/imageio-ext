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

import it.geosolutions.imageio.plugins.png.PNGImageWriterSPI;
import it.geosolutions.imageio.plugins.png.PNGWriter;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.BandMergeDescriptor;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.ConstantDescriptor;

import org.junit.Test;

import ar.com.hjg.pngj.FilterType;
import it.geosolutions.imageio.plugins.png.PNGWriter;

public class GrayAlpha8bitTest {

    @Test
    public void testGrayAlpha8Bit() throws Exception {
        BufferedImage bi = new BufferedImage(50, 50, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = bi.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 16, 32);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(16, 0, 16, 32);
        graphics.dispose();
        
        final ImageLayout tempLayout = new ImageLayout(bi);
        tempLayout.unsetValid(ImageLayout.COLOR_MODEL_MASK).unsetValid(
                ImageLayout.SAMPLE_MODEL_MASK);
        RenderedImage alpha = ConstantDescriptor.create(Float.valueOf(bi.getWidth()),
                Float.valueOf(bi.getHeight()), new Byte[] { Byte.valueOf((byte) 255) },
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, tempLayout));
        RenderedImage grayAlpha = BandMergeDescriptor.create(bi, alpha, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float quality = 5f/9 - 1;
        new PNGWriter().writePNG(grayAlpha, bos, -quality, FilterType.FILTER_NONE);
        
        BufferedImage read = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        BufferedImage gaBuffered = PlanarImage.wrapRenderedImage(grayAlpha).getAsBufferedImage();
        ImageAssert.assertImagesEqual(gaBuffered, read);
        
        // now using imagewriter interface
        ImageWriter writer = new PNGImageWriterSPI().createWriterInstance();
        writer.setOutput(bos);
        ImageWriteParam wp = writer.getDefaultWriteParam();
        wp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        wp.setCompressionQuality(-quality);
        writer.write(null, new IIOImage(bi, null, null), wp);
        writer.dispose();
        ImageAssert.assertImagesEqual(bi, ImageIO.read(new ByteArrayInputStream(bos.toByteArray())));
    }

    @Test
    public void testGrayAlpha8BitFromBandSelect() throws Exception {
        // Create a Square with 4 quadrants:
        // Top Left     = FULLY OPAQUE BLUE
        // Top Right    = FULLY OPAQUE RED
        // Bottom Left  = 50% TRANSPARENT BLUE
        // BOttom Right = FULLY TRANSPARENT GREEN

        BufferedImage bi = new BufferedImage(64, 64, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = bi.createGraphics();

        // FULLY OPAQUE BLUE
        final Rectangle blueRectangle = new Rectangle(0, 0, 32, 32);
        fillRectangle(graphics, blueRectangle, Color.BLUE);

        // FULLY OPAQUE RED
        final Rectangle redRectangle = new Rectangle(32, 0, 32, 32);
        fillRectangle(graphics, redRectangle, Color.RED);

        // 50% TRANSPARENT BLUE
        final Rectangle blueAlphaRectangle = new Rectangle(0, 32, 32, 32);
        final Color BLUE_WITH_TRANSPARENCY = new Color(0, 0, 255, 128);
        fillRectangle(graphics, blueAlphaRectangle, BLUE_WITH_TRANSPARENCY);

        // FULLY TRANSPARENT GREEN
        final Rectangle greenRectangle = new Rectangle(32, 32, 32, 32);
        final Color FULLY_TRANSPARENT_GREEN = new Color(0, 255, 0, 0);
        fillRectangle(graphics, greenRectangle, FULLY_TRANSPARENT_GREEN);

        graphics.dispose();

        // Only select Blue and Alpha bands
        RenderedImage grayAlpha = BandSelectDescriptor.create(bi, new int[] { 2, 3 }, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        float quality = 5f / 9 - 1;
        new PNGWriter().writePNG(grayAlpha, bos, -quality, FilterType.FILTER_NONE);
        BufferedImage read = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        Raster raster = read.getData();

        // Check Quadrants are 
        // Top Left     = FULLY WHITE <------------------ FULLY OPAQUE BLUE before BandSelect 
        // Top Right    = FULLY BLACK <------------------ FULLY OPAQUE RED before BandSelect
        // Bottom Left  = WHITE WITH 50% TRANSPARENCY <-- 50% TRANSPARENT BLUE before BandSelect
        // Bottom Right = FULLY TRANSPARENT BLACK <------ FULLY TRANSPARENT GREEN before BandSelect

        assertRectangle(raster, blueRectangle, 255, 255);
        assertRectangle(raster, blueAlphaRectangle, 255, 128);
        assertRectangle(raster, redRectangle, 0, 255);
        assertRectangle(raster, greenRectangle, 0, 0);
    }

    /**
     * check that every pixel of a rectangle has expectedPixelValue and alpha
     */
    private void assertRectangle(Raster raster, Rectangle r, int expectedPixelValue, int expectedAlpha) {
        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                assertEquals(expectedPixelValue, raster.getSample(x, y, 0));
                assertEquals(expectedAlpha, raster.getSample(x, y, 1));
            }
        }
    }

    /**
     * Fill a rectangle with given Color
     */
    private void fillRectangle(Graphics2D graphics, Rectangle rectangle, Color color) {
        graphics.setColor(color);
        graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
}
