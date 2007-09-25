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
package it.geosolutions.imageio.plugins.swan;

import it.geosolutions.resources.TestData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.logging.Logger;

import junit.framework.TestCase;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AbstractSwanTest extends TestCase {

	private static final Logger LOGGER = Logger
	.getLogger("it.geosolutions.imageio.plugins.swan");
	
	public AbstractSwanTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
		protected void warningMessage(){
		StringBuffer sb = new StringBuffer(
				"Test file not available. Please download it as "
						+ "anonymous FTP from "
						+ "ftp://ftp.geo-solutions.it/incoming/swantest.zip"
						+ "\n Use a tool supporting Active Mode.\n"
						+ "Then unzip it on: plugin/"
						+ "swan/src/test/resources/it/geosolutions/"
						+ "imageio/plugins/swan/test-data folder and"
						+ " repeat the test.");
		LOGGER.info(sb.toString());
	}

}
