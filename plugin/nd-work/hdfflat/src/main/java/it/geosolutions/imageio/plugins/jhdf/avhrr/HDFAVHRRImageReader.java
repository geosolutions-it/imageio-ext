/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jhdf.avhrr;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4SDS;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.hdf.object.h4.H4Utilities;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFImageReader;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

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
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.hdflib.HDFException;

/**
 * Specific Implementation of the <code>AbstractHDFImageReader</code> needed
 * to work on AVHRR produced HDF
 * 
 * @author Romagnoli Daniele
 */
public class HDFAVHRRImageReader extends AbstractHDFImageReader {

    /** The Products Dataset List contained within the APS File */
    private String[] productList;

    private int numGlobalAttributes;

    private IIOMetadata streamMetadata = null;

    public HDFAVHRRImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    private Map<Integer, AVHRRDatasetWrapper> avhrrDatasetsWrapperMap = null;

    /**
     * Inner class to represent interesting attributes of a APS Dataset
     * 
     * @author Daniele Romagnoli, GeoSolutions.
     */
    private class AVHRRDatasetWrapper {

        private H4SDS sds;

        private int width;

        private int height;

        private int tileHeight;

        private int tileWidth;

        private SampleModel sampleModel;

        private int numAttributes;

        public AVHRRDatasetWrapper(int sdsIdentifier) {
            // This constructor is called after checking h4SdsCollection is not
            // null
            sds = (H4SDS) getH4SDSCollection().get(sdsIdentifier);
            final int dimSizes[] = sds.getDimSizes();
            final int chunkSizes[] = sds.getChunkSizes();

            width = dimSizes[1];
            height = dimSizes[0];
            if (chunkSizes == null) {
                tileWidth = Math.min(512, width);
                tileHeight = Math.min(512, height);
            } else {
                tileWidth = chunkSizes[1];
                tileHeight = chunkSizes[0];
            }
            // TODO: Set the proper data buffer type
            final int bufferType = H4Utilities.getBufferTypeFromDataType(sds
                    .getDatatype());
            numAttributes = sds.getNumAttributes();
            sampleModel = new BandedSampleModel(bufferType, width, height, 1);
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

        public H4SDS getSds() {
            return sds;
        }

        public int getNumAttributes() {
            return numAttributes;
        }
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        initialize();
        final List l = new java.util.ArrayList(5);
        AVHRRDatasetWrapper apsw = getAvhrrDatasetWrapper(imageIndex);
        SampleModel sm = apsw.getSampleModel();
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(ImageIOUtilities
                .getCompatibleColorModel(sm), sm);
        l.add(imageType);
        return l.iterator();
    }

    /**
     * Retrieve Avhrr specific information.
     * 
     * @throws IOException
     */
    protected void initializeProfile() throws IOException {
        int nSDS = 0;
        boolean checkProducts = true;
        final H4SDSCollection h4SDSCollection = getH4SDSCollection();
        if (h4SDSCollection == null) {
            throw new IOException(
                    "Unable to initialize profile due to a null H4SDS collection");
        }
        nSDS = h4SDSCollection.size();
        productList = HDFAVHRRProperties.refineProductList(h4SDSCollection);
        int numImages = productList.length;
        setNumImages(numImages);
        numGlobalAttributes = h4SDSCollection.getNumAttributes();

        subDatasetsMap = new HashMap<String, H4SDS>(numImages);
        avhrrDatasetsWrapperMap = new HashMap<Integer, AVHRRDatasetWrapper>(
                numImages);

        // Scanning all the datasets
        for (int i = 0; i < nSDS; i++) {
            final H4SDS sds = (H4SDS) h4SDSCollection.get(i);

            final String name = sds.getName();
            boolean added = false;
            for (int j = 0; j < numImages; j++) {

                // Checking if the actual dataset is a product.
                if (!checkProducts || name.equals(productList[j])) {
                    // Updating the subDatasetsMap map
                    subDatasetsMap.put(name, sds);
                    avhrrDatasetsWrapperMap.put(Integer.valueOf(j),
                            new AVHRRDatasetWrapper(i));
                    added = true;
                    break;
                }
            }

            if (!added)
                sds.dispose();
        }
    }

    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        initialize();
        checkImageIndex(imageIndex);
        return new HDFAVHRRImageMetadata(this, imageIndex);
    }

