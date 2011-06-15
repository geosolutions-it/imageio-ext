/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
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
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;

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
    
    /** Contains the name of the underlying data arrays 
     * The implementation uses a LinkedList in order to associate
     * imageIndexes to arrays name 
     */
    protected List<String> dataArrays = new LinkedList<String>();
    
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
                	dataSource = Utilities.urlToFile(tempURL);
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
                    dataSource = Utilities.urlToFile(tempURL);
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
            StringBuilder sb = new StringBuilder();
            if (imageInputStream == null)
                sb.append("Unable to create a valid ImageInputStream for the provided input:").append("\n")
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
        if (matReader != null)
            matReader.dispose();
        if (dataArrays != null){
        	dataArrays.clear();
        	dataArrays = null;
        }
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

    protected static void initFilter(MatFileFilter filter, Set<String> filterElements) {
        if (filterElements != null && !filterElements.isEmpty()) {
            for (String element : filterElements) {
                filter.addArrayName(element);
            }
        }
    }
    
    /**
     * Get an AffineTransform to filter the matrix 
     * @param param an ImageReadParam used to compute scales and translates by checking the
     * 			subsampling as well as the source region. 
     * 
     * @return
     * @throws IOException 
     */
    protected AffineTransform getAffineTransform(final ImageReadParam param) throws IOException{
    	
    	//Compute standard transpose
    	AffineTransform transposed = getTransposed(param);
    	
    	//preconcatenate additional transformation, depending on the data interpretation
    	transposed.preConcatenate(getPreTransform(param));
    	return transposed;
    }

    /**
     * The Standard implementation returns a simple Identity.
     * Special Implementations may add additional transformation to be preconcatenated  
     * @param param
     * @return
     * @throws IOException
     */
	protected AffineTransform getPreTransform(final ImageReadParam param) throws IOException {
		return AffineTransform.getRotateInstance(0.0);//identity
	}

	/**
     *   Note that the underlying matrix fills a buffer where samples are sorted as:
     *   First row, first column, second row, first column, third row, first column...
     *   Therefore I'm getting a transposed image. I will transpose it 
     *   
     *   Numerical Example: The Matlab Matrix is 3X3 as: 
     *   
     *    1, 2, 3
     *    4, 5, 6
     *    7, 8, 9
     *   
     *    The DataBuffer will contains data as:
     *    1, 4, 7, 2, 5, 8, 3, 6, 9
     *   
     *
	 * @param param
	 * @return
	 */
	private AffineTransform getTransposed(ImageReadParam param) {
		final AffineTransform transform = AffineTransform.getRotateInstance(0);// identity
    	if (param!=null){
    	   final int xSubsamplingFactor = param.getSourceXSubsampling();
    	   final int ySubsamplingFactor = param.getSourceYSubsampling();
    	   if (xSubsamplingFactor != 1 || ySubsamplingFactor != 1) {
            	transform.preConcatenate(AffineTransform.getScaleInstance(1.0d/ySubsamplingFactor, 1.0d/xSubsamplingFactor));
           }
    	}
        
        // //
        //
        // Transposing the Matlab data matrix
        //
        // //
        AffineTransform transposeTransform = AffineTransform.getRotateInstance(0);
        transposeTransform.preConcatenate(AffineTransform.getScaleInstance(1,-1));
        transposeTransform.preConcatenate(AffineTransform.getRotateInstance(Math.PI*0.5d));
        transform.preConcatenate(transposeTransform);
        return transform;
	}
    
}
