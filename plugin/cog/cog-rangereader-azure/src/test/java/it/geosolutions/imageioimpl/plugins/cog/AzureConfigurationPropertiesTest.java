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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import it.geosolutions.imageio.core.BasicAuthURI;
import org.junit.Test;

public class AzureConfigurationPropertiesTest {

    private static final String AZURE_URL =
            "https://fakeaccount.blob.core.windows.net/cogtestdata/testvirtualfolder/land_topo_cog_jpeg_1024.tif";

    @Test
    public void testPartsFromUrl() {
        AzureConfigurationProperties props = new AzureConfigurationProperties(new BasicAuthURI(AZURE_URL));

        assertTrue(props.isUseHTTPS());
        assertEquals("fakeaccount", props.getAccountName());
        assertEquals("cogtestdata", props.getContainer());
        assertEquals("testvirtualfolder", props.getPrefix());
        assertNull(props.getServiceURL());
        assertEquals(Integer.valueOf(64), props.getMaxConnections());
    }

    @Test
    public void testAccountKeyFromBasicAuthURI() {
        BasicAuthURI uri = new BasicAuthURI(AZURE_URL);
        uri.setUser("testAccountName");
        uri.setPassword("testAccountKey");
        AzureConfigurationProperties props = new AzureConfigurationProperties(uri);

        assertTrue(props.isUseHTTPS());
        assertEquals("testAccountName", props.getAccountName());
        assertEquals("testAccountKey", props.getAccountKey());
        assertNull(props.getSasToken());
    }

    @Test
    public void testSasTokenFromBasicAuthURI() {
        String sasToken = "sp=r&sv=2023-11-03&sr=c&sig=fake%2Bsignature%3D";
        BasicAuthURI uri = new BasicAuthURI(AZURE_URL);
        uri.setUser("testAccountName");
        uri.setPassword(sasToken);

        AzureConfigurationProperties props = new AzureConfigurationProperties(uri);

        assertEquals("testAccountName", props.getAccountName());
        assertEquals(sasToken, props.getSasToken());
        assertNull(props.getAccountKey());
    }

    @Test
    public void testSasTokenLeadingQuestionMarkIsRemoved() {
        String sasToken = "sp=r&sv=2023-11-03&sr=c&sig=fake%2Bsignature%3D";
        BasicAuthURI uri = new BasicAuthURI(AZURE_URL);
        uri.setUser("testAccountName");
        uri.setPassword("?" + sasToken);

        AzureConfigurationProperties props = new AzureConfigurationProperties(uri);

        assertEquals(sasToken, props.getSasToken());
        assertNull(props.getAccountKey());
    }

    @Test
    public void testSasTokenFromSystemProperty() {
        String propertyName = "azure.reader.sasToken";
        String previousValue = System.getProperty(propertyName);
        String sasToken = "sp=r&sv=2023-11-03&sr=c&sig=fake%2Bsignature%3D";
        try {
            System.setProperty(propertyName, sasToken);

            AzureConfigurationProperties props = new AzureConfigurationProperties(new BasicAuthURI(AZURE_URL));

            assertEquals(sasToken, props.getSasToken());
            assertNull(props.getAccountKey());
        } finally {
            if (previousValue == null) {
                System.clearProperty(propertyName);
            } else {
                System.setProperty(propertyName, previousValue);
            }
        }
    }

    @Test
    public void testContainerSasCannotReadContainerProperties() {
        AzureConfigurationProperties props =
                configurationWithSasToken("sp=r&sv=2023-11-03&sr=c&sig=fake%2Bsignature%3D");

        assertFalse(props.canReadContainerProperties());
    }

    @Test
    public void testAccountSasCanReadContainerProperties() {
        AzureConfigurationProperties props =
                configurationWithSasToken("ss=b&srt=sco&sp=r&sv=2023-11-03&sig=fake%2Bsignature%3D");

        assertTrue(props.canReadContainerProperties());
    }

    @Test
    public void testAccountSasWithoutContainerAccessCannotReadContainerProperties() {
        AzureConfigurationProperties props =
                configurationWithSasToken("ss=b&srt=o&sp=r&sv=2023-11-03&sig=fake%2Bsignature%3D");

        assertFalse(props.canReadContainerProperties());
    }

    private AzureConfigurationProperties configurationWithSasToken(String sasToken) {
        BasicAuthURI uri = new BasicAuthURI(AZURE_URL);
        uri.setUser("testAccountName");
        uri.setPassword(sasToken);
        return new AzureConfigurationProperties(uri);
    }
}
