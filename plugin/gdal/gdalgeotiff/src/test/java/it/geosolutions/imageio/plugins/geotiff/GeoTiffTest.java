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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.core.GCP;
import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.media.jai.operator.ImageWriteDescriptor;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class GeoTiffTest extends AbstractGDALTest {

    final static List<GCP> referenceGCPs = new ArrayList<GCP>(4);
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        File file = TestData.file(this, "test-data.zip");
        Assert.assertTrue(file.exists());

        // unzip it
        TestData.unzipFile(this, "test-data.zip");
	    
        final int columns[] = new int[] { 0, 50, 0, 50 };
        final int rows[] = new int[] { 0, 50, 50, 0 };
        final int easting[] = new int[] { -180, -135, -180, -135 };
        final int northing[] = new int[] { 90, 45, 45, 90 };
        for (int i = 0; i < 4; i++) {
            GCP gcp = new GCP();
            gcp.setId(Integer.toString(i + 1));
            gcp.setDescription("");
            gcp.setColumn(columns[i]);
            gcp.setRow(rows[i]);
            gcp.setEasting(easting[i]);
            gcp.setNorthing(northing[i]);
            referenceGCPs.add(gcp);
        }

    }
	
    public GeoTiffTest() {
        super();
    }

    /**
     * Test Read without exploiting JAI-ImageIO Tools
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void manualRead() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final ImageReadParam irp = new ImageReadParam();

        // Reading a simple GrayScale image
        String fileName = "utmByte.tif";
        final File inputFile = TestData.file(this, fileName);
        irp.setSourceSubsampling(2, 2, 0, 0);
        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        final RenderedImage image = reader.readAsRenderedImage(0, irp);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, fileName);
        Assert.assertEquals(128, image.getWidth());
        Assert.assertEquals(128, image.getHeight());
        reader.dispose();
    }

    /**
     * Test Read exploiting JAI-ImageIO tools capabilities
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void read() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        final ParameterBlockJAI pbjImageRead;
        String fileName = "utmByte.tif";
        final File file = TestData.file(this, fileName);

        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", new FileImageInputStreamExtImpl(file));
        pbjImageRead.setParameter("Reader", new GeoTiffImageReaderSpi().createReaderInstance());
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, "", true);
        else
        	Assert.assertNotNull(image.getTiles());
    }

    /**
     * Test Writing capabilities.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void write() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final File outputFile = TestData.temp(this, "writetest.tif", false);
        outputFile.deleteOnExit();
        final File inputFile = TestData.file(this, "utmByte.tif");

        ImageReadParam rparam = new ImageReadParam();
        rparam.setSourceRegion(new Rectangle(1, 1, 300, 500));
        rparam.setSourceSubsampling(1, 2, 0, 0);
        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        final IIOMetadata metadata = reader.getImageMetadata(0);

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        pbjImageRead.setParameter("reader", reader);
        pbjImageRead.setParameter("readParam", rparam);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256).setTileWidth(256);

        RenderedOp image = JAI.create("ImageRead", pbjImageRead,new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image,"geotiff");

        // ////////////////////////////////////////////////////////////////
        // preparing to write
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI("ImageWrite");
        ImageWriter writer = new GeoTiffImageWriterSpi().createWriterInstance();
        pbjImageWrite.setParameter("Output", outputFile);
        pbjImageWrite.setParameter("writer", writer);
        pbjImageWrite.setParameter("ImageMetadata", metadata);
        pbjImageWrite.setParameter("Transcode", false);
        ImageWriteParam param = new ImageWriteParam(Locale.getDefault());
        param.setSourceRegion(new Rectangle(10, 10, 100, 100));
        param.setSourceSubsampling(2, 1, 0, 0);
        pbjImageWrite.setParameter("writeParam", param);

        pbjImageWrite.addSource(image);
        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
        final ImageWriter writer2 = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
        writer2.dispose();

        // ////////////////////////////////////////////////////////////////
        // preparing to read again
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI("ImageRead");
        pbjImageReRead.setParameter("Input", outputFile);
        pbjImageReRead.setParameter("Reader", new GeoTiffImageReaderSpi() .createReaderInstance());
        final RenderedOp image2 = JAI.create("ImageRead", pbjImageReRead);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image2,"geotif2");
        else
        	Assert.assertNotNull(image2.getTiles());
    }

    /**
     * Test Read on a Paletted Image
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void palette() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        final File outputFile = TestData.temp(this, "writetest.tif", false);
        outputFile.deleteOnExit();
        final File inputFile = TestData.file(this, "paletted.tif");

        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        final IIOMetadata metadata = reader.getImageMetadata(0);

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        pbjImageRead.setParameter("reader", reader);

        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(256).setTileWidth(256);

        RenderedOp image = JAI.create("ImageRead", pbjImageRead, new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image, "Paletted image read");

        // ////////////////////////////////////////////////////////////////
        // preparing to write
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI("ImageWrite");
        ImageWriter writer = new GeoTiffImageWriterSpi().createWriterInstance();
        pbjImageWrite.setParameter("Output", outputFile);
        pbjImageWrite.setParameter("writer", writer);
        pbjImageWrite.setParameter("ImageMetadata", metadata);
        pbjImageWrite.setParameter("Transcode", false);
        pbjImageWrite.addSource(image);
        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
        final ImageWriter writer2 = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
        writer2.dispose();

        // ////////////////////////////////////////////////////////////////
        // preparing to read again
        // ////////////////////////////////////////////////////////////////
        final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI("ImageRead");
        pbjImageReRead.setParameter("Input", outputFile);
        pbjImageReRead.setParameter("Reader", new GeoTiffImageReaderSpi().createReaderInstance());
        final RenderedOp image2 = JAI.create("ImageRead", pbjImageReRead);
        if (TestData.isInteractiveTest())
            Viewer.visualizeAllInformation(image2,"Paletted image read back after writing");
        else
        	Assert.assertNotNull(image2.getTiles());
        ImageIOUtilities.disposeImage(image2);
        ImageIOUtilities.disposeImage(image);
    }
    
    @Test
    public void testDataTypes() throws IOException, FileNotFoundException {
        if (!isGDALAvailable) {
            return;
        }
        final List<String> fileList = new ArrayList<String>(4);
        fileList.add("paletted.tif");
        fileList.add("utmByte.tif");
        fileList.add("utmInt16.tif");
        fileList.add("utmInt32.tif");
        fileList.add("utmFloat32.tif");
        fileList.add("utmFloat64.tif");
        
        for (String fileName: fileList){
	        final ImageReadParam irp = new ImageReadParam();
	        final File inputFile = TestData.file(this, fileName);
	        irp.setSourceSubsampling(1, 1, 0, 0);
	        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
	        reader.setInput(inputFile);
	        final RenderedImage image = reader.readAsRenderedImage(0, irp);
	        if (TestData.isInteractiveTest())
	        	Viewer.visualizeAllInformation(image, fileName);
	        if(!fileName.contains("paletted")){
	        	Assert.assertEquals(256, image.getHeight());
	        	Assert.assertEquals(256, image.getWidth());
	        } else {
	        	Assert.assertEquals(128, image.getHeight());
	        	Assert.assertEquals(128, image.getWidth());
	        }
	        
	        reader.dispose();
        }
    }
    
    /**
     * Test Read exploiting JAI-ImageIO tools capabilities
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testGCP() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
        
        String fileName = "gcp.tif";
        final File file = TestData.file(this, fileName);

        ImageReader reader = new GeoTiffImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        GDALCommonIIOImageMetadata metadata = (GDALCommonIIOImageMetadata)reader.getImageMetadata(0);
        
        final int gcpNumber = metadata.getGcpNumber();
        Assert.assertEquals(gcpNumber, 4);
        
        final List<GCP> gcps = metadata.getGCPs();
        Assert.assertNotNull(gcps);
        Assert.assertFalse(gcps.isEmpty());
        for (int i = 0; i < 4; i++){
            Assert.assertEquals(gcps.get(i), referenceGCPs.get(i));
        }
        
        reader.dispose();
    }

}
