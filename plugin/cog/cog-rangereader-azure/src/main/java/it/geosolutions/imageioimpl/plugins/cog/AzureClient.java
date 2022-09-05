/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Andrea Aime, GeoSolutions, Copyright 2019
 *
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

import com.microsoft.azure.storage.blob.AnonymousCredentials;
import com.microsoft.azure.storage.blob.BlobRange;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.ICredentials;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.models.BlobFlatListSegment;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;
import com.microsoft.rest.v2.RestException;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.NettyClient;
import com.microsoft.rest.v2.http.SharedChannelPoolOptions;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.netty.bootstrap.Bootstrap;
import io.reactivex.Single;

import java.io.Closeable;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * AzureClient class has been adapted from GWC Azure blob module.
 */
class AzureClient implements Closeable {

    private static final int HTTP_CODE_NOT_FOUND = 404;
    private static final int HTTP_CODE_CONFLICT = 409;
    private static boolean is2xxSuccessfulStatus(int status) {
            return status / 100 == 2;
    }

    public static final String AZURE_URL_BASE = "blob.core.windows.net";

    private final NettyClient.Factory factory;
    private AzureConfigurationProperties configuration;
    private final ContainerURL container;

    public AzureClient(AzureConfigurationProperties configuration){
        this.configuration = configuration;
        try {
            ICredentials creds;
            if (configuration.getAccountKey() != null && configuration.getAccountName() != null) {
                creds =
                        new SharedKeyCredentials(
                                configuration.getAccountName(), configuration.getAccountKey());
            } else {
                creds = new AnonymousCredentials();
            }

            // setup the HTTPClient, keep the factory on the side to close it down on destroy
            factory =
                    new NettyClient.Factory(
                            new Bootstrap(),
                            0,
                            new SharedChannelPoolOptions()
                                    .withPoolSize(configuration.getMaxConnections()),
                            null);
            final HttpClient client = factory.create(null);

            // build the container access
            PipelineOptions options = new PipelineOptions().withClient(client);
            URL url = new URL(getServiceURL(configuration));
            ServiceURL serviceURL =
                    new ServiceURL(url,StorageURL.createPipeline(creds, options));
            String containerName = configuration.getContainer();
            this.container = serviceURL.createContainerURL(containerName);

            if (creds instanceof AnonymousCredentials) {
                // Check if we can retrieve blobs
                try {
                    ContainerListBlobFlatSegmentResponse response =
                            this.container.listBlobsFlatSegment(null, new ListBlobsOptions()
                                            .withPrefix(configuration.getPrefix())
                                            .withMaxResults(1)).blockingGet();
                    if (response != null) {
                        BlobFlatListSegment segment = response.body().segment();
                        if (segment.blobItems().isEmpty()) {
                            throw new RuntimeException("Container is empty");
                        }
                    }

                }catch (RestException e) {
                    if (e.response().statusCode() == HTTP_CODE_NOT_FOUND) {
                        throw new RuntimeException("Failed to access the Container", e);
                    }
                }

            } else {
                // no way to see if the containerURL already exists, try to create and see if
                // we get a 409 CONFLICT
                try {

                    int status = this.container.create(null, null, null).blockingGet().statusCode();
                    if (!is2xxSuccessfulStatus(status)
                            && status != HTTP_CODE_CONFLICT) {
                        throw new RuntimeException(
                                "Failed to create container "
                                        + containerName
                                        + ", REST API returned a "
                                        + status);
                    }
                } catch (RestException e) {
                    if (e.response().statusCode() != HTTP_CODE_CONFLICT) {
                        throw new RuntimeException("Failed to create container", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup Azure connection and container", e);
        }
    }

    public String getServiceURL(AzureConfigurationProperties configuration) {
        String serviceURL = configuration.getServiceURL();
        if (serviceURL == null) {
            // default to account name based location
            serviceURL =
                    (configuration.isUseHTTPS() ? "https" : "http")
                            + "://"
                            + configuration.getAccountName()
                            + ".blob.core.windows.net";
        }
        return serviceURL;
    }

    public byte[] getBytes(String key, BlobRange range) {
        BlockBlobURL blob = container.createBlockBlobURL(key);
        try {
            Single<DownloadResponse> download = blob.download(range, null, false, null);
            DownloadResponse response = download.blockingGet();

            ByteBuffer buffer =
                    FlowableUtil.collectBytesInBuffer(response.body(null)).blockingGet();
            byte[] result = new byte[buffer.remaining()];
            buffer.get(result);
            return result;
        } catch (RestException e) {
            if (e.response().statusCode() == HTTP_CODE_NOT_FOUND) {
                return null;
            }
            throw new RuntimeException("Failed to retrieve bytes for " + key, e);
        }
    }

    @Override
    public void close() {
        factory.close();
    }

    public ContainerURL getContainer() {
        return container;
    }
}
