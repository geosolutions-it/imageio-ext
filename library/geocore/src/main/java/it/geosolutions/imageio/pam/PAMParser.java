package it.geosolutions.imageio.pam;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


/**
 * Parsing class which allows to parse GDAL PAM Auxiliary files into  PAMDataset objects.
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 */
public class PAMParser {

    private static PAMParser instance = null;

    public static synchronized PAMParser getInstance () {
        if (instance == null) {
            instance = new PAMParser();
        }
        return instance;
    }

    private static JAXBContext CONTEXT;

    private final static Logger LOGGER = Logger.getLogger(PAMParser.class.toString());

    static {
        try 
        {
            CONTEXT = JAXBContext.newInstance("it.geosolutions.imageio.pam");
        } catch (JAXBException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }
    }

    public PAMDataset parsePAM(final String filePath) throws JAXBException {
        final File file = new File(filePath);
        return parsePAM(file);
    }

    public PAMDataset parsePAM(final File file) throws JAXBException {
        PAMDataset pamDataset = unmarshal(file);
        return pamDataset;
    }

    /**
     * Unmarshal the file and return and PAMDataset object.
     * 
     * @param pamFile
     * @return
     * @throws JAXBException
     */
    private PAMDataset unmarshal(final File pamFile) throws JAXBException {
        Unmarshaller unmarshaller = null;
        PAMDataset  pamDataset = null;
        if (pamFile != null) {
            unmarshaller = CONTEXT.createUnmarshaller();
            pamDataset = (PAMDataset) unmarshaller.unmarshal(pamFile);
        }
        return pamDataset;
    }
}
