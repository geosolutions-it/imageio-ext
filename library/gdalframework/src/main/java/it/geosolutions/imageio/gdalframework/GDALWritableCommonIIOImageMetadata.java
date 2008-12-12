/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.gdalframework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class extending {@link GDALCommonIIOImageMetadata} in order to provide write
 * capabilities to the metadata instance. It is worth to point out that this
 * class doesn't work on an underlying dataset. It simply allows to define a
 * {@link GDALImageWriter}'s understandable metadata object.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class GDALWritableCommonIIOImageMetadata extends
        GDALCommonIIOImageMetadata {

    public static final String nativeMetadataFormatName = "it.geosolutions.imageio.gdalframework.writablecommonImageMetadata_1.0";

    private static final String DEFAULT_DATASET_NAME = "dummydataset";

    /**
     * Default constructor of <code>GDALWritableCommonIIOImageMetadata</code>.
     */
    public GDALWritableCommonIIOImageMetadata() {
        this(DEFAULT_DATASET_NAME);
    }

    /**
     * Constructor of <code>GDALWritableCommonIIOImageMetadata</code>.
     */
    public GDALWritableCommonIIOImageMetadata(final String datasetName) {
        super(null, datasetName, false);
        gdalMetadataMap = Collections.synchronizedMap(new HashMap(2));
    }

    /**
     * Set the metadata for a specific domain.
     * 
     * @param metadataNameValuePairs
     *                a <code>Map</code> containing name-value pairs where
     *                each pair represents a metadata element.
     * @param domain
     *                the domain where the metadata need to be stored.
     * @see GDALUtilities.GDALMetadataDomain <BR>
     *      TODO: future version could check for already existent key or provide
     *      a step-to-step single metadata item setting
     * @throws IllegalArgumentException
     *                 in case the specified domain is unsupported.
     */
    public synchronized void setGdalMetadataDomain(Map metadataNameValuePairs,
            String domain) {
        if (domain == null
                || domain.length() > 0
                && (!domain.equals(GDALUtilities.GDALMetadataDomain.DEFAULT)
                        || !domain
                                .equals(GDALUtilities.GDALMetadataDomain.IMAGESTRUCTURE) || !domain
                        .startsWith(GDALUtilities.GDALMetadataDomain.XML_PREFIX)))
            throw new IllegalArgumentException("Unsupported domain");
        if (domain.equals(GDALUtilities.GDALMetadataDomain.DEFAULT)
                || domain.length() == 0)
            domain = GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP;
        gdalMetadataMap.put(domain, metadataNameValuePairs);
    }
}
