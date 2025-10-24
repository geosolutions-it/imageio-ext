/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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

import it.geosolutions.imageio.maskband.DatasetLayout;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.*;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.tiff.TiffDatasetLayoutImpl;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileImageInputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests reading COGs with both caching and non-caching CogImageInputStreams.
 *
 * @author joshfix Created on 2019-08-22
 */
public class CogHTTPReadOnlineTest {

    private static final String cogUrl = "https://gs-cog.s3.eu-central-1.amazonaws.com/land_topo_cog_jpeg_8192.tif";

    private static final String cogUrl2 =
            "https://s3-us-west-2.amazonaws.com/sentinel-cogs/sentinel-s2-l2a-cogs/5/C/MK/2018/10/S2B_5CMK_20181020_0_L2A/B01.tif";

    /** Read the first tiles of the stream */
    @Test
    public void readCogFirstTiles() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 0;
        int y = 0;
        int width = 1830;
        int height = 1830;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    /** Read an isolated piece of data. */
    @Test
    public void readCogPiece() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 1000;
        int y = 1000;
        int width = 830;
        int height = 830;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    /** Read a piece of an overview. */
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

    /** Read a whole overview (the latest) */
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

    /** Read a COG with Header not contained in a single read. */
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
        int width = 830;
        int height = 830;

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
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(cogUrl2);
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        CogImageReadParam param = new CogImageReadParam();
        param.setRangeReaderClass(HttpRangeReader.class);
        param.setHeaderLength(1024);
        int x = 1000;
        int y = 1000;
        int width = 830;
        int height = 830;

        param.setSourceRegion(new Rectangle(x, y, width, height));
        BufferedImage cogImage = reader.read(0, param);

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    @Test
    public void readCogJpegMask() throws IOException {
        DefaultCogImageInputStream cogStream = new DefaultCogImageInputStream(
                "http://localhost/tc_81235068/homedepot_spokane_wa_section1_concrete_final_20230921_ortho_COG.tif");
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);

        int x = 0;
        int y = 0;
        int width = 1830;
        int height = 1830;

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        param.setRangeReaderClass(HttpRangeReader.class);
        BufferedImage cogImage = reader.read(0, param);

        System.out.println(cogImage.getSampleModel());
        System.out.println(cogImage.getColorModel());

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }

    @Test
    public void readLocalJpegMask() throws IOException {
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
        reader.setInput(new FileImageInputStream(new File(
                "/var/www/html/tc_81235068/homedepot_spokane_wa_section1_concrete_final_20230921_ortho_COG.tif")));

        DatasetLayout dtLayout = TiffDatasetLayoutImpl.parseLayout(reader.getStreamMetadata());
        System.out.println(dtLayout);

        int x = 0;
        int y = 0;
        int width = 1830;
        int height = 1830;

        ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        BufferedImage cogImage = reader.read(1, param);

        System.out.println(cogImage.getSampleModel());
        System.out.println(cogImage.getColorModel());
        System.out.println(cogImage.getWidth());
        System.out.println(cogImage.getHeight());

        Assert.assertEquals(width, cogImage.getWidth());
        Assert.assertEquals(height, cogImage.getHeight());
    }
}
