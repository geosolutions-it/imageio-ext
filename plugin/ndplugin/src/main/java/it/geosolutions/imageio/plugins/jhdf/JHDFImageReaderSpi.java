package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.imageio.plugins.jhdf.pool.DatasetPoolManager;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

/**
 * WORK IN PROGRESS - NOT YET COMPLETED - ACTUALLY ABANDONED
 */

public class JHDFImageReaderSpi extends ImageReaderSpi{

	public final DatasetPoolManager dsPoolManager = new DatasetPoolManager();

	static final String[] suffixes = { "hdf"};

	static final String[] formatNames = { "HDF" , "HDF4", "HDF5" };

	static final String[] MIMETypes = { "image/hdf"};

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.JHDFImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = {null };

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

	public JHDFImageReaderSpi() {
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
	}

	/**
	 * This method checks if the provided input can be decoded from this SPI
	 */
	public boolean canDecodeInput(Object input) throws IOException {
		return true;
	}

	/**
	 * Returns an instance of the JHDFImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new JHDFImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return new StringBuffer("JHDF Image Reader, version ").append(version)
				.toString();
	}
	
}
