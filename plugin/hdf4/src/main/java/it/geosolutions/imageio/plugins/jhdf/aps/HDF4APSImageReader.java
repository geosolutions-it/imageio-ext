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

import it.geosolutions.imageio.plugins.jhdf.BaseHDF4ImageReader;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * {@link HDF4APSImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from APS generated HDF sources.
 * 
 * @author Daniele Romagnoli
 */
public class HDF4APSImageReader extends BaseHDF4ImageReader {
	
    /** The Products Dataset List contained within the APS File */
    private String[] productList;

    /** The name of the SDS containing projection */
    private String projectionDatasetName;

    private HDFAPSStreamMetadata streamMetadata;
    
//    private static ColorModel colorModel = RasterFactory
//            .createComponentColorModel(DataBuffer.TYPE_FLOAT, // dataType
//                    ColorSpace.getInstance(ColorSpace.CS_GRAY), // color space
//                    false, // has alpha
//                    false, // is alphaPremultiplied
//                    Transparency.OPAQUE); // transparency

    private Map<Integer, APSDatasetWrapper> apsDatasetsWrapperMap = null;

    Map<String, String> projectionMap = null;

    private class APSDatasetWrapper extends HDF4DatasetWrapper{
        private APSDatasetWrapper(Variable var) {
        	super(var);
		}
    }

    /**
     * Initialize main properties for this <code>HDF4APSImageReader</code>
     */
    protected void initializeProfile() throws IOException {
        final NetcdfDataset dataset = getDataset();
        if (dataset == null) {
            throw new IOException(
                    "Unable to initialize profile due to a null dataset");
        }
        final List<Variable> variables = dataset.getVariables();
        final List<Attribute> attributes = dataset.getGlobalAttributes();
        final int numVars = variables.size();
        setNumGlobalAttributes(attributes.size());

        // //
        //
        // Getting projection dataset name
        //
        // //
        
        final String navAttrib = NetCDFUtilities.getGlobalAttributeAsString(dataset,
        		HDFAPSProperties.PFA_NA_MAPPROJECTION);
        if (navAttrib != null && navAttrib.length()>0) {
            projectionDatasetName = navAttrib;
        }

        final String prodAttrib = NetCDFUtilities.getGlobalAttributeAsString(dataset,
        		HDFAPSProperties.PRODLIST);
        int numImages = 0;
        if (prodAttrib != null && prodAttrib.length()>0) {
            String products[] = prodAttrib.split(",");
            productList = HDFAPSProperties.refineProductList(products);
            numImages = productList.length;
        } else {
            numImages = numVars;
        }
        setNumImages(numImages);
        apsDatasetsWrapperMap = new HashMap<Integer, APSDatasetWrapper>(
                numImages);

        Variable varProjection;
        // //
        //
        // Setting spatial domain
        //
        // //

        // getting map dataset
        varProjection = dataset.findVariable(projectionDatasetName);
        if (varProjection != null
                && varProjection.getName().equalsIgnoreCase(projectionDatasetName)) {
            // TODO: All projection share the same dataset
            // structure?
            Array data = varProjection.read();
            final int datatype = NetCDFUtilities.getRawDataType(varProjection);
            if (projectionMap == null) {
                projectionMap = buildProjectionAttributesMap(data, datatype);
                // Force UoM of MapBoundary product as the last element in
                // the map
            }
        }

        // Scanning all the datasets
        for (Variable var : variables) {
            final String name = var.getName();
            for (int j = 0; j < numImages; j++) {
                // Checking if the actual dataset is a product.
                if (name.equals(productList[j])) {
                    // Updating the subDatasetsMap map
                    apsDatasetsWrapperMap.put(j, new APSDatasetWrapper(var));
                    break;
                }
            }
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

    private Map<String,String> buildProjectionAttributesMap(final Array data, int datatype) {
        final Map<String,String> projMap = new LinkedHashMap<String,String>(29);

        if (datatype == DataBuffer.TYPE_DOUBLE && data instanceof ArrayDouble) {
            double[] values = (double[])data.get1DJavaArray(double.class);
            // synchronized (projMap) {
            // TODO: I need to build a parser or a formatter to properly
            // interprete these settings
            projMap.put("Code", Double.toString(values[0]));
            projMap.put(HDFAPSStreamMetadata.PROJECTION, Double.toString(values[1]));
            projMap.put(HDFAPSStreamMetadata.ZONE, Double.toString(values[2]));
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

    public HDF4APSImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
    
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return new HDF4APSImageMetadata(this, imageIndex);
    }

    /**
     * Returns a {@link APSDatasetWrapper} given a specified imageIndex.
     * 
     * @param imageIndex
     * @return a {@link APSDatasetWrapper}.
     */
    @Override
    protected HDF4DatasetWrapper getDatasetWrapper(final int imageIndex) {
        checkImageIndex(imageIndex);
        final APSDatasetWrapper wrapper = apsDatasetsWrapperMap.get(imageIndex);
        return wrapper;
    }

    protected int getBandNumberFromProduct(String productName) {
        return HDFAPSProperties.apsProducts.get(productName).getNBands();
    }

    public void dispose() {
        super.dispose();
        productList = null;
        if (apsDatasetsWrapperMap!=null)
        	apsDatasetsWrapperMap.clear();
        apsDatasetsWrapperMap = null;
        streamMetadata = null;
        setNumGlobalAttributes(-1);
//        projectionMap.clear();
//        projectionMap = null;
    }

    String getDatasetName(final int imageIndex) {
        checkImageIndex(imageIndex);
        String datasetName = "";
        APSDatasetWrapper wrapper = (APSDatasetWrapper) getDatasetWrapper(imageIndex);
        if (wrapper != null) {
            datasetName = wrapper.getVariable().getName();
        }
        return datasetName;
    }

//    String getAttributeAsString(final int imageIndex,
//            String attributeName) {
//        String attributeValue = "";
//        try {
//            APSDatasetWrapper wrapper = getApsDatasetWrapper(imageIndex);
//            if (wrapper != null) {
//                H4Attribute attribute = wrapper.getSds().getAttribute(
//                        attributeName);
//                if (attribute != null) {
//                    attributeValue = attribute.getValuesAsString();
//                }
//            }
//        } catch (HDFException hdfe) {
//            if (LOGGER.isLoggable(Level.WARNING))
//                LOGGER.warning("HDF Exception while getting the attribute "
//                        + attributeName);
//        }
//
//        return attributeValue;
//    }
//    
//    String getGlobalAttributeAsString(final int index) throws IOException {
//        String attributePair = "";
//        H4Attribute attribute;
//        try {
//            attribute = getH4file().getH4SdsCollection().getAttribute(index);
//            if (attribute != null) {
//                attributePair = attribute.getName() + SEPARATOR
//                        + attribute.getValuesAsString();
//            }
//        } catch (HDFException e) {
//            IOException ioe = new IOException();
//            ioe.initCause(e);
//            throw ioe;
//        }
//
//        return attributePair;
//    }
//    
//    String getAttributeAsString(int imageIndex, final int attributeIndex)
//			throws IOException {
//		initialize();
//		String attributePair = "";
//		H4Attribute attribute;
//		APSDatasetWrapper wrapper = getApsDatasetWrapper(imageIndex);
//		try {
//			attribute = wrapper.getSds().getAttribute(attributeIndex);
//			if (attribute != null) {
//				attributePair = attribute.getName() + SEPARATOR
//						+ attribute.getValuesAsString();
//			}
//		} catch (HDFException e) {
//			IOException ioe = new IOException();
//			ioe.initCause(e);
//			throw ioe;
//		}
//
//		return attributePair;
//	}

    public void reset(){
        super.reset();
    }
    
    public synchronized IIOMetadata getStreamMetadata() throws IOException {
        if (streamMetadata == null){
        	streamMetadata = new HDFAPSStreamMetadata(this);
        }
        return streamMetadata;
    }
    
    int getNumAttributes(int imageIndex) {
        return getDatasetWrapper(imageIndex).getNumAttributes();
    }
}
