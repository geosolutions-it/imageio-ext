/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2019, Open Source Geospatial Foundation (OSGeo)
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
 *
 */

package it.geosolutions.imageioimpl.plugins.tiff.gdal;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import it.geosolutions.imageio.pam.PAMParser;

/**
 * Utility class to parse GDALMetadata objects from XML strings contained in the TIFF tags
 */
public class GDALMetadataParser {

    private static JAXBContext CONTEXT;

    private static final Logger LOGGER = Logger.getLogger(PAMParser.class.toString());

    static {
        try {
            CONTEXT = JAXBContext.newInstance(GDALMetadata.class, GDALMetadata.Item.class);
        } catch (JAXBException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }
    }

    /**
     * Parses the provided XML into a {@link GDALMetadata}
     *
     * @param xml the source XML
     * @return the parsed {@link GDALMetadata}
     * @throws JAXBException if the source is not compliant with the expected XML structure
     */
    public static GDALMetadata parse(String xml) throws JAXBException {
        Unmarshaller unmarshaller = null;
        GDALMetadata metadata = null;
        if (xml != null) {
            unmarshaller = CONTEXT.createUnmarshaller();
            metadata = (GDALMetadata) unmarshaller.unmarshal(new StringReader(xml));
        }
        return metadata;
    }
}
