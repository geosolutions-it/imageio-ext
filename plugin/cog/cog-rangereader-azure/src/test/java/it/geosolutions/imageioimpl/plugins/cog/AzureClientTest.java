/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2026, GeoSolutions
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import it.geosolutions.imageio.core.BasicAuthURI;
import org.junit.Test;

public class AzureClientTest {

    private static final String AZURE_URL =
            "https://fakeaccount.blob.core.windows.net/cogtestdata/testvirtualfolder/test.tif";

    @Test
    public void testSasCredentialConfiguresSasPolicy() {
        AzureConfigurationProperties configuration =
                configurationWithCredential("sp=r&sv=2023-11-03&sr=c&sig=fake%2Bsignature%3D");

        BlobServiceClient client = buildClient(configuration);

        assertTrue(hasPolicy(client, AzureSasCredentialPolicy.class));
        assertFalse(hasPolicy(client, StorageSharedKeyCredentialPolicy.class));
    }

    @Test
    public void testAccountKeyConfiguresSharedKeyPolicy() {
        AzureConfigurationProperties configuration = configurationWithCredential("fakeAccountKey");

        BlobServiceClient client = buildClient(configuration);

        assertTrue(hasPolicy(client, StorageSharedKeyCredentialPolicy.class));
        assertFalse(hasPolicy(client, AzureSasCredentialPolicy.class));
    }

    private AzureConfigurationProperties configurationWithCredential(String credential) {
        BasicAuthURI uri = new BasicAuthURI(AZURE_URL);
        uri.setUser("fakeaccount");
        uri.setPassword(credential);
        return new AzureConfigurationProperties(uri);
    }

    private BlobServiceClient buildClient(AzureConfigurationProperties configuration) {
        BlobServiceClientBuilder builder =
                new BlobServiceClientBuilder().endpoint("https://fakeaccount.blob.core.windows.net");
        return AzureClient.configureCredentials(builder, configuration).buildClient();
    }

    private boolean hasPolicy(BlobServiceClient client, Class<? extends HttpPipelinePolicy> policyClass) {
        for (int i = 0; i < client.getHttpPipeline().getPolicyCount(); i++) {
            if (policyClass.isInstance(client.getHttpPipeline().getPolicy(i))) {
                return true;
            }
        }
        return false;
    }
}
