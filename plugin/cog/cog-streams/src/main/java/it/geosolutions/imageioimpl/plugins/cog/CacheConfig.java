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

import static it.geosolutions.imageioimpl.plugins.cog.PropertyLocator.getEnvironmentValue;

/**
 * Configuration properties for tile cache.  Attempts to read environment variables containing connection settings and
 * if not found, will fallback to attempting to read system properties.  If still not found, the provided default
 * values will be used.
 *
 * @author joshfix
 * Created on 2019-09-19
 */
public class CacheConfig {

    // whether disk caching should be disabled
    public static final String COG_CACHING_USE_DISK = "IIO_COG_CACHING_USEDISK";

    // whether off heap should be used. currently not supported
    public static final String COG_CACHING_USE_OFF_HEAP = "IIO_COG_CACHING_USEOFFHEAP";

    // the disk cache size.
    public static final String COG_CACHING_DISK_CACHE_SIZE = "IIO_COG_CACHING_DISKCACHESIZE";

    // path for the disk cache
    public static final String COG_CACHING_DISK_PATH = "IIO_COG_CACHING_DISKPATH";

    // alternatively an EhCache 2.x XML config can be used to override all cache config
    public static final String COG_CACHING_EH_CACHE_CONFIG = "IIO_COG_CACHING_EHCACHECONFIG";

    // in heap cache size in number of entries
    public static final String COG_CACHING_HEAP_ENTRIES = "IIO_COG_CACHING_HEAPENTRIES";

    // in heap cache size in bytes
    public static final String COG_CACHING_OFF_HEAP_SIZE = "IIO_COG_CACHING_OFFHEAPSIZE";

    // time to idle in seconds
    public static final String COG_CACHING_TIME_TO_IDLE = "IIO_COG_CACHING_TIMETOIDLE";

    // time to live in seconds
    public static final String COG_CACHING_TIME_TO_LIVE = "IIO_COG_CACHING_TIMETOLIVE";

    public static final int MEBIBYTE_IN_BYTES = 1048576;

    private static boolean useDiskCache;
    private static boolean useOffHeapCache;
    private static int diskCacheSize;
    private static int offHeapSize;
    private static int heapEntries;
    private static String cacheDirectory;
    private static long timeToIdle;
    private static long timeToLive;
    private static String xmlConfigPath;

    public CacheConfig() {
        useDiskCache = Boolean.getBoolean(getEnvironmentValue(COG_CACHING_USE_DISK, "false"));
        useOffHeapCache = Boolean.getBoolean(getEnvironmentValue(COG_CACHING_USE_OFF_HEAP, "false"));
        diskCacheSize = Integer.parseInt(
                getEnvironmentValue(COG_CACHING_DISK_CACHE_SIZE, Integer.toString(500 * MEBIBYTE_IN_BYTES)));
        offHeapSize = Integer.parseInt(
                getEnvironmentValue(COG_CACHING_OFF_HEAP_SIZE, Integer.toString(50 * MEBIBYTE_IN_BYTES)));
        heapEntries = Integer.parseInt(getEnvironmentValue(COG_CACHING_HEAP_ENTRIES, "2000"));
        cacheDirectory = getEnvironmentValue(COG_CACHING_DISK_PATH, null);
        timeToIdle = Integer.parseInt(getEnvironmentValue(COG_CACHING_TIME_TO_IDLE, "0"));
        timeToLive = Integer.parseInt(getEnvironmentValue(COG_CACHING_TIME_TO_LIVE, "0"));
        xmlConfigPath = getEnvironmentValue(COG_CACHING_EH_CACHE_CONFIG, null);
    }

    public static CacheConfig getDefaultConfig() {
        return new CacheConfig();
    }

    public boolean isUseDiskCache() {
        return useDiskCache;
    }

    public void setUseDiskCache(boolean useDiskCache) {
        this.useDiskCache = useDiskCache;
    }

    public boolean isUseOffHeapCache() {
        return useOffHeapCache;
    }

    public void setUseOffHeapCache(boolean useOffHeapCache) {
        this.useOffHeapCache = useOffHeapCache;
    }

    public int getDiskCacheSize() {
        return diskCacheSize;
    }

    public void setDiskCacheSize(int diskCacheSize) {
        this.diskCacheSize = diskCacheSize;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public int getHeapEntries() {
        return heapEntries;
    }

    public void setHeapEntries(int heapEntries) {
        this.heapEntries = heapEntries;
    }

    public int getOffHeapSize() {
        return offHeapSize;
    }

    public void setOffHeapSize(int offHeapSize) {
        this.offHeapSize = offHeapSize;
    }

    public long getTimeToIdle() {
        return timeToIdle;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToIdle(long timeToIdle) {
        this.timeToIdle = timeToIdle;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public String getXmlConfigPath() {
        return xmlConfigPath;
    }

    public void setXmlConfigPath(String xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }
}
