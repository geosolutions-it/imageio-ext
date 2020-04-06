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
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;

/**
 * Helps locate configuration properties in system/environment for use in building S3 client.  Attempts to read
 * environment variables containing connection settings and if not found, will fallback to attempting to read system
 * properties.  If still not found, the provided default values will be used.
 *
 * @author joshfix
 * Created on 2019-09-19
 */
public class S3ConfigurationProperties {

    private String user;
    private String password;
    private String endpoint;
    private String region;
    private String alias;
    private String bucket;
    private String key;
    private String filename;
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
	private String requestPayer;

    public final String AWS_S3_USER_KEY;
    public final String AWS_S3_PASSWORD_KEY;
    public final String AWS_S3_ENDPOINT_KEY;
    public final String AWS_S3_REGION_KEY;
    public final String AWS_S3_CORE_POOL_SIZE_KEY;
    public final String AWS_S3_MAX_POOL_SIZE_KEY;
    public final String AWS_S3_KEEP_ALIVE_TIME;
	public final String AWS_S3_REQUEST_PAYER;

    private final static Logger LOGGER = Logger.getLogger(S3ConfigurationProperties.class.getName());

    public S3ConfigurationProperties(String alias, URI uri) {
        this.alias = alias.toUpperCase();
        AWS_S3_USER_KEY = "IIO_" + this.alias + "_AWS_USER";
        AWS_S3_PASSWORD_KEY = "IIO_" + this.alias + "_AWS_PASSWORD";
        AWS_S3_ENDPOINT_KEY = "IIO_" + this.alias + "_AWS_ENDPOINT";
        AWS_S3_REGION_KEY = "IIO_" + this.alias + "_AWS_REGION";
        AWS_S3_CORE_POOL_SIZE_KEY = "IIO_" + this.alias + "_AWS_CORE_POOL_SIZE";
        AWS_S3_MAX_POOL_SIZE_KEY = "IIO_" + this.alias + "_AWS_MAX_POOL_SIZE";
        AWS_S3_KEEP_ALIVE_TIME = "IIO_" + this.alias + "_AWS_KEEP_ALIVE_TIME";
		AWS_S3_REQUEST_PAYER = "IIO_" + this.alias + "_AWS_REQUEST_PAYER";

        user = PropertyLocator.getEnvironmentValue(AWS_S3_USER_KEY, null);
        password = PropertyLocator.getEnvironmentValue(AWS_S3_PASSWORD_KEY, null);
        region = PropertyLocator.getEnvironmentValue(AWS_S3_REGION_KEY, null);
        endpoint = PropertyLocator.getEnvironmentValue(AWS_S3_ENDPOINT_KEY, null);
        corePoolSize = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(AWS_S3_CORE_POOL_SIZE_KEY, "50"));
        maxPoolSize = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(AWS_S3_MAX_POOL_SIZE_KEY, "128"));
        keepAliveTime = Integer.parseInt(
                PropertyLocator.getEnvironmentValue(AWS_S3_KEEP_ALIVE_TIME, "10"));
		requestPayer = PropertyLocator.getEnvironmentValue(AWS_S3_REQUEST_PAYER, null);

        String userPass = uri.getUserInfo();
        if (userPass != null && !userPass.isEmpty()) {
            String[] userPassArray = userPass.split(":");
            user = userPassArray[0];
            password = userPassArray[1];
        }

        // if protocol is http, get the region from the host
        String scheme = uri.getScheme().toLowerCase();
        if (scheme.startsWith("http")) {
            String host = uri.getHost();
            if (host.toLowerCase().startsWith("s3-") && host.contains(".")) {
                region = host.substring(3).split("\\.")[0];
            }
        }

        // if region is null, try to get the region from a URL parameter
        if (region == null) {
            for (Map.Entry<String, List<String>> entry : splitQuery(uri).entrySet()) {
                if (entry.getKey().equalsIgnoreCase("region")) {
                    List<String> regions = entry.getValue();
                    if (regions != null && regions.size() > 0) {
                        region = regions.get(0);
                    }
                }
            }
        }

        // if region is null,
        if (region == null) {
            throw new RuntimeException("No region info found for alias " + this.alias
                    + ".  Please set environment variable '" + AWS_S3_REGION_KEY
                    + "' or system property '" + PropertyLocator.convertEnvVarToProperty(AWS_S3_REGION_KEY) + "'");
        }

        String path = uri.getPath();
        path = path.startsWith("/") ? path.substring(1) : path;
        bucket = scheme.startsWith("http") ? path.split("/")[0] : uri.getHost();
        key = scheme.startsWith("http") ? path.substring(bucket.length() + 1) : path;
        filename = nameFromKey(key);
    }

    private String nameFromKey(String key) {
        String[] parts = key.split("/");
        return parts[parts.length - 1];
    }

    public static Map<String, List<String>> splitQuery(URI uri)  {
        try {
            final Map<String, List<String>> query_pairs = new LinkedHashMap<>();
            final String[] pairs = uri.getQuery().split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.get(key).add(value);
            }
            return query_pairs;
        } catch (Exception e) {
            LOGGER.warning("Unable to split query into key/value pairs for URI " + uri + ". "  + e.getMessage());
            return Collections.emptyMap();
        }
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getRegion() {
        return region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAlias() {
        return alias;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    public String getFilename() {
        return filename;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }
	
	public String getRequestPayer() {
        return requestPayer;
    }
}
