package it.geosolutions.imageioimpl.plugins.cog;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static java.net.http.HttpClient.Version.HTTP_2;

/**
 * @author joshfix
 * Created on 2019-08-21
 */
public class HttpRangeReader implements RangeReader {

    protected URI uri;
    protected HttpClient client;
    protected ByteBuffer buffer;

    protected int timeout = 5;
    protected int filesize = -1;
    protected int headerByteLength = 16384;

    public static final String CONTENT_RANGE_HEADER = "content-range";
    private final static Logger LOGGER = Logger.getLogger(HttpRangeReader.class.getName());

    public HttpRangeReader(String url) {
        this(URI.create(url));
    }

    public HttpRangeReader(URL url) {
        this(URI.create(url.toString()));
    }

    public HttpRangeReader(URI uri) {
        this.uri = uri;
        client = HttpClient.newBuilder()
                .version(HTTP_2)
                .connectTimeout(Duration.ofSeconds(timeout))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public byte[] readHeader(int headerByteLength) {
        LOGGER.fine("Reading header");
        this.headerByteLength = headerByteLength;
        byte[] headerBytes = read(0, headerByteLength - 1);
        writeValue(0, headerBytes);
        return headerBytes;
    }

    public void setHeaderByteLength(int headerByteLength) {
        this.headerByteLength = headerByteLength;
    }

    @Override
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
    public void readAsync(Collection<long[]> ranges) {
        readAsync(ranges.toArray(new long[][]{}));
    }

    @Override
    public void readAsync(long[]... ranges) {
        ranges = reconcileRanges(ranges);

        Instant start = Instant.now();
        Map<Long, CompletableFuture<byte[]>> futureResults = new HashMap<>(ranges.length);

        for (int i = 0; i < ranges.length; i++) {
            HttpRequest request = buildRequest(ranges[i]);
            futureResults.put(ranges[i][0], getAsync(request));
        }

        awaitCompletion(futureResults);
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
     * @param futureResults
     */
    protected void awaitCompletion(Map<Long, CompletableFuture<byte[]>> futureResults) {
        boolean stillWaiting = true;
        List<Long> completed = new ArrayList<>(futureResults.size());
        while (stillWaiting) {
            boolean allDone = true;
            for (Map.Entry<Long, CompletableFuture<byte[]>> entry : futureResults.entrySet()) {
                long key = entry.getKey();
                CompletableFuture<byte[]> value = entry.getValue();
                if (value.isDone()) {
                    if (!completed.contains(key)) {
                        try {
                            writeValue((int) key, value.get());
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

    protected HttpRequest buildRequest(long[] range) {
        LOGGER.fine("Building request for range " + range[0] + '-' + range[1] + " to " + uri.toString());
        return HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Accept", "*/*")
                .header("range", "bytes=" + range[0] + "-" + range[1])
                .build();
    }

    protected CompletableFuture<byte[]> getAsync(HttpRequest request) {
        return client
                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body);
    }

    protected byte[] read(long start, long end) {
        byte[] bytes = get(buildRequest(new long[]{start, end}));
        writeValue((int) start, bytes);
        return bytes;
    }

    /**
     * Blocking request used to read the header
     * @param request
     * @return
     */
    protected byte[] get(HttpRequest request) {
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            // if the filesize variable has not been initialized, read it from the response
            if (filesize == -1) {
                HttpHeaders headers = response.headers();
                String contentRange = headers.firstValue(CONTENT_RANGE_HEADER).get();
                if (contentRange.contains("/")) {
                    String length = contentRange.split("/")[1];
                    try {
                        filesize = Integer.parseInt(length);
                        buffer = ByteBuffer.allocate(filesize);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
