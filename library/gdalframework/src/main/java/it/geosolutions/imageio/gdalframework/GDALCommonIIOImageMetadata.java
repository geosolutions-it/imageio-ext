/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.gdalframework;

import it.geosolutions.imageio.gdalframework.GDALImageReader.GDALDatasetWrapper;

import java.awt.image.IndexColorModel;
import java.util.Iterator;
import java.util.List;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.gdal.gdal.GCP;
import org.w3c.dom.Node;

/**
 * Class representing common image metadata returned by a
 * {@link GDALImageReader}. A wide set of getters method allow to retrieve
 * several information directly from the metadata instance, without need of
 * getting the XML DOM nodes tree.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class GDALCommonIIOImageMetadata extends IIOMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.gdalframework.commonImageMetadata_1.0";

	public static final String nativeMetadataFormatClassName = "it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadataFormat";

	/**
	 * The internal {@link GDALDatasetWrapper} from which information will be
	 * retrieved in order to expose metadata
	 */
	protected final GDALDatasetWrapper dsWrapper;

	/** Array containing noData value for each band */
	private double[] noDataValues;

	/** Array containing maximum value for each band */
	private double[] maximums;

	/** Array containing minimum value for each band */
	private double[] minimums;

	/** Array containing scale value for each band */
	private double[] scales;

	/** Array containing offset value for each band */
	private double[] offsets;

	/** Array containing the number of overviews for each band */
	private int[] numOverviews;

	public GDALCommonIIOImageMetadata(final GDALDatasetWrapper ds) {
		this(ds, nativeMetadataFormatName, nativeMetadataFormatClassName);
	}

	public GDALCommonIIOImageMetadata(final GDALDatasetWrapper ds,
			String formatName, String formatClass) {
		super(false, formatName, formatClass, null, null);
		this.dsWrapper = ds;
	}

	private Node createCommonNativeTree() {
		assert Thread.holdsLock(this);
		// Create root node
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);

		// ////////////////////////////////////////////////////////////////////
		//
		// DatasetDescriptor
		//
		// ////////////////////////////////////////////////////////////////////
		IIOMetadataNode node = new IIOMetadataNode("DatasetDescriptor");
		node.setAttribute("name", dsWrapper.getDatasetName());
		node.setAttribute("description", dsWrapper.getDatasetDescription());
		node.setAttribute("driverName", dsWrapper.getDriverName());
		node
				.setAttribute("driverDescription", dsWrapper
						.getDriverDescription());
		node.setAttribute("projection", dsWrapper.getProjection());
		node
				.setAttribute("numGCPs", Integer.toString(dsWrapper
						.getGcpNumber()));
		node.setAttribute("gcpProjection", dsWrapper.getGcpProjection());
		root.appendChild(node);

		// ////////////////////////////////////////////////////////////////////
		//
		// RasterDimensions
		//
		// ////////////////////////////////////////////////////////////////////
		node = new IIOMetadataNode("RasterDimensions");
		node.setAttribute("width", Integer.toString(dsWrapper.getWidth()));
		node.setAttribute("height", Integer.toString(dsWrapper.getHeight()));
		node.setAttribute("tileWidth", Integer.toString(dsWrapper
				.getTileHeight()));
		node.setAttribute("tileHeight", Integer.toString(dsWrapper
				.getTileWidth()));
		node.setAttribute("numBands", Integer.toString(dsWrapper
				.getBandsNumber()));
		root.appendChild(node);

		// ////////////////////////////////////////////////////////////////////
		//
		// GeoTransform
		//
		// ////////////////////////////////////////////////////////////////////
		node = new IIOMetadataNode("GeoTransform");
		final double[] geotransform = dsWrapper.getGeoTransformation();
		final boolean hasgeoTransform = geotransform != null
				&& geotransform.length > 0;
		node.setAttribute("m0", hasgeoTransform ? Double
				.toString(geotransform[0]) : null);
		node.setAttribute("m1", hasgeoTransform ? Double
				.toString(geotransform[1]) : null);
		node.setAttribute("m2", hasgeoTransform ? Double
				.toString(geotransform[2]) : null);
		node.setAttribute("m3", hasgeoTransform ? Double
				.toString(geotransform[3]) : null);
		node.setAttribute("m4", hasgeoTransform ? Double
				.toString(geotransform[4]) : null);
		node.setAttribute("m5", hasgeoTransform ? Double
				.toString(geotransform[5]) : null);
		root.appendChild(node);

		// ////////////////////////////////////////////////////////////////////
		//
		// GCPS
		//
		// ////////////////////////////////////////////////////////////////////
		final int gcpNumber = dsWrapper.getGcpNumber();
		if (gcpNumber > 0) {
			IIOMetadataNode nodeGCPs = new IIOMetadataNode("GCPS");
			final List gcps = dsWrapper.getGcps();
			final Iterator it = gcps.iterator();
			while (it.hasNext()) {
				node = new IIOMetadataNode("GCP");
				final GCP gcp = (GCP) it.next();
				node.setAttribute("x", Double.toString(gcp.getGCPPixel()));
				node.setAttribute("y", Double.toString(gcp.getGCPLine()));
				node.setAttribute("id", gcp.getId());
				node.setAttribute("info", gcp.getInfo());
				node.setAttribute("lon", Double.toString(gcp.getGCPX()));
				node.setAttribute("lat", Double.toString(gcp.getGCPY()));
				node.setAttribute("elevation", Double.toString(gcp.getGCPZ()));
				nodeGCPs.appendChild(node);
			}
			root.appendChild(nodeGCPs);
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// BandsInfo
		//
		// ////////////////////////////////////////////////////////////////////
		final int numBand = dsWrapper.getBandsNumber();
		IIOMetadataNode bandsNode = new IIOMetadataNode("BandsInfo");

		initBandValues();

		// //
		//
		// BandInfos -> BandInfo
		//
		// //
		for (int i = 0; i < numBand; i++) {
			node = new IIOMetadataNode("BandInfo");
			node.setAttribute("index", Integer.toString(i));
			node.setAttribute("colorInterpretation", dsWrapper
					.getColorInterpretations() != null
					& dsWrapper.getColorInterpretations().length > i ? Integer
					.toBinaryString(dsWrapper.getColorInterpretations()[i])
					: "");
			node.setAttribute("noData", noDataValues != null
					&& noDataValues.length > i
					&& !Double.isNaN(noDataValues[i]) ? Double
					.toString(noDataValues[i]) : null);
			node.setAttribute("maximum",
					maximums != null && maximums.length > i
							&& !Double.isNaN(maximums[i]) ? Double
							.toString(maximums[i]) : null);
			node.setAttribute("minimum",
					minimums != null && minimums.length > i
							&& !Double.isNaN(minimums[i]) ? Double
							.toString(minimums[i]) : null);
			node.setAttribute("scale", scales != null && scales.length > i
					&& !Double.isNaN(scales[i]) ? Double.toString(scales[i])
					: null);
			node.setAttribute("offset", offsets != null && offsets.length > i
					&& !Double.isNaN(offsets[i]) ? Double.toString(offsets[i])
					: null);
			node.setAttribute("numOverviews", numOverviews != null
					&& numOverviews.length > i ? Integer
					.toString(numOverviews[i]) : null);
			bandsNode.appendChild(node);
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// BandInfos -> BandInfo -> ColorTable
		//
		// ////////////////////////////////////////////////////////////////////
		if (dsWrapper.getColorModel() instanceof IndexColorModel) {
			final IndexColorModel icm = (IndexColorModel) dsWrapper
					.getColorModel();
			final int mapSize = icm.getMapSize();
			IIOMetadataNode node1 = new IIOMetadataNode("ColorTable");
			node1.setAttribute("sizeOfLocalColorTable", Integer
					.toString(mapSize));
			final byte rgb[][] = new byte[3][mapSize];
			icm.getReds(rgb[0]);
			icm.getReds(rgb[1]);
			icm.getReds(rgb[2]);
			for (int i = 0; i < mapSize; i++) {
				IIOMetadataNode nodeEntry = new IIOMetadataNode(
						"ColorTableEntry");
				nodeEntry.setAttribute("index", Integer.toString(i));
				nodeEntry.setAttribute("red", Byte.toString(rgb[0][i]));
				nodeEntry.setAttribute("green", Byte.toString(rgb[1][i]));
				nodeEntry.setAttribute("blue", Byte.toString(rgb[2][i]));
				nodeEntry.setAttribute("alpha", Byte.toString(rgb[3][i]));
				node1.appendChild(nodeEntry);
			}
			node.appendChild(node1);
		}
		root.appendChild(bandsNode);
		return root;
	}

	/**
	 * Init all Bands properties (NoData, Maximums, Minimums, Scales and Offsets
	 * values)
	 */
	private void initBandValues() {
		assert Thread.holdsLock(this);
		initNoDataValues();
		initMaximums();
		initMinimums();
		initNumOverviews();
		initScales();
		initOffsets();
	}

	public Node getAsTree(String formatName) {
		synchronized (this) {
			initBandValues();
		}
		if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
			return createCommonNativeTree();
		
		throw new UnsupportedOperationException(formatName
				+ " is not a supported format name");
	}

	public boolean isReadOnly() {
		return true;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		throw new UnsupportedOperationException(
				"reset operation is not allowed");
	}

	public void reset() {
		throw new UnsupportedOperationException(
				"reset operation is not allowed");
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Dataset and Driver Properties
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Return the name of the dataset which is the source for this
	 * {@code IIOMetadata}
	 */
	public String getName() {
		return dsWrapper.getDatasetName();
	}

	/**
	 * Return the description of the dataset which is the source for this
	 * <code>IIOMetadata</code>
	 */
	public String getDescription() {
		return dsWrapper.getDatasetDescription();
	}

	/**
	 * Return the name of the GDAL driver used to open the source dataset for
	 * this <code>IIOMetadata</code>
	 */
	public String getDriverName() {
		return dsWrapper.getDriverName();
	}

	/**
	 * Return the description of the GDAL driver used to open the source dataset
	 * for this <code>IIOMetadata</code>
	 */
	public String getDriverDescription() {
		return dsWrapper.getDriverDescription();
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Raster Properties
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Return the number of bands of the dataset which is the source for this
	 * <code>IIOMetadata</code>
	 */
	public int getBandsNumber() {
		return dsWrapper.getBandsNumber();
	}

	/** Return the width of the image */
	public int getWidth() {
		return dsWrapper.getWidth();
	}

	/** Return the height of the image */
	public int getHeight() {
		return dsWrapper.getHeight();
	}

	/** Return the tile height of the image */
	public int getTileHeight() {
		return dsWrapper.getTileHeight();
	}

	/** Return the tile width of the image */
	public int getTileWidth() {
		return dsWrapper.getTileWidth();
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Referencing
	// 
	// ////////////////////////////////////////////////////////////////////////
	/** Return the projection */
	public String getProjection() {
		return dsWrapper.getProjection();
	}

	/** Return the grid to world transformation of the image */
	public double[] getGeoTransformation() {
		return (double[]) dsWrapper.getGeoTransformation().clone();
	}

	/** Return the number of Ground Control Points */
	public int getGcpNumber() {
		return dsWrapper.getGcpNumber();
	}

	/** return the Ground Control Point's projection */
	public String getGcpProjection() {
		return dsWrapper.getGcpProjection();
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Bands Properties
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the maximum value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public double getMaximum(final int bandIndex) {
		checkBandIndex(bandIndex);
		return maximums[bandIndex];
	}

	/** Initialize the array containing the maximum value for each band */
	private void initMaximums() {
		assert Thread.holdsLock(this);
		if (maximums == null) {
			final Double[] maxValues = dsWrapper.getMaximums();
			final int nMaximums = maxValues.length;
			maximums = new double[nMaximums];
			for (int i = 0; i < nMaximums; i++)
				maximums[i] = maxValues[i] != null ? maxValues[i].doubleValue()
						: Double.NaN;
		}
	}

	/**
	 * Returns the minimum value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public double getMinimum(final int bandIndex) {
		checkBandIndex(bandIndex);
		return minimums[bandIndex];
	}

	/** Initialize the array containing the minimum value for each band */
	private  void initMinimums() {
		assert Thread.holdsLock(this);
		if (minimums == null) {
			final Double[] minValues = dsWrapper.getMinimums();
			final int nMinimums = minValues.length;
			minimums = new double[nMinimums];
			for (int i = 0; i < nMinimums; i++)
				minimums[i] = minValues[i] != null ? minValues[i].doubleValue()
						: Double.NaN;
		}
	}

	/**
	 * Returns the number of overviews for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public int getNumOverviews(final int bandIndex) {
		checkBandIndex(bandIndex);
		return numOverviews[bandIndex];
	}

	/** Initialize the array containing the number of overviews for each band */
	private  void initNumOverviews() {
		assert Thread.holdsLock(this);
		if (numOverviews == null) {
			numOverviews = dsWrapper.getNumOverviews();
		}
	}

	/**
	 * Returns the scale value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public double getScale(final int bandIndex) {
		checkBandIndex(bandIndex);
		return scales[bandIndex];
	}

	/** Initialize the array containing the scale value for each band */
	private  void initScales() {
		assert Thread.holdsLock(this);
		if (scales == null) {
			final Double[] scaleValues = dsWrapper.getScales();
			final int nScales = scaleValues.length;
			scales = new double[nScales];
			for (int i = 0; i < nScales; i++)
				scales[i] = scaleValues[i] != null ? scaleValues[i]
						.doubleValue() : Double.NaN;
		}
	}

	/**
	 * Returns the offset value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public double getOffset(final int bandIndex) {
		checkBandIndex(bandIndex);
		return offsets[bandIndex];
	}

	/** Initialize the array containing the offset value for each band */
	private void initOffsets() {
		assert Thread.holdsLock(this);
		if (offsets == null) {
			final Double[] offsetValues = dsWrapper.getOffsets();
			final int nOffsets = offsetValues.length;
			offsets = new double[nOffsets];
			for (int i = 0; i < nOffsets; i++)
				offsets[i] = offsetValues[i] != null ? offsetValues[i]
						.doubleValue() : Double.NaN;
		}
	}

	/**
	 * Returns the noData value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public double getNoDataValue(final int bandIndex) {
		checkBandIndex(bandIndex);
		return noDataValues[bandIndex];
	}

	/** Initialize the array containing the noData value for each band */
	private void initNoDataValues() {
		assert Thread.holdsLock(this);
		if (noDataValues == null) {
			final Double[] noDataVals = dsWrapper.getNoDataValues();
			final int nNoDataValues = noDataVals.length;
			noDataValues = new double[nNoDataValues];
			for (int i = 0; i < nNoDataValues; i++)
				noDataValues[i] = noDataVals[i] != null ? noDataVals[i]
						.doubleValue() : Double.NaN;
		}
	}

	/**
	 * Check the validity of the specified band index. Band indexes are in the
	 * range [0, numBands -1 ]
	 * 
	 * @param bandIndex
	 *            the band index to be validated.
	 * @throws IndexOutOfBoundsException
	 *             in case the specified band index isn't in the valid range
	 */
	private synchronized void checkBandIndex(final int bandIndex) {
		initBandValues();
		final int bandsNum = dsWrapper.getBandsNumber();
		if (bandIndex < 0 || bandIndex > bandsNum) {
			final StringBuffer sb = new StringBuffer("Specified band index (")
					.append(bandIndex).append(
							") is out of range. It should be in the range [0,")
					.append(bandsNum - 1).append("]");
			throw new IndexOutOfBoundsException(sb.toString());
		}
	}


}
