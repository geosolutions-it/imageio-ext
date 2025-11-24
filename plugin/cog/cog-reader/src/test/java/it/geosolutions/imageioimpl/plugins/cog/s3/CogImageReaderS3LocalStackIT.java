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
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * {@link CogImageReader} integration test for {@link S3RangeReader} using testcontainers with a
 * {@link LocalStackContainer}.
 *
 * <p>This test uses the LocalStack emulator to test AWS S3 integration without requiring real AWS credentials or making
 * actual network calls to S3.
 *
 * <p>The emulator is configured via system properties that are read by {@link S3ConfigurationProperties} via
 * {@code PropertyLocator}:
 *
 * <ul>
 *   <li>{@code iio.s3.aws.endpoint} - The LocalStack S3 endpoint
 *   <li>{@code iio.s3.aws.region} - The AWS region (LocalStack default)
 *   <li>{@code iio.s3.aws.user} - The LocalStack access key
 *   <li>{@code iio.s3.aws.password} - The LocalStack secret key
 * </ul>
 *
 * <p>Test files are uploaded to the emulator using the AWS S3 SDK, and then accessed via {@link S3RangeReader} using
 * the standard {@code s3://bucket/key} URI format.
 *
 * <p><b>Note:</b> This test may interfere with other S3-based tests (e.g., {@link CogImageReaderS3MinioIT}) when run in
 * the same JVM. The root cause is that {@link S3ClientFactory} caches S3AsyncClient instances in a static map keyed by
 * region only, not considering the endpoint. If multiple tests use the same region but different endpoints, they may
 * inadvertently share a cached client pointing to a stopped container, causing "Connection refused" errors.
 */
public class CogImageReaderS3LocalStackIT extends BaseCogImageReaderTest {

    private static final DockerImageName DOCKER_IMAGE_NAME = DockerImageName.parse("localstack/localstack:s3-latest");

    private static final String BUCKET_NAME = "test-bucket";

    @ClassRule
    public static CogTestData testData = new CogTestData();

    static LocalStackContainer localstack = new LocalStackContainer(DOCKER_IMAGE_NAME);

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
        localstack.start();

        // Configure S3RangeReader to use LocalStack endpoint
        // Property names must be lowercase to work with PropertyLocator.getEnvironmentValue()
        System.setProperty("iio.s3.aws.endpoint", localstack.getEndpoint().toString());
        System.setProperty("iio.s3.aws.region", localstack.getRegion());
        System.setProperty("iio.s3.aws.user", localstack.getAccessKey());
        System.setProperty("iio.s3.aws.password", localstack.getSecretKey());

        s3Client = createClient();
        createBucket();

        Path landTopoCog1024 = testData.landTopoCog1024();
        upload(landTopoCog1024);
    }

    @AfterClass
    public static void stopContainer() {
        localstack.stop();
    }

    @AfterClass
    public static void clearProperties() {
        System.clearProperty("iio.s3.aws.endpoint");
        System.clearProperty("iio.s3.aws.region");
        System.clearProperty("iio.s3.aws.user");
        System.clearProperty("iio.s3.aws.password");
    }

    private static S3Client createClient() {
        // Create credentials provider
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // Initialize S3 client with explicit endpoint configuration for MinIO
        S3Client client = S3Client.builder()
                .endpointOverride(localstack.getEndpoint())
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(credentialsProvider)
                .forcePathStyle(true) // Important for S3 compatibility with LocalStack
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
        // The system properties set in setUpContainer() will make the S3 client use LocalStack
        String uri = "s3://%s/%s".formatted(BUCKET_NAME, blobName);
        return new BasicAuthURI(uri);
    }
}
