/**
 * 
 */
package it.geosolutions.imageio.plugins.mrsid;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @author Simone Giannecchini
 * 
 */
public class MrSIDIIOImageMetadataFormat extends IIOMetadataFormatImpl implements
		IIOMetadataFormat {

	protected static MrSIDIIOImageMetadataFormat mrsidMetadataInstance;

	public static synchronized IIOMetadataFormat getInstance() {
		if (mrsidMetadataInstance == null) {
			mrsidMetadataInstance = new MrSIDIIOImageMetadataFormat();
		}
		return mrsidMetadataInstance;
	}

	/**
	 * @param rootName
	 * @param childPolicy
	 */
	protected MrSIDIIOImageMetadataFormat() {
		super(MrSIDIIOImageMetadata.mrsidImageMetadataName, CHILD_POLICY_SOME);

		// root -> ImageDescriptor
		addElement("ImageDescriptor",
				MrSIDIIOImageMetadata.mrsidImageMetadataName, CHILD_POLICY_EMPTY);
		addAttribute("ImageDescriptor", "IMAGE__INPUT_NAME", DATATYPE_STRING,
				true, null);
		addAttribute("ImageDescriptor", "IMAGE__INPUT_FILE_SIZE",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__DYNAMIC_RANGE_WINDOW",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__DYNAMIC_RANGE_LEVEL",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_VERSION",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__TARGET_COMPRESSION_RATIO",
				DATATYPE_INTEGER, true, null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_NLEV",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_WEIGHT",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_GAMMA",
				DATATYPE_DOUBLE, false, null);
		addAttribute("ImageDescriptor", "IMAGE__COMPRESSION_BLOCK_SIZE",
				DATATYPE_INTEGER, true, null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__CREATION_DATE", DATATYPE_STRING, false,
				null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__WIDTH", DATATYPE_INTEGER, true,
				null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__HEIGHT", DATATYPE_INTEGER,
				true, null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__TRANSPARENT_DATA_VALUE", DATATYPE_STRING,
				false, null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__COLOR_SCHEME",
				DATATYPE_INTEGER, true, null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__DATA_TYPE", DATATYPE_INTEGER,
				true, null, null, null, false, false);
		addAttribute("ImageDescriptor", "IMAGE__BITS_PER_SAMPLE",
				DATATYPE_INTEGER, true, null, null, null, false, false);

		// root -> Georeferenncing
		addElement("Georeferenncing",
				MrSIDIIOImageMetadata.mrsidImageMetadataName, CHILD_POLICY_EMPTY);
		addAttribute("Georeferenncing", "IMG__HORIZONTAL_UNITS",
				DATATYPE_STRING, true, null);
		addAttribute("Georeferenncing", "IMG__PROJECTION_TYPE",
				DATATYPE_STRING, true, null);
		addAttribute("Georeferenncing", "IMG__PROJECTION_NUMBER",
				DATATYPE_INTEGER, true, null, null, null, false, false);
		addAttribute("Georeferenncing", "IMG__PROJECTION_ZONE",
				DATATYPE_INTEGER, true, null, null, null, false, false);
		addAttribute("Georeferenncing", "IMG__SPHEROID_NAME", DATATYPE_STRING,
				true, null);
		addAttribute("Georeferenncing", "IMG__SPHEROID_SEMI_MAJOR_AXIS",
				DATATYPE_DOUBLE, false, null);
		addAttribute("Georeferenncing", "IMG__SPHEROID_SEMI_MINOR_AXIS",
				DATATYPE_DOUBLE, false, null);
		addAttribute("Georeferenncing", "IMG__SPHEROID_ECCENTRICITY_SQUARED",
				DATATYPE_DOUBLE, false, null);
		addAttribute("Georeferenncing", "IMG__SPHEROID_RADIUS",
				DATATYPE_DOUBLE, false, null);
		addAttribute("Georeferenncing", "IMG__PROJECTION_PARAMETERS",
				DATATYPE_STRING, true, null);
		addAttribute("Georeferenncing", "IMAGE__XY_ORIGIN", DATATYPE_STRING,
				true, null);
		addAttribute("Georeferenncing", "IMAGE__X_RESOLUTION", DATATYPE_DOUBLE,
				false, null);
		addAttribute("Georeferenncing", "IMAGE__Y_RESOLUTION", DATATYPE_DOUBLE,
				false, null);
		addAttribute("Georeferenncing", "IMAGE__WKT", DATATYPE_STRING, true,
				null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.imageio.metadata.IIOMetadataFormatImpl#canNodeAppear(java.lang.String,
	 *      javax.imageio.ImageTypeSpecifier)
	 */
	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {
		return false;
	}

}
