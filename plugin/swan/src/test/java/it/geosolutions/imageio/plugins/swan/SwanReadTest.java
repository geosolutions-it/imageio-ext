/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.swan;

import it.geosolutions.resources.TestData;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.Assert;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class SwanReadTest  {

	private static final Logger LOGGER = Logger
	.getLogger("it.geosolutions.imageio.plugins.swan");




	/**
	 * Read a raster related to the next-to-last forecast of the next-to-last
	 * Swan Output quantity contained within a single file source
	 */

	@org.junit.Test
	public void testReadFromMultipleQuantitiesFile()
			throws FileNotFoundException, IOException {
		try {
			File f = TestData.file(this, "Dirs.swo");
			final SwanImageReader reader = new SwanImageReader(
					new SwanImageReaderSpi());
			reader.setInput(f);

			// getting forecasts, datasets number, baseTime
			final IIOMetadata metadata = reader.getStreamMetadata();
			Node node = metadata
					.getAsTree(SwanStreamMetadata.nativeMetadataFormatName);
			Node generalNode = node.getFirstChild();
			final NamedNodeMap attributes = generalNode.getAttributes();
			final int quantities = Integer.parseInt(attributes.getNamedItem(
					"datasetNumber").getNodeValue());
			final int forecasts = Integer.parseInt(attributes.getNamedItem(
					"tauNumber").getNodeValue());
			final String baseTime = attributes.getNamedItem("baseTime")
					.getNodeValue();

			// getting dataset properties
			Node datasetNamesNode = generalNode.getFirstChild()
					.getNextSibling();
			Node tauNode = datasetNamesNode.getNextSibling();
			final NamedNodeMap tauAttrib = tauNode.getAttributes();
			final int tau = Integer.parseInt(tauAttrib.getNamedItem("time")
					.getNodeValue());
			final String tauUom = tauAttrib.getNamedItem("unitOfMeasure")
					.getNodeValue();

			// getting dataset
			Node datasetNode = datasetNamesNode.getFirstChild();

			// getting the shortname of the quantity
			for (int i = 0; i < quantities; i++) {
				final String quantityShortName = datasetNode.getAttributes()
						.getNamedItem("name").getNodeValue();

				// retrieving the index related to the next-to-last forecast of
				// the next-to-last output quantity
				for (int j = 0; j < forecasts; j++) {
					final StringBuffer title = new StringBuffer(
							quantityShortName).append(">");
					final int imageIndex = reader
							.getImageIndexFromTauAndDatasets(j, i);

					// preparing a new read operation
					reader.dispose();
					final int forecast = j * tau;
					title.append(" Forecast = ").append(baseTime);
					if (forecast != 0)
						title.append(" + ").append(Integer.toString(forecast))
								.append(tauUom);

					// reading the required raster using an imageRead operation
					final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
							"ImageRead");
					pbjImageRead.setParameter("Input", f);
					pbjImageRead.setParameter("imageChoice", imageIndex);
					RenderedOp image = JAI.create("ImageRead", pbjImageRead);
					if (TestData.isInteractiveTest())
						visualize(image, title.toString());
					else
						Assert.assertNotNull(image.getTiles());

				}
				datasetNode = datasetNode.getNextSibling();
			}
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Read a single forecast of a single quantity
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	@org.junit.Test
	public void testReadSingleQuantity() throws FileNotFoundException,
			IOException {
		try {
			File f = TestData.file(this, "per.swo");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", f);
			ImageReadParam irp = new ImageReadParam();
			irp.setSourceSubsampling(1, 1, 0, 0);

			// Reading the second Forecast
			pbjImageRead.setParameter("ImageChoice", 1);
			pbjImageRead.setParameter("readParam", irp);
			RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			if (TestData.isInteractiveTest())
				visualize(image);
			else
				Assert.assertNotNull(image.getTiles());
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Read some bi-components (X,Y) quantities. Wind and Force. Finally,
	 * display the single components.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@org.junit.Test
	public void testBiComponentQuantities() throws FileNotFoundException,
			IOException {
		try {
			File f[] = new File[] { TestData.file(this, "wind.swo"),
					TestData.file(this, "force.swo") };
			final int nFiles = f.length;
			for (int i = 0; i < nFiles; i++) {
				File inputFile = f[i];
				final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
						"ImageRead");
				pbjImageRead.setParameter("Input", inputFile);
				RenderedOp image = JAI.create("ImageRead", pbjImageRead);
				final int width = image.getWidth();
				final int height = image.getHeight();
				DataBufferFloat db = (DataBufferFloat) image.getData()
						.getDataBuffer();

				for (int band = 0; band < 2; band++) {
					final String component = band == 0 ? "X" : "Y";
					final BufferedImage bi = getQuantityComponent(db, band,
							width, height);
					final String fileName = inputFile.getName();
					if (TestData.isInteractiveTest())
						visualizeRescaled(bi, fileName.substring(0, fileName
								.length() - 4)
								+ component, null);
					else
						Assert.assertNotNull(image.getTiles());
				}
			}
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Read the second forecast of a single component quantity from a File
	 * containing 3 forecasts of a 2-components quantity (WIND) and the single
	 * quantity (PDIR).
	 */

	@org.junit.Test
	public void testSingleFromMixedQuantities() throws FileNotFoundException,
			IOException {
		try {
			File f = TestData.file(this, "WIND&PDIR.swo");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			final int imageIndex = 5;
			pbjImageRead.setParameter("Input", f);
			pbjImageRead.setParameter("imageChoice", imageIndex);
			RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			if (TestData.isInteractiveTest())
				visualize(image, "PDIR from mixed file", imageIndex);
			else
				Assert.assertNotNull(image.getTiles());
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Read the first forecast of the first quantity from files containing only
	 * single component quantities.
	 */
	@org.junit.Test
	public void testAnyQuantity() throws FileNotFoundException, IOException {
		try {
			File[] testFiles;
			testFiles = TestData.file(this, "/").listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					final String fileName = pathname.getName().toLowerCase();
					if (fileName.endsWith(".swo")
							&& !(fileName.contains("wind")
									|| fileName.contains("force") || fileName
									.contains("trans")))
						return true;
					return false;
				}
			});

			final int nFiles = testFiles.length;
			for (int i = 0; i < nFiles; i++) {
				File f = testFiles[i];
				final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
						"ImageRead");
				pbjImageRead.setParameter("Input", f);
				RenderedOp image = JAI.create("ImageRead", pbjImageRead);
				if (TestData.isInteractiveTest())
					visualize(image);
				else
					Assert.assertNotNull(image.getTiles());
			}
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Simple Manual Read operation followed by a rescaled image visualization.
	 * 
	 * @throws IOException
	 */

	@org.junit.Test
	public void testManualRead() throws IOException {
		try {
			File f = TestData.file(this, "depth.swo");
			ImageReader reader = new SwanImageReader(new SwanImageReaderSpi());
			reader.setInput(f);
			BufferedImage bi = reader.read(0, null);
			if (TestData.isInteractiveTest())
				visualizeRescaled(bi, "Manual Read", null);
			else
				Assert.assertNotNull(bi.getData());
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Visualization method with rescaling operation.
	 * 
	 * @param image
	 * @param title
	 * @param i
	 * @throws IOException
	 */

	private void visualize(RenderedOp image, String title, int i)
			throws IOException {
		// Getting reader.
		ImageReader reader = (ImageReader) image
				.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);

		// Getting metadata
		IIOMetadata metadata = reader.getImageMetadata(i);
		Node node = metadata.getAsTree(metadata.getNativeMetadataFormatName());
		Node datasetNode = node.getFirstChild();
		if (title == null)
			title = datasetNode.getAttributes().getNamedItem("shortName")
					.getNodeValue();

		// Getting noDataValue which will be used to set ROI
		final double noDataValue = Double.parseDouble(datasetNode
				.getAttributes().getNamedItem("noDataValue").getNodeValue());
		ROI roi = new ROI(image, (int) noDataValue);

		// Visualization
		visualizeRescaled(image, title, roi);
	}

	/**
	 * Visualize the image, rescaling its values.
	 * 
	 * @param image
	 * @param title
	 * @param roi
	 */
	private void visualizeRescaled(RenderedImage image, String title, ROI roi) {
		final JFrame frame = new JFrame();

		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image); // The source image
		if (roi != null)
			pb.add(roi); // The region of the image to scan

		// Perform the extrema operation on the source image
		RenderedOp op = JAI.create("extrema", pb);

		// Retrieve both the maximum and minimum pixel value
		double[][] extrema = (double[][]) op.getProperty("extrema");

		final double[] scale = new double[] { (255) / (extrema[1][0] - extrema[0][0]) };
		final double[] offset = new double[] { ((255) * extrema[0][0])
				/ (extrema[0][0] - extrema[1][0]) };

		// Preparing to rescaling values
		ParameterBlock pbRescale = new ParameterBlock();
		pbRescale.add(scale);
		pbRescale.add(offset);
		pbRescale.addSource(image);
		RenderedOp rescaledImage = JAI.create("Rescale", pbRescale);

		ParameterBlock pbConvert = new ParameterBlock();
		pbConvert.addSource(rescaledImage);
		pbConvert.add(DataBuffer.TYPE_BYTE);
		RenderedOp destImage = JAI.create("format", pbConvert);

		frame.getContentPane()
				.add(new ScrollingImagePanel(destImage, 300, 180));

		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.pack();
				frame.show();
			}
		});

	}

	/**
	 * Visualization Methods
	 * 
	 * @throws IOException
	 */
	private void visualize(final RenderedOp image, String title)
			throws IOException {
		visualize(image, title, 0);
	}

	private void visualize(RenderedOp image) throws IOException {
		visualize(image, null);
	}

	/**
	 * Build a single Band <code>BufferedImage</code>, given a
	 * <code>DataBuffer</code> and the index of the required band as input
	 * parameter
	 * 
	 * @param db
	 * @param band
	 * @param height
	 * @param width
	 * @return
	 */
	private BufferedImage getQuantityComponent(DataBufferFloat db,
			final int band, final int width, final int height) {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorModel cm = RasterFactory.createComponentColorModel(
				DataBuffer.TYPE_FLOAT, // dataType
				cs, // color space
				false, // has alpha
				false, // is alphaPremultiplied
				Transparency.OPAQUE); // transparency

		// Get the required bank of data.
		float[] dataFloat = db.getData(band);
		final int size = width * height;
		DataBufferFloat reducedDb = new DataBufferFloat(dataFloat, size);
		final SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_FLOAT,
				width, height, 1);
		final WritableRaster raster = RasterFactory.createWritableRaster(sm,
				reducedDb, null);
		return new BufferedImage(cm, raster, false, null);
	}

	protected void warningMessage() {
		StringBuffer sb = new StringBuffer(
				"Test file not available. Please download it as "
						+ "anonymous FTP from "
						+ "ftp://ftp.geo-solutions.it/incoming/swantest.zip"
						+ "\n Use a tool supporting Active Mode.\n"
						+ "Then unzip it on: plugin/"
						+ "swan/src/test/resources/it/geosolutions/"
						+ "imageio/plugins/swan/test-data folder and"
						+ " repeat the test.");
		LOGGER.info(sb.toString());
	}
}