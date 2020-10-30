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
import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * RangeReader implementation to asynchronously read multiple ranges from an HTTP endpoint.
 *
 * @author joshfix
 * Created on 2019-08-21
 */
public class HttpRangeReader extends AbstractRangeReader {

    protected OkHttpClient client;
    private String credentials;

    private final static int MAX_RETRIES;

    static {
        String maxRetries = System.getProperty("it.geosolutions.cog.http.maxretries", "5");
        MAX_RETRIES = Integer.parseInt(maxRetries);
    }

    private final static Logger LOGGER = Logger.getLogger(HttpRangeReader.class.getName());

    public HttpRangeReader(String url, int headerLength) {
        this(URI.create(url), headerLength);
    }

    public HttpRangeReader(URL url, int headerLength) {
        this(URI.create(url.toString()), headerLength);
    }

    public HttpRangeReader(URI uri, int headerLength) {
        this (new BasicAuthURI(uri), headerLength);
    }

    public HttpRangeReader(BasicAuthURI uri, int headerLength) {
        super(uri, headerLength);
        if (uri.getUser() != null && uri.getPassword() != null) {
            credentials = Credentials.basic(uri.getUser(), uri.getPassword());
        }
        client = HttpClientFactory.getClient();
    }

    @Override
    public byte[] readHeader() {
        LOGGER.fine("reading header");
        byte[] currentHeader  = HEADERS_CACHE.get(uri.toString());

        if (currentHeader != null) {
            return currentHeader;
        }
        Request request = buildRequest(new long[]{headerOffset, (headerOffset + headerLength - 1)}, null);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unable to read header for " + uri + ". "
                        + "Code: " + response.code() + ". Reason: " + response.message());
            }

            // get the header bytes
            byte[] headerBytes = response.body().bytes();
            data.put(0L, headerBytes);
            HEADERS_CACHE.put(uri.toString(), headerBytes);
            return headerBytes;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read header for " + uri, e);
        }
    }

    @Override
    public byte[] fetchHeader() {
        LOGGER.fine("Fetching header");
        byte[] currentHeader = data.get(0L);
        if ( currentHeader != null) {
            headerOffset = currentHeader.length;
        }

        Request request = buildRequest(new long[]{headerOffset, (headerOffset + headerLength - 1)}, null);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unable to read header for " + uri + ". "
                        + "Code: " + response.code() + ". Reason: " + response.message());
            }

            // get the header bytes
            byte[] headerBytes = response.body().bytes();
            if (headerOffset != 0) {
                byte [] oldHeader = data.get(0L);
                byte [] newHeader = new byte[headerBytes.length + oldHeader.length];
                System.arraycopy(oldHeader, 0, newHeader, 0, oldHeader.length);
                System.arraycopy(headerBytes, 0, newHeader, oldHeader.length, headerBytes.length);
                headerBytes = newHeader;
                HEADERS_CACHE.put(uri.toString(), newHeader);
            }

            data.put(0L, headerBytes);
            return headerBytes;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read header for " + uri, e);
        }
    }

    @Override
    public Map<Long, byte[]> read(Collection<long[]> ranges) {
        return read(ranges.toArray(new long[][]{}));
    }

    @Override
    public Map<Long, byte[]> read(long[]... ranges) {
        ranges = reconcileRanges(ranges);
        if (ranges.length == 0) {
            return data;
        }

        Instant start = Instant.now();
        List<AsyncHttpCallback> callbacks = new ArrayList<>(ranges.length);
        Map<Long, byte[]> values = new HashMap<>();
        int missingRanges[] = new int[ranges.length];
        int missing = 0;

        for (int i = 0; i < ranges.length; i++) {
            byte[] dataRange = data.get(ranges[i][0]);
            // check for available data
            if (dataRange == null) {
                Call call = client.newCall(buildRequest(ranges[i], null));
                AsyncHttpCallback callback = new AsyncHttpCallback().initRange(ranges[i]);
                call.enqueue(callback);
                callbacks.add(callback);
                // Mark the range as missing
                missingRanges[missing++] = i;
            } else {
                values.put(ranges[i][0], dataRange);
            }
        }

        awaitCompletion(values, callbacks);
        Instant end = Instant.now();
        LOGGER.fine("Time to read all ranges: " + Duration.between(start, end));
        for (int k = 0; k < missing; k++) {
            long range = ranges[missingRanges[k]][0];
            data.put(range, values.get(range));
        }
        return values;

    }

    /**
     * Blocks until all ranges have been read and written to the ByteBuffer
     *
     * @param data
     * @param callbacks
     */
    protected void awaitCompletion(Map<Long, byte[]> data, List<AsyncHttpCallback> callbacks) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(callbacks.size());
        int attempts = 0;
        while (stillWaiting) {
            boolean allDone = true;
            for (AsyncHttpCallback callback : callbacks) {
                AsyncHttpCallback.Status status = callback.getStatus();
                if (status == AsyncHttpCallback.Status.DONE) {
                    if (!completed.contains(callback.getStartPosition())) {
                        try {
                            data.put(callback.getStartPosition(), callback.getBytes());
                            completed.add(callback.getStartPosition());
                        } catch (Exception e) {
                            LOGGER.severe("An error occurred while writing the contents of the HTTP response "
                                    + "to the final ByteBuffer.  " + e.getMessage());
                        }
                    }
                } else if (status == AsyncHttpCallback.Status.FAILED) {
                    // Re-enqueue
                    if (attempts < MAX_RETRIES) {
                        long[] range = new long[]{callback.getStartPosition(), callback.getEndPosition()};
                        callback.resetStatus();
                        Call call = client.newCall(buildRequest(range, "*/*"));
                        call.enqueue(callback);
                        allDone = false;
                        attempts++;
                    }
                } else {
                    allDone = false;
                }
            }
            stillWaiting = !allDone;
        }
    }

    protected Request buildRequest(long[] range, String accept) {
        LOGGER.fine("Building request for range " + range[0] + '-' + range[1] + " to " + uri.toString());
        Request.Builder requestBuilder = new Request.Builder()
                .url(uri.toString())
                .header("range", "bytes=" + range[0] + "-" + range[1]);
        if (accept != null && !accept.isEmpty()) {
            requestBuilder.header("Accept", accept);
        }

        if (credentials != null) {
            requestBuilder.header("Authorization", credentials);
        }

        return requestBuilder.build();
    }

}
