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
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author Simone Giannecchini, GeoSolutions.
 */
public class ImageIOExtTest extends TestCase {

	/**
	 * @param name
	 */
	public ImageIOExtTest(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static void main(String[] args) {
		TestRunner.run(ImageIOExtTest.class);
	}

	public void testImageReadMT() {
		ImageReadDescriptorMT.register(JAI.getDefaultInstance());
		final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageReadMT");
		assertNotNull(pbj);
	}
}
