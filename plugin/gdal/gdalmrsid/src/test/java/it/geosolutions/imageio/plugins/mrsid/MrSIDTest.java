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
package it.geosolutions.imageio.plugins.mrsid;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
public class MrSIDTest extends AbstractGDALTest {

    protected static final String fileName = "n13250i.sid";
	/** A simple flag set to true in case the MrSID driver is available */
    private final static boolean isMrSidAvailable;
    
    static{
        if (isGDALAvailable) {  
            isMrSidAvailable = GDALUtilities.isDriverAvailable("MrSID");
        } else {
            isMrSidAvailable = false;
        }
        if (!isMrSidAvailable){
            AbstractGDALTest.missingDriverMessage("MrSID");
        }
    }


    /**
     * Test retrieving all available metadata properties
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void metadataAccess() throws FileNotFoundException, IOException {
        if (!isMrSidAvailable) {
            return;
        }
        try {
            final File file = TestData.file(this, fileName);
            final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
            pbjImageRead.setParameter("Input", file);
            RenderedOp image = JAI.create("ImageRead", pbjImageRead);
            IIOMetadata metadata = (IIOMetadata) image
                    .getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);
            Assert.assertTrue(metadata instanceof GDALCommonIIOImageMetadata);
            Assert.assertTrue(metadata instanceof MrSIDIIOImageMetadata);
            GDALCommonIIOImageMetadata commonMetadata = (GDALCommonIIOImageMetadata) metadata;
            ImageIOUtilities
                    .displayImageIOMetadata(commonMetadata
                            .getAsTree(GDALCommonIIOImageMetadata.nativeMetadataFormatName));
            ImageIOUtilities.displayImageIOMetadata(commonMetadata
                    .getAsTree(MrSIDIIOImageMetadata.mrsidImageMetadataName));
            if (TestData.isInteractiveTest())
                Viewer.visualizeAllInformation(image, "", TestData.isInteractiveTest());
            else {
            	ImageIOUtilities.disposeImage(image);
            }
        } catch (FileNotFoundException fnfe) {
            warningMessage();
        }
        
        try {
            final File file = TestData.file(this, fileName);
            ImageReader reader= new MrSIDImageReaderSpi().createReaderInstance();
            reader.setInput(ImageIO.createImageInputStream(file));
            Assert.assertEquals(618,reader.getWidth(0));
            Assert.assertEquals(1265,reader.getHeight(0));
            IIOMetadata metadata = (IIOMetadata) reader.getImageMetadata(0);
            Assert.assertTrue(metadata instanceof GDALCommonIIOImageMetadata);
            Assert.assertTrue(metadata instanceof MrSIDIIOImageMetadata);
            GDALCommonIIOImageMetadata commonMetadata = (GDALCommonIIOImageMetadata) metadata;
            ImageIOUtilities
                    .displayImageIOMetadata(commonMetadata
                            .getAsTree(GDALCommonIIOImageMetadata.nativeMetadataFormatName));
            ImageIOUtilities.displayImageIOMetadata(commonMetadata
                    .getAsTree(MrSIDIIOImageMetadata.mrsidImageMetadataName));
            reader.dispose();
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
    @Test
    public void jaiOperations() throws FileNotFoundException, IOException {
        if (!isMrSidAvailable) {
            return;
        }
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
                Viewer.visualizeAllInformation(image, "Subsampling Read");
            else
            	Assert.assertNotNull(image.getTiles());

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
                Viewer.visualizeAllInformation(croppedImage, "Cropped Image");
            else
            	Assert.assertNotNull(croppedImage.getTiles());

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
                Viewer.visualizeAllInformation(translatedImage, "Translated Image");
            else
            	Assert.assertNotNull(image.getTiles());

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
                Viewer.visualizeAllInformation(rotatedImage, "Rotated Image");
            else {
            	Assert.assertNotNull(image.getTiles());
            	ImageIOUtilities.disposeImage(image);
            }
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
    @Test
    public void subBandsRead() throws IOException {
        if (!isMrSidAvailable) {
            return;
        }
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
                Viewer.visualizeAllInformation(image, "SourceBand selection");
            else {
            	Assert.assertNotNull(image.getTiles());
            	ImageIOUtilities.disposeImage(image);
            }
            
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
    @Test
    public void manualRead() throws IOException {
        if (!isMrSidAvailable) {
            return;
        }
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
            irp.setDestinationOffset(new Point(0, 0));
            reader.read(0, irp);
            irp.setSourceRegion(new Rectangle(halfWidth, halfHeight / 2,
                    halfWidth, halfHeight / 4));
            irp.setDestinationOffset(new Point(halfWidth, halfHeight / 2));
            reader.read(0, irp);

            if (TestData.isInteractiveTest())
                Viewer.visualizeAllInformation(bi, "MrSID Destination settings");
            else
            	Assert.assertNotNull(bi);

            reader.dispose();
        } catch (FileNotFoundException fnfe) {
            super.warningMessage();
        }
    }
}
