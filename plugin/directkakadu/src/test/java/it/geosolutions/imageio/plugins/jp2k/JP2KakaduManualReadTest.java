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

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JP2KakaduManualReadTest extends TestCase {

	private static Logger LOGGER = Logger
			.getLogger(JP2KakaduManualReadTest.class.toString());

	public JP2KakaduManualReadTest(String name) {
		super(name);
	}

	/**
	 * Test Read without exploiting JAI-ImageIO Tools
	 * 
	 * @throws IOException
	 */
	public void testManualReadAndVisualize() throws IOException {

		final File file = new File("D:\\DatiPlugin\\jp2\\pana-field.jp2");
		JP2KakaduImageReader reader = new JP2KakaduImageReader(
				new JP2KakaduImageReaderSpi());

		reader.setInput(file);
		ImageReadParam param = new ImageReadParam();
		param.setSourceRegion(new Rectangle(4368, 0, 1092, 1024));
		// Viewer.visualize(reader.read(0, param));

		param.setSourceRegion(new Rectangle(19656, 0, 484 + 1092, 1752));
		// Viewer.visualize(reader.read(0, param));
	}

	/**
	 * Test manual read
	 * 
	 * @throws IOException
	 */
	public void testManualReadFullScale() throws IOException {

		final File file = new File("c:\\20070228160443004764.jp2");
		JP2KakaduImageReader reader = new JP2KakaduImageReader(
				new JP2KakaduImageReaderSpi());

		reader.setInput(file);
		ImageReadParam param = new ImageReadParam();
		param.setSourceSubsampling(1, 1, 0, 0);

		final long start = System.currentTimeMillis();
		reader.read(0, param);
		final long end = System.currentTimeMillis();
		final long executionTime = end - start;
		LOGGER.info(new StringBuffer("Full Scale Read Time: ").append(
				Long.toString(executionTime)).toString());
	}

	/**
	 * Test manual read
	 * 
	 * @throws IOException
	 */
	public void testManualReadReducedScale() throws IOException {

		final File file = TestData.file(this,"CB_TM432.jp2");
		JP2KakaduImageReader reader = new JP2KakaduImageReader(
				new JP2KakaduImageReaderSpi());

		reader.setInput(file);
		ImageReadParam param = new ImageReadParam();
		param.setSourceSubsampling(1, 1, 0, 0);
		final long start = System.currentTimeMillis();
		RenderedImage image = reader.read(0, param);
		final long end = System.currentTimeMillis();
		final long executionTime = end - start;
		LOGGER.info(new StringBuffer("Reduced Scale Read Time: ").append(
				Long.toString(executionTime)).toString());
		final JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(image, 1600, 1200));
		jf.pack();
		jf.setVisible(true);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

//		suite
//				.addTest(new JP2KakaduManualReadTest(
//						"testManualReadAndVisualize"));
//
//		suite.addTest(new JP2KakaduManualReadTest("testManualReadFullScale"));

		suite
				.addTest(new JP2KakaduManualReadTest(
						"testManualReadReducedScale"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}