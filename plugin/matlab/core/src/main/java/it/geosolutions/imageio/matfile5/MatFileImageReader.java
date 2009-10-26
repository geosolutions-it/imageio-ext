/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2009, GeoSolutions
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
package it.geosolutions.imageio.matfile5;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RasterFactory;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * Main abstract class defining a reader to access Matlab 5 files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class MatFileImageReader extends ImageReader {

    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.matfile5");

    public void setInput(Object input, boolean seekForwardOnly) {
        this.setInput(input, seekForwardOnly, false);
    }

    /** The ImageInputStream */
    private ImageInputStream imageInputStream;

    /** The input dataSource */
    private File dataSource = null;

    protected MatFileReader matReader = null;

    /**
     * Constructs a <code>MatFileImageReader</code> using a
     * {@link MatFileImageReaderSpi}.
     * 
     * @param originatingProvider
     *                The {@link MatFileImageReaderSpi} to use for building this
     *                <code>MatFileImageReader</code>.
     */
    protected MatFileImageReader(MatFileImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
    
    protected abstract void initialize();

    /**
     * Tries to retrieve the data Source for the ImageReader's input.
     */
    protected File getDatasetSource(Object myInput) {
        if (dataSource == null) {
            if (myInput instanceof File)
                dataSource = (File) myInput;
            else if (myInput instanceof FileImageInputStreamExt)
                dataSource = ((FileImageInputStreamExt) myInput).getFile();
            else if (input instanceof URL) {
                final URL tempURL = (URL) input;
                if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                    try {
                        dataSource = new File(URLDecoder.decode(tempURL
                                .getFile(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Not a Valid Input ", e);
                    }
                }
            } else
                // should never happen
                throw new RuntimeException(
                        "Unable to retrieve the Data Source for"
                                + " the provided input");
        }
        return dataSource;
    }

    /**
     * Sets the input for the specialized reader.
     * 
     * @throws IllegalArgumentException
     *                 if the provided input is <code>null</code>
     */
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Setting Input");

        // Prior to set a new input, I need to do a pre-emptive reset in order
        // to clear any value-object which was related to the previous input.
        if (this.imageInputStream != null) {
            reset();
            imageInputStream = null;
        }

        if (input == null)
            throw new IllegalArgumentException("The provided input is null!");

        // //
        //
        // File input
        //
        // //
        if (input instanceof File) {
            dataSource = (File) input;
            try {
                imageInputStream = new FileImageInputStreamExtImpl((File)input);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to create a valid input stream ", e);
            }
        }
        // //
        //
        // FileImageInputStreamExt input
        //
        // //
        else if (input instanceof FileImageInputStreamExt) {
            dataSource = ((FileImageInputStreamExt) input).getFile();
            imageInputStream = (ImageInputStream) input;
        }
        // //
        //
        // URL input
        //
        // //
        else if (input instanceof URL) {
            final URL tempURL = (URL) input;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {

                try {
                    dataSource = new File(URLDecoder.decode(tempURL
                            .getFile(), "UTF-8"));
                    imageInputStream = ImageIO.createImageInputStream(input);
                } catch (IOException e) {
                    throw new RuntimeException(
                            "Failed to create a valid input stream ", e);
                }
            }
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Checking if this input is of a supported format.
        // Now, I have an ImageInputStream and I can try to see if the input's
        // format is supported by the specialized reader
        //
        // /////////////////////////////////////////////////////////////////////
        boolean isInputDecodable = false;

        try {
            isInputDecodable = ((MatFileImageReaderSpi) this
                    .getOriginatingProvider()).isDecodable(dataSource
                    .getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(
                    "The provided input is not supported by this reader", e);
        }

        if (isInputDecodable)
            super.setInput(imageInputStream, seekForwardOnly, ignoreMetadata);
        else {
            StringBuffer sb = new StringBuffer();
            if (imageInputStream == null)
                sb.append(
                        "Unable to create a valid ImageInputStream "
                                + "for the provided input:").append("\n")
                        .append(input.toString());
            else
                sb.append("The provided input is not supported by this reader");
            throw new RuntimeException(sb.toString());
        }
    }

    /**
     * Allows resources to be released
     */
    public synchronized void dispose() {
        super.dispose();
        if (imageInputStream != null)
            try {
                imageInputStream.close();
            } catch (IOException ioe) {

            }
        imageInputStream = null;
        if (matReader!=null)
            matReader.dispose();
        matReader = null;
    }

    /**
     * Reset main values
     */
    public synchronized void reset() {
        super.setInput(null, false, false);
        dispose();
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("getting NumImages");
        return 1;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public static String getString(final MatFileReader reader, final String element) {
        String value = "";
        if (element != null) {
            MLArray array = reader.getMLArray(element);
            final MLChar text = array != null ? (MLChar) array : null;
            if (text != null)
                value = text.getString(0);
        }
        return value;
    }

    public static double[] getDoubles(final MatFileReader reader, final String element, double[] values) {
        MLArray array = reader.getMLArray(element);
        final MLDouble dArray = array != null ? (MLDouble) array : null;
        if (dArray != null) {
            final int nDims;
            if (values == null) {
                nDims = dArray.getM();
                values = new double[nDims];
            } else
                nDims = values.length;

            for (int i = 0; i < nDims; i++) {
                values[i] = dArray.get(i).doubleValue();
            }

        } else {
            if (values == null) {
                values = new double[] { Double.NaN, Double.NaN };
            } else {
                for (int i = 0; i < values.length; i++) {
                    values[i] = Double.NaN;
                }
            }
        }
        return values;

    }

    public static double getDouble(final MatFileReader reader, final String element){
        return getDouble(reader, element,0);
    }
    
    public static double getDouble(final MatFileReader reader, final String element, final int index) {
        double value = Double.NaN;
        if (element != null && reader!=null) {
            MLArray array = reader.getMLArray(element);
            final MLDouble arrayD = array != null ? (MLDouble) array : null;
            if (arrayD != null)
                value = arrayD.get(index).doubleValue();
        }
        return value;
    }

    protected void initFilter(MatFileFilter filter, Set<String> filterElements) {
        if (filterElements != null && !filterElements.isEmpty()) {
            for (String element : filterElements) {
                filter.addArrayName(element);
            }
        }
    }
    
    public static ColorModel buildColorModel(final SampleModel sampleModel) {
        ColorSpace cs = null;
        ColorModel colorModel = null;
        final int buffer_type = sampleModel.getDataType();
        final int numBands = sampleModel.getNumBands();
        if (numBands > 1) {
            // /////////////////////////////////////////////////////////////////
            //
            // Number of Bands > 1.
            // ImageUtil.createColorModel provides to Creates a
            // ColorModel that may be used with the specified
            // SampleModel
            //
            // /////////////////////////////////////////////////////////////////
            colorModel = ImageUtil.createColorModel(sampleModel);
        } else if ((buffer_type == DataBuffer.TYPE_BYTE)
                || (buffer_type == DataBuffer.TYPE_USHORT)
                || (buffer_type == DataBuffer.TYPE_INT)
                || (buffer_type == DataBuffer.TYPE_FLOAT)
                || (buffer_type == DataBuffer.TYPE_DOUBLE)) {

            // Just one band. Using the built-in Gray Scale Color Space
            cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            colorModel = RasterFactory.createComponentColorModel(buffer_type, // dataType
                    cs, // color space
                    false, // has alpha
                    false, // is alphaPremultiplied
                    Transparency.OPAQUE); // transparency
        } else {
            if (buffer_type == DataBuffer.TYPE_SHORT) {
                // Just one band. Using the built-in Gray Scale Color
                // Space
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                colorModel = new ComponentColorModel(cs, false, false,
                        Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
            }
        }
        return colorModel;
    }
}
