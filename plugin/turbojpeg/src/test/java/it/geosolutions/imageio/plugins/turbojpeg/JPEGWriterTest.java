/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.turbojpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import it.geosolutions.imageio.plugins.exif.EXIFMetadata;
import it.geosolutions.imageio.plugins.exif.EXIFTags;
import it.geosolutions.imageio.plugins.exif.EXIFTags.Type;
import it.geosolutions.imageio.plugins.exif.EXIFUtilities;
import it.geosolutions.imageio.plugins.exif.TIFFTagWrapper;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.resources.TestData;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.operator.BandSelectDescriptor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.libjpegturbo.turbojpeg.TJ;

import com.sun.media.jai.operator.ImageReadDescriptor;

public class JPEGWriterTest extends BaseTest {

    private static final Logger LOGGER = Logger.getLogger(JPEGWriterTest.class.toString());    
    
    @Before
    public void setup() {
        SKIP_TESTS = !TurboJpegUtilities.isTurboJpegAvailable();
    }
    
    static {
        try {
            JAI.getDefaultInstance().getTileCache().setMemoryCapacity(128 * 1024 * 1024);
            if (!(INPUT_FILE.exists() && INPUT_FILE.canRead())) {
                SKIP_TESTS = true;
                LOGGER.warning(ERROR_FILE_MESSAGE);
            } else {
                FileImageInputStream fis = new FileImageInputStream(INPUT_FILE);
                ImageReader reader = ImageIO.getImageReaders(fis).next();
                reader.setInput(fis);
                SAMPLE_IMAGE = ImageReadDescriptor.create(fis, 0, false, false, false, null, null,
                        null, reader, null);
            }

        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.severe(e.getLocalizedMessage());
            }
        }
    }


    public static EXIFMetadata getDefaultInstance() {
        List<TIFFTagWrapper> baselineTiffTags = new ArrayList<TIFFTagWrapper>(2);
        List<TIFFTagWrapper> exifTags = new ArrayList<TIFFTagWrapper>(1);

        // Make sure to set them in proper order
        TIFFTagWrapper copyrightTag = EXIFUtilities.createTag(EXIFTags.COPYRIGHT);
        TIFFTagWrapper exifPointerTag = EXIFUtilities.createTag(EXIFTags.EXIF_IFD_POINTER);

        baselineTiffTags.add(copyrightTag);
        baselineTiffTags.add(exifPointerTag);

        TIFFTagWrapper userCommentTag = EXIFUtilities.createTag(EXIFTags.USER_COMMENT);
        exifTags.add(userCommentTag);

        EXIFMetadata exif = new EXIFMetadata(baselineTiffTags, exifTags);

        return exif;
    }

    /**
     * @return
     */
    protected EXIFMetadata initExif() {
        EXIFMetadata exif = getDefaultInstance();
        exif.setTag(EXIFTags.USER_COMMENT, "Sample User Comment 2".getBytes(), Type.EXIF);
        exif.setTag(EXIFTags.COPYRIGHT, "Copyright 2011 DigitalGlobe".getBytes(), Type.BASELINE);
        return exif;
    }
    

    
    @Test
    @Ignore
    public void testExifReplace() throws IOException {
        EXIFMetadata exif = initExif();
        FileImageInputStreamExt inStream = new FileImageInputStreamExtImpl(new File(
                "/media/bigdisk/data/turbojpeg/lastExif.jpeg"));
        EXIFUtilities.replaceEXIFs(inStream, exif);
    }
    
    @Test
    public void basicWriterTest() throws IOException{
    	if (SKIP_TESTS){
    	    LOGGER.warning(ERROR_LIB_MESSAGE);
            assumeTrue(!SKIP_TESTS);
            return;
    	}
        
    	//test-data
        final File input = TestData.file(this, "testmergb.png");
        assertTrue("Unable to find test data", input.exists() && input.isFile() && input.canRead());

        // get the SPI for writer\
        final Iterator<ImageWriter> it = ImageIO
                .getImageWritersByFormatName(TurboJpegImageWriterSpi.formatNames[0]);
        assertTrue(it.hasNext());
        TurboJpegImageWriter writer = null;
        while (it.hasNext()) {
            ImageWriterSpi writer_ = it.next().getOriginatingProvider();
            if (writer_ instanceof TurboJpegImageWriterSpi) {
                writer = (TurboJpegImageWriter) writer_.createWriterInstance();
                break;
            }
        }
        assertNotNull("Unable to find TurboJpegImageWriter", writer);

        // create output file
        final File output = TestData.temp(this, "output.jpeg", false);
        writer.setOutput(output);
        writer.write(ImageIO.read(input));
        LOGGER.info("Writing output to " + output);
        assertTrue("Unable to create output file", output.exists() && output.isFile());
    }

    @Test
    public void writerTest() throws IOException {
        if (SKIP_TESTS){
            LOGGER.warning(ERROR_LIB_MESSAGE);
            assumeTrue(!SKIP_TESTS);
            return;
        }
        
        // test-data
        final File input = TestData.file(this, "testmergb.png");
        assertTrue("Unable to find test data", input.exists() && input.isFile() && input.canRead());

        // get the SPI for writer\
        final Iterator<ImageWriter> it = ImageIO
                .getImageWritersByFormatName(TurboJpegImageWriterSpi.formatNames[0]);
        assertTrue(it.hasNext());
        TurboJpegImageWriter writer = null;
        while (it.hasNext()) {
            ImageWriterSpi writer_ = it.next().getOriginatingProvider();
            if (writer_ instanceof TurboJpegImageWriterSpi) {
                writer = (TurboJpegImageWriter) writer_.createWriterInstance();
                break;
            }
        }
        assertNotNull("Unable to find TurboJpegImageWriter", writer);

        // create write param
        ImageWriteParam wParam_ = writer.getDefaultWriteParam();
        assertTrue(wParam_ instanceof TurboJpegImageWriteParam);

        TurboJpegImageWriteParam wParam = (TurboJpegImageWriteParam) wParam_;
        wParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        try {
            wParam.setCompressionType("");
            assertTrue("We should not be allowed to set an empty compression type", false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        try {
            wParam.setCompressionType("aaa");
            assertTrue("We should not be allowed to set a generic compression type", false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        wParam.setCompressionType("JPEG");
        wParam.setCompressionQuality(.75f);

        // create output file
        final File output = TestData.temp(this, "output.jpeg", true);
        writer.setOutput(output);
        writer.write(null, new IIOImage(ImageIO.read(input), null, null), wParam);
        assertTrue("Unable to create output file", output.exists() && output.isFile());

    }
    
    @Test
    public void writerTestComponentsSubsampling() throws IOException {
        if (SKIP_TESTS){
            LOGGER.warning(ERROR_LIB_MESSAGE);
            assumeTrue(!SKIP_TESTS);
            return;
        }
        
        final long[] lengths = new long[3];
        final int[] componentSubsampling = new int[]{TJ.SAMP_444,TJ.SAMP_422, TJ.SAMP_420}; 
        // test-data
        final File input = TestData.file(this, "testmergb.png");
        assertTrue("Unable to find test data", input.exists() && input.isFile() && input.canRead());

        
        // get the SPI for writer\
        final Iterator<ImageWriter> it = ImageIO
                .getImageWritersByFormatName(TurboJpegImageWriterSpi.formatNames[0]);
        assertTrue(it.hasNext());
        TurboJpegImageWriter writer = null;
        while (it.hasNext()) {
            ImageWriterSpi writer_ = it.next().getOriginatingProvider();
            if (writer_ instanceof TurboJpegImageWriterSpi) {
                writer = (TurboJpegImageWriter) writer_.createWriterInstance();
                break;
            }
        }
        assertNotNull("Unable to find TurboJpegImageWriter", writer);

        IIOImage image = new IIOImage(ImageIO.read(input), null, null);
        
        for (int i = 0; i < 3; i++) {
            // create write param
            ImageWriteParam wParam_ = writer.getDefaultWriteParam();
            assertTrue(wParam_ instanceof TurboJpegImageWriteParam);
    
            TurboJpegImageWriteParam wParam = (TurboJpegImageWriteParam) wParam_;
            wParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            wParam.setCompressionType("JPEG");
            wParam.setCompressionQuality(.75f);
            wParam.setComponentSubsampling(componentSubsampling[i]);
    
            // create output file
            final File output = TestData.temp(this, "output.jpeg", false);
            LOGGER.info("output file is " + output);
            writer.setOutput(output);
            writer.write(null, image, wParam);
            writer.dispose();
            
            assertTrue("Unable to create output file", output.exists() && output.isFile());
            lengths[i] = output.length();
//            output.delete();
        }
        
        assertEquals(lengths[0], 11604l);
        assertEquals(lengths[1], 9376l);
        assertEquals(lengths[2], 8209l);
    }

    @Test
    public void writerTestRefineLayout() throws IOException {
        if (SKIP_TESTS){
            LOGGER.warning(ERROR_LIB_MESSAGE);
            assumeTrue(!SKIP_TESTS);
            return;
        }
        
        // test-data
        final File input = TestData.file(this, "testme.jpg");
        FileImageInputStream stream = null;
        ImageReader reader = null;
        try {
            
            stream = new FileImageInputStream(input);
            ImageLayout layout = new ImageLayout();
            layout.setTileGridXOffset(-2);
            layout.setTileGridYOffset(-2);
            layout.setTileWidth(228);
            layout.setTileHeight(104);
            reader = ImageIO.getImageReaders(stream).next();
            RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            BufferedImage sourceImage = ImageIO.read(input);
            sourceImage.getWidth();
            RenderedImage inputImage = BandSelectDescriptor.create(sourceImage, new int[]{0}, hints); 

            // get the SPI for writer\
            final Iterator<ImageWriter> it = ImageIO
                    .getImageWritersByFormatName(TurboJpegImageWriterSpi.formatNames[0]);
            assertTrue(it.hasNext());
            TurboJpegImageWriter writer = null;
            while (it.hasNext()) {
                ImageWriterSpi writer_ = it.next().getOriginatingProvider();
                if (writer_ instanceof TurboJpegImageWriterSpi) {
                    writer = (TurboJpegImageWriter) writer_.createWriterInstance();
                    break;
                }
            }
            assertNotNull("Unable to find TurboJpegImageWriter", writer);
    
            IIOImage image = new IIOImage(inputImage, null, null);

            // create write param
            ImageWriteParam wParam_ = writer.getDefaultWriteParam();
            assertTrue(wParam_ instanceof TurboJpegImageWriteParam);
            TurboJpegImageWriteParam wParam = (TurboJpegImageWriteParam) wParam_;
            wParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            wParam.setCompressionType("JPEG");
            wParam.setCompressionQuality(.75f);

            // create output file
            final File output = TestData.temp(this, "output.jpeg", false);
            LOGGER.info("output file is " + output);
            writer.setOutput(output);
            writer.write(null, image, wParam);
            writer.dispose();

            assertTrue("Unable to create output file", output.exists() && output.isFile());
        
        } catch (Throwable t) {
            
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) { 
                }
            }
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (Throwable t) { 
                }
            }
        }
    }
    
}
