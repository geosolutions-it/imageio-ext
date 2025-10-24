/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageio.plugins.arcbinarygrid;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.ParameterBlockImageN;
import org.eclipse.imagen.RenderedOp;
import org.junit.Assert;

/**
 * Testing reading capabilities for {@link ArcBinaryGridImageReader} leveraging on ImageN.
 *
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class ArcBinaryGridReadTest extends AbstractGDALTest {
    public ArcBinaryGridReadTest() {
        super();
    }

    /**
     * To run this test, you need to get the content of the whole nzdem500 folder available at:
     * http://download.osgeo.org/gdal/data/aig/nzdem/nzdem500/
     */
    private static final String fileName = "nzdem500/vat.adf";

    private static final StringBuilder warningMessage = new StringBuilder("test-data not found: ")
            .append(fileName)
            .append("\n download it at http://download.osgeo.org/gdal/data/aig/nzdem/nzdem500/")
            .append("\nTests are skipped");

    /**
     * Simple test read through ImageN - ImageIO
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testReadJAI() throws IOException {
        if (!isGDALAvailable) {
            return;
        }
        final ParameterBlockImageN pbjImageRead;
        File file = null;
        try {
            file = TestData.file(this, fileName);
        } catch (FileNotFoundException fnfe) {
            LOGGER.warning(warningMessage.toString());
            return;
        }
        pbjImageRead = new ParameterBlockImageN("ImageRead");
        ImageReadParam rp = new ImageReadParam();
        rp.setSourceSubsampling(8, 8, 0, 0);
        pbjImageRead.setParameter("readParam", rp);
        pbjImageRead.setParameter("Input", file);
        RenderedOp image = ImageN.create("ImageRead", pbjImageRead);

        if (TestData.isInteractiveTest()) ImageIOUtilities.visualize(image, fileName, true);
        else image.getTiles();
        Assert.assertEquals(251, image.getWidth());
        Assert.assertEquals(369, image.getHeight());
        ImageIOUtilities.disposeImage(image);
    }

    /**
     * Simple test read through ImageIO
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void testReadImageIO() throws IOException {
        if (!isGDALAvailable) {
            return;
        }
        File file = null;
        try {
            file = TestData.file(this, fileName);
        } catch (FileNotFoundException fnfe) {
            LOGGER.warning(warningMessage.toString());
            return;
        }

        // //
        //
        // Try to get a reader for this raster data
        //
        // //
        final Iterator<ImageReader> it = ImageIO.getImageReaders(file);
        Assert.assertTrue(it.hasNext());

        // //
        //
        // read some data from it using subsampling
        //
        // //
        final ImageReader reader = (ImageReader) it.next();
        Assert.assertTrue(reader instanceof ArcBinaryGridImageReader);
        ImageReadParam rp = reader.getDefaultReadParam();
        rp.setSourceSubsampling(4, 4, 0, 0);
        reader.setInput(file);
        RenderedImage image = reader.read(0, rp);
        if (TestData.isInteractiveTest()) ImageIOUtilities.visualize(image, "subsample read " + file.getName(), true);
        reader.reset();
        reader.dispose();
        ImageIOUtilities.disposeImage(image);
    }
}
