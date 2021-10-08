/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2021, GeoSolutions
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
package it.geosolutions.imageio.tiff;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.cog.DefaultCogImageInputStream;
import it.geosolutions.imageioimpl.plugins.cog.GSRangeReader;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class COGGoogleReadOnlineTest {

    private static String GS_COG_URI = "gs://gcp-public-data-landsat/LC08/01/044/034" +
            "/LC08_L1GT_044034_20130330_20170310_01_T2" +
            "/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF";

    private static String HTTPS_COG_URI = "https://storage.googleapis" +
            ".com/gcp-public-data-landsat/LC08/01/044/034" +
            "/LC08_L1GT_044034_20130330_20170310_01_T2" +
            "/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF";


    @Test
    public void testGSReadFirstTiles() throws Exception {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(GS_COG_URI);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 0;
        int y = 0;
        int width = 2000;
        int height = 2000;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(GSRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    @Test
    public void testHTTPReadFirstTiles() throws Exception {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(HTTPS_COG_URI);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 0;
        int y = 0;
        int width = 2000;
        int height = 2000;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(GSRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }
}
