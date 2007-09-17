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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import com.sun.media.jai.operator.ImageReadDescriptor;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MrSIDJAIReadTest extends AbstractMrSIDTestCase{
	public MrSIDJAIReadTest(String name) {
		super(name);
	}

	/**
	 * Test reading of a GrayScale image
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testGrayScaleImageRead() throws FileNotFoundException,
			IOException {
		final ParameterBlockJAI pbjImageRead;
		final String fileName = "B201761B.sid";
		final File file = TestData.file(this, fileName);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualize(image, fileName);
	}
	
	/**
	 * Test reading of a RGB image
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testRGBImageRead() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final String fileName = "B260362A.sid";
		final File file = TestData.file(this, fileName);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		IIOMetadata metadata=(IIOMetadata) image.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);
		assert metadata instanceof GDALCommonIIOImageMetadata;
		GDALCommonIIOImageMetadata commonMetadata=(GDALCommonIIOImageMetadata) metadata;
		Viewer.displayImageIOMetadata(commonMetadata.getAsTree(GDALCommonIIOImageMetadata.nativeMetadataFormatName));
		Viewer.displayImageIOMetadata(commonMetadata.getAsTree(MrSIDIIOImageMetadata.mrsidImageMetadataName));
		Viewer.visualizeAllInformation(image, "",true);
	}
	
	/**
	 * Test reading of a big MrSID dataset
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testBigImageRead() throws FileNotFoundException, IOException {
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		final String fileName = "N-17-25_2000.sid";
		final File file = TestData.file(this, fileName);
		
		irp.setSourceSubsampling(2, 2, 0, 0);
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualize(image, fileName);
	}
	
	/**
	 * Test read exploiting common JAI operations (Crop-Translate-Rotate)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testJaiOperations()throws FileNotFoundException,IOException{
		final String fileName = "89926.sid";
		final File file = TestData.file(this, fileName);
		
		// ////////////////////////////////////////////////////////////////
		// preparing to read
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjImageRead;
		final ImageReadParam irp = new ImageReadParam();
		
		Integer xSubSampling = new Integer(3);
		Integer ySubSampling = new Integer(3);
		Integer xSubSamplingOffset = new Integer(0);
		Integer ySubSamplingOffset = new Integer(0);
		
		irp.setSourceSubsampling(xSubSampling.intValue(),ySubSampling.intValue(),
				xSubSamplingOffset.intValue(),ySubSamplingOffset.intValue());
		
		pbjImageRead = new ParameterBlockJAI("ImageRead");
		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		Viewer.visualize(image, "Subsampling Read");
		
		// ////////////////////////////////////////////////////////////////
		// preparing to crop
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjCrop = new ParameterBlockJAI("Crop");
		pbjCrop.addSource(image);
		
		Float xCrop=new Float(400);
		Float yCrop=new Float(400);
		Float cropWidth=new Float(800);
		Float cropHeigth=new Float(800);
		
		pbjCrop.setParameter("x", xCrop);
		pbjCrop.setParameter("y", yCrop);
		pbjCrop.setParameter("width", cropWidth);
		pbjCrop.setParameter("height", cropHeigth);
		
		final RenderedOp croppedImage = JAI.create("Crop", pbjCrop);
		
		// ////////////////////////////////////////////////////////////////
		// preparing to translate
		// ////////////////////////////////////////////////////////////////
		final ParameterBlockJAI pbjTranslate = new ParameterBlockJAI("Translate");
		pbjTranslate.addSource(croppedImage);
		
		Float xTrans=new Float(-800);
		Float yTrans=new Float(-800);
		
		pbjTranslate.setParameter("xTrans", xTrans);
		pbjTranslate.setParameter("yTrans", yTrans);
		
		final RenderedOp translatedImage = JAI.create("Translate", pbjTranslate);
		Viewer.visualize(translatedImage,"Cropped And Traslated Image ");
		
		// ////////////////////////////////////////////////////////////////
		// preparing to rotate
		// ////////////////////////////////////////////////////////////////
		
		final ParameterBlockJAI pbjRotate = new ParameterBlockJAI("Rotate");
		pbjRotate.addSource(translatedImage);
		
		Float xOrigin=new Float(cropWidth.floatValue()/2);
		Float yOrigin=new Float(cropHeigth.floatValue()/2);
		Float angle=new Float(java.lang.Math.PI/2);
		
		pbjRotate.setParameter("xOrigin", xOrigin);
		pbjRotate.setParameter("yOrigin", yOrigin);
		pbjRotate.setParameter("angle", angle);
		
		final RenderedOp rotatedImage = JAI.create("Rotate", pbjRotate);
		Viewer.visualize(rotatedImage,"Rotated Image");
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test read exploiting common JAI operations (Crop-Translate-Rotate)
		suite.addTest(new MrSIDJAIReadTest("testJaiOperations"));

		// Test reading of a GrayScale image 
		suite.addTest(new MrSIDJAIReadTest("testGrayScaleImageRead"));
		
		// Test reading of a RGB image
		suite.addTest(new MrSIDJAIReadTest("testRGBImageRead"));

		// Test reading of a big MrSID dataset
		suite.addTest(new MrSIDJAIReadTest("testBigImageRead"));
		
		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
