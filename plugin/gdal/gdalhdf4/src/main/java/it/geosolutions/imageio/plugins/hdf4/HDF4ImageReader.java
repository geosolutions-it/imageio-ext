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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;

/**
 * {@link HDF4ImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from HDF4 files.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class HDF4ImageReader extends GDALImageReader {

    /**
     * Constructs a
     * <code>HDF4ImageReader<code> using a {@link HDF4ImageReaderSpi}.
     * 
     * @param originatingProvider
     *            The {@link HDF4ImageReaderSpi} to use for building this
     *            <code>HDF4ImageReader<code>.
     */
    public HDF4ImageReader(HDF4ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
}
