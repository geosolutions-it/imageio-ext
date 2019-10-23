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

/**
 * This interface should be implemented by cache providers to be compatible with the CachingHttpCogImageInputStream.
 *
 * @author joshfix
 * Created on 2019-08-29
 */
public interface CogTileCacheProvider {

    /**
     * Returns the cached bytes for a COG tile.
     *
     * @param key The key used to identify the COG tile
     * @return The byte array containing the tile data
     */
    byte[] getTile(TileCacheEntryKey key);

    /**
     * Enters a new tile into cache.
     *
     * @param key The key used to identify the COG tile
     * @param tileBytes The byte array containing the tile data
     */
    void cacheTile(TileCacheEntryKey key, byte[] tileBytes);

    /**
     * Specifies whether or not the key exists in cache.
     *
     * @param key The key used to identify the COG tile
     * @return Boolean value representing whether or not the tile has been cached
     */
    boolean keyExists(TileCacheEntryKey key);

    /**
     * Returns the cached bytes for a COG header.
     *
     * @param key The key used to identify the COG header
     * @return The byte array containing the header data
     */
    byte[] getHeader(String key);

    /**
     * Enters a new tile into cache.
     *
     * @param key The key used to identify the COG header
     * @param headerBytes he byte array containing the header data
     */
    void cacheHeader(String key, byte[] headerBytes);

    /**
     * Specifies whether or not the key exists in cache.
     *
     * @param key The key used to identify the COG header
     * @return Boolean value representing whether or not the header has been cached
     */
    boolean headerExists(String key);

}
