/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2021, GeoSolutions
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
package it.geosolutions.imageio.cog;

import com.google.cloud.storage.BlobId;
import it.geosolutions.imageioimpl.plugins.cog.GSRangeReader;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class BlobIdParsingTest {

    private static String GS_UTIL_COG_URI = "gs://gcp-public-data-landsat/LC08/01/044/034" +
            "/LC08_L1GT_044034_20130330_20170310_01_T2" +
            "/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF";

    private static String PUBLIC_COG_URI = "https://storage.googleapis.com/gcp-public-data-landsat/LC08/01/044/034/LC08_L1GT_044034_20130330_20170310_01_T2/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF";
    
    private static String AUTHENTICATED_COG_URI = "https://storage.cloud.google.com/gcp-public-data-landsat/LC08/01/044/034/LC08_L1GT_044034_20130330_20170310_01_T2/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF";
    
    @Test
    public void testGsUtilCOGURI() throws Exception {
        BlobId blobId = GSRangeReader.getBlobId(new URI(GS_UTIL_COG_URI));
        assertBlobId(blobId);
    }

    @Test
    public void testPublicCOGURI() throws Exception {
        BlobId blobId = GSRangeReader.getBlobId(new URI(PUBLIC_COG_URI));
        assertBlobId(blobId);
    }

    @Test
    public void testAuthenticatedCOGURI() throws Exception {
        BlobId blobId = GSRangeReader.getBlobId(new URI(AUTHENTICATED_COG_URI));
        assertBlobId(blobId);
    }

    private void assertBlobId(BlobId blobId) {
        assertEquals("gcp-public-data-landsat", blobId.getBucket());
        assertEquals("LC08/01/044/034/LC08_L1GT_044034_20130330_20170310_01_T2/LC08_L1GT_044034_20130330_20170310_01_T2_B11.TIF", blobId.getName());
    }


}
