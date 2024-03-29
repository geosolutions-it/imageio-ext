/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.netcdf.BaseNetCDFImageReader;
import it.geosolutions.imageio.plugins.netcdf.BaseVariableWrapper;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

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
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.iosp.hdf4.H4iosp;

public abstract class BaseHDF4ImageReader extends BaseImageReader {

	protected class HDF4DatasetWrapper extends BaseVariableWrapper{
        
        private int numAttributes;

        public int getNumAttributes() {
			return numAttributes;
		}
        
        protected HDF4DatasetWrapper(final Variable var) {
        	super(var);
            numAttributes = var.getAttributes().size();
            final int numBands = getNumBands();
            final int width = getWidth();
            final int height = getHeight();
            if (numBands == 3)
            	setSampleModel(new PixelInterleavedSampleModel(NetCDFUtilities.getRawDataType(var), width,
            			height, numBands, width*numBands, new int[]{0,1,2}));	
            else
            	setSampleModel(new BandedSampleModel(NetCDFUtilities.getRawDataType(var), width, height, numBands));	
        }
	}
	
	final protected BaseNetCDFImageReader reader;
	
	protected final static Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.hdf4");

    /** set it to <code>true</code> when initialization has been performed */
    private boolean isInitialized = false;

	protected abstract HDF4DatasetWrapper getDatasetWrapper(final int imageIndex);
    
    /* (non-Javadoc)
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getImageTypes(int)
	 */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        return reader.getImageTypes(imageIndex);
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
        reader = new BaseNetCDFImageReader(originatingProvider);
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

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#dispose()
	 */
    public void dispose() {
        super.dispose();
        reader.dispose();
        isInitialized = false;
    }
    
    @Override
	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		super.setInput(input, seekForwardOnly, ignoreMetadata);
		reader.setInput(input, seekForwardOnly, ignoreMetadata);
		final NetcdfDataset dataset = reader.getDataset();
		if(dataset!=null){
    		if(!(dataset.getIosp() instanceof H4iosp))
    			throw new IllegalArgumentException("Provided dataset is not an HDF4 file");
    	}
    	else
    		throw new IllegalArgumentException("Provided dataset is not an HDF4 file");
		
		try {
			initialize();
		} catch (IOException e) {
            throw new IllegalArgumentException("Error occurred during NetCDF file parsing", e);
		}
	}

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getStreamMetadata()
	 */
    public IIOMetadata getStreamMetadata() throws IOException {
        throw new UnsupportedOperationException("Stream Metadata is not implemented for the base class, use corecommonstreammetadata");
    }

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getGlobalAttribute(int)
	 */
    protected KeyValuePair getGlobalAttribute(final int attributeIndex) throws IOException {
		return reader.getGlobalAttribute(attributeIndex);
	}

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getAttributeAsString(int, java.lang.String)
	 */
    protected String getAttributeAsString(final int imageIndex, final String attributeName) {
    	return getAttributeAsString(imageIndex, attributeName, false);
    }

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getAttributeAsString(int, java.lang.String, boolean)
	 */
    protected String getAttributeAsString(final int imageIndex, final String attributeName, final boolean isUnsigned) {
        return reader.getAttributeAsString(imageIndex, attributeName, isUnsigned);
    }
    
    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getAttribute(int, int)
	 */
    protected KeyValuePair getAttribute(final int imageIndex, final int attributeIndex) throws IOException {
		return reader.getAttribute(imageIndex, attributeIndex);
	}
    
	@Override
	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input, seekForwardOnly, false);
	}

	@Override
	public void setInput(Object input) {
		this.setInput(input, false, false);
	}

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getWidth(int)
	 */
    public int getWidth(final int imageIndex) throws IOException {
    	checkImageIndex(imageIndex);
        return reader.getWidth(imageIndex);
    }

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getHeight(int)
	 */
    public int getHeight(final int imageIndex) throws IOException {
    	checkImageIndex(imageIndex);
        return reader.getHeight(imageIndex);
    }

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getTileHeight(int)
	 */
    public int getTileHeight(final int imageIndex) throws IOException {
    	checkImageIndex(imageIndex);
        return reader.getTileHeight(imageIndex);
    }

    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#getTileWidth(int)
	 */
    public int getTileWidth(final int imageIndex) throws IOException {
    	checkImageIndex(imageIndex);
        return reader.getTileHeight(imageIndex);
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
        final SampleModel sampleModel = wrapper.getSampleModel().createCompatibleSampleModel(destWidth, destHeight);
        final ColorModel colorModel = ImageIOUtilities.createColorModel(sampleModel);

        /*
         * Reads the requested sub-region only.
         */
        final int size = destHeight*destWidth*numBands;
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
        return image;
    }
    
    /**
	 * @see it.geosolutions.imageio.plugins.hdf4.HDF4ImageReader#read(int, javax.imageio.ImageReadParam)
	 */
    @Override
    public BufferedImage read(final int imageIndex, final ImageReadParam param)
            throws IOException {
    	return read2DVariable(imageIndex, param);
    }

	/**
	 * @see javax.imageio.ImageReader#getImageMetadata(int)
	 */
	@Override
	public IIOMetadata getImageMetadata(final int imageIndex) throws IOException {
		throw new UnsupportedOperationException("Change me as soon as possible to use corecommonimagemetadata");
	}
	
	public IIOMetadata getImageMetadata(final int imageIndex, final String metadataFormat) throws IOException {
		throw new UnsupportedOperationException("Change me as soon as possible to use corecommonimagemetadata");
	}
	
}
