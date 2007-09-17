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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;

import junit.framework.TestCase;

public class GeoTiffManualReadTest extends TestCase{
	
	public GeoTiffManualReadTest(String name) {
		super(name);

	}
	
	/**
	 * Test Read without exploiting JAI-ImageIO Tools
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testManualRead()throws IOException,FileNotFoundException{
		final ImageReadParam irp = new ImageReadParam();

		// Reading a simple GrayScale image
		String fileName = "bogota.tif";
		final File inputFile = TestData.file(this, fileName);
		irp.setSourceSubsampling(2, 2, 0, 0);
		GeoTiffImageReader reader = new GeoTiffImageReader(new GeoTiffImageReaderSpi());
		reader.setInput(inputFile);
		Viewer.visualize(reader.read(0,irp));
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(GeoTiffManualReadTest.class);
	}

}
