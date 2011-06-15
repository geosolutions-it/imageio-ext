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
package it.geosolutions.imageio.plugins.netcdf;

import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.CheckType;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.util.CancelTask;

/**
 * Base implementation for NetCDF-CF image flat reader. Pixels are assumed
 * organized according the COARDS convention (a precursor of <A
 * HREF="http://www.cfconventions.org/">CF Metadata conventions</A>), i.e. in (<var>t</var>,<var>z</var>,<var>y</var>,<var>x</var>)
 * order, where <var>x</var> varies faster. The image is created from the two
 * last dimensions (<var>x</var>,<var>y</var>).
 * 
 * Each ImageIndex corresponds to a 2D-slice of NetCDF.
 * 
 * {@link NetCDFImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from NetCDF-CF sources.
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Simoe Giannecchini, GeoSolutions
 */
public class NetCDFImageReader extends BaseImageReader implements CancelTask {

    protected final static Logger LOGGER = Logger .getLogger(NetCDFImageReader.class.toString());

    private CheckType checkType = CheckType.UNSET;
    
    private BaseNetCDFImageReader reader;

    BaseNetCDFImageReader getInnerReader() {
		return reader;
	}

	/**
     * The last error from the NetCDF library.
     */
    private String lastError;

    /**
     * Mapping of imageIndex Ranges to NetCDF Variables.
     */
//    private Map<Range, NetCDFVariableWrapper> indexMap = null;

    private static class NetCDFVariableWrapper extends BaseVariableWrapper{

        public NetCDFVariableWrapper(Variable variable) {
        	super(variable);
            final int bufferType = NetCDFUtilities.getRawDataType(variable);
            setSampleModel(new BandedSampleModel(bufferType, getWidth(), getHeight(), 1));
        }
    }

