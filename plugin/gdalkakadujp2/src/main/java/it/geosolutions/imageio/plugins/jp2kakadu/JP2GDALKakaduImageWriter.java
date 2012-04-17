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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageWriter;

import javax.imageio.ImageWriteParam;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JP2GDALKakaduImageWriter extends GDALImageWriter {

    /**
     * Constructs a <code>JP2GDALKakaduImageWriter<code> using a 
     * {@link JP2GDALKakaduImageWriterSpi}.
     * 
     * @param originatingProvider
     *            The {@link JP2GDALKakaduImageWriterSpi} to use for building 
     *            this <code>JP2GDALKakaduImageWriter<code>.
     */
    public JP2GDALKakaduImageWriter(
            JP2GDALKakaduImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Build a default {@link JP2GDALKakaduImageWriteParam}
     */
    public ImageWriteParam getDefaultWriteParam() {
        return new JP2GDALKakaduImageWriteParam();
    }

}
