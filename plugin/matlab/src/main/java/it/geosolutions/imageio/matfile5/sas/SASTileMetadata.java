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
package it.geosolutions.imageio.matfile5.sas;

import it.geosolutions.imageio.matfile5.MatFileImageReader;
import it.geosolutions.imageio.matfile5.Utils;
import it.geosolutions.imageio.utilities.Utilities;

import java.util.HashSet;
import java.util.Set;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;

import org.w3c.dom.Node;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

/**
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public class SASTileMetadata extends IIOMetadata {

    private static Set<String> filterElements;

    static {
        // //
        // GeoReferencing Metadata
        // //
        filterElements = new HashSet<String>();
        filterElements.add(SASTileMetadata.SAS_TILE_RAW);
        filterElements.add(SASTileMetadata.SAS_TILE_LOG);
        filterElements.add(SASTileMetadata.SAS_PIXELS);
        filterElements.add(SASTileMetadata.SAS_LATITUDE);
        filterElements.add(SASTileMetadata.SAS_LONGITUDE);
        filterElements.add(SASTileMetadata.SAS_PIXEL_DIMS);
        filterElements.add(SASTileMetadata.SAS_ORIENTATION);
        filterElements.add(SASTileMetadata.SAS_CHANNEL);

        filterElements.add(SASTileMetadata.SAS_MU);
        filterElements.add(SASTileMetadata.SAS_X);
        filterElements.add(SASTileMetadata.SAS_Y);
        filterElements.add(SASTileMetadata.SAS_THETA);

        // Additional Metadata
        filterElements.add(SASTileMetadata.SAS_PINGS);
        filterElements.add(SASTileMetadata.SAS_TILE_RANGES);
        // filterElements.add(SASTileMetadata.SAS_AV_VELOCITY);
        // filterElements.add(SASTileMetadata.SAS_AV_ALTITUDE);

    }

    public static Set<String> getFilterElements() {
        return new HashSet<String>(filterElements);
    }
    
    /**
     * This method tries to guess if this matfile contains sas specific metadata or not.
     * 
     * @param matReader the {@link MatFileReader} that we need to use for guessing
     * @return <code>true</code> if this is a SAS matfile or <code>false</code> otherwise.
     */
    public static boolean isSASFile(final MatFileReader matReader) {
        MLArray sasTileData = matReader.getMLArray(SAS_TILE_RAW);
        if (sasTileData != null) {
            return true;
        } else {
            sasTileData = matReader.getMLArray(SAS_TILE_LOG);
        }
        if (sasTileData == null)
            return false;
        return true;
    }

    public static final String SAS_PIXELS = "pixels";
    public static final String SAS_LATITUDE = "latitude";
    public static final String SAS_LONGITUDE = "longitude";
    public static final String SAS_ORIENTATION = "orientation";
    public static final String SAS_PIXEL_DIMS = "pixel_dims";
    public static final String SAS_CHANNEL = "channel";
    public static final String SAS_TILE_RAW = "sas_tile_raw";
    public static final String SAS_TILE_LOG = "sas_tile_log";
    public static final String SAS_MU = "mu";
    public static final String SAS_PINGS = "pings";
    public static final String SAS_TILE_RANGES = "tile_ranges";
    public static final String SAS_AV_VELOCITY = "av_velocity";
    public static final String SAS_AV_ALTITUDE = "av_altitude";
    public static final String SAS_X = "x";
    public static final String SAS_Y = "y";
    public static final String SAS_THETA = "theta";

    public enum Channel {
        PORT, STARBOARD, UNKNOWN;

        public static Channel getChannel(String channel) {
            if (channel != null && channel.trim().length() > 0) {
                if (channel.equalsIgnoreCase("port"))
                    return PORT;
                else if (channel.equalsIgnoreCase("starboard"))
                    return STARBOARD;
            }
            return UNKNOWN;
        }
    };

    private int xPixels;

    private int yPixels;

    private double latitude;

    private double longitude;

    private double orientation;

    private double xPixelDim;

    private double yPixelDim;

    private boolean logScale;
    
    private int dataType;

    private Channel channel;

