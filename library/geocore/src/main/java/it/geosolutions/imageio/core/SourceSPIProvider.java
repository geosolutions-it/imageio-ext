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
package it.geosolutions.imageio.core;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * A provider containing SPIs to get reading access on a source:
 * a provided SPI to get an ImageReader as well as a provided SPI to get
 * an ImageInputStream on top of that source.
 */
public class SourceSPIProvider {

    private ImageReaderSpi readerSpi;
    private ImageInputStreamSpi streamSpi;
    private Object source;

    public ImageInputStreamSpi getStreamSpi() {
        return streamSpi;
    }

    public void setStreamSpi(ImageInputStreamSpi streamSpi) {
        this.streamSpi = streamSpi;
    }

    public ImageReaderSpi getReaderSpi() {
        return readerSpi;
    }

    public void setReaderSpi(ImageReaderSpi readerSpi) {
        this.readerSpi = readerSpi;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    /**
     * Return a URL representation of the source, returning null
     * when no URL representation can be obtained
     */
    public URL getSourceUrl() throws MalformedURLException {
        if (source instanceof URL) {
            return (URL) source;
        } else if (source instanceof String) {
            return new URL((String)source);
        } else if (source instanceof File) {
            return fileToUrl((File) source);
        } else if (source instanceof URI) {
            return ((URI) source).toURL();
        } else {
            return null;
        }
    }

    public SourceSPIProvider(
            Object source, ImageReaderSpi readerSpi, ImageInputStreamSpi streamSpi) {
        this.readerSpi = readerSpi;
        this.streamSpi = streamSpi;
        this.source = source;
    }


    public ImageReader getReader() throws IOException {
        return readerSpi.createReaderInstance();
    }

    public ImageInputStream getStream() throws IOException {
        return streamSpi.createInputStreamInstance(
                        source,
                        ImageIO.getUseCache(),
                        ImageIO.getCacheDirectory());
    }

    /**
     * Return a compatible SourceProvider (same readerSPI, same streamSPI)
     * for a different URL
     */
    public SourceSPIProvider getCompatibleSourceProvider (URL url) {
        return new SourceSPIProvider(url, readerSpi, streamSpi);
    }

    public static URL fileToUrl(File file) {
        try {
            // URI.toString() and thus URI.toURL() do not
            // percent-encode non-ASCII characters [GEOT-5737]
            String string = file.toURI().toASCIIString();
            if (string.contains("+")) {
                // this represents an invalid URL created using either
                // file.toURL(); or
                // file.toURI().toURL() on a specific version of Java 5 on Mac
                string = string.replace("+", "%2B");
            }
            if (string.contains(" ")) {
                // this represents an invalid URL created using either
                // file.toURL(); or
                // file.toURI().toURL() on a specific version of Java 5 on Mac
                string = string.replace(" ", "%20");
            }
            return new URL(string);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
