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
package it.geosolutions.imageio.tiff;

import it.geosolutions.imageioimpl.plugins.cog.CacheManagement;
import it.geosolutions.imageioimpl.plugins.cog.TileCacheEntryKey;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests cache.  CogImageInputStreams are tested in the cog-reader module.
 *
 * @author joshfix
 * Created on 2019-08-22
 */
public class CacheTest {

    @Test
    public void testCache() {
        byte[] content = new byte[128];
        content[0] = 74;

        TileCacheEntryKey key = new TileCacheEntryKey("http://test.url.com/image.tif", 0);
        CacheManagement.DEFAULT.cacheTile(key, content);

        Assert.assertTrue(CacheManagement.DEFAULT.keyExists(key));

        byte[] fetchedContent = CacheManagement.DEFAULT.getTile(key);

        Assert.assertEquals(content.length, fetchedContent.length);
        Assert.assertEquals(content[0], fetchedContent[0]);
    }

}
