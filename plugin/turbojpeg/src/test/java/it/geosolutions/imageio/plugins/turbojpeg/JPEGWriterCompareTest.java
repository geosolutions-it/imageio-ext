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
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.operator.ExtremaDescriptor;
import javax.media.jai.operator.SubtractDescriptor;

import org.junit.Test;
import static org.junit.Assume.*;

import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.sun.imageio.plugins.png.PNGImageReaderSpi;
import com.sun.media.imageioimpl.common.PackageUtil;
import java.util.logging.Logger;

public class JPEGWriterCompareTest extends BaseTest {

    private static final Logger LOGGER = Logger.getLogger(JPEGWriterCompareTest.class.toString());    
    
    static final int LOOP = 30;

    static final long DELAY_MS = 15000;
    
    static final boolean CODEC_LIB_AVAILABLE = PackageUtil.isCodecLibAvailable();

    static void dispose(ByteArrayOutputStream out, FileOutputStream fos) {

        if (out != null) {
            try {
                out.close();
            } catch (Throwable t) {

            }
        }
        if (fos != null) {
            try {
                fos.flush();
                fos.close();
                fos = null;
            } catch (Throwable t) {

            }
        }
    }

    public static java.io.ByteArrayOutputStream encodeImageAsJpeg(RenderedImage image,
            final float quality, final boolean useNative) throws Exception {
        // sanity check, the two writers will emit very odd messages in this case, let's
        // have a human readable one instead
        if (image.getColorModel().hasAlpha()) {
            throw new Exception("Can't write images with alpha band in JPEG "
                    + "format, please use alpha=false with jpeg output");
        }

        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        // use efficient native jai writer
        writeJPEG(image, output, "JPEG", quality, useNative);

        return output;

    }

    private static int write(final int loop, final long delayMs, final BufferedImage buffered,
            final boolean useNative) throws Exception {
        int differences = 0;
        for (int i = 0; i < loop; i++) {
            ByteArrayOutputStream out1 = encodeImageAsJpeg(buffered, 0.75f, useNative);
            Thread.sleep(delayMs);

            ByteArrayOutputStream out2 = encodeImageAsJpeg(buffered, 0.75f, useNative);
            final File file1 = new File(OUTPUT_FOLDER + "outA" + i + ".jpg");
            file1.delete();

            final File file2 = new File(OUTPUT_FOLDER + "outB" + i + ".jpg");
            file2.delete();

            FileOutputStream fos1 = new FileOutputStream(file1);
            FileOutputStream fos2 = new FileOutputStream(file2);
            out1.writeTo(fos1);
            out2.writeTo(fos2);
            dispose(out2, fos2);
            dispose(out1, fos1);

            ImageReaderSpi spi = new JPEGImageReaderSpi();
            ImageReader reader1 = spi.createReaderInstance();
            ImageReader reader2 = spi.createReaderInstance();
            FileImageInputStream fis1 = new FileImageInputStream(file1);
            reader1.setInput(fis1);

            FileImageInputStream fis2 = new FileImageInputStream(file2);
            reader2.setInput(fis2);

            BufferedImage bi1 = reader1.read(0);
            BufferedImage bi2 = reader2.read(0);
            if (!imagesAreEquals(bi1, bi2)) {
                differences++;
            }
            fis1.close();
            fis2.close();
            file1.delete();
            file2.delete();
        }

        return differences;
    }

    private static boolean imagesAreEquals(BufferedImage bi1, BufferedImage bi2) {
        RenderedImage subtractA = SubtractDescriptor.create(bi1, bi2, null);
        double[][] extremaA = (double[][]) ExtremaDescriptor.create(subtractA, null, 1, 1, false,
                1, null).getProperty("Extrema");
        System.out.println("A - B");

        return extremaIsZero(extremaA);
    }

    private static boolean extremaIsZero(double[][] extrema) {
        System.out.println("extrema values: MIN[R,G,B]; MAX[R,G,B] = [" + extrema[0][0] + ","
                + extrema[0][1] + "," + extrema[0][2] + "] ; [" + extrema[1][0] + ","
                + extrema[1][1] + "," + extrema[1][2] + "]");

        if (isZero(extrema[0][0]) && isZero(extrema[0][1]) && isZero(extrema[0][2])
                && isZero(extrema[1][0]) && isZero(extrema[1][1]) && isZero(extrema[1][2])) {
            return true;
        }

        return false;
    }

