/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJDecompressor;

/**
 * @author Emanuele Tajariol, GeoSolutions SaS
 * @author Daniele Romagnoli, GeoSolutions SaS An {@link ImageReader} for JPEG decompression using the TurboJPEG library. It can accept (as setInput
 *         method) both an ImageInputStream as any other {@link ImageReader}, as well as byte[] object. The last one is useful when the reader is
 *         initialized by a TiffImageReader having internally JPEG compressed tiles. It can pass down the byte[] array instead of doing any copy,
 *         repeated read of a buffer (Which should be faster)
 */
public class TurboJpegImageReader extends ImageReader {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.turbojpeg");

    private int INITIAL_JPG_BUFFER_SIZE = 1 * 1024 * 1024;

    private static final int DEFAULT_READ_BUFFER_SIZE = 1024 * 4;

    private int width = -1;

    private int height = -1;

    private byte[] data = null;

    private ImageInputStream iis = null;

    private static int EXTERNAL_FLAGS = -1;

    private int flags = TJ.FLAG_FASTUPSAMPLE;

    // the type of subsampling of the compressed data. (GRAY, 4:2:0, ...)
    private int subsamp;

    TJDecompressor decompressor;

    static {
        String flagsProp = System.getProperty(TurboJpegUtilities.FLAGS_PROPERTY);
        if (flagsProp != null) {
            String[] tjFlags = flagsProp.split(",");
            EXTERNAL_FLAGS = 0;
            for (String tjFlag : tjFlags) {
                EXTERNAL_FLAGS |= TurboJpegUtilities.getTurboJpegFlag(tjFlag);
            }
        }
    }

    public TurboJpegImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
        try {
            this.decompressor = new TJDecompressor();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize native decompressor", e);
        }
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        return width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        return height;
    }

    private static final List<ImageTypeSpecifier> FIXEDIMGETYPES = Collections
            .unmodifiableList(Arrays.asList(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB),
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB),
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR),
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_4BYTE_ABGR),
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY)));

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        checkIndex(imageIndex);

        // uhm, cannot get the pixelFormat from the decompressor
        return FIXEDIMGETYPES.iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        checkIndex(imageIndex);
        if (data == null) {
            throw new IllegalArgumentException("Missing data array");
        }

        BufferedImage bi = new BufferedImage(width, height,
                       subsamp == TJ.SAMP_GRAY ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR);

        // Using local variables to avoid changing the internal state
        
        try {
            decompressor.setJPEGImage(data, data.length);
            decompressor.decompress(bi, flags);
        } catch (Exception e) {
            throw new IOException("Exception while decompressing:", e);
        }
        return bi;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {

        // Preliminar reset
        reset();
        this.ignoreMetadata = ignoreMetadata;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("setting input");
        }

        if (input instanceof ImageInputStream) {
            iis = (ImageInputStream) input; // Always works
        }

        /*
         * Read the whole JPG. there's no exposed API to read the header only the decompressor requires the whole data to be in a byte array.
         * Therefore we need to load the byte array data in case it's not already available as input.
         */
        try {
            if (input instanceof byte[]) {
                data = (byte[]) input;
            } else {
                if (iis == null) {
                    throw new NullPointerException("The provided input is null!");
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream(INITIAL_JPG_BUFFER_SIZE);
                byte[] buffer = new byte[DEFAULT_READ_BUFFER_SIZE];

                int n = 0;

                while (-1 != (n = iis.read(buffer))) {
                    baos.write(buffer, 0, n);
                }
                data = baos.toByteArray();
            }
            flags = EXTERNAL_FLAGS > 0 ? EXTERNAL_FLAGS : flags;
            decompressor.setJPEGImage(data, data.length);
            width = decompressor.getWidth();
            height = decompressor.getHeight();
            subsamp = decompressor.getSubsamp();

        } catch (Exception ex) {
            throw new RuntimeException("Error creating jpegturbo decompressor: " + ex.getMessage(),
                    ex);
        }
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        // CHECKME: should we mark the position?
    }

    public void reset() {
        super.setInput(null, false, false);
        data = null;
    }

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("ImageIndex out of bound: " + imageIndex);
        }
    }

    @Override
    public void dispose() {
        try {
            reset();
        } finally {
            if (decompressor != null) {
                try {
                    decompressor.close();
                    decompressor = null;
                } catch (Exception ex) {
                    // Eat exception. There is nothing else we can do
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(
                                "Exception occurred while closing the decompressor: "
                                        + ex.getLocalizedMessage());
                    }
                }
            }
        }
    }

    protected static String toString(ImageReadParam param) {
        StringBuilder sb = new StringBuilder();
        if (param == null) {
            sb.append("ImageReadParam is null");
        } else {
            sb.append(param.getClass().getSimpleName()).append('[');
            sb.append(" srcXsub:").append(param.getSourceXSubsampling());
            sb.append(" srcYsub:").append(param.getSourceYSubsampling());
            sb.append(" subXoff:").append(param.getSubsamplingXOffset());
            sb.append(" subYoff:").append(param.getSubsamplingYOffset());
            sb.append(" srcRegion:").append(param.getSourceRegion());
            sb.append(" destOff:").append(param.getDestinationOffset());

            ImageTypeSpecifier its = param.getDestinationType();
            if(its != null) {
                sb.append(" its:").append(its.getSampleModel());
                if(its.getSampleModel() != null ) {
                    sb.append(" its.sm.datatype:").append(its.getSampleModel().getDataType());
                    sb.append(" its.sm.numbands:").append(its.getSampleModel().getNumBands());
                }
            }

            BufferedImage bi = param.getDestination();
            if(bi != null) {
                sb.append(" bi.sm:").append(bi.getSampleModel());
                if(bi.getSampleModel() != null) {
                    sb.append(" bi.sm.datatype:").append(bi.getSampleModel().getDataType());
                    sb.append(" bi.sm.numbands:").append(bi.getSampleModel().getNumBands());
                }
            }

            sb.append(']');
        }
        return sb.toString();
    }
}
