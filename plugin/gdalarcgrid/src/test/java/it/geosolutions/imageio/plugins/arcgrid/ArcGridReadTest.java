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
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link ArcGridImageReader} leveraging on
 * JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 * 
 */
public class ArcGridReadTest extends AbstractArcGridTestCase {
	public ArcGridReadTest(String name) {
		super(name);
	}

	/**
	 * Simple test read through JAI - ImageIO
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testReadJAI() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final String fileName = "arcGrid.asc";
		final File file = TestData.file(this, fileName);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, fileName);
		else
			image.getTiles();
		assertEquals(120, image.getWidth());
		assertEquals(85, image.getHeight());
	}

	/**
	 * Simple test read through ImageIO
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testReadImageIO() throws FileNotFoundException, IOException {
		final File file = TestData.file(this, "arcGrid.asc");

		// //
		//
		// Try to get a reader for this raster data
		//
		// //
		final Iterator it = ImageIO.getImageReaders(file);
		assertTrue(it.hasNext());

		// //
		//
		// read some data from it using subsampling
		//
		// //
		final ImageReader reader = (ImageReader) it.next();
		assertTrue(reader instanceof ArcGridImageReader);
		ImageReadParam rp = reader.getDefaultReadParam();
		rp.setSourceSubsampling(2, 2, 0, 0);
		reader.setInput(file);
		RenderedImage image = reader.read(0, rp);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, "subsample read " + file.getName());
		reader.reset();

		assertEquals((int) (reader.getWidth(0) / 2.0 + 0.5), image.getWidth());
		assertEquals((int) (reader.getHeight(0) / 2.0 + 0.5), image.getHeight());

		// //
		//
		// read some data from it using sourceregion
		//
		// //
		assertTrue(reader instanceof ArcGridImageReader);
		rp = reader.getDefaultReadParam();
		rp.setSourceRegion(new Rectangle(0, 0, 60, 42));
		reader.setInput(file);
		image = reader.read(0, rp);
		if (TestData.isInteractiveTest())
			Viewer.visualize(image, "subsample read " + file.getName());
		reader.reset();

		assertEquals(60, image.getWidth());
		assertEquals(42, image.getHeight());

		reader.dispose();
	}

	public static Test suite() {
		final TestSuite suite = new TestSuite();

		// Test reading of a simple image
		suite.addTest(new ArcGridReadTest("testReadJAI"));

		// Test reading of a simple image
		suite.addTest(new ArcGridReadTest("testReadImageIO"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
