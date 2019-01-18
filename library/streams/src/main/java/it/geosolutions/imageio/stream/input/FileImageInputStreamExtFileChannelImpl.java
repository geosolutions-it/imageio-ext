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

package it.geosolutions.imageio.stream.input;

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

/**
 * An implementation of {@link ImageInputStream} that gets its input from a
 * {@link File} dealing with FileChannels. 
 * 
 * @author Simone Giannecchini, GeoSolutions SaS
 * @author Daniele Romagnoli, GeoSolutions SaS
 */
public class FileImageInputStreamExtFileChannelImpl extends ImageInputStreamImpl
        implements FileImageInputStreamExt {

    /** the associated {@link File}*/
    private File file;
    
    /** the underlying {@link FileChannelImageInputStream} */
    private FileChannelImageInputStream fileChannelInputStream;

    public byte readByte() throws IOException {
        return fileChannelInputStream.readByte();
    }

    public char readChar() throws IOException {
        return fileChannelInputStream.readChar();
    }

    public double readDouble() throws IOException {
        return fileChannelInputStream.readDouble();
    }

    public float readFloat() throws IOException {
        return fileChannelInputStream.readFloat();
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        fileChannelInputStream.readFully(b, off, len);
    }

    public void readFully(byte[] b) throws IOException {
        fileChannelInputStream.readFully(b);
    }

    public int readInt() throws IOException {
        return fileChannelInputStream.readInt();
    }

    public String readLine() throws IOException {
        return fileChannelInputStream.readLine();
    }

    public ByteOrder getByteOrder() {
        return fileChannelInputStream.getByteOrder();
    }

    public long getStreamPosition() throws IOException {
        return fileChannelInputStream.getStreamPosition();
    }

    public int read(byte[] b) throws IOException {
        return fileChannelInputStream.read(b);
    }

    public long skipBytes(long n) throws IOException {
        return fileChannelInputStream.skipBytes(n);
    }

    public long readLong() throws IOException {
        return fileChannelInputStream.readLong();
    }

    public short readShort() throws IOException {
        return fileChannelInputStream.readShort();
    }

    public int readUnsignedByte() throws IOException {
        return fileChannelInputStream.readUnsignedByte();
    }

    public long readUnsignedInt() throws IOException {
        return fileChannelInputStream.readUnsignedInt();
    }

    public int readUnsignedShort() throws IOException {
        return fileChannelInputStream.readUnsignedShort();
    }

    public String readUTF() throws IOException {
        return fileChannelInputStream.readUTF();
    }

    public void setByteOrder(ByteOrder byteOrder) {
        fileChannelInputStream.setByteOrder(byteOrder);
    }

    public int skipBytes(int n) throws IOException {
        return fileChannelInputStream.skipBytes(n);
    }

    /**
     * Constructs a {@link FileImageInputStreamExtFileChannelImpl} that will read from a
     * given {@link File}.
     * 
     * <p>
     * The eraf contents must not change between the time this object is
     * constructed and the time of the last call to a read method.
     * 
     * @param f
     *                a {@link File} to read from.
     * 
     * @exception NullPointerException
     *                    if <code>f</code> is <code>null</code>.
     * @exception SecurityException
     *                    if a security manager exists and does not allow read
     *                    access to the eraf.
     * @exception FileNotFoundException
     *                    if <code>f</code> is a directory or cannot be opened
     *                    for reading for any other reason.
     * @exception IOException
     *                    if an I/O error occurs.
     */
    public FileImageInputStreamExtFileChannelImpl(File f) throws FileNotFoundException,
            IOException {
        this(f, -1);
    }

    /**
     * Constructs a {@link FileImageInputStreamExtFileChannelImpl} that will read from a
     * given {@link File}.
     * 
     * <p>
     * The eraf contents must not change between the time this object is
     * constructed and the time of the last call to a read method.
     * 
     * @param f
     *                a {@link File} to read from.
     * @param bufferSize
     *                size of the underlying buffer.
     * 
     * @exception NullPointerException
     *                    if <code>f</code> is <code>null</code>.
     * @exception SecurityException
     *                    if a security manager exists and does not allow read
     *                    access to the eraf.
     * @exception FileNotFoundException
     *                    if <code>f</code> is a directory or cannot be opened
     *                    for reading for any other reason.
     * @exception IOException
     *                    if an I/O error occurs.
     */
    public FileImageInputStreamExtFileChannelImpl(File f, int bufferSize)
            throws IOException {
        // //
        //
        // Check that the input file is a valid file
        //
        // //
        if (f == null) {
            throw new NullPointerException("f == null!");
        }
        this.file = f;
        // The underlying class already report potential IO issues with an IOException
        // permissionDenied / notExists / isADirectory
        FileChannel fc = new FileInputStream(f).getChannel();
        this.fileChannelInputStream = new FileChannelImageInputStream(fc);
        // NOTE: this must be done accordingly to what ImageInputStreamImpl
        // does, otherwise some ImageReader subclasses might not work.
        this.fileChannelInputStream.setByteOrder(ByteOrder.BIG_ENDIAN);

    }

    /**
     * Reads an int from the underlying {@link EnhancedRandomAccessFile}.
     */
    public int read() throws IOException {
        return fileChannelInputStream.read();
    }

    /**
     * Read up to <code>len</code> bytes into an array, at a specified offset.
     * This will block until at least one byte has been read.
     * 
     * @param b
     *                the byte array to receive the bytes.
     * @param off
     *                the offset in the array where copying will start.
     * @param len
     *                the number of bytes to copy.
     * @return the actual number of bytes read, or -1 if there is not more data
     *         due to the end of the eraf being reached.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return fileChannelInputStream.read(b, off, len);
    }

    /**
     * Returns the length of the underlying eraf, or <code>-1</code> if it is
     * unknown.
     * 
     * @return the eraf length as a <code>long</code>, or <code>-1</code>.
     */
    public long length() {
        return fileChannelInputStream.length();
    }

    /**
     * Seeks the current position to pos.
     */
    public void seek(long pos) throws IOException {
        fileChannelInputStream.seek(pos);
    }

    /**
     * Closes the underlying {@link EnhancedRandomAccessFile}.
     * 
     * @throws IOException
     *                 in case something bad happens.
     */
    public void close() throws IOException {
    	fileChannelInputStream.close();
    }

    /**
     * Retrieves the {@link File} we are connected to.
     */
    public File getFile() {
        return file;
    }

    /**
     * Disposes this {@link FileImageInputStreamExtFileChannelImpl} by closing its
     * underlying {@link EnhancedRandomAccessFile}.
     * 
     */
    public void dispose() {
        try {
            close();
        } catch (IOException e) {

        }
    }

    /**
     * Provides a simple description for this {@link ImageInputStream}.
     * 
     * @return a simple description for this {@link ImageInputStream}.
     */
    public String toString() {
        return "FileImageInputStreamExtFileChannelImpl which points to " + this.file.toString();
    }

    public File getTarget() {
        return file;
    }

    public Class<File> getBinding() {
        return File.class;
    }
}
