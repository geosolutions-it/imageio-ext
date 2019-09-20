package it.geosolutions.imageioimpl.plugins.cog;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author joshfix
 * Created on 2019-09-19
 */
public class CogUrl {

    private URI uri;

    public CogUrl(String url) {
        uri = URI.create(url);
    }

    public CogUrl(URL url) throws URISyntaxException {
        uri = url.toURI();
    }

    public CogUrl(URI uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return uri.toString();
    }

    public URI getUri() {
        return uri;
    }

}
