/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;

import java.util.logging.Logger;

/**
 * @author joshfix
 * Created on 1/18/18
 */
public class AzureConnector {

    private String connectionString;
    private static final Logger LOGGER = Logger.getLogger(AzureConnector.class.getName());

    /**
     * Attempts to discover a connection string in system properties and environment variables based on the account
     * name defined in the WASB URL.
     *
     * @param accountName
     */
    public AzureConnector(String accountName) {
        connectionString = ConnectionDirectory.getConnectionString(accountName);
    }

    public BlobAsyncClient getAzureClient(String container, String filename) {
        // Retrieve storage account from connection-string.
        BlobClientBuilder blobClientBuilder = new BlobClientBuilder();
        if (connectionString != null && !connectionString.isEmpty()) {
            blobClientBuilder.connectionString(connectionString);
        }
        return blobClientBuilder.containerName(container)
                .blobName(filename)
                .buildBlobAsyncClient();
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getConnectionString() {
        return connectionString;
    }

}
