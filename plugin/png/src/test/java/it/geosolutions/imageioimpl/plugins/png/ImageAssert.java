/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class ImageAssert {

    public static void assertImagesEqual(BufferedImage original, BufferedImage image) {
        assertEquals(original.getWidth(), image.getWidth());
        assertEquals(original.getHeight(), image.getHeight());
        // these tests got disabled, as depending on the reader being used you can get a different
        // structure back
        // assertEquals(original.getSampleModel(), image.getSampleModel());
        // assertEquals(original.getColorModel(), image.getColorModel());

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int rgbOriginal = original.getRGB(x, y);
                int rgbActual = image.getRGB(x, y);
                if (rgbOriginal != rgbActual) {
                    fail("Comparison failed at x:" + x + ", y: " + y + ", expected "
                            + colorToString(rgbOriginal) + ", got " + colorToString(rgbActual));
                }
            }
        }
    }

    private static String colorToString(int rgb) {
        Color c = new Color(rgb);
        return "RGBA[" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ", "
                + c.getAlpha() + "]";
    }

    public static void showImage(String title, long timeOut, final BufferedImage image)
            throws InterruptedException {
        final String headless = System.getProperty("java.awt.headless", "false");
        if (!headless.equalsIgnoreCase("true")) {
            try {
                Frame frame = new Frame(title);
                frame.addWindowListener(new WindowAdapter() {

                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

                Panel p = new Panel() {

                    /** <code>serialVersionUID</code> field */
                    private static final long serialVersionUID = 1L;

                    {
                        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                    }

                    public void paint(Graphics g) {
                        g.drawImage(image, 0, 0, this);
                    }

                };

                frame.add(p);
                frame.pack();
                frame.setVisible(true);

                Thread.sleep(timeOut);
                frame.dispose();
            } catch (HeadlessException exception) {
                // The test is running on a machine without X11 display. Ignore.
            }
        }
    }

}
