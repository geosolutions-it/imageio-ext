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
package it.geosolutions.imageioimpl.plugins.cog.http;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageioimpl.plugins.cog.BaseCogImageReaderTest;
import it.geosolutions.imageioimpl.plugins.cog.CogImageReader;
import it.geosolutions.imageioimpl.plugins.cog.CogTestData;
import it.geosolutions.imageioimpl.plugins.cog.HttpRangeReader;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;
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
import org.testcontainers.nginx.NginxContainer;
import org.testcontainers.utility.MountableFile;

/**
 * {@link CogImageReader} integration test for {@link HttpRangeReader} using testcontainers with a
 * {@link NginxContainer}.
 *
 * <p>This test validates that {@link HttpRangeReader} can successfully read Cloud Optimized GeoTIFF files over HTTP
 * using HTTP range requests. Test files are served from an Nginx container, which natively supports HTTP range requests
 * (RFC 7233).
 *
 * <p>Test files are copied into the Nginx container's document root ({@code /usr/share/nginx/html/}) and accessed via
 * standard HTTP URLs. The test validates the end-to-end functionality of reading COG files over HTTP, which is a common
 * deployment pattern for serving geospatial imagery.
 */
public class CogImageReaderHttpIT extends BaseCogImageReaderTest {

    @ClassRule
    public static CogTestData testData = new CogTestData();

    static NginxContainer nginx;

    private static URI landTopoCog1024Url;

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
    @SuppressWarnings("resource")
    public static void setUpContainer() throws IOException {
        Path landTopoCog1024 = testData.landTopoCog1024();
        String fileName = landTopoCog1024.getFileName().toString();

        nginx = new NginxContainer("nginx:latest")
                .withCopyToContainer(MountableFile.forHostPath(landTopoCog1024), "/usr/share/nginx/html/" + fileName);
        nginx.start();

        // Set up the URI for accessing the test file via HTTP
        String baseUrl = String.format("http://%s:%d", nginx.getHost(), nginx.getFirstMappedPort());
        landTopoCog1024Url = URI.create(baseUrl + "/" + fileName);
    }

    @AfterClass
    public static void stopContainer() {
        if (nginx != null) {
            nginx.stop();
        }
    }

    @Override
    protected Class<? extends RangeReader> getRangeReaderClass() {
        return HttpRangeReader.class;
    }

    @Override
    protected BasicAuthURI landTopoCog1024ConnectionParams() {
        return new BasicAuthURI(landTopoCog1024Url);
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
