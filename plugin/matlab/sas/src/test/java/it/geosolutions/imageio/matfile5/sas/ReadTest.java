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
package it.geosolutions.imageio.matfile5.sas;

import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReader;

import org.junit.Test;

public class ReadTest {
	
    private static final Logger LOGGER = Logger.getLogger(ReadTest.class.toString());
	
	@Test
	public void testSAS() throws IOException {
		File file;
		try {
			file = TestData.file(this,"sas_sample.mat");
		} catch (IOException e) {
			LOGGER.info("Unable to run test due to " + e.getLocalizedMessage());
			return;
		}
		ImageReader reader = new SASTileImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        reader.getWidth(0);
        reader.read(0);
        reader.dispose();
        reader=null;
	}
}
