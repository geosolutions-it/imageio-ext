package it.geosolutions.imageio.plugins.jhdf;

import javax.imageio.metadata.IIOMetadata;

public abstract class BaseHDFStreamMetadata extends IIOMetadata {

	public BaseHDFStreamMetadata(boolean standardMetadataFormatSupported,
			String nativeMetadataFormatName,
			String nativeMetadataFormatClassName,
			String[] extraMetadataFormatNames,
			String[] extraMetadataFormatClassNames) {
		super(standardMetadataFormatSupported, nativeMetadataFormatName,
				nativeMetadataFormatClassName, extraMetadataFormatNames,
				extraMetadataFormatClassNames);
	}

}
