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

import java.awt.*;

/**
 * Interface for ImageReader requiring an initialization step
 * usually needed right before invoking the setInput method
 */
public interface InitializingReader {

    /**
     * Initialize the reader. hints may be provided to customize the
     * initialization steps. A boolean will be returned to report
     * that the initialization occurred.
     */
    boolean init (RenderingHints hints);
}
