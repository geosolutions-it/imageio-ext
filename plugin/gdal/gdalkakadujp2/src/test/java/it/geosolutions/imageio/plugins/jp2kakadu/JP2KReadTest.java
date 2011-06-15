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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.plugins.jp2kakadu.JP2GDALKakaduImageReaderSpi.KakaduErrorManagementType;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Testing reading capabilities for {@link JP2GDALKakaduImageReader} leveraging
 * on JAI.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JP2KReadTest extends AbstractJP2KTestCase {

    public final static String fileName = "test.jp2";

    /**
     * Simple test read
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void read() throws FileNotFoundException, IOException {
        if (!isJp2KakDriverAvailable) {
            return;
        }

        final ParameterBlockJAI pbjImageRead;
        final File file = TestData.file(this, fileName);
        JP2GDALKakaduImageReaderSpi
                .setKakaduInputErrorManagement(KakaduErrorManagementType.FAST);
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("Reader", new JP2GDALKakaduImageReaderSpi()
                .createReaderInstance());
        final ImageLayout layout = new ImageLayout();
        layout.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256)
                .setTileWidth(256);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
        if (TestData.isInteractiveTest())
            Viewer.visualizeBothMetadata(image, "");
        else
            Assert.assertNotNull(image.getTiles());
        ImageIOUtilities.disposeImage(image);
    }

    /**
     * Test read exploiting common JAI operations (Crop-Translate-Rotate)
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void jaiOperations() throws IOException {
        if (!isJp2KakDriverAvailable) {
            return;
        }
        final File inputFile = TestData.file(this, fileName);

        JP2GDALKakaduImageReaderSpi
                .setKakaduInputErrorManagement(KakaduErrorManagementType.FAST);

        // ////////////////////////////////////////////////////////////////
        // preparing to read
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageRead;
        final ImageReadParam irp = new ImageReadParam();

        Integer xSubSampling = new Integer(2);
        Integer ySubSampling = new Integer(1);
        Integer xSubSamplingOffset = new Integer(0);
        Integer ySubSamplingOffset = new Integer(0);

        irp.setSourceSubsampling(xSubSampling.intValue(), ySubSampling
                .intValue(), xSubSamplingOffset.intValue(), ySubSamplingOffset
                .intValue());

        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        pbjImageRead.setParameter("readParam", irp);
        pbjImageRead.setParameter("Reader", new JP2GDALKakaduImageReaderSpi()
                .createReaderInstance());

        final ImageLayout layout = new ImageLayout();
        layout.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512)
                .setTileWidth(512);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "subsampled");

        // ////////////////////////////////////////////////////////////////
        // preparing to crop
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjCrop = new ParameterBlockJAI("Crop");
        pbjCrop.addSource(image);

        // Setting a square crop to avoid blanks zone when rotating.
        Float xCrop = new Float(image.getMinX() + image.getWidth() / 4);
        Float yCrop = new Float(image.getMinX() + image.getWidth() / 4);
        Float cropWidth = new Float(image.getWidth() / 4);
        Float cropHeigth = new Float(image.getWidth() / 4);
        pbjCrop.setParameter("x", xCrop);
        pbjCrop.setParameter("y", yCrop);
        pbjCrop.setParameter("width", cropWidth);
        pbjCrop.setParameter("height", cropHeigth);
        final RenderedOp croppedImage = JAI.create("Crop", pbjCrop);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(croppedImage, "cropped");

        // ////////////////////////////////////////////////////////////////
        // preparing to translate
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjTranslate = new ParameterBlockJAI(
                "Translate");
        pbjTranslate.addSource(croppedImage);
        Float xTrans = new Float(xCrop.floatValue() * (-1));
        Float yTrans = new Float(yCrop.floatValue() * (-1));
        pbjTranslate.setParameter("xTrans", xTrans);
        pbjTranslate.setParameter("yTrans", yTrans);
        final RenderedOp translatedImage = JAI
                .create("Translate", pbjTranslate);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(translatedImage, "translated");

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

        StringBuffer title = new StringBuffer("SUBSAMP:").append("X[").append(
                xSubSampling.toString()).append("]-Y[").append(
                ySubSampling.toString()).append("]-Xof[").append(
                xSubSamplingOffset.toString()).append("]-Yof[").append(
                ySubSamplingOffset).append("]CROP:X[").append(xCrop.toString())
                .append("]-Y[").append(yCrop.toString()).append("]-W[").append(
                        cropWidth.toString()).append("]-H[").append(
                        cropHeigth.toString()).append("]TRANS:X[").append(
                        xTrans.toString()).append("]-Y[").append(
                        yTrans.toString()).append("]ROTATE:xOrig[").append(
                        xOrigin.toString()).append("]-yOrig[").append(
                        yOrigin.toString()).append("]-ang[").append(
                        angle.toString()).append("]");
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(rotatedImage, title.toString());
        else
            Assert.assertNotNull(rotatedImage.getTiles());
        ImageIOUtilities.disposeImage(rotatedImage);
    }

}
