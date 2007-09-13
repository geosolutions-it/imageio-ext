package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;

import junit.framework.TestCase;

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
