/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.imageioimpl.imagereadmt.CloneableImageReadParam;
import it.geosolutions.resources.TestData;
import it.geosolutions.util.FileCache;

import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link JP2KakaduImageReader} leveraging on
 * JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 * 
 */
public class JP2KakaduJAIReadTest extends AbstractJP2KakaduTestCase {
	static FileCache fileCache = new FileCache();

	public JP2KakaduJAIReadTest(String name) {
		super(name);

	}

	// /**
	// * Test Read exploiting Linear Interpolation
	// *
	// * @throws IOException
	// */
	//
	// public void testJaiReadFromUrl() throws IOException {
	// final URL url = new URL(
	// "http://gatso.test.ambrero.nl/violation_images/bla.jp2");
	// final File file = fileCache.getFile(url);
	// final ParameterBlockJAI pbjImageRead;
	// pbjImageRead = new ParameterBlockJAI("ImageRead");
	// pbjImageRead.setParameter("Input", file);
	// RenderedOp image = JAI.create("ImageRead", pbjImageRead);
	// if (TestData.isInteractiveTest())
	// visualize(image);
	// else
	// assertNotNull(image.getTiles());
	// }

	public void testJaiReadFromFile() throws IOException {

		// double sum = 0;
		// for (int i = 0; i < 10; i++) {
		// long init=System.nanoTime();
		final File file = TestData.file(this, "CB_TM432.jp2");
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageReadMT");
		ImageLayout l = new ImageLayout();
		l.setTileHeight(512);
		l.setTileWidth(512);
		try {
			pbjImageRead.setParameter("Reader", new JP2KakaduImageReaderSpi()
					.createReaderInstance());
		} catch (IOException e) {
			// XXX

		}
		CloneableImageReadParam rp = new JP2KakaduImageReadParam();
		rp.setSourceSubsampling(4, 4, 0, 0);
		pbjImageRead.setParameter("ReadParam", rp);
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageReadMT", pbjImageRead,
				new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
		// image.getTiles();
		// sum+=(System.nanoTime()-init);
		// }
		// System.out.println(sum/10E9);
		if (TestData.isInteractiveTest())
			visualize(image, 1600, 1200);
		else
			assertNotNull(image.getData());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new JP2KakaduJAIReadTest("testJaiReadFromFile"));

		// suite.addTest(new JP2KakaduJAIReadTest("testJaiReadFromUrl"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
