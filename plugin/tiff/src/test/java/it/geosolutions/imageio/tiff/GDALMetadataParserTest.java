/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.tiff;

import static org.junit.Assert.*;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.util.List;

import it.geosolutions.imageioimpl.plugins.tiff.gdal.GDALMetadata;
import it.geosolutions.imageioimpl.plugins.tiff.gdal.GDALMetadataParser;

public class GDALMetadataParserTest {
    
    @Test
    public void testParseXML() throws Exception {
        String xml = IOUtils.toString(getClass().getResourceAsStream("test-data/gdalmetadata.xml"), "UTF-8");
        GDALMetadata metadata = GDALMetadataParser.parse(xml);

        // check we got the list
        List<GDALMetadata.Item> items = metadata.getItems();
        assertEquals(19, items.size());
        
        // check a few just to make sure all elements/attributes are getting parsed correctly
        assertEquals("Band_1", items.get(0).getName());
        assertEquals("Max Band_1", items.get(0).getValue());
        assertNull(items.get(0).getSample());
        assertNull(items.get(0).getRole());

        assertEquals("OFFSET", items.get(7).getName());
        assertEquals("0", items.get(7).getValue());
        assertEquals(Integer.valueOf(0), items.get(7).getSample());
        assertEquals("offset", items.get(7).getRole());
    }
}
