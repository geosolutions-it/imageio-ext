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
package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.hdf.object.h4.H4Attribute;
import it.geosolutions.hdf.object.h4.H4SDS;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.hdf.object.h4.H4Utilities;
import it.geosolutions.imageio.plugins.jhdf.AbstractHDFImageReader;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
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
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.RasterFactory;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;

/**
 * {@link HDFAPSImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from APS generated HDF sources.
 * 
 * @author Daniele Romagnoli
 */
public class HDFAPSImageReader extends AbstractHDFImageReader {
	
    /** The Products Dataset List contained within the APS File */
    private String[] productList;

    /** The name of the SDS containing projection */
    private String projectionDatasetName;

    private int numGlobalAttributes;
    
    private HDFAPSStreamMetadata streamMetadata;
    
    private static ColorModel colorModel = RasterFactory
            .createComponentColorModel(DataBuffer.TYPE_FLOAT, // dataType
                    ColorSpace.getInstance(ColorSpace.CS_GRAY), // color space
                    false, // has alpha
                    false, // is alphaPremultiplied
                    Transparency.OPAQUE); // transparency

    private Map<Integer, APSDatasetWrapper> apsDatasetsWrapperMap = null;

    Map<String, String> projectionMap = null;

    private class APSDatasetWrapper {

        private H4SDS sds;

        private int width;

        private int height;

        private int tileHeight;

        private int tileWidth;

        private SampleModel sampleModel;

        public APSDatasetWrapper(int sdsNum) {
            // This constructor is called after checking h4SdsCollection is not
            // null
            sds = (H4SDS) getH4SDSCollection().get(sdsNum);
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
            sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width,
                    height, 1);
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
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        final List l = new java.util.ArrayList(5);
        APSDatasetWrapper apsw = getApsDatasetWrapper(imageIndex);
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(colorModel, apsw
                .getSampleModel());
        l.add(imageType);
        return l.iterator();
    }

    /**
     * Initialize main properties for this <code>HDFAPSImageReader</code>
     */
    protected void initializeProfile() throws IOException {
        final H4SDSCollection h4SDSCollection = getH4SDSCollection();
        if (h4SDSCollection == null) {
            throw new IOException(
                    "Unable to initialize profile due to a null H4SDS collection");
        }
        final int nSDS = h4SDSCollection.size();

        try {
            // //
            //
            // Getting projection dataset name
            //
            // //
            final H4Attribute navAttrib = h4SDSCollection
                    .getAttribute(HDFAPSProperties.PFA_NA_MAPPROJECTION);
            if (navAttrib != null) {
                final String attribValue = H4Utilities
                        .buildAttributeString(navAttrib);
                projectionDatasetName = attribValue;
            }

            final H4Attribute attrib = h4SDSCollection.getAttribute("prodList");
            int numImages = 0;
            if (attrib != null) {
                final String values = attrib.getValuesAsString();
                String products[] = values.split(",");
                productList = HDFAPSProperties.refineProductList(products);
                numImages = productList.length;

            } else {
                numImages = nSDS;
            }
            setNumImages(numImages);
            numGlobalAttributes = h4SDSCollection.getNumAttributes();
            subDatasetsMap = new LinkedHashMap<String, H4SDS>(numImages);
            apsDatasetsWrapperMap = new HashMap<Integer, APSDatasetWrapper>(
                    numImages);

            H4SDS sds;
            // //
            //
            // Setting spatial domain
            //
            // //

            // getting map dataset
            sds = h4SDSCollection.get(projectionDatasetName);
            if (sds != null
                    && sds.getName().equalsIgnoreCase(projectionDatasetName)) {
                // TODO: All projection share the same dataset
                // structure?
                Object data = sds.read();
                final int datatype = sds.getDatatype();
                if (projectionMap == null) {
                    projectionMap = buildProjectionAttributesMap(data, datatype);
                    // Force UoM of MapBoundary product as the last element in
                    // the map
                }
            }

            // Scanning all the datasets
            for (int i = 0; i < nSDS; i++) {
                sds = (H4SDS) h4SDSCollection.get(i);

                final String name = sds.getName();
                boolean added = false;
                for (int j = 0; j < numImages; j++) {
                    // Checking if the actual dataset is a product.
                    if (name.equals(productList[j])) {
                        // Updating the subDatasetsMap map
                        subDatasetsMap.put(name, sds);
                        apsDatasetsWrapperMap.put(Integer.valueOf(j),
                                new APSDatasetWrapper(i));
                        added = true;
                        break;
                    }
                }
                if (!added)
                    sds.dispose();
            }
        } catch (HDFException hde) {
            IOException ioe = new IOException(
                    "Error while Initializing APS data");
            ioe.initCause(hde);
            throw ioe;
        }
    }

