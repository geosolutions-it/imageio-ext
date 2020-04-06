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

import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;

/**
 * SPI for creating either `CachingCogImageInputStream` or `DefaultCogImageInputStream`.
 *
 * @author joshfix
 * Created on 2019-08-23
 */
public class CogImageInputStreamSpi extends ImageInputStreamSpi {

    private static final String vendorName = "GeoSolutions";
    private static final String version = "1.0";
    private static final Class<CogUri> inputClass = CogUri.class;

    public CogImageInputStreamSpi() {
        super(vendorName, version, inputClass);
    }

    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) throws IOException {
        if (input instanceof CogUri) {
            return ((CogUri) input).isUseCache()
                    ? new CachingCogImageInputStream((CogUri) input)
                    : new DefaultCogImageInputStream((CogUri) input);
        }

        if (input instanceof String || input instanceof URL || input instanceof URI) {
            return useCache
                    ? new CachingCogImageInputStream(new CogUri(input.toString()))
                    : new DefaultCogImageInputStream(new CogUri(input.toString()).useCache(false));
        }
        throw new IOException("Invalid input.");
    }

    @Override
    public String getDescription(Locale locale) {
        return "Cloud Optimized GeoTIFF reader";
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        super.onRegistration(registry, category);
        Class<ImageInputStreamSpi> targetClass = ImageInputStreamSpi.class;
        for (Iterator<? extends ImageInputStreamSpi> i =
             registry.getServiceProviders(targetClass, true); i.hasNext(); ) {
            ImageInputStreamSpi other = i.next();

            if (this != other)
                registry.setOrdering(targetClass, this, other);

        }
    }
}
