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

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;

import java.net.URI;

/**
 * @author joshfix
 * Created on 2019-09-19
 */
public class S3ClientFactory {

    public static S3AsyncClient getS3Client(S3ConfigurationProperties configProps) {
        S3AsyncClientBuilder builder = S3AsyncClient.builder();

        if (configProps.getUser() != null || configProps.getPassword() != null) {
            builder.credentialsProvider(() ->
                    AwsBasicCredentials.create(configProps.getUser(), configProps.getPassword()));
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

        return builder.build();
    }
}