    /**
     * Initialize main properties for this reader.
     * 
     * @throws exception
     *                 {@link InvalidRangeException}
     */
    protected synchronized void initialize()  {
        int numImages = 0;
        
        final Map<Range,NetCDFVariableWrapper>indexMap = new HashMap<Range, NetCDFVariableWrapper>();
        final NetcdfDataset dataset = reader.getDataset();
        
        try {
	        if (dataset != null) {
	            checkType = NetCDFUtilities.getCheckType(dataset);
	
	            final List<Variable> variables = dataset.getVariables();
	            if (variables != null) {
	                for (final Variable variable : variables) {
	                    if (variable != null && variable instanceof VariableDS) {
	                        if (!NetCDFUtilities.isVariableAccepted(variable,checkType))
	                            continue;
	                        int[] shape = variable.getShape();
	                        switch (shape.length) {
	                        case 2:
	                        	indexMap.put(new Range(numImages, numImages + 1),new NetCDFVariableWrapper(variable));
	                            numImages++;
	                            break;
	                        case 3:
	                            indexMap.put(new Range(numImages, numImages+ shape[0]), new NetCDFVariableWrapper(variable));
	                            numImages += shape[0];
	                            break;
	                        case 4:
	                            indexMap.put(new Range(numImages, numImages+ shape[0] * shape[1]),new NetCDFVariableWrapper(variable));
	                            numImages += shape[0] * shape[1];
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	        else 
	        	throw new IllegalArgumentException( "Not a valid dataset has been found");
        } catch (InvalidRangeException e) {
        	 throw new IllegalArgumentException( "Error occurred during NetCDF file parsing", e);
		}
        reader.setIndexMap(indexMap);
        setNumImages(numImages);
        reader.setNumImages(numImages);
        int numAttribs = 0;
        final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
        if (globalAttributes != null && !globalAttributes.isEmpty())
        	numAttribs = globalAttributes.size();
        reader.setNumGlobalAttributes(numAttribs);
    }

    /**
     * Explicit Constructor getting {@link ImageReaderSpi} originatingProvider
     * as actual parameter.
     * 
     * @param originatingProvider
     *                {@link ImageReaderSpi}
     */
    public NetCDFImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
        reader = new BaseNetCDFImageReader(originatingProvider);
    }

    protected NetCDFVariableWrapper getVariableWrapper(int imageIndex) {
        return (NetCDFVariableWrapper) reader.getVariableWrapper(imageIndex);
    }

    /**
     * @see javax.imageio.ImageReader#getImageMetadata(int)
     */
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return new NetCDFImageMetadata(this, imageIndex);
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        return new NetCDFStreamMetadata(this);
    }


    /**
     * @see javax.imageio.ImageReader#read(int, javax.imageio.ImageReadParam)
     */
    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        clearAbortRequest();
        Variable variable = null;
        Range indexRange = null;
        NetCDFVariableWrapper wrapper = null;
        Map<Range,?> indexMap = reader.getIndexMap();
        for (Range range : indexMap.keySet()) {
            if (range.contains(imageIndex) && range.first() <= imageIndex
                    && imageIndex < range.last()) {
                wrapper = (NetCDFVariableWrapper) indexMap.get(range);
                indexRange = range;
                break;
            }
        }
        variable = wrapper.getVariable();

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
        final int rank = wrapper.getRank();
        final int bandDimension = rank - NetCDFUtilities.Z_DIMENSION;

        /*
         * Gets the destination image of appropriate size. We create it now
         * since it is a convenient way to get the number of destination bands.
         */
        final int width = wrapper.getWidth();
        final int height = wrapper.getHeight();
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
            switch (rank - i) {
            case NetCDFUtilities.X_DIMENSION: {
                first = srcRegion.x;
                length = srcRegion.width;
                stride = strideX;
                break;
            }
            case NetCDFUtilities.Y_DIMENSION: {
                first = srcRegion.y;
                length = srcRegion.height;
                stride = strideY;
                break;
            }
            default: {
                if (i == bandDimension) {
                    first = NetCDFUtilities.getZIndex(variable, indexRange,
                            imageIndex);
                } else {
                    first = NetCDFUtilities.getTIndex(variable, indexRange,
                            imageIndex);
                }
                length = 1;
                stride = 1;
                break;
            }
            }
            try {
                ranges.add(new Range(first, first + length - 1, stride));
            } catch (InvalidRangeException e) {
                throw netcdfFailure(e);
            }
        }
        final Section sections = new Section(ranges);

        /*
         * Setting SampleModel and ColorModel.
         */
        final SampleModel sampleModel = wrapper.getSampleModel().createCompatibleSampleModel(destWidth, destHeight);
        final ColorModel colorModel = ImageIOUtilities.createColorModel(sampleModel);

        final WritableRaster raster = Raster.createWritableRaster(sampleModel,
                new Point(0, 0));
        final BufferedImage image = new BufferedImage(colorModel, raster,
                colorModel.isAlphaPremultiplied(), null);

        /*
         * Reads the requested sub-region only.
         */
        processImageStarted(imageIndex);
        final int numDstBands = 1;
        final float toPercent = 100f / numDstBands;
        final int type = raster.getSampleModel().getDataType();
        final int xmin = destRegion.x;
        final int ymin = destRegion.y;
        final int xmax = destRegion.width + xmin;
        final int ymax = destRegion.height + ymin;
        for (int zi = 0; zi < numDstBands; zi++) {
//            final int srcBand = (srcBands == null) ? zi : srcBands[zi];
            final int dstBand = (dstBands == null) ? zi : dstBands[zi];
            final Array array;
            try {
                array = variable.read(sections);
            } catch (InvalidRangeException e) {
                throw netcdfFailure(e);
            }
            final IndexIterator it = array.getIndexIterator();
            // for (int y = ymax; --y >= ymin;) {
            for (int y = ymin; y < ymax; y++) {
                for (int x = xmin; x < xmax; x++) {
                    switch (type) {
                    case DataBuffer.TYPE_DOUBLE: {
                        raster.setSample(x, y, dstBand, it.getDoubleNext());
                        break;
                    }
                    case DataBuffer.TYPE_FLOAT: {
                        raster.setSample(x, y, dstBand, it.getFloatNext());
                        break;
                    }
                    case DataBuffer.TYPE_BYTE: {
                        byte b = it.getByteNext();
                        // int myByte = (0x000000FF & ((int) b));
                        // short anUnsignedByte = (short) myByte;
                        // raster.setSample(x, y, dstBand, anUnsignedByte);
                        raster.setSample(x, y, dstBand, b);
                        break;
                    }
                    default: {
                        raster.setSample(x, y, dstBand, it.getIntNext());
                        break;
                    }
                    }
                }
            }
            /*
             * Checks for abort requests after reading. It would be a waste of a
             * potentially good image (maybe the abort request occurred after we
             * just finished the reading) if we didn't implemented the
             * 'isCancel()' method. But because of the later, which is checked
             * by the NetCDF library, we can't assume that the image is
             * complete.
             */
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
            /*
             * Reports progress here, not in the deeper loop, because the costly
             * part is the call to 'variable.read(...)' which can't report
             * progress. The loop that copy pixel values is fast, so reporting
             * progress there would be pointless.
             */
            processImageProgress(zi * toPercent);
        }
        if (lastError != null) {
            throw new IIOException(lastError);
        }
        processImageComplete();
        return image;
    }

    /**
     * Wraps a generic exception into a {@link IIOException}.
     */
    private IIOException netcdfFailure(final Exception e) throws IOException {
        return new IIOException(new StringBuffer("Can't read file ").append(
        		reader.getDataset().getLocation()).toString(), e);
    }

    /**
     * Allows any resources held by this reader to be released. <BR>
     * TODO: To grant thread safety, we may prevent a user call of this method.
     * 
     * @throws IOException
     */
    public void dispose() {
        super.dispose();
        lastError = null;
        checkType = CheckType.UNSET;
        reader.dispose();
    }

    /**
     * Invoked by the NetCDF library when an error occurred during the read
     * operation. Users should not invoke this method directly.
     */
    public void setError (final String message) {
        lastError = message;
    }

    /**
     * Invoked by the NetCDF library during read operation in order to check if
     * the task has been canceled. Users should not invoke this method directly.
     */
    public boolean isCancel() {
        return abortRequested();
    }

    /**
     * Retrieve the scale factor for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getScale(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        double scale = Double.NaN;
        final String scaleS = reader.getAttributeAsString(imageIndex,
                NetCDFUtilities.DatasetAttribs.SCALE_FACTOR);
        if (scaleS != null && scaleS.trim().length() > 0)
            scale = Double.parseDouble(scaleS);
        return scale;
    }

    /**
     * Retrieve the fill value for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getFillValue(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        double fillValue = Double.NaN;
        final String fillValueS = reader.getAttributeAsString(imageIndex,
                NetCDFUtilities.DatasetAttribs.FILL_VALUE);
        if (fillValueS != null && fillValueS.trim().length() > 0)
            fillValue = Double.parseDouble(fillValueS);
        return fillValue;
    }

    /**
     * Retrieve the offset factor for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getOffset(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        double offset = Double.NaN;
        final String offsetS = reader.getAttributeAsString(imageIndex,
                NetCDFUtilities.DatasetAttribs.ADD_OFFSET);
        if (offsetS != null && offsetS.trim().length() > 0)
            offset = Double.parseDouble(offsetS);
        return offset;
    }

    /**
     * Retrieve the valid Range for the specified imageIndex. Return null if
     * parameters aren't available
     * 
     * @throws IOException
     */
    double[] getValidRange(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        double range[] = null;

        final String validRange = reader.getAttributeAsString(imageIndex,
                NetCDFUtilities.DatasetAttribs.VALID_RANGE, true);
        if (validRange != null && validRange.trim().length() > 0) {
            String validRanges[] = validRange.split(",");
            if (validRanges.length == 2) {
                range = new double[2];
                range[0] = Double.parseDouble(validRanges[0]);
                range[1] = Double.parseDouble(validRanges[1]);
            }
        } else {
        	final String validMin = reader.getAttributeAsString(imageIndex,
                    NetCDFUtilities.DatasetAttribs.VALID_MIN, true);
            final String validMax = reader.getAttributeAsString(imageIndex,
                    NetCDFUtilities.DatasetAttribs.VALID_MAX, true);
            if (validMax != null && validMax.trim().length() > 0
                    && validMin != null && validMin.trim().length() > 0) {
                range = new double[2];
                range[0] = Double.parseDouble(validMin);
                range[1] = Double.parseDouble(validMax);
            }
        }
        return range;
    }

//    Variable getVariable(final int imageIndex) {
//        Variable var = null;
//        final NetCDFVariableWrapper wrapper = getVariableWrapper(imageIndex);
//        if (wrapper != null)
//            var = wrapper.getVariable();
//        return var;
//    }
//
//    Variable getVariableByName(final String varName) {
//    	final List<Variable> varList = dataset.getVariables();
//        for (Variable var : varList) {
//            if (var.getName().equals(varName))
//                return var;
//        }
//        return null;
//    }

    CoordinateSystem getCoordinateSystem(Variable variable) {
        CoordinateSystem cs = null;
        if (variable != null) {
            final List<CoordinateSystem> systems = ((VariableDS) variable)
                    .getCoordinateSystems();
            if (!systems.isEmpty())
                cs = systems.get(0);
        }
        return cs;
    }

	@Override
	public int getHeight(int imageIndex) throws IOException {
		return reader.getHeight(imageIndex);
	}

	@Override
	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
			throws IOException {
		return reader.getImageTypes(imageIndex);
}

	@Override
	public int getWidth(int imageIndex) throws IOException {
		return reader.getWidth(imageIndex);
	}

	@Override
	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		super.setInput(input, seekForwardOnly, ignoreMetadata);
		reader.setInput(input, seekForwardOnly, ignoreMetadata);
		initialize();
	}

	@Override
	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input, seekForwardOnly, false);
	}

	@Override
	public void setInput(Object input) {
		this.setInput(input, false, false);
	}
}