//    private MLDouble pings;
//
//    private MLDouble tileRanges;
//
//    private MLDouble x;
//
//    private MLDouble y;
//
//    private MLDouble theta;
//
//    private MLDouble mu;

    public SASTileMetadata(final MatFileReader matReader) {
    	Utilities.checkNotNull(matReader, "The provided MatFileReader was null");
        MLArray sasTileData = matReader.getMLArray(SAS_TILE_RAW);
        if (sasTileData != null) {
            logScale = false;
        } else {
            logScale = true;
            sasTileData = matReader.getMLArray(SAS_TILE_LOG);
        }
        Utilities.checkNotNull(sasTileData, "The provided input doesn't contain any valid SAS tile data");
        dataType = Utils.getDatatype(sasTileData);
        latitude = getDouble(matReader, SAS_LATITUDE);
        longitude = getDouble(matReader, SAS_LONGITUDE);
        orientation = getDouble(matReader, SAS_ORIENTATION);
        
        final double pixelDims[] = new double[2];
//        final int pixelsD[] = new int[2];
        int pixels[] = null;
        final int pixelType = getElementType(matReader, SAS_PIXELS);
        
        if (pixelType == MLArray.mxDOUBLE_CLASS){
        	final double pixelsD[] = new double[2];
        	getDoubles(matReader, SAS_PIXELS, pixelsD);
        	pixels = new int[] {
                    Double.isNaN(pixelsD[0]) ? Integer.MIN_VALUE : (int) pixelsD[0],
                    Double.isNaN(pixelsD[1]) ? Integer.MIN_VALUE : (int) pixelsD[1] };
        } else if (pixelType == MLArray.mxINT32_CLASS){
        	final int pixelsI[] = new int[2];
        	getIntegers(matReader, SAS_PIXELS, pixelsI);	
        	pixels = pixelsI;
        }
        
        getDoubles(matReader, SAS_PIXEL_DIMS, pixelDims);

        String channel = Utils.getString(matReader, SAS_CHANNEL);

//        mu = (MLDouble) matReader.getMLArray(SAS_MU);
//        x = (MLDouble) matReader.getMLArray(SAS_X);
//        y = (MLDouble) matReader.getMLArray(SAS_Y);
//        pings = (MLDouble) matReader.getMLArray(SAS_PINGS);
//        theta = (MLDouble) matReader.getMLArray(SAS_THETA);
//        tileRanges = (MLDouble) matReader.getMLArray(SAS_TILE_RANGES);

        // Setting values
        this.xPixels = pixels[0];
        this.yPixels = pixels[1];
        this.channel = Channel.getChannel(channel);
        this.xPixelDim = pixelDims[0];
        this.yPixelDim = pixelDims[1];

    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getXPixels() {
        return xPixels;
    }

    public void setXPixels(int pixels) {
        xPixels = pixels;
    }

    public int getYPixels() {
        return yPixels;
    }

    public void setYPixels(int pixels) {
        yPixels = pixels;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public boolean isLogScale() {
        return logScale;
    }

    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public double getXPixelDim() {
        return xPixelDim;
    }

    public void setXPixelDim(double pixelDim) {
        xPixelDim = pixelDim;
    }

    public double getYPixelDim() {
        return yPixelDim;
    }

    public void setYPixelDim(double pixelDim) {
        yPixelDim = pixelDim;
    }

    @Override
    public Node getAsTree(String formatName) {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
    }

    @Override
    public void reset() {

    }

    
    //TODO: Refactor and implement this.
    public String getMetadataAsXML(){
        return null;
    }
    
    static double[] getDoubles(final MatFileReader reader, final String element, double[] values) {
        MLArray array = reader.getMLArray(element);
        final MLDouble dArray = array != null ? (MLDouble) array : null;
        if (dArray != null) {
            final int nDims;
            if (values == null) {
                nDims = dArray.getM();
                values = new double[nDims];
            } else
                nDims = values.length;

            for (int i = 0; i < nDims; i++) {
                values[i] = dArray.get(i).doubleValue();
            }

        } else {
            if (values == null) {
                values = new double[] { Double.NaN, Double.NaN };
            } else {
                for (int i = 0; i < values.length; i++) {
                    values[i] = Double.NaN;
                }
            }
        }
        return values;
    }
    
    static int[] getIntegers(final MatFileReader reader, final String element, int[] values) {
        MLArray array = reader.getMLArray(element);
        final MLInt32 iArray = array != null ? (MLInt32) array : null;
        if (iArray != null) {
            final int nDims;
            if (values == null) {
                nDims = iArray.getM();
                values = new int[nDims];
            } else
                nDims = values.length;

            for (int i = 0; i < nDims; i++) {
                values[i] = iArray.get(i).intValue();
            }

        } else {
            if (values == null) {
                values = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE};
            } else {
                for (int i = 0; i < values.length; i++) {
                    values[i] = Integer.MAX_VALUE;
                }
            }
        }
        return values;

    }


    static double getDouble(final MatFileReader reader, final String element){
        return getDouble(reader, element,0);
    }
    
    static double getDouble(final MatFileReader reader, final String element, final int index) {
        double value = Double.NaN;
        if (element != null && reader!=null) {
            MLArray array = reader.getMLArray(element);
            final MLDouble arrayD = array != null ? (MLDouble) array : null;
            if (arrayD != null)
                value = arrayD.get(index).doubleValue();
        }
        return value;
    }
    
    static int getElementType (final MatFileReader reader, final String element){
    	Utilities.checkNotNull(reader, "The provided MatFileReader is null");
        if (reader != null){
            final MLArray array = reader.getMLArray(element);
            return Utils.getMatDatatype(array);   
        }
        return MLArray.mxUNKNOWN_CLASS;
    }

}
