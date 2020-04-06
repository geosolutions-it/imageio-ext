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
package it.geosolutions.imageio.stream;

/**
 * Testing custom ImageInputStream and ImageOutputStream.
 *
 * @author Simone Giannecchini, GeoSolutions
 */
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.stream.input.spi.StringImageInputStreamSpi;
import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;
import it.geosolutions.resources.TestData;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import org.junit.Test;

import junit.framework.Assert;

public class ImageInputStreamTest  {

        private final String fileName = "a.txt";
        private final String directoryName = "test-data";
        private final static Logger LOGGER = Logger.getLogger(ImageInputStreamTest.class.toString());



	/**
	 * Testing {@link
	 */
    @Test
	public void imageInputStreamAdapter() {
		// try {
		// final BufferedInputStream stream = new BufferedInputStream(
		// new FileInputStream(TestData.file(this, "sample.jpeg")));
		// final ImageInputStreamAdapter adapter = new ImageInputStreamAdapter(
		// stream);
		// final RenderedOp image= JAI.create("ImageRead", adapter);
		// visualize(image,"testURLImageInputStreamSpi");
		// } catch (IOException e) {
		// LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		// }

	}

	/**
	 * @param image
	 * @param string
	 * @throws HeadlessException
	 */
	private void visualize(final RenderedOp image, String string)
			throws HeadlessException {
		final JFrame f = new JFrame();
		f.setTitle(string);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new ScrollingImagePanel(image, 800, 800));
		f.pack();
		f.setVisible(true);
	}

	@Test
	public void inflaterInputStream() throws FileNotFoundException,
			IOException {

		// // open up test eraf
		// final BufferedInputStream inStream = new BufferedInputStream(
		// new FileInputStream(TestData.file(this, "jam.txt")));
		// // deflate it
		// final ByteArrayOutputStream testOutStream = new
		// ByteArrayOutputStream();
		// final DeflaterOutputStream deflaterStream = new DeflaterOutputStream(
		// testOutStream);
		// ByteArrayOutputStream inflatedOutStream = new
		// ByteArrayOutputStream();
		// int read = 0;
		// final byte[] buf = new byte[8192];
		// while ((read = inStream.read(buf)) > 0) {
		// deflaterStream.write(buf, 0, read);
		// inflatedOutStream.write(buf, 0, read);
		// }
		// inStream.close();
		// deflaterStream.close();
		//
		// // inflate it back
		// // inflated buffer
		// final byte[] compressedBuffer = testOutStream.toByteArray();
		// final byte[] originalBuffer = inflatedOutStream.toByteArray();
		// final InflaterImageInputStream inflateIIS = new
		// InflaterImageInputStream(
		// new MemoryCacheImageInputStream(new ByteArrayInputStream(
		// compressedBuffer)));
		// inflatedOutStream = new ByteArrayOutputStream();
		// while ((read = inflateIIS.read(buf)) > 0) {
		// inflatedOutStream.write(buf, 0, read);
		// }
		// inflateIIS.close();
		// inflatedOutStream.close();
		// final byte[] inflatedBuffer = inflatedOutStream.toByteArray();
		//
		// // check them
		// assertTrue(inflatedBuffer.length == originalBuffer.length);
		// for (int i = 0; i < inflatedBuffer.length; i++) {
		// assertTrue(inflatedBuffer[i] == originalBuffer[i]);
		// System.out.print((char) inflatedBuffer[i]);
		// }

	}

	// /**
	// * @throws IOException
	// * @throws FileNotFoundException
	// */
	// public void testGZIPImageInputStream() throws FileNotFoundException,
	// IOException {
	//
	// // decompress the gzipped eraf to a stringbuffer
	// final GZIPImageInputStream gzipIIS = new GZIPImageInputStream(
	// new FileImageInputStreamExtImpl(TestData.file(this,
	// "jam.txt.gz")));
	//
	// StringBuffer buf = new StringBuffer();
	// String s;
	//
	// while ((s = gzipIIS.readLine()) != null) {
	// buf.append(s).append("\n");
	// }
	// gzipIIS.close();
	// if (TestData.isInteractiveTest())
	// LOGGER.info("\n\n\nReading gzipped\n\n\n" + buf.toString() + "\n\n\n");
	//
	// //compare them
	// assertTrue(buf2==buf);
	// // decompress the gzipped eraf to a stringbuffer
	// final GZIPImageInputStream gzipIIS = new GZIPImageInputStream(
	// new FileImageInputStreamExtImpl(TestData.file(this,
	// "jam.txt.gz")));
	//
	// StringBuffer buf = new StringBuffer();
	// String s;
	// int b;
	// while ((b = gzipIIS.read())>0) {
	// buf.append((char)b);
	// }
	// gzipIIS.close();
	// LOGGER.info(buf.toString());
	//
	// get the original unzipped eraf
	// final FileImageInputStreamExtImpl fileIIS = new
	// FileImageInputStreamExtImpl(
	// TestData.file(this, "jam.txt"));
	//
	// StringBuffer buf2 = new StringBuffer();
	// String s2 = "";
	// while ((s2 = fileIIS.readLine()) != null) {
	// buf2.append(s2).append("\n");
	// }
	// fileIIS.close();
	// if (TestData.isInteractiveTest())
	// LOGGER.info("\n\n\nReading gzipped\n\n\n" + buf.toString() + "\n\n\n");
	//
	// // compare them
	// // assertTrue(buf2.toString() == buf.toString());
	//
	//
	//
	// }

	/**
	 * Testing capabilities of {@link URLImageInputStreamSpi}.
	 *
	 */
	@Test
	public void URLImageInputStream() {

		LOGGER.info("Testing capabilities of URLImageInputStreamSpi");
		// get a URL pointing to a FILE
		final URL inURLToFile = TestData.getResource(this, "a.txt");
		// get an ImageInputStream
		ImageInputStream instream;
		try {
			instream = ImageIO.createImageInputStream(inURLToFile);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			instream = null;
		}
		Assert.assertNotNull(
				"Unable to get an URLImageInputStreamSpi from a URL pointing to a File",
				instream);

		// get a URL pointing to an http page
		try {
			final URL httpURL = new URL("http://www.corriere.it/");
			instream = ImageIO.createImageInputStream(httpURL);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			instream = null;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			instream = null;
		}
		Assert.assertNotNull(
				"Unable to get an URLImageInputStreamSpi from a URL pointing to an http page",
				instream);

		try {
			final URL url = TestData.url(this, "sample.jpeg");
			final ImageInputStream stream = ImageIO.createImageInputStream(url);
			final RenderedOp image = JAI.create("ImageRead", stream);
			if (TestData.isInteractiveTest())
				visualize(image, "testURLImageInputStreamSpi");
			else
				image.getAsBufferedImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		LOGGER
				.info("Testing capabilities of URLImageInputStreamSpi: SUCCESS!!!");

	}

	/**
	 * Testing capabilities of {@link FileImageInputStreamExtImpl}.
	 *
	 */
	@Test
	public void fileImageInputStreamExtImpl() {

		LOGGER.info("Testing capabilities of FileImageInputStreamExt");
		try {
			final File url = TestData.file(this, "sample.jpeg");
			final ImageInputStream stream = ImageIO.createImageInputStream(url);
			final RenderedOp image = JAI.create("ImageRead", stream);
			if (TestData.isInteractiveTest())
				visualize(image, "testFileImageInputStreamExtImpl");
			else
				image.getAsBufferedImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		LOGGER
				.info("Testing capabilities of URLImageInputStreamSpi: SUCCESS!!!");

	}

    /**
     * Testing capabilities of {@link StringImageInputStreamSpi}.
     *
     */
	@Test
    public void stringImageInputStream() {

        LOGGER.info("Testing capabilities of StringImageInputStreamSpi");
        // get a URL pointing to a FILE
        final String inURLToFile = TestData.getResource(this, "a.txt")
                .toString();
        // get an ImageInputStream
        ImageInputStream instream;
        try {
            instream = ImageIO.createImageInputStream(inURLToFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            instream = null;
        }
        Assert.assertNotNull(
                "Unable to get an StringImageInputStreamSpi from a URL pointing to a File",
                instream);

        // get a URL pointing to an http page
        try {
            final String httpURL = new URL("http://www.corriere.it/")
                    .toString();
            instream = ImageIO.createImageInputStream(httpURL);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            instream = null;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            instream = null;
        }
        Assert.assertNotNull(
                "Unable to get an URLImageInputStreamSpi from a URL pointing to an http page",
                instream);

        try {
            final String url = TestData.url(this, "sample.jpeg").toString();
            final ImageInputStream stream = ImageIO.createImageInputStream(url);
            final RenderedOp image = JAI.create("ImageRead", stream);
            if (TestData.isInteractiveTest())
                visualize(image, "testURLImageInputStreamSpi");
            else
                image.getAsBufferedImage();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        LOGGER
                .info("Testing capabilities of URLImageInputStreamSpi: SUCCESS!!!");

    }

    /**
     *  Test if <code>NullPointerException</code> is thrown when null file is passed.
     */
	@Test
    public void imageInputStreamExtInvalidContructor() {

        try {
            new FileImageInputStreamExtImpl(null);
            Assert.fail("NullPointerException must be thrown.");
        } catch (NullPointerException e) {
            // OK
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName() + " should not be thrown");
        }
    }

    /**
     *  Test if <code>FileNotFoundException</code> is thrown when a non-existing file is passed.
     */
	@Test
    public void imageInputStreamExtInvalidContructor2() {

        try {
            File file = new File("this/file/is/invalid");
            new FileImageInputStreamExtImpl(file);
            Assert.fail("FileNotFoundException must be thrown.");
        } catch (FileNotFoundException e) {
            // OK
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName() + " should not be thrown");
        }
    }

    /**
     *  Test if <code>FileNotFoundException</code> is thrown when a directory is passed.
     */
	@Test
    public void imageInputStreamExtInvalidContructor3() {

        try {
            URI fileURI = getClass().getResource(directoryName).toURI();
            File file = new File(fileURI);
            new FileImageInputStreamExtImpl(file);
            Assert.fail("FileNotFoundException must be thrown.");
        } catch (FileNotFoundException e) {
            // OK
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName() + " should not be thrown");
        }
    }

    /**
     *  Test if no exception is thrown when valid arguments are used.
     */
	@Test
    public void imageInputStreamExtValidContructor() {

        try {
            URI fileURI = getClass()
                    .getResource(directoryName + "/" + fileName).toURI();
            File file = new File(fileURI);
            new FileImageInputStreamExtImpl(file);
        } catch (Exception e) {
            Assert.fail(e.getClass().getSimpleName() + " should not be thrown");
        }
    }
}
