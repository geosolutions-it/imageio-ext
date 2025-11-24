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
package it.geosolutions.imageioimpl.plugins.cog.gs;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.aiven.testcontainers.fakegcsserver.FakeGcsServerContainer;
import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageioimpl.plugins.cog.BaseCogImageReaderTest;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogTestData;
import it.geosolutions.imageioimpl.plugins.cog.GSRangeReader;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

/**
 * {@link CogImageReader} integration test for {@link GSRangeReader} using testcontainers with a
 * {@link FakeGcsServerContainer}.
 *
 * <p>This test uses the fake-gcs-server emulator to test Google Cloud Storage integration without requiring real GCS
 * credentials or making actual network calls to GCS.
 *
 * <p>The emulator is configured via the {@code gs.reader.host} system property, which is read by
 * {@code BlobCache.createDefaultStorage()} during static initialization. This property tells the GCS client to connect
 * to the fake-gcs-server instead of real GCS.
 *
 * <p>Test files are uploaded to the emulator using the GCS Java SDK, and then accessed via {@link GSRangeReader} using
 * the standard {@code gs://bucket/object} URI format.
 */
public class CogImageReaderGoogleCoudStorageIT extends BaseCogImageReaderTest {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String PROJECT_ID = "test-project";

    @ClassRule
    public static CogTestData testData = new CogTestData();

    static FakeGcsServerContainer container = new FakeGcsServerContainer();

    private static Storage storage;

    private static String emulatorEndpoint;

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

        // Configure the Storage client to use the emulator
        String emulatorHost = container.getHost();
        Integer emulatorPort = container.getFirstMappedPort();
        emulatorEndpoint = "http://" + emulatorHost + ":" + emulatorPort;

        // Set system property for GSRangeReader/BlobCache to use the emulator
        // This must be set before BlobCache.DEFAULT_STORAGE is initialized
        System.setProperty("gs.reader.host", emulatorEndpoint);

        storage = createBucket();

        Path landTopoCog1024 = testData.landTopoCog1024();
        upload(landTopoCog1024);
    }

    @AfterClass
    public static void stopContainer() {
        container.stop();
    }

    private static Storage createBucket() {
        // Create Storage client
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(PROJECT_ID)
                .setHost(emulatorEndpoint)
                .build()
                .getService();

        // Create a bucket
        BucketInfo bucketInfo = BucketInfo.newBuilder(BUCKET_NAME).build();
        storage.create(bucketInfo);
        return storage;
    }

    private static void upload(Path localFile) throws IOException {
        String blobName = localFile.getFileName().toString();

        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, blobName).build();
        storage.createFrom(blobInfo, localFile);
    }

    @Override
    protected Class<? extends RangeReader> getRangeReaderClass() {
        return GSRangeReader.class;
    }

    @Override
    protected BasicAuthURI landTopoCog1024ConnectionParams() {
        String blobName = testData.landTopoCog1024().getFileName().toString();
        // Use gs:// URI format which GSRangeReader supports
        // The STORAGE_EMULATOR_HOST system property set in setUpContainer()
        // will make the GCS client use the emulator
        String uri = "gs://%s/%s".formatted(BUCKET_NAME, blobName);
        return new BasicAuthURI(uri);
    }
}
