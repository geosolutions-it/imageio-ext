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
