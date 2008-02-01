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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * Testing reading capabilities for {@link MrSIDImageReader}.
 * 
 * In case you get direct buffer memory problems use the following hint
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

			// subsample by 2 on both dimensions
			final int xSubSampling = 2;
			final int ySubSampling = 2;
			final int xSubSamplingOffset = 0;
			final int ySubSamplingOffset = 0;
			irp.setSourceSubsampling(xSubSampling, ySubSampling,
					xSubSamplingOffset, ySubSamplingOffset);

			// re-tile on the fly to 512x512
			final ImageLayout l = new ImageLayout();
			l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
					.setTileWidth(512);

			pbjImageRead = new ParameterBlockJAI("ImageRead");
			pbjImageRead.setParameter("Input", file);
			pbjImageRead.setParameter("readParam", irp);

			// get a RenderedImage
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

			Float xCrop = new Float(image.getWidth() * 3 / 4.0
					+ image.getMinX());
			Float yCrop = new Float(image.getHeight() * 3 / 4.0
					+ image.getMinY());
			Float cropWidth = new Float(image.getWidth() / 4.0);
			Float cropHeigth = new Float(image.getHeight() / 4.0);

			pbjCrop.setParameter("x", xCrop);
			pbjCrop.setParameter("y", yCrop);
			pbjCrop.setParameter("width", cropWidth);
			pbjCrop.setParameter("height", cropHeigth);

			final RenderedOp croppedImage = JAI.create("Crop", pbjCrop);
			if (TestData.isInteractiveTest())
				Viewer.visualize(croppedImage, "Cropped Image");
			else
				assertNotNull(croppedImage.getTiles());

			// ////////////////////////////////////////////////////////////////
			// preparing to translate
			// ////////////////////////////////////////////////////////////////
			final ParameterBlockJAI pbjTranslate = new ParameterBlockJAI(
					"Translate");
			pbjTranslate.addSource(croppedImage);

			Float xTrans = new Float(-croppedImage.getMinX());
			Float yTrans = new Float(-croppedImage.getMinY());

			pbjTranslate.setParameter("xTrans", xTrans);
			pbjTranslate.setParameter("yTrans", yTrans);

			final RenderedOp translatedImage = JAI.create("Translate",
					pbjTranslate);
			if (TestData.isInteractiveTest())
				Viewer.visualize(translatedImage, "Translated Image");
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

	/**
	 * Test read exploiting the setSourceBands and setDestinationType on
	 * imageReadParam
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testSubBandsRead() throws IOException {
		try {
			ImageReader reader = new MrSIDImageReaderSpi()
					.createReaderInstance();
			final File file = TestData.file(this, fileName);
			reader.setInput(file);

			// //
			//
			// Getting image properties
			//
			// //
			ImageTypeSpecifier spec = (ImageTypeSpecifier) reader
					.getImageTypes(0).next();
			SampleModel sm = spec.getSampleModel();
			final int width = reader.getWidth(0);
			final int height = reader.getHeight(0);

			// //
			//
			// Setting a ColorModel
			//
			// //
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorModel cm = RasterFactory.createComponentColorModel(sm
					.getDataType(), // dataType
					cs, // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency

			// //
			// 
			// Setting Image Read Parameters
			//
			// //
			final ImageReadParam param = new ImageReadParam();
			final int ssx = 2;
			final int ssy = 2;
			param.setSourceSubsampling(ssx, ssy, 0, 0);
			final Rectangle sourceRegion = new Rectangle(50, 50, 300, 300);
			param.setSourceRegion(sourceRegion);
			param.setSourceBands(new int[] { 0 });
			Rectangle intersRegion = new Rectangle(0, 0, width, height);
			intersRegion = intersRegion.intersection(sourceRegion);

			int subsampledWidth = (intersRegion.width + ssx - 1) / ssx;
			int subsampledHeight = (intersRegion.height + ssy - 1) / ssy;
			param.setDestinationType(new ImageTypeSpecifier(cm, sm
					.createCompatibleSampleModel(subsampledWidth,
							subsampledHeight).createSubsetSampleModel(
							new int[] { 0 })));

			// //
			//
			// Preparing the ImageRead operation
			//
			// //
			ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
			pbjImageRead.setParameter("Input", file);
			pbjImageRead.setParameter("readParam", param);
			pbjImageRead.setParameter("reader", reader);

			// //
			//
			// Setting a Layout
			//
			// //
			final ImageLayout l = new ImageLayout();
			l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256)
					.setTileWidth(256);
			RenderedOp image = JAI.create("ImageRead", pbjImageRead,
					new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

			if (TestData.isInteractiveTest())
				Viewer.visualize(image, "MrSID ImageRead");
			else
				assertNotNull(image.getTiles());
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	/**
	 * Test read exploiting the setDestination on imageReadParam
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void testManualRead() throws IOException {
		try {
			ImageReader reader = new MrSIDImageReaderSpi()
					.createReaderInstance();

			final File file = TestData.file(this, fileName);
			reader.setInput(file);
			ImageTypeSpecifier spec = (ImageTypeSpecifier) reader
					.getImageTypes(0).next();
			final int width = reader.getWidth(0);
			final int halfWidth = width / 2;
			final int height = reader.getHeight(0);
			final int halfHeight = height / 2;

			final ImageReadParam irp = new ImageReadParam();
			irp.setSourceRegion(new Rectangle(halfWidth, halfHeight, halfWidth,
					halfHeight));
			WritableRaster raster = Raster.createWritableRaster(spec
					.getSampleModel()
					.createCompatibleSampleModel(width, height), null);
			final BufferedImage bi = new BufferedImage(spec.getColorModel(),
					raster, false, null);

			irp.setDestination(bi);
			irp.setDestinationOffset(new Point(halfWidth, halfHeight));
			reader.read(0, irp);
			irp.setSourceRegion(new Rectangle(0, 0, halfWidth, halfHeight));
			irp.setDestination(bi);
			irp.setDestinationOffset(new Point(0, 0));
			reader.read(0, irp);
			irp.setSourceRegion(new Rectangle(halfWidth, halfHeight / 2,
					halfWidth, halfHeight / 4));
			irp.setDestination(bi);
			irp.setDestinationOffset(new Point(halfWidth, halfHeight / 2));
			RenderedImage ri = reader.read(0, irp);

			if (TestData.isInteractiveTest())
				Viewer.visualize(ri, "MrSID ImageRead");
			else
				assertNotNull(ri);

			reader.dispose();
		} catch (FileNotFoundException fnfe) {
			warningMessage();
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		 // Test read exploiting common JAI operations
		// (Crop-Translate-Rotate)
		suite.addTest(new MrSIDTest("testJaiOperations"));

		// Test reading metadata information
		suite.addTest(new MrSIDTest("testMetadata"));

		// Test read without exploiting JAI
		suite.addTest(new MrSIDTest("testManualRead"));

		// Test read without exploiting JAI
		suite.addTest(new MrSIDTest("testSubBandsRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
