package it.geosolutions.imageio.plugins.jhdf;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

public class JHDFTest extends TestCase {
	public JHDFTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				64 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);

	}

	//
	// public void testJaiMultithreadingRead() throws IOException {
	// // SETS JHDFImageReaderSpi as SPI in
	// // META-INF/services/javax.imageio.spi.ImageReaderSpi
	//
	// final File file = TestData.file(this,
	// "MODPM2007027122358.L3_000_EAST_MED");
	// ImageReadParam irp = new ImageReadParam();
	// irp.setSourceSubsampling(1, 1, 0, 0);
	// // irp.setSourceRegion(new Rectangle(0, 512, 1024, 1024));
	// int i = 2;
	// final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
	// "ImageRead");
	//
	// pbjImageRead.setParameter("Input", file);
	// pbjImageRead.setParameter("readParam", irp);
	// pbjImageRead.setParameter("imageChoice", Integer.valueOf(i));
	// final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
	// visualize(image, "");
	// }

	/**
	 * Visualization Methods
	 */
	protected static void visualize(final RenderedImage bi, String test) {
		visualize(bi, test, false);
	}

	/**
	 * Visualization Methods
	 */
	protected static void visualize(final RenderedImage bi, String test,
			boolean rescaleImage) {
		final JFrame frame = new JFrame(test);

		if (rescaleImage) {
			ROI roi = new ROI(bi, -999);

			ParameterBlock pb = new ParameterBlock();
			pb.addSource(bi); // The source image
			pb.add(roi); // The region of the image to scan

			// Perform the extrema operation on the source image
			RenderedOp op = JAI.create("extrema", pb);

			// Retrieve both the maximum and minimum pixel value
			double[][] extrema = (double[][]) op.getProperty("extrema");

			final double[] scale = new double[] { (255) / (extrema[1][0] - extrema[0][0]) };
			final double[] offset = new double[] { ((255) * extrema[0][0])
					/ (extrema[0][0] - extrema[1][0]) };

			ParameterBlock pbRescale = new ParameterBlock();
			pbRescale.add(scale);
			pbRescale.add(offset);
			pbRescale.addSource(bi);
			PlanarImage rescaledImage = (PlanarImage) JAI.create("Rescale",
					pbRescale);

			ParameterBlock pbConvert = new ParameterBlock();
			pbConvert.addSource(rescaledImage);
			pbConvert.add(DataBuffer.TYPE_BYTE);
			PlanarImage convertedImage = JAI.create("format", pbConvert);
			frame.getContentPane().add(
					new ScrollingImagePanel(convertedImage, 800, 600));
		} else
			frame.getContentPane().add(new ScrollingImagePanel(bi, 800, 600));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				frame.pack();
				frame.show();
			}
		});
	}

}
