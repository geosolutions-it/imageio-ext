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

public class GDALCommonIIOImageMetadata extends IIOMetadata {
	// package scope
	
	public static final String nativeMetadataFormatName = "org_gdal_imageio_common_image_metadata_1.0";

	public static final String nativeMetadataFormatClassName = "it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadataFormat";

	protected GDALDatasetWrapper dsWrapper;

	public GDALCommonIIOImageMetadata(final GDALDatasetWrapper ds) {
		this(ds, nativeMetadataFormatName,
				nativeMetadataFormatClassName);

	}

	public GDALCommonIIOImageMetadata(final GDALDatasetWrapper ds,
			String formatName, String formatClass) {
		super(false, formatName, formatClass, null, null);
		this.dsWrapper = ds;

	}

	/**
	 * @param dsWrapper
	 */
	private Node getCommonNativeTree() {
		// Create root node
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);

		// DatasetDescriptor
		IIOMetadataNode node = new IIOMetadataNode("DatasetDescriptor");
		node.setAttribute("name", dsWrapper.getDatasetName());
		node.setAttribute("description", dsWrapper.getDatasetDescription());
		node.setAttribute("driverName", dsWrapper.getDrivername());
		node
				.setAttribute("driverDescription", dsWrapper
						.getDriverDescription());
		node.setAttribute("projection", dsWrapper.getProjection());
		node
				.setAttribute("numGCPs", Integer.toString(dsWrapper
						.getGcpNumber()));
		node.setAttribute("gcpProjection", dsWrapper.getGcpProjection());
		root.appendChild(node);

		// RasterDimensions
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

		// GeoTransform
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

		// GCPS
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

		// BandInfo
		final int numBand = dsWrapper.getBandsNumber();
		IIOMetadataNode bandsNode = new IIOMetadataNode("BandsInfo");
		for (int i = 0; i < numBand; i++) {
			node = new IIOMetadataNode("BandInfo");
			node.setAttribute("index", Integer.toString(i));
			node.setAttribute("colorInterpretation", dsWrapper
					.getColorInterpretations() != null
					& dsWrapper.getColorInterpretations().length > i ? Integer
					.toBinaryString(dsWrapper.getColorInterpretations()[i])
					: "");
			Double[] temp = dsWrapper.getNoDataValues();
			node.setAttribute("noData", temp != null && temp.length > i
					&& temp[i] != null ? temp[i].toString() : null);
			temp = dsWrapper.getMaximums();
			node.setAttribute("maximum", temp != null && temp.length > i
					&& temp[i] != null ? temp[i].toString() : null);
			temp = dsWrapper.getMinimums();
			node.setAttribute("minimum", temp != null && temp.length > i
					&& temp[i] != null ? temp[i].toString() : null);
			temp = dsWrapper.getScales();
			node.setAttribute("scale", temp != null && temp.length > i
					&& temp[i] != null ? temp[i].toString() : null);
			temp = dsWrapper.getOffsets();
			node.setAttribute("offset", temp != null && temp.length > i
					&& temp[i] != null ? temp[i].toString() : null);
			bandsNode.appendChild(node);
		}

		// BandInfos -> BandInfo -> ColorTable
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
				IIOMetadataNode nodeEntry = new IIOMetadataNode("ColorTableEntry");
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

	public Node getAsTree(String formatName) {
		if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
			return getCommonNativeTree();
		throw new UnsupportedOperationException(formatName
				+ " is not a supported format name");

	}

	public boolean isReadOnly() {
		// TODO change this
		return true;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		// TODO change this
		throw new UnsupportedOperationException("");

	}

	public void reset() {
		// TODO change this
		throw new UnsupportedOperationException("");

	}

}
