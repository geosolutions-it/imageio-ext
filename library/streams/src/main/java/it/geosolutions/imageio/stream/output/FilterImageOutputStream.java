/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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
package it.geosolutions.imageio.stream.output;

import it.geosolutions.imageio.stream.AccessibleStream;

import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * Decorator class for decorating {@link ImageOutputStream} subclasses.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class FilterImageOutputStream extends ImageOutputStreamImpl implements
        ImageOutputStream, AccessibleStream {

    protected ImageOutputStream ios;

    public FilterImageOutputStream(ImageOutputStream ios) {
        this.ios = ios;
    }

    public void writeDouble(double v) throws IOException {
        ios.writeDouble(v);
    }

    public void writeFloat(float v) throws IOException {
        ios.writeFloat(v);
    }

    public void write(int b) throws IOException {
        ios.write(b);
    }

    public void writeBit(int bit) throws IOException {
        ios.writeBit(bit);
    }

    public void writeByte(int v) throws IOException {
        ios.writeByte(v);
    }

    public void writeChar(int v) throws IOException {
        ios.writeChar(v);
    }

    public void writeInt(int v) throws IOException {
        ios.writeInt(v);
    }

    public void writeShort(int v) throws IOException {
        ios.writeShort(v);
    }

    public void flushBefore(long pos) throws IOException {
        ios.flushBefore(pos);
    }

    public void writeLong(long v) throws IOException {
        ios.writeLong(v);
    }

    public void writeBits(long bits, int numBits) throws IOException {
        ios.writeBits(bits, numBits);
    }

    public void writeBoolean(boolean v) throws IOException {
        ios.writeBoolean(v);
    }

    public void write(byte[] b) throws IOException {
        ios.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ios.write(b, off, len);
    }

    public void writeChars(char[] c, int off, int len) throws IOException {
        ios.writeChars(c, off, len);
    }

    public void writeDoubles(double[] d, int off, int len) throws IOException {
        ios.writeDoubles(d, off, len);
    }

    public void writeFloats(float[] f, int off, int len) throws IOException {
        ios.writeFloats(f, off, len);
    }

    public void writeInts(int[] i, int off, int len) throws IOException {
        ios.writeInts(i, off, len);
    }

    public void writeLongs(long[] l, int off, int len) throws IOException {
        ios.writeLongs(l, off, len);
    }

    public void writeShorts(short[] s, int off, int len) throws IOException {
        ios.writeShorts(s, off, len);
    }

    public void writeBytes(String s) throws IOException {
        ios.writeBytes(s);
    }

    public void writeChars(String s) throws IOException {
        ios.writeChars(s);
    }

    public void writeUTF(String s) throws IOException {
        ios.writeUTF(s);
    }

    public byte readByte() throws IOException {
        return ios.readByte();
    }

    public char readChar() throws IOException {
        return ios.readChar();
    }

    public double readDouble() throws IOException {
        return ios.readDouble();
    }

    public float readFloat() throws IOException {
        return ios.readFloat();
    }

    public int getBitOffset() throws IOException {
        return ios.getBitOffset();
    }

    public int read() throws IOException {
        return ios.read();
    }

    public int readBit() throws IOException {
        return ios.readBit();
    }

    public int readInt() throws IOException {
        return ios.readInt();
    }

    public int readUnsignedByte() throws IOException {
        return ios.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
        return ios.readUnsignedShort();
    }

    public long getFlushedPosition() {
        return ios.getFlushedPosition();
    }

    public long getStreamPosition() throws IOException {
        return ios.getStreamPosition();
    }

    public long readLong() throws IOException {
        return ios.readLong();
    }

    public long readUnsignedInt() throws IOException {
        return ios.readUnsignedInt();
    }

    public short readShort() throws IOException {
        return ios.readShort();
    }

    public void close() throws IOException {
        ios.close();
    }

    public void flush() throws IOException {
        ios.flush();
    }

    public void mark() {
        ios.mark();
    }

    public void reset() throws IOException {
        ios.reset();
    }

    public boolean isCached() {
        return ios.isCached();
    }

    public boolean isCachedFile() {
        return ios.isCachedFile();
    }

    public boolean isCachedMemory() {
        return ios.isCachedMemory();
    }

    public boolean readBoolean() throws IOException {
        return ios.readBoolean();
    }

    public int skipBytes(int n) throws IOException {
        return ios.skipBytes(n);
    }

    public long readBits(int numBits) throws IOException {
        return ios.readBits(numBits);
    }

    public void setBitOffset(int bitOffset) throws IOException {
        ios.setBitOffset(bitOffset);
    }

    public long skipBytes(long n) throws IOException {
        return ios.skipBytes(n);
    }

    public void seek(long pos) throws IOException {
        ios.seek(pos);

    }

    public int read(byte[] b) throws IOException {
        return ios.read(b);
    }

    public void readFully(byte[] b) throws IOException {
        ios.readFully(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return ios.read(b, off, len);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        ios.readFully(b, off, len);
    }

    public void readFully(char[] c, int off, int len) throws IOException {
        ios.readFully(c, off, len);
    }

    public void readFully(double[] d, int off, int len) throws IOException {
        ios.readFully(d, off, len);
    }

    public void readFully(float[] f, int off, int len) throws IOException {
        ios.readFully(f, off, len);
    }

    public void readFully(int[] i, int off, int len) throws IOException {
        ios.readFully(i, off, len);
    }

    public void readFully(long[] l, int off, int len) throws IOException {
        ios.readFully(l, off, len);
    }

    public void readFully(short[] s, int off, int len) throws IOException {
        ios.readFully(s, off, len);
    }

    public String readLine() throws IOException {
        return ios.readLine();
    }

    public String readUTF() throws IOException {
        return ios.readUTF();
    }

    public ByteOrder getByteOrder() {
        return ios.getByteOrder();
    }

    public void setByteOrder(ByteOrder byteOrder) {
        ios.setByteOrder(byteOrder);
    }

    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        ios.readBytes(buf, len);
    }

    /**
     * Allows us to access the underlying ImageOutputStream.
     * 
     * @return the underlying {@link ImageOutputStream}.
     */
    public ImageOutputStream getTarget() {
        return ios;
    }

    public Class<ImageOutputStream> getBinding() {
        return ImageOutputStream.class;
    }

}
