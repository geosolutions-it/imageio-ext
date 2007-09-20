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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;

import junit.framework.TestCase;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class MrSIDManualReadTest extends TestCase {

	/**
	 * The file used in this test is available at:
	 * https://zulu.ssc.nasa.gov/mrsid/
	 */

	public MrSIDManualReadTest(String name) {
		super(name);

	}

	/**
	 * Test Read without exploiting JAI-ImageIO Tools
	 * 
	 * @throws IOException
	 */
	public void testManualRead() throws IOException {
		MrSIDImageReader reader = new MrSIDImageReader(
				new MrSIDImageReaderSpi());
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "N-17-25_2000.sid";
		final File file = TestData.file(this, fileName);
		irp.setSourceSubsampling(16, 16, 0, 0);
		reader.setInput(file);
		Viewer.visualize(reader.read(0, irp));

	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(MrSIDManualReadTest.class);
	}

}
