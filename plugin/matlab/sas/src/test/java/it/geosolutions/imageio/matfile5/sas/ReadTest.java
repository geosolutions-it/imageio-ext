/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.matfile5.sas;

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import org.junit.Test;

public class ReadTest {

    private static final Logger LOGGER = Logger.getLogger(ReadTest.class.toString());

    @Test
    public void testAffine() throws IOException {

        File file;
        try {
            // file = new File("D:/MUSCLE_CAT2_091011_2_1_p_2_156_40_150.mat");
            file = TestData.file(this, "sas_sample.mat");
        } catch (IOException e) {
            LOGGER.info("Unable to run test due to " + e.getLocalizedMessage());
            return;
        }
        Long timeStart = System.currentTimeMillis();
        ImageReader reader = new SASTileImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        ImageReadParam param = new ImageReadParam();
         param.setSourceSubsampling(2,2,0,0);
        // param.setSourceRegion(new Rectangle(0,0,500,800));
        BufferedImage bi = reader.read(0, param);
        Long timeEnd = System.currentTimeMillis();
        System.out.println((timeEnd - timeStart) / 1000);
         ImageIOUtilities.visualize(bi, "prova", true);
        reader.dispose();
        reader = null;
    }
}
