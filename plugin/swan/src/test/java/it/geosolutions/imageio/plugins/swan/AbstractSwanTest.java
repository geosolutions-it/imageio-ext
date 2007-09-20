/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.swan;

import it.geosolutions.resources.TestData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import junit.framework.TestCase;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractSwanTest extends TestCase {

	public AbstractSwanTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		super.setUp();
		final String[] dataSources = new String[] { "Mixed.zip",
				"2cQuantities.zip", "MultiQuantitySources.zip",
				"Quantities1.zip", "Quantities2.zip" };
		final int nTestData = dataSources.length;

		for (int i = 0; i < nTestData; i++) {
			try {
				File fileZip = TestData.file(this, dataSources[i]);
			} catch (FileNotFoundException fnfe) {
				try{
					getTestData(dataSources[i]);
					TestData.unzipFile(this, dataSources[i]);
				}
				catch(FileNotFoundException fnfe2){
					final IOException ioe = new IOException("Unable to download sample data");
					ioe.initCause(fnfe2);
					throw ioe;
				}
			}
		}
	}
	
	/**
	 * Getting testData from remote URL.
	 */
	private File getTestData(final String name) throws FileNotFoundException,
			IOException {
		final StringBuffer urlLocation = new StringBuffer(
				"http://danielsan.altervista.org/data/");
		urlLocation.append(name);
		final URL url = new URL(urlLocation.toString());
		final File tempFile;

		// /////////////////////////////////////////////////////////////////////
		//
		// Preliminary checks
		//
		// /////////////////////////////////////////////////////////////////////
		// input URL may rely to a local File. In this case, no caching
		// mechanism occurs and we simply returns that file.
		if (url.getProtocol().compareToIgnoreCase("file") == 0) {
			tempFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		} else {

			// Getting the folder where TestData should be put
			final File tempFileForFolder = TestData.temp(this, "temp", true);

			String tempFilePath = tempFileForFolder.getAbsolutePath();
			tempFilePath = tempFilePath.substring(0, tempFilePath
					.lastIndexOf("\\") + 1);

			// Creating tempFile in TestData folder.
			tempFile = File.createTempFile("zipped", ".zip", new File(
					tempFilePath));
			tempFile.deleteOnExit();

			String tempFileName = tempFile.getAbsolutePath();
			// //
			//
			// getting an InputStream from the connection to the
			// object referred to by the URL
			//
			// //

			URLConnection connection = url.openConnection();
			final int byteLength = connection.getContentLength();
			final int length = byteLength / 1024;
			int byteDownloaded = 0;
			final InputStream is = connection.getInputStream();

			// //
			//
			// Preparing a FileOutputStream where to write all data
			// we will read by the InputStream
			//
			// //
			final BufferedOutputStream os = new BufferedOutputStream(
					new FileOutputStream(tempFile));
			final byte b[] = new byte[65536];
			int num = 0;
			int step = 0;

			// "read from InputStream -> write to FileOutputStream"
			// operation
			System.out
					.println("Downloading File " + name + " (" + length + "KB)");
			while ((num = is.read(b)) > 0) {
				byteDownloaded += num;
				if (byteDownloaded > byteLength / 5
						&& byteDownloaded < ((byteLength * 2) / 5) && step < 1) {
					System.out.println("20%");
					step++;
				} else if (byteDownloaded > ((byteLength * 2) / 5)
						&& byteDownloaded < ((byteLength * 3) / 5) && step < 2) {
					System.out.println("40%");
					step++;
				} else if (byteDownloaded > ((byteLength * 3) / 5)
						&& byteDownloaded < ((byteLength * 4) / 5) && step < 3) {
					System.out.println("60%");
					step++;
				} else if (byteDownloaded > ((byteLength * 4) / 5) && step < 4) {
					System.out.println("80%");
					step++;
				}
				os.write(b, 0, num);
			}
			System.out.println("DONE\n");

			// closing streams and flushing the outputStream
			os.flush();
			is.close();
			os.close();
			// final String pathSeparator =
			// System.getProperty("file.separator");
			// final String targetString;
			//			
			// if (pathSeparator.equals("\\")){
			// //Windows Style
			// targetString = "target\\test-classes";
			// }
			// else{
			// // Linux Style
			// targetString = "target/test-classes";
			// }
			// if (tempFileName.contains(targetString)){
			// final int targetStringLength = targetString.length();
			// final int targetStringPos = tempFileName.indexOf(targetString);
			// final String prefix = tempFileName.substring(0,targetStringPos);
			// final String suffix =
			// tempFileName.substring(targetStringPos+targetStringLength,
			// tempFileName.length());
			// final StringBuffer sb = new StringBuffer(prefix);
			// sb.append("src").append(pathSeparator).append("test").append(pathSeparator).append("resources").append(suffix);
			// tempFileName = sb.toString();
			// }

			// Renaming the tempFile in the proper zipped file required by test
			final StringBuffer destFileName = new StringBuffer(tempFileName
					.substring(0, tempFileName.lastIndexOf("\\") + 1));
			destFileName.append(name);
			tempFile.renameTo(new File(destFileName.toString()));
		}
		// returning the tempFile
		return tempFile;
	}

}
