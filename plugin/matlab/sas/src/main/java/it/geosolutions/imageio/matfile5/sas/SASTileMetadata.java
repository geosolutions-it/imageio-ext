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

import java.util.HashSet;
import java.util.Set;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;

import org.w3c.dom.Node;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;

/**
 * 
 * @author Daniele
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

    public SASTileMetadata(MatFileReader matReader) {

        MLArray sasTileData = matReader.getMLArray(SAS_TILE_RAW);
        if (sasTileData != null) {
            logScale = false;
        } else {
            logScale = true;
            sasTileData = matReader.getMLArray(SAS_TILE_LOG);
        }
        if (sasTileData == null)
            throw new IllegalArgumentException(
                    "The provided input doesn't contain any valid SAS tile data");

        latitude = MatFileImageReader.getDouble(matReader, SAS_LATITUDE);
        longitude = MatFileImageReader.getDouble(matReader, SAS_LONGITUDE);
        orientation = MatFileImageReader.getDouble(matReader, SAS_ORIENTATION);
        final double pixelDims[] = new double[2];
        final double pixelsD[] = new double[2];
        MatFileImageReader.getDoubles(matReader, SAS_PIXEL_DIMS, pixelDims);
        MatFileImageReader.getDoubles(matReader, SAS_PIXELS, pixelsD);
        final int pixels[] = new int[] {
                Double.isNaN(pixelsD[0]) ? Integer.MIN_VALUE : (int) pixelsD[0],
                Double.isNaN(pixelsD[1]) ? Integer.MIN_VALUE : (int) pixelsD[1] };

        String channel = MatFileImageReader.getString(matReader, SAS_CHANNEL);

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

//    public double[][] getPings() {
//        return pings != null ? pings.getArray() : null;
//    }
//
//    public double[][] getX() {
//        return x != null ? x.getArray() : null;
//    }
//
//    public double[][] getY() {
//        return y != null ? y.getArray() : null;
//    }
//
//    public double[][] getTheta() {
//        return theta != null ? theta.getArray() : null;
//    }
//
//    public double[][] getMu() {
//        return mu != null ? mu.getArray() : null;
//    }
//
//    public double[][] getTileRanges() {
//        return tileRanges != null ? tileRanges.getArray() : null;
//    }
    
    //TODO: Refactor and implement this.
    public String getMetadataAsXML(){
        return null;
    }

}
