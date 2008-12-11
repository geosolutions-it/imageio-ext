/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.util.logging.Logger;

import javax.media.jai.JAI;

import junit.framework.TestCase;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractHDF4TestCase extends TestCase {

	/** A simple flag set to true in case the HDF4 driver is available */
	protected final static boolean isDriverAvailable = GDALUtilities
			.isDriverAvailable("HDF4"); 
	
	private final static String msg = "HDF4 Tests are skipped due to missing Driver.\n"
		+ "Be sure GDAL has been built against HDF4 and the required"
		+ " libs are in the classpath";
	
	protected static final Logger LOGGER = Logger
	.getLogger("it.geosolutions.imageio.plugins.mrsid");
	
	public AbstractHDF4TestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		if (!isDriverAvailable){
			LOGGER.warning(msg);
			return;
		}
		// general settings
		JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
		JAI.getDefaultInstance().getTileScheduler().setPriority(4);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(2);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				180 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
	}
	
	protected void warningMessage(){
		StringBuffer sb = new StringBuffer(
				"Test file not available. Please download it at: "
						+ "http://www.hdfgroup.uiuc.edu/UserSupport/code-"
						+ "examples/sample-programs/convert/Conversion.html"
						+ "\nThen copy it to: plugin/hdf4/src/test/resources/"
						+ "it/geosolutions/imageio/plugins/hdf4/test-data"
						+ " and repeat the test.");
		LOGGER.info(sb.toString());
	}
}