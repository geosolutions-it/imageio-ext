package it.geosolutions.imageioimpl.plugins.cog;

/**
 * POJO to store metadata about a single COG tile.
 *
 * @author joshfix
 * Created on 2019-09-24
 */
public class TileRange implements Comparable<TileRange> {

    private final long start;
    private final long end;
    private final long byteLength;
    private final int index;

    public TileRange(int index, long start, long byteLength) {
        this.index = index;
        this.start = start;
        this.byteLength = byteLength;
        this.end = start + byteLength - 1;
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
        int result = 1;
        result = 31 * result + (int)start;
        result = 31 * result + (int)byteLength;
        result = 31 * result + (int)end;
        result = 31 * result + index;
        return result;
    }

    @Override
    public int compareTo(TileRange o) {
        return o.index > this.index ? 1 : 0;
    }
}

