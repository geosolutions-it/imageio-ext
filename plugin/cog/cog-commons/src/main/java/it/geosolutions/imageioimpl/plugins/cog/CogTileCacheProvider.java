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

    byte[] getTile(TileCacheEntryKey key);

    void cacheTile(TileCacheEntryKey key, byte[] tileBytes);

    boolean keyExists(TileCacheEntryKey key);

    byte[] getHeader(String key);

    void cacheHeader(String key, byte[] headerBytes);

    boolean headerExists(String key);

    int getFilesize(String key);

    void cacheFilesize(String key, int filesize);

    boolean filesizeExists(String key);
}
