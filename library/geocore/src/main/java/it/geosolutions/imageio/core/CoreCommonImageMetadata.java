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
package it.geosolutions.imageio.core;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * Class needed to store all available information of a 2D Dataset with the add
 * of additional information. For convenience and future re-use this class also
 * represents an {@link IIOMetadata}. A wide set of getters method allow to
 * retrieve several information directly from the metadata instance, without
 * need of getting the XML DOM nodes tree.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public abstract class CoreCommonImageMetadata extends IIOMetadata {

    /**
     * The name of the native metadata format for this object.
     */
    public static final String nativeMetadataFormatName = "it_geosolutions_imageio_core_commonImageMetadata_1.0";

    /**
     * The name of the class implementing <code>IIOMetadataFormat</code> and
     * representing the native metadata format for this object.
     */
    public static final String nativeMetadataFormatClassName = "it.geosolutions.imageio.core.CommonImageMetadataFormat";

    /**
     * the name of the driver which has opened the dataset represented by this
     * common metadata object.
     */
    private String driverName;

    /**
     * The description of the driver which has opened the dataset represented by
     * this common metadata object.
     */
    private String driverDescription;

    /** The dataset name */
    private String datasetName;

    /** The dataset description */
    private String datasetDescription;

    /** The dataset projection. */
    private String projection;

    /** The number of Ground Control Points */
    private int gcpNumber;

    /** The GCP's Projection */
    private String gcpProjection;

    /** The grid to world transformation. */
    private double[] geoTransformation;

     /**
     * The list of Ground Control Points. <BR>
     * Any Ground Control Point has the following fields:<BR>
     * <UL>
     * <LI>ID: Unique Identifier</LI>
     * <LI>Info: Informational message/description</LI>
     * <LI>x: GCPPixel -----> Pixel (x) location of GCP on Raster</LI>
     * <LI>y: GCPLine ------> Line(y) location of GCP on Raster</LI>
     * <LI>lon: GCPX -------> X position of GCP in Georeferenced Space</LI>
     * <LI>lat: GCPY -------> Y position of GCP in Georeferenced Space</LI>
     * <LI>elevation: GCPZ -> elevation of GCP in Georeferenced Space</LI>
     * </UL>
     */
    private List<GCP> gcps = Collections.emptyList();

    /** The raster width */
    private int width;

    /** The raster height */
    private int height;

    /** The raster tile height */
    private int tileHeight;

    /** The raster tile width */
    private int tileWidth;

    /** The <code>ColorModel</code> used for the dataset */
    private ColorModel colorModel;

    /** The <code>SampleModel</code> used for the dataset */
    private SampleModel sampleModel;

    // ////////////////////////////////////////////////
    // 
    // Band Properties
    //
    // ////////////////////////////////////////////////

    /** Number of bands */
    private int numBands;

    /** Array to store the maximum value for each band */
    private Double[] maximums;

    /** Array to store the minimum value for each band */
    private Double[] minimums;

    /** Array to store the noData value for each band */
    private Double[] noDataValues;

    /** Array to store the scale value for each band */
    private Double[] scales;

    /** Array to store the offset value for each band */
    private Double[] offsets;

    /** Array to store the number of numOverviews for each band */
    private int[] numOverviews;

    /** Array to store the color interpretation for each band */
    private int[] colorInterpretations;

    /** The nodata range for this reader, if any (may be null) */
    private double[] noData;

    /**
     * Private constructor
     */
    protected CoreCommonImageMetadata(
    		final boolean standardMetadataFormatSupported,
    		final String nativeMetadataFormatName,
    		final String nativeMetadataFormatClassName,
    		final String[] extraMetadataFormatNames,
    		final String[] extraMetadataFormatClassNames) {
        super(standardMetadataFormatSupported, nativeMetadataFormatName,nativeMetadataFormatClassName, extraMetadataFormatNames,extraMetadataFormatClassNames);
    }

    /**
     * Returns the XML DOM <code>Node</code> object that represents the root
     * of a tree of metadata contained within this object on its native format.
     * 
     * @return a root node containing common metadata exposed on its native
     *         format.
     */
    protected Node createCommonNativeTree() {
        // Create root node
        final IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);

        // ////////////////////////////////////////////////////////////////////
        //
        // DatasetDescriptor
        //
        // ////////////////////////////////////////////////////////////////////
        IIOMetadataNode node = new IIOMetadataNode("DatasetDescriptor");
        node.setAttribute("name", datasetName);
        node.setAttribute("description", datasetDescription);
        node.setAttribute("driverName", driverName);
        node.setAttribute("driverDescription", driverDescription);
        node.setAttribute("projection", projection);
        node.setAttribute("numGCPs", Integer.toString(gcpNumber));
        node.setAttribute("gcpProjection", gcpProjection);
        root.appendChild(node);

        // ////////////////////////////////////////////////////////////////////
        //
        // RasterDimensions
        //
        // ////////////////////////////////////////////////////////////////////
        node = new IIOMetadataNode("RasterDimensions");
        node.setAttribute("width", Integer.toString(width));
        node.setAttribute("height", Integer.toString(height));
        node.setAttribute("tileWidth", Integer.toString(tileWidth));
        node.setAttribute("tileHeight", Integer.toString(tileHeight));
        node.setAttribute("numBands", Integer.toString(numBands));
        root.appendChild(node);

        // ////////////////////////////////////////////////////////////////////
        //
        // GeoTransform
        //
        // ////////////////////////////////////////////////////////////////////
        node = new IIOMetadataNode("GeoTransform");
        final boolean hasgeoTransform = geoTransformation != null && geoTransformation.length > 0;
        node.setAttribute("m0", hasgeoTransform ? Double.toString(geoTransformation[0]) : null);
        node.setAttribute("m1", hasgeoTransform ? Double.toString(geoTransformation[1]) : null);
        node.setAttribute("m2", hasgeoTransform ? Double.toString(geoTransformation[2]) : null);
        node.setAttribute("m3", hasgeoTransform ? Double.toString(geoTransformation[3]) : null);
        node.setAttribute("m4", hasgeoTransform ? Double.toString(geoTransformation[4]) : null);
        node.setAttribute("m5", hasgeoTransform ? Double.toString(geoTransformation[5]) : null);
        root.appendChild(node);

        // ////////////////////////////////////////////////////////////////////
        //
        // GCPS
        //
        // ////////////////////////////////////////////////////////////////////
        if (gcpNumber > 0) {
            IIOMetadataNode nodeGCPs = new IIOMetadataNode("GCPS");
            final List<? extends GCP> gcps = getGCPs();
            if (gcps != null && !gcps.isEmpty()) {
                final Iterator<? extends GCP> it = gcps.iterator();
                while (it.hasNext()) {
                    node = new IIOMetadataNode("GCP");
                    final GCP gcp = it.next();
                    node.setAttribute("column", Double.toString(gcp.getColumn()));
                    node.setAttribute("row", Double.toString(gcp.getRow()));
                    node.setAttribute("id", gcp.getId());
                    node.setAttribute("info", gcp.getDescription());
                    node.setAttribute("easting", Double.toString(gcp.getEasting()));
                    node.setAttribute("northing", Double.toString(gcp.getNorthing()));
                    node.setAttribute("elevation", Double.toString(gcp.getElevation()));
                    nodeGCPs.appendChild(node);
                }
            }
            root.appendChild(nodeGCPs);
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // BandsInfo
        //
        // ////////////////////////////////////////////////////////////////////
        IIOMetadataNode bandsNode = new IIOMetadataNode("BandsInfo");

        // //
        //
        // BandsInfo -> BandInfo
        //
        // //
        for (int i = 0; i < numBands; i++) {
            node = new IIOMetadataNode("BandInfo");
            node.setAttribute(
            		"index", 
            		Integer.toString(i));
            node.setAttribute(
            		"colorInterpretation", 
            		colorInterpretations != null&& colorInterpretations.length > i ? Integer .toBinaryString(colorInterpretations[i]) : "");
            node.setAttribute(
            		"noData",
            		noDataValues != null && noDataValues.length > i&& noDataValues[i] != null ? noDataValues[i].toString() : null);
            node.setAttribute(
            		"maximum", 
            		maximums != null&& maximums.length > i && maximums[i] != null ? maximums[i].toString() : null);
            node.setAttribute(
            		"minimum", 
            		minimums != null && minimums.length > i && minimums[i] != null ? minimums[i].toString() : null);
            node.setAttribute(
            		"scale", 
            		scales != null && scales.length > i&& scales[i] != null ? scales[i].toString() : null);
            node.setAttribute(
            		"offset", 
            		offsets != null && offsets.length > i&& offsets[i] != null ? offsets[i].toString() : null);
            node.setAttribute(
            		"numOverviews", numOverviews != null&& numOverviews.length > i ? Integer.toString(numOverviews[i]) : null);
            bandsNode.appendChild(node);
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // BandsInfo -> BandInfo -> ColorTable
        //
        // ////////////////////////////////////////////////////////////////////
        if (colorModel instanceof IndexColorModel) {
            final IndexColorModel icm = (IndexColorModel) colorModel;
            final int mapSize = icm.getMapSize();
            final boolean hasAlpha = icm.hasAlpha();
            IIOMetadataNode node1 = new IIOMetadataNode("ColorTable");
            node1.setAttribute("sizeOfLocalColorTable", Integer.toString(mapSize));
            final byte rgb[][] = new byte[3 + (hasAlpha? 1:0)][mapSize];
            icm.getReds(rgb[0]);
            icm.getGreens(rgb[1]);
            icm.getBlues(rgb[2]);
            if (hasAlpha){
                icm.getAlphas(rgb[3]);
            }
            for (int i = 0; i < mapSize; i++) {
                IIOMetadataNode nodeEntry = new IIOMetadataNode("ColorTableEntry");
                nodeEntry.setAttribute("index", Integer.toString(i));
                nodeEntry.setAttribute("red", Byte.toString(rgb[0][i]));
                nodeEntry.setAttribute("green", Byte.toString(rgb[1][i]));
                nodeEntry.setAttribute("blue", Byte.toString(rgb[2][i]));
                if (hasAlpha){
                    nodeEntry.setAttribute("alpha", Byte.toString(rgb[3][i]));
                }
                node1.appendChild(nodeEntry);
            }
            node.appendChild(node1);
        }
        root.appendChild(bandsNode);
        return root;
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     * 
     * @param formatName
     *                the name of the requested metadata format. Note that
     *                actually, the only supported format name is the
     *                {@link CoreCommonImageMetadata#nativeMetadataFormatName}.
     *                Requesting other format names will result in an
     *                <code>IllegalArgumentException</code>
     */
    public Node getAsTree(String formatName) {
        if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
            return createCommonNativeTree();
        throw new IllegalArgumentException(formatName+ " is not a supported format name");
    }

    /**
     * Returns <code>true</code> since this object does not support the
     * <code>mergeTree</code>, <code>setFromTree</code>, and
     * <code>reset</code> methods.
     * 
     * @return <code>true</code> since this <code>IIOMetadata</code> object
     *         cannot be modified.
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Method unsupported. Calling this method will throws an
     * <code>UnsupportedOperationException</code>
     * 
     * @see javax.imageio.metadata.IIOMetadata#mergeTree()
     * 
     * @see #isReadOnly()
     */
    public void mergeTree(String formatName, Node root)
            throws IIOInvalidTreeException {
        throw new UnsupportedOperationException("mergeTree operation is not allowed");
    }

    /**
     * Method unsupported. Calling this method will throws an
     * <code>UnsupportedOperationException</code>
     * 
     * @see javax.imageio.metadata.IIOMetadata#reset()
     * 
     * @see #isReadOnly()
     */
    public void reset() {
        throw new UnsupportedOperationException( "reset operation is not allowed");
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // Dataset and Driver Properties
    // 
    // ////////////////////////////////////////////////////////////////////////
    /**
     * Returns the name of the dataset which is the source for this
     * <code>IIOMetadata</code>
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * Returns the description of the dataset which is the source for this
     * <code>IIOMetadata</code>
     */
    public String getDescription() {
        return datasetDescription;
    }

    /**
     * Returns the name of the GDAL driver used to open the source dataset for
     * this <code>IIOMetadata</code>
     */
    public String getDriverName() {
        return driverName;
    }

    /**
     * Returns the description of the GDAL driver used to open the source
     * dataset for this <code>IIOMetadata</code>
     */
    public String getDriverDescription() {
        return driverDescription;
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // Raster Properties
    // 
    // ////////////////////////////////////////////////////////////////////////
    /**
     * Returns the number of bands of the dataset which is the source for this
     * <code>IIOMetadata</code>
     */
    public int getNumBands() {
        return numBands;
    }

    /** Returns the width of the image */
    public int getWidth() {
        return width;
    }

    /** Returns the height of the image */
    public int getHeight() {
        return height;
    }

    /** Returns the tile height of the image */
    public int getTileHeight() {
        return tileHeight;
    }

    /** Returns the tile width of the image */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Returns the <code>ColorModel</code> for the dataset held by this
     * object.
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * Returns the <code>SampleModel</code> for the dataset held by this
     * object.
     */
    public SampleModel getSampleModel() {
        return sampleModel;
    }

    /**
     * Returns the data for this dataset, as a range. May be <code>null</code>
     */
    public double[] getNoData() {
        return noData;
    }

    /**
     * Sets the nodata for this dataset. May be <code>null</code>
     * @param noData
     */
    public void setNoData(double[] noData) {
        this.noData = noData;
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // Referencing
    // 
    // ////////////////////////////////////////////////////////////////////////
    /** Returns the projection */
    public String getProjection() {
        return projection;
    }

    protected void setGcps(final List<GCP> gcps) {
        this.gcps = gcps;
    }

    /** Returns the grid to world transformation of the image */
    public double[] getGeoTransformation() {
        return (double[]) geoTransformation.clone();
    }

    /** Returns the number of Ground Control Points */
    public int getGcpNumber() {
        return gcpNumber;
    }

    /** Returns the Ground Control Point's projection */
    public String getGcpProjection() {
        return gcpProjection;
    }

    /** Returns the Ground Control Points */
    public List<GCP> getGCPs() {
        return Collections.unmodifiableList(gcps);
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // Bands Properties
    // 
    // ////////////////////////////////////////////////////////////////////////
    public Double[] getMaximums() {
        return maximums == null ? null : maximums.clone();
    }

    protected void setMaximums(Double[] maximums) {
        if (this.maximums!=null)
            throw new UnsupportedOperationException("maximums have already been defined");
        this.maximums = maximums;
    }

    public Double[] getMinimums() {
        return minimums == null ? null : minimums.clone();
    }

    protected void setMinimums(Double[] minimums) {
        if (this.minimums!=null)
            throw new UnsupportedOperationException("minimums have already been defined");
        this.minimums = minimums;
    }

    public Double[] getNoDataValues() {
        return noDataValues == null ? null : noDataValues.clone();
    }

    protected void setNoDataValues(Double[] noDataValues) {
        if (this.noDataValues!=null)
            throw new UnsupportedOperationException("noDataValues have already been defined");
        this.noDataValues = noDataValues;
    }

    public Double[] getScales() {
        return scales == null ? null : scales.clone();
    }

    public void setScales(Double[] scales) {
        if (this.scales!=null)
            throw new UnsupportedOperationException("scales have already been defined");
        this.scales = scales;
    }

    public Double[] getOffsets() {
        return offsets == null ? null : offsets.clone();
    }

    public void setOffsets(final Double[] offsets) {
        if (this.offsets!=null)
            throw new UnsupportedOperationException("offsets have already been defined");
        this.offsets = offsets;
    }

    public int[] getNumOverviews() {
        return numOverviews == null ? null : numOverviews.clone();
    }

    protected void setNumOverviews(final int[] numOverviews) {
        this.numOverviews = numOverviews.clone();
    }
    
    /**
     * Returns the number of overviews for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range
     */
    public int getNumOverviews(final int bandIndex) {
        checkBandIndex(bandIndex);
        return numOverviews[bandIndex];
    }

    /**
     * Returns the colorInterpretation for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range
     */
    public int getColorInterpretations(final int bandIndex) {
        checkBandIndex(bandIndex);
        return colorInterpretations[bandIndex];
    }

    /**
     * Returns the maximum value for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range or
     *                 maximum value has not been found
     */
    public double getMaximum(final int bandIndex)
            throws IllegalArgumentException {
        checkBandIndex(bandIndex);
        if (maximums != null) {
            Double maximum = maximums[bandIndex];
            if (maximum != null)
                return maximum.doubleValue();
        }
        throw new IllegalArgumentException(
                "no maximum value available for the specified band "
                        + bandIndex);
    }

    /**
     * Returns the minimum value for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range or
     *                 minimum value has not been found
     */
    public double getMinimum(final int bandIndex)
            throws IllegalArgumentException {
        checkBandIndex(bandIndex);
        if (minimums != null) {
            Double minimum = minimums[bandIndex];
            if (minimum != null)
                return minimum.doubleValue();
        }
        throw new IllegalArgumentException(
                "no minimum value available for the specified band "
                        + bandIndex);
    }

    /**
     * Returns the scale value for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range or
     *                 scale value has not been found
     */
    public double getScale(final int bandIndex) throws IllegalArgumentException {
        checkBandIndex(bandIndex);
        if (scales != null) {
            Double scale = scales[bandIndex];
            if (scale != null)
                return scale.doubleValue();
        }
        throw new IllegalArgumentException(
                "no scale value available for the specified band " + bandIndex);
    }

    /**
     * Returns the offset value for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range or
     *                 offset value has not been found
     */
    public double getOffset(final int bandIndex)
            throws IllegalArgumentException {
        checkBandIndex(bandIndex);
        if (offsets != null) {
            Double offset = offsets[bandIndex];
            if (offset != null)
                return offset.doubleValue();
        }
        throw new IllegalArgumentException(
                "no Offset value available for the specified band " + bandIndex);
    }

    /**
     * Returns the noDataValue value for the specified band
     * 
     * @param bandIndex
     *                the index of the required band
     * @throws IllegalArgumentException
     *                 in case the specified band number is out of range or
     *                 noDataValue has not been found
     */
    public double getNoDataValue(final int bandIndex)
            throws IllegalArgumentException {
        checkBandIndex(bandIndex);
        if (noDataValues != null) {
            Double noDataValue = noDataValues[bandIndex];
            if (noDataValue != null)
                return noDataValue.doubleValue();
        }
        throw new IllegalArgumentException(
                "no noDataValue available for the specified band " + bandIndex);
    }

    /**
     * Check the validity of the specified band index. Band indexes are in the
     * range [0, numBands -1 ]
     * 
     * @param bandIndex
     *                the band index to be validated.
     * @throws IllegalArgumentException
     *                 in case the specified band index isn't in the valid range
     */
    private void checkBandIndex(final int bandIndex)
            throws IllegalArgumentException {
        if (bandIndex < 0 || bandIndex > numBands) {
            final StringBuilder sb = new StringBuilder("Specified band index (")
                    .append(bandIndex).append( ") is out of range. It should be in the range [0,")
                    .append(numBands - 1).append("]");
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public String getDatasetDescription() {
        return datasetDescription;
    }

    protected void setDatasetDescription(String datasetDescription) {
        this.datasetDescription = datasetDescription;
    }

    protected void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    protected void setDriverDescription(String driverDescription) {
        this.driverDescription = driverDescription;
    }

    protected void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    protected void setProjection(String projection) {
        this.projection = projection;
    }

    protected void setGcpNumber(int gcpNumber) {
        this.gcpNumber = gcpNumber;
    }

    protected void setGcpProjection(String gcpProjection) {
        this.gcpProjection = gcpProjection;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    protected void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    protected void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    protected void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    protected void setSampleModel(SampleModel sampleModel) {
        this.sampleModel = sampleModel;
    }

    protected void setNumBands(int numBands) {
        this.numBands = numBands;
    }

    protected void setGeoTransformation(double[] geoTransformation) {
        if (this.geoTransformation!=null)
            throw new UnsupportedOperationException("geoTransformation have already been defined");
        this.geoTransformation = geoTransformation;
    }

    protected int[] getColorInterpretations() {
        return (int[]) colorInterpretations.clone();
    }

    protected void setColorInterpretations(int[] colorInterpretations) {
        if (this.colorInterpretations!=null)
            throw new UnsupportedOperationException("colorInterpretations have already been defined");
        this.colorInterpretations = colorInterpretations;
    }

}
