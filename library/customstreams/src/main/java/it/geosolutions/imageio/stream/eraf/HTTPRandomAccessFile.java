// $Id$
/*
 * Copyright 1997-2006 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * HTTPRandomAccessFile.java.
 * @author John Caron, based on work by Donald Denbo
 */

package it.geosolutions.imageio.stream.eraf;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;

/**
 * Gives access to files over HTTP, using jakarta commons HttpClient library.
 * This version uses a single instance of HttpClient, following performance
 * guidelines at http://jakarta.apache.org/commons/httpclient/performance.html
 * Plus other improvements.
 * 
 * @author John Caron
 * @deprecated
 */

public class HTTPRandomAccessFile extends EnhancedRandomAccessFile {
	static public int defaultHTTPBufferSize = 20000;

	static public final HttpClient _client;
	static {
		_client = new HttpClient(new MultiThreadedHttpConnectionManager());
	}

	private String url;

	private long total_length = 0;

	public HTTPRandomAccessFile(String url) throws IOException {
		this(url, defaultHTTPBufferSize);
	}

	public HTTPRandomAccessFile(String url, int bufferSize) throws IOException {
		super(bufferSize);
		eraf = null;
		this.url = url;
		HttpMethod method = null;
		try {
			method = new HeadMethod(url);
			method.setFollowRedirects(true);
			doConnect(method);

			Header head = method.getResponseHeader("Accept-Ranges");
			if (head == null || !head.getValue().equalsIgnoreCase("bytes")) {
				throw new IOException("Server does not support byte Ranges");
			}

			head = method.getResponseHeader("Content-Length");
			if (head == null) {
				throw new IOException("Server does not support Content-Length");
			}

			try {
				total_length = Integer.parseInt(head.getValue());
			} catch (NumberFormatException e) {
				throw new IOException(
						"Server has malformed Content-Length header");
			}

		} finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	private void doConnect(HttpMethod method) throws IOException {

		// Execute the method.
		int statusCode = _client.executeMethod(method);

		if (statusCode == 404)
			throw new FileNotFoundException(url + " " + method.getStatusLine());

		if (statusCode >= 300)
			throw new IOException(url + " " + method.getStatusLine());

	}
//
//	private void printHeaders(String title, Header[] heads) {
//		System.out.println(title);
//		for (int i = 0; i < heads.length; i++) {
//			Header head = heads[i];
//			System.out.print("  " + head.toString());
//		}
//		System.out.println();
//	}

	protected int read_(long pos, byte[] buff, int offset, int len)
			throws IOException {
		long end = pos + len - 1;
		if (end >= total_length)
			end = total_length - 1;

		HttpMethod method = null;
		try {
			method = new GetMethod(url);
			method.setFollowRedirects(true);
			method.setRequestHeader("Range", "bytes=" + pos + "-" + end);
			doConnect(method);

			final int code = method.getStatusCode();
			if (code != 206)
				throw new IOException(
						"Server does not support Range requests, code= " + code);

			final String s = method.getResponseHeader("Content-Length").getValue();
			int readLen = Integer.parseInt(s);
			readLen = len< readLen?len:readLen;

			final InputStream is = method.getResponseBodyAsStream();
			readLen = copy(is, buff, offset, readLen);
			return readLen;

		} finally {
			if (method != null)
				method.releaseConnection();
		}
	}

	private int copy(InputStream in, byte[] buff, int offset, int want)
			throws IOException {
		int done = 0;
		while (want > 0) {
			int bytesRead = in.read(buff, offset + done, want);
			if (bytesRead == -1)
				break;
			done += bytesRead;
			want -= bytesRead;
		}
		return done;
	}

	public long length() throws IOException {
		long fileLength = total_length;
		if (fileLength < dataEnd)
			return dataEnd;
		else
			return fileLength;
	}

	/**
	 * override the rest of the RandomAccessFile public methods
	 */
	public void close() {

	}

	public FileDescriptor getFD() {
		return null;
	}

}
