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

import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;

/**
 * An object containing elementary objects for a reading access on a source:
 * a provided SPI to get an ImageReader as well as a provided SPI to get
 * an ImageInputStream on top of that source.
 */
public class ReadingAccessObject {

    private ImageReaderSpi readerSpi;
    private ImageInputStreamSpi streamSpi;
    private String source;

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ReadingAccessObject(
            String source, ImageReaderSpi readerSpi, ImageInputStreamSpi streamSpi) {
        this.readerSpi = readerSpi;
        this.streamSpi = streamSpi;
        this.source = source;
    }
}
