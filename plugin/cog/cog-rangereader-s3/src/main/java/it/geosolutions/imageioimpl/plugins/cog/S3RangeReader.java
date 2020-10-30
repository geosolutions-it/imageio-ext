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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static software.amazon.awssdk.core.async.AsyncResponseTransformer.toBytes;

/**
 * Reads URIs from S3 with the format
 * https://s3-<region>.amazonaws.com/<bucket>/<key>
 * https://s3.<region>.amazonaws.com/<bucket>/<key>
 * https://<bucket>.s3-<region>.amazonaws.com/<key>
 * https://<bucket>.s3.<region>.amazonaws.com/<key>
 * or
 * s3://<bucket>/<key>
 * For the latter, the region must be set via environment variables, system properties or be provided via the URL
 * parameter `region`:
 * * s3://<bucket>/<key>?region=us-west-2
 *
 * API documentation: https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/aws-sdk-java-dg-v2.pdf
 *
 * @author joshfix
 * Created on 2019-08-21
 */
public class S3RangeReader extends AbstractRangeReader {

    protected S3AsyncClient client;
    protected S3ConfigurationProperties configProps;

    private final static Logger LOGGER = Logger.getLogger(S3RangeReader.class.getName());

    public S3RangeReader(String url, int headerLength) {
        this(URI.create(url), headerLength);
    }

    public S3RangeReader(URL url, int headerLength) {
        this(URI.create(url.toString()), headerLength);
    }

    public S3RangeReader(URI uri, int headerLength) {
        this(new BasicAuthURI(uri), headerLength);
    }

    public S3RangeReader(BasicAuthURI uri, int headerLength) {
        super(uri, headerLength);
        configProps = new S3ConfigurationProperties(uri.getUri().getScheme(), uri);
        client = S3ClientFactory.getS3Client(configProps);
    }

    @Override
    public byte[] fetchHeader() {
        byte[] currentHeader = data.get(0L);
        if ( currentHeader != null) {
            headerOffset = currentHeader.length;
        }
        GetObjectRequest headerRequest = buildRequest();
        try {
            ResponseBytes<GetObjectResponse> responseBytes = client.getObject(headerRequest, toBytes()).get();

            // get the header bytes
            byte[] headerBytes = responseBytes.asByteArray();
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
        GetObjectRequest headerRequest = buildRequest();
        try {
            ResponseBytes<GetObjectResponse> responseBytes = client.getObject(headerRequest, toBytes()).get();

            // get the header bytes
            byte[] headerBytes = responseBytes.asByteArray();
            data.put(0L, headerBytes);
            HEADERS_CACHE.put(uri.toString(), headerBytes);
            return headerBytes;
        } catch (Exception e) {
            LOGGER.severe("Error reading header for " + uri);
            throw new RuntimeException(e);
        }
    }

    private GetObjectRequest buildRequest() {
        return GetObjectRequest.builder()
                .bucket(configProps.getBucket())
                .key(configProps.getKey())
                .range("bytes="+ headerOffset +"-" + (headerOffset + headerLength - 1))
                .build();
    }

    @Override
    public Map<Long, byte[]> read(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Instant start = Instant.now();
        Map<Long, CompletableFuture<ResponseBytes<GetObjectResponse>>> downloads = new HashMap<>(ranges.length);

        Map<Long, byte[]> values = new HashMap<>();
        int missingRanges[] = new int[ranges.length];
        int missing = 0;
        for (int i = 0; i < ranges.length; i++) {
            byte[] dataRange = data.get(ranges[i]);
            // Check for available data.
            if (dataRange == null) {
                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(configProps.getBucket())
                        .key(configProps.getKey())
                        .range("bytes=" + ranges[i][0] + "-" + ranges[i][1])
                        .build();
                CompletableFuture<ResponseBytes<GetObjectResponse>> futureGet =
                        client.getObject(request, AsyncResponseTransformer.toBytes());
                downloads.put(ranges[i][0], futureGet);
                // Mark the range as missing
                missingRanges[missing++] = i;
            } else {
                values.put(ranges[i][0], dataRange);
            }
        }

        awaitCompletion(values, downloads);
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
     * @param downloads
     */
    protected void awaitCompletion(Map<Long, byte[]> data, Map<Long, CompletableFuture<ResponseBytes<GetObjectResponse>>> downloads) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(downloads.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (Map.Entry<Long, CompletableFuture<ResponseBytes<GetObjectResponse>>> entry : downloads.entrySet()) {
                long key = entry.getKey();
                CompletableFuture<ResponseBytes<GetObjectResponse>> future = entry.getValue();
                if (future.isDone()) {
                    if (!completed.contains(key)) {
                        try {
                            data.put(key, future.get().asByteArray());
                            completed.add(key);
                        } catch (Exception e) {
                            LOGGER.warning("Unable to write data from S3 to the destination ByteBuffer. "
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
