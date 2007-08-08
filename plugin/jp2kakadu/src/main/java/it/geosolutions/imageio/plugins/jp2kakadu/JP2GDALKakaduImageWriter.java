/*
* JImageIO-extension - OpenSource Java Image translation Library
* http://www.geo-solutions.it/
* (C) 2007, GeoSolutions
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation;
* version 2.1 of the License.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageWriter;

import javax.imageio.ImageWriteParam;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;

public class JP2GDALKakaduImageWriter extends GDALImageWriter {
	public JP2GDALKakaduImageWriter(
			JP2GDALKakaduImageWriterSpi originatingProvider) {
		super(originatingProvider);
	}

	protected Driver getDriver() {
		return gdal.GetDriverByName("JP2KAK");
	}

	/**
	 * Build a default {@link JP2GDALKakaduImageWriteParam}
	 */
	public ImageWriteParam getDefaultWriteParam() {
		return new JP2GDALKakaduImageWriteParam();
	}

}
