/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.cog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.geosolutions.imageio.core.BasicAuthURI;

public class AzureConfigurationPropertiesTest {

    @Test
    public void testPartsFromUrl() {
        String azureUrl = "https://fakeaccount.blob.core.windows.net/cogtestdata/testvirtualfolder/land_topo_cog_jpeg_1024.tif";

        AzureConfigurationProperties props = new AzureConfigurationProperties(new BasicAuthURI(azureUrl));
        
        assertTrue(props.isUseHTTPS());
        assertEquals("fakeaccount", props.getAccountName());
        assertEquals("cogtestdata", props.getContainer());
        assertEquals("testvirtualfolder", props.getPrefix());
        assertNull(props.getServiceURL());
        assertEquals(Integer.valueOf(64), props.getMaxConnections());
    }

    @Test
    public void testAccountKeyFromBaicAuthURI() {
        String azureUrl = "https://fakeaccount.blob.core.windows.net/cogtestdata/testvirtualfolder/land_topo_cog_jpeg_1024.tif";
        BasicAuthURI uri = new BasicAuthURI(azureUrl);
        uri.setUser("testAccountName");
        uri.setPassword("testAccountKey");
        AzureConfigurationProperties props = new AzureConfigurationProperties(uri);
        
        assertTrue(props.isUseHTTPS());
        assertEquals("testAccountName", props.getAccountName());
        assertEquals("testAccountKey", props.getAccountKey());
    }
}
