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
package it.geosolutions.imageio.gdalframework;

import java.util.logging.Logger;

import javax.media.jai.JAI;

import org.junit.Test;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractGDALTest  {

    /** A simple flag set to true in case the GDAL Library is available */
    protected final static boolean isGDALAvailable = GDALUtilities.isGDALAvailable();

    protected static final Logger LOGGER = Logger.getLogger(AbstractGDALTest.class.toString());

    public AbstractGDALTest() {
    }

    @Test
    public void setUp() throws Exception {
        if (!isGDALAvailable) {
            LOGGER.warning("GDAL Library is not Available");
            return;
        }
        // general settings
        JAI.getDefaultInstance().getTileScheduler().setParallelism(1);
        JAI.getDefaultInstance().getTileScheduler().setPriority(5);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(1);
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(180 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
    }

	public static void warningMessage() {
		LOGGER.info("Test file not available");
		
	}
}
