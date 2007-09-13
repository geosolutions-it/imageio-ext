package it.geosolutions.imageio.plugins.jhdf;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

public abstract class BaseHDFImageReaderSpi extends ImageReaderSpi {

	protected final static int[] spiMutex = new int[] { 0 };
	
	public BaseHDFImageReaderSpi(final String vendorName, String version,
			String[] formatNames, String[] suffixes, String[] mimeTypes,
			String readerCN, Class[] standard_input_type, String[] wsn,
			boolean supportsStandardStreamMetadataFormat,
			String nativeStreamMetadataFormatName,
			String nativeStreamMetadataFormatClassName,
			String[] extraStreamMetadataFormatNames,
			String[] extraStreamMetadataFormatClassNames,
			boolean supportsStandardImageMetadataFormat,
			String nativeImageMetadataFormatName,
			String nativeImageMetadataFormatClassName,
			String[] extraImageMetadataFormatNames,
			String[] extraImageMetadataFormatClassNames) {
		super(vendorName, version, formatNames, suffixes, mimeTypes, readerCN,
				standard_input_type, wsn, supportsStandardStreamMetadataFormat,
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
	public abstract boolean canDecodeInput(final Object input) throws IOException;
	
	/**
	 * Returns an instance of the BaseHDFImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public abstract ImageReader createReaderInstance(Object source) throws IOException;

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public abstract String getDescription(Locale locale);

}