    /**
     * Returns a {@link AVHRRDatasetWrapper} given a specified imageIndex.
     * 
     * @param imageIndex
     * @return a {@link AVHRRDatasetWrapper}.
     */
    synchronized AVHRRDatasetWrapper getAvhrrDatasetWrapper(int imageIndex) {
        checkImageIndex(imageIndex);
        AVHRRDatasetWrapper wrapper = null;
        if (!avhrrDatasetsWrapperMap.containsKey(Integer.valueOf(imageIndex))) {
            wrapper = new AVHRRDatasetWrapper(imageIndex);
            avhrrDatasetsWrapperMap.put(Integer.valueOf(imageIndex), wrapper);
        } else
            wrapper = (AVHRRDatasetWrapper) avhrrDatasetsWrapperMap.get(Integer
                    .valueOf(imageIndex));
        return wrapper;
    }

    public synchronized void dispose() {
        super.dispose();
        productList = null;
        if (avhrrDatasetsWrapperMap!=null)
        	avhrrDatasetsWrapperMap.clear();
        avhrrDatasetsWrapperMap = null;
        numGlobalAttributes = -1;
        streamMetadata = null;
    }

    /**
     * Retrieve the ValidRange Parameters for the specified imageIndex. Return a
     * couple of NaN if parameters are not available
     * 
     * @throws IOException
     */
    double[] getValidRange(final int imageIndex) throws IOException {
        double[] range = new double[] { Double.NaN, Double.NaN };
        String validRange = getAttributeAsString(imageIndex,
                HDFAVHRRProperties.DatasetAttribs.VALID_RANGE);
        if (validRange != null && validRange.trim().length() > 0) {
            String validRanges[] = validRange.split(" ");
            if (validRanges.length == 2) {
                range = new double[2];
                range[0] = Double.parseDouble(validRanges[0]);
                range[1] = Double.parseDouble(validRanges[1]);
            }
        }
        return range;
    }

    /**
     * Retrieve the fillValue for the specified imageIndex.
     * 
     * @throws IOException
     */
    double getFillValue(final int imageIndex) throws IOException {
        double fillValue = Double.NaN;
        String fillS = getAttributeAsString(imageIndex,
                HDFAVHRRProperties.DatasetAttribs.FILL_VALUE);
        if (fillS != null && fillS.trim().length() > 0)
            fillValue = Double.parseDouble(fillS);
        return fillValue;
    }

    protected int getBandNumberFromProduct(String productName) {
        return HDFAVHRRProperties.avhrrProducts.get(productName).getNBands();
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
        return getAvhrrDatasetWrapper(imageIndex).getWidth();
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
        return getAvhrrDatasetWrapper(imageIndex).getHeight();
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
        return getAvhrrDatasetWrapper(imageIndex).getTileHeight();
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
        return getAvhrrDatasetWrapper(imageIndex).getTileWidth();
    }

