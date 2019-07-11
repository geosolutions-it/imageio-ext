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

import it.geosolutions.imageio.plugins.tiff.PrivateTIFFTagSet;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFStreamMetadata.MetadataNode;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class TIFFReadTest extends Assert {

	
	/** Logger used for recording any possible exception */
	private Logger logger = Logger.getLogger(TIFFReadTest.class.getName());
	
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
        final File file = TestData.file(this, "lzwtest.tif");

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();

        FileImageInputStream inputStream = new FileImageInputStream(file);
        try {
            reader.setInput(inputStream);
            BufferedImage image = reader.read(0);
            image.flush();
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

    @Test
    public void readDeflateWithHorizontalDifferencingPredictorOn16Bits() throws IOException {
        // This image has been created from test.tif using the command:
        // gdal_translate -OT UInt16 -co COMPRESS=DEFLATE -co PREDICTOR=2 test.tif deflatetest.tif
        final File file = TestData.file(this, "deflatetest.tif");

        final TIFFImageReader reader = (TIFFImageReader) new TIFFImageReaderSpi()
                .createReaderInstance();

        FileImageInputStream inputStream = new FileImageInputStream(file);
        try {
            reader.setInput(inputStream);
            BufferedImage image = reader.read(0);
            image.flush();
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
