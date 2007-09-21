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

import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JP2KakaduManualReadTest extends AbstractJP2KakaduTestCase {

	public JP2KakaduManualReadTest(String name) {
		super(name);
	}

	/**
	 * Test Read without exploiting JAI-ImageIO Tools
	 * 
	 * @throws IOException
	 */
	public void testManualRead() throws IOException {

		final File file = TestData.file(this,"CB_TM432.jp2");
		JP2KakaduImageReader reader = new JP2KakaduImageReader(
				new JP2KakaduImageReaderSpi());

		reader.setInput(file);
		ImageReadParam param = new ImageReadParam();
		
		RenderedImage image=reader.read(0, param);
		if (TestData.isInteractiveTest())
			visualize(image);
		else
			assertNotNull(image.getData());
		assertEquals(361, image.getWidth());
		assertEquals(488, image.getHeight());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new JP2KakaduManualReadTest("testManualRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}