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

    protected int headerLength;
    protected Map<Integer, TileRange> tileRanges = new TreeMap<>();
    public static final int HEADER_TILE_INDEX = -100;

    public CogTileInfo(int headerLength) {
        this.headerLength = headerLength;
        addHeaderRange();
    }

    /**
     * Adds a TileRange to the map containing the information about the COG header.
     *
     * @return The created TileRange
     */
    public TileRange addHeaderRange() {
        return tileRanges.put(HEADER_TILE_INDEX, new TileRange(HEADER_TILE_INDEX, 0, headerLength));
    }

    /**
     * Adds a new TileRange to the map.
     *
     * @param tileIndex The index of the tile
     * @param offset The byte offset of the tile
     * @param byteLength The byte length of the tile
     * @return The created TileRange
     */
    public TileRange addTileRange(int tileIndex, long offset, long byteLength) {
        checkHeaderSize(offset);
        return tileRanges.put(tileIndex, new TileRange(tileIndex, offset, byteLength));
    }

    /**
     * We don't actually know the true header size and use an arbitrary value (16KB). If a tile offset starts before
     * the end of the header, we can reduce the header size.  Note that this does not necessarily provide us with the
     * true end location of the header, but we can avoid overlapping header and tile ranges by reducing it.
     *
     * @param offset the starting offset of the tile range to be added
     */
    protected void checkHeaderSize(long offset) {
        if (offset < headerLength && offset > 0) {
            headerLength = (int)offset;
            tileRanges.put(HEADER_TILE_INDEX, new TileRange(HEADER_TILE_INDEX, 0, headerLength));
        }
    }

    /**
     * If the header length is known to be a value other than the default, it may be specified with this method.
     *
     * @param headerLength the byte length of the header
     */
    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    /**
     * Returns the byte length of the header.
     *
     * @return the byte length of the header
     */
    public int getHeaderLength() {
        return headerLength;
    }

    /**
     * Returns the map of TileRanges.
     *
     * @return the map of TileRanges
     */
    public Map<Integer, TileRange> getTileRanges() {
        return tileRanges;
    }

    /**
     * Gets a TileRange by tile index.
     *
     * @param tileIndex the index of the tile
     * @return the TileRange at the given index
     */
    public TileRange getTileRange(int tileIndex) {
        return tileRanges.get(tileIndex);
    }

    /**
     * Given a byte location / offset, returns the corresponding TileRange object.
     *
     * @param offset the byte location of the requested TileRange
     * @return the TileRange for the given offset
     */
    public TileRange getTileRange(long offset) {
        for (TileRange tileRange : tileRanges.values()) {
            if (offset >= tileRange.getStart() && offset < tileRange.getEnd()) {
                return tileRange;
            }
        }
        return null;
    }

    /**
     * Given a byte location / offset, returns the corresponding tile index.
     *
     * @param offset the byte location of the requested TileRange
     * @return the index of the tile
     */
    public int getTileIndex(long offset) {
        for (Map.Entry<Integer, TileRange> entry : tileRanges.entrySet()) {
            if (offset >= entry.getValue().getStart() && offset < entry.getValue().getEnd()) {
                return entry.getKey();
            }
        }
        return -1;
    }

}
