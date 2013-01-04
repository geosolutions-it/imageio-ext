/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import org.junit.Test;

/**
 * @author Simone Giannecchini, GeoSolutions SAS
 * @author Emanuele Tajariol, GeoSolutions SAS
 */
public class JPEGTurboSPITest {

	
    @Test
    public void testWriterSPI() {
    	final Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(TurboJpegImageWriterSpi.formatNames[0]);                

        // at least one writer should exist        
        assertTrue(it.hasNext());
        
        // if turbojpeg has not been loaded, ignore further test clause
        assumeTrue(TurboJpegUtilities.isTurboJpegAvailable());
        
        boolean existTurbo = false;
        boolean existAnother = false;
        
        while (it.hasNext()) {
            if (it.next().getOriginatingProvider() instanceof TurboJpegImageWriterSpi) {
                existTurbo = true;
            } else {
                existAnother = true;
            }
        }

        assertTrue("Unable to find TurboJpegImageWriterSpi", existTurbo);
        assertTrue("Unable to find another jpeg ImageWriter", existAnother);
    }
    
    @Test
    public void testReaderSPI() {                
        final Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(TurboJpegImageReaderSpi.names[0]);
        
        // at least one reader should exist
        assertTrue("Unable to find any jpeg ImageReader", it.hasNext());
        
        // if turbojpeg has not been loaded, ignore further test clause
        assumeTrue(TurboJpegUtilities.isTurboJpegAvailable());
        
        boolean existTurbo = false;
        boolean existAnother = false;
        
        while (it.hasNext()) {
            if (it.next().getOriginatingProvider() instanceof TurboJpegImageReaderSpi) {
                existTurbo = true;
            } else {
                existAnother = true;
            }
        }

        assertTrue("Unable to find TurboJpegImageReaderSpi", existTurbo);
        assertTrue("Unable to find another jpeg ImageReader", existAnother);
        
        
        // Need to investigate on the CLIB Jpeg reader SPIs which seem are registered as first
//        // first one should be the turbojpeg
//        assertTrue("First reader SPI is not the turbo one", it.next().getOriginatingProvider() instanceof TurboJpegImageReaderSpi);
//        
//        // at least another reader should exist
//        assertTrue("Unable to find another jpeg ImageReader", it.hasNext());        
    }
}
