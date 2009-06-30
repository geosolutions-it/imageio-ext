/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.logging.Logger;

import javax.media.jai.JAI;

import org.junit.Before;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractJP2KTestCase extends AbstractGDALTest {

    /** A simple flag set to true in case the JP2 Kakadu driver is available */
    protected final static boolean isDriverAvailable = GDALUtilities
            .isDriverAvailable("JP2KAK");

    private final static String msg = "JP2GDAL Kakadu Tests are skipped due to missing Driver.\n"
            + "Be sure GDAL has been built against Kakadu and the required"
            + " libs are in the classpath";

    protected static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2kakadu");


    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (!isDriverAvailable) {
            LOGGER.warning(msg);
            return;
        }
    }
}
