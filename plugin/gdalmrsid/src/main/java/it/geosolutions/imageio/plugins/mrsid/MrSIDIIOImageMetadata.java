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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.imageio.gdalframework.GDALImageReader.GDALDatasetWrapper;

import java.util.Map;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;
import org.w3c.dom.Node;

/**
 * Specialization of {@link IIOMetadata} specific for the MrSID format. It
 * provides the user with the specific MrSID metadata.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class MrSIDIIOImageMetadata extends GDALCommonIIOImageMetadata {

	/** Name for these metadata. */
	public final static String mrsidImageMetadataName = "org_gdal_imageio_mrsid_metadata";

	public MrSIDIIOImageMetadata(GDALDatasetWrapper ds) {
		super(ds, mrsidImageMetadataName,
				"it.geosolutions.imageio.gdalframework.MrSIDIIOImageMetadataFormat");
	}

	public Node getAsTree(String formatName) {
		if (formatName.equalsIgnoreCase(mrsidImageMetadataName))
			return getMrSIDMetadataTree();
		return super.getAsTree(formatName);
	}

	private Node getMrSIDMetadataTree() {

		// Create root node
		final IIOMetadataNode root = new IIOMetadataNode(mrsidImageMetadataName);
		final Dataset ds = GDALUtilities.acquireDataSet(dsWrapper
				.getDatasetName(), gdalconst.GA_ReadOnly);
		final Map gdalMetadata = ds.GetMetadata_Dict("");
		
		// ImageDescriptor
		IIOMetadataNode node = new IIOMetadataNode("ImageDescriptor");
		node.setAttribute("IMAGE__INPUT_NAME",dsWrapper.getDatasetName());
		GDALUtilities.setNodeAttribute("IMAGE__INPUT_FILE_SIZE", gdalMetadata
				.get("IMAGE__INPUT_FILE_SIZE"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__DYNAMIC_RANGE_WINDOW",
				gdalMetadata.get("IMAGE__DYNAMIC_RANGE_WINDOW"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__DYNAMIC_RANGE_LEVEL",
				gdalMetadata.get("IMAGE__DYNAMIC_RANGE_LEVEL"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_VERSION",
				gdalMetadata.get("IMAGE__COMPRESSION_VERSION"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__TARGET_COMPRESSION_RATIO",
				gdalMetadata.get("IMAGE__TARGET_COMPRESSION_RATIO"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_NLEV",
				gdalMetadata.get("IMAGE__COMPRESSION_NLEV"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_WEIGHT",
				gdalMetadata.get("IMAGE__COMPRESSION_WEIGHT"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_GAMMA",
				gdalMetadata.get("IMAGE__COMPRESSION_GAMMA"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__COMPRESSION_BLOCK_SIZE",
				gdalMetadata.get("IMAGE__COMPRESSION_BLOCK_SIZE"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);	
		GDALUtilities.setNodeAttribute("IMAGE__CREATION_DATE",
				gdalMetadata.get("IMAGE__CREATION_DATE"), node,
				IIOMetadataFormat.DATATYPE_STRING);	
		GDALUtilities.setNodeAttribute("IMAGE__WIDTH",
				gdalMetadata.get("IMAGE__WIDTH"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		GDALUtilities.setNodeAttribute("IMAGE__HEIGHT",
				gdalMetadata.get("IMAGE__HEIGHT"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		GDALUtilities.setNodeAttribute("IMAGE__TRANSPARENT_DATA_VALUE",
				gdalMetadata.get("IMAGE__TRANSPARENT_DATA_VALUE"), node,
				IIOMetadataFormat.DATATYPE_STRING);	
		GDALUtilities.setNodeAttribute("IMAGE__COLOR_SCHEME",
				gdalMetadata.get("IMAGE__COLOR_SCHEME"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		GDALUtilities.setNodeAttribute("IMAGE__DATA_TYPE",
				gdalMetadata.get("IMAGE__DATA_TYPE"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		GDALUtilities.setNodeAttribute("IMAGE__BITS_PER_SAMPLE",
				gdalMetadata.get("IMAGE__BITS_PER_SAMPLE"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		root.appendChild(node);
		
		//Georeferencing
		node = new IIOMetadataNode("Georeferencing");
		GDALUtilities.setNodeAttribute("IMG__HORIZONTAL_UNITS", gdalMetadata
				.get("IMG__HORIZONTAL_UNITS"), node,
				IIOMetadataFormat.DATATYPE_STRING);
		GDALUtilities.setNodeAttribute("IMG__PROJECTION_TYPE", gdalMetadata
				.get("IMG__PROJECTION_TYPE"), node,
				IIOMetadataFormat.DATATYPE_STRING);
		GDALUtilities.setNodeAttribute("IMG__PROJECTION_NUMBER", gdalMetadata
				.get("IMG__PROJECTION_NUMBER"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		GDALUtilities.setNodeAttribute("IMG__PROJECTION_ZONE", gdalMetadata
				.get("IMG__PROJECTION_ZONE"), node,
				IIOMetadataFormat.DATATYPE_INTEGER);
		GDALUtilities.setNodeAttribute("IMG__SPHEROID_NAME", gdalMetadata
				.get("IMG__SPHEROID_NAME"), node,
				IIOMetadataFormat.DATATYPE_STRING);
		GDALUtilities.setNodeAttribute("IMG__SPHEROID_SEMI_MAJOR_AXIS", gdalMetadata
				.get("IMG__SPHEROID_SEMI_MAJOR_AXIS"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMG__SPHEROID_SEMI_MINOR_AXIS", gdalMetadata
				.get("IMG__SPHEROID_SEMI_MINOR_AXIS"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMG__SPHEROID_ECCENTRICITY_SQUARED", gdalMetadata
				.get("IMG__SPHEROID_ECCENTRICITY_SQUARED"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMG__SPHEROID_RADIUS", gdalMetadata
				.get("IMG__SPHEROID_RADIUS"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__XY_ORIGIN", gdalMetadata
				.get("IMAGE__XY_ORIGIN"), node,
				IIOMetadataFormat.DATATYPE_STRING);
		GDALUtilities.setNodeAttribute("IMAGE__X_RESOLUTION", gdalMetadata
				.get("IMAGE__X_RESOLUTION"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__Y_RESOLUTION", gdalMetadata
				.get("IMAGE__Y_RESOLUTION"), node,
				IIOMetadataFormat.DATATYPE_DOUBLE);
		GDALUtilities.setNodeAttribute("IMAGE__WKT", gdalMetadata
				.get("IMAGE__WKT"), node,
				IIOMetadataFormat.DATATYPE_STRING);
		root.appendChild(node);
		
		return root;

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
