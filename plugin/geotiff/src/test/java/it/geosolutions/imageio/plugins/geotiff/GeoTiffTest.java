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

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

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
	 * Test Writing capabilities.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testWrite() throws IOException, FileNotFoundException {

		final File outputFile = TestData.temp(this, "writetest.tif", false);
		outputFile.deleteOnExit();
		final File inputFile = TestData.file(this, "bogota.tif");

		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		pbjImageRead.setParameter("Input", inputFile);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image);

		// ////////////////////////////////////////////////////////////////
		// preparing to write
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
				"ImageWrite");
		pbjImageWrite.setParameter("Output", outputFile);
		pbjImageWrite.addSource(image);
		final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);

		// ////////////////////////////////////////////////////////////////
		// preparing to read again
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI(
				"ImageRead");
		pbjImageReRead.setParameter("Input", outputFile);
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
