package it.geosolutions.imageio.plugins.jhdf.aps;

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

public class APSHDFImageReaderSpi extends BaseHDFImageReaderSpi {

	/**
	 * The list of the required attributes was built in compliance with the
	 * information available at:
	 * http://www7333.nrlssc.navy.mil/docs/aps_v3.4/user/aps/ch06.html
	 * 
	 */
	private final static String[] requiredAPSAttributes = { "file",
			"fileClassification", "fileStatus", "fileTitle", "fileVersion",
			"createAgency", "createSoftware", "createPlatform", "createTime",
	/* "createUser" */}; // TODO: Add more attributes??

	static final String[] suffixes = { "hdf" };

	static final String[] formatNames = { "HDF", "HDF4", "HDF5" };

	static final String[] mimeTypes = { "image/hdf" };

	static final String version = "1.0";

	static final String readerCN = "it.geosolutions.imageio.plugins.jhdf.APSHDFImageReader";

	static final String vendorName = "GeoSolutions";

	// writerSpiNames
	static final String[] wSN = { null };

	// StreamMetadataFormatNames and StreamMetadataFormatClassNames
	static final boolean supportsStandardStreamMetadataFormat = false;

	static final String nativeStreamMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFStreamMetadata_1.0";;

	static final String nativeStreamMetadataFormatClassName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFStreamMetadataFormat";

	static final String[] extraStreamMetadataFormatNames = { null };

	static final String[] extraStreamMetadataFormatClassNames = { null };

	// ImageMetadataFormatNames and ImageMetadataFormatClassNames
	static final boolean supportsStandardImageMetadataFormat = false;

	static final String nativeImageMetadataFormatName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadata_1.0";;

	static final String nativeImageMetadataFormatClassName = "it.geosolutions.imageio.plugins.jhdf.aps.APSHDFImageMetadataFormat";;

	static final String[] extraImageMetadataFormatNames = { null };

	static final String[] extraImageMetadataFormatClassNames = { null };

	public APSHDFImageReaderSpi() {
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

	//
	// This version of the canDecodeInput checks if all required attributes are
	// present
	// in the APS source.
	//
	private boolean OldcanDecodeInput(Object input) throws IOException {
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

							// CHECKS IF APS REQUIRED ATTRIBUTES ARE PRESENT
							final int requiredAttributes = requiredAPSAttributes.length;
							int foundAttributes = 0;
							HObject root = testFile.get("/");
							final Iterator metadataIt = root.getMetadata()
									.iterator();

							while (metadataIt.hasNext()) {
								// get the attribute
								final Attribute att = (Attribute) metadataIt
										.next();
								final String attName = att.getName();
								for (int i = 0; i < requiredAttributes; i++) {
									if (attName
											.equals(requiredAPSAttributes[i])) {
										foundAttributes++;
										if (foundAttributes == requiredAttributes) {
											// Avoid to scan the whole
											// attributes
											// list if I already found the
											// required ones
											testFile.close();
											return true;
										}
										break;
									}
								}
							}

							testFile.close();
							if (foundAttributes == requiredAttributes)
								return true;
							return false;
						}
					}
				} catch (IOException e) {
					throw e;
				} catch (Exception e) {
				}
		}
		return false;
	}

	public boolean canDecodeInput(Object input) throws IOException {
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
							boolean found=false;
							HObject root = testFile.get("/");
							final Iterator metadataIt = root.getMetadata()
									.iterator();

							while (metadataIt.hasNext()) {
								// get the attribute
								final Attribute att = (Attribute) metadataIt
										.next();
								final String attName = att.getName();

								if (attName.equals(APSAttributes.STD_FA_CREATESOFTWARE)) {
									final Object attValue = att.getValue();
									final String value[] = ((String[])attValue);
									if (value[0].startsWith("APS")){
										found=true;
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
				}
		}
		return false;
	}

	/**
	 * Returns an instance of the APSHDFImageReader
	 * 
	 * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
	 */
	public ImageReader createReaderInstance(Object source) throws IOException {
		return new APSHDFImageReader(this);
	}

	/**
	 * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
	 */
	public String getDescription(Locale locale) {
		return new StringBuffer("APS Compliant HDF Image Reader, version ")
				.append(version).toString();
	}

}