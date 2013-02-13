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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
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
public class NetCDFImageReader extends UcarImageReader implements CancelTask {

    protected final static Logger LOGGER = Logger.getLogger(NetCDFImageReader.class.toString());

    private CheckType checkType = CheckType.UNSET;

    /**
     * The NetCDF dataset, or {@code null} if not yet open. The NetCDF file is
     * open by {@link #ensureOpen} when first needed.
     */
    private NetcdfDataset dataset;

    private Map<Range, NetCDFVariableWrapper> indexMap;

    /**
     * The last error from the NetCDF library.
     */
    private String lastError;

    private int numGlobalAttributes;

    /**
     * Explicit Constructor getting {@link ImageReaderSpi} originatingProvider
     * as actual parameter.
     * 
     * @param originatingProvider the {@link ImageReaderSpi}
     */
    public NetCDFImageReader( ImageReaderSpi originatingProvider ) {
        super(originatingProvider);
    }

    public NetcdfDataset getDataset() {
        return dataset;
    }

    /**
     * Initialize main properties for this reader.
     * 
     * @throws exception
     *                 {@link InvalidRangeException}
     */
    protected synchronized void initialize() throws IOException {
        int numImages = 0;

        indexMap = new HashMap<Range, NetCDFVariableWrapper>();
        final NetcdfDataset dataset = getDataset();

        try {
            if (dataset != null) {
                checkType = NetCDFUtilities.getCheckType(dataset);

                final List<Variable> variables = dataset.getVariables();
                if (variables != null) {
                    for( final Variable variable : variables ) {
                        if (variable != null && variable instanceof VariableDS) {
                            if (!NetCDFUtilities.isVariableAccepted(variable, checkType))
                                continue;

                            // get the length of the variables in each dimension
                            int[] shape = variable.getShape();
                            switch( shape.length ) {
                            case 2:
                                indexMap.put(new Range(numImages, numImages + 1), new NetCDFVariableWrapper(variable));
                                numImages++;
                                break;
                            case 3:
                                indexMap.put(new Range(numImages, numImages + shape[0]), new NetCDFVariableWrapper(variable));
                                numImages += shape[0];
                                break;
                            case 4:
                                indexMap.put(new Range(numImages, numImages + shape[0] * shape[1]), new NetCDFVariableWrapper(
                                        variable));
                                numImages += shape[0] * shape[1];
                                break;
                            }
                        }
                    }
                }
            } else
                throw new IllegalArgumentException("Not a valid dataset has been found");
        } catch (InvalidRangeException e) {
            throw new IllegalArgumentException("Error occurred during NetCDF file parsing", e);
        }
        setNumImages(numImages);
        numGlobalAttributes = 0;
        final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
        if (globalAttributes != null && !globalAttributes.isEmpty())
            numGlobalAttributes = globalAttributes.size();
    }

