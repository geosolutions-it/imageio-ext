/*
 * GeoTools - The Open Source Java GIS Toolkit
 * http://geotools.org
 *
 * (C) 2017, Open Source Geospatial Foundation (OSGeo)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package it.geosolutions.imageioimpl.plugins.cog;

import java.io.Serializable;

/** Simple key for cache entries */
public class TileCacheEntryKey implements Serializable {

    private String url;
    private int tileIndex;

    public TileCacheEntryKey(String url, int tileIndex) {
        this.url = url;
        this.tileIndex = tileIndex;
    }

    public String getUrl() {
        return url;
    }

    public int getTileIndex() {
        return tileIndex;
    }

    @Override
    public String toString() {
        return String.format("TileCacheEntry[url: %s, tile index: %d]", url, tileIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TileCacheEntryKey that = (TileCacheEntryKey) o;

        if (!url.equals(that.url) || tileIndex != that.tileIndex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + tileIndex;
        return result;
    }

}
