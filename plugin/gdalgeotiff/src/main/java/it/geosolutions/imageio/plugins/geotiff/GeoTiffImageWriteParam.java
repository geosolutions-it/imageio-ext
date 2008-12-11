/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.gdalframework.GDALImageWriteParam;

import java.util.Locale;

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class GeoTiffImageWriteParam  extends GDALImageWriteParam {
		public GeoTiffImageWriteParam() {
			
			//change this
			super(new TIFFImageWriteParam(Locale.getDefault()), new GeoTiffCreateOptionsHandler());
			// TODO Auto-generated constructor stub
		}
}
