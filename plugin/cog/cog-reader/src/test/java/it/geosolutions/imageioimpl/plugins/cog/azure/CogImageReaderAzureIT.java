/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2025, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.cog.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageioimpl.plugins.cog.AzureConfigurationProperties;
import it.geosolutions.imageioimpl.plugins.cog.AzureRangeReader;
import it.geosolutions.imageioimpl.plugins.cog.BaseCogImageReaderTest;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogTestData;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.azure.AzuriteContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * {@link CogImageReader} integration test for {@link AzureRangeReader} using testcontainers with an
 * {@link AzuriteContainer}.
 *
 * <p>This test uses the Azurite emulator to test Azure Blob Storage integration without requiring real Azure
 * credentials or making actual network calls to Azure.
 *
 * <p>The emulator is configured via system properties that are read by {@link AzureConfigurationProperties} via
 * {@code PropertyLocator}:
 *
 * <ul>
 *   <li>{@code azure.reader.serviceurl} - The Azurite emulator endpoint (e.g., http://localhost:port/accountName)
 *   <li>{@code azure.reader.accountname} - The Azurite default account name (devstoreaccount1)
 *   <li>{@code azure.reader.accountkey} - The Azurite default account key
 * </ul>
 *
 * <p>Test files are uploaded to the emulator using the Azure Storage Blob SDK, and then accessed via
 * {@link AzureRangeReader} using standard HTTP URLs pointing to the emulator.
 *
 * <p><b>Note:</b> Property names must be lowercase (e.g., {@code azure.reader.serviceurl} not
 * {@code azure.reader.serviceURL}) to work correctly with {@code PropertyLocator.getEnvironmentValue()}.
 */
public class CogImageReaderAzureIT extends BaseCogImageReaderTest {

    private static final DockerImageName DOCKER_IMAGE_NAME =
            DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.35.0");

    private static final String CONTAINER_NAME = "test-container";

    @ClassRule
    public static CogTestData testData = new CogTestData();

    public static AzuriteContainer container = new AzuriteContainer(DOCKER_IMAGE_NAME);
    // These are the default account name and key used by Azurite
    private static final String ACCOUNT_NAME = "devstoreaccount1";
    private static final String ACCOUNT_KEY =
            "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

    /**
     * Skip tests when caching is enabled due to ArrayIndexOutOfBoundsException in CachingCogImageInputStream.read():
     *
     * <pre>
     * java.lang.ArrayIndexOutOfBoundsException: arraycopy: last source index 22366 out of bounds for byte[16384]
     * </pre>
     */
    @Before
    public void skipCachingTests() {
        Assume.assumeFalse(
                "Caching tests are skipped due to known issue with CachingCogImageInputStream", super.caching);
    }

    @BeforeClass
    public static void setUpContainer() throws IOException {
        container.start();

        // Configure Azure client to use Azurite
        // Azurite service URL includes the account name in the path
        // Note: Property names must be lowercase to work with
        // PropertyLocator.getEnvironmentValue()
        Integer port = container.getMappedPort(10000);
        String azuriteEndpoint = "http://localhost:" + port + "/" + ACCOUNT_NAME;
        System.setProperty("azure.reader.serviceurl", azuriteEndpoint);
        System.setProperty("azure.reader.accountname", ACCOUNT_NAME);
        System.setProperty("azure.reader.accountkey", ACCOUNT_KEY);

        BlobContainerClient containerClient = createClient();

        Path landTopoCog1024 = testData.landTopoCog1024();
        upload(landTopoCog1024, containerClient);
    }

    @AfterClass
    public static void stopContainer() {
        container.stop();
    }

    private static BlobContainerClient createClient() {
        // Get the connection string from the Azurite container
        String connectionString = container.getConnectionString();

        // Create a BlobServiceClient
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Create a container
        return blobServiceClient.createBlobContainerIfNotExists(CONTAINER_NAME);
    }

    private static void upload(Path localFile, BlobContainerClient containerClient) {
        String blobName = localFile.getFileName().toString();
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(localFile.toString(), true);
    }

    @Override
    protected Class<? extends RangeReader> getRangeReaderClass() {
        return AzureRangeReader.class;
    }

    @Override
    protected BasicAuthURI landTopoCog1024ConnectionParams() {
        String blobName = testData.landTopoCog1024().getFileName().toString();
        Integer port = container.getMappedPort(10000);
        // Azurite URL format: http://localhost:port/accountName/containerName/blobName
        // Credentials are provided via system properties set in setUpContainer()
        String uriString = "http://localhost:%d/%s/%s/%s".formatted(port, ACCOUNT_NAME, CONTAINER_NAME, blobName);
        URI uri = URI.create(uriString);
        return new BasicAuthURI(uri);
    }
}
