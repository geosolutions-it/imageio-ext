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
package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public abstract class BaseHDF4ImageReader extends BaseImageReader {

	protected class HDF4DatasetWrapper{
        private Variable variable;
        
        private int numAttributes;

        public int getNumAttributes() {
			return numAttributes;
		}

		private int width;

        private int height;

        private int tileHeight;

        private int tileWidth;
        
        private int numBands;

        private SampleModel sampleModel;
        
        protected HDF4DatasetWrapper(final Variable var) {
            // This constructor is called after checking h4SdsCollection is not
            // null
            variable = var;
            numAttributes = variable.getAttributes().size();
//            final int dimSizes[] = variable.getDimSizes();
//            final int chunkSizes[] = variable.getChunkSizes();

            width = variable.getDimension(1).getLength();
            height = variable.getDimension(0).getLength();
            numBands = variable.getRank()>2?variable.getDimension(2).getLength():1;
            tileWidth = Math.min(512, width);
            tileHeight = Math.min(512, height);
            if (numBands == 3)
            	sampleModel = new PixelInterleavedSampleModel(NetCDFUtilities.getRawDataType(var), width,
            			height, numBands, width*numBands, new int[]{0,1,2});	
            else
            	sampleModel = new BandedSampleModel(NetCDFUtilities.getRawDataType(var), width,
            			height, numBands);	
        }

        public int getNumBands() {
			return numBands;
		}

		public void setNumBands(int numBands) {
			this.numBands = numBands;
		}

		public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public SampleModel getSampleModel() {
            return sampleModel;
        }

        public int getTileHeight() {
            return tileHeight;
        }

        public int getTileWidth() {
            return tileWidth;
        }

        public Variable getVariable() {
            return variable;
        }
	}
	
    protected final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jhdf");

    /** set it to <code>true</code> when initialization has been performed */
    private boolean isInitialized = false;

    private int numGlobalAttributes;
    
    public int getNumGlobalAttributes() {
		return numGlobalAttributes;
	}

	public void setNumGlobalAttributes(int numGlobalAttributes) {
		this.numGlobalAttributes = numGlobalAttributes;
	}

	protected abstract HDF4DatasetWrapper getDatasetWrapper(final int imageIndex);
    
    private NetcdfDataset dataset = null;

    protected NetcdfDataset getDataset() {
		return dataset;
	}

    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        initialize();
        final List<ImageTypeSpecifier> l = new ArrayList<ImageTypeSpecifier>(1);
        final HDF4DatasetWrapper ds = getDatasetWrapper(imageIndex);
        final SampleModel sm = ds.getSampleModel();
        final ImageTypeSpecifier imageType = new ImageTypeSpecifier(ImageIOUtilities
                .getCompatibleColorModel(sm), sm);
        l.add(imageType);
        return l.iterator();
    }
    
    /**
     * Additional initialization for a specific HDF "Profile". Depending on the
     * HDF data producer, the originating file has a proper data/metadata
     * structure. For this reason, a specific initialization should be
     * implemented for each different HDF "Profile". As an instance, the
     * Automated Processing System (APS) produces HDF files having a different
     * structure with respect to the HDF structure of a file produced by TIROS
     * Operational Vertical Sounder (TOVS).
     * 
     * @throws Exception
     */
    protected abstract void initializeProfile() throws IOException;

    protected BaseHDF4ImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {

        // ////////////////////////////////////////////////////////////////////
        //
        // Reset the state of this reader
        //
        // Prior to set a new input, I need to do a pre-emptive reset in order
        // to clear any value-object related to the previous input.
        // ////////////////////////////////////////////////////////////////////

        // TODO: Add URL & String support.
        if (dataset != null)
            reset();
        try {

            // TODO: Check this
            // if (input instanceof URI) {
            // input = ((URI) input).toURL();
            // }
        	if (dataset == null) {
                dataset = getDataset(input);
            }
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            initialize();
        } catch (IOException e) {
            throw new IllegalArgumentException("Not a Valid Input", e);
        }
    }

    public void setInput(Object input, boolean seekForwardOnly) {
        this.setInput(input, seekForwardOnly, true);
    }

    public void setInput(Object input) {
        this.setInput(input, true, true);
    }

    /**
     * Simple initialization method
     */
    protected synchronized void initialize() throws IOException {
        if (!isInitialized) {
            // initialize information specific to this profile
            initializeProfile();
            isInitialized = true;
        }
    }

    public synchronized void dispose() {
        super.dispose();
        isInitialized = false;
        try {
            if (dataset != null) {
                dataset.close();
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Errors closing NetCDF dataset."
                        + e.getLocalizedMessage());
        } finally {
            dataset = null;
        }
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        // TODO message and/or implement
        throw new UnsupportedOperationException();
    }

    public synchronized void reset() {
        super.setInput(null, false, false);
        dispose();
    }
    
    /**
     * Returns a {@code NetcdfDataset} given an input object
     * 
     * @param input
     *                the input object (usually a {@code File}, a
     *                {@code String} or a {@code FileImageInputStreamExt).
     * @return {@code NetcdfDataset} in case of success.
     * @throws IOException
     *                 if some error occur while opening the dataset.
     * @throws {@link IllegalArgumentException}
     *                 in case the specified input is a directory
     */
    public static NetcdfDataset getDataset(Object input) throws IOException {
        NetcdfDataset dataset = null;
        if (input instanceof File) {
            if (!((File) input).isDirectory())
                dataset = NetcdfDataset.openDataset(((File) input).getPath());
            else
                throw new IllegalArgumentException("Error occurred during NetCDF file reading: The input file is a Directory.");
        } else if (input instanceof String) {
            File file = new File((String) input);
            if (!file.isDirectory())
                dataset = NetcdfDataset.openDataset(file.getPath());
            else
                throw new IllegalArgumentException( "Error occurred during NetCDF file reading: The input file is a Directory.");
        } else if (input instanceof URL) {
            final URL tempURL = (URL) input;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                File file = Utilities.urlToFile(tempURL);
                if (!file.isDirectory())
                    dataset = NetcdfDataset.openDataset(file.getPath());
                else
                    throw new IllegalArgumentException( "Error occurred during NetCDF file reading: The input file is a Directory.");
            }
        }

        else if (input instanceof FileImageInputStreamExt) {
            File file = ((FileImageInputStreamExt) input).getFile();
            if (!file.isDirectory())
                dataset = NetcdfDataset.openDataset(file.getPath());
            else
                throw new IllegalArgumentException("Error occurred during NetCDF file reading: The input file is a Directory.");
        }
        return dataset;
    }
    
    public KeyValuePair getGlobalAttribute(final int attributeIndex) throws IOException {
		return NetCDFUtilities.getGlobalAttribute(getDataset(), attributeIndex);
	}

    public String getAttributeAsString(final int imageIndex, final String attributeName) {
	     return getAttributeAsString(imageIndex, attributeName, false);
    }

    public String getAttributeAsString(final int imageIndex, final String attributeName,
            final boolean isUnsigned) {
        String attributeValue = "";
        final HDF4DatasetWrapper wrapper = getDatasetWrapper(imageIndex);
        final Attribute attr = wrapper.getVariable().findAttributeIgnoreCase(attributeName);
        if (attr != null)
            attributeValue = NetCDFUtilities.getAttributesAsString(attr,
                    isUnsigned);
        return attributeValue;
    }
    
    public KeyValuePair getAttribute(final int imageIndex, final int attributeIndex)
    throws IOException {
		KeyValuePair attributePair = null;
		final Variable var = getDatasetWrapper(imageIndex).getVariable();
		if (var != null) 
			attributePair = NetCDFUtilities.getAttribute(var, attributeIndex);
		return attributePair;
	}
    
    /**
     * Returns the width in pixels of the given image within the input source.
     * 
     * @param imageIndex
     *                the index of the image to be queried.
     * 
     * @return the width of the image, as an <code>int</code>.
     */
    public int getWidth(final int imageIndex) throws IOException {
        initialize();
        return getDatasetWrapper(imageIndex).getWidth();
    }

    /**
     * Returns the height in pixels of the given image within the input source.
     * 
     * @param imageIndex
     *                the index of the image to be queried.
     * 
     * @return the height of the image, as an <code>int</code>.
     */
    public int getHeight(final int imageIndex) throws IOException {
        initialize();
        return getDatasetWrapper(imageIndex).getHeight();
    }

    /**
     * Returns the height of a tile in the given image.
     * 
     * @param imageIndex
     *                the index of the image to be queried.
     * 
     * @return the height of a tile.
     * 
     * @exception IOException
     *                    if an error occurs during reading.
     */
    public int getTileHeight(final int imageIndex) throws IOException {
        initialize();
        return getDatasetWrapper(imageIndex).getTileHeight();
    }

    /**
     * Returns the width of a tile in the given image.
     * 
     * @param imageIndex
     *                the index of the image to be queried.
     * 
     * @return the width of a tile.
     * 
     * @exception IOException
     *                    if an error occurs during reading.
     */
    public int getTileWidth(final int imageIndex) throws IOException {
        initialize();
        return getDatasetWrapper(imageIndex).getTileWidth();
    }
    
    protected BufferedImage read2DVariable (final int imageIndex, final ImageReadParam param) throws IOException{
    	BufferedImage image = null;
        final HDF4DatasetWrapper wrapper = getDatasetWrapper(imageIndex);
        final Variable variable = wrapper.getVariable();

        /*
         * Fetches the parameters that are not already processed by utility
         * methods like 'getDestination' or 'computeRegions' (invoked below).
         */
        final int strideX, strideY;
        final int[] srcBands, dstBands;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
            srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
        } else {
            strideX = 1;
            strideY = 1;
            srcBands = null;
            dstBands = null;
        }
        final int rank = variable.getRank();

        /*
         * Gets the destination image of appropriate size. We create it now
         * since it is a convenient way to get the number of destination bands.
         */
        final int width = wrapper.getWidth();
        final int height = wrapper.getHeight();
        final int numBands = wrapper.getNumBands();
        /*
         * Computes the source region (in the NetCDF file) and the destination
         * region (in the buffered image). Copies those informations into UCAR
         * Range structure.
         */
        final Rectangle srcRegion = new Rectangle();
        final Rectangle destRegion = new Rectangle();
        computeRegions(param, width, height, null, srcRegion, destRegion);
        // flipVertically(param, height, srcRegion);
        int destWidth = destRegion.x + destRegion.width;
        int destHeight = destRegion.y + destRegion.height;

        final List<Range> ranges = new LinkedList<Range>();
        for (int i = 0; i < rank; i++) {
            final int first, length, stride;
            switch (i) {
	            case 1: {
	                first = srcRegion.x;
	                length = srcRegion.width;
	                stride = strideX;
	                break;
	            }
	            case 0: {
	                first = srcRegion.y;
	                length = srcRegion.height;
	                stride = strideY;
	                break;
	            }
	            default: {
	                first = 0;
	            	length = numBands;
	                stride = 1;
	                break;
	            }
            }
            try {
                ranges.add(new Range(first, first + length - 1, stride));
            } catch (InvalidRangeException e) {
             	//TODO LOGME
            }
        }
        final Section sections = new Section(ranges);

        /*
         * Setting SampleModel and ColorModel.
         */
        SampleModel sampleModel = wrapper.getSampleModel()
                .createCompatibleSampleModel(destWidth, destHeight);
        ColorModel colorModel = ImageIOUtilities
                .getCompatibleColorModel(sampleModel);

        /*
         * Reads the requested sub-region only.
         */
        final int size = destHeight*destWidth*numBands;
