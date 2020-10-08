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
        LOGGER.fine("Reading header");
        Request.Builder requestBuilder = new Request.Builder()
                .url(uri.toString())
                .header("range", "bytes=0-" + (headerLength - 1));

        if (credentials != null) {
            requestBuilder.header("Authorization", credentials);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unable to read header for " + uri + ". "
                        + "Code: " + response.code() + ". Reason: " + response.message());
            }

            // get the header bytes
            byte[] headerBytes = response.body().bytes();
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

        for (int i = 0; i < ranges.length; i++) {
            Call call = client.newCall(buildRequest(ranges[i]));
            AsyncHttpCallback callback = new AsyncHttpCallback().startPosition(ranges[i][0]);
            call.enqueue(callback);
            callbacks.add(callback);
        }

        awaitCompletion(callbacks);
        Instant end = Instant.now();
        LOGGER.fine("Time to read all ranges: " + Duration.between(start, end));
        return data;
    }

    /**
     * Blocks until all ranges have been read and written to the ByteBuffer
     *
     * @param callbacks
     */
    protected void awaitCompletion(List<AsyncHttpCallback> callbacks) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(callbacks.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (AsyncHttpCallback callback : callbacks) {
                if (callback.isDone()) {
                    if (!completed.contains(callback.getStartPosition())) {
                        try {
                            data.put(callback.getStartPosition(), callback.getBytes());
                            completed.add(callback.getStartPosition());
                        } catch (Exception e) {
                            LOGGER.severe("An error occurred while writing the contents of the HTTP response "
                                    + "to the final ByteBuffer.  " + e.getMessage());
                        }
                    }
                } else {
                    allDone = false;
                }
            }
            stillWaiting = !allDone;
        }
    }

    protected Request buildRequest(long[] range) {
        LOGGER.fine("Building request for range " + range[0] + '-' + range[1] + " to " + uri.toString());
        Request.Builder requestBuilder = new Request.Builder()
                .url(uri.toString())
                .header("Accept", "*/*")
                .header("range", "bytes=" + range[0] + "-" + range[1]);

        if (credentials != null) {
            requestBuilder.header("Authorization", credentials);
        }

        return requestBuilder.build();
    }

}
