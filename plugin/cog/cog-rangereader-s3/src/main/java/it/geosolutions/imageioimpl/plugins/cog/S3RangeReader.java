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
 * https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/aws-sdk-java-dg-v2.pdf
 *
 * @author joshfix
 * Created on 2019-08-21
 */
public class S3RangeReader implements RangeReader {

    protected URI uri;
    protected ByteBuffer buffer;
    protected S3AsyncClient client;
    protected S3ConfigurationProperties configProps;

    protected int timeout = 5;
    protected long filesize = -1;
    protected int headerByteLength = 16384;

    private final static Logger LOGGER = Logger.getLogger(S3RangeReader.class.getName());

    public S3RangeReader(String url) {
        this(URI.create(url));
    }

    public S3RangeReader(URL url) {
        this(URI.create(url.toString()));
    }

    public S3RangeReader(URI uri) {
        this.uri = uri;
        configProps = new S3ConfigurationProperties(uri.getScheme(), uri);

        client = S3ClientFactory.getS3Client(configProps);
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(configProps.getBucket())
                .key(configProps.getKey())
                .build();
        try {
            HeadObjectResponse headResponse = client.headObject(headObjectRequest).get();
            filesize = headResponse.contentLength();
            buffer = ByteBuffer.allocate((int) filesize);
        } catch (Exception e) {
            LOGGER.severe("Error reading file " + uri);
            throw new RuntimeException(e);
        }
    }

    public void setHeaderByteLength(int headerByteLength) {
        this.headerByteLength = headerByteLength;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
        buffer = ByteBuffer.allocate(filesize);
    }

    @Override
    public int getFilesize() {
        return (int) filesize;
    }

    @Override
    public byte[] getBytes() {
        return buffer.array();
    }

    @Override
    public byte[] readHeader(int headerByteLength) {
        GetObjectRequest headerRequest = GetObjectRequest.builder()
                .bucket(configProps.getBucket())
                .key(configProps.getKey())

                .range("bytes=0-" + (headerByteLength - 1))
                .build();
        try {
            ResponseBytes<GetObjectResponse> responseBytes = client.getObject(headerRequest, toBytes()).get();
            byte[] bytes = responseBytes.asByteArray();
            buffer.put(bytes, 0, headerByteLength);
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
            CompletableFuture<ResponseBytes<GetObjectResponse>> futureGet = client.getObject(request, AsyncResponseTransformer.toBytes());
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

    /**
     * Prevents making new range requests for image data that overlap with the header range that has already been read
     *
     * @param ranges
     * @return
     */
    protected long[][] reconcileRanges(long[][] ranges) {
        boolean modified = false;
        List<long[]> newRanges = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            if (ranges[i][0] < headerByteLength) {
                // this range starts inside of what we already read for the header
                modified = true;
                if (ranges[i][1] < headerByteLength) {
                    // this range is fully inside the header which was already read; discard this range
                    LOGGER.fine("Removed range " + ranges[i][0] + "-" + ranges[i][1] + " as it lies fully within"
                            + " the data already read in the header request");
                } else {
                    // this range starts inside the header range, but ends outside of it.
                    // add a new range that starts at the end of the header range
                    long[] newRange = new long[]{headerByteLength + 1, ranges[i][1]};

                    if (newRange[0] >= newRange[1]) {
                        // TODO this can happen because of the way the header is added to CogTileInfo -- needs to be fixed
                        continue;
                    }
                    newRanges.add(newRange);
                    LOGGER.fine("Modified range " + ranges[i][0] + "-" + ranges[i][1]
                            + " to " + (headerByteLength + 1) + "-" + ranges[i][1] + " as it overlaps with data previously"
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


}
