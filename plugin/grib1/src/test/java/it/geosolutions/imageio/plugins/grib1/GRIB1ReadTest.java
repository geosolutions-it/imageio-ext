package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.AbstractSpatioTemporalReader;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.joda.time.Instant;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class GRIB1ReadTest extends TestCase {

	final static String dataPathPrefix = "E:/work/data/grib/";

	public GRIB1ReadTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of a simple image
		suite.addTest(new GRIB1ReadTest("testSpatioTemporalRead"));
		//
		// Test reading of a simple image
		suite.addTest(new GRIB1ReadTest("testSimpleRead"));
		//
		// Test reading of a simple image
		suite.addTest(new GRIB1ReadTest("testSpatioTemporalRead2"));
		//		
		// Test reading of a simple image
		suite.addTest(new GRIB1ReadTest("testSpatioTemporalReadSingleFile"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public void testSpatioTemporalReadSingleFile() throws IOException {
		final File inputFile = new File(dataPathPrefix + "SSAtlantic.wind.grb");
		AbstractSpatioTemporalReader reader = new GRIB1SpatioTemporalReader();

		String timeInstants[] = new String[] {
				new Instant("2005-03-30T00:00:00.000Z").toString(),
				new Instant("2005-03-30T06:00:00.000Z").toString(),
				new Instant("2005-03-30T12:00:00.000Z").toString() };
		reader.setInput(inputFile);
		String[] bands = null;
		List ris = ((GRIB1SpatioTemporalReader) reader).read("WIND",
				timeInstants, null, null, bands);
		final int images = ris.size();
		for (int i = 0; i < images; i++) {
			RenderedImage ri = (RenderedImage) ris.get(i);
			visualize(ri, "");
		}
	}
	
	public void testSpatioTemporalRead2() throws IOException {
		final String fileName = "lami_20060210.grib";
		AbstractSpatioTemporalReader reader = new GRIB1SpatioTemporalReader();

		reader.setInput(dataPathPrefix + fileName);
		displayImageIOMetadata(reader.getCoverageMetadata().getAsTree(
				GRIB1CoverageMetadata.nativeMetadataFormatName));

		String timeInstants[] = new String[] {
				new Instant("2005-04-02T06:00:00.000Z").toString(),//this one is not valid.
				new Instant("2006-02-10T12:00:00.000Z").toString(),
				new Instant("2006-02-10T21:00:00.000Z").toString() };

		List ris = ((GRIB1SpatioTemporalReader) reader).read("WIND",
				timeInstants, null, null, new String[] { "UGRD", "VGRD" });

		final int images = ris.size();
		for (int i = 0; i < images; i++) {
			RenderedImage ri = (RenderedImage) ris.get(i);
			visualize(ri, "");
		}
	}

	public void testSpatioTemporalRead() throws IOException {
		AbstractSpatioTemporalReader reader = new GRIB1SpatioTemporalReader();
		reader.setInput(dataPathPrefix + "coamps");
		displayImageIOMetadata(reader.getCoverageMetadata().getAsTree(
				GRIB1CoverageMetadata.nativeMetadataFormatName));

		String timeInstants[] = new String[] {
				new Instant("2005-04-02T03:00:00.000Z").toString(),
				new Instant("2005-04-02T15:00:00.000Z").toString(),
				new Instant("2005-04-03T03:00:00.000Z").toString() };

		// Old test time: 2005-04-02T03:00:00.000Z
		List ris = ((GRIB1SpatioTemporalReader) reader).read("WIND",
				timeInstants, null, null, new String[] { "var33", "var34" });

		final int images = ris.size();

		for (int i = 0; i < images; i++) {
			RenderedImage ri = (RenderedImage) ris.get(i);
			visualize(ri, "");
		}
	}

	public void testSimpleRead() throws IOException {
		final File file = new File(dataPathPrefix
				+ "coamps/separated WINDS/coamps_ugrd_2003090600_00.grib");
		GRIB1ImageReader imageReader = new GRIB1ImageReader(
				new GRIB1ImageReaderSpi());
		imageReader.setInput(file);
		ImageReadParam irp = new ImageReadParam();
		// irp.setSourceRegion(new Rectangle(20, 20, 100, 100));
		irp.setSourceSubsampling(2, 3, 0, 0);
		visualize(imageReader.read(0, irp));
	}

	public static void displayImageIOMetadata(Node root) {
		displayMetadata(root, 0);
	}

	static void indent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
	}

	static void displayMetadata(Node node, int level) {
		indent(level); // emit open tag
		System.out.print("<" + node.getNodeName());
		NamedNodeMap map = node.getAttributes();
		if (map != null) { // print attribute values
			int length = map.getLength();
			for (int i = 0; i < length; i++) {
				Node attr = map.item(i);
				System.out.print(" " + attr.getNodeName() + "=\""
						+ attr.getNodeValue() + "\"");
			}
		}
		System.out.print(">"); // close current tag
		String nodeValue = node.getNodeValue();
		if (nodeValue != null)
			System.out.println(" " + nodeValue);
		else
			System.out.println("");

		Node child = node.getFirstChild();
		if (child != null) {
			while (child != null) { // emit child tags recursively
				displayMetadata(child, level + 1);
				child = child.getNextSibling();
			}
			indent(level); // emit close tag
			System.out.println("</" + node.getNodeName() + ">");
		} else {
			// System.out.println("/>");
		}
	}

	private static void visualize(RenderedImage ri) {
		visualize(ri, "");
	}

	private static void visualize(final RenderedImage ri, String title) {
		final JFrame frame = new JFrame(title);
		frame.getContentPane().add(new ScrollingImagePanel(ri, 640, 480));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
