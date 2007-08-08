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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.media.jai.operator.ImageReadDescriptor;

public class SwanReadTest extends TestCase {

	public SwanReadTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		final String[] dataSources = new String[] { "Mixed.zip",
				"2cQuantities.zip", "MultiQuantitySources.zip",
				"Quantities1.zip", "Quantities2.zip" };
		final int nTestData = dataSources.length;

		for (int i = 0; i < nTestData; i++) {
			try {
				File fileZip = TestData.file(this, dataSources[i]);
			} catch (FileNotFoundException fnfe) {
				try{
					getTestData(dataSources[i]);
					TestData.unzipFile(this, dataSources[i]);
				}
				catch(FileNotFoundException fnfe2){
					final IOException ioe = new IOException("Unable to download sample data");
					ioe.initCause(fnfe2);
					throw ioe;
				}
			}
		}
	}

	/**
	 * Read a raster related to the next-to-last forecast of the next-to-last
	 * Swan Output quantity contained within a single file source
	 */
	public void testReadFromMultipleQuantitiesFile()
			throws FileNotFoundException, IOException {

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
		Node datasetNamesNode = generalNode.getFirstChild().getNextSibling();
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

			// retrieving the index related to the next-to-last forecast of the
			// next-to-last output quantity
			for (int j = 0; j < forecasts; j++) {
				final StringBuffer title = new StringBuffer(quantityShortName)
						.append(">>>>");
				final int imageIndex = reader.getImageIndexFromTauAndDatasets(
						j, i);

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
				visualize(image, title.toString());
			}
			datasetNode = datasetNode.getNextSibling();
		}
	}

	/**
	 * Read a single forecast of a single quantity
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testReadSingleQuantity() throws FileNotFoundException,
			IOException {
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
		visualize(image);
	}

	/**
	 * Read some bi-components (X,Y) quantities. Wind and Force. Finally,
	 * display the single components.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testBiComponentQuantities() throws FileNotFoundException,
			IOException {
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
				final BufferedImage bi = getQuantityComponent(db, band, width,
						height);
				final String fileName = inputFile.getName();
				visualizeRescaled(bi, fileName.substring(0,
						fileName.length() - 4)
						+ component, null);
			}
		}
	}

	/**
	 * Read the second forecast of a single component quantity from a File
	 * containing 3 forecasts of a 2-components quantity (WIND) and the single
	 * quantity (PDIR).
	 */
	public void testSingleFromMixedQuantities() throws FileNotFoundException,
			IOException {
		File f = TestData.file(this, "WIND&PDIR.swo");
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");
		final int imageIndex = 5;
		pbjImageRead.setParameter("Input", f);
		pbjImageRead.setParameter("imageChoice", imageIndex);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		visualize(image, "PDIR from mixed file", imageIndex);
	}

	/**
	 * Read the first forecast of the first quantity from files containing only
	 * single component quantities.
	 */
	public void testAnyQuantity() throws FileNotFoundException, IOException {
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
			visualize(image);
		}
	}
	
	/**
	 * Simple Manual Read operation followed by a rescaled image visualization.
	 * @throws IOException
	 */

	public void testManualRead() throws IOException {
		File f = TestData.file(this, "depth.swo");
		ImageReader reader = new SwanImageReader(new SwanImageReaderSpi());
		reader.setInput(f);
		BufferedImage bi = reader.read(0, null);
		visualizeRescaled(bi, "Manual Read", null);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		 suite.addTest(new SwanReadTest("testReadFromMultipleQuantitiesFile"));
		 suite.addTest(new SwanReadTest("testReadSingleQuantity"));
		 suite.addTest(new SwanReadTest("testAnyQuantity"));
		suite.addTest(new SwanReadTest("testBiComponentQuantities"));
		 suite.addTest(new SwanReadTest("testSingleFromMixedQuantities"));
		 suite.addTest(new SwanReadTest("testManualRead"));
		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	//
	// AUXILIARY METHODS
	//
	//
	// ////////////////////////////////////////////////////////////////////////

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
		RenderedOp rescaledImage = JAI.create("Rescale",
				pbRescale);

		ParameterBlock pbConvert = new ParameterBlock();
		pbConvert.addSource(rescaledImage);
		pbConvert.add(DataBuffer.TYPE_BYTE);
		RenderedOp destImage = JAI.create("format", pbConvert);

		frame.getContentPane()
				.add(new ScrollingImagePanel(destImage, 800, 600));

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

	/**
	 * Getting testData from remote URL.
	 */
	private File getTestData(final String name) throws FileNotFoundException,
			IOException {
		final StringBuffer urlLocation = new StringBuffer(
				"http://danielsan.altervista.org/data/");
		urlLocation.append(name);
		final URL url = new URL(urlLocation.toString());
		final File tempFile;

		// /////////////////////////////////////////////////////////////////////
		//
		// Preliminary checks
		//
		// /////////////////////////////////////////////////////////////////////
		// input URL may rely to a local File. In this case, no caching
		// mechanism occurs and we simply returns that file.
		if (url.getProtocol().compareToIgnoreCase("file") == 0) {
			tempFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
		} else {

			// Getting the folder where TestData should be put
			final File tempFileForFolder = TestData.temp(this, "temp", true);

			String tempFilePath = tempFileForFolder.getAbsolutePath();
			tempFilePath = tempFilePath.substring(0, tempFilePath
					.lastIndexOf("\\") + 1);

			// Creating tempFile in TestData folder.
			tempFile = File.createTempFile("zipped", ".zip", new File(
					tempFilePath));
			tempFile.deleteOnExit();

			String tempFileName = tempFile.getAbsolutePath();
			// //
			//
			// getting an InputStream from the connection to the
			// object referred to by the URL
			//
			// //

			URLConnection connection = url.openConnection();
			final int byteLength = connection.getContentLength();
			final int length = byteLength / 1024;
			int byteDownloaded = 0;
			final InputStream is = connection.getInputStream();

			// //
			//
			// Preparing a FileOutputStream where to write all data
			// we will read by the InputStream
			//
			// //
			final BufferedOutputStream os = new BufferedOutputStream(
					new FileOutputStream(tempFile));
			final byte b[] = new byte[65536];
			int num = 0;
			int step = 0;

			// "read from InputStream -> write to FileOutputStream"
			// operation
			System.out
					.println("Downloading File " + name + " (" + length + "KB)");
			while ((num = is.read(b)) > 0) {
				byteDownloaded += num;
				if (byteDownloaded > byteLength / 5
						&& byteDownloaded < ((byteLength * 2) / 5) && step < 1) {
					System.out.println("20%");
					step++;
				} else if (byteDownloaded > ((byteLength * 2) / 5)
						&& byteDownloaded < ((byteLength * 3) / 5) && step < 2) {
					System.out.println("40%");
					step++;
				} else if (byteDownloaded > ((byteLength * 3) / 5)
						&& byteDownloaded < ((byteLength * 4) / 5) && step < 3) {
					System.out.println("60%");
					step++;
				} else if (byteDownloaded > ((byteLength * 4) / 5) && step < 4) {
					System.out.println("80%");
					step++;
				}
				os.write(b, 0, num);
			}
			System.out.println("DONE\n");

			// closing streams and flushing the outputStream
			os.flush();
			is.close();
			os.close();
			// final String pathSeparator =
			// System.getProperty("file.separator");
			// final String targetString;
			//			
			// if (pathSeparator.equals("\\")){
			// //Windows Style
			// targetString = "target\\test-classes";
			// }
			// else{
			// // Linux Style
			// targetString = "target/test-classes";
			// }
			// if (tempFileName.contains(targetString)){
			// final int targetStringLength = targetString.length();
			// final int targetStringPos = tempFileName.indexOf(targetString);
			// final String prefix = tempFileName.substring(0,targetStringPos);
			// final String suffix =
			// tempFileName.substring(targetStringPos+targetStringLength,
			// tempFileName.length());
			// final StringBuffer sb = new StringBuffer(prefix);
			// sb.append("src").append(pathSeparator).append("test").append(pathSeparator).append("resources").append(suffix);
			// tempFileName = sb.toString();
			// }

			// Renaming the tempFile in the proper zipped file required by test
			final StringBuffer destFileName = new StringBuffer(tempFileName
					.substring(0, tempFileName.lastIndexOf("\\") + 1));
			destFileName.append(name);
			tempFile.renameTo(new File(destFileName.toString()));
		}
		// returning the tempFile
		return tempFile;
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
}