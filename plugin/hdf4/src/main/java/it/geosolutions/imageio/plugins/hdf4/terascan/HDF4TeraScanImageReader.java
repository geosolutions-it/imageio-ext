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
package it.geosolutions.imageio.plugins.hdf4.terascan;

import it.geosolutions.imageio.plugins.hdf4.BaseHDF4ImageReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Specific Implementation of the <code>BaseHDF4ImageReader</code> needed
 * to work on AVHRR produced HDF
 * 
 * @author Romagnoli Daniele
 */
public class HDF4TeraScanImageReader extends BaseHDF4ImageReader {

    /** The Products Dataset List contained within the APS File */
    private String[] productList;

    private IIOMetadata streamMetadata = null;

    public HDF4TeraScanImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    private Map<Integer, TerascanDatasetWrapper> avhrrDatasetsWrapperMap = null;

    /**
     * Inner class to represent interesting attributes of a APS Dataset
     * 
     * @author Daniele Romagnoli, GeoSolutions.
     */
    private class TerascanDatasetWrapper extends HDF4DatasetWrapper{

       public TerascanDatasetWrapper(final Variable var) {
    	   super(var);
       }
    }

    /**
     * Retrieve Avhrr specific information.
     * 
     * @throws IOException
     */
    protected void initializeProfile() throws IOException {
    	boolean checkProducts = true;
    	final NetcdfDataset dataset = getDataset();
        if (dataset == null) {
            throw new IOException(
                    "Unable to initialize profile due to a null dataset");
        }
        final List<Variable> variables = dataset.getVariables();
        final List<Attribute> attributes = dataset.getGlobalAttributes();
        setNumGlobalAttributes(attributes.size());
        productList = HDF4TeraScanProperties.refineProductList(variables);
        int numImages = productList!=null?productList.length:0;
        setNumImages(numImages);

        avhrrDatasetsWrapperMap = new HashMap<Integer, TerascanDatasetWrapper>(
                numImages);

     // Scanning all the datasets
        for (Variable var : variables) {
            final String name = var.getName();
            for (int j = 0; j < numImages; j++) {
                // Checking if the actual dataset is a product.
            	if (!checkProducts || name.equals(productList[j])) {
                    // Updating the subDatasetsMap map
                	avhrrDatasetsWrapperMap.put(j, new TerascanDatasetWrapper(var));
                    break;
                }
            }
        }
    }

    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        initialize();
        checkImageIndex(imageIndex);
        return new HDF4TeraScanImageMetadata(this, imageIndex);
    }

    /**
     * Returns a {@link TerascanDatasetWrapper} given a specified imageIndex.
     * 
     * @param imageIndex
     * @return a {@link TerascanDatasetWrapper}.
     */
    @Override
    protected HDF4DatasetWrapper getDatasetWrapper(int imageIndex) {
        checkImageIndex(imageIndex);
        final TerascanDatasetWrapper wrapper = avhrrDatasetsWrapperMap.get(imageIndex);
        return wrapper;
    }

    public void dispose() {
        super.dispose();
        productList = null;
        if (avhrrDatasetsWrapperMap!=null)
        	avhrrDatasetsWrapperMap.clear();
        avhrrDatasetsWrapperMap = null;
        setNumGlobalAttributes(-1);
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
                HDF4TeraScanProperties.DatasetAttribs.VALID_RANGE);
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
                HDF4TeraScanProperties.DatasetAttribs.FILL_VALUE);
        if (fillS != null && fillS.trim().length() > 0)
            fillValue = Double.parseDouble(fillS);
        return fillValue;
    }

    protected int getBandNumberFromProduct(String productName) {
        return HDF4TeraScanProperties.avhrrProducts.get(productName).getNBands();
    }

    /**
     * Retrieve the longName for the specified imageIndex.
     * 
     * @throws IOException
     */
    String getLongName(final int imageIndex) throws IOException {
        String name = "";
        String nameS = getAttributeAsString(imageIndex,
                HDF4TeraScanProperties.DatasetAttribs.LONG_NAME);
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
                HDF4TeraScanProperties.DatasetAttribs.SCALE_FACTOR);
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
                HDF4TeraScanProperties.DatasetAttribs.ADD_OFFSET);
        if (offsetS != null && offsetS.trim().length() > 0)
            offset = Double.parseDouble(offsetS);
        return offset;
    }

    @Override
    public synchronized IIOMetadata getStreamMetadata() throws IOException {
        if (streamMetadata == null)
            streamMetadata = new HDF4TeraScanStreamMetadata(this);
        return streamMetadata;
    }

    int getNumAttributes(int imageIndex) {
        return getDatasetWrapper(imageIndex).getNumAttributes();
    }

    public void reset() {
        super.reset();
    }

}
