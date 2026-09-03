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

import com.azure.storage.blob.BlobUrlParts;
import it.geosolutions.imageio.core.BasicAuthURI;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * Helps locate configuration properties in system/environment for use in building Azure client.
 *
 * <p>Supported properties:
 *
 * <ul>
 *   <li>azure.reader.accountName - Azure storage account name
 *   <li>azure.reader.accountKey - Azure storage account key
 *   <li>azure.reader.sasToken - Azure Shared Access Signature token
 *   <li>azure.reader.container - Azure blob container name
 *   <li>azure.reader.prefix - Blob name prefix
 *   <li>azure.reader.maxConnections - Maximum number of connections
 *   <li>azure.reader.serviceurl - Custom service URL (useful for Azurite or other Azure-compatible storage emulators)
 * </ul>
 */
public class AzureConfigurationProperties {

    private static final String AZURE_ACCOUNT_NAME = "azure.reader.accountName";
    private static final String AZURE_ACCOUNT_KEY = "azure.reader.accountKey";
    private static final String AZURE_SAS_TOKEN = "azure.reader.sasToken";
    private static final String AZURE_ACCOUNT_CONTAINER = "azure.reader.container";
    private static final String AZURE_ACCOUNT_PREFIX = "azure.reader.prefix";
    private static final String AZURE_MAX_CONNECTIONS = "azure.reader.maxConnections";

    private String container;
    private String prefix;
    private String accountName;
    private String accountKey;
    private String sasToken;
    private Integer maxConnections = 64;
    private boolean useHTTPS = true;
    private String serviceURL = null;

    AzureConfigurationProperties(BasicAuthURI cogUri) {
        URI uri = cogUri.getUri();

        // sample uri
        // https://myaccount.blob.core.windows.net/mycontainer/prefix/myblob.tif
        String path = uri.toASCIIString();

        if (path.startsWith("https")) {
            // replace for quicker finding
            path = path.replaceFirst("https", "http");
            useHTTPS = true;
        }

        try {
            BlobUrlParts parts = BlobUrlParts.parse(uri.toURL());
            container = parts.getBlobContainerName();
            String blobName = parts.getBlobName();
            int lastIndex = blobName.lastIndexOf("/");
            if (lastIndex > 0) {
                prefix = blobName.substring(0, lastIndex);
            }
            String host = parts.getHost();
            int blobcoreIdx = host.indexOf("." + AzureClient.AZURE_URL_BASE);
            if (blobcoreIdx > 0) {
                accountName = host.substring(0, blobcoreIdx);
            }

        } catch (IllegalStateException | MalformedURLException e) {
            throw new RuntimeException(
                    "Unable to parse the provided uri " + path + "due to " + e.getLocalizedMessage());
        }

        if (container == null) { // REVISIT: dead code
            container = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_CONTAINER, null);
        }
        if (prefix == null) { // REVISIT: dead code
            prefix = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_PREFIX, null);
        }
        if (cogUri.getUser() != null && cogUri.getPassword() != null) {
            accountName = cogUri.getUser();
            setCredential(cogUri.getPassword());
        }

        if (accountName == null) { // REVISIT: dead code
            accountName = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_NAME, null);
        }
        if (accountKey == null && sasToken == null) {
            String configuredSasToken = PropertyLocator.getPropertyValue(AZURE_SAS_TOKEN, null);
            if (configuredSasToken != null) {
                setSasToken(configuredSasToken);
            } else {
                accountKey = PropertyLocator.getEnvironmentValue(AZURE_ACCOUNT_KEY, null);
            }
        }
        if (maxConnections == null) { // REVISIT: dead code
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

    public String getSasToken() {
        return sasToken;
    }

    public void setSasToken(String sasToken) {
        this.sasToken = normalizeSasToken(sasToken);
    }

    private void setCredential(String credential) {
        if (isSasToken(credential)) {
            setSasToken(credential);
        } else {
            accountKey = credential;
        }
    }

    static boolean isSasToken(String credential) {
        return getSasParameter(credential, "sig") != null;
    }

    boolean canReadContainerProperties() {
        if (sasToken == null) {
            return true;
        }
        // Account SAS must include Blob (ss=b), container (srt=c), and read (sp=r) access.
        return sasParameterContains("ss", 'b') && sasParameterContains("srt", 'c') && sasParameterContains("sp", 'r');
    }

    private boolean sasParameterContains(String name, char value) {
        String parameterValue = getSasParameter(sasToken, name);
        return parameterValue != null && parameterValue.indexOf(value) >= 0;
    }

    private static String getSasParameter(String sasToken, String parameterName) {
        String normalized = normalizeSasToken(sasToken);
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        for (String parameter : normalized.split("&")) {
            int separator = parameter.indexOf('=');
            String name = separator >= 0 ? parameter.substring(0, separator) : parameter;
            if (parameterName.equalsIgnoreCase(name)) {
                return separator >= 0 ? parameter.substring(separator + 1) : "";
            }
        }
        return null;
    }

    private static String normalizeSasToken(String sasToken) {
        if (sasToken == null) {
            return null;
        }
        String normalized = sasToken.trim();
        return normalized.startsWith("?") ? normalized.substring(1) : normalized;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean isUseHTTPS() {
        return useHTTPS;
    }

    public void setUseHTTPS(boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }
}
