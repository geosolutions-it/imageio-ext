/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2025, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *
 */
package it.geosolutions.imageio.plugins.raw;

import it.geosolutions.imageio.stream.input.RawImageInputStream;
import org.eclipse.imagen.PlanarImage;
import org.eclipse.imagen.TiledImage;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Dimension;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class RawReadWriteTest {

    final int SIZE = 20;
    final int TILE_SIZE = 5;

    @Test
    public void testReadWriteByte() throws IOException {
        assertReadWrite(DataBuffer.TYPE_BYTE);
    }

    @Test
    public void testReadWriteUShort() throws IOException {
        assertReadWrite(DataBuffer.TYPE_USHORT);
    }

    @Test
    public void testReadWriteInt() throws IOException {
        assertReadWrite(DataBuffer.TYPE_INT);
    }

    @Test
    public void testReadWriteFloat() throws IOException {
        assertReadWrite(DataBuffer.TYPE_FLOAT);
    }

    @Test
    public void testReadWriteDouble() throws IOException {
        assertReadWrite(DataBuffer.TYPE_DOUBLE);
    }

    public void assertReadWrite(int pixelType) throws IOException {
        ImageWriter writer = getWriter();
        ImageReader reader = getReader();

        TiledImage source = getTestImage(pixelType);
        byte[] data;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ImageOutputStream ios = new MemoryCacheImageOutputStream(bos)) {
            writer.setOutput(ios);
            writer.write(source);
            ios.flush();
            data = bos.toByteArray();
        }

        RenderedImage target;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             RawImageInputStream ris = new RawImageInputStream(new MemoryCacheImageInputStream(bis),
                     ImageTypeSpecifier.createFromRenderedImage(source), new long[]{0}, new Dimension[]{new Dimension(SIZE, SIZE)})) {
            reader.setInput(ris);
            target = reader.read(0);
        }

        if (pixelType == DataBuffer.TYPE_FLOAT ||
                pixelType == DataBuffer.TYPE_DOUBLE) {
            compareImagesDouble(source, target);
        } else {
            compareImagesInteger(source, target);
        }
    }

    private void compareImagesInteger(TiledImage image, RenderedImage ri) {
        int[] pixel1 = new int[1];
        int[] pixel2 = new int[1];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                image.getData().getPixel(i, j, pixel1);
                ri.getData().getPixel(i, j, pixel2);
                assertArrayEquals("Pixel differ at " + i + "," + j, pixel1, pixel2);
            }
        }
    }

    private void compareImagesDouble(TiledImage image, RenderedImage ri) {
        double[] pixel1 = new double[1];
        double[] pixel2 = new double[1];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                image.getData().getPixel(i, j, pixel1);
                ri.getData().getPixel(i, j, pixel2);
                assertArrayEquals("Pixel differ at " + i + "," + j, pixel1, pixel2, 1E-6);
            }
        }
    }

    private TiledImage getTestImage(int pixelType) {
        SampleModel sm = new ComponentSampleModel(pixelType, TILE_SIZE, TILE_SIZE, 1, TILE_SIZE, new int[]{0});
        TiledImage image = new TiledImage(0, 0, SIZE, SIZE, 0, 0, sm, PlanarImage.createColorModel(sm));
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int value = (i + j) % 128;
                image.setSample(i, j, 0, value);
            }
        }
        return image;
    }

    private static ImageWriter getWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("raw");
        assertTrue(writers.hasNext());
        ImageWriter writer = writers.next();
        assertTrue(writer.getOriginatingProvider() instanceof RawImageWriterSpi);
        return writer;
    }

    private static ImageReader getReader() {
        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("raw");
        assertTrue(readers.hasNext());
        ImageReader reader = readers.next();
        assertTrue(reader.getOriginatingProvider() instanceof RawImageReaderSpi);
        return reader;
    }

}
