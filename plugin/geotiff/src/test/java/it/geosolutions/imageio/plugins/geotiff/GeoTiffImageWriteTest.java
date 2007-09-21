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
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class GeoTiffImageWriteTest extends AbstractGeoTiffTestCase {

	public GeoTiffImageWriteTest(String name) {
		super(name);
	}

	/**
	 * Test Writing capabilities.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testWrite() throws IOException, FileNotFoundException {

		final File outputFile = TestData.temp(this, "writetest.tif",false);
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

		suite.addTest(new GeoTiffImageWriteTest("testWrite"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
