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

import it.geosolutions.imageio.gdalframework.AbstractGDALTest;
import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.Viewer;
import it.geosolutions.imageio.pam.PAMDataset;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.ParameterBlockImageN;
import org.eclipse.imagen.RenderedOp;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test reading RasterAttributeTable embedded inside VRT file
 */
public class RasterAttributeTableTest extends AbstractGDALTest {
    public RasterAttributeTableTest() {
        super();
    }


    @Test
    public void readImageIO() throws FileNotFoundException, IOException {
        if (!isGDALAvailable) {
            return;
        }
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

        // Check each field
        List<PAMDataset.PAMRasterBand.FieldDefn> fields = rat.getFieldDefn();
        assertEquals(3, fields.size());
        assertField(fields.get(0), "con_min", PAMDataset.PAMRasterBand.FieldType.Real,
                PAMDataset.PAMRasterBand.FieldUsage.Min);
        assertField(fields.get(1), "con_max", PAMDataset.PAMRasterBand.FieldType.Real,
                PAMDataset.PAMRasterBand.FieldUsage.Max);
        assertField(fields.get(2), "test", PAMDataset.PAMRasterBand.FieldType.String,
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


    private void assertField(PAMDataset.PAMRasterBand.FieldDefn fieldDefn, String name,
                             PAMDataset.PAMRasterBand.FieldType type,
                             PAMDataset.PAMRasterBand.FieldUsage usage) {
        assertEquals(name, fieldDefn.getName());
        assertEquals(type, fieldDefn.getType());
        assertEquals(usage, fieldDefn.getUsage());
    }
}

