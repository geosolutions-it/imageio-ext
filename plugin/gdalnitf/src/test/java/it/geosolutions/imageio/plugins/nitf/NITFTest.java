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
package it.geosolutions.imageio.plugins.nitf;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

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
 * Testing reading capabilities for {@link EnvisatImageReader}.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class NITFTest extends AbstractNITFTestCase {
	public final static String fileName = "001zc013.on1";

	public NITFTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test read exploiting common JAI operations (Crop-Translate-Rotate)
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testJaiOperations() throws FileNotFoundException, IOException {
		if (!isGDALAvailable) {
			return;
		}
		File file;
		try {
			file = TestData.file(this, fileName);
		} catch (FileNotFoundException fnfe) {
			warningMessage();
			return;
		}
		// ////////////////////////////////////////////////////////////////
		// preparing to read
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();

		// subsample by 2 on both dimensions
		final int xSubSampling = 2;
		final int ySubSampling = 2;
		final int xSubSamplingOffset = 0;
		final int ySubSamplingOffset = 0;
		irp.setSourceSubsampling(xSubSampling, ySubSampling,
				xSubSamplingOffset, ySubSamplingOffset);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);

		// get a RenderedImage
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);

		if (TestData.isInteractiveTest())
			Viewer.visualize(image, "Subsampling Read");
		else
			assertNotNull(image.getTiles());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test read exploiting common JAI operations
		suite.addTest(new NITFTest("testJaiOperations"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
