/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2020, GeoSolutions
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
package it.geosolutions.imageio.cog;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageioimpl.plugins.cog.S3ConfigurationProperties;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class S3UrlsTests {

    @Test
    public void testVirtualHostedStyle() {
        S3ConfigurationProperties config = new S3ConfigurationProperties("ALIAS",
                new BasicAuthURI("http://my-bucket.s3.eu-central-1.amazonaws.com/sampleFiles/myfile.jpeg"));
        Assert.assertEquals("my-bucket", config.getBucket());
        Assert.assertEquals("eu-central-1", config.getRegion());
        Assert.assertEquals("sampleFiles/myfile.jpeg", config.getKey());
        Assert.assertEquals("myfile.jpeg", config.getFilename());
    }

    @Test
    public void testVirtualHostedStyleNoRegion() {
        System.setProperty("iio.alias.aws.region","eu-central-1");
        S3ConfigurationProperties config = new S3ConfigurationProperties("ALIAS",
                new BasicAuthURI("http://my-bucket.s3.amazonaws.com/sampleFiles/myfile.jpeg"));
        Assert.assertEquals("my-bucket", config.getBucket());
        Assert.assertEquals("eu-central-1", config.getRegion());
        Assert.assertEquals("sampleFiles/myfile.jpeg", config.getKey());
        Assert.assertEquals("myfile.jpeg", config.getFilename());
    }

    @Test
    public void testVirtualHostedStyleOldRegion() {
        S3ConfigurationProperties config = new S3ConfigurationProperties("ALIAS",
                new BasicAuthURI("http://my-bucket.s3-us-west-2.amazonaws.com/sampleFiles/myfile.jpeg"));
        Assert.assertEquals("my-bucket", config.getBucket());
        Assert.assertEquals("us-west-2", config.getRegion());
        Assert.assertEquals("sampleFiles/myfile.jpeg", config.getKey());
        Assert.assertEquals("myfile.jpeg", config.getFilename());
    }

    @Test
    public void testPathStyleOldRegion() {
        S3ConfigurationProperties config = new S3ConfigurationProperties("ALIAS",
                new BasicAuthURI("http://s3-us-west-2.amazonaws.com/my-bucket/sampleFiles/myfile.jpeg"));
        Assert.assertEquals("my-bucket", config.getBucket());
        Assert.assertEquals("us-west-2", config.getRegion());
        Assert.assertEquals("sampleFiles/myfile.jpeg", config.getKey());
        Assert.assertEquals("myfile.jpeg", config.getFilename());
    }

    @Test
    public void testPathStyle() {
        S3ConfigurationProperties config = new S3ConfigurationProperties("ALIAS",
                new BasicAuthURI("http://s3.eu-central-1.amazonaws.com/my-bucket/sampleFiles/myfile.jpeg"));
        Assert.assertEquals("my-bucket", config.getBucket());
        Assert.assertEquals("eu-central-1", config.getRegion());
        Assert.assertEquals("sampleFiles/myfile.jpeg", config.getKey());
        Assert.assertEquals("myfile.jpeg", config.getFilename());
    }

    @Test
    public void testS3Url() {
        String cogUrl = "s3://landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";
        URI uri = URI.create(cogUrl);
        URI queryUri;
        String queryRegion = "region=us-west-2";
        try {
            queryUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), queryRegion, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        S3ConfigurationProperties config = new S3ConfigurationProperties("ALIAS", new BasicAuthURI(queryUri));
        Assert.assertEquals("landsat-pds", config.getBucket());
        Assert.assertEquals("us-west-2", config.getRegion());
        Assert.assertEquals("c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF", config.getKey());
        Assert.assertEquals("LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF", config.getFilename());
    }

}
