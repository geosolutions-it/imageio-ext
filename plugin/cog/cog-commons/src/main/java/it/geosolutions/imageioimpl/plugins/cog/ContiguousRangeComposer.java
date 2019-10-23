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
package it.geosolutions.imageioimpl.plugins.cog;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to examine all COG tiles that need to be read for the current request and build a list of ranges
 * tiles that are in a contiguous sequence.
 *
 * @author joshfix
 * Created on 2019-08-27
 */
public class ContiguousRangeComposer {

    protected long currentRangeStart;
    protected long currentRangeEnd;
    protected boolean tileAdded = false;
    protected Set<long[]> ranges = new HashSet<>();

    /**
     * we don't add the initial start/end range immediately.  instead, we will wait until the next range is added
     * and compare it's start position with the current end position.  if it's contiguous, we extend the end range
     * by the byte count of the added tile.  if it's not, we add the current start/end as a range.
     * @param initialRangeStart start byte location of the tile
     * @param initialRangeEnd end byte location of the tile
     */
    public ContiguousRangeComposer(long initialRangeStart, long initialRangeEnd) {
        currentRangeStart = initialRangeStart;
        currentRangeEnd = initialRangeEnd;
    }

    /**
     * Accepts a start and end byte position and determines if the start position directly follows the current range
     * end position.  If true, the range end will be extended.  If false, the current range will be added to the list
     * of ranges and a new current range start/end will be established.
     *
     * @param start the start byte position
     * @param end the end byte position
     */
    public void addTileRange(long start, long end) {
        tileAdded = true;

        if (start == currentRangeEnd + 1) {
            // this tile starts where the last one left off
            currentRangeEnd = end;
        } else {
            // this tile is in a new position.  add the current range and start a new one.
            ranges.add(new long[]{currentRangeStart, currentRangeEnd});
            currentRangeStart = start;
            currentRangeEnd = end;
        }
    }

    /**
     * Returns the set of ranges.  Note that the addTileRange method only adds a range to the list if the range was not
     * contiguous
     * @return
     */
    public Set<long[]> getRanges() {
        if (tileAdded) {
            Set<long[]> rangeCopy = new HashSet<>(ranges);
            rangeCopy.add(new long[]{currentRangeStart, currentRangeEnd});
            return rangeCopy;
        }

        return Collections.EMPTY_SET;
    }
}
