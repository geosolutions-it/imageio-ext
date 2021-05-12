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
package it.geosolutions.io.output.adapter;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;

/**
 * @author Simone Giannecchini, GeoSolutions
 */
public final class OutputStreamAdapter extends OutputStream {

    ImageOutputStream stream;

    public OutputStreamAdapter(ImageOutputStream stream) {
        super();
        this.stream = stream;
    }

    public void close() throws IOException {
        stream.close();
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be
     * written out. The general contract of <code>flush</code> is that calling
     * it is an indication that, if any bytes previously written have been
     * buffered by the implementation of the output stream, such bytes should
     * immediately be written to their intended destination.
     * <p>
     * The <code>flush</code> method of <code>OutputStream</code> does
     * nothing.
     * 
     * @exception IOException
     *                    if an I/O error occurs.
     */
    public void flush() throws IOException {
        stream.flush();
    }

    public void write(byte[] b) throws IOException {
        stream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }

    public void write(int b) throws IOException {
        stream.write(b);
    }

    /**
     * Return the underlying {@link ImageOutputStream}
     */
    public ImageOutputStream getWrappedStream() {
        return stream;
    }
}