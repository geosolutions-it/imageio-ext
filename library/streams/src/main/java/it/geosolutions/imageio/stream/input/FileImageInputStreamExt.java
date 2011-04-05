/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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

import java.io.File;

import javax.imageio.stream.ImageInputStream;

/**
 * An {@link ImageInputStream} that gets its input from a {@link File}.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public interface FileImageInputStreamExt extends ImageInputStream {
    /**
     * Returns the associated {@link File}
     * 
     * @return the associated {@link File}
     */
    public File getFile();
}
