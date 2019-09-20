package it.geosolutions.imageioimpl.plugins.cog;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a utility class that stores all of the required tile range metadata for a given read operation.
 *
 * @author joshfix
 * Created on 2019-08-28
 */
public class CogTileInfo {

    protected int headerSize;
    protected long firstTileOffset = Long.MAX_VALUE;
    protected long firstTileByteLength;
    protected Map<Integer, TileRange> tileRanges = new HashMap<>();

    public CogTileInfo() {}

    public CogTileInfo(int headerSize) {
        this.headerSize = headerSize;
    }

    public void addTileRange(int tileIndex, long offset, long byteLength) {
        if (offset < firstTileOffset && offset > 0) {
            firstTileOffset = offset;
            firstTileByteLength = byteLength;
        }
        if (offset < headerSize) {
            headerSize = (int)offset -1;
        }
        tileRanges.put(tileIndex, new TileRange(tileIndex, offset, byteLength));
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public long getFirstTileOffset() {
        return firstTileOffset;
    }

    public long getFirstTileByteLength() {
        return firstTileByteLength;
    }

    public Map<Integer, TileRange> getTileRanges() {
        return tileRanges;
    }

    public TileRange getTileRange(int tileIndex) {
        return tileRanges.get(tileIndex);
    }

    public TileRange getTileRange(long offset) {
        for (TileRange tileRange : tileRanges.values()) {
            if (offset >= tileRange.getStart() && offset < tileRange.getEnd()) {
                return tileRange;
            }
        }
        return null;
    }

    public int getTileIndex(long offset) {
        for (Map.Entry<Integer, TileRange> entry : tileRanges.entrySet()) {
            if (offset >= entry.getValue().getStart() && offset < entry.getValue().getEnd()) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static class TileRange {

        private final long start;
        private final long end;
        private final long byteLength;
        private final int index;

        public TileRange(int index, long start, long byteLength) {
            this.index = index;
            this.start = start;
            this.byteLength = byteLength;
            this.end = start + byteLength;
        }

        public int getIndex() {
            return index;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public long getByteLength() {
            return byteLength;
        }

        @Override
        public String toString() {
            return "index: " + index + " - start: " + start + " - byteLength: " + byteLength + " - end: " + end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TileRange that = (TileRange) o;

            if (index != that.index || start != that.start || byteLength != that.byteLength || end != that.end) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 * result + (int)start;
            result = 31 * result + (int)byteLength;
            result = 31 * result + (int)end;
            result = 31 * result + index;
            return result;
        }

    }
}
