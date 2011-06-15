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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALImageReader;

import java.awt.image.RenderedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link MrSIDImageReader} is a {@link GDALImageReader} able to create
 * {@link RenderedImage} from MrSID files.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class MrSIDImageReader extends GDALImageReader {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.mrsid");

    /**
     * Constructs a
     * <code>MrSIDImageReader<code> using a {@link MrSIDImageReaderSpi}.
     * 
     * @param originatingProvider
     *            The {@link MrSIDImageReaderSpi} to use for building this
     *            <code>MrSIDImageReader<code>.
     */
    public MrSIDImageReader(MrSIDImageReaderSpi originatingProvider) {
        super(originatingProvider, 0);
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("MrSIDImageReader Constructor");
    }

    /**
     * Build a proper {@link MrSIDIIOImageMetadata} given the name of a dataset
     * 
     * @param datasetName
     *                the name of the dataset
     */
    protected GDALCommonIIOImageMetadata createDatasetMetadata(
            String datasetName) {
        return new MrSIDIIOImageMetadata(datasetName);
    }

}
