/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
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

import com.microsoft.azure.storage.blob.BlobRange;
import com.microsoft.azure.storage.blob.BlobURLParts;
import com.microsoft.azure.storage.blob.URLParser;
import it.geosolutions.imageio.core.BasicAuthURI;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Reads URIs from Azure
 *  */
public class AzureRangeReader extends AbstractRangeReader {

    protected AzureClient client;
    protected AzureConfigurationProperties  configProps;
    private String blobKey;

    private static final int CORE_POOL_SIZE = Integer.getInteger("azure.reader.core.poolsize", 64);
    private static final int MAX_POOL_SIZE = Integer.getInteger("azure.reader.max.poolsize", 128);
    private static final int THREAD_TIMEOUT = Integer.getInteger("azure.reader.timeout.ms", 10000);

    static final ThreadPoolExecutor EXECUTORS;

    static {
        EXECUTORS = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, THREAD_TIMEOUT,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }


    private final static Logger LOGGER = Logger.getLogger(AzureRangeReader.class.getName());

    public AzureRangeReader(String url, int headerLength) {
        this(URI.create(url), headerLength);
    }

    public AzureRangeReader(URL url, int headerLength) {
        this(URI.create(url.toString()), headerLength);
    }

    public AzureRangeReader(URI uri, int headerLength) {
        this(new BasicAuthURI(uri), headerLength);
    }

    public AzureRangeReader(BasicAuthURI uri, int headerLength) {
        super(uri, headerLength);
        configProps = new AzureConfigurationProperties(uri);
        client = AzureClientFactory.getClient(configProps);
        blobKey = getBlobKey(uri.getUri());
    }

    public String getBlobKey(URI uri) {
        BlobURLParts parts = null;
        String path = uri.toASCIIString();
        try {
            parts = URLParser.parse(uri.toURL());
            return parts.blobName();
        } catch (UnknownHostException| MalformedURLException e) {
            throw new RuntimeException("Unable to parse the provided uri " +
                    path + "due to " + e.getLocalizedMessage());
        }
    }

    @Override
    public byte[] fetchHeader() {
        byte[] currentHeader = data.get(0L);
        if ( currentHeader != null) {
            headerOffset = currentHeader.length;
        }
        BlobRange range = buildRange(headerOffset, headerLength);
        try {
            byte[] headerBytes = client.getBytes(blobKey, range);
            if (headerOffset != 0) {
                byte [] oldHeader = data.get(0L);
                byte [] newHeader = new byte[headerBytes.length + oldHeader.length];
                System.arraycopy(oldHeader, 0, newHeader, 0, oldHeader.length);
                System.arraycopy(headerBytes, 0, newHeader, oldHeader.length, headerBytes.length);
                headerBytes = newHeader;
            }

            data.put(0L, headerBytes);
            return headerBytes;
        } catch (Exception e) {
            LOGGER.severe("Error reading header for " + uri);
            throw new RuntimeException(e);
        }

    }

    private BlobRange buildRange(long rangeStart, long rangeLength) {
        return new BlobRange().withOffset(rangeStart).withCount(rangeLength);
    }

    @Override
    public Map<Long, byte[]> read(Collection<long[]> ranges) {
        return read(ranges.toArray(new long[][]{}));
    }

    @Override
    public byte[] readHeader() {
        LOGGER.fine("reading header");
        byte[] currentHeader  = HEADERS_CACHE.get(uri.toString());

        if (currentHeader != null) {
            return currentHeader;
        }
        BlobRange range = buildRange(headerOffset, headerLength);
        try {
            byte[] headerBytes = client.getBytes(blobKey, range);
            data.put(0L, headerBytes);
            HEADERS_CACHE.put(uri.toString(), headerBytes);
            return headerBytes;
        } catch (Exception e) {
            LOGGER.severe("Error reading header for " + uri);
            throw new RuntimeException(e);
        }

    }

    @Override
    public Map<Long, byte[]> read(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Map<Long, byte[]> values = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            long[] range = ranges[i];
            final long rangeStart = range[0];
            final byte[] dataRange = data.get(rangeStart);
            // Check for available data.
            if (dataRange == null) {
                Future<?> future = EXECUTORS.submit(() -> {
                    final long rangeEnd = range[1];
                    int length = (int) (rangeEnd - rangeStart) + 1;
                    byte[] bytes = readInternal(rangeStart, length);
                    data.put(rangeStart, bytes);
                    values.put(rangeStart, bytes);
                });
                futures.add(future);
            } else {
                values.put(rangeStart, dataRange);
            }
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to read data from Azure Blob Storage", e);
            }
        }

        return values;
    }

    byte[] readInternal(long readOffset, int readLength) {
        BlobRange range = buildRange(readOffset, readLength);
        return client.getBytes(blobKey, range);
    }
}
