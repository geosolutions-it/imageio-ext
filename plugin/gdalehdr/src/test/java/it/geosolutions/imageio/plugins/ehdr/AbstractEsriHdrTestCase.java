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
package it.geosolutions.imageio.plugins.ehdr;

import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.util.logging.Logger;

import javax.media.jai.JAI;

import junit.framework.TestCase;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractEsriHdrTestCase extends TestCase {
	/** A simple flag set to true in case the GDAL Library is available */
	protected final static boolean isGDALAvailable = GDALUtilities
			.isGDALAvailable();

	protected static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.ehdr");

	public AbstractEsriHdrTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		if (!isGDALAvailable) {
			LOGGER.warning("GDAL Library is not Available");
			return;
		}

		File file = TestData.file(this, "test.zip");
		assertTrue(file.exists());

		// unzip it
		TestData.unzipFile(this, "test.zip");

		// general settings
		JAI.getDefaultInstance().getTileScheduler().setParallelism(1);
		JAI.getDefaultInstance().getTileScheduler().setPriority(5);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(1);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				180 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
	}
}
