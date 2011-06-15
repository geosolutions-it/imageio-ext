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
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata.RasterSpaceType;

import java.util.Arrays;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Class defining the structure of metadata documents describing AsciiGrid image
 * metadata.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public final class AsciiGridsImageMetadataFormat extends IIOMetadataFormatImpl {
	private static IIOMetadataFormat instance = null;

	protected AsciiGridsImageMetadataFormat() {
		super(AsciiGridsImageMetadata.nativeMetadataFormatName,
				IIOMetadataFormatImpl.CHILD_POLICY_ALL);

		// root -> FormatDescriptor
		addElement("FormatDescriptor",
				AsciiGridsImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_EMPTY);
		addAttribute("FormatDescriptor", "GRASS", DATATYPE_BOOLEAN, true, null);

		// root -> GridDescriptor
		addElement("GridDescriptor",
				AsciiGridsImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_EMPTY);
		addAttribute("GridDescriptor", "nColumns", DATATYPE_INTEGER, true, null);
		addAttribute("GridDescriptor", "nRows", DATATYPE_INTEGER, true, null);

		addAttribute("GridDescriptor", "rasterSpaceType", DATATYPE_STRING,
				true, null, Arrays.asList(RasterSpaceType.PixelIsArea.toString(),RasterSpaceType.PixelIsPoint.toString()	));
		addAttribute("GridDescriptor", "noDataValue", DATATYPE_DOUBLE, false,
				null);

		// root -> EnvelopeDescriptor
		addElement("EnvelopeDescriptor",
				AsciiGridsImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_EMPTY);
		addAttribute("EnvelopeDescriptor", "cellsizeX", DATATYPE_DOUBLE, true,
				null);
		addAttribute("EnvelopeDescriptor", "cellsizeY", DATATYPE_DOUBLE, true,
				null);
		addAttribute("EnvelopeDescriptor", "xll", DATATYPE_DOUBLE, true, null);
		addAttribute("EnvelopeDescriptor", "yll", DATATYPE_DOUBLE, true, null);

	}

	/**
	 * Returns an instance of the {@link AsciiGridsImageMetadataFormat} class.
	 * We construct only a single instance and we cache it for future uses.
	 * 
	 * @return an instance of the {@link AsciiGridsImageMetadataFormat} class.
	 */
	public static synchronized IIOMetadataFormat getInstance() {
		if (instance == null)
			instance = new AsciiGridsImageMetadataFormat();
		return instance;
	}

	/**
	 * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
	 *      javax.imageio.ImageTypeSpecifier)
	 */
	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {

		// @todo @task TODO
		return true;
	}
}
