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

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.resources.TestData;
import it.geosolutions.util.FileCache;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageReadParam;
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
public class JP2KakaduReadTest extends AbstractJP2KakaduTestCase {
	static FileCache fileCache = new FileCache();

	public JP2KakaduReadTest(String name) {
		super(name);

	}

	/**
	 * Test Read exploiting Linear Interpolation
	 * 
	 * @throws IOException
	 */
	public void testJaiReadFromUrl() throws IOException {
		final URL url = new URL(
				"http://www.microimages.com/gallery/jp2/CB_TM432.jp2");
		final File file = fileCache.getFile(url);
		final ParameterBlockJAI pbjImageRead;
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (TestData.isInteractiveTest())
			visualize(image,
					"http://www.microimages.com/gallery/jp2/CB_TM432.jp2");
		else
			assertNotNull(image.getTiles());
	}

	public void testJaiReadFromFile() throws IOException {

		final File file = TestData.file(this, "CB_TM432.jp2");
		ImageReadDescriptorMT.register(JAI.getDefaultInstance());

		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageReadMT");
		ImageLayout l = new ImageLayout();
		l.setTileHeight(512);
		l.setTileWidth(512);


		ImageReadParam rp = new JP2KakaduImageReadParam();
		rp.setSourceSubsampling(1, 1, 0, 0);
		pbjImageRead.setParameter("ReadParam", rp);
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageReadMT", pbjImageRead,
				new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
		if (TestData.isInteractiveTest())
			visualize(image, 800, 600);
		else
			assertNotNull(image.getTiles());

	}

	/**
	 * Test Read without exploiting JAI-ImageIO Tools
	 * 
	 * @throws IOException
	 */
	public void testManualRead() throws IOException {

		final File file = TestData.file(this, "CB_TM432.jp2");
		JP2KakaduImageReader reader = new JP2KakaduImageReader(
				new JP2KakaduImageReaderSpi());

		reader.setInput(file);
		ImageReadParam param = new ImageReadParam();

		RenderedImage image = reader.read(0, param);
		if (TestData.isInteractiveTest())
			visualize(image, "testManualRead");
		else
			assertNotNull(image.getData());
		assertEquals(361, image.getWidth());
		assertEquals(488, image.getHeight());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new JP2KakaduReadTest("testJaiReadFromFile"));

		suite.addTest(new JP2KakaduReadTest("testManualRead"));
		//
		// suite.addTest(new JP2KakaduReadTest("testJaiReadFromUrl"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
