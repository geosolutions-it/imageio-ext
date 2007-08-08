/*
* JImageIO-extension - OpenSource Java Image translation Library
* http://www.geo-solutions.it/
* (C) 2007, GeoSolutions
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation;
* version 2.1 of the License.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.gdalframework.visualizationutilities.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ArcGridJaiReadTest extends AbstractArcGridTestCase {
	public ArcGridJaiReadTest(String name) {
		super(name);
	}

	/**
	 * Simple test read
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testRead() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final String fileName = "arcGrid.asc";
		final File file = TestData.file(this, fileName);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualize(image);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of a simple image
		suite.addTest(new ArcGridJaiReadTest("testRead"));

		return suite;
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
