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

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.utils.ThreadFactoryBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to assist building S3 async client.  S3 clients should be singletons and re-used.  We should also
 * maintain one client per region.
 *
 * @author joshfix
 * Created on 2019-09-19
 */
public class S3ClientFactory {

    private S3ClientFactory() {
    }

    private static Map<String, S3AsyncClient> s3AsyncClients = new HashMap<>();

    public static S3AsyncClient getS3Client(S3ConfigurationProperties configProps) {
        String region = configProps.getRegion();
        if (s3AsyncClients.containsKey(region)) {
            return s3AsyncClients.get(region);
        }

        S3AsyncClientBuilder builder = S3AsyncClient.builder();

        if (configProps.getUser() != null || configProps.getPassword() != null) {
            builder.credentialsProvider(() ->
                    AwsBasicCredentials.create(configProps.getUser(), configProps.getPassword()));
        } else {
            builder.credentialsProvider(() -> AnonymousCredentialsProvider.create().resolveCredentials());
        }

        if (configProps.getEndpoint() != null) {
            String endpoint = configProps.getEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint = endpoint + "/";
            }
            builder.endpointOverride(URI.create(endpoint));
        }
        if (configProps.getRegion() != null) {
            builder.region(Region.of(configProps.getRegion()));
        }

        // configure executor / thread pool
        builder.asyncConfiguration(b -> b.advancedOption(SdkAdvancedAsyncClientOption
                        .FUTURE_COMPLETION_EXECUTOR,
                getExecutor(configProps)
        ));

        S3AsyncClient s3AsyncClient = builder.build();
        s3AsyncClients.put(region, s3AsyncClient);
        return s3AsyncClient;
    }

    protected static ThreadPoolExecutor getExecutor(S3ConfigurationProperties configProps) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                configProps.getCorePoolSize(),
                configProps.getMaxPoolSize(),
                configProps.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10_000),
                new ThreadFactoryBuilder()
                        .threadNamePrefix("sdk-async-response-" + configProps.getRegion()).build());

        // Allow idle core threads to time out
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }
}
