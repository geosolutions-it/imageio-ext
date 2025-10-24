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
package it.geosolutions.imageioimpl.plugins.cog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing HTTP range reading capabilities.
 *
 * @author joshfix
 */
public class S3RangeReaderOnlineTest {

    private static String cogUrl =
            "https://s3-us-west-2.amazonaws.com/sentinel-cogs/sentinel-s2-l2a-cogs/5/C/MK/2018/10/S2B_5CMK_20181020_0_L2A/B01.tif";

    private static BasicAuthURI uri;

    static {
        uri = new BasicAuthURI(cogUrl);
        uri.setPassword("");
        uri.setUser("");
    }

    @Test
    public void readS3Ranges() {
        String region = "us-west-2";
        System.setProperty("iio.s3.aws.region", region);
        readRanges(new S3RangeReader(uri, CogImageReadParam.DEFAULT_HEADER_LENGTH));
    }

    @Test
    public void s3RangeGetURL() throws MalformedURLException {
        String bucket = "sentinel-cogs";
        String file = "sentinel-s2-l2a-cogs/5/C/MK/2018/10/S2B_5CMK_20181020_0_L2A/B01.tif";
        String cogUrl = "s3://" + bucket + "/" + file;
        String region = "us-west-2";
        BasicAuthURI s3uri = new BasicAuthURI(cogUrl);
        s3uri.setPassword("");
        s3uri.setUser("");
        System.setProperty("iio.s3.aws.region", region);

        S3RangeReader reader = new S3RangeReader(s3uri, CogImageReadParam.DEFAULT_HEADER_LENGTH);

        // s3:// isn't recognized as a known protocol so a real URL can't be built on top of it.
        // Let's check that we can get a valid URL anyway (translating it to http protocol).
        URL url = reader.getURL();
        S3ConfigurationProperties configurationProperties =
                new S3ConfigurationProperties(uri.getUri().getScheme(), uri);
        assertEquals(region, configurationProperties.getRegion());
        assertEquals(bucket, configurationProperties.getBucket());
    }

    @Test
    public void readHttpRanges() {
        readRanges(new S3RangeReader(uri, CogImageReadParam.DEFAULT_HEADER_LENGTH));
    }

    @Test
    public void readCache() {
        S3RangeReader reader = new S3RangeReader(uri, CogImageReadParam.DEFAULT_HEADER_LENGTH);

        reader = spy(reader);

        long[] range1 = new long[] {20000, 21000};

        Map<Long, byte[]> data1 = reader.read(range1);
        Map<Long, byte[]> data2 = reader.read(range1);

        verify(reader, times(1)).readAsync(20000, 21000);

        assertNotNull(data1.get(range1[0]));
        assertEquals(data1.get(range1[0]), data2.get(range1[0]));
    }

    public void readRanges(RangeReader rangeReader) {
        int headerByteLength = 16384;
        byte[] header = rangeReader.readHeader();
        assertEquals(headerByteLength, header.length);

        long[] range1 = new long[] {20000, 21000};
        long[] range2 = new long[] {30000, 31000};

        Map<Long, byte[]> data = rangeReader.read(range1, range2);

        // verify the first range was read
        byte[] range1Bytes = data.get(range1[0]);
        long range1Length = range1[1] - range1[0];
        boolean nonZeroValueFound = false;
        for (int i = 0; i < range1Length; i++) {
            if (range1Bytes[i] != 0) {
                nonZeroValueFound = true;
                break;
            }
        }
        Assert.assertTrue(nonZeroValueFound);

        // verify the second range was read
        byte[] range2Bytes = data.get(range2[0]);
        long range2Length = range2[1] - range2[0];
        nonZeroValueFound = false;
        for (long i = 0; i < range2Length; i++) {
            if (range2Bytes[(int) i] != 0) {
                nonZeroValueFound = true;
                break;
            }
        }
        Assert.assertTrue(nonZeroValueFound);
    }

    @Test
    public void testUSA() throws Exception {
        BasicAuthURI uri = new BasicAuthURI(
                "s3-us://sentinel-cogs/sentinel-s2-l2a-cogs/32/T/MS/2025/2/S2B_32TMS_20250209_0_L2A/B02.tif");
        uri.setPassword("");
        uri.setUser("");
        String region = "us-west-2";
        System.setProperty("ioo.s3-us.aws.region", region);
        readRanges(new S3RangeReader(uri, CogImageReadParam.DEFAULT_HEADER_LENGTH));
    }
}
