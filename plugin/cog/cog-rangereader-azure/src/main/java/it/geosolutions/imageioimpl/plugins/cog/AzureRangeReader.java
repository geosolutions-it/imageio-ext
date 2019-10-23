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
public class AzureRangeReader extends AbstractRangeReader {

    protected String containerName;
    protected String filename;
    private final AzureConnector connector;
    private BlobAsyncClient client;

    private final static Logger LOGGER = Logger.getLogger(AzureRangeReader.class.getName());

    public AzureRangeReader(String url, int headerLength) {
        this(URI.create(url), headerLength);
    }

    public AzureRangeReader(URL url, int headerLength) {
        this(URI.create(url.toString()), headerLength);
    }

    public AzureRangeReader(URI uri, int headerLength) {
        super(uri, headerLength);

        containerName = AzureUrlParser.getContainerName(uri);
        filename = AzureUrlParser.getFilename(uri, containerName);
        connector = new AzureConnector(AzureUrlParser.getAccountName(uri));
        client = connector.getAzureClient(containerName, filename);
    }

    @Override
    public byte[] readHeader() {
        BlobRange blobRange = new BlobRange(0l, (long) headerLength);
        Response<Flux<ByteBuffer>> response = client
                .downloadWithResponse(blobRange, null, null, false)
                .block();

        ByteBuffer buffer = ByteBuffer.allocate(headerLength);

        response.getValue()
                .map(buffer::put)
                .blockLast();

        byte[] header = buffer.array();
        data.put(0L, header);
        return header;
    }


    @Override
    public Map<Long, byte[]> read(Collection<long[]> ranges) {
        return read(ranges.toArray(new long[][]{}));
    }

    @Override
    public Map<Long, byte[]> read(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Instant start = Instant.now();
        Map<long[], CompletableFuture<Response<Flux<ByteBuffer>>>> futures = new HashMap<>(ranges.length);

        for (int i = 0; i < ranges.length; i++) {
            long length = ranges[i][1] - ranges[i][0];
            BlobRange blobRange = new BlobRange(ranges[i][0], length);

            CompletableFuture<Response<Flux<ByteBuffer>>> future = client
                    .downloadWithResponse(blobRange, null, null, false)
                    .toFuture();
            futures.put(ranges[i], future);
        }

        awaitCompletion(futures);
        Instant end = Instant.now();
        LOGGER.fine("Time to read all ranges: " + Duration.between(start, end));
        return data;
    }

    /**
     * Blocks until all ranges have been read
     *
     * @param futures
     */
    protected void awaitCompletion(Map<long[], CompletableFuture<Response<Flux<ByteBuffer>>>> futures) {
        boolean stillWaiting = true;
        List<long[]> completed = new ArrayList<>(futures.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (Map.Entry<long[], CompletableFuture<Response<Flux<ByteBuffer>>>> entry : futures.entrySet()) {
                long[] key = entry.getKey();
                CompletableFuture<Response<Flux<ByteBuffer>>> future = entry.getValue();
                if (future.isDone() && !completed.contains(key)) {
                    try {
                        long length = key[1] - key[0] + 8192;
                        ByteBuffer buffer = ByteBuffer.allocate((int) length);
                        //LOGGER.severe("Reading results for range " + key[0] + "-" + key[1] + " with length " + length);
                        //buffer.position((int) key);
                        int pos = 0;
                        future
                                .get()
                                .getValue()
                                .map(buffer::put)
                                .blockLast();
                        buffer.rewind();
                        //buffer.limit((int) (length - 8192));
                       byte[] bytes = buffer.array();
                       LOGGER.severe("before byte length: " + bytes.length);
                        byte[] rangeBytes = Arrays
                                .copyOfRange(bytes, 0, (int)length - 8192);
                        LOGGER.severe("final byte length: " + rangeBytes.length);
                        data.put(key[0], rangeBytes);
                        completed.add(key);
                    } catch (Exception e) {
                        LOGGER.severe("An error occurred while reading the contents of the buffered response "
                                + "from Azure. " + e.getMessage());
                    }
                } else {
                    allDone = false;
                }
            }
            stillWaiting = !allDone;
        }
    }

}
