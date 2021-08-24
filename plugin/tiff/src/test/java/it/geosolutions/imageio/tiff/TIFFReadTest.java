/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007 - 2016, GeoSolutions
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
package it.geosolutions.imageio.tiff;

import static org.junit.Assume.assumeTrue;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.PlanarImage;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.media.jai.operator.ImageReadDescriptor;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.PrivateTIFFTagSet;
import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageReader;
import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegUtilities;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFJPEGDecompressor;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFStreamMetadata.MetadataNode;
import it.geosolutions.resources.TestData;

/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class TIFFReadTest extends Assert {

    /** Logger used for recording any possible exception */
    private final static Logger logger = Logger.getLogger(TIFFReadTest.class.getName());

    @Test
    public void readFromFileJAI() throws IOException {
        final File file = TestData.file(this, "test.tif");

        // double sum = 0;
        // final long num = 10000l;

        // for (long i = 0; i < num; i++) {
        // final double time = System.nanoTime();

        // IMAGE 0
        RenderedImage image = ImageReadDescriptor.create(new FileImageInputStream(file),
                Integer.valueOf(0), false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(PlanarImage.wrapRenderedImage(image).getTiles());
        // sum += System.nanoTime() - time;
        Assert.assertEquals(30, image.getWidth());
        Assert.assertEquals(26, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;
        // }

        // IMAGE 2
        final ImageReadParam readParam = new ImageReadParam();
        readParam.setSourceRegion(new Rectangle(0, 0, 10, 10));
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(2),
                false, false, false, null, null, readParam,
                new TIFFImageReaderSpi().createReaderInstance(), null);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(8, image.getWidth());
        Assert.assertEquals(7, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 4
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(4),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(2, image.getWidth());
        Assert.assertEquals(2, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 5
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(5),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(1, image.getWidth());
        Assert.assertEquals(1, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 1
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(1),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(15, image.getWidth());
        Assert.assertEquals(13, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 3
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(3),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(4, image.getWidth());
        Assert.assertEquals(4, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;

        // IMAGE 5
        image = ImageReadDescriptor.create(new FileImageInputStream(file), Integer.valueOf(5),
                false, false, false, null, null, null,
                new TIFFImageReaderSpi().createReaderInstance(), null);

        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
            Assert.assertNotNull(image.getData());
        // sum+=System.nanoTime()-time;
        Assert.assertEquals(1, image.getWidth());
        Assert.assertEquals(1, image.getHeight());

        PlanarImage.wrapRenderedImage(image).dispose();
        image = null;



    }

    @Test
    public void readFromFileDirect() throws IOException {

        final File file = TestData.file(this, "test.tif");

        final ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(0, 0, 2, 2));

        // double sum=0;
        // final long num = 10000l;

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        
        FileImageInputStream inputStream = new FileImageInputStream(file);
        try{
        	reader.setInput(inputStream);
            // System.out.println(new IIOMetadataDumper(
            // reader.getImageMetadata(0),TIFFImageMetadata.nativeMetadataFormatName).getMetadata());

            // for(long i=0;i<num;i++){
            // final double time= System.nanoTime();
            // IMAGE 0
            BufferedImage image = reader.read(0, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            image = reader.read(1, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            image = reader.read(2, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            image = reader.read(1, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            image = reader.read(3, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            image = reader.read(0, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;


            // sum+=System.nanoTime()-time;
            // Assert.assertEquals(120, image.getWidth());
            // Assert.assertEquals(107, image.getHeight());

            // System.out.println("test "+i);
            //
            // }
            // System.out.println(sum/num);
        }catch(Exception e){
			// If an exception occurred the logger catch the exception and print
			// the message
			logger.log(Level.SEVERE, e.getMessage(), e);
        }finally{
			// Finally, if an exception has been thrown or not, the reader
			// and the input stream are closed
			if(inputStream!=null){
				inputStream.flush();
				inputStream.close();
			}
			
			if (reader != null) {
				reader.dispose();
			}
        }
    }

    @Test
    public void readMasks() throws IOException {
        // Reading a File with internal masks
        final File file = TestData.file(this, "masks.tif");
        // Setting Read parameters
        final ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(0, 0, 2, 2));
        // Reader creation
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        // Stream creation
        FileImageInputStream inputStream = new FileImageInputStream(file);
        try {
            // Setting input
            reader.setInput(inputStream);
            // IMAGE 0
            BufferedImage image = reader.read(0, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            // Getting Stream Metadata
            IIOMetadata metadata = reader.getStreamMetadata();
            Node tree = metadata.getAsTree("com_sun_media_imageio_plugins_tiff_stream_1.0");
            // Ensuring not null
            Assert.assertNotNull(tree);

            // Checking Childs
            NodeList list = tree.getChildNodes();
            int len = list.getLength();
            // Loop on the list
            for (int i = 0; i < len; i++) {
                // Node i-th
                Node node = list.item(i);
                // Ensuring not null
                Assert.assertNotNull(node);
                // Getting the name
                String nodeName = node.getNodeName();
                // Checking attributes
                Assert.assertTrue(node.hasAttributes());
                // Getting Attribute Value
                String value = node.getAttributes().item(0).getNodeValue();
                // Getting related enum
                MetadataNode mnode = MetadataNode.getFromName(nodeName);
                // Checking Attribute value
                switch (mnode) {
                case B_ORDER:
                    Assert.assertTrue(value.equalsIgnoreCase(ByteOrder.LITTLE_ENDIAN.toString()));
                    break;
                case N_INT_MASK:
                    Assert.assertEquals(5, Integer.parseInt(value));
                    break;
                case N_EXT_MASK:
                    Assert.assertEquals(-1, Integer.parseInt(value));
                    break;
                case N_INT_OVR:
                    Assert.assertEquals(4, Integer.parseInt(value));
                    break;
                case N_EXT_OVR:
                    Assert.assertEquals(-1, Integer.parseInt(value));
                    break;
                case N_EXT_OVR_MASK:
                    Assert.assertEquals(-1, Integer.parseInt(value));
                    break;
                case EXT_MASK_FILE:
                    Assert.assertTrue(value.isEmpty());
                    break;
                case EXT_OVR_FILE:
                    Assert.assertTrue(value.isEmpty());
                    break;
                case EXT_OVR_MASK_FILE:
                    Assert.assertTrue(value.isEmpty());
                    break;
                default:
                    // Wrong element
                    Assert.assertTrue(false);
                }
            }
        } catch (Exception e) {
            // If an exception occurred the logger catch the exception and print
            // the message
            logger.log(Level.SEVERE, e.getMessage(), e);
            Assert.assertTrue(false);
        } finally {
            // Finally, if an exception has been thrown or not, the reader
            // and the input stream are closed
            if (inputStream != null) {
                inputStream.flush();
                inputStream.close();
            }

            if (reader != null) {
                reader.dispose();
            }
        }
    }

    @Test
    public void readExternalMasks() throws IOException {
        // Reading file with external masks
        final File file = TestData.file(this, "external.tif");
        // Setting read parameters
        final ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(0, 0, 2, 2));
        // Creating the reader
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        // Using FileImageInputStreamExt for being able to locate the file path
        FileImageInputStreamExt inputStream = new FileImageInputStreamExtImpl(file);
        try {
            // reading phase
            reader.setInput(inputStream);
            // IMAGE 0
            BufferedImage image = reader.read(0, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            // Getting Stream Metadata
            IIOMetadata metadata = reader.getStreamMetadata();
            Node tree = metadata.getAsTree("com_sun_media_imageio_plugins_tiff_stream_1.0");
            // Ensuring not null
            Assert.assertNotNull(tree);

            // Checking Childs
            NodeList list = tree.getChildNodes();
            int len = list.getLength();
            // Loop the nodes
            for (int i = 0; i < len; i++) {
                // Node i-th
                Node node = list.item(i);
                // Ensuring not null
                Assert.assertNotNull(node);
                // Getting the name
                String nodeName = node.getNodeName();
                // Checking attributes
                Assert.assertTrue(node.hasAttributes());
                // Getting Attribute Value
                String value = node.getAttributes().item(0).getNodeValue();
                // Getting related enum
                MetadataNode mnode = MetadataNode.getFromName(nodeName);
                // Checking Attribute value
                switch (mnode) {
                case B_ORDER:
                    Assert.assertTrue(value.equalsIgnoreCase(ByteOrder.LITTLE_ENDIAN.toString()));
                    break;
                case N_INT_MASK:
                    Assert.assertEquals(0, Integer.parseInt(value));
                    break;
                case N_EXT_MASK:
                    Assert.assertEquals(5, Integer.parseInt(value));
                    break;
                case N_INT_OVR:
                    Assert.assertEquals(4, Integer.parseInt(value));
                    break;
                case N_EXT_OVR:
                    Assert.assertEquals(0, Integer.parseInt(value));
                    break;
                case N_EXT_OVR_MASK:
                    Assert.assertEquals(0, Integer.parseInt(value));
                    break;
                case EXT_MASK_FILE:
                    Assert.assertTrue(value.contains("external.tif.msk"));
                    break;
                case EXT_OVR_FILE:
                    Assert.assertTrue(value.isEmpty());
                    break;
                case EXT_OVR_MASK_FILE:
                    Assert.assertTrue(value.isEmpty());
                    break;
                default:
                    // Wrong element
                    Assert.assertTrue(false);
                }
            }
        } catch (Exception e) {
            // If an exception occurred the logger catch the exception and print
            // the message
            logger.log(Level.SEVERE, e.getMessage(), e);
            Assert.assertTrue(false);
        } finally {
            // Finally, if an exception has been thrown or not, the reader
            // and the input stream are closed
            if (inputStream != null) {
                inputStream.flush();
                inputStream.close();
            }

            if (reader != null) {
                reader.dispose();
            }
        }
    }

    @Test
    public void readExternalMasksOvr() throws IOException {
        // Reading file with external mask and external mask overviews
        final File file = TestData.file(this, "external2.tif");
        // Read parameters
        final ImageReadParam param = new ImageReadParam();
        param.setSourceRegion(new Rectangle(0, 0, 2, 2));
        // Creating a new Reader
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();
        // Using FileImageInputStreamExt for being able to locate the file path
        FileImageInputStreamExt inputStream = new FileImageInputStreamExtImpl(file);
        try {
            // Reading
            reader.setInput(inputStream);
            // IMAGE 0
            BufferedImage image = reader.read(0, param);
            Assert.assertEquals(2, image.getWidth());
            Assert.assertEquals(2, image.getHeight());
            image.flush();
            image = null;

            // Getting Stream Metadata
            IIOMetadata metadata = reader.getStreamMetadata();
            Node tree = metadata.getAsTree("com_sun_media_imageio_plugins_tiff_stream_1.0");
            // Ensuring not null
            Assert.assertNotNull(tree);

            // Checking Childs
            NodeList list = tree.getChildNodes();
            int len = list.getLength();
            // Loop on the Node list
            for (int i = 0; i < len; i++) {
                Node node = list.item(i);
                // Ensuring not null
                Assert.assertNotNull(node);
                // Getting the name
                String nodeName = node.getNodeName();
                // Checking attributes
                Assert.assertTrue(node.hasAttributes());
                // Getting Attribute Value
                String value = node.getAttributes().item(0).getNodeValue();
                // Getting related enum
                MetadataNode mnode = MetadataNode.getFromName(nodeName);
                // Checking Attribute value
                switch (mnode) {
                case B_ORDER:
                    Assert.assertTrue(value.equalsIgnoreCase(ByteOrder.LITTLE_ENDIAN.toString()));
                    break;
                case N_INT_MASK:
                    Assert.assertEquals(0, Integer.parseInt(value));
                    break;
                case N_EXT_MASK:
                    Assert.assertEquals(1, Integer.parseInt(value));
                    break;
                case N_INT_OVR:
                    Assert.assertEquals(0, Integer.parseInt(value));
                    break;
                case N_EXT_OVR:
                    Assert.assertEquals(0, Integer.parseInt(value));
                    break;
                case N_EXT_OVR_MASK:
                    Assert.assertEquals(4, Integer.parseInt(value));
                    break;
                case EXT_MASK_FILE:
                    Assert.assertTrue(value.contains("external2.tif.msk"));
                    break;
                case EXT_OVR_FILE:
                    Assert.assertTrue(value.isEmpty());
                    break;
                case EXT_OVR_MASK_FILE:
                    Assert.assertTrue(value.contains("external2.tif.msk.ovr"));
                    break;
                default:
                    // Wrong element
                    Assert.assertTrue(false);
                }
            }
        } catch (Exception e) {
            // If an exception occurred the logger catch the exception and print
            // the message
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            // Finally, if an exception has been thrown or not, the reader
            // and the input stream are closed
            if (inputStream != null) {
                inputStream.flush();
                inputStream.close();
            }

            if (reader != null) {
                reader.dispose();
            }
        }
    }

    @Test
    public void readWithEmptyTiles() throws IOException {

        // This input image is a 1440x720 image. However, the right half of the image
        // is made of empty tiles filled with nodata
        final File file = TestData.file(this, "emptyTiles.tif");

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();

        FileImageInputStream inputStream = new FileImageInputStream(file);
        try {
            reader.setInput(inputStream);
            ImageReadParam param = new ImageReadParam();
            // Setting up a region to fall in the half of the image containing empty tiles
            param.setSourceRegion(new Rectangle(360,0,720,720));
            BufferedImage image = reader.read(0, param);
            Assert.assertEquals(720, image.getWidth());
            Assert.assertEquals(720, image.getHeight());

            IIOMetadata metadata = reader.getImageMetadata(0);
            Node rootNode = metadata.getAsTree(metadata.getNativeMetadataFormatName());
            double noDataValue = getNoDataValue(rootNode);

            // get it from the core common metadata too
            CoreCommonImageMetadata ccm = (CoreCommonImageMetadata) metadata;
            double[] noDataArray = ccm.getNoData();
            assertNotNull(noDataArray);
            assertEquals(noDataArray[0], noDataValue, 0d);
            assertEquals(noDataArray[1], noDataValue, 0d);

            // Check that the value is noData (the empty Tiles are filled with NoData) 
            double val = image.getData().getSampleDouble(719, 0, 0);
            assertEquals(Double.toString(noDataValue), Double.toString(val));
            image.flush();
            image = null;
        } catch (Exception e) {
            // If an exception occurred the logger catch the exception and print
            // the message
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {

            if (inputStream != null) {
                inputStream.flush();
                inputStream.close();
            }

            if (reader != null) {
                reader.dispose();
            }
        }
    }

    @Test
    public void readLZWWithHorizontalDifferencingPredictorOn16Bits() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -OT UInt16 -co COMPRESS=LZW -co PREDICTOR=2 test.tif lzwtest.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("lzwtest.tif"));
    }

    @Test
    public void readDeflateWithHorizontalDifferencingPredictorOn16Bits() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -OT UInt16 -co COMPRESS=DEFLATE -co PREDICTOR=2 test.tif deflatetest.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("deflatetest.tif"));
    }

    @Test
    public void readDeflatePredictor2On32BitsInt() throws IOException {
        // This image has been created from test.tif using the command:
        //  gdal_translate -OT UInt32 -co COMPRESS=DEFLATE -co PREDICTOR=2 test.tif deflate32_p2.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("deflate32_p2.tif"));
    }

    @Test
    public void readDeflatePredictor2On32BitsIntBigEndian() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot UInt32 -co COMPRESS=DEFLATE -co PREDICTOR=2 test.tif 
        //                --config GDAL_TIFF_ENDIANNESS BIG deflate32_p2_bigendian.tif  
        assertImagesEqual(readTiff("test.tif"), readTiff("deflate32_p2_bigendian.tif"));
    }

    @Test
    public void reaLzwPredictor2On32BitsInt() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -OT UInt32 -co COMPRESS=LZW -co PREDICTOR=2 test.tif lzw32_p2.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("lzw32_p2.tif"));
    }

    @Test
    public void readLzwPredictor2On32BitsIntBigEndian() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot UInt32 -co COMPRESS=LZW -co PREDICTOR=2 test.tif --config GDAL_TIFF_ENDIANNESS BIG lzw32_p2_bigendian.tif  
        assertImagesEqual(readTiff("test.tif"), readTiff("lzw32_p2_bigendian.tif"));
    }

    @Test
    public void readDeflatePredictor2On32BitsFloat() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot Float32 -co COMPRESS=DEFLATE -co PREDICTOR=2 test.tif deflate32f_p2.tif  
        assertImagesEqual(readTiff("test.tif"), readTiff("deflate32f_p2.tif"));
    }

    @Test
    public void readDeflatePredictor2On32BitsFloatBigEndian() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot Float32 -co COMPRESS=DEFLATE -co PREDICTOR=2 --config
        // GDAL_TIFF_ENDIANNESS BIG test.tif deflate32f_p2_bigendian.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("deflate32f_p2_bigendian.tif"));
    }

    @Test
    public void readLzwPredictor2On32BitsFloat() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot Float32 -co COMPRESS=LZW -co PREDICTOR=2 test.tif lzw32f_p2.tif  
        assertImagesEqual(readTiff("test.tif"), readTiff("lzw32f_p2.tif"));
    }

    @Test
    public void readLzwPredictor2On32BitsFloatBigEndian() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot Float32 -co COMPRESS=LZW -co PREDICTOR=2 --config
        // GDAL_TIFF_ENDIANNESS BIG test.tif lzw32f_p2_bigendian.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("lzw32f_p2_bigendian.tif"));
    }

    @Test
    public void readDeflateWithFloatingPointPredictor() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot Float32 -co COMPRESS=DEFLATE -co PREDICTOR=3 test.tif deflate_predictor_3.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("deflate_predictor_3.tif"));
    }

    @Test
    public void readZSTDOn16BitsInteger() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot UInt16 -co COMPRESS=ZSTD test.tif zstd.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("zstd.tif"));
    }

    @Test
    public void readZSTDPredictor2On32BitsInteger() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot UInt32 -co COMPRESS=ZSTD -co PREDICTOR=2 test.tif zstd_p2.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("zstd_p2.tif"));
    }

    @Test
    public void readZSTDPredictor3On32BitsFloat() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -ot Float32 -co COMPRESS=ZSTD -CO PREDICTOR=3 test.tif zstd_p3.tif
        assertImagesEqual(readTiff("test.tif"), readTiff("zstd_p3.tif"));
    }

    @Test
    public void readZSTDOnRGB() throws IOException {
        // This image has been created from sampleRGBA.tif using the command:
        // gdal_translate -co COMPRESS=ZSTD sampleRGBA.tif zstd_rgba.tif
        assertImagesEqual(readTiff("sampleRGBA.tif"), readTiff("zstd_rgba.tif"));
    }

    static void assertImagesEqual(BufferedImage expected, BufferedImage actual) {
        assertEquals("Widths are different", expected.getWidth(), actual.getWidth());
        assertEquals("Heights are different", expected.getHeight(), actual.getHeight());
        int w = expected.getRaster().getWidth();
        int h = expected.getRaster().getHeight();
        assertArrayEquals(
                "Rasters are different",
                toByteArray(expected.getSampleModel().getDataType(), expected.getRaster().getDataElements(0, 0, w, h, null)),
                toByteArray(actual.getSampleModel().getDataType(), actual.getRaster().getDataElements(0, 0, w, h, null)));
    }

    static int[] toByteArray(int dataType, Object arr) {
        int[] result = new int[Array.getLength(arr)];
        for (int i = 0; i < result.length; i++) {
            Number value = (Number) Array.get(arr, i);
            if (dataType == DataBuffer.TYPE_BYTE) {
                result[i] = value.byteValue() & 0xFF;
            } else {
                result[i] = value.intValue();
            }
        }
        return result;
    }

    private BufferedImage readTiff(String filename) throws IOException {
        final File file = TestData.file(this, filename);
        return readTiff(file);
    }

    static BufferedImage readTiff(File file) throws IOException {

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();

        FileImageInputStream inputStream = new FileImageInputStream(file);
        try {
            reader.setInput(inputStream);
            BufferedImage image = reader.read(0);
            image.flush();
            return image;
        } finally {
            inputStream.flush();
            inputStream.close();
            reader.dispose();
        }
    }

    @Test
    public void readRGBAlphaExtraSample() throws IOException {
        final boolean hasAlpha = true;
        final String description = "Unassociated Alpha";
        final int value = 2;
        readExtraSample("sampleRGBA.tif", hasAlpha, description, value);
    }

    @Test
    public void readRGBNotAlphaExtraSample() throws IOException {
        final boolean hasAlpha = false;
        final String description = "Unspecified";
        final int value = 0;
        readExtraSample("sampleRGBIR.tif", hasAlpha, description, value);
    }

    @Test
    public void readTIFFTurboJpegNoJpegTables() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // This image has been created with this command on GDAL 2.1.3:
        // gdal_translate -co COMPRESS=JPEG -CO TILED=YES -CO JPEGTABLESMODE=0\ 
        // -CO BLOCKXSIZE=64 -CO BLOCKYSIZE=64 -outsize 256 256 -r bilinear test.tif notables.tif

        // This will create a TIFF with internally compressed JPEG images but no JPEGTables metadata
        // TurboJPEG Reader decodes byte array provided by the compressor
        if (!TurboJpegUtilities.isTurboJpegAvailable()) {
            logger.warning("Unable to find native libs. Tests are skipped");
            assumeTrue(false);
            return;
        }
        final File file = TestData.file(this, "notables.tif");

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
        FileImageInputStream fis = null;
        BufferedImage image = null;

        try {
            fis = new FileImageInputStream(file);
            reader.setInput(fis);
            ImageReadParam param = new ImageReadParam();
            param.setSourceRegion(new Rectangle(0,0,64,64));
            image = reader.read(0, param);

            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
            assertEquals(1, image.getSampleModel().getNumBands());

            // Using reflection to check the data array being used
            Field f = reader.getClass().getDeclaredField("decompressor");
            f.setAccessible(true);
            TIFFJPEGDecompressor decompressor = (TIFFJPEGDecompressor) f.get(reader);

            f = decompressor.getClass().getDeclaredField("JPEGReader");
            f.setAccessible(true);
            TurboJpegImageReader jpegReader = (TurboJpegImageReader) f.get(decompressor);

            f = jpegReader.getClass().getDeclaredField("data");
            f.setAccessible(true);
            byte[] data = (byte[]) f.get(jpegReader);

            // Before the fix, the data array would have been, more or less, big as 
            // the whole stream content (almost 16000), making this check fail.
            assertTrue(data.length < 300);
            image.flush();
            image = null;
        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Throwable t) {
                    // Does nothing
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable t) {
                    // Does nothing
                }
            }
        }
    }

    private void readExtraSample(String inputFile, boolean hasAlpha, String description, int value) throws IOException {
        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();

        final File file = TestData.file(this, inputFile);
        FileImageInputStream inputStream = new FileImageInputStream(file);
        try {
            reader.setInput(inputStream);
            BufferedImage image = reader.read(0);
            SampleModel sm = image.getSampleModel();
            ColorModel cm = image.getColorModel();
            assertEquals(4, sm.getNumBands());
            assertEquals(4, cm.getNumComponents());
            assertTrue(hasAlpha == cm.hasAlpha());
            image.flush();

            IIOMetadata metadata = reader.getImageMetadata(0);
            Node rootNode = metadata.getAsTree(metadata.getNativeMetadataFormatName());
            IIOMetadataNode field = getTiffField(rootNode, BaselineTIFFTagSet.TAG_EXTRA_SAMPLES);
            assertNotNull(field);
            Node node = ((IIOMetadataNode) field.getFirstChild()).getElementsByTagName("TIFFShort").item(0);
            NamedNodeMap map = node.getAttributes();
            assertTrue(description.equalsIgnoreCase(map.item(1).getNodeValue()));
            assertEquals(value, Integer.parseInt(map.item(0).getNodeValue()));
            image = null;
        } finally {

            if (inputStream != null) {
                inputStream.flush();
                inputStream.close();
            }

            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private double getNoDataValue(Node rootNode) {
        final IIOMetadataNode noDataNode = getTiffField(rootNode, PrivateTIFFTagSet.TAG_GDAL_NODATA);
        if (noDataNode == null) {
            return Double.NaN;
        }
        Node node = ((IIOMetadataNode) noDataNode .getFirstChild()).getElementsByTagName("TIFFAscii").item(0);
        final String valueAttribute = node.getAttributes().getNamedItem("value").getNodeValue();
        final int length = valueAttribute.length() + 1;

        final String noData = valueAttribute.substring(0, length - 1);
        if (noData == null) {
            return Double.NaN;
        }
        try {
            if ("nan".equalsIgnoreCase(noData)) {
                return Double.NaN;
            }
            return Double.parseDouble(noData);
        } catch (NumberFormatException nfe) {
            // TODO: Log a message.
            return Double.NaN;
        }
    }

    private IIOMetadataNode getTiffField(Node rootNode, final int tag) {
        Node node = rootNode.getFirstChild();
        if (node != null) {
            node = node.getFirstChild();
            for (; node != null; node = node.getNextSibling()) {
                Node number = node.getAttributes().getNamedItem("number");
                if (number != null && tag == Integer.parseInt(number.getNodeValue())) {
                    return (IIOMetadataNode) node;
                }
            }
        }
        return null;
    }
}
