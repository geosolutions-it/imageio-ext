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

import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * RangeReader implementation to asynchronously read multiple ranges from an HTTP endpoint.
 *
 * @author joshfix
 * Created on 2019-08-21
 */
public class HttpRangeReader extends RangeReader {

    protected OkHttpClient client;

    public static final String CONTENT_RANGE_HEADER = "content-range";
    private final static Logger LOGGER = Logger.getLogger(HttpRangeReader.class.getName());

    public HttpRangeReader(String url) {
        this(URI.create(url));
    }

    public HttpRangeReader(URL url) {
        this(URI.create(url.toString()));
    }

    public HttpRangeReader(URI uri) {
        super(uri);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if (uri.getUserInfo() != null && !uri.getUserInfo().isEmpty()) {
            String[] userPassArray = uri.getUserInfo().split(":");
            final String user = userPassArray[0];
            final String password = userPassArray.length == 2 ? userPassArray[1] : null;

            Authenticator authenticator = (route, response) -> {
                if (response.request().header("Authorization") != null) {
                    return null; // Give up, we've already attempted to authenticate.
                }

                String credential = Credentials.basic(user, password);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            };

            clientBuilder.authenticator(authenticator);
        }

        client = clientBuilder.build();
    }

    @Override
    public byte[] readHeader() {
        LOGGER.fine("Reading header");
        Request request = new Request.Builder()
                .url(uri.toString())
                .header("range", "bytes=" + 0 + "-" + (headerLength - 1))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException();
            }
            // get the filesize
            String contentRange = response.header(CONTENT_RANGE_HEADER);
            if (contentRange.contains("/")) {
                String length = contentRange.split("/")[1];
                try {
                    filesize = Integer.parseInt(length);
                    buffer = ByteBuffer.allocate(filesize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // get the header bytes
            byte[] headerBytes = response.body().bytes();
            writeValue(0, headerBytes);
            return headerBytes;
        } catch (IOException e) {
            LOGGER.severe("Unable to read header for " + uri);
            throw new RuntimeException("Unable to read header for " + uri, e);
        }
    }

    @Override
    public void readAsync(Collection<long[]> ranges) {
        readAsync(ranges.toArray(new long[][]{}));
    }

    @Override
    public void readAsync(long[]... ranges) {
        ranges = reconcileRanges(ranges);
        if (ranges.length == 0) {
            return;
        }

        Instant start = Instant.now();
        Map<Long, AsyncHttpCallback> callbacks = new HashMap<>(ranges.length);

        for (int i = 0; i < ranges.length; i++) {

            Request request = new Request.Builder()
                    .url(uri.toString())
                    .header("Accept", "*/*")
                    .header("range", "bytes=" + ranges[i][0] + "-" + ranges[i][1])
                    .build();

            Call call = client.newCall(request);
            AsyncHttpCallback callback = new AsyncHttpCallback().startPosition((int) ranges[i][0]);

            call.enqueue(callback);

            callbacks.put(ranges[i][0], callback);
        }

        awaitCompletion(callbacks);
        Instant end = Instant.now();
        LOGGER.fine("Time to read all ranges: " + Duration.between(start, end));
    }

    protected void writeValue(int position, byte[] bytes) {
        buffer.position(position);
        try {
            buffer.put(bytes);
        } catch (Exception e) {
            LOGGER.severe("Error writing bytes to ByteBuffer for source " + uri);
        }
    }

    /**
     * Blocks until all ranges have been read and written to the ByteBuffer
     *
     * @param callbacks
     */
    protected void awaitCompletion(Map<Long, AsyncHttpCallback> callbacks) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(callbacks.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (Map.Entry<Long, AsyncHttpCallback> entry : callbacks.entrySet()) {
                long key = entry.getKey();
                AsyncHttpCallback callback = entry.getValue();
                if (callback.isDone()) {
                    if (!completed.contains(key)) {
                        try {
                            writeValue(callback.getStartPosition(), callback.getBytes());
                            completed.add(key);
                        } catch (Exception e) {
                            e.printStackTrace();
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
        return new Request.Builder()
                .url(uri.toString())
                .header("Accept", "*/*")
                .header("range", "bytes=" + range[0] + "-" + range[1])
                .build();
    }

}
