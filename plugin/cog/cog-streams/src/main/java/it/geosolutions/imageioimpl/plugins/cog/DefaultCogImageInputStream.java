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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static it.geosolutions.imageioimpl.plugins.cog.CogTileInfo.HEADER_TILE_INDEX;

/**
 * ImageInputStream implementation for COG.  This class will request all requested ranges be read by
 * the provided RangeReader implementation and store the results in memory.  When TIFFImageReader requests tiles, the
 * byte data will be served from the `data` Map.
 *
 * NOTE: This is a special use case class and is intended for use ONLY with the CogImageReader.  Using this
 * ImageInputStream for other purposes will almost certainly result in errors/failures.
 *
 * @author joshfix
 * Created on 2019-08-23
 */
public class DefaultCogImageInputStream extends ImageInputStreamImpl implements CogImageInputStream {

    private boolean initialized = false;

    protected URI uri;
    protected CogTileInfo cogTileInfo;
    protected RangeReader rangeReader;
    protected Map<Long, byte[]> data;

    private final static Logger LOGGER = Logger.getLogger(DefaultCogImageInputStream.class.getName());

    public DefaultCogImageInputStream(String url) {
        this(URI.create(url));
    }

    public DefaultCogImageInputStream(URL url) {
        this(URI.create(url.toString()));
    }

    public DefaultCogImageInputStream(URI uri) {
        this.uri = uri;
    }

    public DefaultCogImageInputStream(CogUri cogUri) {
        this.uri = cogUri.getUri();
    }

    public DefaultCogImageInputStream(URI uri, RangeReader rangeReader) {
        this.uri = uri;
        this.rangeReader = rangeReader;
        cogTileInfo = new CogTileInfo();
        initializeHeader();
    }

    public void init(RangeReader rangeReader) {
        this.rangeReader = rangeReader;
        cogTileInfo = new CogTileInfo();
        initializeHeader();
    }

    public void init(CogImageReadParam param) {
        Class<? extends RangeReader> rangeReaderClass = ((CogImageReadParam) param).getRangeReaderClass();
        if (null != rangeReaderClass) {
            try {
                cogTileInfo = new CogTileInfo();
                rangeReader = rangeReaderClass.getDeclaredConstructor(URI.class, int.class)
                        .newInstance(uri, cogTileInfo.getHeaderLength());
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

    protected void initializeHeader() {
        data = new HashMap<>();
        data.put(0L, rangeReader.readHeader());
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public CogTileInfo getCogTileInfo() {
        return cogTileInfo;
    }

    @Override
    public void readRanges() {
        // read data with the RangeReader and set the byte order and pointer on the new input stream
        ContiguousRangeComposer contiguousRangeComposer = new ContiguousRangeComposer(0, cogTileInfo.getHeaderLength() - 1);

        cogTileInfo.getTileRanges().forEach((tileIndex, tileRange) -> {
            if (tileIndex == HEADER_TILE_INDEX) {
                return;
            }
            contiguousRangeComposer.addTileRange(tileRange.getStart(), tileRange.getEnd());
        });

        // read all of the ranges asynchronously
        Set<long[]> ranges = contiguousRangeComposer.getRanges();
        LOGGER.fine("Submitting " + ranges.size() + " range request(s)");

        data = rangeReader.read(ranges);
    }

    public String getUrl() {
        return uri.toString();
    }

    @Override
    public int read() throws IOException {
        streamPos++;
        return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        byte[] contiguousRange = null;
        long rangeStart = -1L;

        // find they byte array in the data map corresponding to the current request
        long absoluteOffset = streamPos + off;
        for (Map.Entry<Long, byte[]> entry : data.entrySet()) {
            long start = entry.getKey();
            long end = entry.getKey() + entry.getValue().length;
            if (absoluteOffset >= start && absoluteOffset < end) {
                contiguousRange = entry.getValue();
                rangeStart = entry.getKey();
            }
        }

        // this should never happen -- we should have read all bytes from all tiles in the request envelope
        if (contiguousRange == null || rangeStart == -1L) {
            streamPos += len;
            return len;
        }

        int relativeStreamPos = (int)(streamPos - rangeStart) + off;
        // copy the bytes from the fetched tile into the destination byte array
        for (int i = 0; i < len; i++) {
            try {
                b[i] = contiguousRange[relativeStreamPos + i];
            } catch (Exception e) {
                LOGGER.severe("Error copying bytes. requested offset: " + off
                        + " - requested length: " + len
                        + " - relativeStreamPos: " + relativeStreamPos
                        + " - streamPos: " + streamPos);
            }
        }

        streamPos += len;
        return len;
    }

}
