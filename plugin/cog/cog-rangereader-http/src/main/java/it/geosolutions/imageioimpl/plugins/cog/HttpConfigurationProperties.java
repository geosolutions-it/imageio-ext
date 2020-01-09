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
                PropertyLocator.getPropertyValue(HTTP_MAX_REQUESTS, "128"));
        maxRequestsPerHost = Integer.parseInt(
                PropertyLocator.getPropertyValue(HTTP_MAX_REQUESTS_PER_HOST, "5"));
        maxIdleConnections = Integer.parseInt(
                PropertyLocator.getPropertyValue(HTTP_MAX_IDLE_CONNECTIONS, "5"));
        keepAliveDuration = Integer.parseInt(
                PropertyLocator.getPropertyValue(HTTP_KEEP_ALIVE_TIME, "60"));
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
