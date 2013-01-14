/*
 * =========================================================================
 * This file is part of NITRO
 * =========================================================================
 * 
 * (C) Copyright 2004 - 2010, General Dynamics - Advanced Information Systems
 * 
 * NITRO is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, If not,
 * see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.imageio.plugins.nitronitf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.awt.image.SunWritableRaster;

public class ImageIOUtils {

    private static final Log log = LogFactory.getLog(ImageIOUtils.class);

    private ImageIOUtils() {
    }

    /**
     * Returns an ImageReader given the input filename
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public static ImageReader getImageReader(String filename) throws IOException {
        return getImageReader(new File(filename));
    }

    /**
     * Returns an ImageReader given the input file
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static ImageReader getImageReader(File file) throws IOException {
        String ext = FilenameUtils.getExtension(file.getName().toLowerCase());
        ImageReader reader = null;

        Iterator<ImageReader> imageReaders = ImageIO.getImageReadersBySuffix(ext);
        if (imageReaders.hasNext()) {
            reader = imageReaders.next();
            ImageInputStream stream = ImageIO.createImageInputStream(file);
            reader.setInput(stream);
        }
        return reader;
    }

    /**
     * Returns an ImageReader given the format, and sets the input source
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static ImageReader getImageReader(String format, Object input) throws IOException {
        ImageReader reader = null;
        Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(format);
        if (imageReaders.hasNext()) {
            reader = imageReaders.next();
            reader.setInput(input);
        }
        return reader;
    }

    /**
     * Returns an ImageWriter given the output filename
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public static ImageWriter getImageWriter(String filename) throws IOException {
        return getImageWriter(new File(filename));
    }

    /**
     * Returns an ImageWriter given the input file
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static ImageWriter getImageWriter(File file) throws IOException {
        String ext = FilenameUtils.getExtension(file.getName().toLowerCase());
        ImageWriter writer = null;

        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersBySuffix(ext);
        if (imageWriters.hasNext()) {
            writer = imageWriters.next();
            ImageOutputStream stream = ImageIO.createImageOutputStream(file);
            writer.setOutput(stream);
        }
        return writer;
    }

    public static String getPackageName(Class clazz) {
        // we can cheat and use the FilenameUtils to remove the class name
        return FilenameUtils.removeExtension(clazz.getCanonicalName());
    }

    public static boolean canDisplay(BufferedImage image) {
        log.info("Data Type: " + image.getSampleModel().getDataType());
        log.info("Pixel Size: " + image.getColorModel().getPixelSize());

        if (image.getSampleModel().getDataType() == DataBuffer.TYPE_FLOAT
                && image.getColorModel().getPixelSize() == 64)
            return false;
        return !GraphicsEnvironment.isHeadless();
    }

    /**
     * Returns a list of Files contained in the given String array of files or directories. If one of the array contents is a directory, it searches
     * it. Files ending in the extensions provided are returned in the list.
     * 
     * @param filesOrDirs
     * @param extensions
     * @return
     */
    public static List<File> getFiles(String[] filesOrDirs, String[] extensions) {
        List<File> files = new ArrayList<File>();
        final String[] exts = extensions;
        for (int i = 0; i < filesOrDirs.length; i++) {
            String arg = filesOrDirs[i];
            File file = new File(arg);
            if (file.isDirectory() && file.exists()) {
                files.addAll(Arrays.asList(file.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return ArrayUtils.contains(exts,
                                FilenameUtils.getExtension(name.toLowerCase()));
                    }
                })));
            } else
                files.add(file);
        }
        return files;
    }

    public static JFrame showImage(BufferedImage image, String title) {
        return showImage(image, title, true);
    }

    public static JFrame showImage(BufferedImage image, String title, boolean fitToScreen) {
        JFrame frame = new JFrame(title != null ? title : "");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Image im = image;

        if (fitToScreen) {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int imageHeight = image.getHeight();
            int imageWidth = image.getWidth();
            if (imageHeight > screen.height || imageWidth > screen.width) {
                double hRatio = (imageHeight - screen.height) / screen.height;
                double wRatio = (imageWidth - screen.width) / screen.width;

                int w = -1;
                int h = -1;

                if (hRatio > wRatio)
                    h = screen.height;
                else
                    w = screen.width;
                im = image.getScaledInstance(w, h, Image.SCALE_DEFAULT);
            }
        }

        JLabel label = new JLabel(new ImageIcon(im));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        centerWindow(frame);
        frame.setVisible(true);
        return frame;
    }

    public static void centerWindow(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = w.getSize();
        if (window.width == 0)
            return;
        int left = screen.width / 2 - window.width / 2;
        int top = (screen.height - window.height) / 4;
        if (top < 0)
            top = 0;
        w.setLocation(left, top);
    }

    public static ColorModel createGrayscaleColorModel(boolean invert) {
        byte[] rLUT = new byte[256];
        byte[] gLUT = new byte[256];
        byte[] bLUT = new byte[256];
        if (invert)
            for (int i = 0; i < 256; i++) {
                rLUT[255 - i] = (byte) i;
                gLUT[255 - i] = (byte) i;
                bLUT[255 - i] = (byte) i;
            }
        else {
            for (int i = 0; i < 256; i++) {
                rLUT[i] = (byte) i;
                gLUT[i] = (byte) i;
                bLUT[i] = (byte) i;
            }
        }
        return (new IndexColorModel(8, 256, rLUT, gLUT, bLUT));
    }

    public static float[] findMinAndMax(float[] buffer, int pixelStride, int numBands) {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int i = 0; i < buffer.length; i += numBands) {
            for (int j = 0; j < pixelStride; ++j) {
                float value = buffer[i + j];
                if (!Float.isInfinite(value)) {
                    if (value < min)
                        min = value;
                    if (value > max)
                        max = value;
                }
            }
        }
        return new float[] { min, max };
    }

    public static double[] findMinAndMax(double[] buffer, int pixelStride, int numBands) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < buffer.length; i += numBands) {
            for (int j = 0; j < pixelStride; ++j) {
                double value = buffer[i + j];
                if (!Double.isInfinite(value)) {
                    if (value < min)
                        min = value;
                    if (value > max)
                        max = value;
                }
            }
        }
        return new double[] { min, max };
    }

    public static int[] findMinAndMax(short[] buffer, int pixelStride, int numBands) {
        int min = 65535;
        int max = 0;
        for (int i = 0; i < buffer.length; i += numBands) {
            for (int j = 0; j < pixelStride; ++j) {
                short value = buffer[i + j];
                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
            }
        }
        return new int[] { min, max };
    }

    /**
     * Returns a generic banded WritableRaster
     * 
     * @param numElems
     * @param numLines
     * @param bandOffsets
     * @param dataType
     * @return
     */
    public static WritableRaster makeGenericBandedWritableRaster(int numElems, int numLines,
            int numBands, int dataType) {
        int[] bandOffsets = new int[numBands];
        for (int i = 0; i < numBands; ++i)
            bandOffsets[i] = i;

        DataBuffer d = null;
        if (dataType == DataBuffer.TYPE_BYTE)
            d = new DataBufferByte(numElems * numLines * numBands);
        else if (dataType == DataBuffer.TYPE_FLOAT)
            d = new DataBufferFloat(numElems * numLines * numBands);
        else
            throw new IllegalArgumentException("Invalid datatype: " + dataType);

        BandedSampleModel bsm = new BandedSampleModel(dataType, numElems, numLines,
                bandOffsets.length, bandOffsets, bandOffsets);

        SunWritableRaster ras = new SunWritableRaster(bsm, d, new Point(0, 0));
        return ras;
    }

    /**
     * Returns a generic pixel interleaved WritableRaster
     * 
     * @param numElems
     * @param numLines
     * @param bandOffsets
     * @param dataType
     * @return
     */
    public static WritableRaster makeGenericPixelInterleavedWritableRaster(int numElems,
            int numLines, int numBands, int dataType) {
        int[] bandOffsets = new int[numBands];
        for (int i = 0; i < numBands; ++i)
            bandOffsets[i] = i;

        DataBuffer d = null;
        if (dataType == DataBuffer.TYPE_BYTE)
            d = new DataBufferByte(numElems * numLines * numBands);
        else if (dataType == DataBuffer.TYPE_SHORT)
            d = new DataBufferShort(numElems * numLines * numBands);
        else if (dataType == DataBuffer.TYPE_USHORT)
            d = new DataBufferUShort(numElems * numLines * numBands);
        else if (dataType == DataBuffer.TYPE_FLOAT)
            d = new DataBufferFloat(numElems * numLines * numBands);
        else if (dataType == DataBuffer.TYPE_DOUBLE)
            d = new DataBufferDouble(numElems * numLines * numBands);
        else
            throw new IllegalArgumentException("Invalid datatype: " + dataType);

        PixelInterleavedSampleModel pism = new PixelInterleavedSampleModel(dataType, numElems,
                numLines, bandOffsets.length, numElems * bandOffsets.length, bandOffsets);

        SunWritableRaster ras = new SunWritableRaster(pism, d, new Point(0, 0));
        return ras;
    }

    /**
     * Converts the float data to byte data, and sets the values in the byteData buffer
     * 
     * @param floatData
     * @param byteData
     */
    public static void floatToByteBuffer(float[] floatData, byte[] byteData, int pixelStride,
            int numBands) {
        float[] minMax = ImageIOUtils.findMinAndMax(floatData, pixelStride, numBands);
        float scale = 255f / (minMax[1] - minMax[0]);
        for (int i = 0, j = 0; i < floatData.length; i += numBands, j++) {
            float val = floatData[i] - minMax[0];
            if (val < 0.0f)
                val = 0.0f;
            int iVal = (int) (val * scale);
            if (iVal > 255)
                iVal = 255;
            byteData[j] = (byte) iVal;
        }
    }

    /**
     * Converts the float data to byte data, and sets the values in the byteData buffer
     * 
     * @param doubleData
     * @param byteData
     */
    public static void doubleToByteBuffer(double[] doubleData, byte[] byteData, int pixelStride,
            int numBands) {
        double[] minMax = ImageIOUtils.findMinAndMax(doubleData, pixelStride, numBands);
        double scale = 255F / (minMax[1] - minMax[0]);
        for (int i = 0, j = 0; i < doubleData.length; i += numBands, j++) {
            double val = doubleData[i] - minMax[0];
            if (val < 0.0f)
                val = 0.0f;
            int iVal = (int) (val * scale);
            if (iVal > 255)
                iVal = 255;
            byteData[j] = (byte) iVal;
        }
    }

    /**
     * Converts the float data to byte data, and sets the values in the byteData buffer
     * 
     * @param shortData
     * @param byteData
     */
    public static void shortToByteBuffer(short[] shortData, byte[] byteData, int pixelStride,
            int numBands) {
        int[] minMax = ImageIOUtils.findMinAndMax(shortData, pixelStride, numBands);
        double scale = 1.0;
        if (minMax[1] != minMax[0])
            scale = 256.0 / (minMax[1] - minMax[0]);
        for (int i = 0, j = 0; i < shortData.length; i += numBands, j++) {
            int value = (shortData[i] & 0xffff) - minMax[0];
            if (value < 0)
                value = 0;
            value = (int) (value * scale);
            if (value > 255)
                value = 255;
            byteData[j] = (byte) value;
        }
    }

    /**
     * Utility method for creating a BufferedImage from a source raster Currently only Float->Byte and Byte->Byte are supported. Will throw an
     * {@link UnsupportedOperationException} if the conversion is not supported.
     * 
     * @param raster
     * @param imageType
     * @return
     */
    public static BufferedImage rasterToBufferedImage(Raster raster, ImageTypeSpecifier imageType) {
        if (imageType == null) {
            if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                imageType = ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_BYTE, false);
            else
                throw new IllegalArgumentException("unable to dynamically determine the imageType");
        }
        // create a new buffered image, for display
        BufferedImage bufImage = imageType.createBufferedImage(raster.getWidth(),
                raster.getHeight());

        if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT
                && bufImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
            // convert short pixels to bytes
            short[] shortData = ((DataBufferUShort) raster.getDataBuffer()).getData();
            byte[] byteData = ((DataBufferByte) bufImage.getWritableTile(0, 0).getDataBuffer())
                    .getData();
            ImageIOUtils.shortToByteBuffer(shortData, byteData, 1, raster.getNumBands());
        } else if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_FLOAT
                && bufImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
            // convert float pixels to bytes
            float[] floatData = ((DataBufferFloat) raster.getDataBuffer()).getData();
            byte[] byteData = ((DataBufferByte) bufImage.getWritableTile(0, 0).getDataBuffer())
                    .getData();
            ImageIOUtils.floatToByteBuffer(floatData, byteData, 1, raster.getNumBands());
        } else if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_DOUBLE
                && bufImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
            // convert double pixels to bytes
            double[] doubleData = ((DataBufferDouble) raster.getDataBuffer()).getData();
            byte[] byteData = ((DataBufferByte) bufImage.getWritableTile(0, 0).getDataBuffer())
                    .getData();
            ImageIOUtils.doubleToByteBuffer(doubleData, byteData, 1, raster.getNumBands());
        } else if ((raster.getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE && bufImage
                .getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                || (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT && bufImage
                        .getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                || (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_SHORT && bufImage
                        .getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_SHORT)) {
            bufImage.setData(raster);
        } else {
            throw new UnsupportedOperationException(
                    "Unable to convert raster type to bufferedImage type: "
                            + raster.getDataBuffer().getDataType() + " ==> "
                            + bufImage.getRaster().getDataBuffer().getDataType());
        }
        return bufImage;
    }

    /**
     * Turns a signed byte into an unsigned one.
     * 
     * @param b The byte to read
     * @return An unsigned integer
     */
    public static int makeUnsigned(byte b) {
        return b >= 0 ? (int) b : 255 + (int) b + 1;
    }

    public static void main(String[] args) {
        List<File> files = getFiles(new String[] { "c:/", "c:/dev/" }, new String[] { "jpg" });
        for (File file : files) {
            log.info(file.getAbsolutePath());
        }
    }

}
