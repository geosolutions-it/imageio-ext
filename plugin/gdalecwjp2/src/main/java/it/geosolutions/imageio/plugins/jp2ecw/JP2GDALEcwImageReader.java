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
package it.geosolutions.imageio.plugins.jp2ecw;

import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link JP2GDALEcwImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from JP2K files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JP2GDALEcwImageReader extends GDALImageReader {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2ecw");

    public JP2GDALEcwImageReader(JP2GDALEcwImageReaderSpi originatingProvider) {
        super(originatingProvider, 0);
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("JP2GDALEcwImageReader Constructor");
    }
}
