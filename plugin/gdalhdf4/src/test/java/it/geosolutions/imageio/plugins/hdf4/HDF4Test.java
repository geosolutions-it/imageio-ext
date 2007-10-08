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

import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
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
public class HDF4Test extends AbstractHDF4TestCase {

	public HDF4Test(String name) {
		super(name);
	}

	/**
	 * This test method uses an HDF4 file containing several subdatasets
	 */

	public void testSubDatasets() throws FileNotFoundException, IOException {
		try {
			final int initialIndex = 2;
			final int nSubdatasetsLoop = 3;

			final ImageReadParam irp = new ImageReadParam();
			irp.setSourceSubsampling(1, 1, 0, 0);

			HDF4ImageReader mReader = new HDF4ImageReader(
					new HDF4ImageReaderSpi());
			ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
			final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
			final File file = TestData.file(this, fileName);

			pbjImageRead.setParameter("Input", file);
			pbjImageRead.setParameter("Reader", mReader);
			pbjImageRead.setParameter("readParam", irp);

			for (int i = initialIndex; i < initialIndex + nSubdatasetsLoop; i++) {
				pbjImageRead.setParameter("ImageChoice", new Integer(i));
				RenderedOp image = JAI.create("ImageRead", pbjImageRead);
				if (TestData.isInteractiveTest())
					Viewer.visualizeImageMetadata(image, fileName, i);
				else
					assertNotNull(image.getTiles());

				HDF4ImageReader reader = (HDF4ImageReader) image
						.getProperty("JAI.ImageReader");
				pbjImageRead.setParameter("Reader", reader);
			}
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	public void testManualRead() throws FileNotFoundException, IOException {
		try {
			HDF4ImageReader mReader = new HDF4ImageReader(
					new HDF4ImageReaderSpi());
			final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
			final File file = TestData.file(this, fileName);
			mReader.setInput(file);
			RenderedImage ri = mReader.read(0);
			if (TestData.isInteractiveTest())
				Viewer.visualize(ri);
			else
				assertNotNull(ri.getData());
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * This test method retrieves properties from each band of the sample
	 * dataset
	 */
	public void testRasterBandsProperties() throws FileNotFoundException,
			IOException {
		try {
			HDF4ImageReader mReader = new HDF4ImageReader(
					new HDF4ImageReaderSpi());
			String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
			File file = TestData.file(this, fileName);
			mReader.setInput(file);
			final int numImages = 3;
			int bands;
			Iterator it;
			SampleModel sm;
			for (int i = 1; i < numImages; i++) {
				it = mReader.getImageTypes(i);
				ImageTypeSpecifier its;
				if (it.hasNext()) {
					its = (ImageTypeSpecifier) it.next();
					sm = its.getSampleModel();
					bands = sm.getNumBands();
					double d;
					StringBuffer sb = new StringBuffer(
							"RasterBands properties retrieval").append(
							" Image: ").append(i);
					for (int j = 0; j < bands; j++) {
						sb.append(" \n\t Band: ").append(j).append(" --- ");
						try {
							d = mReader.getNoDataValue(i, j);
							sb.append("NoDataV=").append(d);
						} catch (IllegalArgumentException iae) {
							sb.append(" NoDataV=NotAvailable");
						}
						try {
							d = mReader.getOffset(i, j);
							sb.append(" Off=").append(d);
						} catch (IllegalArgumentException iae) {
							sb.append(" Off=NotAvailable");
						}
						try {
							d = mReader.getScale(i, j);
							sb.append(" Scale=").append(d);
						} catch (IllegalArgumentException iae) {
							sb.append(" Scale=NotAvailable");
						}
						try {
							d = mReader.getMinimum(i, j);
							sb.append(" Min=").append(d);
						} catch (IllegalArgumentException iae) {
							sb.append(" Min=NotAvailable");
						}
						try {
							d = mReader.getMaximum(i, j);
							sb.append(" MAX=").append(d);
						} catch (IllegalArgumentException iae) {
							sb.append(" MAX=NotAvailable");
						}
					}
					LOGGER.info(sb.toString());
				}
			}
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * This test method retrieves and visualizes specified image metadata and
	 * stream metadata for a dataset containing several subdatasets
	 */
	public void testMetadata() throws FileNotFoundException, IOException {
		try {
			final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
			final File file = TestData.file(this, fileName);
			final ParameterBlockJAI pbjImageRead;
			final ImageReadParam irp = new ImageReadParam();

			irp.setSourceSubsampling(1, 1, 0, 0);
			pbjImageRead = new ParameterBlockJAI("ImageRead");
			pbjImageRead.setParameter("Input", file);
			pbjImageRead.setParameter("readParam", irp);

			int imageIndex = 2;
			pbjImageRead.setParameter("imageChoice", imageIndex);
			RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			if (TestData.isInteractiveTest()) {
				Viewer.visualizeImageMetadata(image, fileName, imageIndex,
						false);
				Viewer.visualizeStreamMetadata(image, fileName, false);
			}
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}

	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of several subdatasets
		suite.addTest(new HDF4Test("testSubDatasets"));

		// Test read without exploiting JAI
		suite.addTest(new HDF4Test("testManualRead"));

		// Test reading of several subdatasets
		suite.addTest(new HDF4Test("testRasterBandsProperties"));

		// Test Image Metadata
		suite.addTest(new HDF4Test("testMetadata"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
