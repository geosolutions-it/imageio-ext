/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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
import it.geosolutions.imageioimpl.plugins.cog.*;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Tests reading COGs with both caching and non-caching CogImageInputStreams.
 *
 * @author joshfix
 * Created on 2019-08-22
 */
public class CogHTTPReadOnlineTest {

    private static final String cogUrl = "https://gs-cog.s3.eu-central-1.amazonaws.com/land_topo_cog_jpeg_8192.tif";

    private static final String cogUrl2 = "https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";

    /**
     * Read the first tiles of the stream
     */
    @Test
    public void readCogFirstTiles() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 0;
        int y = 0;
        int width = 2000;
        int height = 2000;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    /**
     * Read an isolated piece of data.
     */
    @Test
    public void readCogPiece() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 1000;
        int y = 1000;
        int width = 1000;
        int height = 1000;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    /**
     * Read a piece of an overview.
     */
    @Test
    public void readCogOverview() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 500;
        int y = 500;
        int width = 500;
        int height = 500;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(2, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    /**
     * Read a whole overview (the latest)
     */
    @Test
    public void readWholeOverview() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        CogImageReadParam param = new CogImageReadParam();
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(4, param);

        Assert.assertEquals(512, cogImage.getWidth());
        Assert.assertEquals(256, cogImage.getHeight());
    }

    /**
     * Read a COG with Header not contained in a single read.
     */
    @Test
    public void testFetchHeader() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        CogImageReadParam param = new CogImageReadParam();
        param.setRangeReaderClass(HttpRangeReader.class);
        param.setHeaderLength(1024);
        int x = 1000;
        int y = 1000;
        int width = 1000;
        int height = 1000;

        param.setSourceRegion(new Rectangle(x, y, width, height));
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());

        // Redo-it
        cogStream = new DefaultCogImageInputStream(cogUrl2);
        reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        param = new CogImageReadParam();
        param.setRangeReaderClass(HttpRangeReader.class);
        param.setHeaderLength(1024);
        param.setSourceRegion(new Rectangle(x, y, width, height));
        cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }



    @Test
    public void readCogCaching() throws IOException {
        CachingCogImageInputStream cogStream = new CachingCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 1000;
        int y = 1000;
        int width = 2000;
        int height = 2000;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

}
