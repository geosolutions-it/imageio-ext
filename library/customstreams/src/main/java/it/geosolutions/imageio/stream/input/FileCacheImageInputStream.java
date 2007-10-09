/*
 * @(#)FileCacheImageInputStream.java	1.29 05/08/17
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package it.geosolutions.imageio.stream.input;

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStreamImpl;

import com.sun.imageio.stream.StreamCloser;

/**
 * An implementation of <code>ImageInputStream</code> that gets its input from
 * a regular <code>InputStream</code>. A file is used to cache previously
 * read data.
 * 
 * @version 0.5
 */
public class FileCacheImageInputStream extends ImageInputStreamImpl implements
		FileImageInputStreamExt {

	private InputStream stream;

	private File cacheFile;

	private EnhancedRandomAccessFile cache;

	private static final int BUFFER_LENGTH = 1024;

	private byte[] buf = new byte[BUFFER_LENGTH];

	private long length = 0L;

	private boolean foundEOF = false;

	/**
	 * Constructs a <code>FileCacheImageInputStream</code> that will read from
	 * a given <code>InputStream</code>.
	 * 
	 * <p>
	 * A temporary file is used as a cache. If <code>cacheDir</code>is non-<code>null</code>
	 * and is a directory, the file will be created there. If it is
	 * <code>null</code>, the system-dependent default temporary-file
	 * directory will be used (see the documentation for
	 * <code>File.createTempFile</code> for details).
	 * 
	 * @param stream
	 *            an <code>InputStream</code> to read from.
	 * @param cacheDir
	 *            a <code>File</code> indicating where the cache file should
	 *            be created, or <code>null</code> to use the system
	 *            directory.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>stream</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>cacheDir</code> is non-<code>null</code> but
	 *                is not a directory.
	 * @exception IOException
	 *                if a cache file cannot be created.
	 */
	public FileCacheImageInputStream(InputStream stream, File cacheDir)
			throws IOException {
		this(stream, cacheDir, EnhancedRandomAccessFile.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Constructs a <code>FileCacheImageInputStream</code> that will read from
	 * a given <code>InputStream</code>.
	 * 
	 * <p>
	 * A temporary file is used as a cache. If <code>cacheDir</code>is non-<code>null</code>
	 * and is a directory, the file will be created there. If it is
	 * <code>null</code>, the system-dependent default temporary-file
	 * directory will be used (see the documentation for
	 * <code>File.createTempFile</code> for details).
	 * 
	 * @param stream
	 *            an <code>InputStream</code> to read from.
	 * @param cacheDir
	 *            a <code>File</code> indicating where the cache file should
	 *            be created, or <code>null</code> to use the system
	 *            directory.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>stream</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>cacheDir</code> is non-<code>null</code> but
	 *                is not a directory.
	 * @exception IOException
	 *                if a cache file cannot be created.
	 */
	public FileCacheImageInputStream(InputStream stream, File cacheDir,
			final int readBufferLength) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("stream == null!");
		}
		if ((cacheDir != null) && !(cacheDir.isDirectory())) {
			throw new IllegalArgumentException("Not a directory!");
		}
		this.stream = stream;
		this.cacheFile = File.createTempFile("imageio", ".tmp", cacheDir);
		this.cache = new EnhancedRandomAccessFile(cacheFile, "rw",
				readBufferLength);
		StreamCloser.addToQueue(this);
	}

	/**
	 * Ensures that at least <code>pos</code> bytes are cached, or the end of
	 * the source is reached. The return value is equal to the smaller of
	 * <code>pos</code> and the length of the source file.
	 */
	private long readUntil(long pos) throws IOException {
		// We've already got enough data cached
		if (pos < length) {
			return pos;
		}
		// pos >= length but length isn't getting any bigger, so return it
		if (foundEOF) {
			return length;
		}

		long len = pos - length;
		cache.seek(length);
		while (len > 0) {
			// Copy a buffer's worth of data from the source to the cache
			// BUFFER_LENGTH will always fit into an int so this is safe
			int nbytes = stream.read(buf, 0, (int) Math.min(len,
					(long) BUFFER_LENGTH));
			if (nbytes == -1) {
				foundEOF = true;
				return length;
			}

			cache.write(buf, 0, nbytes);
			len -= nbytes;
			length += nbytes;
		}

		return pos;
	}

	public int read() throws IOException {
		bitOffset = 0;
		long next = streamPos + 1;
		long pos = readUntil(next);
		if (pos >= next) {
			cache.seek(streamPos++);
			return cache.read();
		} else {
			return -1;
		}
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		}
		// Fix 4430357 - if off + len < 0, overflow occurred
		if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return 0;
		}

		checkClosed();

		bitOffset = 0;
		long pos = readUntil(streamPos + len);

		// len will always fit into an int so this is safe
		len = (int) Math.min((long) len, pos - streamPos);
		if (len > 0) {
			cache.seek(streamPos);
			cache.readFully(b, off, len);
			streamPos += len;
			return len;
		} else {
			return -1;
		}
	}

	/**
	 * Returns <code>true</code> since this <code>ImageInputStream</code>
	 * caches data in order to allow seeking backwards.
	 * 
	 * @return <code>true</code>.
	 * 
	 * @see #isCachedMemory
	 * @see #isCachedFile
	 */
	public boolean isCached() {
		return true;
	}

	/**
	 * Returns <code>true</code> since this <code>ImageInputStream</code>
	 * maintains a file cache.
	 * 
	 * @return <code>true</code>.
	 * 
	 * @see #isCached
	 * @see #isCachedMemory
	 */
	public boolean isCachedFile() {
		return true;
	}

	/**
	 * Returns <code>false</code> since this <code>ImageInputStream</code>
	 * does not maintain a main memory cache.
	 * 
	 * @return <code>false</code>.
	 * 
	 * @see #isCached
	 * @see #isCachedFile
	 */
	public boolean isCachedMemory() {
		return false;
	}

	/**
	 * Closes this <code>FileCacheImageInputStream</code>, closing and
	 * removing the cache file. The source <code>InputStream</code> is not
	 * closed.
	 * 
	 * @exception IOException
	 *                if an error occurs.
	 */
	public void close() throws IOException {
		super.close();
		cache.close();
		cacheFile.delete();
		stream = null;
		StreamCloser.removeFromQueue(this);
	}

	public File getFile() {
		return cacheFile;
	}
}
