package it.geosolutions.imageio.plugins.turbojpeg;

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Iterator;
import java.util.function.Predicate;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

/** @author etajario */
public abstract class BaseTest {

    static final String INPUT_FILE_PATH = "/tmp/test.tif";

    static final File INPUT_FILE = new File(INPUT_FILE_PATH);

    static RenderedImage SAMPLE_IMAGE = null;

    static boolean SKIP_TESTS = false;

    static final String ERROR_LIB_MESSAGE = "The TurboJpeg native library hasn't been loaded: Skipping test";

    static final String ERROR_FILE_MESSAGE = "The specified input file can't be read: Skipping test";

    static final String OUTPUT_FOLDER = "/tmp" // /media/bigdisk/data/turbojpeg"// System.getProperty("java.io.tmpdir")
            + File.separatorChar;

    static final ImageWriterSpi standardSPI = getDefaultJPEGWriterSpi();

    static final TurboJpegImageWriterSpi turboSPI = new TurboJpegImageWriterSpi();

    static {
        SKIP_TESTS = !TurboJpegUtilities.isTurboJpegAvailable();
    }

    protected static ImageWriterSpi getDefaultJPEGWriterSpi() {
        // Ensure all providers are registered
        IIORegistry registry = IIORegistry.getDefaultInstance();

        // Iterate all available ImageWriterSpis
        Iterator<ImageWriterSpi> spis = registry.getServiceProviders(ImageWriterSpi.class, true);

        while (spis.hasNext()) {
            ImageWriterSpi spi = spis.next();
            for (String format : spi.getFormatNames()) {
                if (format.equalsIgnoreCase("jpeg") || format.equalsIgnoreCase("jpg")) {
                    System.out.println("Found JPEG SPI: " + spi.getClass().getName());
                    return spi;
                }
            }
        }

        throw new RuntimeException("No JPEG ImageWriterSpi found");
    }

    protected static ImageReaderSpi getDefaultReaderSpi(Predicate<ImageReaderSpi> predicate) {
        // Ensure all providers are registered
        IIORegistry registry = IIORegistry.getDefaultInstance();

        // Iterate all available ImageWriterSpis
        Iterator<ImageReaderSpi> spis = registry.getServiceProviders(ImageReaderSpi.class, true);

        while (spis.hasNext()) {
            ImageReaderSpi spi = spis.next();
            if (predicate.test(spi)) {
                return spi;
            }
        }

        throw new RuntimeException("No ImageReaderSPI found matching the predicate");
    }
}
