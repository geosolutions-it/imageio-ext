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

import com.microsoft.azure.storage.blob.BlobURLParts;
import com.microsoft.azure.storage.blob.URLParser;
import it.geosolutions.imageio.core.BasicAuthURI;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Helps locate configuration properties in system/environment for use in building Azure client.
 */
public class AzureConfigurationProperties {

    private static final String AZURE_ACCOUNT_NAME = "azure.reader.accountName";
    private static final String AZURE_ACCOUNT_KEY = "azure.reader.accountKey";
    private static final String AZURE_ACCOUNT_CONTAINER = "azure.reader.container";
    private static final String AZURE_ACCOUNT_PREFIX = "azure.reader.prefix";
    private static final String AZURE_MAX_CONNECTIONS = "azure.reader.maxConnections";

    private String container;
    private String prefix;
    private String accountName;
    private String accountKey;
    private Integer maxConnections = 64;
    private Boolean useHTTPS = Boolean.TRUE;
    private String serviceURL = null;

    AzureConfigurationProperties(BasicAuthURI cogUri) {
        URI uri = cogUri.getUri();

        // sample uri
        // https://myaccount.blob.core.windows.net/mycontainer/prefix/myblob.tif
        String path = uri.toASCIIString();

        if (path.startsWith("https")) {
            // replace for quicker finding
            path = path.replace("https", "http");
            useHTTPS = true;
        }

        BlobURLParts parts = null;
        try {
            parts = URLParser.parse(uri.toURL());
            container = parts.containerName();
            String blobName = parts.blobName();
            int lastIndex = blobName.lastIndexOf("/");
            if (lastIndex > 0) {
                prefix = blobName.substring(0, lastIndex);
            }
            String host = parts.host();
            int blobcoreIdx = host.indexOf("." + AzureClient.AZURE_URL_BASE);
            if (blobcoreIdx > 0) {
                accountName = host.substring(0, blobcoreIdx);
            }

        } catch (UnknownHostException| MalformedURLException e) {
            throw new RuntimeException("Unable to parse the provided uri " + path + "due to " + e.getLocalizedMessage());
        }

        if (container == null) {
            container = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_CONTAINER, null);
        }
        if (prefix == null) {
            prefix = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_PREFIX, null);
        }
        if (cogUri.getUser() != null && cogUri.getPassword()!= null) {
            accountName = cogUri.getUser();
            accountKey = cogUri.getPassword();
        }

        if (accountName == null) {
            accountName = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_NAME, null);
        }
        if (accountKey == null) {
            accountKey = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_KEY, null);
        }
        if (maxConnections == null) {
            maxConnections = Integer.parseInt(PropertyLocator.getEnvironmentValue(AZURE_MAX_CONNECTIONS, "5"));
        }

    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Boolean isUseHTTPS() {
        return useHTTPS;
    }

    public void setUseHTTPS(Boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }
}
