/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.arcbinarygrid;

import java.util.logging.Logger;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

import javax.media.jai.JAI;

import junit.framework.TestCase;

/**
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class AbstractArcBinaryGridTestCase extends TestCase {

    /** A simple flag set to true in case the GDAL Library is available */
    protected final static boolean isGDALAvailable = GDALUtilities
            .isGDALAvailable();

    protected static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.arcbinarygrid");

    public AbstractArcBinaryGridTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (!isGDALAvailable) {
            LOGGER.warning("GDAL Library is not Available");
            return;
        }
        // general settings
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                128 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
    }
}
