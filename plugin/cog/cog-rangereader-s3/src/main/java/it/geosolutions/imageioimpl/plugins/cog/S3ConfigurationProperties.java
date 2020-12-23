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


import it.geosolutions.imageio.core.BasicAuthURI;

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

    private final static String S3_DOT = "s3.";

    /** Some Old Regions support S3 dash Region endpoints, using a dash instead of a dot */
    private final static String S3_DASH = "s3-";

    /** Virtual-Hosted-Styles URL pieces */
    private final static String S3_DOT_VH = "." + S3_DOT;

    private final static String S3_DASH_VH = "." + S3_DASH;

    private final static String AMAZON_AWS = ".amazonaws.com";

    /**
     * Some more info on different types of supported URLs and old regions syntax:
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html
     */

    static abstract class S3URIParser {
        protected String region;

        protected String bucket;

        protected String key;

        protected String scheme;

        protected String host;

        protected URI uri;

        S3URIParser(URI uri, String defaultRegion) {
            this.uri = uri;
            this.region = defaultRegion;
            scheme = uri.getScheme().toLowerCase();
            host = uri.getHost();
        }
    }

    /**
     * A Parser dealing with path-style URLs
     *
     * http://s3.aws-region.amazonaws.com/bucket (S3 dot Regions)
     * http://s3-aws-region.amazonaws.com/bucket (S3 dash Regions)
     */
    class HTTPPathStyleParser extends S3ConfigurationProperties.S3URIParser {

        private final boolean isOldDashRegion;

        HTTPPathStyleParser(URI uri, String defaultRegion, boolean isOldDashRegion) {
            super(uri, defaultRegion);
            this.isOldDashRegion = isOldDashRegion;
            String hostRegion = host.substring(S3_DASH.length()).split("\\.")[0];
            if (hostRegion != null) region = hostRegion;
            String path = uri.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;
            bucket = path.split("/")[0];
            key = path.substring(bucket.length() + 1);
        }
    }

    /**
     * A Parser dealing with virtual-hosted-style URL
     *
     * http://bucket.s3.aws-region.amazonaws.com (S3 dot region)
     * http://bucket.s3-aws-region.amazonaws.com (S3 dash region)
     */
    class HTTPVirtualHostedStyleParser extends S3ConfigurationProperties.S3URIParser {

        private final boolean isOldDashRegion;

        HTTPVirtualHostedStyleParser(URI uri, String defaultRegion, boolean isOldDashRegion) {
            super(uri, defaultRegion);
            this.isOldDashRegion = isOldDashRegion;
            int domainIndex = host.indexOf(AMAZON_AWS);
            String s3Prefix = isOldDashRegion ? S3_DASH_VH : S3_DOT_VH;
            int s3Index = host.indexOf(s3Prefix);
            bucket = host.substring(0, s3Index);
            if (bucket.length() + s3Prefix.length() < domainIndex) {
                String hostRegion = host.substring(bucket.length() + s3Prefix.length(), domainIndex);
                if (hostRegion != null) region = hostRegion;
            }
            String path = uri.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;
            key = path;
        }
    }

    /**
     * A Parser dealing with S3 URLs
     *
     * s3://bucket/key?region=us-west-2 (Region as query param)
     * s3://bucket/key
     */
    class S3Parser extends S3ConfigurationProperties.S3URIParser {

        S3Parser(URI uri, String defaultRegion) {
            super(uri, defaultRegion);
            String path = uri.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;
            bucket = uri.getHost();
            key = path;
            String queryRegion = null;
            for (Map.Entry<String, List<String>> entry : splitQuery(uri).entrySet()) {
                if (entry.getKey().equalsIgnoreCase("region")) {
                    List<String> regions = entry.getValue();
                    if (regions != null && regions.size() > 0) {
                        queryRegion = regions.get(0);
                    }
                }
            }
            if (queryRegion != null) region = queryRegion;
        }
    }

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

    public final String AWS_S3_USER_KEY;
    public final String AWS_S3_PASSWORD_KEY;
    public final String AWS_S3_ENDPOINT_KEY;
    public final String AWS_S3_REGION_KEY;
    public final String AWS_S3_CORE_POOL_SIZE_KEY;
    public final String AWS_S3_MAX_POOL_SIZE_KEY;
    public final String AWS_S3_KEEP_ALIVE_TIME;

    private final static Logger LOGGER = Logger.getLogger(S3ConfigurationProperties.class.getName());

    public S3ConfigurationProperties(String alias, BasicAuthURI cogUri) {
        this.alias = alias.toUpperCase();
        AWS_S3_USER_KEY = "IIO_" + this.alias + "_AWS_USER";
        AWS_S3_PASSWORD_KEY = "IIO_" + this.alias + "_AWS_PASSWORD";
        AWS_S3_ENDPOINT_KEY = "IIO_" + this.alias + "_AWS_ENDPOINT";
        AWS_S3_REGION_KEY = "IIO_" + this.alias + "_AWS_REGION";
        AWS_S3_CORE_POOL_SIZE_KEY = "IIO_" + this.alias + "_AWS_CORE_POOL_SIZE";
        AWS_S3_MAX_POOL_SIZE_KEY = "IIO_" + this.alias + "_AWS_MAX_POOL_SIZE";
        AWS_S3_KEEP_ALIVE_TIME = "IIO_" + this.alias + "_AWS_KEEP_ALIVE_TIME";

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

        if (cogUri.getUser() != null && cogUri.getPassword()!= null) {
            user = cogUri.getUser();
            password = cogUri.getPassword();
        }

        S3URIParser parser = null;
        URI uri = cogUri.getUri();
        String scheme = uri.getScheme().toLowerCase();
        // if protocol is http, get the region from the host
        if (scheme.startsWith("http")) {
            String host = uri.getHost();
            String hostLowerCase = host.toLowerCase();
            if ((hostLowerCase.startsWith(S3_DASH) || hostLowerCase.startsWith(S3_DOT))  && host.contains(".")) {
                parser = new HTTPPathStyleParser(uri, region, hostLowerCase.startsWith(S3_DASH));
            } else if (hostLowerCase.contains(S3_DASH_VH) || hostLowerCase.contains(S3_DOT_VH)) {
                parser = new HTTPVirtualHostedStyleParser(uri, region, hostLowerCase.contains(S3_DASH_VH));
            }
        } else if (scheme.startsWith("s3")) {
            parser = new S3Parser(uri, region);
        }

        if (parser == null) {
            throw new RuntimeException("Unable to parse the specified URI: " + uri);
        }
        region = parser.region;
        // if region is null,
        if (region == null) {
            throw new RuntimeException("No region info found for alias " + this.alias
                    + ".  Please set environment variable '" + AWS_S3_REGION_KEY
                    + "' or system property '" + PropertyLocator.convertEnvVarToProperty(AWS_S3_REGION_KEY) + "'");
        }

        bucket = parser.bucket;
        key = parser.key;
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
}
