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

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class GDALCommonIIOImageMetadataFormat extends IIOMetadataFormatImpl implements
		IIOMetadataFormat {

	protected static GDALCommonIIOImageMetadataFormat commonInstace;

	public static synchronized IIOMetadataFormat getInstance() {
		if (commonInstace == null) {
			commonInstace = new GDALCommonIIOImageMetadataFormat();
		}
		return commonInstace;
	}

	protected GDALCommonIIOImageMetadataFormat() {
		super(GDALCommonIIOImageMetadata.nativeMetadataFormatName, CHILD_POLICY_SOME);

		// root -> DatasetDescriptor
		addElement("DatasetDescriptor",
				GDALCommonIIOImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
		addAttribute("DatasetDescriptor", "name", DATATYPE_STRING, true, null);
		addAttribute("DatasetDescriptor", "description", DATATYPE_STRING, true, null);
		addAttribute("DatasetDescriptor", "driverDescription", DATATYPE_STRING, true, null);
		
		addAttribute("DatasetDescriptor", "driverName", DATATYPE_STRING, true,
				null);
		addAttribute("DatasetDescriptor", "projection", DATATYPE_STRING, true,
				"");
		addAttribute("DatasetDescriptor", "numGCPs", DATATYPE_INTEGER, true,
				"0", "0", null, true, false);
		addAttribute("DatasetDescriptor", "gcpProjection", DATATYPE_STRING,
				true, "");
		
		// root -> RasterDimensions
		addElement("RasterDimensions",
				GDALCommonIIOImageMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
		addAttribute("RasterDimensions", "width", DATATYPE_INTEGER, true,
				null, "0", null, false, false);
		addAttribute("RasterDimensions", "height", DATATYPE_INTEGER, true,
				null, "0", null, false, false);
		addAttribute("RasterDimensions", "tileWidth", DATATYPE_INTEGER, true,
				null, "1", null, true, false);
		addAttribute("RasterDimensions", "tileHeight", DATATYPE_INTEGER, true,
				null, "1", null, true, false);
		addAttribute("RasterDimensions", "numBands", DATATYPE_INTEGER, true,
				"1", "1", null, true, false);
		
		// root -> GeoTransform
		addElement("GeoTransform", GDALCommonIIOImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_EMPTY);
		addAttribute("GeoTransform", "m0", DATATYPE_DOUBLE, false, null);
		addAttribute("GeoTransform", "m1", DATATYPE_DOUBLE, false, null);
		addAttribute("GeoTransform", "m2", DATATYPE_DOUBLE, false, null);
		addAttribute("GeoTransform", "m3", DATATYPE_DOUBLE, false, null);
		addAttribute("GeoTransform", "m4", DATATYPE_DOUBLE, false, null);
		addAttribute("GeoTransform", "m5", DATATYPE_DOUBLE, false, null);

		// root -> GCPS
		addElement("GCPS", GDALCommonIIOImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_REPEAT);
		addElement("GCP", "GCPS",
				CHILD_POLICY_EMPTY);
		addAttribute("GCP", "x", DATATYPE_INTEGER, true, null, "0", null, true,
				false);
		addAttribute("GCP", "y", DATATYPE_INTEGER, true, null, "0", null, true,
				false);
		addAttribute("GCP", "id", DATATYPE_STRING, true, "");
		addAttribute("GCP", "info", DATATYPE_STRING, false, "");
		addAttribute("GCP", "lon", DATATYPE_DOUBLE, true, null);
		addAttribute("GCP", "lat", DATATYPE_DOUBLE, true, null);
		addAttribute("GCP", "elevation", DATATYPE_DOUBLE, true, null);
		
		addElement("BandsInfo", GDALCommonIIOImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_REPEAT);
		// root -> BandsInfo -> BandInfo
		addElement("BandInfo", "BandsInfo",
				CHILD_POLICY_SOME);
		addAttribute("BandInfo", "index", DATATYPE_INTEGER, true, null, null, null, false,
				false);
		addAttribute("BandInfo", "colorInterpretation", DATATYPE_INTEGER, true, null, null, null, false,
				false);
		addAttribute( "BandInfo", "noData",DATATYPE_DOUBLE, false, Double
				.toString(Double.NaN), null, null, false, false);
		addAttribute("BandInfo", "scale", DATATYPE_DOUBLE, false, "1.0", "0.0",
				null, false, false);
		addAttribute("BandInfo", "offset", DATATYPE_DOUBLE, false, "0.0", null,
				null, false, false);
		addAttribute("BandInfo", "minimum", DATATYPE_DOUBLE, false, null,
				null, null, false, false);
		addAttribute("BandInfo", "maximum", DATATYPE_DOUBLE, false, null,
				null, null, false, false);
		addAttribute("BandInfo", "unit", DATATYPE_STRING, false, "");

		// root -> BandInfo -> ColorTable
		addElement("ColorTable", "BandInfo", 0, 256);
		addAttribute("ColorTable", "sizeOfLocalColorTable", DATATYPE_INTEGER,
				true, null);
		// root -> BandInfo -> ColorTable -> ColorTableEntry
		addElement("ColorTableEntry", "ColorTable", CHILD_POLICY_EMPTY);
		addAttribute("ColorTableEntry", "index", DATATYPE_INTEGER, true, null,
				"0", "255", true, true);
		addAttribute("ColorTableEntry", "red", DATATYPE_INTEGER, true, null,
				"0", "255", true, true);
		addAttribute("ColorTableEntry", "green", DATATYPE_INTEGER, true, null,
				"0", "255", true, true);
		addAttribute("ColorTableEntry", "blue", DATATYPE_INTEGER, true, null,
				"0", "255", true, true);
		addAttribute("ColorTableEntry", "alpha", DATATYPE_INTEGER, true, null,
				"0", "255", true, true);
	}

	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {
		//TODO implement me
		return true;
	}

}
