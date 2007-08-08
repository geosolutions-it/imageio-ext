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

public class HDF4PropertiesTest extends HDF4BaseTestCase {

	public HDF4PropertiesTest(String name) {
		super(name);
	}

	private static final Logger logger = Logger.getLogger(HDF4PropertiesTest.class
			.toString());

	/**
	 * This test method simply tests the <code>getNumImages</code> method
	 */

	public void testNumberOfImages() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final String fileName = "PAL_APR_01_1988.HDF";
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
		String fileName = "PAL_APR_01_1988.HDF";
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
				for (int j = 0; j < bands; j++) {
					StringBuffer sb = new StringBuffer("Image: ").append(i)
							.append(" Band: ").append(j)
							.append(" --- NoDataV=").append(
									mReader.getNoDataValue(i, j)).append(
									" Off=").append(mReader.getOffset(i, j))
							.append(" Scale=").append(mReader.getScale(i, j))
							.append(" Min=").append(mReader.getMaximum(i, j))
							.append(" MAX=").append(mReader.getMinimum(i, j));
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
