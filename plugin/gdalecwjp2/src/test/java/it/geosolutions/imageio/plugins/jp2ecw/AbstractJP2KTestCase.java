/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
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

import javax.media.jai.JAI;

import junit.framework.TestCase;

import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public class AbstractJP2KTestCase extends TestCase {

	static{
		gdal.AllRegister();
		final Driver driverkak = gdal.GetDriverByName("JP2KAK");
		final Driver drivermrsid = gdal.GetDriverByName("JP2MrSID");
		if (driverkak!=null || drivermrsid!=null){
			final StringBuffer skipDriver = new StringBuffer("");
			if (driverkak!=null)
				skipDriver.append("JP2KAK ");
			if (drivermrsid!=null)
				skipDriver.append("JP2MrSID");
			gdal.SetConfigOption("GDAL_SKIP", skipDriver.toString());
		}
	}
	
	public AbstractJP2KTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// general settings
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				64 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);

	}
}
