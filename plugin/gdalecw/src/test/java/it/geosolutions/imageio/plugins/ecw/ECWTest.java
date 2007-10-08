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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.Viewer;
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
 * Testing reading capabilities for {@link ECWImageReader}.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class ECWTest extends AbstractECWTestCase {

	public ECWTest(String name) {
		super(name);
	}

	/**
	 * Test reading of a RGB image
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testImageRead() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "dq2807ne.ecw";
		final File file = TestData.file(this, fileName);

		irp.setSourceSubsampling(8, 8, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, fileName);
		else
			image.getTiles();
		assertEquals(784, image.getWidth());
		assertEquals(878, image.getHeight());
	}

	/**
	 * Test reading of a GrayScale image
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testGrayScaleImageRead() throws FileNotFoundException,
			IOException {
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "wing.ecw";
		final File file = TestData.file(this, fileName);

		irp.setSourceSubsampling(4, 4, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, fileName);
		else
			image.getTiles();
		assertEquals(1969, image.getWidth());
		assertEquals(1760, image.getHeight());
	}

	public void testManualRead() throws FileNotFoundException, IOException {
		final ECWImageReaderSpi spi = new ECWImageReaderSpi();
		final ECWImageReader mReader = new ECWImageReader(spi);
		final String fileName = "samplergb.ecw";
		final File file = TestData.file(this, fileName);
		final ImageReadParam param = new ImageReadParam();
		param.setSourceSubsampling(4, 4, 0, 0);
		final int imageIndex = 0;

		mReader.setInput(file);
		final RenderedImage image = mReader.readAsRenderedImage(imageIndex,
				param);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, fileName);
		assertEquals(688, image.getWidth());
		assertEquals(471, image.getHeight());
		mReader.dispose();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of a GrayScale image
		suite.addTest(new ECWTest("testGrayScaleImageRead"));

		// Test reading of a RGB image
		suite.addTest(new ECWTest("testImageRead"));

		// Test reading of a RGB image
		suite.addTest(new ECWTest("testManualRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
