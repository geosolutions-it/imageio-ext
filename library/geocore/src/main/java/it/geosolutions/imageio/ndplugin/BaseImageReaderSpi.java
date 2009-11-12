package it.geosolutions.imageio.ndplugin;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public abstract class BaseImageReaderSpi extends ImageReaderSpi {

	protected static final Class<?>[] STANDARD_INPUT_TYPES = new Class[] {FileImageInputStreamExt.class,ImageInputStream.class,File.class,URL.class,URI.class };
	
	protected static final Logger LOGGER = Logger.getLogger(BaseImageReaderSpi.class.toString());
	
	protected static final String vendorName = "GeoSolutions";

	public BaseImageReaderSpi() {
		super();
	}

	public BaseImageReaderSpi(String vendorName, String version,
			String[] names, String[] suffixes, String[] MIMETypes,
			String readerClassName, Class<?>[] inputTypes,
			String[] writerSpiNames,
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
		super(vendorName, version, names, suffixes, MIMETypes, readerClassName,
				inputTypes, writerSpiNames,
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
}