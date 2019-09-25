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

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.BlobRange;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Reads URIs from Azure with the format
 * wasb://<container>@<storage_account>.blob.core.windows.net/path/image.tif
 * or
 * http[s]://<storage_account>.blob.core.windows.net/<container>/path/image.tif
 *
 * @author joshfix
 * Created on 2019-08-21
 */
public class AzureRangeReader extends RangeReader {

    protected String containerName;
    protected String filename;
    private final AzureConnector connector;
    private BlobAsyncClient client;

    private final static Logger LOGGER = Logger.getLogger(AzureRangeReader.class.getName());

    public AzureRangeReader(String url) {
        this(URI.create(url));
    }

    public AzureRangeReader(URL url) {
        this(URI.create(url.toString()));
    }

    public AzureRangeReader(URI uri) {
       super(uri);

        containerName = AzureUrlParser.getContainerName(uri);
        filename = AzureUrlParser.getFilename(uri, containerName);
        connector = new AzureConnector(AzureUrlParser.getAccountName(uri));
        client = connector.getAzureClient(containerName, filename);
        filesize = (int)client.getProperties().block().blobSize();
        buffer = ByteBuffer.allocate(filesize);
    }

    @Override
    public byte[] readHeader() {
        BlobRange blobRange = new BlobRange(0l, (long)headerLength);
        Response<Flux<ByteBuffer>> response = client
                .downloadWithResponse(blobRange, null, null, false)
                .block();

        final PositionTracker positionTracker = new PositionTracker(0);
        response.value()
                .map(bb -> {
                    buffer.position(positionTracker.getPosition());
                    buffer.put(bb);
                    positionTracker.advancePosition(bb.limit());
                    return bb;
                })
                .blockLast();

        byte[] b = new byte[headerLength];
        buffer.rewind();
        buffer.get(b, 0, headerLength);

        return b;
    }


    @Override
    public void readAsync(Collection<long[]> ranges) {
        readAsync(ranges.toArray(new long[][]{}));
    }

    @Override
    public void readAsync(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Instant start = Instant.now();
        Map<Long, CompletableFuture<Response<Flux<ByteBuffer>>>> futures = new HashMap<>(ranges.length);

        for (int i = 0; i < ranges.length; i++) {
            BlobRange blobRange = new BlobRange(ranges[i][0], ranges[i][1]);

            CompletableFuture<Response<Flux<ByteBuffer>>> future = client
                    .downloadWithResponse(blobRange, null, null, false)
                    .toFuture();
                    futures.put(ranges[i][0], future);
        }

        awaitCompletion(futures);
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
     * @param futures
     */
    protected void awaitCompletion(Map<Long, CompletableFuture<Response<Flux<ByteBuffer>>>> futures) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(futures.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (Map.Entry<Long, CompletableFuture<Response<Flux<ByteBuffer>>>> entry : futures.entrySet()) {
                long key = entry.getKey();
                CompletableFuture<Response<Flux<ByteBuffer>>> future = entry.getValue();
                if (future.isDone()) {
                    if (!completed.contains(key)) {
                        try {
                            final PositionTracker positionTracker = new PositionTracker((int)key);
                            future.get().value().map(bb -> {
                                buffer.position(positionTracker.getPosition());
                                buffer.put(bb);
                                positionTracker.advancePosition(bb.limit());
                                return bb;
                            }).blockLast();

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

    class PositionTracker {
        private int position;

        public PositionTracker(int position) {
            this.position = position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public PositionTracker position(int position) {
            setPosition(position);
            return this;
        }

        public PositionTracker advancePosition(int len) {
            position += len;
            return this;
        }


    }

}
