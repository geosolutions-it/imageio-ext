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

import it.geosolutions.imageio.plugins.mrsid.MrSIDImageReaderSpi;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

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
		// ImageReadDescriptorMT.register(JAI.getDefaultInstance());
		// final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageReadMT");
		// assertNotNull(pbj);
	}

	public void testImageReadMTOperation() throws IOException {

		final String opName = "ImageReadMT";
		JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
		ImageReader reader = new MrSIDImageReaderSpi().createReaderInstance();
		final File file = new File(new String("C:/test.sid"));
		reader.setInput(file);
		ImageTypeSpecifier spec = (ImageTypeSpecifier) reader.getImageTypes(0)
				.next();
		// //
		//
		// Getting image properties
		//
		// //
		final int width = reader.getWidth(0);
		final int height = reader.getHeight(0);

		// //
		// 
		// Setting Image Read Parameters
		//
		// //
		final ImageReadParam param = new DefaultCloneableImageReadParam();
		// final Rectangle sourceRegion = new Rectangle(50, 50, 300, 300);
		// param.setSourceRegion(sourceRegion);

		BufferedImage bi = new BufferedImage(spec.getColorModel(),
				RasterFactory.createWritableRaster(spec.getSampleModel()
						.createCompatibleSampleModel(width, height), null),
				false, null);
		param.setDestination(bi);
		// param.setDestinationOffset(new Point(50, 50));

		// //
		//
		// Preparing the ImageRead operation
		//
		// //
		ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(opName);
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", param);
		pbjImageRead.setParameter("reader", reader);

		// //
		//
		// Setting a Layout
		//
		// //
		final ImageLayout l = new ImageLayout();
		l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256)
				.setTileWidth(256);
		RenderedOp image = JAI.create(opName, pbjImageRead, new RenderingHints(
				JAI.KEY_IMAGE_LAYOUT, l));
		image.getTiles();
		final JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(bi, 800, 800));
		jf.pack();
		jf.setVisible(true);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// suite.addTest(new ImageIOExtTest("testImageReadMT"));

		suite.addTest(new ImageIOExtTest("testImageReadMTOperation"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
