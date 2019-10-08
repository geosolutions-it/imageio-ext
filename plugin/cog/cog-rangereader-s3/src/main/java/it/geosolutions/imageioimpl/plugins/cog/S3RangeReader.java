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

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static software.amazon.awssdk.core.async.AsyncResponseTransformer.toBytes;

/**
 * Reads URIs from S3 with the format
 * https://s3-<region>.amazonaws.com/<bucket>/<key>
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
public class S3RangeReader extends RangeReader {

    protected S3AsyncClient client;
    protected S3ConfigurationProperties configProps;

    private final static Logger LOGGER = Logger.getLogger(S3RangeReader.class.getName());

    public S3RangeReader(String url) {
        this(URI.create(url));
    }

    public S3RangeReader(URL url) {
        this(URI.create(url.toString()));
    }

    public S3RangeReader(URI uri) {
        super(uri);

        configProps = new S3ConfigurationProperties(uri.getScheme(), uri);

        client = S3ClientFactory.getS3Client(configProps);
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(configProps.getBucket())
                .key(configProps.getKey())
                .build();
        try {
            HeadObjectResponse headResponse = client.headObject(headObjectRequest).get();
            filesize = headResponse.contentLength().intValue();
            buffer = ByteBuffer.allocate(filesize);
        } catch (Exception e) {
            LOGGER.severe("Error reading file " + uri);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readHeader() {
        GetObjectRequest headerRequest = GetObjectRequest.builder()
                .bucket(configProps.getBucket())
                .key(configProps.getKey())

                .range("bytes=0-" + (headerLength - 1))
                .build();
        try {
            ResponseBytes<GetObjectResponse> responseBytes = client.getObject(headerRequest, toBytes()).get();
            byte[] bytes = responseBytes.asByteArray();
            buffer.put(bytes, 0, headerLength);
            return bytes;
        } catch (Exception e) {
            LOGGER.severe("Error reading header for " + uri);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readAsync(Collection<long[]> ranges) {
        readAsync(ranges.toArray(new long[][]{}));
    }


    @Override
    public void readAsync(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Instant start = Instant.now();
        Map<Long, CompletableFuture<ResponseBytes<GetObjectResponse>>> downloads = new HashMap<>(ranges.length);

        for (int i = 0; i < ranges.length; i++) {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(configProps.getBucket())
                    .key(configProps.getKey())
                    .range("bytes=" + ranges[i][0] + "-" + ranges[i][1])
                    .build();
            CompletableFuture<ResponseBytes<GetObjectResponse>> futureGet =
                    client.getObject(request, AsyncResponseTransformer.toBytes());
            downloads.put(ranges[i][0], futureGet);
        }

        awaitCompletion(downloads);
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
     * @param downloads
     */
    protected void awaitCompletion(Map<Long, CompletableFuture<ResponseBytes<GetObjectResponse>>> downloads) {
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
                            writeValue((int)key, future.get().asByteArray());
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
