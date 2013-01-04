package it.geosolutions.imageio.plugins.turbojpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import org.junit.Test;

public class JPEGReaderTest {

    private static final Logger LOGGER = Logger.getLogger(JPEGReaderTest.class.toString());

    private static final String FILENAME = "test.jpg";

    private static final String FILENAMEGRAY = "testgray.jpg";

    @Test
    public void readManual() throws Exception {
        final File file = TestData.file(this, FILENAME);
        final File fileGray = TestData.file(this, FILENAMEGRAY);
        if (!TurboJpegUtilities.isTurboJpegAvailable()) {
            LOGGER.warning("Unable to find native libs. Tests are skipped");
            assumeTrue(false);
            return;
        }

        final ImageReader reader = new TurboJpegImageReaderSpi().createReaderInstance();

        FileImageInputStream fis = null;
        BufferedImage image = null;

        // //
        // Read RGB image
        // //
        try {
            fis = new FileImageInputStream(file);
            reader.setInput(fis);
            image = reader.read(0, null);
            assertEquals(227, image.getWidth());
            assertEquals(103, image.getHeight());
            assertEquals(3, image.getSampleModel().getNumBands());

            if (TestData.isInteractiveTest()) {
                ImageIOUtilities.visualize(image, "testManualRead");
                Thread.sleep(1000);
            } else {
                assertNotNull(image.getData());
            }

            image.flush();
            image = null;
        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable t) {
                    // Does nothing
                }
            }
        }

        // //
        // Read GRAY image
        // //
        try {
            fis = new FileImageInputStream(fileGray);

            reader.setInput(fis);
            image = reader.read(0, null);
            assertEquals(227, image.getWidth());
            assertEquals(103, image.getHeight());
            assertEquals(1, image.getSampleModel().getNumBands());

            if (TestData.isInteractiveTest()) {
                ImageIOUtilities.visualize(image, "testManualRead");
                Thread.sleep(1000);
            } else {
                assertNotNull(image.getData());
            }

            image.flush();
            image = null;

        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable t) {
                    // Does nothing
                }
            }
        }
    }
}
