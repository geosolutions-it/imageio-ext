package it.geosolutions.imageio.tiff;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.stream.FileImageInputStream;

import it.geosolutions.imageio.plugins.tiff.PrivateTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFField;
import it.geosolutions.imageio.plugins.tiff.TIFFImageReadParam;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFIFD;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageMetadata;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.tiff.gdal.GDALMetadata;
import it.geosolutions.imageioimpl.plugins.tiff.gdal.GDALMetadataParser;
import it.geosolutions.resources.TestData;

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
			// Test Assertions
			assertTrue(subIFD.containsTIFFField(tagNumberExifVersion));
			assertTrue(subIFD.containsTIFFField(tagNumberDateTime));
			assertTrue(subIFD.containsTIFFField(tagNumberCompConfig));
			assertTrue(subIFD.containsTIFFField(tagNumberFlashPix));
			assertTrue(subIFD.containsTIFFField(tagNumberColor));
			assertTrue(subIFD.containsTIFFField(tagNumberXpixelRes));
			assertTrue(subIFD.containsTIFFField(tagNumberYpixelRes));
		} catch (Exception e) {
			// If an exception occurred the logger catch the exception and print
			// the message
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			// Finally, if an exception has been thrown or not, the reader
			// and the input stream are closed
			if(stream0!=null){
				stream0.flush();
				stream0.close();
			}
			
			if (reader != null) {
				reader.dispose();
			}
		}
	}

    @Test
    public void testGDALMetadataLowLevel() throws Exception {
        // Selection of the input file from the TestData directory
        File inputFile = TestData.file(this, "scaleOffset.tif");
        // Instantiation of the read-params
        final TIFFImageReadParam param = new TIFFImageReadParam();
        // Instantiation of the file-reader
        // Creation of the file input stream associated to the selected file
        try (FileImageInputStream stream = new FileImageInputStream(inputFile)) {
            TIFFImageReader reader =
                    (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
            reader.setInput(stream);

            // Reading of the associated metadata
            TIFFImageMetadata metadata = (TIFFImageMetadata) reader.getImageMetadata(0);
            assertNotNull(metadata);

            // check the GDAL metadata tag has been read and has the expected content
            TIFFField gdalMetadata = metadata.getTIFFField(PrivateTIFFTagSet.TAG_GDAL_METADATA);
            assertNotNull(gdalMetadata);
            String xml = gdalMetadata.getAsString(0);

            // check it parses
            GDALMetadata parsed = GDALMetadataParser.parse(xml);
            List<GDALMetadata.Item> items = parsed.getItems();

            // Checking one item to double check it's as expected
            assertEquals("Band_1", items.get(0).getName());
            assertEquals("Max Band_1", items.get(0).getValue());
            assertNull(items.get(0).getSample());
            assertNull(items.get(0).getRole());
        }
    }

    @Test
    public void testGDALMetadataHighLevel() throws Exception {
        // Selection of the input file from the TestData directory
        File inputFile = TestData.file(this, "scaleOffset.tif");
        // Instantiation of the read-params
        final TIFFImageReadParam param = new TIFFImageReadParam();
        // Instantiation of the file-reader
        // Creation of the file input stream associated to the selected file
        try (FileImageInputStream stream = new FileImageInputStream(inputFile)) {
            TIFFImageReader reader =
                    (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
            reader.setInput(stream);

            // Reading of the associated metadata
            TIFFImageMetadata metadata = (TIFFImageMetadata) reader.getImageMetadata(0);
            assertNotNull(metadata);

            // check the GDAL metadata tag has been read and has the expected content
            Double[] expectedScales = new Double[6];
            Arrays.fill(expectedScales, 0.000100000000000000005);
            assertArrayEquals(expectedScales, metadata.getScales());
            Double[] expectedOffsets = new Double[6];
            Arrays.fill(expectedOffsets, 0d);
            assertArrayEquals(expectedOffsets, metadata.getOffsets());
        }
    }
}
