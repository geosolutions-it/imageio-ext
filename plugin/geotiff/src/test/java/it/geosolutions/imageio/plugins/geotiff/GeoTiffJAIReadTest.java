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

import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Testing reading capabilities for {@link GeoTiffImageReader} leveraging on JAI.
 *
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class GeoTiffJAIReadTest extends AbstractGeoTiffTestCase {

	public GeoTiffJAIReadTest(String name) {
		super(name);
	}

	/**
	 * Test Read exploiting JAI-ImageIO tools capabilities
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public void testRead() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		// final ImageReadParam irp = new ImageReadParam();
		String fileName = "bogota.tif";
		final File file = TestData.file(this, fileName);

		// irp.setSourceSubsampling(5, 5, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input",
				new FileImageInputStreamExtImpl(file));
		// pbjImageRead.setParameter("readParam", irp);
		// pbjImageRead.setParameter("Reader", new
		// TIFFImageReaderSpi().createReaderInstance());
		pbjImageRead.setParameter("Reader", new GeoTiffImageReaderSpi()
				.createReaderInstance());

//		final ImageLayout layout= new ImageLayout();
//		layout.setTileHeight(162);
//		layout.setTileWidth(120);
		final RenderingHints hints = new RenderingHints(JAI.KEY_TILE_CACHE,
				null);
//		hints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT,layout));
		RenderedOp image = JAI.create("ImageRead", pbjImageRead, hints);
		if (TestData.isInteractiveTest())
			Viewer.visualizeAllInformation(image, "", true);
		else
			assertNotNull(image.getTiles());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test Read exploiting JAI-ImageIO tools capabilities
		suite.addTest(new GeoTiffJAIReadTest("testRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
