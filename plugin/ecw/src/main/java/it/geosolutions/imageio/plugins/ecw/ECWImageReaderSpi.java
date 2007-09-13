package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;

/**
 * Service provider interface for the ECW Image
 * 
 * @author Simone Giannecchini
 * @author Daniele Romagnoli
 * 
 */
public class ECWImageReaderSpi extends GDALImageReaderSpi {

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.ecw");

	static final String[] suffixes = { "ecw" };

	static final String[] formatNames = { "ECW" };

	static final String[] MIMETypes = { "image/ecw" };

	static final String version = "1.0";

	static final String readerCN = "javax.imageio.plugins.ecw.ECWImageReader";

	static final String vendorName = "RomagnoliD-GiannecchiniS";

	// writerSpiNames
	static final String[] wSN = {/* "javax.imageio.plugins.ecw.ECWImageWriterSpi" */null };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = null;

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = { null };

	static final String[] extraStreamMetadataFormatClassNames = { null };

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = null;

	static final String nativeImageMetadataFormatClassName = null;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public ECWImageReaderSpi() {
		super(
				vendorName,
				version,
				formatNames,
				suffixes,
				MIMETypes,
				readerCN, // readerClassName
				STANDARD_INPUT_TYPE,
				wSN, // writer Spi Names
				supportsStandardStreamMetadataFormat,
				nativeStreamMetadataFormatName,
				nativeStreamMetadataFormatClassName,
				extraStreamMetadataFormatNames,
				extraStreamMetadataFormatClassNames,
				supportsStandardImageMetadataFormat,
				nativeImageMetadataFormatName,
				nativeImageMetadataFormatClassName,
				extraImageMetadataFormatNames,
				extraImageMetadataFormatClassNames);

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("ECWImageReaderSpi Constructor");
		
		needsTileTuning=true;
		supportsSubDataSets=false;

	}

	/**
	 * This method checks if the provided input can be decoded from this SPI
	 */
	public boolean canDecodeInput(Object input) throws IOException {
		return super.canDecodeInput(input);
	}

	/**
	 * Returns an instance of the ECWImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new ECWImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return new StringBuffer("ECW Image Reader, version ").append(version)
				.toString();
	}

	protected String getSupportedFormats() {
		return "ECW";
	}

}