    // /**
    // * Build a proper ISO8601 Time given start/end times obtained as
    // attributes
    // * of the input {@code H4SDSCollection}. In case start time is equal to
    // the
    // * end time, simply returns a Joda {@link Instant}. Otherwise, returns a
    // * proper Joda {@link Interval}.
    // *
    // * @param startTime
    // * the starting time
    // * @param endTime
    // * the ending time
    // * @return a proper Joda Time (Instant/Interval).
    // * @throws ParseException
    // */
    // private TemporalObject parseTemporalDomain(H4SDSCollection collection)
    // throws HDFException {
    // // TODO: Discover why parse set a compile pattern containing Japaneses
    // // chars.
    // final String startTime = collection.getAttribute(
    // HDFAPSProperties.STD_TA_TIMESTART).getValuesAsString();
    // final String endTime = collection.getAttribute(
    // HDFAPSProperties.STD_TA_TIMEEND).getValuesAsString();
    // // Setup an ISO8601 Start Time
    // final String iso8601StartTime = HDFAPSProperties
    // .buildISO8601Time(startTime);
    // try {
    // if (startTime.equalsIgnoreCase(endTime))
    // return new DefaultInstant(new DefaultPosition(Utils
    // .getDateFromString(iso8601StartTime)));
    //
    // // Setup an ISO8601 End Time in case it isn't equal to the startime
    // final String iso8601EndTime = HDFAPSProperties
    // .buildISO8601Time(endTime);
    //
    // // Build an Interval given start time and end time.
    // return new DefaultPeriod(new DefaultInstant(new DefaultPosition(
    // Utils.getDateFromString(iso8601StartTime))),
    // new DefaultInstant(new DefaultPosition(Utils
    // .getDateFromString(iso8601EndTime))));
    // } catch (ParseException pse) {
    // LOGGER
    // .warning("Error parsing the ISO8601 string; Fix the parseTemporalDomain
    // method."
    // + "For the moment, we return the actual date");
    // return new DefaultInstant(new DefaultPosition(new Date(System
    // .currentTimeMillis())));
    // }
    // }

    private Map<String,String> buildProjectionAttributesMap(final Object data, int datatype) {
        final Map<String,String> projMap = new LinkedHashMap<String,String>(29);

        if (datatype == HDFConstants.DFNT_FLOAT64) {
            double[] values = (double[]) data;
            // synchronized (projMap) {
            // TODO: I need to build a parser or a formatter to properly
            // interprete these settings
            projMap.put("Code", Double.toString(values[0]));
            projMap.put(HDFAPSStreamMetadata.PROJECTION, Double.toString(values[1]));
            projMap.put("Zone", Double.toString(values[2]));
            projMap.put(HDFAPSStreamMetadata.DATUM, Double.toString(values[3]));
            projMap.put(HDFAPSStreamMetadata.SEMI_MAJOR_AXIS, Double.toString(values[4]));
            projMap.put(HDFAPSStreamMetadata.SEMI_MINOR_AXIS, Double.toString(values[5]));
            projMap.put("Param2", Double.toString(values[6]));
            projMap.put("Param3", Double.toString(values[7]));
            projMap.put(HDFAPSStreamMetadata.LONGITUDE_OF_CENTRAL_MERIDIAN, Double.toString(values[8]));
            projMap.put(HDFAPSStreamMetadata.LATITUDE_OF_TRUE_SCALE, Double.toString(values[9]));
            projMap.put(HDFAPSStreamMetadata.FALSE_EASTINGS, Double.toString(values[10]));
            projMap.put(HDFAPSStreamMetadata.FALSE_NORTHINGS, Double.toString(values[11]));
            projMap.put("Param8", Double.toString(values[12]));
            projMap.put("Param9", Double.toString(values[13]));
            projMap.put("Param10", Double.toString(values[14]));
            projMap.put("Param11", Double.toString(values[15]));
            projMap.put("Param12", Double.toString(values[16]));
            projMap.put("Param13", Double.toString(values[17]));
            projMap.put("Param14", Double.toString(values[18]));
            projMap.put("Width", Double.toString(values[19]));
            projMap.put("Height", Double.toString(values[20]));
            projMap.put("Longitude_1", Double.toString(values[21]));
            projMap.put("Latitude_1", Double.toString(values[22]));
            projMap.put("Pixel_1", Double.toString(values[23]));
            projMap.put("Line_1", Double.toString(values[24]));
            projMap.put("Longitude_2", Double.toString(values[25]));
            projMap.put("Latitude_2", Double.toString(values[26]));
            projMap.put("Delta", Double.toString(values[27]));
            projMap.put("Aspect", Double.toString(values[28]));
            // }
        }
        return projMap;
    }

