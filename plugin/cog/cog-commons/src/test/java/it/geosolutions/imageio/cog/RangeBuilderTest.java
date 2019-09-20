/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007 - 2016, GeoSolutions
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

import it.geosolutions.imageioimpl.plugins.cog.RangeBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Testing HTTP range reading capabilities.
 * 
 * @author joshfix
 */
public class RangeBuilderTest extends Assert {

    @Test
    public void buildRanges() {
        long initialRangeStart = 50;
        long initialRangeEnd = 100;

        // verify that after adding a single range that is not contiguous with the initial range there are two ranges
        // that start and end where expected
        long tileRange1Start = 200;
        long tileRange1Length = 100;
        RangeBuilder rangeBuilder = new RangeBuilder(initialRangeStart, initialRangeEnd);
        rangeBuilder.addTileRange(tileRange1Start, tileRange1Length);
        List<long[]> ranges = rangeBuilder.getRanges();
        Assert.assertEquals(2, ranges.size());
        Assert.assertEquals(initialRangeStart, ranges.get(0)[0]);
        Assert.assertEquals(initialRangeEnd, ranges.get(0)[1]);
        Assert.assertEquals(tileRange1Start, ranges.get(1)[0]);
        Assert.assertEquals(tileRange1Start + tileRange1Length - 1, ranges.get(1)[1]);

        // verify that after adding adding a tile range that is contiguous with the initial range there is a single
        // range that starts at the initial start and ends at the end of the new tile length
        long tileRange2Start = 101;
        long tileRange2Length = 200;
        rangeBuilder = new RangeBuilder(initialRangeStart, initialRangeEnd);
        rangeBuilder.addTileRange(tileRange2Start, tileRange2Length);
        ranges = rangeBuilder.getRanges();
        Assert.assertEquals(1, ranges.size());
        Assert.assertEquals(initialRangeStart, ranges.get(0)[0]);
        Assert.assertEquals(tileRange2Start + tileRange2Length - 1, ranges.get(0)[1]);
        // verify the byte length of the range
        Assert.assertEquals((initialRangeEnd - initialRangeStart) + tileRange2Length, ranges.get(0)[1] - initialRangeStart);
    }
}
