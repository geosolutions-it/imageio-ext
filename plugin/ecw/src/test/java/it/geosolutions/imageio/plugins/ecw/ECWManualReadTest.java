package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

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
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		final String fileName = "downtown.ecw";
		final File file = TestData.file(this, fileName);
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("Reader", mReader);
		final ImageReadParam param = new ImageReadParam();
		param.setSourceSubsampling((int) Math.pow(2, 0), (int) Math.pow(2, 0),
				0, 0);
		pbjImageRead.setParameter("ReadParam", param);
		
		final int imageIndex = 0;
		pbjImageRead.setParameter("ImageChoice", new Integer(imageIndex));
		Viewer.visualizeCRS(JAI.create("ImageRead", pbjImageRead), fileName);
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(ECWManualReadTest.class);
	}

}
