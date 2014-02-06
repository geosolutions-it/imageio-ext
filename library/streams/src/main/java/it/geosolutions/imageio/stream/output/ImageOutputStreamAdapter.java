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
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * @author Simone Giannecchini, GeoSolutions
 */
public class ImageOutputStreamAdapter extends ImageOutputStreamImpl implements AccessibleStream<OutputStream>{

    // Supporting marking is a big issue. I should overline this somehow 
    
    @Override
    public void flushBefore(long arg0) throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public long getFlushedPosition() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public long getStreamPosition() throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public boolean isCachedFile() {
        return false;
    }

    @Override
    public boolean isCachedMemory() {
        return false;
    }

    @Override
    public void mark() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public void seek(long arg0) throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public int skipBytes(int arg0) throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public long skipBytes(long arg0) throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }
    
    private OutputStream os;

    public ImageOutputStreamAdapter(OutputStream os) {
        this.os = os;
    }

    /**
     * @see javax.imageio.stream.ImageOutputStreamImpl#write(int)
     */
    public void write(int b) throws IOException {
        os.write(b);
    }

    /**
     * @see javax.imageio.stream.ImageOutputStreamImpl#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    /**
     * @see javax.imageio.stream.ImageInputStreamImpl#read()
     */
    public int read() throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * @see javax.imageio.stream.ImageInputStreamImpl#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * @see javax.imageio.stream.ImageInputStreamImpl#flush()
     */
    public void flush() throws IOException {
        os.flush();
    }

    /**
     * @see javax.imageio.stream.ImageInputStreamImpl#close()
     */
    public void close() throws IOException {
    	try{
    		super.close();
    	}finally{
    		os.close();
    	}
    }

    public OutputStream getTarget() {
        return os;
    }

    public Class<OutputStream> getBinding() {
        return OutputStream.class;
    }
}
