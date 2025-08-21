/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.utilities.SoftValueHashMap;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static it.geosolutions.imageioimpl.plugins.cog.CogTileInfo.HEADER_TILE_INDEX;

/**
 * This ImageInputStream implementation asynchronously fetches all tiles/ranges via the RangeReader implementation and
 * utilizes ehcache to cache each tile requested by the TIFFImageReader.  All subsequent tile reads will be fetched
 * from cache.
 * <p>
 * NOTE: This is a special use case class and is intended for use ONLY with the CogImageReader.  Using this
 * ImageInputStream for other purposes will almost certainly result in errors/failures.
 *
 * @author joshfix
 * Created on 2019-08-28
 */
public class CachingCogImageInputStream extends DefaultCogImageInputStream implements CogImageInputStream {

    private boolean initialized = false;

    private static final Logger LOGGER = Logger.getLogger(CachingCogImageInputStream.class.getName());

    public CachingCogImageInputStream(String url) {
        super(url);
    }

    public CachingCogImageInputStream(URL url) {
        super(url);
    }

    public CachingCogImageInputStream(URI uri) {
        super(uri);
    }

    public CachingCogImageInputStream(BasicAuthURI cogUri) {
        super(cogUri);
    }

    public CachingCogImageInputStream(URI uri, RangeReader rangeReader) {
        super(uri, rangeReader);
    }


    @Override
    protected void initializeHeader(int headerLength) {
        header = new CogTileInfo(headerLength);
        data = new SoftValueHashMap<>(0);

        // determine if the header has already been cached
        byte[] headerBytes;
        if (CacheManagement.DEFAULT.headerExists(uri.toString())) {
            headerBytes = CacheManagement.DEFAULT.getHeader(uri.toString());
            rangeReader.setHeaderLength(headerBytes.length);
        } else {
            headerBytes = rangeReader.readHeader();
            CacheManagement.DEFAULT.cacheHeader(uri.toString(), headerBytes);
        }
        data.put(0L, headerBytes);
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * TIFFImageReader will read and decode the requested region of the GeoTIFF tile by tile.  Because of this, we will
     * not arbitrarily store fixed-length byte chunks in cache, but instead create a cache entry for all the bytes for
     * each tile.
     */
    @Override
    public void readRanges(CogTileInfo cogTileInfo) {

        Map<Integer, TileRange> missingTiles = loadDataFromCache(cogTileInfo);

        // fetch the missing tiles
        ContiguousRangeComposer contiguousRangeComposer =
                new ContiguousRangeComposer(0L, cogTileInfo.getHeaderLength() - 1L);
        missingTiles.forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            contiguousRangeComposer.addTileRange(tileRange.getStart(), tileRange.getEnd());
        });
        rangeReader.setHeaderLength(cogTileInfo.getHeaderLength());
        Set<long[]> ranges = contiguousRangeComposer.getRanges();
        LOGGER.fine("Submitting " + ranges.size() + " range request(s)");
        Map<Long, byte[]> fetchedTiles = rangeReader.read(ranges);
        this.data.putAll(fetchedTiles);

        //create contiguous data chunks (required for super.read(...))
        this.data = ByteChunksMerger.merge(this.data);

        //populate cache
        updateTileCache(missingTiles);

    }

    private Map<Integer, TileRange> loadDataFromCache(CogTileInfo cogTileInfo) {
        Map<Integer, TileRange> missingTiles = new HashMap<>();
        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileIndex);
            if (CacheManagement.DEFAULT.keyExists(key)) {
                data.put(tileRange.getStart(), CacheManagement.DEFAULT.getTile(key));
            } else {
                missingTiles.put(tileIndex, tileRange);
            }
        });

        return missingTiles;
    }


    private void updateTileCache(Map<Integer, TileRange> missingTiles) {
        missingTiles.forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileIndex);

            for (Map.Entry<Long, byte[]> entry : data.entrySet()) {
                long contiguousRangeOffset = entry.getKey();
                byte[] contiguousBytes = entry.getValue();
                int contiguousRangeLength = (int) contiguousRangeOffset + contiguousBytes.length;
                if (tileRange.getStart() >= contiguousRangeOffset && tileRange.getEnd() < contiguousRangeLength) {
                    int relativeOffset = (int) (tileRange.getStart() - contiguousRangeOffset);
                    int tileByteLen = (int) tileRange.getByteLength();
                    byte[] tileBytes = new byte[tileByteLen];
                    System.arraycopy(contiguousBytes, relativeOffset, tileBytes, 0, tileByteLen);
                    CacheManagement.DEFAULT.cacheTile(key, tileBytes);
                }
            }
        });
    }

}
