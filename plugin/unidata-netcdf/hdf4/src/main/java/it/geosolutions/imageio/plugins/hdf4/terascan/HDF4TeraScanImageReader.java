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
package it.geosolutions.imageio.plugins.hdf4.terascan;

import it.geosolutions.imageio.plugins.hdf4.BaseHDF4ImageReader;
import it.geosolutions.imageio.plugins.netcdf.BaseNetCDFImageReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Specific Implementation of the <code>BaseHDF4ImageReader</code> needed
 * to work on Terascan produced HDF
 * 
 * @author Romagnoli Daniele
 */
public class HDF4TeraScanImageReader extends BaseHDF4ImageReader {

    /** The Products Dataset List contained within the Terascan File */
    private String[] productList;

    private IIOMetadata streamMetadata = null;

    public HDF4TeraScanImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Inner class to represent interesting attributes of a Terascan Dataset
     * 
     * @author Daniele Romagnoli, GeoSolutions.
     */
    private class TerascanDatasetWrapper extends HDF4DatasetWrapper{

       public TerascanDatasetWrapper(final Variable var) {
    	   super(var);
       }
    }

    /**
     * Retrieve Terascan specific information.
     * 
     * @throws IOException
     */
    protected void initializeProfile() throws IOException {
    	boolean checkProducts = true;
    	final NetcdfDataset dataset = reader.getDataset();
        if (dataset == null) {
            throw new IOException(
                    "Unable to initialize profile due to a null dataset");
        }
        final List<Variable> variables = dataset.getVariables();
        final List<Attribute> attributes = dataset.getGlobalAttributes();
        reader.setNumGlobalAttributes(attributes.size());
        productList = HDF4TeraScanProperties.refineProductList(variables);
        final int numImages = productList != null ? productList.length : 0;
        setNumImages(numImages);
        reader.setNumImages(numImages);

        final Map<Range,TerascanDatasetWrapper> indexMap = new HashMap<Range, TerascanDatasetWrapper>(numImages);

     // Scanning all the datasets
        try{
	        for (Variable var : variables) {
	            final String name = var.getName();
	            for (int j = 0; j < numImages; j++) {
	                // Checking if the actual dataset is a product.
	            	if (!checkProducts || name.equals(productList[j])) {
	                    // Updating the subDatasetsMap map
	            		indexMap.put(new Range(j,j+1), new TerascanDatasetWrapper(var));
	                    break;
	                }
	            }
	        }
        } catch (InvalidRangeException e) {
	    	throw new IllegalArgumentException( "Error occurred during NetCDF file parsing", e);
		}
	    reader.setIndexMap(indexMap);
    }

    /**
     * Returns a {@link TerascanDatasetWrapper} given a specified imageIndex.
     * 
     * @param imageIndex
     * @return a {@link TerascanDatasetWrapper}.
     */
    @Override
    protected HDF4DatasetWrapper getDatasetWrapper(int imageIndex) {
    	return (HDF4DatasetWrapper) reader.getVariableWrapper(imageIndex);
    }

    BaseNetCDFImageReader getInnerReader() {
		return reader;
	}
    
    public void dispose() {
        super.dispose();
        productList = null;
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
        String unsigned = getAttributeAsString(imageIndex, HDF4TeraScanProperties.DatasetAttribs.UNSIGNED);
        boolean isUnsigned = false;
        if (unsigned != null && unsigned.trim().length()>0)
        	isUnsigned = Boolean.parseBoolean(unsigned);
        	
        String validRange = getAttributeAsString(imageIndex, HDF4TeraScanProperties.DatasetAttribs.VALID_RANGE, isUnsigned);
        if (validRange != null && validRange.trim().length() > 0) {
            String validRanges[] = validRange.split(",");
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
        return HDF4TeraScanProperties.terascanProducts.get(productName).getNBands();
    }

    /**
     * Retrieve the longName for the specified imageIndex.
     * 
     * @throws IOException
     */
    String getLongName(final int imageIndex) throws IOException {
        String name = "";
        String nameS = getAttributeAsString(imageIndex, HDF4TeraScanProperties.DatasetAttribs.LONG_NAME);
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
        String scaleS = getAttributeAsString(imageIndex, HDF4TeraScanProperties.DatasetAttribs.SCALE_FACTOR);
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
        String offsetS = getAttributeAsString(imageIndex, HDF4TeraScanProperties.DatasetAttribs.ADD_OFFSET);
        if (offsetS != null && offsetS.trim().length() > 0)
            offset = Double.parseDouble(offsetS);
        return offset;
    }

    public void reset() {
        super.reset();
    }

	/**
	 * @see javax.imageio.ImageReader#getImageMetadata(int, java.lang.String, java.util.Set)
	 */
	@Override
	public IIOMetadata getImageMetadata(int imageIndex, String formatName,
			Set<String> nodeNames) throws IOException {
		initialize();
        checkImageIndex(imageIndex);
        if(formatName.equalsIgnoreCase(HDF4TeraScanImageMetadata.nativeMetadataFormatName))
        	return new HDF4TeraScanImageMetadata(this, imageIndex);
        
        // fallback on the super type metadata
		return super.getImageMetadata(imageIndex, formatName, nodeNames);
	}

	/**
	 * @see javax.imageio.ImageReader#getStreamMetadata(java.lang.String, java.util.Set)
	 */
	@Override
	public synchronized IIOMetadata getStreamMetadata(String formatName,
			Set<String> nodeNames) throws IOException {
		if(formatName.equalsIgnoreCase(HDF4TeraScanStreamMetadata.nativeMetadataFormatName)){
	        if (streamMetadata == null)
	            streamMetadata = new HDF4TeraScanStreamMetadata(this);
	        return streamMetadata;
		}
		return super.getStreamMetadata(formatName, nodeNames);
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    	return getImageMetadata(imageIndex, HDF4TeraScanImageMetadata.nativeMetadataFormatName, null);
    }
	
	public IIOMetadata getImageMetadata(int imageIndex, final String format) throws IOException {
    	return getImageMetadata(imageIndex, format, null);
    }
    
	public synchronized IIOMetadata getStreamMetadata() throws IOException {
		return getStreamMetadata(HDF4TeraScanStreamMetadata.nativeMetadataFormatName, null);
	}
	
}
