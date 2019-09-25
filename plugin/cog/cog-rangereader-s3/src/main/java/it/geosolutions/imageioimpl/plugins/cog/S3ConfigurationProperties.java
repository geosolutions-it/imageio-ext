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

/**
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

    public final String AWS_S3_USER_KEY;
    public final String AWS_S3_PASSWORD_KEY;
    public final String AWS_S3_ENDPOINT_KEY;
    public final String AWS_S3_REGION_KEY;

    public S3ConfigurationProperties(String alias, URI uri) {
        this.alias = alias;
        AWS_S3_USER_KEY = alias + ".aws.user";
        AWS_S3_PASSWORD_KEY = alias + ".aws.password";
        AWS_S3_ENDPOINT_KEY = alias + ".aws.endpoint";
        AWS_S3_REGION_KEY = alias + ".aws.region";

        user = PropertyLocator.getPropertyValue(AWS_S3_USER_KEY, null);
        password = PropertyLocator.getPropertyValue(AWS_S3_PASSWORD_KEY, null);
        region = PropertyLocator.getPropertyValue(AWS_S3_REGION_KEY, null);
        endpoint = PropertyLocator.getPropertyValue(AWS_S3_ENDPOINT_KEY, null);

        if (user == null && password == null) {
            String userPass = uri.getUserInfo();
            if (userPass != null && !userPass.isEmpty()) {
                String[] userPassArray = userPass.split(":");
                user = userPassArray[0];
                password = userPassArray[1];
            }
        }

        String scheme = uri.getScheme().toLowerCase();
        if (scheme.startsWith("http")) {
            String host = uri.getHost();
            if (host.toLowerCase().startsWith("s3-") && host.contains(".")) {
                region = host.substring(3).split("\\.")[0];
            }
        }

        if (region == null) {
            throw new RuntimeException("No region info found for alias " + alias + ".  Please set system property "
                    + AWS_S3_REGION_KEY + " or environment variable "
                    + PropertyLocator.convertPropertyToEnvVar(AWS_S3_REGION_KEY));
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
}
