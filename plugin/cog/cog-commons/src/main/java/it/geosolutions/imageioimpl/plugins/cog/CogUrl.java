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
