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
package it.geosolutions.imageioimpl.plugins.cog;

import java.util.Map;
import java.util.TreeMap;

/**
 * This is a utility class that stores all of the required tile range metadata for a given read operation.
 *
 * @author joshfix
 * Created on 2019-08-28
 */
public class CogTileInfo {

    protected int headerSize;
    protected long firstTileOffset = Long.MAX_VALUE;
    protected long firstTileByteLength;
    protected Map<Integer, TileRange> tileRanges = new TreeMap<>();
    public static final int HEADER_TILE_INDEX = -100;

    public CogTileInfo(int headerSize) {
        this.headerSize = headerSize;
    }

    public void addTileRange(int tileIndex, long offset, long byteLength) {
        //if ((offset < firstTileOffset && offset > 0) || tileIndex == 0) {
        if (offset < firstTileOffset && offset > 0) {
            firstTileOffset = offset;
            firstTileByteLength = byteLength;
        }
        if (offset < headerSize && offset > 0) {
            //headerSize = (int)offset - 1;
            headerSize = (int)offset;
            tileRanges.put(HEADER_TILE_INDEX, new TileRange(HEADER_TILE_INDEX, 0, headerSize));
        }


        tileRanges.put(tileIndex, new TileRange(tileIndex, offset, byteLength));
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public long getFirstTileOffset() {
        return firstTileOffset;
    }

    public long getFirstTileByteLength() {
        return firstTileByteLength;
    }

    public Map<Integer, TileRange> getTileRanges() {

        return tileRanges;
    }

    public TileRange getTileRange(int tileIndex) {
        return tileRanges.get(tileIndex);
    }

    public TileRange getTileRange(long offset) {
        for (TileRange tileRange : tileRanges.values()) {
            if (offset >= tileRange.getStart() && offset < tileRange.getEnd()) {
                return tileRange;
            }
        }
        return null;
    }

    public int getTileIndex(long offset) {
        for (Map.Entry<Integer, TileRange> entry : tileRanges.entrySet()) {
            if (offset >= entry.getValue().getStart() && offset < entry.getValue().getEnd()) {
                return entry.getKey();
            }
        }
        return -1;
    }

}