//        for (int zi = 0; zi < numBands; zi++) {
            Array array = null;
            try {
                array = variable.read(sections);
                DataBuffer dataBuffer = null;
                if (array instanceof ArrayByte){
                	dataBuffer = new DataBufferByte((byte[])array.get1DJavaArray(byte.class),size);
                } else if (array instanceof ArrayShort){
                	dataBuffer = new DataBufferShort((short[])array.get1DJavaArray(short.class),size);
                } else if (array instanceof ArrayInt){
                	dataBuffer = new DataBufferInt((int[])array.get1DJavaArray(int.class),size);
                } else if (array instanceof ArrayFloat){
                	dataBuffer = new DataBufferFloat((float[])array.get1DJavaArray(float.class),size);
                } else if (array instanceof ArrayDouble){
                	dataBuffer = new DataBufferDouble((double[])array.get1DJavaArray(double.class),size);
                }
                
                WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0,0));
                image = new BufferedImage(colorModel, raster,
                        colorModel.isAlphaPremultiplied(), null);
            } catch (InvalidRangeException e) {
            	//TODO LOGME
            } 
            
//        }
        return image;
    }
    
    @Override
    public BufferedImage read(final int imageIndex, final ImageReadParam param)
            throws IOException {
    	return read2DVariable(imageIndex, param);
    }
}