    /**
     * Reads the image indexed by <code>imageIndex</code> and returns it as a
     * <code>BufferedImage</code>, using a supplied
     * <code>ImageReadParam</code>
     * 
     * @param imageIndex
     *                the index of the image to be retrieved.
     * @param param
     *                an <code>ImageReadParam</code> used to control the
     *                reading process, or <code>null</code>.
     * 
     * @return the desired portion of the image as a <code>BufferedImage</code>.
     */
    public BufferedImage read(final int imageIndex, ImageReadParam param)
            throws IOException {
        // ////////////////////////////////////////////////////////////////////
        //
        // INITIALIZATIONS
        //
        // ////////////////////////////////////////////////////////////////////

        initialize();

        final AVHRRDatasetWrapper avhrrw = getAvhrrDatasetWrapper(imageIndex);
        final H4SDS dataset = avhrrw.getSds();

        BufferedImage bimage = null;

        final int rank = 2;
        final int width = avhrrw.getWidth();
        final int height = avhrrw.getHeight();
        final int datatype = dataset.getDatatype();

        if (param == null)
            param = getDefaultReadParam();

        int dstWidth = -1;
        int dstHeight = -1;
        int srcRegionWidth = -1;
        int srcRegionHeight = -1;
        int srcRegionXOffset = -1;
        int srcRegionYOffset = -1;
        int xSubsamplingFactor = -1;
        int ySubsamplingFactor = -1;

        // //
        //
        // Retrieving Information about Source Region and doing
        // additional intialization operations.
        //
        // //
        Rectangle srcRegion = param.getSourceRegion();
        if (srcRegion != null) {
            srcRegionWidth = (int) srcRegion.getWidth();
            srcRegionHeight = (int) srcRegion.getHeight();
            srcRegionXOffset = (int) srcRegion.getX();
            srcRegionYOffset = (int) srcRegion.getY();

            // //
            //
            // Minimum correction for wrong source regions
            //
            // When you do subsampling or source subsetting it might
            // happen that the given source region in the read param is
            // uncorrect, which means it can be or a bit larger than the
            // original file or can begin a bit before original limits.
            //
            // We got to be prepared to handle such case in order to avoid
            // generating ArrayIndexOutOfBoundsException later in the code.
            //
            // //

            if (srcRegionXOffset < 0)
                srcRegionXOffset = 0;
            if (srcRegionYOffset < 0)
                srcRegionYOffset = 0;
            if ((srcRegionXOffset + srcRegionWidth) > width) {
                srcRegionWidth = width - srcRegionXOffset;
            }
            // initializing destWidth
            dstWidth = srcRegionWidth;

            if ((srcRegionYOffset + srcRegionHeight) > height) {
                srcRegionHeight = height - srcRegionYOffset;
            }
            // initializing dstHeight
            dstHeight = srcRegionHeight;

        } else {
            // Source Region not specified.
            // Assuming Source Region Dimension equal to Source Image
            // Dimension
            dstWidth = width;
            dstHeight = height;
            srcRegionXOffset = srcRegionYOffset = 0;
            srcRegionWidth = width;
            srcRegionHeight = height;
        }

        // SubSampling variables initialization
        xSubsamplingFactor = param.getSourceXSubsampling();
        ySubsamplingFactor = param.getSourceYSubsampling();

        // ////
        //
        // Updating the destination size in compliance with
        // the subSampling parameters
        //
        // ////

        dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
        dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

        // getting dataset properties.

        final int[] start = new int[rank];
        final int[] stride = new int[rank];
        final int[] sizes = new int[rank];

        // final int[] start = dataset.getStartDims();
        // final int[] stride = dataset.getStride();
        // final int[] sizes = dataset.getSelectedDims();

        // Setting variables needed to execute read operation.
        start[rank - 2] = srcRegionYOffset;
        start[rank - 1] = srcRegionXOffset;
        sizes[rank - 2] = dstHeight;
        sizes[rank - 1] = dstWidth;
        stride[rank - 2] = ySubsamplingFactor;
        stride[rank - 1] = xSubsamplingFactor;

        final int nBands = getBandNumberFromProduct(dataset.getName());

        // bands variables
        final int[] banks = new int[nBands];
        final int[] offsets = new int[nBands];
        for (int band = 0; band < nBands; band++) {
            banks[band] = band;
            offsets[band] = 0;
        }

        // Setting SampleModel and ColorModel
        final int bufferType = H4Utilities.getBufferTypeFromDataType(datatype);
        SampleModel sm = new BandedSampleModel(bufferType, dstWidth, dstHeight,
                dstWidth, banks, offsets);
        ColorModel cm = ImageIOUtilities.getCompatibleColorModel(sm);

        // ////////////////////////////////////////////////////////////////////
        //
        // DATA READ
        //
        // ////////////////////////////////////////////////////////////////////

        WritableRaster wr = null;
        final Object data;
        try {
            data = dataset.read(start, stride, sizes);
            final int size = dstWidth * dstHeight;
            DataBuffer dataBuffer = null;

            switch (bufferType) {
            case DataBuffer.TYPE_BYTE:
                dataBuffer = new DataBufferByte((byte[]) data, size);
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                dataBuffer = new DataBufferShort((short[]) data, size);
                break;
            case DataBuffer.TYPE_INT:
                dataBuffer = new DataBufferInt((int[]) data, size);
                break;
            case DataBuffer.TYPE_FLOAT:
                dataBuffer = new DataBufferFloat((float[]) data, size);
                break;
            case DataBuffer.TYPE_DOUBLE:
                dataBuffer = new DataBufferDouble((double[]) data, size);
                break;
            }

            wr = Raster.createWritableRaster(sm, dataBuffer, null);
            bimage = new BufferedImage(cm, wr, false, null);

        } catch (HDFException e) {
            RuntimeException rte = new RuntimeException(
                    "Exception occurred while data Reading" + e);
            rte.initCause(e);
            throw rte;
        }

        return bimage;
    }

