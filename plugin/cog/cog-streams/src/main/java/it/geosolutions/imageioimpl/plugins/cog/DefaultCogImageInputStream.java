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

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.Set;
import java.util.logging.Logger;

import static it.geosolutions.imageioimpl.plugins.cog.CogTileInfo.HEADER_TILE_INDEX;

/**
 * ImageInputStream implementation for COG.  This class will asynchronously request all requested ranges be read by
 * the provided RangeReader implementation and cache the results in a delegate MemoryCacheImageInputStream.  When
 * TIFFImageReader requests tiles, all of the data will be already prefetched and ready to be used.
 *
 * @author joshfix
 * Created on 2019-08-23
 */
public class DefaultCogImageInputStream implements ImageInputStream, CogImageInputStream {

    protected int initialHeaderReadLength = 16384;
    private boolean initialized = false;

    protected URI uri;
    protected CogTileInfo cogTileInfo;
    protected RangeReader rangeReader;
    protected MemoryCacheImageInputStream delegate;

    private final static Logger LOGGER = Logger.getLogger(DefaultCogImageInputStream.class.getName());

    public DefaultCogImageInputStream(String url) {
        this(URI.create(url));
    }

    public DefaultCogImageInputStream(URL url) {
        this(URI.create(url.toString()));
    }

    public DefaultCogImageInputStream(URI uri) {
        this.uri = uri;
    }

    public DefaultCogImageInputStream(CogUri cogUri) {
        this.uri = cogUri.getUri();
    }

    public DefaultCogImageInputStream(URI uri, RangeReader rangeReader) {
        this.uri = uri;
        this.rangeReader = rangeReader;
        initialHeaderReadLength = rangeReader.getHeaderLength();
        cogTileInfo = new CogTileInfo(initialHeaderReadLength);
        initializeHeader();
    }

    public void init(RangeReader rangeReader) {
        this.rangeReader = rangeReader;
        initialHeaderReadLength = rangeReader.getHeaderLength();
        cogTileInfo = new CogTileInfo(initialHeaderReadLength);
        initializeHeader();
    }

    public void init(CogImageReadParam param) {
        Class<? extends RangeReader> rangeReaderClass = ((CogImageReadParam) param).getRangeReaderClass();
        if (null != rangeReaderClass) {
            try {
                rangeReader = rangeReaderClass.getDeclaredConstructor(URI.class).newInstance(uri);
                rangeReader.setHeaderLength(initialHeaderReadLength);
                cogTileInfo = new CogTileInfo(initialHeaderReadLength);
            } catch (Exception e) {
                LOGGER.severe("Unable to instantiate range reader class " + rangeReaderClass.getCanonicalName());
                throw new RuntimeException(e);
            }
        }

        if (rangeReader == null) {
            return;
        }

        initializeHeader();
    }

    protected void initializeHeader() {
        rangeReader.readHeader();
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
    public void setInitialHeaderReadLength(int initialHeaderReadLength) {
        this.initialHeaderReadLength = initialHeaderReadLength;
    }

    @Override
    public void readRanges() {
        // read data with the RangeReader and set the byte order and pointer on the new input stream
        RangeBuilder rangeBuilder = new RangeBuilder(0, cogTileInfo.getHeaderSize() - 1);

        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            rangeBuilder.addTileRange(tileRange.getStart(), tileRange.getEnd());
        });

        // read all of the ranges asynchronously
        Set<long[]> ranges = rangeBuilder.getRanges();
        LOGGER.fine("Submitting " + ranges.size() + " range request(s)");

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