package it.geosolutions.imageio.plugins.turbojpeg;

import com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.logging.Logger;
import org.junit.Before;

/**
 *
 * @author etajario
 */
public abstract class BaseTest {        
    
    static final String INPUT_FILE_PATH = "/tmp/test.tif";

    static final File INPUT_FILE = new File(INPUT_FILE_PATH);

    static RenderedImage SAMPLE_IMAGE = null;

    static boolean SKIP_TESTS = false;
    
    static final String ERROR_LIB_MESSAGE = "The TurboJpeg native library hasn't been loaded: Skipping test";

    static final String ERROR_FILE_MESSAGE = "The specified input file can't be read: Skipping test";
    
    static final String OUTPUT_FOLDER = "/tmp" // /media/bigdisk/data/turbojpeg"// System.getProperty("java.io.tmpdir")
            + File.separatorChar;

    static final CLibJPEGImageWriterSpi clibSPI = new CLibJPEGImageWriterSpi();

    static final JPEGImageWriterSpi standardSPI = new JPEGImageWriterSpi();

    static final TurboJpegImageWriterSpi turboSPI = new TurboJpegImageWriterSpi();
    

    static {
        SKIP_TESTS = !TurboJpegUtilities.isTurboJpegAvailable();
    }
    
}
