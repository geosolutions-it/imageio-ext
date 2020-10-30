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

import javax.imageio.stream.ImageInputStream;

/**
 * This interface provides important methods for ImageInputStream implementations that wish to use the CogImageReader.
 * CogImageInputStream implementations are special ImageInputStream implementations in that they are not immediately
 * ready for use after instantiation.  The COG header must first be read in order to determine all tile byte locations
 * and lengths.  This should be accomplished via one of the `init` methods.
 *
 * A CogImageInputStream implementation is responsible to create and provide a `CogTileInfo` instance via the
 * `getCogTileInfo` method.  This object will hold information about the requested COG tiles.  Once this object is
 * populated, the `readRanges` method may be called.  This method is responsible for calling the appropriate
 * `RangeReader` implementation to read all of the ranges specified by the `CogTileInfo` instance.
 *
 * @author joshfix
 * Created on 2019-08-23
 */
public interface CogImageInputStream extends ImageInputStream {

    /**
     * Instructs the input stream to read the ranges for the requested tiles.
     */
    void readRanges(CogTileInfo cogTileInfo);

    /**
     * Makes available the `CogTileInfo` object, responsible for holding all location information for requested tiles.
     *
     * @return The CogTileInfo instance
     */
    CogTileInfo getHeader();

    /**
     * Initializes the stream and reads the COG header.
     *
     * @param param An `ImageReadParam` that contains information about which `RangeReader` implementation to use.
     */
    void init(CogImageReadParam param);

    /**
     * Initializes the stream and reads the COG header.
     *
     * @param rangeReader A `RangeReader` implementation to be used.
     */
    void init(RangeReader rangeReader);

    /**
     * Signals whether or not the input stream has been initialized.  If false, no header information has been read
     * and the input stream is non-operational.
     * @return
     */
    boolean isInitialized();

}
