package it.geosolutions.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import junit.framework.TestCase;

public class FileCacheTest extends TestCase {
	static FileCache fileCache = new FileCache();

	private static Logger LOGGER = Logger.getLogger("it.geosolutions.util");

	public FileCacheTest(String name) {
		super(name);
	}

	public void testFileCache() throws MalformedURLException, IOException {

		long start, end;
		File file;
		StringBuffer sb;

		/** getting File from the first URL */
		final URL url = new URL(
				"http://www.microimages.com/gallery/jp2/Otoe_OrthoImage8.jp2");
		LOGGER.info(new StringBuffer("Getting File from URL:").append(
				url.toString()).toString());
		for (int i = 0; i < 3; i++) {
			sb = new StringBuffer("Attempt N° ")
					.append(Integer.toString(i + 1)).append(
							": Elapsed Time is ");

			start = System.nanoTime();
			file = fileCache.getFile(url);
			end = System.nanoTime() - start;
			LOGGER.info(sb.append(Long.toString(end)).append(" nanoseconds")
					.toString());
		}

		/** getting File from the second URL */
		final URL url2 = new URL(
				"http://www.microimages.com/gallery/jp2/CB_TM432.jp2");
		LOGGER.info(new StringBuffer("Getting File from URL:").append(
				url2.toString()).toString());
		for (int i = 0; i < 3; i++) {
			sb = new StringBuffer("Attempt N° ")
					.append(Integer.toString(i + 1)).append(
							": Elapsed Time is ");
			start = System.nanoTime();
			file = fileCache.getFile(url2);
			end = System.nanoTime() - start;
			LOGGER.info(sb.append(Long.toString(end)).append(" nanoseconds")
					.toString());
		}

		/** getting again File from the first URL */
		LOGGER.info(new StringBuffer("Getting again File from URL:").append(
				url.toString()).toString());
		start = System.nanoTime();
		file = fileCache.getFile(url);
		end = System.nanoTime() - start;
		LOGGER.info(new StringBuffer("Elapsed Time is ").append(
				Long.toString(end)).append(" nanoseconds").toString());

		/** getting again File from the second URL */
		LOGGER.info(new StringBuffer("Getting again File from URL:").append(
				url2.toString()).toString());
		start = System.nanoTime();
		file = fileCache.getFile(url2);
		end = System.nanoTime() - start;
		LOGGER.info(new StringBuffer("Elapsed Time is ").append(
				Long.toString(end)).append(" nanoseconds").toString());
		LOGGER.info("now: Cleaning Cache");
		fileCache.clear();

	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(FileCacheTest.class);
	}
}
