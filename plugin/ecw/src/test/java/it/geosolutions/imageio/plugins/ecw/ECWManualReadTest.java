package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

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
		Viewer.visualize(mReader.readAsRenderedImage(imageIndex, param), fileName);
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(ECWManualReadTest.class);
	}

}
