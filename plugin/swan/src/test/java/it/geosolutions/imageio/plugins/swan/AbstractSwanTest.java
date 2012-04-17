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
package it.geosolutions.imageio.plugins.swan;

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

	public void test(){
	    
	}
	
	protected void warningMessage() {
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