    public HDFAPSImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return new HDFAPSImageMetadata(this, imageIndex);
    }

    /**
     * Returns a {@link APSDatasetWrapper} given a specified imageIndex.
     * 
     * @param imageIndex
     * @return a {@link APSDatasetWrapper}.
     */
    synchronized APSDatasetWrapper getApsDatasetWrapper(int imageIndex) {
        checkImageIndex(imageIndex);
        APSDatasetWrapper wrapper = null;
        if (!apsDatasetsWrapperMap.containsKey(Integer.valueOf(imageIndex))) {
            wrapper = new APSDatasetWrapper(imageIndex);
            apsDatasetsWrapperMap.put(Integer.valueOf(imageIndex), wrapper);
        } else
            wrapper = (APSDatasetWrapper) apsDatasetsWrapperMap.get(Integer
                    .valueOf(imageIndex));
        return wrapper;
    }

    protected int getBandNumberFromProduct(String productName) {
        return HDFAPSProperties.apsProducts.get(productName).getNBands();
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
        return getApsDatasetWrapper(imageIndex).getWidth();
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
        return getApsDatasetWrapper(imageIndex).getHeight();
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
        return getApsDatasetWrapper(imageIndex).getTileHeight();
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
        return getApsDatasetWrapper(imageIndex).getTileWidth();
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

        final APSDatasetWrapper apsw = getApsDatasetWrapper(imageIndex);
        final H4SDS dataset = apsw.getSds();

        BufferedImage bimage = null;

        final int rank = dataset.getRank();
        final int width = apsw.getWidth();
        final int height = apsw.getHeight();
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
        final int[] dimSizes = dataset.getDimSizes();
        
//         int[] start = dataset.getStartDims();
//         int[] stride = dataset.getStride();
//         int[] sizes = dataset.getSelectedDims();

        // Setting variables needed to execute read operation.
        start[0] = srcRegionYOffset;
        start[1] = srcRegionXOffset;
        sizes[0] = dstHeight;
        sizes[1] = dstWidth;
        stride[0] = ySubsamplingFactor;
        stride[1] = xSubsamplingFactor;
        
        if (rank==3){
            start[2]=0;
            sizes[2]=dimSizes[2];
            stride[2]=1;
        }

        final int nBands = getBandNumberFromProduct(dataset.getName());

        // bands variables
        final int[] banks = new int[nBands];
        final int[] offsets = new int[nBands];
        for (int band = 0; band < nBands; band++) {
            banks[band] = band;
            offsets[band] = band;
        }

        // Setting SampleModel and ColorModel
        final int bufferType = H4Utilities.getBufferTypeFromDataType(datatype);
        SampleModel sm =null;
        if (nBands == 3)
            sm = new PixelInterleavedSampleModel(bufferType,
                    dstWidth, dstHeight, 3, dstWidth * 3,
                    offsets);
        else
            sm = new BandedSampleModel(bufferType, dstWidth, dstHeight,
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
            final int size = dstWidth * dstHeight * nBands;
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

        } catch (Exception e) {
            RuntimeException rte = new RuntimeException(
                    "Exception occurred while data Reading" + e);
            rte.initCause(e);
            throw rte;
        }

        return bimage;
    }

//    public Map<String, String> getProjectionMap() {
//        return Collections.unmodifiableMap(projectionMap);
//    }

    public synchronized void dispose() {
        super.dispose();
        productList = null;
        if (apsDatasetsWrapperMap!=null)
        	apsDatasetsWrapperMap.clear();
        apsDatasetsWrapperMap = null;
        streamMetadata = null;
        numGlobalAttributes = -1;
//        projectionMap.clear();
//        projectionMap = null;
    }

    String getDatasetName(final int imageIndex) {
        checkImageIndex(imageIndex);
        String datasetName = "";
        APSDatasetWrapper wrapper = getApsDatasetWrapper(imageIndex);
        if (wrapper != null) {
            datasetName = wrapper.getSds().getName();
        }
        return datasetName;
    }

    String getAttributeAsString(final int imageIndex,
            String attributeName) {
        String attributeValue = "";
        try {
            APSDatasetWrapper wrapper = getApsDatasetWrapper(imageIndex);
            if (wrapper != null) {
                H4Attribute attribute = wrapper.getSds().getAttribute(
                        attributeName);
                if (attribute != null) {
                    attributeValue = attribute.getValuesAsString();
                }
            }
        } catch (HDFException hdfe) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("HDF Exception while getting the attribute "
                        + attributeName);
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
		APSDatasetWrapper wrapper = getApsDatasetWrapper(imageIndex);
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

    String getGlobalAttributeAsString(String attributeName) {
        String attributeValue = "";
        try {
            final H4SDSCollection h4SDSCollection = getH4SDSCollection();
            if (h4SDSCollection != null) {
                H4Attribute attribute = h4SDSCollection
                        .getAttribute(attributeName);
                if (attribute != null) {
                    attributeValue = attribute.getValuesAsString();
                }
            }
        } catch (HDFException hdfe) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("HDF Exception while getting the attribute "
                        + attributeName);
        }
        return attributeValue;
    }
    
    public void reset(){
        super.reset();
    }
    
    public synchronized IIOMetadata getStreamMetadata() throws IOException {
        if (streamMetadata == null){
        	streamMetadata = new HDFAPSStreamMetadata(this);
//        	 final H4SDSCollection h4SDSCollection = getH4SDSCollection();
//             if (h4SDSCollection != null)
//            	 streamMetadata = new HDFAPSStreamMetadata(h4SDSCollection);
        }
        return streamMetadata;
    }
    
    int getNumAttributes(int imageIndex) {
        return getApsDatasetWrapper(imageIndex).getSds().getNumAttributes();
    }
    
    int getNumGlobalAttributes() {
        return numGlobalAttributes;
    }

}
