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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sun.media.jai.operator.ImageWriteDescriptor;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class GeoTiffTest extends AbstractGeoTiffTestCase {

	public GeoTiffTest(String name) {
		super(name);

	}

	/**
	 * Test Read without exploiting JAI-ImageIO Tools
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testManualRead() throws IOException, FileNotFoundException {
		final ImageReadParam irp = new ImageReadParam();

		// Reading a simple GrayScale image
		String fileName = "bogota.tif";
		final File inputFile = TestData.file(this, fileName);
		irp.setSourceSubsampling(2, 2, 0, 0);
		GeoTiffImageReader reader = new GeoTiffImageReader(
				new GeoTiffImageReaderSpi());
		reader.setInput(inputFile);
		final RenderedImage image = reader.readAsRenderedImage(0, irp);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, fileName);
		assertEquals(256, image.getWidth());
		assertEquals(256, image.getHeight());
		reader.dispose();
	}

	/**
	 * Test Read exploiting JAI-ImageIO tools capabilities
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public void testRead() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		String fileName = "bogota.tif";
		final File file = TestData.file(this, fileName);

		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input",
				new FileImageInputStreamExtImpl(file));
		pbjImageRead.setParameter("Reader", new GeoTiffImageReaderSpi()
				.createReaderInstance());
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (TestData.isInteractiveTest())
			Viewer.visualizeAllInformation(image, "", true);
		else
			assertNotNull(image.getTiles());
	}

	/**
	 * Test Read exploiting JAI-ImageIO tools capabilities
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public void testReadDirect() throws FileNotFoundException, IOException {
		String fileName = "bogota.tif";
		final File file = TestData.file(this, fileName);
		assertTrue(new GeoTiffImageReaderSpi().canDecodeInput(file));
		ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
		reader.setInput(file);
		final RenderedImage image = reader.read(0);
		assertNotNull(image);
		if (TestData.isInteractiveTest())
			Viewer.visualizeAllInformation(image, "", true);
	}

	/**
	 * Test Writing capabilities.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testWrite() throws IOException, FileNotFoundException {

		final File outputFile = TestData.temp(this, "writetest.tif", false);

		outputFile.deleteOnExit();
		final File inputFile = TestData.file(this, "bogota.tif");
		ImageReadParam rparam = new ImageReadParam();
		rparam.setSourceRegion(new Rectangle(1, 1, 200, 500));
		rparam.setSourceSubsampling(1, 2, 0, 0);
		final ImageLayout l = new ImageLayout();
		l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
				.setTileWidth(512);

		ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();

		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		pbjImageRead.setParameter("Input", inputFile);
		pbjImageRead.setParameter("reader", reader);
		pbjImageRead.setParameter("readParam", rparam);

		RenderedOp image = JAI.create("ImageRead", pbjImageRead,
				new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

		if (TestData.isInteractiveTest())
			Viewer.visualize(image);

		// ////////////////////////////////////////////////////////////////
		// preparing to write
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
				"ImageWrite");
		ImageWriter writer = new GeoTiffImageWriterSpi().createWriterInstance();
		pbjImageWrite.setParameter("Output", outputFile);
		pbjImageWrite.setParameter("writer", writer);
		pbjImageWrite.setParameter("ImageMetadata", reader.getImageMetadata(0));
		pbjImageWrite.setParameter("Transcode", false);
		ImageWriteParam param = new ImageWriteParam(Locale.getDefault());
		pbjImageWrite.setParameter("writeParam", param);
		param.setSourceRegion(new Rectangle(10, 10, 100, 100));
		param.setSourceSubsampling(2, 1, 0, 0);

		pbjImageWrite.addSource(image);
		final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
		final ImageWriter writer2 = (ImageWriter) op
				.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
		writer2.dispose();

		// ////////////////////////////////////////////////////////////////
		// preparing to read again
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI(
				"ImageRead");
		pbjImageReRead.setParameter("Input", outputFile);
		pbjImageReRead.setParameter("Reader", new GeoTiffImageReaderSpi()
				.createReaderInstance());
		final RenderedOp image2 = JAI.create("ImageRead", pbjImageReRead);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image2);
		else
			assertNotNull(image2.getTiles());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test Read exploiting JAI-ImageIO tools capabilities
		suite.addTest(new GeoTiffTest("testRead"));

		// Test Read without exploiting JAI-ImageIO tools capabilities
		suite.addTest(new GeoTiffTest("testManualRead"));

		// Test Write
		suite.addTest(new GeoTiffTest("testWrite"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());

	}
}
