/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2020, GeoSolutions
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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import it.geosolutions.imageio.core.ReadingAccessObject;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

/**
 * A ReadingAccessObject subclass containing additional specific
 * info for COG
 */
public class CogReadingAccessObject extends ReadingAccessObject {

    private CogUri cogUri;

    /** The full classname of the RangeReader implementation */
    private String rangeReaderClassname;

    public CogReadingAccessObject(
            CogUri cogUri,
            ImageReaderSpi readerSpi,
            ImageInputStreamSpi streamSpi,
            String rangeReader) {
        super(cogUri.getUri().toString(), readerSpi, streamSpi);
        this.cogUri = cogUri;
        this.rangeReaderClassname = rangeReader;
    }

    public String getRangeReaderClassname() {
        return rangeReaderClassname;
    }

    public CogUri getCogUri() {
        return cogUri;
    }

    /**
     * Get an initialized COG stream: The Header will be read before
     * returning the stream back to the caller.
     */
    public ImageInputStream getInitializedCogInputStream() throws IOException {
        CogUri uri = getCogUri();
        CogImageInputStream inStream =
                (CogImageInputStream)
                        ((CogImageInputStreamSpi) getStreamSpi())
                                .createInputStreamInstance(uri, uri.isUseCache(), null);
        RangeReader rangeReader = createRangeReaderInstance(rangeReaderClassname, uri.getUri(), CogImageReadParam.DEFAULT_HEADER_LENGTH);
        inStream.init(rangeReader);
        return inStream;
    }

    /**
     * Instantiate a new RangeReader based on the specified className implementation,
     * on top of the given URI, using the specified headerLength
     *
     * @param className the complete className of the required RangeReader implementation
     * @param uri the source URI
     * @param headerLength the headerLength
     * @return a RangeReader instance
     */
    public static RangeReader createRangeReaderInstance(String className, URI uri, int headerLength) {
        RangeReader rangeReader = null;
        if (className != null) {
            try {
                final Class<?> clazz = Class.forName(className);
                Constructor constructor = clazz.getConstructor(new Class[] {URI.class, int.class});
                rangeReader =
                        (RangeReader)
                                constructor.newInstance(uri, headerLength);
            } catch (Exception e) {
                rangeReader = null;
            }
        }
        return rangeReader;
    }
}
