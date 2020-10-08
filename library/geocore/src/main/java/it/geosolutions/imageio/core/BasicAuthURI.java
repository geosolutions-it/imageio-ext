/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageio.core;

import java.net.URI;
import java.net.URL;

/**
 * A simple wrapper for URI to allow for easier ImageInputStream selection via ImageIOExt.getImageInputStreamSPI()
 *
 * @author joshfix
 * Created on 2019-09-25
 */
public class BasicAuthURI {

    private URI uri;
    private boolean useCache = true;

    private String user;

    private String password;

    public BasicAuthURI(String uri) {
        this(URI.create(uri));
    }

    public BasicAuthURI(String uri, boolean useCache) {
        this(URI.create(uri), useCache);
    }

    public BasicAuthURI(URL url) {
        this(url.toString());
    }

    public BasicAuthURI(URL url, boolean useCache) {
        this(url.toString(), useCache);
    }

    public BasicAuthURI(URI uri) {
        this(uri, true);
    }

    public BasicAuthURI(URI uri, boolean useCache) {
        this(uri, useCache, null, null);
    }

    public BasicAuthURI(URI uri, boolean useCache, String user, String password) {
        setUri(uri);
        setUseCache(useCache);
        if (user == null && password == null) {
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String[] userInfoArray = userInfo.split(":");
                user = userInfoArray[0];
                password = userInfoArray[1];
            }
        }
        setUser(user);
        setPassword(password);
    }

    public URI getUri() {
        return uri;
    }

    public BasicAuthURI uri(URI uri) {
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

    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public BasicAuthURI useCache(boolean useCache) {
        setUseCache(useCache);
        return this;
    }
}
