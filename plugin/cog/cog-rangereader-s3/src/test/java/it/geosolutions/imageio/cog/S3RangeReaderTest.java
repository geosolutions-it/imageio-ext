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
package it.geosolutions.imageio.cog;

import it.geosolutions.imageioimpl.plugins.cog.RangeReader;
import it.geosolutions.imageioimpl.plugins.cog.S3RangeReader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing HTTP range reading capabilities.
 * 
 * @author joshfix
 */
public class S3RangeReaderTest {

    @Test
    public void readS3Ranges() {
        String cogUrl = "s3://landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";
        String region = "us-west-2";

        System.setProperty("s3.aws.region", region);
        readRanges(new S3RangeReader(cogUrl));
    }

    @Test
    public void readHttpRanges() {
        String cogUrl = "https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";
        readRanges(new S3RangeReader(cogUrl));
    }

    public void readRanges(RangeReader rangeReader) {
        int headerByteLength = 16384;
        byte[] header = rangeReader.readHeader();
        Assert.assertEquals(headerByteLength, header.length);

        long[] range1 = new long[]{20000, 21000};
        long[] range2 = new long[]{30000, 31000};
        rangeReader.readAsync(range1, range2);

        byte[] bytes = rangeReader.getBytes();

        // verify the first range was read
        boolean nonZeroValueFound = false;
        for (long i = range1[0]; i < range1[1]; i++) {
            if (bytes[(int)i] != 0) {
                nonZeroValueFound = true;
                break;
            }
        }
        Assert.assertTrue(nonZeroValueFound);

        // verify the second range was read
        nonZeroValueFound = false;
        for (long i = range2[0]; i < range2[1]; i++) {
            if (bytes[(int)i] != 0) {
                nonZeroValueFound = true;
                break;
            }
        }
        Assert.assertTrue(nonZeroValueFound);

        // verify there is no data between the end of the first range and the start of the second range
        nonZeroValueFound = false;
        for (long i = range1[1] + 1; i < range2[0] - 1; i++) {
            if (bytes[(int)i] != 0) {
                nonZeroValueFound = true;
                break;
            }
        }
        Assert.assertFalse(nonZeroValueFound);
    }

}
