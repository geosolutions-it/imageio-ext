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
package it.geosolutions.imageioimpl.plugins.cog;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.azure.CogImageReaderAzureIT;
import it.geosolutions.imageioimpl.plugins.cog.gs.CogImageReaderGoogleCoudStorageIT;
import it.geosolutions.imageioimpl.plugins.cog.http.CogImageReaderHttpIT;
import it.geosolutions.imageioimpl.plugins.cog.s3.CogImageReaderS3LocalStackIT;
import it.geosolutions.imageioimpl.plugins.cog.s3.CogImageReaderS3MinioIT;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Abstract base class for {@link CogImageReader} integration tests across different storage backends.
 *
 * <p>This class provides a standardized test suite that validates {@link CogImageReader} functionality with various
 * {@link RangeReader} implementations (HTTP, S3, Azure, Google Cloud Storage, etc.). Each concrete subclass configures
 * a specific storage backend (e.g., Nginx for HTTP, MinIO for S3, Azurite for Azure) and provides connection parameters
 * to the test file.
 *
 * <p>The test suite validates:
 *
 * <ul>
 *   <li>Image metadata reading (dimensions, tile sizes, number of images)
 *   <li>Image type specification and sample models
 *   <li>Partial image reading using source regions
 *   <li>Multi-resolution support (COG overviews)
 * </ul>
 *
 * <p>All tests use a standard test file ({@code land_topo_cog_jpeg_1024.tif}), a Cloud Optimized GeoTIFF with:
 *
 * <ul>
 *   <li>Full resolution: 1024x512 pixels
 *   <li>Overview: 512x256 pixels
 *   <li>Tile size: 512x512 pixels
 *   <li>3 bands (RGB)
 * </ul>
 *
 * <p><b>Implementing a new test:</b> Subclasses must:
 *
 * <ol>
 *   <li>Start and configure the storage backend/emulator in {@code @BeforeClass}
 *   <li>Upload the test file from {@link CogTestData#landTopoCog1024()} to the backend
 *   <li>Implement {@link #landTopoCog1024ConnectionParams()} to return the URI to access the uploaded file
 *   <li>Implement {@link #getRangeReaderClass()} to specify which {@link RangeReader} to use
 *   <li>Stop the backend/emulator in {@code @AfterClass}
 * </ol>
 *
 * <p><b>Note:</b> {@link FileCogImageReaderTest} is a related test that validates {@link CogImageReader} functionality
 * using local file access instead of remote storage. Unlike the integration tests listed below, which run during the
 * {@code verify} phase with testcontainers, {@code FileCogImageReaderTest} runs during the {@code test} phase as a
 * pre-flight check against a local file without requiring Docker or external services.
 *
 * @see CogImageReaderHttpIT
 * @see CogImageReaderAzureIT
 * @see CogImageReaderGoogleCoudStorageIT
 * @see CogImageReaderS3LocalStackIT
 * @see CogImageReaderS3MinioIT
 * @see FileCogImageReaderTest
 */
@RunWith(Parameterized.class)
public abstract class BaseCogImageReaderTest {

    @Parameters(name = "caching={0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {false}, // without caching
            {true} // with caching
        });
    }

    @Parameter
    public boolean caching;

    protected ImageReader imageReader;

    @After
    public void dispose() {
        if (imageReader != null) {
            imageReader.dispose();
        }
    }
    /** Connection parameters for {@link #getLandTopoCog1024ImageInputStream()} */
    protected abstract BasicAuthURI landTopoCog1024ConnectionParams();

    /**
     * Builds the {@link CogImageInputStream} to be uses as {@link ImageReader#setInput(Object) input} in
     * {@link #getLandTopoCog1024ImageReader()}
     *
     * @param caching whether to use a {@link CachingCogImageInputStream} (true) or {@link DefaultCogImageInputStream}
     *     (false)
     */
    protected ImageInputStream getLandTopoCog1024ImageInputStream(boolean caching) {
        BasicAuthURI params = landTopoCog1024ConnectionParams();
        ImageInputStream cogStream;
        if (caching) {
            cogStream = new CachingCogImageInputStream(params);
        } else {
            cogStream = new DefaultCogImageInputStream(params);
        }
        return cogStream;
    }

    /** Builds a {@link CogImageReader} with {@link #getLandTopoCog1024ImageInputStream()} as input */
    protected CogImageReader getLandTopoCog1024ImageReader() throws IOException {
        return getLandTopoCog1024ImageReader(false);
    }

    /** Builds a {@link CogImageReader} with {@link #getLandTopoCog1024ImageInputStream(boolean)} as input */
    protected CogImageReader getLandTopoCog1024ImageReader(boolean caching) throws IOException {
        ImageInputStream cogStream = getLandTopoCog1024ImageInputStream(caching);
        CogImageReader imageReader = new CogImageReader(new CogImageReaderSpi());
        imageReader.setInput(cogStream);
        return imageReader;
    }

    /**
     * {@link RangeReader} concrete class to use for {@link CogImageReadParam#setRangeReaderClass(Class)}
     * before asking for images to {@link ImageReader#read(int, javax.imageio.ImageReadParam).
     *
     * @return {@code null} if no specific range reader class is to be used, the concrete class to use otherwise
     */
    protected abstract Class<? extends RangeReader> getRangeReaderClass();

    @Test
    public void getNumImages() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);
        boolean allowSearch = true;
        int numImages = imageReader.getNumImages(allowSearch);
        assertEquals(2, numImages);
    }

    @Test
    public void isSeekForwardOnly() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);
        assertFalse(imageReader.isSeekForwardOnly());
    }

    @Test
    public void isImageTiled() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);
        assertTrue(imageReader.isImageTiled(0));
        assertTrue(imageReader.isImageTiled(1));
    }

    @Test
    public void getWidthAndHeight() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);

        assertEquals(1024, imageReader.getWidth(0));
        assertEquals(512, imageReader.getHeight(0));

        assertEquals(512, imageReader.getWidth(1));
        assertEquals(256, imageReader.getHeight(1));
    }

    @Test
    public void getAspectRatio() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);
        assertEquals(2f, imageReader.getAspectRatio(0), 0.0001f);
        assertEquals(2f, imageReader.getAspectRatio(1), 0.0001f);
    }

    @Test
    public void getRawImageType() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);
        ImageTypeSpecifier rawImageType = imageReader.getRawImageType(0);
        assertNotNull(rawImageType);
        int numBands = rawImageType.getNumBands();
        int numComponents = rawImageType.getNumComponents();
        assertEquals(3, numBands);
        assertEquals(3, numComponents);
        SampleModel sampleModel = rawImageType.getSampleModel();
        assertThat(sampleModel, instanceOf(PixelInterleavedSampleModel.class));
    }

    @Test
    public void getTileDimensions() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);

        int tileWidth = imageReader.getTileWidth(0);
        int tileHeight = imageReader.getTileHeight(0);
        assertEquals(512, tileWidth);
        assertEquals(512, tileHeight);

        tileWidth = imageReader.getTileWidth(1);
        tileHeight = imageReader.getTileHeight(1);
        assertEquals(512, tileWidth);
        assertEquals(512, tileHeight);
    }

    @Test
    public void testReadOverview() throws IOException {
        imageReader = getLandTopoCog1024ImageReader(caching);
        final int imageIndex = 1;
        // Overview bounds: 0,0,512,256

        // Single pixel
        testReadImage(imageReader, imageIndex, 256, 128, 1, 1);

        // Top-left corner
        testReadImage(imageReader, imageIndex, 0, 0, 128, 64);

        // Top-right corner
        testReadImage(imageReader, imageIndex, 384, 0, 128, 64);

        // Bottom-left corner
        testReadImage(imageReader, imageIndex, 0, 192, 128, 64);

        // Bottom-right corner
        testReadImage(imageReader, imageIndex, 384, 192, 128, 64);

        // Center region
        testReadImage(imageReader, imageIndex, 192, 96, 128, 64);

        // Full image
        testReadImage(imageReader, imageIndex, 0, 0, 512, 256);
    }

    @Test
    public void testReadFullResolution() throws IOException {
        ImageReader imageReader = getLandTopoCog1024ImageReader(caching);
        final int imageIndex = 0;
        // Full resolution bounds: 0,0,1024,512

        // Single pixel
        testReadImage(imageReader, imageIndex, 512, 256, 1, 1);

        // Top-left corner
        testReadImage(imageReader, imageIndex, 0, 0, 256, 128);

        // Top-right corner
        testReadImage(imageReader, imageIndex, 768, 0, 256, 128);

        // Bottom-left corner
        testReadImage(imageReader, imageIndex, 0, 384, 256, 128);

        // Bottom-right corner
        testReadImage(imageReader, imageIndex, 768, 384, 256, 128);

        // Center region
        testReadImage(imageReader, imageIndex, 384, 192, 256, 128);

        // Sub-region not aligned to full image bounds
        testReadImage(imageReader, imageIndex, 1, 1, 1022, 510);

        // Full image
        testReadImage(imageReader, imageIndex, 0, 0, 1024, 512);
    }

    protected void testReadImage(
            ImageReader imageReader, final int imageIndex, final int x, final int y, final int width, final int height)
            throws IOException {

        BufferedImage cogImage = readImage(imageReader, x, y, width, height, imageIndex);

        assertEquals(width, cogImage.getWidth());
        assertEquals(height, cogImage.getHeight());
    }

    protected BufferedImage readImage(
            ImageReader imageReader, final int x, final int y, final int width, final int height, final int imageIndex)
            throws IOException {

        CogImageReadParam param = new CogImageReadParam();
        param.setSourceRegion(new Rectangle(x, y, width, height));
        Class<? extends RangeReader> rangeReaderClass = getRangeReaderClass();
        if (rangeReaderClass != null) {
            param.setRangeReaderClass(rangeReaderClass);
        }
        BufferedImage cogImage = imageReader.read(imageIndex, param);
        return cogImage;
    }
}
