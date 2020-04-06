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
package it.geosolutions.imageio.cog;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.CogTileInfo;
import it.geosolutions.imageioimpl.plugins.cog.TileRange;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing ability to build tile range metadata.
 * 
 * @author joshfix
 */
public class CogTileInfoTest {

    @Test
    public void buildTileInfo() {
        CogTileInfo info = new CogTileInfo(CogImageReadParam.DEFAULT_HEADER_LENGTH);

        int tile1Index = 0;
        int tile1Offset = 10000;
        int tile1ByteLength = 100;
        info.addTileRange(tile1Index, tile1Offset, tile1ByteLength);

        // verify the header length is adjusted to not overlap with the first tile offset
        Assert.assertEquals(tile1Offset, info.getHeaderLength());

        // verify that given a position in the byte array, the proper tile index is returned
        Assert.assertEquals(tile1Index, info.getTileIndex(tile1Offset + (tile1ByteLength / 2)));

        // test that getting a tile range by an offset or by index results in the same TileRange object
        TileRange tileRange1 = info.getTileRange((long)(tile1Offset + (tile1ByteLength / 2)));
        TileRange tileRange2 = info.getTileRange(tile1Index);
        Assert.assertEquals(tileRange1, tileRange2);

    }
}
