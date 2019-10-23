package it.geosolutions.imageioimpl.plugins.cog;

import java.util.Collection;
import java.util.Map;

/**
 * Defines methods that should be implemented by classes that support remotely reading tile ranges.
 *
 * @author joshfix
 * Created on 10/22/19
 */
public interface RangeReader {

    /**
     * Sets the length of the header to be read.
     *
     * @param headerLength the length of the header
     */
    void setHeaderLength(int headerLength);

    /**
     * Returns the length of the header
     *
     * @return the length of the header
     */
    int getHeaderLength();

    /**
     * Reads the byte ranges specified in the parameter.  To incur maximum performance, where the ranges are contiguous
     * (eg, 1000-1346, 1347-2000), they should be concatenated (eg, 1000-2000).  Additionally, the read operations for
     * each range should execute in parallel and return after all parallel reads have completed.
     *
     * @param ranges a 2D array of start/end byte locations to be read
     * @return Map of start range positions to byte arrays for the provided range
     */
    Map<Long, byte[]> read(long[]... ranges);

    /**
     * Reads the byte ranges specified in the parameter.  To incur maximum performance, where the ranges are contiguous
     * (eg, 1000-1346, 1347-2000), they should be concatenated (eg, 1000-2000).  Additionally, the read operations for
     * each range should execute in parallel and return after all parallel reads have completed.
     *
     * @param ranges a collection of long arrays containing the start/end byte locations to be read
     * @return Map of start range positions to byte arrays for the provided range
     */
    Map<Long, byte[]> read(Collection<long[]> ranges);

    /**
     * Reads the COG header given the provided header length.
     *
     * @return They byte data of the header
     */
    byte[] readHeader();
}
