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
package it.geosolutions.imageioimpl.plugins.cog;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * Provides configuration properties for the OkHttp client. Attempts to read environment variables containing connection
 * settings and if not found, will fallback to attempting to read system properties. If still not found, the provided
 * default values will be used.
 *
 * @author joshfix Created on 10/23/19
 */
public class HttpConfigurationProperties {

    private int maxRequests;
    private int maxRequestsPerHost;
    private int maxIdleConnections;
    private int keepAliveDuration;
    private String httpProxyHost;
    private int httpProxyPort;

    public final String HTTP_MAX_REQUESTS = "IIO_HTTP_MAX_REQUESTS";
    public final String HTTP_MAX_REQUESTS_PER_HOST = "IIO_HTTP_MAX_REQUESTS_PER_HOST";
    public final String HTTP_MAX_IDLE_CONNECTIONS = "IIO_HTTP_MAX_IDLE_CONNECTIONS";
    public final String HTTP_KEEP_ALIVE_TIME = "IIO_HTTP_KEEP_ALIVE_TIME";
    public final String HTTP_PROXY_HOST = "HTTP_PROXY_HOST";
    public final String HTTP_PROXY_PORT = "HTTP_PROXY_PORT";

    public HttpConfigurationProperties() {
        maxRequests = Integer.parseInt(PropertyLocator.getEnvironmentValue(HTTP_MAX_REQUESTS, "128"));
        maxRequestsPerHost = Integer.parseInt(PropertyLocator.getEnvironmentValue(HTTP_MAX_REQUESTS_PER_HOST, "5"));
        maxIdleConnections = Integer.parseInt(PropertyLocator.getEnvironmentValue(HTTP_MAX_IDLE_CONNECTIONS, "5"));
        keepAliveDuration = Integer.parseInt(PropertyLocator.getEnvironmentValue(HTTP_KEEP_ALIVE_TIME, "60"));
        httpProxyHost = PropertyLocator.getEnvironmentValue(HTTP_PROXY_HOST, null);
        httpProxyPort = Integer.parseInt(PropertyLocator.getEnvironmentValue(HTTP_PROXY_PORT, "3128"));
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public Proxy getHttpProxy() {
        if (httpProxyHost != null) {
            SocketAddress addr = new InetSocketAddress(this.httpProxyHost, this.httpProxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            return proxy;
        } else {
            return null;
        }
    }
}
