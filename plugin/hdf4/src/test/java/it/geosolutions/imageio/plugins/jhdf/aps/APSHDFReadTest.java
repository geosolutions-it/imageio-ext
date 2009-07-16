/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.ndplugin.BaseImageMetadata;
import it.geosolutions.imageio.plugins.jhdf.JHDFTestCase;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;

import org.junit.Assert;
import org.junit.Test;

public class APSHDFReadTest extends JHDFTestCase {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jhdf.aps");

    private void warningMessage() {
        StringBuffer sb = new StringBuffer(
                "Test file not available. Test are skipped");
        LOGGER.info(sb.toString());
    }

    @Test
    public void testRead() throws IOException {
        File file;
        try {
            file = TestData
                    .file(this, "MODPM2008275124021.L3_000_LIGURIAN.hdf4");
        } catch (FileNotFoundException fnfe) {
            warningMessage();
            return;
        }
        ImageReader reader = new HDFAPSImageReaderSpi().createReaderInstance();
        reader.setInput(file);
        final int index = 0;
        if (true || TestData.isInteractiveTest()) {
//            ImageIOUtilities.visualize(reader.read(0), "sst",  true);
            ImageIOUtilities.visualize(reader.read(0), "true_color",  false);
            
        } else
        	Assert.assertNotNull(reader.read(index));

        IIOMetadata metadata = reader.getImageMetadata(index);
        ImageIOUtilities.displayImageIOMetadata(metadata
                .getAsTree(BaseImageMetadata.nativeMetadataFormatName));
        ImageIOUtilities.displayImageIOMetadata(metadata
                .getAsTree(HDFAPSImageMetadata.nativeMetadataFormatName));
        
        IIOMetadata streamMetadata = reader.getStreamMetadata();
        ImageIOUtilities.displayImageIOMetadata(streamMetadata
                .getAsTree(HDFAPSStreamMetadata.nativeMetadataFormatName));
        reader.dispose();
    }
}
