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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class CogTestData extends ExternalResource {

    private TemporaryFolder tmpDir = new TemporaryFolder();

    private Path landTopoCog1024;

    @Override
    protected void before() throws Throwable {
        tmpDir.create();
        landTopoCog1024 = extract("/land_topo_cog_jpeg_1024.tif");
    }

    @Override
    protected void after() {
        tmpDir.delete();
    }

    public Path landTopoCog1024() {
        return landTopoCog1024;
    }

    private Path extract(String resource) throws IOException {
        String fileName = Paths.get(resource).getFileName().toString();
        File file = tmpDir.newFile(fileName);
        URL url = getClass().getResource(resource);
        IOUtils.copy(url, file);
        return file.toPath();
    }
}
