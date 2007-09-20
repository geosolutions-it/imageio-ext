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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class HDF4ManualReadTest extends AbstractHDF4TestCase {

	public HDF4ManualReadTest(String name) {
		super(name);
	}

	/**
	 * In this test, we manually set the reader used from the ImageRead
	 * operation.
	 */
	public void testManualSetting() throws FileNotFoundException, IOException {
		HDF4ImageReader mReader = new HDF4ImageReader(new HDF4ImageReaderSpi());
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		final File file = TestData.file(this, fileName);
		mReader.setInput(file);
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("Reader", mReader);
		final int imageIndex = 0;
		pbjImageRead.setParameter("ImageChoice", new Integer(imageIndex));
		Viewer.visualizeImageMetadata(JAI.create("ImageRead", pbjImageRead),
				fileName, imageIndex);
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(HDF4ManualReadTest.class);
	}
}
