package it.geosolutions.imageioimpl.plugins.cog;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

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

        if (region == null) {
            throw new RuntimeException("No region info found for alias " + alias + ".  Please set system property "
                    + AWS_S3_REGION_KEY + " or environment variable "
                    + PropertyLocator.convertPropertytoEnvVar(AWS_S3_REGION_KEY));
        }

        String[] parts = uri.toString().split("/");
        if (alias.startsWith("http")) {
            bucket = parts[parts.length - 2];
            key = parts[parts.length - 1];
        } else {
            StringBuilder keyBuilder = new StringBuilder();
            bucket = parts[2];
            for (int i = 3; i < parts.length; i++) {
                try {
                    keyBuilder.append("/").append(URLDecoder.decode(parts[i], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Unable to decode key part " + parts[i] + " for URL " + uri);
                }
            }
            key = keyBuilder.toString();
            /* Strip leading slash */
            key = key.startsWith("/") ? key.substring(1) : key;
        }

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
