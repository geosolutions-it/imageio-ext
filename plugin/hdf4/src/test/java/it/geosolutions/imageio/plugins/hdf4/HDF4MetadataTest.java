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
package it.geosolutions.imageio.plugins.hdf4;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class HDF4MetadataTest extends AbstractHDF4TestCase {

	public HDF4MetadataTest(String name) {
		super(name);
	}
	
	/**
	 * This test method retrieves and visualizes specified images and
	 * image metadata for a dataset containing several subdatasets
	 */
	public void testImageMetadata() throws FileNotFoundException, IOException {
		final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		final File file = TestData.file(this, fileName);
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();

		irp.setSourceSubsampling(1, 1, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);

		int imageIndex = 2;
		pbjImageRead.setParameter("imageChoice", imageIndex);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeImageMetadata(image, fileName);

		imageIndex = 8;
		pbjImageRead.setParameter("imageChoice", imageIndex);
		image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeImageMetadata(image, fileName, imageIndex);
	}
	
	/**
	 * This test method retrieves and visualizes 
	 * stream metadata
	 */
	public void testStreamMetadata() throws FileNotFoundException, IOException {
		final String fileName = "TOVS_DAILY_AM_870330_NG.HDF";
		final File file = TestData.file(this, fileName);
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();

		irp.setSourceSubsampling(1, 1, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);

		int imageIndex = 0;
		pbjImageRead.setParameter("imageChoice", imageIndex);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualizeStreamMetadata(image, fileName, false);
	}
		
	
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test reading of image as well as image metadata information
		suite.addTest(new HDF4MetadataTest("testImageMetadata"));

		// Test stream metadata retrieval  
		suite.addTest(new HDF4MetadataTest("testStreamMetadata"));
		
		return suite;
	}
	
	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
