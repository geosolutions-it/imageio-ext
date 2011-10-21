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
import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import com.sun.media.imageio.stream.FileChannelImageOutputStream;

/**
 * Interfaces for for {@link ImageOutputStream} subclasses that exclusively
 * points to a {@link File} object.
 * 
 * <p>
 * Such an {@link ImageOutputStream} internally uses an
 * {@link EnhancedRandomAccessFile} which basically is a
 * {@link RandomAccessFile} with buffering.
 * 
 * <p>
 * Overall performance is improved with respect to simple
 * {@link FileImageOutputStream}. Some simplicistic tests showed that
 * performances are close to the performances of the
 * {@link FileChannelImageOutputStream} but without the burden of using
 * {@link FileChannel} which on some platform with some older versions of Java
 * can be problematic.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 * @deprecated use {@link AccessibleStream} instead
 */
public interface FileImageOutputStreamExt extends ImageOutputStream, AccessibleStream<File> {

    /**
     * Returns the associated {@link File}
     * 
     * @return the associated {@link File}
     */
    public File getFile();
}
