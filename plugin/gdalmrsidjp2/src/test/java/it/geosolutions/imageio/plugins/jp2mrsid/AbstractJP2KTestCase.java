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
package it.geosolutions.imageio.plugins.jp2mrsid;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;

import junit.framework.TestCase;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractJP2KTestCase extends TestCase {

    /** A simple flag set to true in case the JP2 MrSID driver is available */
    protected static boolean isDriverAvailable;

    private final static String msg = "JP2 MRSID Tests are skipped due to missing Driver.\n"
            + "Be sure GDAL has been built against MRSID and the required"
            + " lib is in the classpath";

    protected static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2mrsid");

    static {
        try {
            gdal.AllRegister();
            final Driver driverkak = gdal.GetDriverByName("JP2KAK");
            final Driver driverecw = gdal.GetDriverByName("JP2ECW");
            if (driverkak != null || driverecw != null) {
                final StringBuffer skipDriver = new StringBuffer("");
                if (driverkak != null)
                    skipDriver.append("JP2KAK ");
                if (driverecw != null)
                    skipDriver.append("JP2ECW");
                gdal.SetConfigOption("GDAL_SKIP", skipDriver.toString());
            }
            isDriverAvailable = GDALUtilities.isDriverAvailable("JP2MrSID");
        } catch (UnsatisfiedLinkError e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(new StringBuffer("GDAL library unavailable.")
                        .toString());
            isDriverAvailable = false;
        }
    }

    public AbstractJP2KTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (!isDriverAvailable) {
            LOGGER.warning(msg);
            return;
        }
        // general settings
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                64 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
    }
}
