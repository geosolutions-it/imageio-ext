/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2023, GeoSolutions
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
package it.geosolutions.imageio.plugins.vrt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.pam.PAMDataset;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.TableType;
import it.geosolutions.resources.TestData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import org.junit.Test;

/** Test reading RasterAttributeTable embedded inside VRT file */
public class RasterAttributeTableTest extends AbstractGDALTest {
    public RasterAttributeTableTest() {
        super();
    }

    @Test
    public void readImageIO() throws FileNotFoundException, IOException {
        assumeTrue("GDAL library is not available", isGDALAvailable);
        final File file = TestData.file(this, "095b_dem_90m.asc.vrt");

        final Iterator<ImageReader> it = ImageIO.getImageReaders(file);
        assertTrue(it.hasNext());
        final ImageReader reader = (ImageReader) it.next();
        assertTrue(reader instanceof VRTImageReader);
        reader.setInput(file);

        IIOMetadata genericMeta = reader.getImageMetadata(0);
        assertTrue(genericMeta instanceof GDALCommonIIOImageMetadata);

        GDALCommonIIOImageMetadata gdalMeta = (GDALCommonIIOImageMetadata) genericMeta;

        PAMDataset pam = gdalMeta.getPamDataset();
        assertNotNull(pam);

        PAMDataset.PAMRasterBand band = pam.getPAMRasterBand().get(0);
        PAMDataset.PAMRasterBand.GDALRasterAttributeTable rat = band.getGdalRasterAttributeTable();
        assertNotNull(rat);
        assertEquals(TableType.Thematic, rat.getTableType());

        // Check each field
        List<PAMDataset.PAMRasterBand.FieldDefn> fields = rat.getFieldDefn();
        assertEquals(3, fields.size());
        assertField(
                fields.get(0),
                "con_min",
                PAMDataset.PAMRasterBand.FieldType.Real,
                PAMDataset.PAMRasterBand.FieldUsage.Min);
        assertField(
                fields.get(1),
                "con_max",
                PAMDataset.PAMRasterBand.FieldType.Real,
                PAMDataset.PAMRasterBand.FieldUsage.Max);
        assertField(
                fields.get(2),
                "test",
                PAMDataset.PAMRasterBand.FieldType.String,
                PAMDataset.PAMRasterBand.FieldUsage.Generic);

        // Check rows
        List<PAMDataset.PAMRasterBand.Row> rows = rat.getRow();
        assertEquals(8, rows.size());

        // one sample row
        PAMDataset.PAMRasterBand.Row row = rows.get(1);
        List<String> fieldValues = row.getF();
        assertEquals("1.4", fieldValues.get(0));
        assertEquals("1.6", fieldValues.get(1));
        assertEquals("white", fieldValues.get(2));

        reader.dispose();
    }

    @Test
    public void readGdal312FieldTypes() throws FileNotFoundException, IOException {
        assumeTrue("GDAL library is not available", isGDALAvailable);
        final File file = TestData.file(this, "rat-gdal312-types.vrt");

        final ImageReader reader = ImageIO.getImageReaders(file).next();
        reader.setInput(file);
        try {
            GDALCommonIIOImageMetadata gdalMeta = (GDALCommonIIOImageMetadata) reader.getImageMetadata(0);
            PAMDataset.PAMRasterBand.GDALRasterAttributeTable rat =
                    gdalMeta.getPamDataset().getPAMRasterBand().get(0).getGdalRasterAttributeTable();
            assertNotNull(rat);
            assertEquals(TableType.Thematic, rat.getTableType());

            List<PAMDataset.PAMRasterBand.FieldDefn> fields = rat.getFieldDefn();
            assertEquals(8, fields.size());
            assertField(
                    fields.get(2),
                    "fullSeafloorCoverageAchieved",
                    PAMDataset.PAMRasterBand.FieldType.Boolean,
                    PAMDataset.PAMRasterBand.FieldUsage.Generic);
            assertField(
                    fields.get(3),
                    "surveyDateRange.dateStart",
                    PAMDataset.PAMRasterBand.FieldType.DateTime,
                    PAMDataset.PAMRasterBand.FieldUsage.Generic);
            assertField(
                    fields.get(7),
                    "footprint",
                    PAMDataset.PAMRasterBand.FieldType.WKBGeometry,
                    PAMDataset.PAMRasterBand.FieldUsage.Generic);

            List<PAMDataset.PAMRasterBand.Row> rows = rat.getRow();
            assertEquals(3, rows.size());
            assertEquals(
                    List.of(
                            "54602",
                            "1",
                            "true",
                            "2024-03-01T00:00:00.000+00:00",
                            "2024-03-14T23:59:59.000+00:00",
                            "NOAA",
                            "4.75",
                            "POLYGON ((0 0,1 0,1 1,0 1,0 0))"),
                    rows.get(0).getF());
        } finally {
            reader.dispose();
        }
    }

    private void assertField(
            PAMDataset.PAMRasterBand.FieldDefn fieldDefn,
            String name,
            PAMDataset.PAMRasterBand.FieldType type,
            PAMDataset.PAMRasterBand.FieldUsage usage) {
        assertEquals(name, fieldDefn.getName());
        assertEquals(type, fieldDefn.getType());
        assertEquals(usage, fieldDefn.getUsage());
    }
}
