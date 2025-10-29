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
package it.geosolutions.imageioimpl.plugins.cog.s3;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageioimpl.plugins.cog.BaseCogImageReaderTest;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogTestData;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;
import it.geosolutions.imageioimpl.plugins.cog.S3ClientFactory;
import it.geosolutions.imageioimpl.plugins.cog.S3ConfigurationProperties;
import it.geosolutions.imageioimpl.plugins.cog.S3RangeReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.MinIOContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * {@link CogImageReader} integration test for {@link S3RangeReader} using testcontainers with a {@link MinIOContainer}.
 *
 * <p>This test uses the MinIO container to test S3-compatible storage integration without requiring real AWS
 * credentials or making actual network calls to S3. MinIO provides an S3-compatible API that is commonly used for
 * development and testing.
 *
 * <p>The MinIO container is configured via system properties that are read by {@link S3ConfigurationProperties} via
 * {@code PropertyLocator}:
 *
 * <ul>
 *   <li>{@code iio.s3.aws.endpoint} - The MinIO S3-compatible endpoint
 *   <li>{@code iio.s3.aws.region} - Set to us-east-1 (required by SDK but not used by MinIO)
 *   <li>{@code iio.s3.aws.user} - The MinIO access key
 *   <li>{@code iio.s3.aws.password} - The MinIO secret key
 *   <li>{@code iio.s3.aws.force.path.style} - Must be set to true for MinIO compatibility
 * </ul>
 *
 * <p>Test files are uploaded to MinIO using the AWS S3 SDK (which is S3-compatible), and then accessed via
 * {@link S3RangeReader} using the standard {@code s3://bucket/key} URI format. The {@code force.path.style} property
 * ensures the S3 client uses path-style URLs when communicating with MinIO.
 *
 * <p><b>Note:</b> This test may interfere with other S3-based tests (e.g., {@link CogImageReaderS3LocalStackIT}) when
 * run in the same JVM. The root cause is that {@link S3ClientFactory} caches S3AsyncClient instances in a static map
 * keyed by region only, not considering the endpoint. If multiple tests use the same region but different endpoints,
 * they may inadvertently share a cached client pointing to a stopped container, causing "Connection refused" errors.
 */
public class CogImageReaderS3MinioIT extends BaseCogImageReaderTest {

    private static final String BUCKET_NAME = "test-bucket";

    @ClassRule
    public static CogTestData testData = new CogTestData();

    static MinIOContainer container = new MinIOContainer("minio/minio:latest");

    private static S3Client s3Client;

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

        // Configure S3RangeReader to use MinIO endpoint
        // Property names must be lowercase to work with PropertyLocator.getEnvironmentValue()
        System.setProperty("iio.s3.aws.endpoint", container.getS3URL());
        System.setProperty("iio.s3.aws.region", "us-east-1"); // MinIO doesn't care, but required
        System.setProperty("iio.s3.aws.user", container.getUserName());
        System.setProperty("iio.s3.aws.password", container.getPassword());
        System.setProperty("iio.s3.aws.force.path.style", "true"); // Required for MinIO

        s3Client = createClient();
        createBucket();

        Path landTopoCog1024 = testData.landTopoCog1024();
        upload(landTopoCog1024);
    }

    @AfterClass
    public static void stopContainer() {
        container.stop();
    }

    @AfterClass
    public static void clearProperties() {
        System.clearProperty("iio.s3.aws.endpoint");
        System.clearProperty("iio.s3.aws.region");
        System.clearProperty("iio.s3.aws.user");
        System.clearProperty("iio.s3.aws.password");
        System.clearProperty("iio.s3.aws.force.path.style");
    }

    private static S3Client createClient() {
        // Create credentials provider
        AwsBasicCredentials credentials = AwsBasicCredentials.create(container.getUserName(), container.getPassword());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // Initialize S3 client with explicit endpoint configuration for MinIO
        S3Client client = S3Client.builder()
                .endpointOverride(URI.create(container.getS3URL()))
                .region(Region.US_EAST_1) // MinIO doesn't care about region, but it's required by the SDK
                .credentialsProvider(credentialsProvider)
                .forcePathStyle(true) // Important for S3 compatibility with MinIO
                .build();
        return client;
    }

    private static void createBucket() {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
    }

    private static void upload(Path localFile) {
        String blobName = localFile.getFileName().toString();
        PutObjectRequest putRequest =
                PutObjectRequest.builder().bucket(BUCKET_NAME).key(blobName).build();
        RequestBody body = RequestBody.fromFile(localFile);
        s3Client.putObject(putRequest, body);
    }

    @Override
    protected Class<? extends RangeReader> getRangeReaderClass() {
        return S3RangeReader.class;
    }

    @Override
    protected BasicAuthURI landTopoCog1024ConnectionParams() {
        String blobName = testData.landTopoCog1024().getFileName().toString();
        // Use s3:// URI format which S3RangeReader supports
        // The system properties set in setUpContainer() (including force.path.style)
        // will make the S3 client use MinIO
        String uri = "s3://%s/%s".formatted(BUCKET_NAME, blobName);
        return new BasicAuthURI(uri);
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void getNumImages() throws IOException {
        super.getNumImages();
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void isSeekForwardOnly() throws IOException {
        super.isSeekForwardOnly();
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void isImageTiled() throws IOException {
        super.isImageTiled();
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void getWidthAndHeight() throws IOException {
        super.getWidthAndHeight();
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void getAspectRatio() throws IOException {
        super.getAspectRatio();
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void getRawImageType() throws IOException {
        super.getRawImageType();
    }

    @Test
    @Ignore(
            """
            Fails with Cannot invoke "java.util.Map.entrySet()" because "this.data" is null
            """)
    public void getTileDimensions() throws IOException {
        super.getTileDimensions();
    }
}
