/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.matfile5.sas;

import it.geosolutions.imageio.matfile5.sas.SASTileImageReaderSpi;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.media.jai.JAI;

public class ReadTest {
    public static void main(String[] args) throws FileNotFoundException,
            IOException {

        long startTime = System.currentTimeMillis();
        final int nLoop = 3;
        System.gc();
        JAI.getDefaultInstance().getTileCache().setMemoryCapacity(512*1024*1024);
        for (int i=0;i<nLoop;i++){
    //         final String fileName = "E:/Work/data/baralli/copia.mat";
            final String fileName = "h:\\mat\\utest.mat";
            
            ImageReader reader = new SASTileImageReaderSpi().createReaderInstance();
            reader.setInput(new File(fileName));
            reader.getWidth(0);
            BufferedImage bi = reader.read(0);
            reader.dispose();
            reader=null;
            System.out.println("loop: " +i);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("TIME: "+ (endTime-startTime)/nLoop);
        
        
        
    }

}
