package it.geosolutions.imageioimpl.plugins.cog;

import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.ReliableDownloadOptions;
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
 * @author joshfix
 * Created on 2019-08-21
 */
public class AzureRangeReader implements RangeReader {

    protected int timeout = 5;
    protected int filesize = -1;
    protected int headerByteLength = 16384;

    protected URI uri;

    protected String containerName;
    protected String filename;
    protected ByteBuffer buffer;
    private final AzureConnector connector;

    private BlobAsyncClient client;

    private final static Logger LOGGER = Logger.getLogger(AzureRangeReader.class.getName());

    public AzureRangeReader(String url) {
        this(URI.create(url));
    }

    public AzureRangeReader(URL url) {
        this(URI.create(url.toString()));
    }

    public static void main(String... args) {
        URI uri = URI.create("wasb://destination@imageryproducts.blob.core.windows.net/1000004_2128820_2017-12-19_100c/1000004_2128820_2017-12-19_100c-6u03f6c6-d437da84facd6e6f187b8cb1a3e85cf4-zoneclip-20171219224236000.tif");
        AzureRangeReader reader = new AzureRangeReader(uri);
        reader.readHeader(16384);
        long[] range1 = new long[]{20000, 25000};
        long[] range2 = new long[]{30000, 35000};
        reader.readAsync(range1, range2);
        byte[] bytes = reader.getBytes();
        System.out.println("bytes length: " + bytes.length);

    }

    public AzureRangeReader(URI uri) {
        this.uri = uri;

        containerName = AzureUrlParser.getContainerName(uri);
        filename = AzureUrlParser.getFilename(uri, containerName);
        connector = new AzureConnector(AzureUrlParser.getAccountName(uri));
        client = connector.getAzureClient(containerName, filename);
        filesize = (int)client.getProperties().block().blobSize();
        buffer = ByteBuffer.allocate(filesize);
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
        return filesize;
    }

    @Override
    public byte[] getBytes() {
        return buffer.array();
    }

    @Override
    public byte[] readHeader(int headerByteLength) {
        BlobRange blobRange = new BlobRange(0l, (long)headerByteLength);
        blobRange = new BlobRange(0l, (long)filesize);

        BlobAccessConditions conditions = new BlobAccessConditions();
        //conditions.

        Response<Flux<ByteBuffer>> response = client
                .downloadWithResponse(blobRange, null, null, false)
                .block();


        response.value()
                .map(bb -> {
                    //byte[] b = bb.array();
                    int length = bb.capacity();
                    buffer.put(bb);
                    System.out.println("put length " + length + " - current pos: " + buffer.position());
                    return bb;
                })
                .blockLast();

        //buffer.put(header, 0, headerByteLength);
        byte[] b = new byte[headerByteLength];
        buffer.rewind();
        buffer.get(b, 0, headerByteLength);

        return b;
        //return header;
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
                            future.get().value().map(bb -> {
                                buffer.put(bb);
                                System.out.println("b len: " + bb.capacity());
                                return bb;
                            }).blockLast();

                            //writeValue((int)key, future.get().value().blockFirst().array());
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
