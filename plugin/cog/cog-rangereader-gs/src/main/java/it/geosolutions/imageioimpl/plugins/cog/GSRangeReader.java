/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2021, GeoSolutions
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

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import it.geosolutions.imageio.core.BasicAuthURI;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Reads URIs from Google Storage with the formats:
 * <ul>
 *   <li><code>gs://[bucket]/[name]</code></li>
 *   <li><code>https://storage.cloud.google.com/[bucket]/[name]</code></li>
 *   <li><code>https://storage.googleapis.com/[bucket]/[name]</code></li>
 * </ul>
 *
 * <p>API documentation:
 *
 * <ul>
 *   <li>https://cloud.google.com/storage/docs/reference/libraries#client-libraries-install-java
 *   <li>https://googleapis.dev/java/google-cloud-storage/latest/index.html
 * </ul>
 */
public class GSRangeReader extends AbstractRangeReader {

    public static final String AUTH_URL_BASE = "https://storage.cloud.google.com";
    public static final String PUBLIC_URL_BASE = "https://storage.googleapis.com";

    private static final Logger LOGGER = Logger.getLogger(GSRangeReader.class.getName());

    // Google Storage seems to love concurrent access, so pump it up by default 
    private static final int CORE_POOL_SIZE = Integer.getInteger("gs.reader.core.poolsize", 64);
    private static final int MAX_POOL_SIZE = Integer.getInteger("gs.reader.max.poolsize", 128);
    private static final int THREAD_TIMEOUT = Integer.getInteger("gs.reader.timeout.ms", 10000);

    static final ThreadPoolExecutor EXECUTORS;

    static {
        EXECUTORS = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, THREAD_TIMEOUT,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    private final BlobId id;
    private Blob blob;

    public GSRangeReader(String uri, int headerLength) {
        this(URI.create(uri), headerLength);
    }

    public GSRangeReader(URI uri, int headerLength) {
        this(new BasicAuthURI(uri), headerLength);
    }

    public GSRangeReader(BasicAuthURI uri, int headerLength) {
        super(uri, headerLength);
        this.id = getBlobId(uri.getUri());
        // lazy load the blob field, makes a huge performance difference when loading a mosaic
    }

    @Override
    public URL getURL() throws MalformedURLException {
        return new URL(AUTH_URL_BASE + "/" + id.getBucket() + "/" + id.getName());
    }

    public static BlobId getBlobId(URI uri) {
        BlobId id;
        if (uri.getScheme().equals("gs")) {
            id = BlobId.fromGsUtilUri(uri.toASCIIString());
        } else if (uri.toASCIIString().startsWith(AUTH_URL_BASE)) {
            String path = uri.toASCIIString().substring(AUTH_URL_BASE.length() + 1);
            int idx = path.indexOf('/');
            String bucket = path.substring(0, idx);
            String key = path.substring(idx + 1);
            id = BlobId.of(bucket, key);
        } else if (uri.toASCIIString().startsWith(PUBLIC_URL_BASE)) {
            String path = uri.toASCIIString().substring(PUBLIC_URL_BASE.length() + 1);
            int idx = path.indexOf('/');
            String bucket = path.substring(0, idx);
            String key = path.substring(idx + 1);
            id = BlobId.of(bucket, key);
        } else {
            throw new IllegalArgumentException("Don't know how to process GS link: " + uri);
        }
        return id;
    }

    @Override
    public byte[] fetchHeader() {
        byte[] currentHeader = data.get(0L);
        if (currentHeader != null) {
            headerOffset = currentHeader.length;
        }

        byte[] headerBytes = readInternal(headerOffset, headerLength);
        data.put(0L, headerBytes);
        return headerBytes;
    }

    private byte[] readInternal(long readOffset, int readLength) {
        try (ReadChannel channel = getBlob().reader()) {
            ByteBuffer buffer = ByteBuffer.allocate(readLength);
            channel.seek(readOffset);
            int bytesRead = channel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[bytesRead];
            buffer.get(bytes, 0, bytesRead);
            return bytes;
        } catch (Exception e) {
            LOGGER.severe("Error reading range, offset " + readOffset + ", length " + readLength);
            throw new RuntimeException(e);
        }
    }

    private Blob getBlob() {
        if (this.blob == null) {
            synchronized (this) {
                if (this.blob == null) {
                    this.blob = BlobCache.getBlob(authUri.getUser(), id);
                    if (blob == null)
                        throw new IllegalArgumentException("No blob exist at " + id);
                }
            }
        }
        return this.blob;
    }

    @Override
    public Map<Long, byte[]> read(Collection<long[]> ranges) {
        return read(ranges.toArray(new long[][]{}));
    }

    @Override
    public byte[] readHeader() {
        LOGGER.fine("reading header");
        byte[] currentHeader = HEADERS_CACHE.get(uri.toString());

        if (currentHeader != null) {
            return currentHeader;
        }

        byte[] headerBytes = readInternal(headerOffset, headerLength);
        data.put(0L, headerBytes);
        HEADERS_CACHE.put(uri.toString(), headerBytes);
        return headerBytes;
    }

    @Override
    public Map<Long, byte[]> read(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Instant start = Instant.now();
        Map<Long, CompletableFuture<byte[]>> downloads = new HashMap<>(ranges.length);

        Map<Long, byte[]> values = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            long[] range = ranges[i];
            final byte[] dataRange = data.get(range);
            // Check for available data.
            if (dataRange == null) {
                Future<?> future = EXECUTORS.submit(() -> {
                    int length = (int) (range[1] - range[0]) + 1;
                    byte[] bytes = readInternal(range[0], length);
                    data.put(range[0], bytes);
                    values.put(range[0], bytes);
                });
                futures.add(future);
            } else {
                values.put(range[0], dataRange);
            }
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to read data from Google Storage", e);
            }
        }

        return values;
    }

    /**
     * Blocks until all ranges have been read and written to the ByteBuffer
     *
     * @param data
     * @param downloads
     */
    protected void awaitCompletion(Map<Long, byte[]>
                                           data, Map<Long,
            CompletableFuture<byte[]>> downloads) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(downloads.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (Map.Entry<Long, CompletableFuture<byte[]>> entry :
                    downloads.entrySet()) {
                long key = entry.getKey();
                CompletableFuture<byte[]> future = entry.getValue();
                if (future.isDone()) {
                    if (!completed.contains(key)) {
                        try {
                            data.put(key, future.get());
                            completed.add(key);
                        } catch (Exception e) {
                            LOGGER.warning("Unable to write data from S3 to the destination " +
                                    "ByteBuffer. "
                                    + e.getMessage());
                        }
                    }
                } else {
                    allDone = false;
                }
            }
            stillWaiting = !allDone;
        }
    }
}
