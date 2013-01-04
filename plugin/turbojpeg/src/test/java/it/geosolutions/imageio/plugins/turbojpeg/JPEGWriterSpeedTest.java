/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.turbojpeg;

import it.geosolutions.imageio.utilities.ImageOutputStreamAdapter2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assume.*;

public class JPEGWriterSpeedTest extends BaseTest {

    private static final Logger LOGGER = Logger.getLogger(JPEGWriterSpeedTest.class.toString());    
    
    private static final int LOOP = 20;
    
    static {
        if (SAMPLE_IMAGE != null){
            LOGGER.info("If enabled, tests are made of " + LOOP + " iterations on top of a "
                    + SAMPLE_IMAGE.getWidth() + "*" + SAMPLE_IMAGE.getHeight() + " ("
                    + SAMPLE_IMAGE.getSampleModel().getNumBands() + " bands) image");
        }
    }
    
    @Test
    @Ignore
    public void testJPEGCLIB() throws FileNotFoundException, IOException, SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        assumeTrue(!SKIP_TESTS);            
        
        String fileName = null;
        ImageOutputStream out1 = null;
        
        try {

            ImageWriterSpi spi = clibSPI;
            fileName = OUTPUT_FOLDER
                    + ((SAMPLE_IMAGE.getSampleModel().getNumBands() == 1) ? "GRAY" : "RGB")
                    + "CLIBoutput.jpeg";
            final File file = new File(fileName);
            out1 = new FileImageOutputStream(file);

            ImageWriter writer1 = spi.createWriterInstance();
            ImageWriteParam param = writer1.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.75f);
            
            writer1.setOutput(out1);
            writer1.write(null, new IIOImage(SAMPLE_IMAGE, null, null), param);
            out1.close();
            writer1.dispose();

            // Writing loops
            long start = System.nanoTime();
            for (int i = 0; i < LOOP; i++) {
                // Startup write
                out1 = new FileImageOutputStream(file);
                writer1 = spi.createWriterInstance();
                writer1.setOutput(out1);
                writer1.write(null, new IIOImage(SAMPLE_IMAGE, null, null), param);
                out1.close();
                writer1.dispose();
            }

            long end = System.nanoTime();
            long total = end - start;
            reportTime("Clib", total, LOOP);

        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(t.getLocalizedMessage());
            }
        } finally {
            if (out1 != null) {
                try {
                    out1.close();
                } catch (Throwable t) {
                    //
                }
            }
        }
    }

    @Test
    @Ignore
    public void testJPEGJDK() throws FileNotFoundException, IOException, SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        assumeTrue(!SKIP_TESTS);
        
        String fileName = null;
        ImageOutputStream out1 = null;
        try {

            ImageWriterSpi spi = standardSPI;
            fileName = OUTPUT_FOLDER
                    + ((SAMPLE_IMAGE.getSampleModel().getNumBands() == 1) ? "GRAY" : "jdkRGB")
                    + "METAoutput.jpeg";
            final File file = new File(fileName);
            out1 = new FileImageOutputStream(file);

            ImageWriter writer1 = spi.createWriterInstance();
            ImageWriteParam param = writer1.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.75f);
            writer1.setOutput(out1);
            writer1.write(null, new IIOImage(SAMPLE_IMAGE, null, null), param);
            out1.close();
            writer1.dispose();

            // Writing loops
            long start = System.nanoTime();
            for (int i = 0; i < LOOP; i++) {
                // Startup write
                out1 = new FileImageOutputStream(file);
                writer1 = spi.createWriterInstance();
                writer1.setOutput(out1);
                writer1.write(null, new IIOImage(SAMPLE_IMAGE, null, null), param);
                out1.close();
                writer1.dispose();
            }

            long end = System.nanoTime();
            long total = end - start;
            reportTime("JDK", total, LOOP);
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(t.getLocalizedMessage());
            }
        } finally {
            if (out1 != null) {
                try {
                    out1.close();
                } catch (Throwable t) {
                    //
                }
            }
        }
    }

    @Test
    @Ignore
    public void testJPEGTurbo() throws FileNotFoundException, IOException, SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        assumeTrue(!SKIP_TESTS);
        
        if (!TurboJpegUtilities.isTurboJpegAvailable()) {
            LOGGER.warning(ERROR_LIB_MESSAGE);
            return;
        }
        
        ImageWriterSpi spi = turboSPI;
        String fileName = null;
        OutputStream os = null;
        ImageOutputStream out1 = null;
            
        TurboJpegImageWriteParam param = new TurboJpegImageWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.75f);
//        EXIFMetadata exif = initExif();
//        param.setExif(exif);
        
        try {

            fileName = OUTPUT_FOLDER
                    + ((SAMPLE_IMAGE.getSampleModel().getNumBands() == 1) ? "GRAY"
                            : "RGBTurbo") + INPUT_FILE.getName() + ".jpeg";
            final File file = new File(fileName);
            os = new FileOutputStream(file);
            out1 = new ImageOutputStreamAdapter2(os);

            
            TurboJpegImageWriter writer1 = (TurboJpegImageWriter) spi.createWriterInstance();
            writer1.setOutput(out1);
            writer1.write(null, new IIOImage(SAMPLE_IMAGE, null, null), param);
            out1.close();
            writer1.dispose();

            // Writing loops
            long start = System.nanoTime();
            for (int i = 0; i < LOOP; i++) {
                // Startup write
                os = new FileOutputStream(file);
                out1 = new ImageOutputStreamAdapter2(os);
                writer1 = (TurboJpegImageWriter) spi.createWriterInstance();
                writer1.setOutput(out1);
                writer1.write(null, new IIOImage(SAMPLE_IMAGE, null, null), param);
                out1.close();
                writer1.dispose();
            }

            long end = System.nanoTime();
            long total = end - start;
            reportTime("Turbo", total, LOOP);

        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(t.getLocalizedMessage());
            }
        } finally {
            if (out1 != null) {
                try {
                    out1.close();
                } catch (Throwable t) {
                    //
                }
            }
        }
    }

    /**
     * @param total
     */
    protected static void reportTime(String encoder, long total, final int LOOP) {
        LOGGER.info("JPEG " + encoder + " TOTAL TIME = " + ((total) / 1000000)
                + "(ms) ; AVERAGE TIME = " + (total / (1000 * LOOP)) + "(micros)");

    }
    
}
