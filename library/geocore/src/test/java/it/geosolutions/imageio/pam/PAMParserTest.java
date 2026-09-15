package it.geosolutions.imageio.pam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.FieldType;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.FieldUsage;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.GDALRasterAttributeTable;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Histograms;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Histograms.HistItem;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Metadata;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Metadata.MDI;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.TableType;
import it.geosolutions.resources.TestData;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PAMParserTest {

    private static final double DELTA = 1E-5;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testMarshalling() throws Exception {

        // Getting a parser
        final PAMParser parser = PAMParser.getInstance();
        final File sampleFile = TestData.file(this, "sample.tif.aux.xml");

        // Parsing the PAMDataset
        final PAMDataset dataset = parser.parsePAM(sampleFile);
        final List<PAMRasterBand> bands = dataset.getPAMRasterBand();
        assertEquals(3, bands.size());

        // Scan the first band
        final PAMRasterBand band = bands.get(0);
        assertEquals(1, (int) band.getBand());

        // Checking histogram properties
        final Histograms histograms = band.getHistograms();
        final HistItem histItem = histograms.getHistItem();
        assertEquals(256, histItem.getBucketCount());
        assertEquals(1, histItem.getIncludeOutOfRange());
        assertEquals(0, histItem.getApproximate());
        assertEquals(255.5, histItem.getHistMax().doubleValue(), DELTA);
        assertEquals(-0.5, histItem.getHistMin().doubleValue(), DELTA);

        // Checking metadata
        final Metadata metadata = band.getMetadata();
        final List<MDI> metadataItems = metadata.getMDI();
        assertEquals("LAYER_TYPE", metadataItems.get(0).getKey());
        assertEquals("athematic", metadataItems.get(0).getValue());
        assertEquals("STATISTICS_MINIMUM", metadataItems.get(1).getKey());
        assertEquals(0, Integer.parseInt(metadataItems.get(1).getValue()));
        assertEquals("STATISTICS_MAXIMUM", metadataItems.get(2).getKey());
        assertEquals(255, Integer.parseInt(metadataItems.get(2).getValue()));
        assertEquals("STATISTICS_MEAN", metadataItems.get(3).getKey());
        assertEquals(75.8095684, Double.parseDouble(metadataItems.get(3).getValue()), DELTA);
        assertEquals("STATISTICS_STDDEV", metadataItems.get(4).getKey());
        assertEquals(65.7914086, Double.parseDouble(metadataItems.get(4).getValue()), DELTA);
    }

    @Test
    public void testRasterAttributeTable() throws Exception {
        // Getting a parser
        final PAMParser parser = PAMParser.getInstance();
        final File sampleFile = TestData.file(this, "pam_rat.aux.xml");

        // Parsing the PAMDataset
        final PAMDataset dataset = parser.parsePAM(sampleFile);
        final List<PAMRasterBand> bands = dataset.getPAMRasterBand();
        assertEquals(1, bands.size());

        // Scan the first band
        final PAMRasterBand band = bands.get(0);
        assertEquals(1, (int) band.getBand());

        // Get the Raster Attribute Table
        GDALRasterAttributeTable rat = band.getGdalRasterAttributeTable();
        assertNotNull(rat);
        assertEquals(TableType.Thematic, rat.getTableType());

        // Check each field
        List<PAMRasterBand.FieldDefn> fields = rat.getFieldDefn();
        assertEquals(3, fields.size());
        assertField(fields.get(0), "con_min", FieldType.Real, FieldUsage.Min);
        assertField(fields.get(1), "con_max", FieldType.Real, FieldUsage.Max);
        assertField(fields.get(2), "test", FieldType.String, FieldUsage.Generic);

        // Check rows
        List<PAMRasterBand.Row> rows = rat.getRow();
        assertEquals(8, rows.size());

        // one sample row
        PAMRasterBand.Row row = rows.get(1);
        List<String> fieldValues = row.getF();
        assertEquals("1.4", fieldValues.get(0));
        assertEquals("1.6", fieldValues.get(1));
        assertEquals("white", fieldValues.get(2));
    }

    @Test
    public void testParsePamRejectsDoctype() throws Exception {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!DOCTYPE PAMDataset [<!ENTITY blocked \"x\">]>\n"
                        + "<PAMDataset>\n"
                        + "  <Metadata>&blocked;</Metadata>\n"
                        + "</PAMDataset>\n";

        Path tempFile = Files.createTempFile("pamparser-security-", ".pam");
        Files.writeString(tempFile, xml, StandardCharsets.UTF_8);

        try {
            assertThrows(Exception.class, () -> PAMParser.getInstance().parsePAM(tempFile.toFile()));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testRasterAttributeTableGdal312Types() throws Exception {
        final File sampleFile = TestData.file(this, "rat-gdal312-types.aux.xml");
        final PAMDataset dataset = PAMParser.getInstance().parsePAM(sampleFile);

        GDALRasterAttributeTable rat = assertGdal312Fields(dataset);

        List<PAMRasterBand.Row> rows = rat.getRow();
        assertEquals(4, rows.size());
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
        // the survey starting later carries an earlier looking date, the offsets differ
        assertEquals("2024-02-29T23:00:00.000-05:00", rows.get(1).getF().get(3));
        // GDAL offsets are not whole hours, the minutes are 0, 15, 30 or 45
        assertEquals("2025-01-02T23:59:59.000+01:15", rows.get(2).getF().get(3));
        // a survey that filled in nothing but its id: GDAL writes 0 for the numbers,
        // false for the boolean and an empty element for string, date and geometry
        assertEquals(
                List.of("61004", "0", "false", "", "", "", "0", ""), rows.get(3).getF());
    }

    @Test
    public void testGdal312TypesSurviveMarshalling() throws Exception {
        final File sampleFile = TestData.file(this, "rat-gdal312-types.aux.xml");
        final PAMDataset dataset = PAMParser.getInstance().parsePAM(sampleFile);

        final PAMDataset reparsed = PAMParser.getInstance().parsePAM(marshal(dataset));

        GDALRasterAttributeTable rat = assertGdal312Fields(reparsed);
        assertEquals(4, rat.getRow().size());
        // the empty cells survive as empty, not as a dropped or null element
        assertEquals(
                List.of("61004", "0", "false", "", "", "", "0", ""),
                rat.getRow().get(3).getF());
        assertEquals("POINT (2 3)", rat.getRow().get(2).getF().get(7));
    }

    @Test
    public void testUnknownFieldTypeReadAsString() throws Exception {
        final PAMDataset dataset = parseSingleField("<Type>9</Type>");
        assertField(singleField(dataset), "broken", FieldType.String, FieldUsage.Generic);

        // an unknown type is written back as String, the same way GDAL read it
        final PAMDataset reparsed = PAMParser.getInstance().parsePAM(marshal(dataset));
        assertEquals(FieldType.String, singleField(reparsed).getType());
    }

    @Test
    public void testBrokenFieldTypeReadAsString() throws Exception {
        assertEquals(
                FieldType.String,
                singleField(parseSingleField("<Type>not a number</Type>")).getType());
        assertEquals(
                FieldType.String, singleField(parseSingleField("<Type></Type>")).getType());
        assertEquals(FieldType.String, singleField(parseSingleField("<Type/>")).getType());
        assertEquals(
                FieldType.String,
                singleField(parseSingleField("<Type>-1</Type>")).getType());
        final String nil = "<Type xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>";
        assertEquals(FieldType.String, singleField(parseSingleField(nil)).getType());
    }

    @Test
    public void testTableType() throws Exception {
        assertEquals(TableType.Athematic, tableTypeOf("tableType=\"athematic\""));
        // GDAL compares the value case insensitively
        assertEquals(TableType.Athematic, tableTypeOf("tableType=\"Athematic\""));
        // and reads anything else as thematic
        assertEquals(TableType.Thematic, tableTypeOf("tableType=\"nonsense\""));
        // no attribute means no table type, so marshalling does not invent one
        assertNull(tableTypeOf(""));
    }

    /** Parses a table carrying the given tableType attribute, and returns the type it got. */
    private TableType tableTypeOf(String attribute) throws Exception {
        final File file = folder.newFile();
        Files.writeString(
                file.toPath(),
                """
                <PAMDataset>
                  <PAMRasterBand band="1">
                    <GDALRasterAttributeTable %s/>
                  </PAMRasterBand>
                </PAMDataset>
                """
                        .formatted(attribute),
                StandardCharsets.UTF_8);
        return PAMParser.getInstance()
                .parsePAM(file)
                .getPAMRasterBand()
                .get(0)
                .getGdalRasterAttributeTable()
                .getTableType();
    }

    /** Parses a one column table carrying the given Type element. */
    private PAMDataset parseSingleField(String typeElement) throws Exception {
        final File file = folder.newFile();
        Files.writeString(
                file.toPath(),
                """
                <PAMDataset>
                  <PAMRasterBand band="1">
                    <GDALRasterAttributeTable>
                      <FieldDefn index="0">
                        <Name>broken</Name>
                        %s
                        <Usage>0</Usage>
                      </FieldDefn>
                    </GDALRasterAttributeTable>
                  </PAMRasterBand>
                </PAMDataset>
                """
                        .formatted(typeElement),
                StandardCharsets.UTF_8);
        return PAMParser.getInstance().parsePAM(file);
    }

    private PAMRasterBand.FieldDefn singleField(PAMDataset dataset) {
        return dataset.getPAMRasterBand()
                .get(0)
                .getGdalRasterAttributeTable()
                .getFieldDefn()
                .get(0);
    }

    /** Asserts the eight field definitions of the GDAL 3.12 sample, and returns the table holding them. */
    private GDALRasterAttributeTable assertGdal312Fields(PAMDataset dataset) {
        final List<PAMRasterBand> bands = dataset.getPAMRasterBand();
        assertEquals(1, bands.size());
        GDALRasterAttributeTable rat = bands.get(0).getGdalRasterAttributeTable();
        assertNotNull(rat);
        assertEquals(TableType.Thematic, rat.getTableType());

        List<PAMRasterBand.FieldDefn> fields = rat.getFieldDefn();
        assertEquals(8, fields.size());
        assertField(fields.get(0), "id", FieldType.Integer, FieldUsage.MinMax);
        assertField(fields.get(1), "dataAssessment", FieldType.Integer, FieldUsage.Generic);
        assertField(fields.get(2), "fullSeafloorCoverageAchieved", FieldType.Boolean, FieldUsage.Generic);
        assertField(fields.get(3), "surveyDateRange.dateStart", FieldType.DateTime, FieldUsage.Generic);
        assertField(fields.get(4), "surveyDateRange.dateEnd", FieldType.DateTime, FieldUsage.Generic);
        assertField(fields.get(5), "surveyAuthority", FieldType.String, FieldUsage.Generic);
        assertField(fields.get(6), "depthRange.minimumDepth", FieldType.Real, FieldUsage.Generic);
        assertField(fields.get(7), "footprint", FieldType.WKBGeometry, FieldUsage.Generic);
        return rat;
    }

    /** Writes the dataset out the way the REST and mosaic code do, and returns the file it wrote. */
    private File marshal(PAMDataset dataset) throws Exception {
        final File file = folder.newFile();
        Marshaller marshaller = JAXBContext.newInstance(PAMDataset.class).createMarshaller();
        marshaller.marshal(dataset, file);
        return file;
    }

    private void assertField(PAMRasterBand.FieldDefn fieldDefn, String name, FieldType type, FieldUsage usage) {
        assertEquals(name, fieldDefn.getName());
        assertEquals(type, fieldDefn.getType());
        assertEquals(usage, fieldDefn.getUsage());
    }
}
