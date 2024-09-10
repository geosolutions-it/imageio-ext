/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
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

import it.geosolutions.imageio.core.ExtCaches;
import it.geosolutions.imageio.utilities.SoftValueHashMap;

import java.util.Map;

/**
 * Utility class to assist building Azure async client.
 * Azure clients should be singletons and re-used.
 */
public class AzureClientFactory {
    private static final Map<String, AzureClient> asyncClients = new SoftValueHashMap<>();

    static {
        ExtCaches.addListener(()->{
        	synchronized (asyncClients) {
            	asyncClients.clear();
			}
        });
    }

    private AzureClientFactory() {
    }

    public static AzureClient getClient(AzureConfigurationProperties configProps) {

        String container = configProps.getContainer();
        synchronized (asyncClients) {
            return asyncClients.computeIfAbsent(container, c->new AzureClient(configProps));
		}
    }
}
