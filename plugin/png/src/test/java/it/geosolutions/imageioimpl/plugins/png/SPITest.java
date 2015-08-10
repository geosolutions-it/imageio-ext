/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2015, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.png;

import static org.junit.Assert.assertTrue;
import it.geosolutions.imageio.plugins.png.PNGImageWriterSPI;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.junit.Test;

public class SPITest {

    @Test
    public void testSPI() throws Exception {
        boolean found = false;
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("PNG");
        while(it.hasNext()){
            ImageWriter wr=it.next();
            if (wr.getOriginatingProvider() instanceof PNGImageWriterSPI) {
                found = true;
                break;
            } 

            assertTrue("Unable to find PNGImageWriterSPI", found);
        }
    }

}
