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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions.
 *
 */
public class ECWManualReadTest extends AbstractECWTestCase{

	public ECWManualReadTest(String name) {
		super(name);
	}
	
	/**
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public void testManualRead() throws FileNotFoundException, IOException {
		final ECWImageReaderSpi spi = new ECWImageReaderSpi();
		final ECWImageReader mReader = new ECWImageReader(spi);
		final String fileName = "samplergb.ecw";
		final File file = TestData.file(this, fileName);
		final ImageReadParam param = new ImageReadParam();
		param.setSourceSubsampling((int) Math.pow(2, 0), (int) Math.pow(2, 0),
				0, 0);
		param.setSourceSubsampling(4,4,0,0);
		final int imageIndex = 0;
		
		mReader.setInput(file);
		final RenderedImage image = mReader.readAsRenderedImage(imageIndex, param);
		if(TestData.isExtensiveTest())
			Viewer.visualize(image, fileName);
		mReader.dispose();
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(ECWManualReadTest.class);
	}

}
