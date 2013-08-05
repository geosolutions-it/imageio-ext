package it.geosolutions.imageio.tiff;

import static org.junit.Assert.*;
import it.geosolutions.imageio.plugins.tiff.TIFFImageReadParam;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFIFD;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageMetadata;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.resources.TestData;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageInputStream;
import org.junit.Test;

public class TIFFMetadataTest {
	/** Logger used for recording any possible exception */
	private Logger logger = Logger.getLogger(TIFFMetadataTest.class.getName());

	/**
	 * This test class is used for testing the capability of the TIFFImageReader
	 * class to read EXIF metadata. TIFFImageReader class presented a bug when
	 * the EXIF IFD pointer tag type was not LONG (type = 4) but was the new
	 * IFD_POINTER type (type = 13) defined in the Technical Note 1 of the TIFF
	 * Specification Supplement documentation. This variation provoked a
	 * ClassCastException when the reader tried to cast the EXIF IFD pointer
	 * data to the TIFFIFD class. This bug has been fixed by adding the
	 * possibility to contain the IFD_POINTER type to the EXIF IFD pointer tag.
	 * The testImageRead() method reads an image with the new TIFF tag type and
	 * checks if the EXIF tags has been read by the TIFFImageReader.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testImageRead() throws IOException {
		// Selection of the input file from the TestData directory
		File inputFile = TestData.file(this, "test_IFD.tif");
		// Instantiation of the read-params
		final TIFFImageReadParam param = new TIFFImageReadParam();
		// Instantiation of the file-reader
		TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
				.createReaderInstance();
		// Creation of the file input stream associated to the selected file
		FileImageInputStream stream0 = new FileImageInputStream(inputFile);
		try {
			// Setting the inputstream to the reader
			reader.setInput(stream0);
			// Reading of the image
			RenderedImage img = reader.read(0, param);
			// Reading of the associated metadata
			TIFFImageMetadata metadata = (TIFFImageMetadata) reader
					.getImageMetadata(0);
			// Check if the Exif pointer metadata is present
			int tagPointer = 34665;
			boolean fieldPointer = metadata.getRootIFD().containsTIFFField(tagPointer);
			assertTrue(fieldPointer);
			// Selection of the subIFD associated to the exif pointer
			TIFFIFD subIFD = (TIFFIFD) metadata.getTIFFField(tagPointer).getData();
			// Selection of the tags associated to the EXIF pointer
			int tagNumberExifVersion = 36864;
			int tagNumberDateTime = 36868;
			int tagNumberCompConfig = 37121;
			int tagNumberFlashPix = 40960;
			int tagNumberColor = 40961;
			int tagNumberXpixelRes = 40962;
			int tagNumberYpixelRes = 40963;
			// Boolean for checking if the EXIF tags are present
			boolean exifVersion = subIFD.containsTIFFField(tagNumberExifVersion);
			boolean dateTime = subIFD.containsTIFFField(tagNumberDateTime);
			boolean compConfig = subIFD.containsTIFFField(tagNumberCompConfig);
			boolean flashPix = subIFD.containsTIFFField(tagNumberFlashPix);
			boolean xPixelRes = subIFD.containsTIFFField(tagNumberColor);
			boolean yPixelRes = subIFD.containsTIFFField(tagNumberXpixelRes);
			boolean colorSpace = subIFD.containsTIFFField(tagNumberYpixelRes);
			// Test Assertions
			assertTrue(exifVersion);
			assertTrue(dateTime);
			assertTrue(compConfig);
			assertTrue(flashPix);
			assertTrue(xPixelRes);
			assertTrue(yPixelRes);
			assertTrue(colorSpace);
		} catch (Exception e) {
			// If an exception occurred the logger catch the exception and print
			// the message
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			// Finally, if an exception has been thrown or not, the reader is
			// closed
			if (reader != null) {
				reader.dispose();
			}
		}
	}
}
