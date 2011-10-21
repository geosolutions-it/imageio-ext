/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.stream.input;

import java.io.IOException;
import java.net.URI;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * An implementation of {@link ImageInputStream} that gets its input from a
 * {@link URI}.
 * 
 * Note that this class doesn't actually allow read operations. It is actually
 * only used to allow defining an {@link ImageInputStream} for a URI.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class URIImageInputStreamImpl extends ImageInputStreamImpl implements
        URIImageInputStream {

    private URI uri;

    public int read() throws IOException {
        throw new UnsupportedOperationException(
                "read method is actually unsupported.");
    }

    public int read(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException(
                "read method is actually unsupported.");
    }

    public URI getUri() {
        return uri;
    }

    public URIImageInputStreamImpl(final URI uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null!");
        }
        this.uri = uri;
    }

    public URI getTarget() {
        return uri;
    }

    public Class<URI> getBinding() {
        return URI.class;
    }
}
