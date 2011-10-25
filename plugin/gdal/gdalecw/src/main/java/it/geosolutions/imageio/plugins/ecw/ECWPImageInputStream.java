/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.stream.input.URIImageInputStream;
import it.geosolutions.imageio.stream.input.URIImageInputStreamImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;

/**
 * A simple class which allow to handle ECWP protocol on GDAL.
 * Actually, this shouldn't be used as a real ImageInputStream.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class ECWPImageInputStream implements URIImageInputStream {

    private final static String ECWP_PREFIX = "ecwp://";

    private URIImageInputStreamImpl uriInputStream;

    public ECWPImageInputStream(String ecwpUrl) {
        // Can improve checks
        if (ecwpUrl == null) {
            throw new NullPointerException("Specified argument is null");
        } else if (!ecwpUrl.startsWith(ECWP_PREFIX)) {
            throw new IllegalArgumentException("Specified ECWP is not valid");
        }
        URI uri;
        try {
            uri = new URI(ecwpUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Unable to create a proper stream for the provided input");
        }
        uriInputStream = new URIImageInputStreamImpl(uri);
    }

    public ECWPImageInputStream(URI uri) {
        uriInputStream = new URIImageInputStreamImpl(uri);
    }

    public URI getUri() {
        return uriInputStream.getUri();
    }

    public String getECWPLink() {
        final URI uri = getUri();
        String ecwp = null;
        if (uri != null)
            ecwp = uri.toString();
        return ecwp;
    }

    public void close() throws IOException {
        uriInputStream.close();
    }

    public void flush() throws IOException {
        uriInputStream.flush();
    }

    public void flushBefore(long pos) throws IOException {
        uriInputStream.flushBefore(pos);
    }

    public int getBitOffset() throws IOException {
        return uriInputStream.getBitOffset();
    }

    public ByteOrder getByteOrder() {
        return uriInputStream.getByteOrder();
    }

    public long getFlushedPosition() {
        return uriInputStream.getFlushedPosition();
    }

    public long getStreamPosition() throws IOException {
        return uriInputStream.getStreamPosition();
    }

    public boolean isCached() {
        return uriInputStream.isCached();
    }

    public boolean isCachedFile() {
        return uriInputStream.isCachedFile();
    }

    public boolean isCachedMemory() {
        return uriInputStream.isCachedMemory();
    }

    public long length() throws IOException {
        return uriInputStream.length();
    }

    public void mark() {
        uriInputStream.mark();
    }

    public int read() throws IOException {
        return uriInputStream.read();
    }

    public int read(byte[] b) throws IOException {
        return uriInputStream.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return uriInputStream.read(b, off, len);
    }

    public int readBit() throws IOException {
        return uriInputStream.readBit();
    }

    public long readBits(int numBits) throws IOException {
        return uriInputStream.readBits(numBits);
    }

    public boolean readBoolean() throws IOException {
        return uriInputStream.readBoolean();
    }

    public byte readByte() throws IOException {
        return uriInputStream.readByte();
    }

    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        // uriInputStream.readBytes(buf, len);
    }

    public char readChar() throws IOException {
        return uriInputStream.readChar();
    }

    public double readDouble() throws IOException {
        return uriInputStream.readDouble();
    }

    public float readFloat() throws IOException {
        return uriInputStream.readFloat();
    }

    public void readFully(byte[] b) throws IOException {
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
    }

    public void readFully(short[] s, int off, int len) throws IOException {
    }

    public void readFully(char[] c, int off, int len) throws IOException {
    }

    public void readFully(int[] i, int off, int len) throws IOException {
    }

    public void readFully(long[] l, int off, int len) throws IOException {
    }

    public void readFully(float[] f, int off, int len) throws IOException {
    }

    public void readFully(double[] d, int off, int len) throws IOException {
    }

    public int readInt() throws IOException {
        return uriInputStream.readInt();
    }

    public String readLine() throws IOException {
        return uriInputStream.readLine();
    }

    public long readLong() throws IOException {
        return uriInputStream.readLong();
    }

    public short readShort() throws IOException {
        return uriInputStream.readShort();
    }

    public String readUTF() throws IOException {
        return uriInputStream.readUTF();
    }

    public int readUnsignedByte() throws IOException {
        return uriInputStream.readUnsignedByte();
    }

    public long readUnsignedInt() throws IOException {
        return uriInputStream.readUnsignedInt();
    }

    public int readUnsignedShort() throws IOException {
        return uriInputStream.readUnsignedShort();
    }

    public void reset() throws IOException {
    }

    public void seek(long pos) throws IOException {
    }

    public void setBitOffset(int bitOffset) throws IOException {
    }

    public void setByteOrder(ByteOrder byteOrder) {
    }

    public int skipBytes(int n) throws IOException {
        return uriInputStream.skipBytes(n);
    }

    public long skipBytes(long n) throws IOException {
        return uriInputStream.skipBytes(n);
    }

    public URI getTarget() {
        return uriInputStream.getTarget();
    }

    public Class<URI> getBinding() {
        return URI.class;
    }
}
