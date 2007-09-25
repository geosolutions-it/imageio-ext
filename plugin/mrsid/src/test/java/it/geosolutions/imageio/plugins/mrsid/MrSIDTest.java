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

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * Testing reading capabilities for {@link MrSIDImageReader}.
 * 
 * In case you get directo buffer memory problems use the following hint
 * -XX:MaxDirectMemorySize=128M
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class MrSIDTest extends AbstractMrSIDTestCase {
	public MrSIDTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
//		new MrSIDImageReaderSpi();
	}

	/**
	 * Test retrieving all available metadata properties
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testMetadata() throws FileNotFoundException, IOException {
		try {
			final File file = TestData.file(this, fileName);
			final ParameterBlockJAI pbjImageRead;
			pbjImageRead = new ParameterBlockJAI("ImageRead");
			pbjImageRead.setParameter("Input", file);
			RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			IIOMetadata metadata = (IIOMetadata) image
					.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);
			assertTrue(metadata instanceof GDALCommonIIOImageMetadata);
			GDALCommonIIOImageMetadata commonMetadata = (GDALCommonIIOImageMetadata) metadata;
			Viewer
					.displayImageIOMetadata(commonMetadata
							.getAsTree(GDALCommonIIOImageMetadata.nativeMetadataFormatName));
			Viewer.displayImageIOMetadata(commonMetadata
					.getAsTree(MrSIDIIOImageMetadata.mrsidImageMetadataName));
			if (TestData.isInteractiveTest())
				Viewer.visualizeAllInformation(image, "", TestData
						.isInteractiveTest());
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}

	}

	/**
	 * Test read exploiting common JAI operations (Crop-Translate-Rotate)
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testJaiOperations() throws FileNotFoundException, IOException {
		try {
			final File file = TestData.file(this, fileName);

			// ////////////////////////////////////////////////////////////////
			// preparing to read
			// ////////////////////////////////////////////////////////////////
			final ParameterBlockJAI pbjImageRead;
			final ImageReadParam irp = new ImageReadParam();

			Integer xSubSampling = new Integer(8);
			Integer ySubSampling = new Integer(8);
			Integer xSubSamplingOffset = new Integer(0);
			Integer ySubSamplingOffset = new Integer(0);

			irp.setSourceSubsampling(xSubSampling.intValue(), ySubSampling
					.intValue(), xSubSamplingOffset.intValue(),
					ySubSamplingOffset.intValue());

			pbjImageRead = new ParameterBlockJAI("ImageRead");
			pbjImageRead.setParameter("Input", file);
			pbjImageRead.setParameter("readParam", irp);
			final ImageLayout l = new ImageLayout();
			l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
					.setTileWidth(512);

			RenderedOp image = JAI.create("ImageRead", pbjImageRead,
					new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
	
		
			if (TestData.isInteractiveTest())
				Viewer.visualize(image, "Subsampling Read");
			else
				assertNotNull(image.getTiles());

			// ////////////////////////////////////////////////////////////////
			// preparing to crop
			// ////////////////////////////////////////////////////////////////
			final ParameterBlockJAI pbjCrop = new ParameterBlockJAI("Crop");
			pbjCrop.addSource(image);

			Float xCrop = new Float(image.getWidth()*3/4.0+image.getMinX());
			Float yCrop = new Float(image.getHeight()*3/4.0+image.getMinY());
			Float cropWidth = new Float(image.getWidth()/4.0);
			Float cropHeigth = new Float(image.getHeight()/4.0);

			pbjCrop.setParameter("x", xCrop);
			pbjCrop.setParameter("y", yCrop);
			pbjCrop.setParameter("width", cropWidth);
			pbjCrop.setParameter("height", cropHeigth);

			final RenderedOp croppedImage = JAI.create("Crop", pbjCrop);
			assertNotNull(croppedImage.getTiles());

			// ////////////////////////////////////////////////////////////////
			// preparing to translate
			// ////////////////////////////////////////////////////////////////
			final ParameterBlockJAI pbjTranslate = new ParameterBlockJAI(
					"Translate");
			pbjTranslate.addSource(croppedImage);

			Float xTrans = new Float(-image.getMinX());
			Float yTrans = new Float(-image.getMinY());

			pbjTranslate.setParameter("xTrans", xTrans);
			pbjTranslate.setParameter("yTrans", yTrans);

			final RenderedOp translatedImage = JAI.create("Translate",
					pbjTranslate);
			if (TestData.isInteractiveTest())
				Viewer.visualize(translatedImage,
						"Cropped And Traslated Image ");
			else
				assertNotNull(image.getTiles());

			// ////////////////////////////////////////////////////////////////
			// preparing to rotate
			// ////////////////////////////////////////////////////////////////

			final ParameterBlockJAI pbjRotate = new ParameterBlockJAI("Rotate");
			pbjRotate.addSource(translatedImage);

			Float xOrigin = new Float(cropWidth.floatValue() / 2);
			Float yOrigin = new Float(cropHeigth.floatValue() / 2);
			Float angle = new Float(java.lang.Math.PI / 2);

			pbjRotate.setParameter("xOrigin", xOrigin);
			pbjRotate.setParameter("yOrigin", yOrigin);
			pbjRotate.setParameter("angle", angle);

			final RenderedOp rotatedImage = JAI.create("Rotate", pbjRotate);
			if (TestData.isInteractiveTest())
				Viewer.visualize(rotatedImage, "Rotated Image");
			else
				assertNotNull(image.getTiles());

		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	public void testManualRead() throws IOException {
		try {
			MrSIDImageReader reader = new MrSIDImageReader(
					new MrSIDImageReaderSpi());
			final ImageReadParam irp = new ImageReadParam();
			final File file = TestData.file(this, fileName);
			irp.setSourceSubsampling(8, 8, 0, 0);
			reader.setInput(file);
			final RenderedImage image = reader.readAsRenderedImage(0, irp);
			if (TestData.isInteractiveTest())
				Viewer.visualize(image, "SubSampled MrSID ImageRead");
//			assertEquals(779, image.getWidth());
//			assertEquals(873, image.getHeight());
			reader.dispose();
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Test read exploiting common JAI operations (Crop-Translate-Rotate)
		suite.addTest(new MrSIDTest("testJaiOperations"));

		// Test reading metadata information
		suite.addTest(new MrSIDTest("testMetadata"));
		
		// Test read withouht exploiting JAI
		suite.addTest(new MrSIDTest("testManualRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
