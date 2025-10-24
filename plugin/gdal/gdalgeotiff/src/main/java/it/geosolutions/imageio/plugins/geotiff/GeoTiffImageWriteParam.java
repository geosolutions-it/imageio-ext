/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageio.plugins.geotiff;

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
import it.geosolutions.imageio.gdalframework.GDALImageWriteParam;
import java.util.Locale;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class GeoTiffImageWriteParam extends GDALImageWriteParam {
    public GeoTiffImageWriteParam() {

        super(new TIFFImageWriteParam(Locale.getDefault()), new GeoTiffCreateOptionsHandler());
    }
}
