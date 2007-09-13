package it.geosolutions.imageio.plugins.mrsid;

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

public class MrSIDInformationRetrievalTest extends AbstractMrSIDTestCase {

	public MrSIDInformationRetrievalTest(String name) {
		super(name);
	}

	public void testReadAndProjections() throws FileNotFoundException,
			IOException {
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "dq2808ne.sid";
		final File file = TestData.file(this, fileName);
		irp.setSourceSubsampling(5, 5, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeCRS(image, fileName);
	}

	public void testAllInformation() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "B202761C.sid";
		final File file = TestData.file(this, fileName);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeAllInformation(image, fileName, true);
	}

	public void testReadAndMetadata() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "B202761C.sid";
		final File file = TestData.file(this, fileName);

		irp.setSourceSubsampling(10, 10, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeImageMetadata(image, fileName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Visualize Image and provides to retrieve CRS information
		suite.addTest(new MrSIDInformationRetrievalTest(
				"testReadAndProjections"));

		// Visualize Image and provides to retrieve metadata
		suite.addTest(new MrSIDInformationRetrievalTest("testReadAndMetadata"));

		// Provides to retrieve both metadata and CRS information
		suite.addTest(new MrSIDInformationRetrievalTest("testAllInformation"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
