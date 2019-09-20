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
        return new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(container)
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
