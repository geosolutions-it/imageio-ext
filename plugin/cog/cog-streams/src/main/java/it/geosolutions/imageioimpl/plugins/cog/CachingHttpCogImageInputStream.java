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

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

import static it.geosolutions.imageioimpl.plugins.cog.CogTileInfo.HEADER_TILE_INDEX;

/**
 * @author joshfix
 * Created on 2019-08-28
 */
public class CachingHttpCogImageInputStream extends ImageInputStreamImpl implements CogImageInputStream {

    private boolean initialized = false;
    protected int initialHeaderReadLength = 16384;

    protected URI uri;
    protected RangeReader rangeReader;
    protected CogTileInfo cogTileInfo;


    private final static Logger LOGGER = Logger.getLogger(CachingHttpCogImageInputStream.class.getName());

    public CachingHttpCogImageInputStream(String url) {
        this(URI.create(url));
    }

    public CachingHttpCogImageInputStream(URL url) {
        this(URI.create(url.toString()));
    }

    public CachingHttpCogImageInputStream(URI uri) {
        this.uri = uri;
    }

    public CachingHttpCogImageInputStream(URI uri, RangeReader rangeReader) {
        this.uri = uri;
        this.rangeReader = rangeReader;
        initialHeaderReadLength = rangeReader.getHeaderLength();
        initializeHeader();
    }

    public void init(RangeReader rangeReader) {
        this.rangeReader = rangeReader;
        initialHeaderReadLength = rangeReader.getHeaderLength();
        initializeHeader();
    }

    public void init(CogImageReadParam param) {
        Class<? extends RangeReader> rangeReaderClass = ((CogImageReadParam) param).getRangeReaderClass();
        if (null != rangeReaderClass) {
            try {
                rangeReader = rangeReaderClass.getDeclaredConstructor(URI.class).newInstance(uri);
                rangeReader.setHeaderLength(initialHeaderReadLength);
            } catch (Exception e) {
                LOGGER.severe("Unable to instantiate range reader class " + rangeReaderClass.getCanonicalName());
                throw new RuntimeException(e);
            }
        }

        if (rangeReader == null) {
            return;
        }

        initializeHeader();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    protected void initializeHeader() {
        // determine if the header has already been cached
        if (!CacheManagement.DEFAULT.headerExists(uri.toString())) {
            CacheManagement.DEFAULT.cacheHeader(uri.toString(), rangeReader.readHeader());
            CacheManagement.DEFAULT.cacheFilesize(uri.toString(), rangeReader.getFilesize());
            cogTileInfo = new CogTileInfo(initialHeaderReadLength);
            cogTileInfo.addTileRange(HEADER_TILE_INDEX, 0, initialHeaderReadLength);
        } else {
            int headerLength = CacheManagement.DEFAULT.getHeader(uri.toString()).length;
            rangeReader.setFilesize(CacheManagement.DEFAULT.getFilesize(uri.toString()));
            rangeReader.setHeaderLength(headerLength);
            cogTileInfo = new CogTileInfo(headerLength);
            cogTileInfo.addTileRange(HEADER_TILE_INDEX, 0, headerLength);
        }
        initialized = true;
    }

    public CogTileInfo getCogTileInfo() {
        return cogTileInfo;
    }

    @Override
    public void setInitialHeaderReadLength(int initialHeaderReadLength) {
        this.initialHeaderReadLength = initialHeaderReadLength;
    }
static int count = 0;
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
    public void readRanges() {
        long firstTileOffset = cogTileInfo.getFirstTileOffset();

        // update the cached header with the actual header header bytes/size (we originally read with a default of 16k)
        byte[] headerBytes = CacheManagement.DEFAULT.getHeader(uri.toString());
        if (firstTileOffset < headerBytes.length) {
            byte[] newHeaderBytes = Arrays.copyOf(headerBytes, (int) firstTileOffset);
            CacheManagement.DEFAULT.cacheHeader(uri.toString(), newHeaderBytes);
            rangeReader.setHeaderLength(newHeaderBytes.length);
        }

        // instantiate the range builder
        RangeBuilder rangeBuilder = new RangeBuilder(0, cogTileInfo.getHeaderSize() - 1);

        // determine which requested tiles are not in cache and build the required ranges that need to be read (if any)
        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }

            TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileIndex);
            if (!CacheManagement.DEFAULT.keyExists(key)) {
                //rangeBuilder.addTileRange(tileRange.getStart(), tileRange.getByteLength());
                rangeBuilder.addTileRange(tileRange.getStart(), tileRange.getEnd());
            }
        });

        // get the ranges for the tiles that are not already cached.  if there are none, simply return
        Set<long[]> ranges = rangeBuilder.getRanges();
        if (ranges.size() == 0) {
            return;
        }

        // read all they byte ranges for tiles that are not in cache
        LOGGER.fine("Submitting " + ranges.size() + " range request(s)");
        rangeReader.readAsync(ranges);

        // cache the bytes for each tile
        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            TileCacheEntryKey key = new TileCacheEntryKey(uri.toString(), tileIndex);
            try {
                byte[] b = rangeReader.getBytes();
                byte[] tileBytes =
                        Arrays.copyOfRange(b, (int) tileRange.getStart(), (int) (tileRange.getEnd() + 1));
                CacheManagement.DEFAULT.cacheTile(key, tileBytes);
            } catch (Exception e) {
                e.printStackTrace();
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
        TileRange tileRange = cogTileInfo.getTileRange(streamPos);

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
        int relativeStreamPos = (int) (streamPos - tileRange.getStart() + off);

        // copy the bytes from the fetched tile into the destination byte array
        for (long i = 0; i < len; i++) {
            try {
                b[(int) i] = bytes[(int) (relativeStreamPos + i)];
            } catch (Exception e) {
                LOGGER.severe("Error copying bytes. requested offset: " + off
                        + " - requested length: " + len
                        + " - relativeStreamPos: " + relativeStreamPos
                        + " - streamPos: " + streamPos
                        + " - tile range start: " + tileRange.getStart()
                        + " - tile range length: " + tileRange.getByteLength());
            }
        }

        streamPos += len;
        return len;
    }

}
