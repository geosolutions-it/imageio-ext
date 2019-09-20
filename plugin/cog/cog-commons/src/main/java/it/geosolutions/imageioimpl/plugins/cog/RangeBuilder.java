package it.geosolutions.imageioimpl.plugins.cog;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to build a list of ranges that need to be read by determining if tiles are in a contiguous sequence.
 *
 * @author joshfix
 * Created on 2019-08-27
 */
public class RangeBuilder {

    protected long currentRangeStart;
    protected long currentRangeEnd;
    protected boolean tileAdded = false;
    protected boolean finalized = false;
    protected List<long[]> ranges = new ArrayList<>();

    public RangeBuilder(long initialRangeStart, long initialRangeEnd) {
        // we don't add the initial start/end range immediately.  instead, we will wait until the next range is added
        // and compare it's start position with the current end position.  if it's contiguous, we extend the end range
        // by the byte count of the added tile.  if it's not, we add the current start/end as a range.
        currentRangeStart = initialRangeStart;
        currentRangeEnd = initialRangeEnd;
    }

    public void addTileRange(long offset, long tileOrStripByteCount) {
        tileAdded = true;

        if (finalized) {
            // in the event the getRanges method has already been called, remove that range from the collection so
            // that we can compare the current tile range for contiguity.
            finalized = false;
            ranges.remove(ranges.size() - 1);
        }
        if (offset == currentRangeEnd + 1) {
            // this tile starts where the last one left off
            currentRangeEnd = offset + tileOrStripByteCount - 1;
        } else {
            // this tile is in a new position.  add the current range and start a new one.
            ranges.add(new long[]{currentRangeStart, currentRangeEnd});
            currentRangeStart = offset;
            currentRangeEnd = currentRangeStart + tileOrStripByteCount - 1;
        }
    }

    public List<long[]> getRanges() {
        if (tileAdded) {
            // calling this method implies we're finished examining ranges.  the final range start/end needs to be
            // added to the set of ranges.
            ranges.add(new long[]{currentRangeStart, currentRangeEnd});
            finalized = true;
        }
        return ranges;
    }
}
