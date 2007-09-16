package it.geosolutions.imageio.plugins.grib1;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import net.sourceforge.jgrib.GribFile;

/**
 * Service provider interface for the GRIB1 Image
 * 
 * @author Daniele Romagnoli
 */
public class GRIB1ImageReaderSpi extends ImageReaderSpi {

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.grib1");

	static final String[] suffixes = { "grib", "grb" };

	static final String[] formatNames = { "GRIB1" };

	static final String[] MIMETypes = { "image/grib", "image/grb" };

	static final String version = "1.0";

	static final String readerCN = "javax.imageio.plugins.grib.GRIB1ImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = {/* "javax.imageio.plugins.grib1.GRIB1ImageWriterSpi" */null };

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

	public GRIB1ImageReaderSpi() {
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
			LOGGER.fine("GRIB1ImageReaderSpi Constructor");

	}

	public boolean canDecodeInput(Object input) throws IOException {
		return GribFile.canDecodeInput(input);
	}

	public ImageReader createReaderInstance(Object input) throws IOException {
		return new GRIB1ImageReader(this);
	}

	public String getDescription(Locale locale) {
		return "GRIB1 Image Reader, version " + version;
	}
}
