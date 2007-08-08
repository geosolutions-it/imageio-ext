package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.stream.output.FileImageOutputStreamExtImpl;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.xmlbeans.XmlException;

import com.sun.media.jai.operator.ImageWriteDescriptor;

public class JP2KProfilesTest extends AbstractJP2KTestCase {

	final static String testFileName = "CB_TM432.jp2";

	final static String profileFileName = "huge.xml";

	public JP2KProfilesTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// multithreading settings
		JP2GDALKakaduImageReaderSpi.setReadMultithreadingLevel(5);
		JP2GDALKakaduImageWriterSpi.setWriteMultithreadingLevel(5);
	}

	public void testWriteProfile() throws IOException, FileNotFoundException,
			XmlException {
		// //
		// Preparing input/output files
		// //
		final File inputFile =
		// new
		// File("F:/geoserver/data/coverages/Blue_marble_dec_2004/writingprofile20588.jp2");
//			new File("F:/work/data/002025_0100_010722_l7_01_utm21.tif");
		new File("F:/geoserver/data/coverages/spezia/spezia_wgs84.tiff");
		// TestData.file(this, testFileName);;
		assertTrue(inputFile.exists());

		final File profileFile = TestData.file(this, profileFileName);
		assertTrue(profileFile.exists());

		// //
		// Preparing to read
		// //
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		pbjImageRead.setParameter("Input", inputFile);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);

		// ////////////////////////////////////////////////////////////////////
		// preparing to write
		// ////////////////////////////////////////////////////////////////////
		final File outputFile = TestData
				.temp(this, "writingprofile.jp2", false);
		// outputFile.deleteOnExit();

		// Setting output and writer
		final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
				"ImageWrite");
		pbjImageWrite.setParameter("Output", new FileImageOutputStreamExtImpl(
				outputFile));
		ImageWriter writer = new JP2GDALKakaduImageWriterSpi()
				.createWriterInstance();
		pbjImageWrite.setParameter("Writer", writer);

		// Specifying image source to write
		pbjImageWrite.addSource(image);

		JP2GDALKakaduImageWriteParam param = new JP2GDALKakaduImageWriteParam(
				profileFile);
		pbjImageWrite.setParameter("writeParam", param);

		// Writing
		final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
		((ImageWriter) op
				.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER))
				.dispose();

		// ////////////////////////////////////////////////////////////////////
		// read it back
		// ////////////////////////////////////////////////////////////////////
		// pbjImageRead.setParameter("Input", outputFile);
		// image = JAI.create("ImageRead", pbjImageRead);
		// Viewer.visualize(image);

	}

	public static Test suite() {
		final TestSuite suite = new TestSuite();
		suite.addTest(new JP2KProfilesTest("testWriteProfile"));
		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
