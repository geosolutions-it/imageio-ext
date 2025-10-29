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

import it.geosolutions.imageio.core.BasicAuthURI;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.stream.FileImageInputStream;
import org.junit.ClassRule;

/**
 * {@link CogImageReader} test for {@link CogImageReader} working against a file. As a unit test serves as validation to
 * run the integration tests later.
 */
public class FileCogImageReaderTest extends BaseCogImageReaderTest {

    @ClassRule
    public static CogTestData testData = new CogTestData();

    /**
     * Override to return the {@link CogImageReader} directly for the test file without calling
     * {@link #landTopoCog1024ConnectionParams()}.
     *
     * <p>The caching parameter is ignored for file-based tests since FileImageInputStream is always used.
     */
    @Override
    protected CogImageReader getLandTopoCog1024ImageReader(boolean caching) throws IOException {
        CogImageReader cogImageReader = new CogImageReader(new CogImageReaderSpi());
        Path file = testData.landTopoCog1024();
        FileImageInputStream input = new FileImageInputStream(file.toFile());
        cogImageReader.setInput(input);
        return cogImageReader;
    }

    /** @return null, this test suite doesn't need a concrete {@link RangeReader} */
    @Override
    protected Class<? extends RangeReader> getRangeReaderClass() {
        return null;
    }

    @Override
    protected BasicAuthURI landTopoCog1024ConnectionParams() {
        throw new UnsupportedOperationException("Unusued method");
    }
}