    /**
     * @see javax.imageio.ImageReader#read(int, javax.imageio.ImageReadParam)
     */
    @Override
    public BufferedImage read( int imageIndex, ImageReadParam param ) throws IOException {
        clearAbortRequest();
        Variable variable = null;
        Range indexRange = null;
        NetCDFVariableWrapper wrapper = null;
        for( Range range : indexMap.keySet() ) {
            if (range.contains(imageIndex)
            /*
             * FIXME why is this even necessary?
             * - contains should handle it
             * - even if not, then shouldn't it be 
             *      imageIndex <= range.last()
             *      
             *      since the last is inclusive?
             */
            && range.first() <= imageIndex && imageIndex < range.last()) {
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
        // final int[] srcBands;
        final int[] dstBands;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
            // srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
        } else {
            strideX = 1;
            strideY = 1;
            // srcBands = null;
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

        /*
         * build the ranges that need to be read from each 
         * dimension based on the source region
         */
        final List<Range> ranges = new LinkedList<Range>();
        for( int i = 0; i < rank; i++ ) {
            final int first, length, stride;
            switch( rank - i ) {
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
                    first = NetCDFUtilities.getZIndex(variable, indexRange, imageIndex);
                } else {
                    first = NetCDFUtilities.getTIndex(variable, indexRange, imageIndex);
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

        /*
         * create the section of multidimensional array indices
         * that defines the exact data that need to be read 
         * for this image index and parameters 
         */
        final Section section = new Section(ranges);

        /*
         * Setting SampleModel and ColorModel.
         */
        final SampleModel sampleModel = wrapper.getSampleModel().createCompatibleSampleModel(destWidth, destHeight);
        final ColorModel colorModel = ImageIOUtilities.createColorModel(sampleModel);

        final WritableRaster raster = Raster.createWritableRaster(sampleModel, new Point(0, 0));
        final BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

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
        for( int zi = 0; zi < numDstBands; zi++ ) {
            // final int srcBand = (srcBands == null) ? zi : srcBands[zi];
            final int dstBand = (dstBands == null) ? zi : dstBands[zi];
            final Array array;
            try {
                array = variable.read(section);
            } catch (InvalidRangeException e) {
                throw netcdfFailure(e);
            }
            final IndexIterator it = array.getIndexIterator();
            // for (int y = ymax; --y >= ymin;) {
            for( int y = ymin; y < ymax; y++ ) {
                for( int x = xmin; x < xmax; x++ ) {
                    switch( type ) {
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
     * Allows any resources held by this reader to be released. <BR>
     * TODO: To grant thread safety, we may prevent a user call of this method.
     * 
     * @throws IOException
     */
    public void dispose() {
        super.dispose();
        lastError = null;
        checkType = CheckType.UNSET;

        indexMap.clear();
        indexMap = null;
        numGlobalAttributes = -1;
        numRasters = -1;
        try {
            if (dataset != null) {
                dataset.close();
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Errors closing NetCDF dataset." + e.getLocalizedMessage());
        } finally {
            dataset = null;
        }

    }

    public BaseVariableWrapper getVariableWrapper( int imageIndex ) {
        checkImageIndex(imageIndex);
        BaseVariableWrapper wrapper = null;
        for( Range range : indexMap.keySet() ) {
            if (range.contains(imageIndex) && range.first() <= imageIndex && imageIndex < range.last()) {
                wrapper = indexMap.get(range);
            }
        }
        return wrapper;
    }

    /**
     * @see javax.imageio.ImageReader#getImageMetadata(int)
     */
    @Override
    public IIOMetadata getImageMetadata( int imageIndex ) throws IOException {
        checkImageIndex(imageIndex);
        return new NetCDFImageMetadata(this, imageIndex);
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        return new NetCDFStreamMetadata(this);
    }

    /**
     * Wraps a generic exception into a {@link IIOException}.
     */
    private IIOException netcdfFailure( final Exception e ) throws IOException {
        return new IIOException(new StringBuffer("Can't read file ").append(getDataset().getLocation()).toString(), e);
    }

    /**
     * Invoked by the NetCDF library when an error occurred during the read
     * operation. Users should not invoke this method directly.
     */
    public void setError( final String message ) {
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
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s, {@code String}s, {@code URL}s, {@code ImageInputStream}s
     * are accepted input types.<BR>
     * Other parameters ({@code seekForwardOnly} and {@code ignoreMetadata})
     * are actually ignored.
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws exception
     *                 {@link IllegalArgumentException} in case the provided
     *                 input {@code Object} cannot be properly parsed and used
     *                 as input for the reader.
     */
    public void setInput( Object input, boolean seekForwardOnly, boolean ignoreMetadata ) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        try {
            if (dataset != null)
                reset();

            dataset = extractDataset(input);

            // super.setInput(input, seekForwardOnly, ignoreMetadata);

            initialize();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred during NetCDF file parsing", e);
        }
    }

    public int getNumGlobalAttributes() {
        return numGlobalAttributes;
    }

    /**
     * Mapping of imageIndex Ranges to NetCDF Variables.
     */
    private static class NetCDFVariableWrapper extends BaseVariableWrapper {
        public NetCDFVariableWrapper( Variable variable ) {
            super(variable);
            final int bufferType = NetCDFUtilities.getRawDataType(variable);
            setSampleModel(new BandedSampleModel(bufferType, getWidth(), getHeight(), 1));
        }
    }
}