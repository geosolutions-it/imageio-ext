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

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
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
public class CachingCogImageInputStream extends ImageInputStreamImpl implements CogImageInputStream {

    private boolean initialized = false;

    protected URI uri;
    protected RangeReader rangeReader;
    protected CogTileInfo header;

    private final static Logger LOGGER = Logger.getLogger(CachingCogImageInputStream.class.getName());

    public CachingCogImageInputStream(URI uri) {
        this.uri = uri;
    }

    public CachingCogImageInputStream(String uri) {
        this(URI.create(uri));
    }

    public CachingCogImageInputStream(URL url) {
        this(url.toString());
    }

    public CachingCogImageInputStream(BasicAuthURI cogUri) {
        this.uri = cogUri.getUri();
    }

    public CachingCogImageInputStream(URI uri, RangeReader rangeReader) {
        this.uri = uri;
        init(rangeReader);
    }

    /**
     * Directly sets the range reader and reads the header.
     *
     * @param rangeReader A `RangeReader` implementation to be used.
     */
    @Override
    public void init(RangeReader rangeReader) {
        this.rangeReader = rangeReader;
        initializeHeader();
    }

    /**
     * Uses the class specified in `CogImageReadParam` to attempt to instantiate a `RangeReader` implementation, then
     * reads the header.
     *
     * @param param An `ImageReadParam` that contains information about which `RangeReader` implementation to use.
     */
    @Override
    public void init(CogImageReadParam param) {
        Class<? extends RangeReader> rangeReaderClass = ((CogImageReadParam) param).getRangeReaderClass();
        if (null != rangeReaderClass) {
            try {
                rangeReader = rangeReaderClass.getDeclaredConstructor(URI.class, int.class)
                        .newInstance(uri, param.getHeaderLength());
            } catch (Exception e) {
                LOGGER.severe("Unable to instantiate range reader class " + rangeReaderClass.getCanonicalName());
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Range reader class not specified in CogImageReadParam.");
        }

        if (rangeReader == null) {
            throw new RuntimeException("Unable to instantiate range reader class "
                    + rangeReaderClass.getCanonicalName());
        }

        initializeHeader(param.getHeaderLength());
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    protected void initializeHeader() {
        initializeHeader(CogImageReadParam.DEFAULT_HEADER_LENGTH);
    }

    protected void initializeHeader(int headerLength) {
        header = new CogTileInfo(headerLength);

        // determine if the header has already been cached
        if (CacheManagement.DEFAULT.headerExists(uri.toString())) {
            headerLength = CacheManagement.DEFAULT.getHeader(uri.toString()).length;
            rangeReader.setHeaderLength(headerLength);
        } else {
            CacheManagement.DEFAULT.cacheHeader(uri.toString(), rangeReader.fetchHeader());
        }
        initialized = true;
    }

    @Override
    public CogTileInfo getHeader() {
        return header;
    }

    /**
     * TIFFImageReader will read and decode the requested region of the GeoTIFF tile by tile.  Because of this, we will
     * not arbitrarily store fixed-length byte chunks in cache, but instead create a cache entry for all the bytes for
     * each tile.
     * <p>
     * The first step is to loop through the tile ranges from CogTileInfo and determine which tiles are already cached.
     * Tile ranges that are not in cache are submitted to RangeBuilder to build contiguous ranges to be read via HTTP.
     * <p>
     * Once the contiguous ranges have been read, we obtain the full image-length byte array from the RangeReader.  Then
     * loop through each of the requested tile ranges from CogTileInfo and cache the bytes.
     * <p>
     * There are likely lots of optimizations to be made in here.
     */
    @Override
    public void readRanges(CogTileInfo cogTileInfo) {
        // instantiate the range builder
        ContiguousRangeComposer contiguousRangeComposer =
                new ContiguousRangeComposer(0, cogTileInfo.getHeaderLength() - 1);

        // determine which requested tiles are not in cache and build the required ranges that need to be read (if any)
        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }

            TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileIndex);
            if (!CacheManagement.DEFAULT.keyExists(key)) {
                contiguousRangeComposer.addTileRange(tileRange.getStart(), tileRange.getEnd());
            }
        });
        rangeReader.setHeaderLength(cogTileInfo.getHeaderLength());

        // get the ranges for the tiles that are not already cached.  if there are none, simply return
        Set<long[]> ranges = contiguousRangeComposer.getRanges();
        if (ranges.size() == 0) {
            return;
        }

        // read all they byte ranges for tiles that are not in cache
        LOGGER.fine("Submitting " + ranges.size() + " range request(s)");
        // Update the headerLength
        Map<Long, byte[]> data = rangeReader.read(ranges);

        // cache the bytes for each tile
        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileIndex);

            for (Map.Entry<Long, byte[]> entry : data.entrySet()) {
                long contiguousRangeOffset = entry.getKey();
                int contiguousRangeLength = (int) contiguousRangeOffset + entry.getValue().length;
                if (tileRange.getStart() >= contiguousRangeOffset && tileRange.getEnd() < contiguousRangeLength) {
                    byte[] contiguousBytes = entry.getValue();
                    long relativeOffset = tileRange.getStart() - contiguousRangeOffset;
                    byte[] tileBytes = Arrays
                            .copyOfRange(contiguousBytes, (int) relativeOffset, (int) tileRange.getEnd());
                    CacheManagement.DEFAULT.cacheTile(key, tileBytes);
                }
            }
        });
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        read(b, 0, 1);
        return b[0];
    }

    @Override
    public int read(byte[] b, int off, int len) {
        // based on the stream position, determine which tile we are in and fetch the corresponding TileRange
        // TODO: CachingCogImageInputStream never worked very well.
        //  We need to update this section too, when fixing the related ticket
        TileRange tileRange = header.getTileRange(streamPos);

        // get the bytes from cache for the tile. need to determine if we're reading from the header or a tile.
        byte[] bytes;
        switch (tileRange.getIndex()) {
            case HEADER_TILE_INDEX:
                bytes = CacheManagement.DEFAULT.getHeader(uri.toString());
                break;
            default:
                TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileRange.getIndex());
                bytes = CacheManagement.DEFAULT.getTile(key);
        }

        // translate the overall stream position to the stream position of the fetched tile
        int relativeStreamPos = (int) (streamPos - tileRange.getStart());

        // copy the bytes from the fetched tile into the destination byte array
        System.arraycopy(bytes, relativeStreamPos, b, off, len);
        streamPos += len;
        return len;
    }
}
