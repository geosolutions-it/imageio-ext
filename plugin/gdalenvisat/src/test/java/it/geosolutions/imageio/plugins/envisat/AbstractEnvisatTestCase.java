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
package it.geosolutions.imageio.plugins.envisat;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.logging.Logger;

import javax.media.jai.JAI;

import junit.framework.TestCase;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractEnvisatTestCase extends TestCase {

    /** A simple flag set to true in case the GDAL Library is available */
    protected final static boolean isGDALAvailable = GDALUtilities
            .isGDALAvailable();

    protected static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.envisat");

    public AbstractEnvisatTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (!isGDALAvailable) {
            LOGGER.warning("GDAL Library is not Available");
            return;
        }
        // general settings
        JAI.getDefaultInstance().getTileScheduler().setParallelism(10);
        JAI.getDefaultInstance().getTileScheduler().setPriority(4);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(2);
        JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                128 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
    }

    protected void warningMessage() {
        StringBuffer sb = new StringBuffer(
                "Test file not available. Please download it from ")
                .append(
                        "http://www.brockmann-consult.de/beam/data/products/ATS_TOA_1CNPDK20030504_111259_000000572016_00080_06146_0157.zip \n")
                .append("Then unzip it on: plugin/").append(
                        "envisat/src/test/resources/it/geosolutions/").append(
                        "imageio/plugins/envisat/test-data folder and").append(
                        " repeat the test.");
        LOGGER.info(sb.toString());
    }
}
