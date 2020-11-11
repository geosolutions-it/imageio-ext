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
package it.geosolutions.imageio.plugins.cog;

import it.geosolutions.imageio.plugins.tiff.TIFFImageReadParam;
import it.geosolutions.imageioimpl.plugins.cog.RangeReader;

/**
 * A subclass of TIFFImageReadParam to hold information about which RangeReader implementation to use.
 *
 * @author joshfix
 * Created on 2019-09-18
 */
public class CogImageReadParam extends TIFFImageReadParam {

    protected int headerLength = DEFAULT_HEADER_LENGTH;
    protected Class<? extends RangeReader> rangeReaderClass;

    public static final String DEFAULT_COG_HEADER_LENGTH_KEY = "it.geosolutions.cog.default.header.length";

    public static final int DEFAULT_HEADER_LENGTH;

    static {
        final String defaultHeaderLength= System.getProperty(DEFAULT_COG_HEADER_LENGTH_KEY);
        if (defaultHeaderLength != null) {
            DEFAULT_HEADER_LENGTH = Integer.parseInt(defaultHeaderLength);
        } else {
            DEFAULT_HEADER_LENGTH = 16384;
        }
    }

    public CogImageReadParam() {
        super();
    }

    public CogImageReadParam(Class<? extends RangeReader> rangeReaderClass) {
        super();
        this.rangeReaderClass = rangeReaderClass;
    }

    public CogImageReadParam(int headerLength) {
        super();
        this.headerLength = headerLength;
    }

    public CogImageReadParam(Class<? extends RangeReader> rangeReaderClass, int headerLength) {
        super();
        this.rangeReaderClass = rangeReaderClass;
        this.headerLength = headerLength;
    }

    public Class<? extends RangeReader> getRangeReaderClass() {
        return rangeReaderClass;
    }

    public void setRangeReaderClass(Class<? extends RangeReader> rangeReaderClass) {
        this.rangeReaderClass = rangeReaderClass;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }
}
