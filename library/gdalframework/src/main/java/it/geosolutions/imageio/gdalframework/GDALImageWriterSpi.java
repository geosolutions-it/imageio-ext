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

import javax.imageio.spi.ImageWriterSpi;

/**
 * The abstract service provider interface (SPI) for
 * <code>GDALImageWriter</code>s.
 * 
 * @author Daniele Romagnoli.
 */
public abstract class GDALImageWriterSpi extends ImageWriterSpi {

	/**
	 * <code>true</code> if the GDAL driver of the extending format support
	 * <code>Create</code> method. When defining a specialized
	 * <code>GDALImageWriterSpi</code> be sure you properly initialize this
	 * field. Information about Create support are available running <BR>
	 * "gdalinfo --formats" command
	 */
	protected boolean isSupportingCreate=false;

	/**
	 * <code>true</code> if the GDAL driver of the extending format support
	 * <code>CreateCopy</code> method. When defining a specialized
	 * <code>GDALImageWriterSpi</code> be sure you properly initialize this
	 * field. Information about CreateCopy support are available running <BR>
	 * "gdalinfo --formats" command
	 */
	protected boolean isSupportingCreateCopy=false;

	public GDALImageWriterSpi(String vendorName, String version,
			String[] names, String[] suffixes, String[] MIMETypes,
			String writerClassName, Class[] outputTypes,
			String[] readerSpiNames,
			boolean supportsStandardStreamMetadataFormat,
			String nativeStreamMetadataFormatName,
			String nativeStreamMetadataFormatClassName,
			String[] extraStreamMetadataFormatNames,
			String[] extraStreamMetadataFormatClassNames,
			boolean supportsStandardImageMetadataFormat,
			String nativeImageMetadataFormatName,
			String nativeImageMetadataFormatClassName,
			String[] extraImageMetadataFormatNames,
			String[] extraImageMetadataFormatClassNames) {

		super(
				vendorName,
				version,
				names,
				suffixes,
				MIMETypes,
				writerClassName, // writer class name
				STANDARD_OUTPUT_TYPE,
				readerSpiNames, // reader spi names
				supportsStandardStreamMetadataFormat,
				nativeStreamMetadataFormatName,
				nativeStreamMetadataFormatClassName,
				extraStreamMetadataFormatNames,
				extraStreamMetadataFormatClassNames,
				supportsStandardImageMetadataFormat,
				nativeImageMetadataFormatName,
				nativeImageMetadataFormatClassName,
				extraImageMetadataFormatNames,
				extraImageMetadataFormatClassNames);
	}

	public boolean isSupportingCreate() {
		return isSupportingCreate;
	}

	public boolean isSupportingCreateCopy() {
		return isSupportingCreateCopy;
	}

}
