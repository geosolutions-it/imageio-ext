package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class HDF4JAIReadTest extends HDF4BaseTestCase {

	public HDF4JAIReadTest(String name) {
		super(name);
	}

	/**
	 * This test method uses an HDF4 file containing datasets having several
	 * rasterBands (More than 4 raster band for dataset)
	 */
	public void testMultiBand() throws FileNotFoundException, IOException {
		final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		final File file = TestData.file(this, fileName);
		final ParameterBlockJAI pbjImageRead;
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		final int imageIndex = 6;
		pbjImageRead.setParameter("ImageChoice", new Integer(imageIndex));
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeImageMetadata(image, fileName, imageIndex);
	}
	
	/**
	 * This test method uses an HDF4 file containing several subdatasets
	 */

	public void testSubDatasets() throws FileNotFoundException, IOException {
		final int initialIndex = 0;
		final int nSubdatasetsLoop = 5;
		
		final ImageReadParam irp = new ImageReadParam();
		irp.setSourceSubsampling(xSubSamplingFactor, ySubSamplingFactor, 0, 0);

		HDF4ImageReader mReader = new HDF4ImageReader(new HDF4ImageReaderSpi());
		ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
		final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		final File file = TestData.file(this, fileName);

		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("Reader", mReader);
		pbjImageRead.setParameter("readParam", irp);

		for (int i = initialIndex; i < initialIndex + nSubdatasetsLoop; i++) {
			pbjImageRead.setParameter("ImageChoice", new Integer(i));
			RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			Viewer.visualizeImageMetadata(image, fileName, i);
			HDF4ImageReader reader = (HDF4ImageReader) image
					.getProperty("JAI.ImageReader");
			pbjImageRead.setParameter("Reader", reader);
		}
	}
	
	
	
	
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of several subdatasets 
		suite.addTest(new HDF4JAIReadTest("testSubDatasets"));

//		// Test reading of a multiband dataset
//		suite.addTest(new HDF4JAIReadTest("testMultiBand"));
	
		return suite;
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
	
	
}
