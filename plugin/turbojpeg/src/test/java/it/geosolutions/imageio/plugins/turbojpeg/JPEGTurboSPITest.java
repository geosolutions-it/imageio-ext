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

import java.util.Iterator;

import javax.imageio.ImageIO;
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
		if (TurboJpegUtilities.isTurboJpegAvailable()) {
		    assertTrue("Unable to find TurboJpegImageWriterSpi",false);   
        
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
    }
   
}
