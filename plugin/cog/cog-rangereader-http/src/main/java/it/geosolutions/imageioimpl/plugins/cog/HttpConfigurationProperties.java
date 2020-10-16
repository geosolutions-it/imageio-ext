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

/**
 * Provides configuration properties for the OkHttp client.  Attempts to read environment variables containing
 * connection settings and if not found, will fallback to attempting to read system properties.  If still not found,
 * the provided default values will be used.
 *
 * @author joshfix
 * Created on 10/23/19
 */
public class HttpConfigurationProperties {

    private int maxRequests;
    private int maxRequestsPerHost;
    private int maxIdleConnections;
    private int keepAliveDuration;

    public final String HTTP_MAX_REQUESTS = "IIO_HTTP_MAX_REQUESTS";
    public final String HTTP_MAX_REQUESTS_PER_HOST = "IIO_HTTP_MAX_REQUESTS_PER_HOST";
    public final String HTTP_MAX_IDLE_CONNECTIONS = "IIO_HTTP_MAX_IDLE_CONNECTIONS";
    public final String HTTP_KEEP_ALIVE_TIME = "IIO_HTTP_KEEP_ALIVE_TIME";

    public HttpConfigurationProperties() {
        maxRequests = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(HTTP_MAX_REQUESTS, "128"));
        maxRequestsPerHost = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(HTTP_MAX_REQUESTS_PER_HOST, "5"));
        maxIdleConnections = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(HTTP_MAX_IDLE_CONNECTIONS, "5"));
        keepAliveDuration = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(HTTP_KEEP_ALIVE_TIME, "60"));
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
}