    /**
     * Retrieve the longName for the specified imageIndex.
     * 
     * @throws IOException
     */
    String getLongName(final int imageIndex) throws IOException {
        String name = "";
        String nameS = getAttributeAsString(imageIndex,
                HDFAVHRRProperties.DatasetAttribs.LONG_NAME);
        if (nameS != null) {
            name = nameS;
        }
        return name;
    }

    /**
     * Retrieve the scale factor for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getScale(final int imageIndex) throws IOException {
        double scale = Double.NaN;
        String scaleS = getAttributeAsString(imageIndex,
                HDFAVHRRProperties.DatasetAttribs.SCALE_FACTOR);
        if (scaleS != null && scaleS.trim().length() > 0)
            scale = Double.parseDouble(scaleS);
        return scale;
    }

    /**
     * Retrieve the offset factor for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getOffset(final int imageIndex) throws IOException {
        double offset = Double.NaN;
        String offsetS = getAttributeAsString(imageIndex,
                HDFAVHRRProperties.DatasetAttribs.ADD_OFFSET);
        if (offsetS != null && offsetS.trim().length() > 0)
            offset = Double.parseDouble(offsetS);
        return offset;
    }

    String getAttributeAsString(int imageIndex, String tattributeNametributeName)
            throws IOException {
        initialize();
        String attributeValue = "";
        AVHRRDatasetWrapper wrapper = getAvhrrDatasetWrapper(imageIndex);
        H4Attribute attribute;
        try {
            attribute = wrapper.getSds()
                    .getAttribute(tattributeNametributeName);
            if (attribute != null) {
                attributeValue = attribute.getValuesAsString();
            }
        } catch (HDFException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }

        return attributeValue;
    }

    String getGlobalAttributeAsString(String attributeName) throws IOException {
        String attributeValue = "";
        H4Attribute attribute;
        try {
            attribute = getH4file().getH4SdsCollection().getAttribute(
                    attributeName);
            if (attribute != null) {
                attributeValue = attribute.getValuesAsString();
            }
        } catch (HDFException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }

        return attributeValue;
    }

    String getGlobalAttributeAsString(final int index) throws IOException {
        String attributePair = "";
        H4Attribute attribute;
        try {
            attribute = getH4file().getH4SdsCollection().getAttribute(index);
            if (attribute != null) {
                attributePair = attribute.getName() + SEPARATOR
                        + attribute.getValuesAsString();
            }
        } catch (HDFException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }

        return attributePair;
    }

    String getAttributeAsString(int imageIndex, final int attributeIndex)
            throws IOException {
        initialize();
        String attributePair = "";
        H4Attribute attribute;
        AVHRRDatasetWrapper wrapper = getAvhrrDatasetWrapper(imageIndex);
        try {
            attribute = wrapper.getSds().getAttribute(attributeIndex);
            if (attribute != null) {
                attributePair = attribute.getName() + SEPARATOR
                        + attribute.getValuesAsString();
            }
        } catch (HDFException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }

        return attributePair;
    }

    int getNumGlobalAttributes() {
        return numGlobalAttributes;
    }

    @Override
    public synchronized IIOMetadata getStreamMetadata() throws IOException {
        if (streamMetadata == null)
            streamMetadata = new HDFAVHRRStreamMetadata(this);
        return streamMetadata;
    }

    int getNumAttributes(int imageIndex) {
        return getAvhrrDatasetWrapper(imageIndex).getSds().getNumAttributes();
    }

    public void reset() {
        super.reset();
    }

}
