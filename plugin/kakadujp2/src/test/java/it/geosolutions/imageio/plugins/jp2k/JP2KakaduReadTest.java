/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.imageio.plugins.jp2k.box.XMLBox;
import it.geosolutions.imageio.plugins.jp2k.box.XMLBoxMetadataNode;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.jai.Histogram;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Testing reading capabilities for {@link JP2KKakaduImageReader} leveraging on
 * JAI.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class JP2KakaduReadTest extends AbstractJP2KakaduTestCase {


    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @org.junit.Test
    public void jaiReadFromFile() throws IOException {
        if (!runTests)
            return;
        final File file = TestData.file(this, "CB_TM432.jp2");
        ImageReadDescriptorMT.register(JAI.getDefaultInstance());

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                "ImageReadMT");
        ImageLayout l = new ImageLayout();
        l.setTileHeight(256);
        l.setTileWidth(256);

        JP2KKakaduImageReadParam rp = new JP2KKakaduImageReadParam();
        rp.setSourceSubsampling(1, 1, 0, 0);
        rp.setSourceRegion(new Rectangle(10,10,200,200));
        rp.setInterpolationType(JP2KKakaduImageReadParam.INTERPOLATION_BILINEAR);
        rp.setQualityLayers(2);
        pbjImageRead.setParameter("ReadParam", rp);
        pbjImageRead.setParameter("Input", file);
        pbjImageRead.setParameter("imageChoice", 0);
        RenderedOp image = JAI.create("ImageReadMT", pbjImageRead,
                new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image);
        else
        	Assert.assertNotNull(image.getTiles());
    }

    @org.junit.Test
    public void inputsTest() throws IOException {
        if (!runTests)
            return;

        // //
        //
        // Testing base reader methods 
        //
        // //
        final File file = TestData.file(this, "CB_TM432.jp2");
        final ImageReader reader = new JP2KKakaduImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        Assert.assertEquals(1,reader.getNumImages(false));
        Assert.assertEquals(488, reader.getTileHeight(0));
        Assert.assertEquals(361, reader.getTileWidth(0));
        Assert.assertEquals(488, reader.getHeight(0));
        Assert.assertEquals(361, reader.getWidth(0));
        Assert.assertNotNull(reader.getStreamMetadata());
        Assert.assertNotNull(reader.getImageMetadata(0));
        Assert.assertNotNull(reader.getImageTypes(0));

        // //
        //
        // Quick Test on wrong image index 
        //
        // //
        boolean isValidImageIndex = false; 
        try{
        	reader.getWidth(99);
        	isValidImageIndex = true;
        } catch (IndexOutOfBoundsException e){
        	Assert.assertFalse(isValidImageIndex);
        }
        
        // //
        //
        // Testing raw jp2 file 
        //
        // //
        final File rawfile = TestData.file(this, "raw.j2c");
        final ImageReader rawreader = new JP2KKakaduImageReaderSpi().createReaderInstance();
        rawreader.setInput(rawfile);
        rawreader.read(0);
        boolean hasStreamMetadata = false;
        try{
        	rawreader.getStreamMetadata();
        	hasStreamMetadata = true;
        } catch (UnsupportedOperationException e){
        	Assert.assertFalse(hasStreamMetadata);
        }
        
        // //
        //
        // Testing a file which isn't a jp2 one 
        //
        // //
        boolean isValidInput = false;
        final File badfile = File.createTempFile("bad", ".jp2");
        badfile.deleteOnExit();
        final FileImageOutputStream fios = new FileImageOutputStream(badfile);
        fios.writeChars("BAD");
        fios.close();
        
        final ImageReader badFileReader = new JP2KKakaduImageReaderSpi().createReaderInstance();
        try{
        	badFileReader.setInput(badfile);	
        	isValidInput = true;
        } catch (Throwable t){
        	Assert.assertFalse(isValidInput);
        }
    }
    
    @org.junit.Test
	public void manualRead() throws IOException {
        if (!runTests)
            return;
        final File file = TestData.file(this, "CB_TM432.jp2");
        JP2KKakaduImageReader reader = new JP2KKakaduImageReader(
                new JP2KKakaduImageReaderSpi());

        reader.setInput(file);
        RenderedImage image = reader.read(0);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, "testManualRead");
        else
        	Assert.assertNotNull(image.getData());
        Assert.assertEquals(361, image.getWidth());
        Assert.assertEquals(488, image.getHeight());
    }

    public static void displayStatistics(boolean b, RenderedImage source) {
        PlanarImage img = JAI.create("extrema", source, null);
        double[] maximum = (double[]) img.getProperty("maximum");
        double[] minimum = (double[]) img.getProperty("minimum");

        ParameterBlock pb = (new ParameterBlock()).addSource(source);
        pb.add(null).add(1).add(1).add(new int[] { 65536 });
        pb.add(new double[] { minimum[0] }).add(new double[] { maximum[0] });

        PlanarImage dst = JAI.create("histogram", pb);
        Histogram h = (Histogram) dst.getProperty("hiStOgRam");
        JFrame frame = new HistogramFrame(h, b);
        frame.pack();
        frame.show();
    }
    
    @Test
    public void testXMLBoxReading() throws Exception {
        if (!runTests)
            return;

        final File file = TestData.file(this, "bogota_gml.jp2");
        final ImageReader reader = new JP2KKakaduImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        Assert.assertEquals(1,reader.getNumImages(false));
        final JP2KStreamMetadata metadata = (JP2KStreamMetadata) reader.getStreamMetadata();
        final List<IIOMetadataNode> boxes = metadata.searchOccurrencesNode(XMLBox.BOX_TYPE);
        assertTrue(boxes != null);
        assertEquals(1, boxes.size());
        final XMLBoxMetadataNode xmlBox = (XMLBoxMetadataNode) boxes.get(0);
        String xml = xmlBox.getXml();
        assertTrue(xml.startsWith("<gml:FeatureCollection"));
        assertTrue(xml.contains("gml:RectifiedGridCoverage"));
        assertTrue(xml.endsWith("</gml:FeatureCollection>\n"));
    }

}
