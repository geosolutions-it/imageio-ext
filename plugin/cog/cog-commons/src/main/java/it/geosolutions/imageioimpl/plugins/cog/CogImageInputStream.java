package it.geosolutions.imageioimpl.plugins.cog;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

/**
 * @author joshfix
 * Created on 2019-08-23
 */
public interface CogImageInputStream {

    void readRanges();
    CogTileInfo getCogTileInfo();
    void setHeaderByteLength(int headerByteLength);
    void init(CogImageReadParam param);
    boolean isInitialized();

}
