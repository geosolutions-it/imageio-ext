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

import it.geosolutions.imageio.plugins.exif.EXIFMetadata;
import it.geosolutions.imageio.plugins.exif.EXIFTags;
import it.geosolutions.imageio.plugins.exif.EXIFTags.Type;
import it.geosolutions.imageio.plugins.exif.EXIFUtilities;
import it.geosolutions.imageio.plugins.exif.TIFFTagWrapper;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.resources.TestData;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi;

public class JPEGWriterTest extends Assert {

    static final String INPUT_FILE_PATH = "/tmp/test.tif";

    static final File INPUT_FILE = new File(INPUT_FILE_PATH);

    static RenderedImage SAMPLE_IMAGE = null;

    static boolean SKIP_TESTS = false;
    
    static final Logger LOGGER = Logger.getLogger(JPEGWriterTest.class.toString());

    static final String ERROR_LIB_MESSAGE = "The TurboJpeg native library hasn't been loaded: Skipping test";

    static final String ERROR_FILE_MESSAGE = "The specified input file can't be read: Skipping test";
    
    @Before
    public void setup() {
        SKIP_TESTS = !TurboJpegUtilities.isTurboJpegAvailable();
    }
    
//    static {
//        try {
//            JAI.getDefaultInstance().getTileCache().setMemoryCapacity(128 * 1024 * 1024);
//            if (!(INPUT_FILE.exists() && INPUT_FILE.canRead())) {
//                SKIP_TESTS = true;
//                LOGGER.warning(ERROR_FILE_MESSAGE);
//            } else {
//                FileImageInputStream fis = new FileImageInputStream(INPUT_FILE);
//                ImageReader reader = ImageIO.getImageReaders(fis).next();
//                reader.setInput(fis);
//                SAMPLE_IMAGE = ImageReadDescriptor.create(fis, 0, false, false, false, null, null,
//                        null, reader, null);
//            }
//
//        } catch (IOException e) {
//            if (LOGGER.isLoggable(Level.SEVERE)) {
//                LOGGER.severe(e.getLocalizedMessage());
//            }
//        }
//    }

    static final String OUTPUT_FOLDER = "/home/dromagno/tmp2" // /media/bigdisk/data/turbojpeg"// System.getProperty("java.io.tmpdir")
            + File.separatorChar;

    static final CLibJPEGImageWriterSpi clibSPI = new CLibJPEGImageWriterSpi();

    static final JPEGImageWriterSpi standardSPI = new JPEGImageWriterSpi();

    static final TurboJpegImageWriterSpi turboSPI = new TurboJpegImageWriterSpi();

    /**
     * @param total
     */
    static void reportTime(String encoder, long total, final int LOOP) {
        LOGGER.info("JPEG " + encoder + " TOTAL TIME = " + ((total) / 1000000)
                + "(ms) ; AVERAGE TIME = " + (total / (1000 * LOOP)) + "(micros)");

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
        final File output = TestData.temp(this, "output.jpeg", true);
        writer.setOutput(output);
        writer.write(ImageIO.read(input));
        assertTrue("Unable to create output file", output.exists() && output.isFile());

    }

    @Test
    public void writerTest() throws IOException {
        if (SKIP_TESTS){
            LOGGER.warning(ERROR_LIB_MESSAGE);
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
            return;
        }
        
        final long[] lengths = new long[3];
        final int[] componentSubsampling = new int[]{TurboJpegLibrary.TJ_444,TurboJpegLibrary.TJ_422, TurboJpegLibrary.TJ_420}; 
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
            final File output = TestData.temp(this, "output.jpeg", true);
            writer.setOutput(output);
            writer.write(null, image, wParam);
            writer.dispose();
            
            assertTrue("Unable to create output file", output.exists() && output.isFile());
            lengths[i] = output.length();
            output.delete();
        }
        
        assertEquals(lengths[0], 11604);
        assertEquals(lengths[1], 9376);
        assertEquals(lengths[2], 8209);
    }

}
