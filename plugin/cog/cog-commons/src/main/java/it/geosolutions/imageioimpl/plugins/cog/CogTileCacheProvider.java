package it.geosolutions.imageioimpl.plugins.cog;

/**
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
