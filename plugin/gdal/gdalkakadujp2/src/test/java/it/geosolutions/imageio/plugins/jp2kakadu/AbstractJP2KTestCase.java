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

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractJP2KTestCase extends AbstractGDALTest {

    protected static boolean isJp2KakDriverAvailable;

    static {
        if (isGDALAvailable) {
            gdal.AllRegister();
            final Driver driverEcw = gdal.GetDriverByName("JP2ECW");
            final Driver drivermrsid = gdal.GetDriverByName("JP2MrSID");
            if (driverEcw != null || drivermrsid != null) {
                final StringBuilder skipDriver = new StringBuilder("");
                if (driverEcw != null)
                    skipDriver.append("JP2ECW ");
                if (drivermrsid != null)
                    skipDriver.append("JP2MrSID");
                gdal.SetConfigOption("GDAL_SKIP", skipDriver.toString());
                gdal.AllRegister();
            }
            isJp2KakDriverAvailable = GDALUtilities.isDriverAvailable("JP2KAK");
        } else {
            isJp2KakDriverAvailable = false;
        }
        if (!isJp2KakDriverAvailable) {
            AbstractGDALTest.missingDriverMessage("JP2KAK");
        }
    }
}
