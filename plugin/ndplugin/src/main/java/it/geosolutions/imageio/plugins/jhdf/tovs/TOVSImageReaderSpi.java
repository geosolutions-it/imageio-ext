package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageReader;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;

public class TOVSImageReaderSpi extends BaseHDFImageReaderSpi {

	static final String[] suffixes = { "hdf" };

	static final String[] formatNames = { "HDF", "HDF4", "HDF5" };

	static final String[] mimeTypes = { "image/hdf" };

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = { null };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSStreamMetadata_1.0";

	static final String nativeStreamMetadataFormatClassName = null;

	static final String[] extraStreamMetadataFormatNames = { null };

	static final String[] extraStreamMetadataFormatClassNames = { null };

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.tovs.TOVSImageMetadata_1.0";

	static final String nativeImageMetadataFormatClassName = null;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public TOVSImageReaderSpi() {
		super(
				vendorName,
				version,
				formatNames,
				suffixes,
				mimeTypes,
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

	public boolean canDecodeInput(Object input) throws IOException {
		synchronized (spiMutex) {
			if (input instanceof FileImageInputStreamExtImpl) {
				input = ((FileImageInputStreamExtImpl) input).getFile();
			}

			if (input instanceof File) {
				final String filepath = ((File) input).getPath();

				// TODO: Improve to add HDF5 support.
				final FileFormat fileFormat = FileFormat
						.getFileFormat(FileFormat.FILE_TYPE_HDF4);
				if (fileFormat != null && fileFormat.isThisType(filepath))
					try {
						final FileFormat testFile = fileFormat.open(filepath,
								FileFormat.READ);
						if (testFile != null) {
							if (testFile.open() >= 0) {
								boolean found = false;
								HObject root = testFile.get("/");
								final Iterator metadataIt = root.getMetadata()
										.iterator();

								while (metadataIt.hasNext()) {
									// get the attribute
									final Attribute att = (Attribute) metadataIt
											.next();
									final String attName = att.getName();

									if (attName.startsWith("File Description")) {
										final Object attValue = att.getValue();
										final String value = (String) attValue;
										if (value.contains("TOVS PATHFINDER")) {
											found = true;
											break;
										}
									}

								}

								testFile.close();
								return found;
							}
						}
					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						// TODO
					}
			}
		}
		return false;
	}

	public ImageReader createReaderInstance(Object source) throws IOException {
		return new TOVSImageReader(this);
	}

	public String getDescription(Locale locale) {
		return new StringBuffer("TOVS Compliant HDF Image Reader, version ")
				.append(version).toString();
	}

}
