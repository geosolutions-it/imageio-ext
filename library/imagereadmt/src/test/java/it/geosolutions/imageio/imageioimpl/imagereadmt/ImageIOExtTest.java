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
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Simone Giannecchini, GeoSolutions.
 */
public class ImageIOExtTest extends TestCase {

	/**
	 * @param name
	 */
	public ImageIOExtTest(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testImageReadMT() {
		ImageReadDescriptorMT.register(JAI.getDefaultInstance());
		final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageReadMT");
		assertNotNull(pbj);
	}

	public void testImageReadMTOperation() throws IOException {
		// final String opName = "ImageReadMT";
		// JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
		// JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
		// ImageReaderSpi spi = new GeoTiffImageReaderSpi();
		// ImageReader reader = spi.createReaderInstance();
		// final File file = new File(new String("C:/bogota.tif"));
		//
		// // //
		// //
		// // Setting Image Read Parameters
		// //
		// // //
		// final ImageReadParam param = new DefaultCloneableImageReadParam();
		// BufferedImage bi = new BufferedImage(512, 512,
		// BufferedImage.TYPE_BYTE_GRAY);
		// param.setDestination(bi);
		//
		// // //
		// //
		// // Preparing the ImageRead operation
		// //
		// // //
		// ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(opName);
		// pbjImageRead
		// .setParameter("Input", ImageIO.createImageInputStream(file));
		// pbjImageRead.setParameter("readParam", param);
		// pbjImageRead.setParameter("reader", reader);
		//
		// // //
		// //
		// // Setting a Layout
		// //
		// // //
		// final ImageLayout l = new ImageLayout();
		// l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256)
		// .setTileWidth(256);
		//
		// // //
		// //
		// // ImageReadMT operation
		// //
		// // //
		// RenderedOp image = JAI.create(opName, pbjImageRead, new
		// RenderingHints(
		// JAI.KEY_IMAGE_LAYOUT, l));
		// image.getTiles();
		// final JFrame jf = new JFrame();
		// jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// jf.getContentPane().add(new ScrollingImagePanel(bi, 800, 800));
		// jf.pack();
		// jf.setVisible(true);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new ImageIOExtTest("testImageReadMT"));

		suite.addTest(new ImageIOExtTest("testImageReadMTOperation"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
