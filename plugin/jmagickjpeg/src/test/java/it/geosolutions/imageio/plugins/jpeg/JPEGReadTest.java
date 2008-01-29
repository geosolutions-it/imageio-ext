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

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing reading capabilities for {@link JpegJMagickImageReader}
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
	public void testJAIRead() throws FileNotFoundException, IOException {

		// register the image read mt operation
		ImageReadDescriptorMT.register(JAI.getDefaultInstance());

		// get the file we are going to read
		final String fileName = "001140.jpg";
		final File file = TestData.file(this, fileName);

		// acquire a reader for it but check that it is the right one
		final Iterator readersIt = ImageIO.getImageReaders(file);
		ImageReader reader = null;
		while (readersIt.hasNext()) {
			reader = (ImageReader) readersIt.next();
			if (reader instanceof JpegJMagickImageReader)
				break;
			reader = null;
		}
		assertNotNull(reader);

		// do an image read with jai
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = reader.getDefaultReadParam();
		irp.setSourceSubsampling(2, 2, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageReadMT");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("Reader", reader);
		pbjImageRead.setParameter("readParam", irp);

		// set the layout so that we shrink the amount of memory needed to load
		// this image
		final ImageLayout l = new ImageLayout();
		l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
				.setTileWidth(512);

		RenderedOp image = JAI.create("ImageReadMT", pbjImageRead,
				new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

		if (TestData.isInteractiveTest()) {
			final JFrame jf = new JFrame();
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.getContentPane().add(new ScrollingImagePanel(image, 1024, 768));
			jf.pack();
			jf.show();
		} else {
			assertNotNull(image.getTiles());
			// remember that if we do not explictly provide an Imagereader to
			// the ImageReadMT operation it consistently dispose the one it
			// creates once we dispose the ImageReadOpImage
			image.dispose();
		}
	}

	/**
	 * Simple test read
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testRead() throws FileNotFoundException, IOException {

		// register the image read mt operation
		ImageReadDescriptorMT.register(JAI.getDefaultInstance());

		// get the file we are going to read
		final String fileName = "001140.jpg";
		final File file = TestData.file(this, fileName);

		// acquire a reader for it but check that it is the right one
		final Iterator readersIt = ImageIO.getImageReaders(file);
		ImageReader reader = null;
		while (readersIt.hasNext()) {
			reader = (ImageReader) readersIt.next();
			if (reader instanceof JpegJMagickImageReader)
				break;
			reader = null;
		}
		assertNotNull(reader);

		// do an image read with jai
		final ImageReadParam irp = reader.getDefaultReadParam();
		irp.setSourceSubsampling(4, 4, 0, 0);

		reader.setInput(ImageIO.createImageInputStream(file));
		BufferedImage image = reader.read(0, irp);

		if (TestData.isInteractiveTest()) {
			final JFrame jf = new JFrame();
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.getContentPane().add(new ScrollingImagePanel(image, 1024, 768));
			jf.pack();
			jf.show();
		} else {
			assertNotNull(image);
			// remember that if we do not explictly provide an Imagereader to
			// the ImageReadMT operation it consistently dispose the one it
			// creates once we dispose the ImageReadOpImage
			image.flush();
		}
	}

	
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of a simple image
		suite.addTest(new JPEGReadTest("testRead"));
		
		// Test reading of a simple image
		suite.addTest(new JPEGReadTest("testJAIRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
