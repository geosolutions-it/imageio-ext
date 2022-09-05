/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
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

import it.geosolutions.imageioimpl.plugins.cog.AzureRangeReader;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;
import org.junit.Assert;
import org.junit.Test;
import java.util.Map;

public class AzureRangeReaderOnlineTest {

    @Test
    public void readAzureRanges() {
        String cogUrl = "https://cogtestdata.blob.core.windows.net/cogtestdata/land_topo_cog_jpeg_1024.tif";
        readRanges(new AzureRangeReader(cogUrl, 4096));
    }

    public void readRanges(RangeReader rangeReader) {
        int headerByteLength = 4096;
        byte[] header = rangeReader.readHeader();
        Assert.assertEquals(headerByteLength, header.length);

        long[] range1 = new long[]{5000, 6000};
        long[] range2 = new long[]{8000, 9000};

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
            if (range2Bytes[(int)i] != 0) {
                nonZeroValueFound = true;
                break;
            }
        }
        Assert.assertTrue(nonZeroValueFound);
        
    }
    

}
