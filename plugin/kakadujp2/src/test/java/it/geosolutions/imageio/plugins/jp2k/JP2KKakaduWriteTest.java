package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import kdu_jni.KduException;

public class JP2KKakaduWriteTest extends TestCase {

    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2k");

    public JP2KKakaduWriteTest(String name) {
        super(name);
    }

    private final static double lossLessQuality = 1;

    private final static double lossyQuality = 0.02;

    private final static String testPath;

    static {

        String path = System.getProperty("data.path");
        if (path != null) {
            path = path.replace("\\", "/");
            final char lastChar = path.charAt(path.length() - 1);
            if (lastChar == '/')
                testPath = path;
            else
                testPath = path + "/";
        } else
            testPath = "C:/";
    }

    private final static String[] files = new String[] { "IM-0001-30023.bmp",
            "IM-0001-0008.bmp", "IM-0001-0010.bmp", "IM-0001-0014.bmp",
            "OT-MONO2-8-hip.bmp", "MR-MONO2-8-16x-heart (12).bmp",
            "8-bit Uncompressed Gray.bmp", };

    private final static String inputFileName = testPath;

    // private final static String inputFileName12bit = testPath + "test1.jp2";

    private final static String outputFileName = testPath + "/out/";

    public void testKakaduWriter() throws KduException, FileNotFoundException,
            IOException {

        for (String fileName : files) {
            final String filePath = inputFileName + fileName;
            final File file = new File(filePath);
            if (!file.exists()) {
                LOGGER.warning("Unable to find the file " + filePath
                        + "\n This test will be skipped");
                continue;
            }

            final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                    "ImageRead");
            ImageReader reader = ImageIO.getImageReaders(
                    ImageIO.createImageInputStream(file)).next();

            pbjImageRead.setParameter("reader", reader);
            pbjImageRead.setParameter("Input", file);
            RenderedOp image = JAI.create("ImageRead", pbjImageRead);

            final String suffix = fileName.substring(0, fileName.length() - 4);
            write(outputFileName + "_" + suffix, image, true, lossLessQuality);
            write(outputFileName + "_" + suffix, image, false, lossLessQuality);
            write(outputFileName + "_" + suffix, image, true, lossyQuality);
            write(outputFileName + "_" + suffix, image, false, lossyQuality);
            write(outputFileName + "_JAI" + suffix, image, true,
                    lossLessQuality, true);
            write(outputFileName + "_JAI" + suffix, image, false,
                    lossLessQuality, true);
            write(outputFileName + "_JAI" + suffix, image, true, lossyQuality,
                    true);
            write(outputFileName + "_JAI" + suffix, image, false, lossyQuality,
                    true);
        }
    }

    public void testKakaduWriterParam() throws KduException,
            FileNotFoundException, IOException {

        final String fileName = files[0];
        final String filePath = inputFileName + fileName;
        final File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.warning("Unable to find the file " + filePath
                    + "\n This test will be skipped");
            return;
        }

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                "ImageRead");
        ImageReader reader = ImageIO.getImageReaders(
                ImageIO.createImageInputStream(file)).next();

        pbjImageRead.setParameter("reader", reader);
        pbjImageRead.setParameter("Input", file);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);

        final String suffix = fileName.substring(0, fileName.length() - 4);
        JP2KKakaduImageWriteParam param = new JP2KKakaduImageWriteParam();
        param.setSourceRegion(new Rectangle(100, 0, 450, 800));
        param.setSourceSubsampling(2, 3, 0, 0);

        write(outputFileName + "_pp" + suffix, image, true, lossLessQuality,
                param);
        write(outputFileName + "_pp" + suffix, image, false, lossLessQuality,
                param);
        write(outputFileName + "_pp" + suffix, image, true, lossyQuality, param);
        write(outputFileName + "_pp" + suffix, image, false, lossyQuality,
                param);
        write(outputFileName + "_pp_JAI" + suffix, image, true,
                lossLessQuality, true, param);
        write(outputFileName + "_pp_JAI" + suffix, image, false,
                lossLessQuality, true, param);
        write(outputFileName + "_pp_JAI" + suffix, image, true, lossyQuality,
                true, param);
        write(outputFileName + "_pp_JAI" + suffix, image, false, lossyQuality,
                true, param);

    }

    private static synchronized void write(String file, RenderedImage bi,
            boolean codeStreamOnly, double quality, boolean useJAI,
            JP2KKakaduImageWriteParam addParam) throws IOException {
        file += "_Q" + quality + "_" + (codeStreamOnly ? ".j2c" : ".jp2");
        final ImageOutputStream outputStream = ImageIO
                .createImageOutputStream(new File(file));
        JP2KKakaduImageWriteParam param = new JP2KKakaduImageWriteParam();
        param.setQuality(quality);
        param.setWriteCodeStreamOnly(codeStreamOnly);

        if (addParam != null) {
            param.setSourceRegion(addParam.getSourceRegion());
            param.setSourceSubsampling(addParam.getSourceXSubsampling(),
                    addParam.getSourceYSubsampling(), addParam
                            .getSubsamplingXOffset(), addParam
                            .getSubsamplingYOffset());
        }

        if (!useJAI) {
            final ImageWriter writer = new JP2KKakaduImageWriterSpi()
                    .createWriterInstance();

            writer.setOutput(outputStream);
            writer.write(null, new IIOImage(bi, null, null), param);
            writer.dispose();
        } else {
            final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
                    "ImageWrite");

            final ImageWriter writer = new JP2KKakaduImageWriterSpi()
                    .createWriterInstance();
            pbjImageWrite.setParameter("writer", writer);
            pbjImageWrite.setParameter("output", outputStream);
            pbjImageWrite.setParameter("writeParam", param);
            pbjImageWrite.addSource(bi);
            RenderedOp image = JAI.create("ImageWrite", pbjImageWrite);
        }
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(new JP2KKakaduWriteTest("testKakaduWriter"));

        suite.addTest(new JP2KKakaduWriteTest("testKakaduWriterParam"));

        suite.addTest(new JP2KKakaduWriteTest("test12BitGray"));

        suite.addTest(new JP2KKakaduWriteTest("test16BitGray"));

        suite.addTest(new JP2KKakaduWriteTest("test24BitGray"));

        // suite.addTest(new JP2KKakaduWriteTest("test12BitProvided"));

        suite.addTest(new JP2KKakaduWriteTest("testPalettedRGB"));

        // suite.addTest(new JP2KKakaduWriteTest("testImageIOJP2KWriter"));

        return suite;
    }

    // public static void test12BitProvided() throws IOException {
    // JP2KKakaduImageReader reader = (JP2KKakaduImageReader) new
    // JP2KKakaduImageReaderSpi()
    // .createReaderInstance();
    // reader.setInput(new File(inputFileName12bit));
    // BufferedImage bi = reader.read(0);
    // write(outputFileName + "_bart12", bi, true, lossLessQuality);
    // write(outputFileName + "_bart12", bi, false, lossLessQuality);
    // write(outputFileName + "_bart12", bi, true, lossyQuality);
    // write(outputFileName + "_bart12", bi, false, lossyQuality);
    // write(outputFileName + "_JAI_bart12", bi, true, lossLessQuality, true);
    // write(outputFileName + "_JAI_bart12", bi, false, lossLessQuality, true);
    // write(outputFileName + "_JAI_bart12", bi, true, lossyQuality, true);
    // write(outputFileName + "_JAI_bart12", bi, false, lossyQuality, true);
    // }

    public static void test12BitGray() throws IOException {
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel cm = new ComponentColorModel(cs, new int[] { 12 }, false,
                false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        final int w = 512;
        final int h = 512;
        SampleModel sm = cm.createCompatibleSampleModel(w, h);
        final int bufferSize = w * h;
        final short[] bufferValues = new short[bufferSize];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++)
                bufferValues[j + (i * h)] = (short) ((j + i) * (4096 / 1024));
        }
        DataBuffer imageBuffer = new DataBufferUShort(bufferValues, bufferSize);
        BufferedImage bi = new BufferedImage(cm, Raster.createWritableRaster(
                sm, imageBuffer, null), false, null);
        write(outputFileName + "_gray12", bi, true, lossLessQuality);
        write(outputFileName + "_gray12", bi, false, lossLessQuality);
        write(outputFileName + "_gray12", bi, true, lossyQuality);
        write(outputFileName + "_gray12", bi, false, lossyQuality);
        write(outputFileName + "_JAI_gray12", bi, true, lossLessQuality, true);
        write(outputFileName + "_JAI_gray12", bi, false, lossLessQuality, true);
        write(outputFileName + "_JAI_gray12", bi, true, lossyQuality, true);
        write(outputFileName + "_JAI_gray12", bi, false, lossyQuality, true);
    }

    public static void test16BitGray() throws IOException {
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel cm = new ComponentColorModel(cs, new int[] { 16 }, false,
                false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        final int w = 512;
        final int h = 512;
        SampleModel sm = cm.createCompatibleSampleModel(w, h);
        final int bufferSize = w * h;
        final short[] bufferValues = new short[bufferSize];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++)
                bufferValues[j + (i * h)] = (short) ((j + i) * (65536 / 1024));
        }
        DataBuffer imageBuffer = new DataBufferUShort(bufferValues, bufferSize);
        BufferedImage bi = new BufferedImage(cm, Raster.createWritableRaster(
                sm, imageBuffer, null), false, null);
        write(outputFileName + "_gray16", bi, true, lossLessQuality);
        write(outputFileName + "_gray16", bi, false, lossLessQuality);
        write(outputFileName + "_gray16", bi, true, lossyQuality);
        write(outputFileName + "_gray16", bi, false, lossyQuality);
        write(outputFileName + "_JAI_gray16", bi, true, lossLessQuality, true);
        write(outputFileName + "_JAI_gray16", bi, false, lossLessQuality, true);
        write(outputFileName + "_JAI_gray16", bi, true, lossyQuality, true);
        write(outputFileName + "_JAI_gray16", bi, false, lossyQuality, true);

    }

    public static void test24BitGray() throws IOException {
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel cm = new ComponentColorModel(cs, new int[] { 24 }, false,
                false, Transparency.OPAQUE, DataBuffer.TYPE_INT);
        final int w = 512;
        final int h = 512;
        SampleModel sm = cm.createCompatibleSampleModel(w, h);
        final int bufferSize = w * h;
        final int[] bufferValues = new int[bufferSize];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++)
                bufferValues[j + (i * h)] = (int) (j + i) * (16777216 / 1024);
        }
        DataBuffer imageBuffer = new DataBufferInt(bufferValues, bufferSize);
        BufferedImage bi = new BufferedImage(cm, Raster.createWritableRaster(
                sm, imageBuffer, null), false, null);

        write(outputFileName + "_gray24", bi, true, lossLessQuality);
        write(outputFileName + "_gray24", bi, false, lossLessQuality);
        write(outputFileName + "_gray24", bi, true, lossyQuality);
        write(outputFileName + "_gray24", bi, false, lossyQuality);
        write(outputFileName + "_JAI_gray24", bi, true, lossLessQuality, true);
        write(outputFileName + "_JAI_gray24", bi, false, lossLessQuality, true);
        write(outputFileName + "_JAI_gray24", bi, true, lossyQuality, true);
        write(outputFileName + "_JAI_gray24", bi, false, lossyQuality, true);
    }

    public void testPalettedRGB() throws IOException {
        BufferedImage bi = ImageIO.read(TestData.file(this, "paletted.tif"));
        write(outputFileName + "_RGB8", bi, true, lossLessQuality);
        write(outputFileName + "_RGB8", bi, false, lossLessQuality);
        write(outputFileName + "_JAI_RGB8", bi, true, lossLessQuality, true);
        write(outputFileName + "_JAI_RGB8", bi, false, lossLessQuality, true);
    }

    private static synchronized void write(String file, final RenderedImage bi,
            final boolean codeStreamOnly, final double quality,
            JP2KKakaduImageWriteParam param) throws IOException {
        write(file, bi, codeStreamOnly, quality, false, param);

    }

    private static synchronized void write(String file, final RenderedImage bi,
            final boolean codeStreamOnly, final double quality)
            throws IOException {
        write(file, bi, codeStreamOnly, quality, false);
    }

    private static synchronized void write(String file, final RenderedImage bi,
            final boolean codeStreamOnly, final double quality,
            final boolean useJAI) throws IOException {
        write(file, bi, codeStreamOnly, quality, useJAI, null);
    }
}
