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

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class GDALCommonIIOStreamMetadataFormat extends IIOMetadataFormatImpl {

	private static GDALCommonIIOStreamMetadataFormat theInstance = null;

	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {
		// TODO implement me
		return false;
	}

	private GDALCommonIIOStreamMetadataFormat() {
		super(GDALCommonIIOStreamMetadata.nativeMetadataFormatName,
				CHILD_POLICY_SOME);

		// root -> DataSets
		addElement("DataSets",
				GDALCommonIIOStreamMetadata.nativeMetadataFormatName,
				CHILD_POLICY_REPEAT);
		addAttribute("DataSets", "number", DATATYPE_STRING, true, null);

		// DataSets -> DataSet
		addElement("DataSet", "DataSets", CHILD_POLICY_EMPTY);
		addAttribute("DataSet", "name", DATATYPE_STRING, true, null);
	}

	public static synchronized IIOMetadataFormat getInstance() {
		if (theInstance == null) {
			theInstance = new GDALCommonIIOStreamMetadataFormat();
		}
		return theInstance;
	}

}
