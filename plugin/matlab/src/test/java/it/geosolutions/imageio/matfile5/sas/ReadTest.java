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

import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import org.junit.Test;

public class ReadTest {

    private static final Logger LOGGER = Logger.getLogger(ReadTest.class.toString());

//    private String REAL_FILES[] = new String[]{
//            "/home/geosolutions/work/16-10-2009/input/20091015/MUSCLE_CAT2_20091015_2/Leg00001/stbd/MUSCLE_CAT2_091015_2_1_p_2_253_30_90.mat",
//            "/home/geosolutions/work/16-10-2009/input/20091015/MUSCLE_CAT2_20091015_2/Leg00001/stbd/MUSCLE_CAT2_091015_2_1_s_2_253_30_90.mat"
//    };
    
    private String REAL_FILES[] = null;
    
    @Test
    public void testSASData() throws IOException {

        final boolean useRealData;
    	if (REAL_FILES == null){
    	    useRealData = false;
    	    REAL_FILES = new String[]{"sas_stbdsample.mat","sas_portsample.mat"};
    	}
    	else
    	    useRealData = true;
    	for (int i=0;i<REAL_FILES.length;i++){
	        File file;
	        try {
	            if (useRealData)
	                file = new File(REAL_FILES[i]);
	            else
	                file = TestData.file(new ReadTest(), REAL_FILES[i]);
	        } catch (IOException e) {
	            LOGGER.info("Unable to run test due to " + e.getLocalizedMessage());
	            return;
	        }
	        ImageReader reader = new SASTileImageReaderSpi().createReaderInstance();
	        reader.setInput(file);
	        final ImageReadParam param = new ImageReadParam();
	        param.setSourceSubsampling(8,16,0,0);
	        param.setSourceRegion(new Rectangle(20,20,50,70));
	        final BufferedImage bi = reader.read(0, param);
	        if (TestData.isInteractiveTest())
	            ImageIOUtilities.visualize(bi, file.getName(), true);
	        reader.dispose();
	        reader = null;
    	}
    }
}
