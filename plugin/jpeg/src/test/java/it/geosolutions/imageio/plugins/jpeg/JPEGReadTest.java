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
package it.geosolutions.imageio.plugins.jpeg;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link JpegGDALImageReader} leveraging on JAI.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JPEGReadTest extends AbstractJPEGTestCase {
	public JPEGReadTest(String name) {
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
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "mountain.jpg";
		final File file = TestData.file(this, fileName);
		irp.setSourceSubsampling(1, 1, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		
		final ImageLayout l = new ImageLayout();
		l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256)
				.setTileWidth(256);

		RenderedOp image = JAI.create("ImageRead", pbjImageRead,
				new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
		if (TestData.isInteractiveTest())
			Viewer.visualize(image);
		else
			assertNotNull(image.getTiles());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of a simple image
		suite.addTest(new JPEGReadTest("testRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
