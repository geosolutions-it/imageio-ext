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
package it.geosolutions.imageio.plugins.javaarcgrid;

import it.geosolutions.resources.ShareableTestData;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EventListener;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOWriteProgressListener;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import com.sun.media.jai.operator.ImageWriteDescriptor;

public class AsciiGridTest extends TestCase {
	public AsciiGridTest(String name) {
		super(name);
	}
	
	private final static Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.javaarcgrid");

	protected void setUp() throws Exception {
		super.setUp();
		File file = TestData.file(this, "arcgrid.zip");
		assertTrue(file.exists());

		// unzip it
		ShareableTestData.unzipFile(this, "arcgrid.zip");

	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(AsciiGridTest.class);
	}

	/**
	 * Visualization Methods
	 */
	private void visualize(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame(test);
		frame.getContentPane().add(new ScrollingImagePanel(bi, 800, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				frame.pack();
				frame.show();
			}
		});
	}

	
	/**
	 * Main test of the {@link AsciiGridTest} class
	 */
	public void testJaiRead() throws FileNotFoundException, IOException {
		// ////////////////////////////////////////////////////////////////////
		//
		// TEST 1:
		// --------------------------------------------------------------------
		// Read an ArcGrid file and write it back to another file
		//
		// ////////////////////////////////////////////////////////////////////
		String title = new String("Simple JAI ImageRead operation test");
		LOGGER.info("\n\n " + title + " \n");
		File inputFile = TestData.file(this, "hsign.asc");
		ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", inputFile);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		if (ShareableTestData.isInteractiveTest())
			visualize(image, title);
		assert image.getWidth() == 278;
		assert image.getHeight() == 144;

		// //
		//
		// Writing it out
		//
		// //
		final File foutput = TestData.temp(this, "hsign.asc", false);
		final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
				"ImageWrite");
		pbjImageWrite.setParameter("Output", foutput);
		pbjImageWrite.addSource(image);

		// //
		//
		// What I am doing here is crucial, that is getting the used writer and
		// disposing it. This will force the underlying stream to write data on
		// disk.
		//
		// //
		RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
		final ImageWriter writer = (ImageWriter) op
				.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
		writer.dispose();

		// //
		//
		// Reading it back
		//
		// //
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", foutput);
		RenderedOp image2 = JAI.create("ImageRead", pbjImageRead);
		title = new String("Read Back the just written image");
		if (TestData.isInteractiveTest())
			visualize(image2, title);
		
		assert image.getWidth() == image2.getWidth();
		assert image.getHeight() == image2.getHeight();

		// ////////////////////////////////////////////////////////////////////
		//
		// TEST 2:
		// --------------------------------------------------------------------
		// Read a GRASS, compressed (GZ) file
		//
		// ////////////////////////////////////////////////////////////////////

		title = new String("JAI ImageRead on a GRASS GZipped file ");
		LOGGER.info("\n\n " + title + " \n");
		inputFile = TestData.file(this, "spearfish.asc.gz");
		final GZIPInputStream stream = new GZIPInputStream(new FileInputStream(
				inputFile));
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", stream);
		image = JAI.create("ImageRead", pbjImageRead);
		if (ShareableTestData.isInteractiveTest())
			visualize(image, title);
		else
			assert image.getData() != null;

		// ////////////////////////////////////////////////////////////////////
		//
		// TEST 3:
		// --------------------------------------------------------------------
		// Read a file using subSampling and sourceRegion settings
		//
		// ////////////////////////////////////////////////////////////////////
		title = new String("JAI ImageRead using subSampling and sourceRegion ");
		LOGGER.info("\n\n " + title + " \n");
		
		// //
		// 
		// Preparing ImageRead parameters
		//
		// //
		inputFile = TestData.file(this, "aust.asc");
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", inputFile);
		final ImageReadParam irp = new ImageReadParam();
		
		//Setting sourceRegion on the original image
		irp.setSourceRegion(new Rectangle(500, 400, 2000, 1500));
		
		//Setting subSampling factors
		irp.setSourceSubsampling(4, 4, 0, 0);
		pbjImageRead.setParameter("ReadParam", irp);
		image = JAI.create("ImageRead", pbjImageRead);
		if (ShareableTestData.isInteractiveTest())
			visualize(image, title);
		else
			image.getData();
	}
}
