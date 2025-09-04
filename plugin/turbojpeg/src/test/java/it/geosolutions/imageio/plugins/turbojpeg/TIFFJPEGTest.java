package it.geosolutions.imageio.plugins.turbojpeg;

import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.resources.TestData;
import org.junit.Test;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileImageInputStream;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class TIFFJPEGTest {

    /** Logger used for recording any possible exception */
    private final static Logger logger = Logger.getLogger(TIFFJPEGTest.class.getName());


    @Test
    public void readTIFFTurboJpegNoJpegTables() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // This image has been created with this command on GDAL 2.1.3:
        // gdal_translate -co COMPRESS=JPEG -CO TILED=YES -CO JPEGTABLESMODE=0\
        // -CO BLOCKXSIZE=64 -CO BLOCKYSIZE=64 -outsize 256 256 -r bilinear test.tif notables.tif

        // This will create a TIFF with internally compressed JPEG images but no JPEGTables metadata
        // TurboJPEG Reader decodes byte array provided by the compressor
        if (!TurboJpegUtilities.isTurboJpegAvailable()) {
            logger.warning("Unable to find native libs. Tests are skipped");
            assumeTrue(false);
            return;
        }
        final File file = TestData.file(this, "notables.tif");

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
        FileImageInputStream fis = null;
        BufferedImage image = null;

        try {
            fis = new FileImageInputStream(file);
            reader.setInput(fis);
            ImageReadParam param = new ImageReadParam();
            param.setSourceRegion(new Rectangle(0,0,64,64));
            image = reader.read(0, param);

            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
            assertEquals(1, image.getSampleModel().getNumBands());

//            // Using reflection to check the data array being used
//            Field f = reader.getClass().getDeclaredField("decompressor");
//            f.setAccessible(true);
//            TIFFJPEGDecompressor decompressor = (TIFFJPEGDecompressor) f.get(reader);
//
//            f = decompressor.getClass().getDeclaredField("JPEGReader");
//            f.setAccessible(true);
//            TurboJpegImageReader jpegReader = (TurboJpegImageReader) f.get(decompressor);
//
//            f = jpegReader.getClass().getDeclaredField("data");
//            f.setAccessible(true);
//            byte[] data = (byte[]) f.get(jpegReader);
//
//            // Before the fix, the data array would have been, more or less, big as
//            // the whole stream content (almost 16000), making this check fail.
//            assertTrue(data.length < 300);
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
