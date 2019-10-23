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

import it.geosolutions.imageioimpl.plugins.cog.ContiguousRangeComposer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Testing HTTP range reading capabilities.
 * 
 * @author joshfix
 */
public class RangeBuilderTest {

    @Test
    public void buildRanges() {
        long initialRangeStart = 50;
        long initialRangeEnd = 100;

        // verify that after adding a single range that is not contiguous with the initial range there are two ranges
        // that start and end where expected
        long tileRange1Start = 200;
        long tileRange1End = 300;
        ContiguousRangeComposer rangeBuilder = new ContiguousRangeComposer(initialRangeStart, initialRangeEnd);
        rangeBuilder.addTileRange(tileRange1Start, tileRange1End);
        List<long[]> ranges = new ArrayList<>(rangeBuilder.getRanges());
        Assert.assertEquals(2, ranges.size());

        ranges.forEach(range -> {
           Assert.assertTrue(range[0] == initialRangeStart || range[0] == tileRange1Start);
            Assert.assertTrue(range[1] == initialRangeEnd || range[1] == tileRange1End);
        });

        // verify that after adding adding a tile range that is contiguous with the initial range there is a single
        // range that starts at the initial start and ends at the end of the new tile length
        long tileRange2Start = 101;
        long tileRange2End = 200;
        rangeBuilder = new ContiguousRangeComposer(initialRangeStart, initialRangeEnd);
        rangeBuilder.addTileRange(tileRange2Start, tileRange2End);
        ranges = new ArrayList<>(rangeBuilder.getRanges());
        Assert.assertEquals(1, ranges.size());
        Assert.assertEquals(initialRangeStart, ranges.get(0)[0]);
        Assert.assertEquals(tileRange2End, ranges.get(0)[1]);
        // verify the byte length of the range
        Assert.assertEquals(tileRange2End - initialRangeStart, ranges.get(0)[1] - ranges.get(0)[0]);
    }
}
