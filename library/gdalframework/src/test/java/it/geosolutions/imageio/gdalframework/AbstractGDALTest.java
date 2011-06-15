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
package it.geosolutions.imageio.gdalframework;

import java.util.logging.Logger;

import org.junit.Test;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractGDALTest  {

    /** A simple flag set to true in case the GDAL Library is available */
    protected final static boolean isGDALAvailable = GDALUtilities.isGDALAvailable();
    
    public final static String GDAL_DATA = "GDAL_DATA";
    
    protected final static boolean isGDALDATAEnvSet;
    
    protected final static String MISSING_DRIVER_MESSAGE = " Driver is not Available. Tests will be skipped"
        +"\n Make sure GDAL has been built with support for this format"
        + " and the required native libs are in the path "; 
    
    static{
        final String gdalData = System.getenv(GDAL_DATA);
        isGDALDATAEnvSet = gdalData!=null && gdalData.trim().length()>0;
    }

    protected static final Logger LOGGER = Logger.getLogger(AbstractGDALTest.class.toString());

    public AbstractGDALTest() {
    }

    @Test
    public void setUp() throws Exception {
        if (!isGDALAvailable) {
            LOGGER.warning("GDAL Library is not Available");
            return;
        }
    }
    
    public final static void missingDriverMessage(final String format){
        LOGGER.warning(format + MISSING_DRIVER_MESSAGE);
    }

    public final static void warningMessage() {
        LOGGER.info("Test file not available");
    }
    
    public final static void warningMessage(final String customMessage) {
        LOGGER.info(customMessage);
    }
}
