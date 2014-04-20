package it.geosolutions.imageio.pam;

import static org.junit.Assert.assertEquals;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Histograms;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Histograms.HistItem;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Metadata;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Metadata.MDI;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class PAMParserTest {

    private final static double DELTA = 1E-5;

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
}
