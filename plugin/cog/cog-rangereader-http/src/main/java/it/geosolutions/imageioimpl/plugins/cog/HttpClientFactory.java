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

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 *  Utility class to assist building OkHttp client.  OkHttp clients should be singletons and re-used.
 *
 * @author joshfix
 * Created on 10/23/19
 */
public class HttpClientFactory {

    private static OkHttpClient client;
    private static HttpConfigurationProperties configProps = new HttpConfigurationProperties();

    public static OkHttpClient getClient() {
        if (client != null) {
            return client;
        }
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(configProps.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(configProps.getMaxRequestsPerHost());

        ConnectionPool connectionPool = new ConnectionPool(
                configProps.getMaxIdleConnections(),
                configProps.getKeepAliveDuration(),
                TimeUnit.SECONDS);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool);

        client = clientBuilder.build();
        return client;
    }
}
