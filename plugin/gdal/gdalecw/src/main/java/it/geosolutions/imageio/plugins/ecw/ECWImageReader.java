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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.GDALImageReader;
import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ECWImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from ECW files and ECWP protocol.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class ECWImageReader extends GDALImageReader {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.ecw");

    public ECWImageReader(ECWImageReaderSpi originatingProvider) {
        super(originatingProvider, 0);
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("ECWImageReader Constructor");
    }

    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Setting Input");
        if (input != null && input instanceof ECWPImageInputStream) {
            try {
                boolean isDecodable = ((GDALImageReaderSpi) this
                        .getOriginatingProvider()).canDecodeInput(input);
                if (isDecodable)
                    super.setInput(input, seekForwardOnly, ignoreMetadata);
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Failed to create a valid input stream ", e);
            }
        } else
            super.setInput(input, seekForwardOnly, ignoreMetadata);
    }

}
