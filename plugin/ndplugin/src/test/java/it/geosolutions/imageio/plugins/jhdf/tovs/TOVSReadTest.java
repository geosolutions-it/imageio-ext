package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.imageio.plugins.jhdf.JHDFTest;
import it.geosolutions.imageio.plugins.slices2D.SliceImageReader;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TOVSReadTest extends JHDFTest {
	public TOVSReadTest(String name) {
		super(name);
	}

	/**
	 * 
	 * @throws IOException
	 */

	public void testJaiTOVSRead() throws IOException {
		for (int i = 0; i < 45; i++) {
			//this Test File is available at 
			//ftp://ftp.geo-solutions.it/incoming/TOVS_5DAYS_PM_B790625.E790629_TN.zip
			//as anonymous ftp access.
			
			//TODO: build an utility to auto-download and unzip this file in 
			//the setUp method.
			
			final File file = TestData.file(this,
					"TOVS_5DAYS_PM_B790625.E790629_TN.HDF");
			final ImageReader reader = new TOVSImageReader(
					new TOVSImageReaderSpi());
			reader.setInput(file);

			final IIOMetadata metadata = reader.getImageMetadata(i);
			Node imageNode = metadata
					.getAsTree(TOVSImageMetadata.nativeMetadataFormatName);
			imageNode = imageNode.getFirstChild();
			NamedNodeMap nodeMap = imageNode.getAttributes();
			final String productName = nodeMap.getNamedItem("Name")
					.getNodeValue();
			final int[] indexes = ((SliceImageReader) reader)
					.getSlice2DIndexCoordinates(i);

			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			ImageReadParam irp = new ImageReadParam();
			irp.setSourceSubsampling(1, 1, 0, 0);
			pbjImageRead.setParameter("reader", reader);
			pbjImageRead.setParameter("readParam", irp);
			pbjImageRead.setParameter("imageChoice", Integer.valueOf(i));

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			final StringBuffer sb = new StringBuffer("coverage index = ")
					.append(Integer.toString(indexes[0])).append(
							" coverage Name = ").append(productName);
			if (indexes.length > 1)
				sb.append(" 3rd Dim index = ").append(
						Integer.toString(indexes[1]));
			visualize(image, sb.toString(), true);
			reader.dispose();
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new TOVSReadTest("testJaiTOVSRead"));
		
		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}