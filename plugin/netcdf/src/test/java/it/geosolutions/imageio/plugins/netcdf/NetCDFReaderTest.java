/**
 * 
 */
package it.geosolutions.imageio.plugins.netcdf;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;

/**
 * @author Alessio
 * 
 */
public class NetCDFReaderTest {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.netcdf");

    private void warningMessage() {
        StringBuffer sb = new StringBuffer(
                "Test file not available. Test are skipped");
        LOGGER.info(sb.toString());
    }

//    private final static String filePath = "C:\\Work\\data\\rixen\\lsvc08\\SHOM";
    private final static String filePath = "y:";
    private final static String subPath = "\\";
        private final static String name = "HOPS1km_H_080929_T.nc";
            
    private final static String fileName = filePath+subPath+name;
//    final static String fileName = "ext-mercatorPsy2v3R1v_med_mean_20080903_R20080903.nc";

    private static final int LIMIT = 300;

    private static final int STEP = 10;

    @org.junit.Test
    public void testRead() throws IOException {
        File inputFile=new File(fileName);
        try {
	        inputFile = TestData.file(this, fileName);
	        if (!inputFile.exists()) {
	            warningMessage();
	            return;
	        }
        } catch (FileNotFoundException fnfe) {
        		warningMessage();
				return;
			}
        final ImageReader ncReader = new NetCDFImageReaderSpi().createReaderInstance();
        ncReader.setInput(inputFile);
        int numImages = ncReader.getNumImages(false);
        if (numImages / STEP > LIMIT)
            numImages = LIMIT * STEP;
        ImageIOUtilities.displayImageIOMetadata(ncReader.getStreamMetadata()
                .getAsTree(NetCDFStreamMetadata.nativeMetadataFormatName));
        ncReader.dispose();
        for (int i = 0; i < numImages; i += STEP) {
            ncReader.setInput(inputFile);
            final IIOMetadata metadata = ncReader.getImageMetadata(i);
            ImageIOUtilities
                    .displayImageIOMetadata(metadata
                            .getAsTree(CoreCommonImageMetadata.nativeMetadataFormatName));
            ImageIOUtilities.displayImageIOMetadata(metadata
                    .getAsTree(NetCDFImageMetadata.nativeMetadataFormatName));

            if (TestData.isInteractiveTest()) {
                ImageIOUtilities.visualize(ncReader.read(i),
                        ((NetCDFImageReader) ncReader).getVariableName(i));
            } else
                ncReader.read(i);
            ncReader.dispose();
        }
    }
}
