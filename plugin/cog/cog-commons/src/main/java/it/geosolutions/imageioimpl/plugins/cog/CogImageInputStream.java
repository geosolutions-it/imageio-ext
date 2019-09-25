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

/**
 * This interface provides important methods for ImageInputStream implementations that wish to use the CogImageReader.
 * The defined methods enable building and reading byte ranges from remote COG files.
 *
 * @author joshfix
 * Created on 2019-08-23
 */
public interface CogImageInputStream {

    void readRanges();
    CogTileInfo getCogTileInfo();
    void setInitialHeaderReadLength(int initialHeaderReadLength);
    void init(CogImageReadParam param);
    boolean isInitialized();

}
