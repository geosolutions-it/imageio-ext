package it.geosolutions.imageioimpl.plugins.cog;

import java.net.URI;
import java.net.URL;

/**
 * A simple wrapper for URI to allow for easier ImageInputStream selection via ImageIOExt.getImageInputStreamSPI()
 *
 * @author joshfix
 * Created on 2019-09-25
 */
public class CogUri {

    private URI uri;
    private boolean useCache = true;

    public CogUri(String uri) {
        this(URI.create(uri));
    }

    public CogUri(String uri, boolean useCache) {
        this(URI.create(uri), useCache);
    }

    public CogUri(URL url) {
        this(url.toString());
    }

    public CogUri(URL url, boolean useCache) {
        this(url.toString(), useCache);
    }

    public CogUri(URI uri) {
        this(uri, true);
    }

    public CogUri(URI uri, boolean useCache) {
        setUri(uri);
        setUseCache(useCache);
    }

    public URI getUri() {
        return uri;
    }

    public CogUri uri(URI uri) {
        setUri(uri);
        return this;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public CogUri useCache(boolean useCache) {
        setUseCache(useCache);
        return this;
    }
}
