/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageioimpl.plugins.cog;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

public class GSRangeReaderOnlineTest {

    private String cogUrl = "gs://gcp-public-data-landsat/LC08/01/044/034/LC08_L1GT_044034_20130330_20170310_01_T2/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF";

    @Test
    public void readGSRanges() {
        readRanges(new GSRangeReader(cogUrl, CogImageReadParam.DEFAULT_HEADER_LENGTH));
    }
    
    @Test
    public void readCache() {
        GSRangeReader reader = new GSRangeReader(cogUrl, CogImageReadParam.DEFAULT_HEADER_LENGTH);

        reader = spy(reader);
        
        long[] range1 = new long[]{20000, 21000};

        Map<Long, byte[]> data1 = reader.read(range1);
        Map<Long, byte[]> data2 = reader.read(range1);
        
        int length = (int) (range1[1] - range1[0]) + 1;
        verify(reader, times(1)).readInternal(20000, length);
        
        assertNotNull(data1.get(range1[0]));
        assertEquals(data1.get(range1[0]), data2.get(range1[0]));
    }
    
    public void readRanges(RangeReader rangeReader) {
        int headerByteLength = 16384;
        byte[] header = rangeReader.readHeader();
        Assert.assertEquals(headerByteLength, header.length);
        long[] range1 = new long[]{20000, 21000};
        long[] range2 = new long[]{30000, 31000};

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
