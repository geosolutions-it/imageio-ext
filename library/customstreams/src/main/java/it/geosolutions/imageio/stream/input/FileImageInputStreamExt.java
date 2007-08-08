/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
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
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */

public interface FileImageInputStreamExt extends ImageInputStream  {
    /**
     * Returns the associated file
     *
     * @return the associated file
     */
    public File getFile();
}
