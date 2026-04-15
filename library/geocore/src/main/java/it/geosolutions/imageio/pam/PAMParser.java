package it.geosolutions.imageio.pam;

import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Metadata;
import it.geosolutions.imageio.pam.PAMDataset.PAMRasterBand.Metadata.MDI;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Parsing class which allows to parse GDAL PAM Auxiliary files into PAMDataset objects.
 *
 * @author Daniele Romagnoli, GeoSolutions SAS
 */
public class PAMParser {

    private static PAMParser instance = null;

    public static synchronized PAMParser getInstance() {
        if (instance == null) {
            instance = new PAMParser();
        }
        return instance;
    }

    private static JAXBContext CONTEXT;

    private static final Logger LOGGER = Logger.getLogger(PAMParser.class.toString());

    static {
        try {
            CONTEXT = JAXBContext.newInstance("it.geosolutions.imageio.pam");
        } catch (JAXBException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }
    }

    public PAMDataset parsePAM(final String filePath) throws IOException {
        final File file = new File(filePath);
        return parsePAM(file);
    }

    public PAMDataset parsePAM(final File file) throws IOException {
        PAMDataset pamDataset;
        try {
            pamDataset = unmarshal(file);
        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            throw new IOException("Exception occurred while parsing the file", e);
        }
        return pamDataset;
    }

    /**
     * Unmarshal the file and return and PAMDataset object.
     *
     * @param pamFile
     * @return
     * @throws JAXBException
     */
    private PAMDataset unmarshal(final File pamFile)
            throws JAXBException, SAXException, ParserConfigurationException, IOException {
        PAMDataset pamDataset = null;
        if (pamFile != null) {
            Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            // Prevent any external entity resolution
            xmlReader.setEntityResolver((publicId, systemId) -> new InputSource(new java.io.StringReader("")));
            try (FileInputStream fis = new FileInputStream(pamFile)) {
                InputSource inputSource = new InputSource(fis);
                SAXSource source = new SAXSource(xmlReader, inputSource);
                pamDataset = (PAMDataset) unmarshaller.unmarshal(source);
            }
        }
        return pamDataset;
    }

    /**
     * Return the value of a metadata string for the provided rasterBand.
     *
     * @param rasterBand the {@link PAMRasterBand} to be checked.
     * @param key The key of the metadata property to be retrieved
     * @return
     */
    public String getMetadataValue(final PAMRasterBand rasterBand, final String key) {
        if (rasterBand == null) {
            throw new IllegalArgumentException("Specified raster band is null");
        }
        Metadata metadata = rasterBand.getMetadata();
        if (metadata == null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("No metadata have been found from the provided Raster band");
            }
            return null;
        }
        List<MDI> mdis = metadata.getMDI();
        if (mdis == null || mdis.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("No metadata have been found from the provided Raster band");
            }
            return null;
        }
        for (MDI mdi : mdis) {
            if (mdi.getKey().equalsIgnoreCase(key)) {
                return mdi.getValue();
            }
        }
        return null;
    }
}
