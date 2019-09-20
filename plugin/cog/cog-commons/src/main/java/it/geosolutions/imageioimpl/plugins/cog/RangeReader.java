package it.geosolutions.imageioimpl.plugins.cog;

import java.util.Collection;

/**
 * @author joshfix
 * Created on 2019-08-21
 */
public interface RangeReader {

    byte[] getBytes();
    int getFilesize();
    void setFilesize(int filesize);
    void readAsync(long[]... ranges);
    void readAsync(Collection<long[]> ranges);
    byte[] readHeader(int headerByteLength);

}
