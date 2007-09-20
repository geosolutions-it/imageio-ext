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

import it.geosolutions.resources.TestData;

import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class HDF4PropertiesTest extends AbstractHDF4TestCase {

	public HDF4PropertiesTest(String name) {
		super(name);
	}

	private static final Logger logger = Logger
			.getLogger(HDF4PropertiesTest.class.toString());

	/**
	 * This test method simply tests the <code>getNumImages</code> method
	 */
	public void testNumberOfImages() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		final File file = TestData.file(this, fileName);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		HDF4ImageReader reader = (HDF4ImageReader) image
				.getProperty("JAI.ImageReader");
		final int nImages = reader.getNumImages(false);
		StringBuffer sb = new StringBuffer(
				"Number Of Available Images within the specified input: ")
				.append(String.valueOf(nImages));
		logger.info(sb.toString());
		reader.dispose();
	}

	/**
	 * This test method retrieves properties from each band of the sample
	 * dataset
	 */
	public void testRasterBandsProperties() throws FileNotFoundException,
			IOException {

		HDF4ImageReader mReader = new HDF4ImageReader(new HDF4ImageReaderSpi());
		String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		File file = TestData.file(this, fileName);
		mReader.setInput(file);
		final int numImages = mReader.getNumImages(false);
		int bands;
		Iterator it;
		SampleModel sm;
		for (int i = 0; i < numImages; i++) {
			it = mReader.getImageTypes(i);
			ImageTypeSpecifier its;
			if (it.hasNext()) {
				its = (ImageTypeSpecifier) it.next();
				sm = its.getSampleModel();
				bands = sm.getNumBands();
				double d;

				for (int j = 0; j < bands; j++) {
					StringBuffer sb = new StringBuffer("Image: ").append(i)
							.append(" Band: ").append(j).append(" --- ");
					try {
						d = mReader.getNoDataValue(i, j);
						sb.append("NoDataV=").append(d);
					} catch (IllegalArgumentException iae) {

					}
					try {
						d = mReader.getOffset(i, j);
						sb.append(" Off=").append(d);
					} catch (IllegalArgumentException iae) {

					}
					try {
						d = mReader.getScale(i, j);
						sb.append(" Scale=").append(d);
					} catch (IllegalArgumentException iae) {

					}
					try {
						d = mReader.getMinimum(i, j);
						sb.append(" Min=").append(d);
					} catch (IllegalArgumentException iae) {

					}
					try {
						d = mReader.getMaximum(i, j);
						sb.append(" MAX=").append(d);
					} catch (IllegalArgumentException iae) {

					}
					logger.info(sb.toString());
				}
			}
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new HDF4PropertiesTest("testRasterBandsProperties"));

		suite.addTest(new HDF4PropertiesTest("testNumberOfImages"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
