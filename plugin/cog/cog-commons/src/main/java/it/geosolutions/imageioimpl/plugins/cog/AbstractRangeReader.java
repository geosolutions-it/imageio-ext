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

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.core.ExtCaches;
import it.geosolutions.imageio.utilities.SoftValueHashMap;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author joshfix
 * Created on 2019-08-21
 */
public abstract class AbstractRangeReader implements RangeReader {

    /**
     * Streams (and rangeReader too) get repeatedly initialized on the same URI.
     * Let's cache the header since some COG datasets might have long
     * tileBytes/tileCount TAG which slow down the repeated accesses.
     */
    protected final static Map<String, byte[]> HEADERS_CACHE = new SoftValueHashMap<>();
    
    static {
        ExtCaches.addListener(() -> HEADERS_CACHE.clear());
    }

    protected BasicAuthURI authUri;
    protected URI uri;
    protected SoftValueHashMap<Long, byte[]> data = new SoftValueHashMap<>(0);
    protected int headerLength;
    protected int headerOffset = 0;

    private final static Logger LOGGER = Logger.getLogger(AbstractRangeReader.class.getName());

    public AbstractRangeReader(BasicAuthURI authUri, int headerLength) {
        this.authUri = authUri;
        // store the underlying uri too to avoid several getUri() calls around on the code
        this.uri = authUri.getUri();
        this.headerLength = headerLength;
    }


    /**
     * By default, the implementation just
     * calls {@link URI#toURL()}, subclass should override to handle their specific protocol.
     */
    @Override
    public URL getURL() throws MalformedURLException {
        return uri.toURL();
    }

    /**
     * Prevents making new range requests for image data that overlap with the header range that
     * has already been read
     *
     * @param ranges
     * @return
     */
    protected long[][] reconcileRanges(long[][] ranges) {
        boolean modified = false;
        List<long[]> newRanges = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            int dataLength = headerLength;
            if (ranges[i][0] < dataLength - 1) {
                // this range starts inside of what we already read for the header
                modified = true;
                if (ranges[i][1] <= dataLength - 1) {
                    // this range is fully inside the header which was already read; discard this
                    // range
                    LOGGER.fine("Removed range " + ranges[i][0] + "-" + ranges[i][1] + " as it " +
                            "lies fully within"
                            + " the data already read in the header request");
                } else {
                    // this range starts inside the header range, but ends outside of it.
                    // add a new range that starts at the end of the header range
                    long[] newRange = new long[]{dataLength - 1, ranges[i][1]};

                    // Adjust the data's header size in case the related range have been adjusted
                    byte[] headersData = data.get(0L);
                    if (headersData != null && dataLength < headersData.length) {
                        byte[] newHeader = new byte[dataLength];
                        System.arraycopy(headersData, 0, newHeader, 0, dataLength);
                        data.put(0L, newHeader);
                        HEADERS_CACHE.put(uri.toString(), newHeader);
                    }

                    newRanges.add(newRange);
                    LOGGER.fine("Modified range " + ranges[i][0] + "-" + ranges[i][1]
                            + " to " + dataLength + "-" + ranges[i][1] + " as it overlaps with " +
                            "data previously"
                            + " read in the header request");
                }
            } else {
                // fully outside the header area, keep the range
                newRanges.add(ranges[i]);
            }
        }

        if (modified) {
            return newRanges.toArray(new long[][]{});
        } else {
            LOGGER.fine("No ranges modified.");
            return ranges;
        }
    }

    @Override
    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    @Override
    public int getHeaderLength() {
        return headerOffset + headerLength;
    }

    public static void invalidateCache() {
        HEADERS_CACHE.clear();
    }
}

