package it.geosolutions.imageioimpl.plugins.cog;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.logging.Logger;

/**
 * @author joshfix
 * Created on 2019-08-23
 */
public class HttpCogImageInputStream implements ImageInputStream, CogImageInputStream {

    protected int headerByteLength = 16384;
    private boolean initialized = false;
    protected URI uri;
    protected CogTileInfo cogTileInfo = new CogTileInfo();
    protected RangeReader rangeReader;
    protected MemoryCacheImageInputStream delegate;
    private final static Logger LOGGER = Logger.getLogger(HttpCogImageInputStream.class.getName());

    public HttpCogImageInputStream(String url) {
        this(URI.create(url));
    }

    public HttpCogImageInputStream(URL url) {
        this(URI.create(url.toString()));
    }

    public HttpCogImageInputStream(URI uri) {
        this.uri = uri;
    }

    public HttpCogImageInputStream(URI uri, RangeReader rangeReader) {
        this.uri = uri;
        this.rangeReader = rangeReader;
        try {
            initializeHeader();
        } catch (IOException e) {
            LOGGER.severe("Unable to initialize header");
            throw new RuntimeException(e);
        }
    }

    public void init(RangeReader rangeReader) {
        this.rangeReader = rangeReader;
        try {
            initializeHeader();
        } catch (IOException e) {
            LOGGER.severe("Unable to initialize header");
            throw new RuntimeException(e);
        }
    }

    public void init(CogImageReadParam param) {
        Class<? extends RangeReader> rangeReaderClass = ((CogImageReadParam) param).getRangeReaderClass();
        if (null != rangeReaderClass) {
            try {
                rangeReader = rangeReaderClass.getDeclaredConstructor(URI.class).newInstance(uri);
            } catch (Exception e) {
                LOGGER.severe("Unable to instantiate range reader class " + rangeReaderClass.getCanonicalName());
                throw new RuntimeException(e);
            }
        }

        if (rangeReader == null) {
            return;
        }

        try {
            initializeHeader();
        } catch (IOException e) {
            LOGGER.severe("Unable to initialize header");
            throw new RuntimeException(e);
        }
    }

    protected void initializeHeader() throws IOException {
        rangeReader.readHeader(headerByteLength);
        // wrap the result in a MemoryCacheInputStream
        delegate = new MemoryCacheImageInputStream(new ByteArrayInputStream(rangeReader.getBytes()));
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public CogTileInfo getCogTileInfo() {
        return cogTileInfo;
    }

    @Override
    public void setHeaderByteLength(int headerByteLength) {
        this.headerByteLength = headerByteLength;
    }

    @Override
    public void readRanges() {
        // read data with the RangeReader and set the byte order and pointer on the new input stream
        long firstTileOffset = cogTileInfo.getFirstTileOffset();
        long firstTileByteLength = cogTileInfo.getFirstTileByteLength();
        RangeBuilder rangeBuilder = new RangeBuilder(firstTileOffset, firstTileOffset + firstTileByteLength);

        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) ->
                rangeBuilder.addTileRange(tileRange.getStart(), tileRange.getByteLength()));

        // read all of the ranges asynchronously
        long[][] ranges = rangeBuilder.getRanges().toArray(new long[][]{});
        LOGGER.fine("Submitting " + ranges.length + " range request(s)");

        rangeReader.readAsync(ranges);

        // obtain the byte order and stream position from the existing delegate
        ByteOrder byteOrder = delegate.getByteOrder();
        long streamPos = 0;
        try {
            streamPos = delegate.getStreamPosition();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create a new delegate with the newly acquired bytes
        delegate = new MemoryCacheImageInputStream(new ByteArrayInputStream(rangeReader.getBytes()));

        // set the byte order and stream position
        delegate.setByteOrder(byteOrder);
        try {
            delegate.seek(streamPos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return uri.toString();
    }

    public ImageInputStream getDelegate() {
        return delegate;
    }

    @Override
    public void setByteOrder(ByteOrder byteOrder) {
        delegate.setByteOrder(byteOrder);
    }

    @Override
    public ByteOrder getByteOrder() {
        return delegate.getByteOrder();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public long length() {
        return rangeReader.getBytes().length;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return delegate.skipBytes(n);
    }

    @Override
    public long skipBytes(long n) throws IOException {
        return delegate.skipBytes(n);
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public void mark() {
        delegate.mark();
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        delegate.flushBefore(pos);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public long getFlushedPosition() {
        return delegate.getFlushedPosition();
    }

    @Override
    public boolean isCached() {
        return delegate.isCached();
    }

    @Override
    public boolean isCachedMemory() {
        return delegate.isCachedMemory();
    }

    @Override
    public boolean isCachedFile() {
        return delegate.isCachedFile();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        delegate.readBytes(buf, len);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return delegate.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return delegate.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return delegate.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return delegate.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return delegate.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return delegate.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return delegate.readInt();
    }

    @Override
    public long readUnsignedInt() throws IOException {
        return delegate.readUnsignedInt();
    }

    @Override
    public long readLong() throws IOException {
        return delegate.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return delegate.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return delegate.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return delegate.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return delegate.readUTF();
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        delegate.readFully(b, off, len);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        delegate.readFully(b);
    }

    @Override
    public void readFully(short[] s, int off, int len) throws IOException {
        delegate.readFully(s, off, len);
    }

    @Override
    public void readFully(char[] c, int off, int len) throws IOException {
        delegate.readFully(c, off, len);
    }

    @Override
    public void readFully(int[] i, int off, int len) throws IOException {
        delegate.readFully(i, off, len);
    }

    @Override
    public void readFully(long[] l, int off, int len) throws IOException {
        delegate.readFully(l, off, len);
    }

    @Override
    public void readFully(float[] f, int off, int len) throws IOException {
        delegate.readFully(f, off, len);
    }

    @Override
    public void readFully(double[] d, int off, int len) throws IOException {
        delegate.readFully(d, off, len);
    }

    @Override
    public long getStreamPosition() throws IOException {
        return delegate.getStreamPosition();
    }

    @Override
    public int getBitOffset() throws IOException {
        return delegate.getBitOffset();
    }

    @Override
    public void setBitOffset(int bitOffset) throws IOException {
        delegate.setBitOffset(bitOffset);
    }

    @Override
    public int readBit() throws IOException {
        return delegate.readBit();
    }

    @Override
    public long readBits(int numBits) throws IOException {
        return delegate.readBits(numBits);
    }

}