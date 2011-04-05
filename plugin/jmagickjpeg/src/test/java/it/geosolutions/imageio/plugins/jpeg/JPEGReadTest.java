/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

import org.junit.Assert;

/**
 * Testing reading capabilities for {@link JpegJMagickImageReader}
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JPEGReadTest extends Assert {
    
    private static final Logger LOGGER = Logger.getLogger(JPEGReadTest.class.toString());

    private static boolean isJmagickAvailable;
    
    static{
        try{
          System.loadLibrary("JMagick");
          isJmagickAvailable = true;
        } catch (UnsatisfiedLinkError e){
            if (LOGGER.isLoggable(Level.WARNING)){
                LOGGER.warning("Failed to load the JMagick libs. This is not a problem unless you need to use the JMagick plugins: they won't be enabled." + e.getLocalizedMessage());
            }
            isJmagickAvailable = false;
        }
        
    }
    
	/**
	 * Simple test read
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void multithreadedJAIRead() throws FileNotFoundException, IOException {
	    if (!isJmagickAvailable) {
	            LOGGER.warning("JMagick Library is not Available; Skipping tests");
	            return;
	        }
	    
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
		irp.setSourceSubsampling(4, 4, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageReadMT");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("Reader", reader);
		pbjImageRead.setParameter("readParam", irp);

		// set the layout so that we shrink the amount of memory needed to load
		// this image
		final ImageLayout l = new ImageLayout();
		l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512).setTileWidth(512);

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
	@org.junit.Test
	public void ImageIORead() throws FileNotFoundException, IOException {
	    if (!isJmagickAvailable) {
                LOGGER.warning("JMagick Library is not Available; Skipping tests");
                return;
            }

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

		if (true) {
			final JFrame jf = new JFrame();
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.getContentPane().add(new ScrollingImagePanel(image, 1024, 768));
			jf.pack();
			jf.show();
		} else {
			assertNotNull(image);
			// flush resources
			image.flush();
			image=null;
		}
	}


}
