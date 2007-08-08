package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

public class HDF4ManualReadTest extends HDF4BaseTestCase{

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
		final String fileName = "PAL_APR_01_1988.HDF";
		final File file = TestData.file(this, fileName);
		mReader.setInput(file);
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("Reader", mReader);
		final int imageIndex = 0;
		pbjImageRead.setParameter("ImageChoice", new Integer(imageIndex));
		Viewer.visualizeImageMetadata(JAI.create("ImageRead", pbjImageRead), fileName, imageIndex);
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(HDF4ManualReadTest.class);
	}
}
