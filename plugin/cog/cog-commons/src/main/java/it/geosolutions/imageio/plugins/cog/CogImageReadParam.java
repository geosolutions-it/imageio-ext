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

    Class<? extends RangeReader> rangeReaderClass;

    public CogImageReadParam() {
        super();
    }

    public CogImageReadParam(Class<? extends RangeReader> rangeReaderClass) {
        super();
        this.rangeReaderClass = rangeReaderClass;
    }

    public Class<? extends RangeReader> getRangeReaderClass() {
        return rangeReaderClass;
    }

    public void setRangeReaderClass(Class<? extends RangeReader> rangeReaderClass) {
        this.rangeReaderClass = rangeReaderClass;
    }
}