    private static boolean isZero(double d) {
        return (Math.abs(d - 0) < 1E-9d);
    }

    /**
     * Writes outs the image contained into this {@link ImageWorker} as a JPEG using the provided
     * destination , compression and compression rate.
     * <p>
     * The destination object can be anything providing that we have an {@link ImageOutputStreamSpi}
     * that recognizes it.
     * 
     * @param destination
     *            where to write the internal {@link #image} as a JPEG.
     * @param compression
     *            algorithm.
     * @param compressionRate
     *            percentage of compression.
     * @param nativeAcc
     *            should we use native acceleration.
     * @return this {@link ImageWorker}.
     * @throws IOException
     *             In case an error occurs during the search for an {@link ImageOutputStream} or
     *             during the eoncding process.
     */
    public static final void writeJPEG(final RenderedImage image, final Object destination,
            final String compression, final float compressionRate, final boolean nativeAcc)
            throws IOException {
        ImageWriterSpi spi = nativeAcc ? clibSPI : turboSPI;
        ImageWriter writer = spi.createWriterInstance();

        // Compression is available on both lib
        final ImageWriteParam iwp = writer.getDefaultWriteParam();

        final ImageOutputStream outStream = nativeAcc ? new MemoryCacheImageOutputStream(
                (OutputStream) destination) : new ImageOutputStreamAdapter2((OutputStream) destination);
        
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionType("JPEG");
        iwp.setCompressionQuality(compressionRate); // We can control quality here.
        if (nativeAcc) {
            iwp.setCompressionType(compression); // Lossy compression.

        }
        if (iwp instanceof JPEGImageWriteParam) {
            final JPEGImageWriteParam param = (JPEGImageWriteParam) iwp;
            param.setOptimizeHuffmanTables(true);
            try {
                param.setProgressiveMode(JPEGImageWriteParam.MODE_DEFAULT);
            } catch (UnsupportedOperationException e) {
                throw (IOException) new IOException().initCause(e);
                // TODO: inline cause when we will be allowed to target Java 6.
            }
        }

        try {

            writer.setOutput(outStream);
            writer.write(null, new IIOImage(image, null, null), iwp);

        } finally {
            if (writer != null) {
                try {
                    writer.dispose();
                } catch (Throwable e) {
                    System.out.println(e.getLocalizedMessage());
    
                }
            }
            if (outStream != null) {
                try {
                    ((ImageOutputStream) outStream).close();
                } catch (Throwable e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }

        }
    }

    @Test
    public void writeAsJpeg() throws Exception {
        if (!TestData.isExtensiveTest()){
            LOGGER.info("Skipping compare tests. Use Extensive tests to enable it");
            return;
        }
        
        if (SKIP_TESTS){
            LOGGER.warning(ERROR_LIB_MESSAGE);
            assumeTrue(!SKIP_TESTS);            
            return;
        }
        
        ImageReaderSpi spiReader = new PNGImageReaderSpi();
        ImageReader reader = null;
        File inputFile = TestData.file(this, "testmergb.png");
        FileImageInputStream fis = null;

        try {
            reader = spiReader.createReaderInstance();
            fis = new FileImageInputStream(inputFile);
            reader.setInput(fis);

            BufferedImage buffered = reader.read(0);
            writeAsJpeg(buffered);
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable t) {

                }
            }

            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Throwable t) {

                }
            }
            th.printStackTrace();
        }

        // System.in.read();
    }

    private void writeAsJpeg(final BufferedImage buffered) throws Exception {

        int differencesNative = 0;
        if (CODEC_LIB_AVAILABLE) {
            System.out.println("----------------------------\nTESTING NATIVE WRITER\n----------------------------\n");
            differencesNative = write(LOOP, DELAY_MS, buffered, true);
        } else {
            System.out.println("----------------------------\nSKIPPING NATIVE WRITER\n----------------------------\n");
        }

        System.out.println("----------------------------\nTESTING Not-Native WRITER\n----------------------------\n");

        int differencesNoNative = write(LOOP, DELAY_MS, buffered, false);

        System.out.println(" Doing "+ LOOP + " couples of writes resulted in \n"
                        + (CODEC_LIB_AVAILABLE ? (differencesNative + " difference on outputImage between 2 consecutive writes using the CLIB writer and \n") : "")
                        + differencesNoNative + " difference on outputImage between 2 consecutive writes using the Turbo writer");

    }
}
