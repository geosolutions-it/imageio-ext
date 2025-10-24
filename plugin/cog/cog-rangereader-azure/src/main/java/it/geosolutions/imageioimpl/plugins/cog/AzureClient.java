/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Andrea Aime, GeoSolutions, Copyright 2019
 *     <p>ImageI/O-Ext - OpenSource Java Image translation Library http://www.geo-solutions.it/
 *     https://github.com/geosolutions-it/imageio-ext (C) 2022, GeoSolutions
 *     <p>This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *     General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your
 *     option) any later version.
 *     <p>This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *     implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 *     License for more details.
 */
package it.geosolutions.imageioimpl.plugins.cog;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import java.time.Duration;

/**
 * AzureClient class has been adapted from GWC Azure blob module, but it won't try to create a missing container since
 * it's read-only
 */
class AzureClient {

    private static final int HTTP_CODE_NOT_FOUND = 404;
    public static final String AZURE_URL_BASE = "blob.core.windows.net";

    private AzureConfigurationProperties configuration;
    private final BlobContainerClient container;

    public AzureClient(AzureConfigurationProperties configuration) {
        this.configuration = configuration;
        BlobServiceClient serviceClient = createBlobServiceClient();
        String containerName = configuration.getContainer();
        this.container = getContainer(serviceClient, containerName);
    }

    BlobContainerClient getContainer(BlobServiceClient serviceClient, String containerName) {
        BlobContainerClient containerClient;
        try {
            containerClient = serviceClient.getBlobContainerClient(containerName);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Failed to setup Azure connection and container", e);
        }
        if (!containerClient.exists()) {
            throw new IllegalArgumentException("container " + containerName + " does not exist");
        }
        return containerClient;
    }

    BlobServiceClient createBlobServiceClient() {
        String serviceURL = getServiceURL(configuration);
        AzureNamedKeyCredential creds = getCredentials(configuration);
        ClientOptions clientOpts = new ClientOptions();
        HttpClient httpClient = createHttpClient(configuration);

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
                .endpoint(serviceURL)
                .clientOptions(clientOpts)
                .httpClient(httpClient);
        if (null != creds) {
            builder = builder.credential(creds);
        }
        return builder.buildClient();
    }

    AzureNamedKeyCredential getCredentials(AzureConfigurationProperties configuration) {
        String accountName = configuration.getAccountName();
        String accountKey = configuration.getAccountKey();
        if (null != accountName && null != accountKey) {
            return new AzureNamedKeyCredential(accountName, accountKey);
        }
        return null;
    }

    HttpClient createHttpClient(AzureConfigurationProperties blobStoreConfig) {

        Integer maxConnections = blobStoreConfig.getMaxConnections();

        HttpClientOptions opts = new HttpClientOptions();
        opts.setMaximumConnectionPoolSize(maxConnections);
        return HttpClient.createDefault(opts);
    }

    String getServiceURL(AzureConfigurationProperties configuration) {
        String serviceURL = configuration.getServiceURL();
        if (serviceURL == null) {
            // default to account name based location
            String proto = configuration.isUseHTTPS() ? "https" : "http";
            String account = configuration.getAccountName();
            serviceURL = String.format("%s://%s.blob.core.windows.net", proto, account);
        }
        return serviceURL;
    }

    /**
     * @return the blob's download response, or {@code null} if not found
     * @throws BlobStorageException
     */
    private BlobDownloadContentResponse download(String key, BlobRange range) {
        BlobClient blobClient = container.getBlobClient(key);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(0);
        BlobRequestConditions conditions = null;
        Duration timeout = null;
        Context context = Context.NONE;
        try {
            return blobClient.downloadContentWithResponse(options, conditions, range, false, timeout, context);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == HTTP_CODE_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public byte[] getBytes(String key, BlobRange range) {
        BlobDownloadContentResponse download = download(key, range);
        return download == null ? null : download.getValue().toBytes();
    }
}
