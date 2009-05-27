/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.plugins.jhdf;

import javax.media.jai.JAI;

import org.junit.Before;

public class JHDFTestCase  {
	
	@Before
	protected void setUp() throws Exception {
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                64 * 1024 * 1024);
        JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);

    }
}
