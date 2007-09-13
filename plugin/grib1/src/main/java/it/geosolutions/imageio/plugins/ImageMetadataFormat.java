package it.geosolutions.imageio.plugins;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class ImageMetadataFormat extends IIOMetadataFormatImpl{

	
	protected ImageMetadataFormat() {
		super(ImageMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);
		
		//		 root -> ImageMetaData (imageIndex, sourceURI)
		// 					|
		// 					\---> RasterGeometry
		// 					\---> bands
		//							|
		// 							\---> Band[i]
		//									|
		// 									\---> key
		// 									\---> description
		// 									\---> noDataValues
		// 									\---> validRange
		// 									\---> categories
		//									|		\---> Category
		//									|				|
		//									|				\---> name
		//									|				\---> description
		//									|				\---> range
		//									|
		// 									\--->Calibration
		//									|		|
		//									|		\--->scale
		//									|		|		|
		//									|		|		\---> value
		//									|		|		\---> error
		//									|		\--->offset
		//									|				|
		//									|				\---> value
		//									|				\---> error
		//									|
		//									\--->Statistics

		addElement("ImageMetaData",
				ImageMetadata.nativeMetadataFormatName,
				CHILD_POLICY_ALL);
		addAttribute("ImageMetadata", "imageIndex", DATATYPE_INTEGER, true,null);
		addAttribute("ImageMetadata", "sourceURI", DATATYPE_STRING, true, null);
		
		addElement("RasterGeometry","ImageMetadata",CHILD_POLICY_ALL);
		//TODO: Add RasterGeometry childrens
		
		addElement("bands", "ImageMetadata",	CHILD_POLICY_REPEAT);
		
		//Band Element
		addElement("Band", "bands",	CHILD_POLICY_ALL);
		addElement("key", "Band",	CHILD_POLICY_EMPTY);
		addElement("description", "Band",	CHILD_POLICY_EMPTY);
		addElement("noDataValues", "Band",	CHILD_POLICY_EMPTY);
		addElement("validRange", "Band",	CHILD_POLICY_EMPTY);
		addElement("categories", "Band",	CHILD_POLICY_REPEAT);
		
		//Band -> Category element
		addElement("Category", "categories",	CHILD_POLICY_ALL);
		addElement("name", "Category",	CHILD_POLICY_EMPTY);
		addElement("description", "Category",	CHILD_POLICY_EMPTY);
		addElement("range", "Category",	CHILD_POLICY_EMPTY);
		
		//Band -> Calibration
		addElement("Calibration", "Band",	CHILD_POLICY_ALL);
		addElement("scale", "Calibration",	CHILD_POLICY_ALL);
		addElement("value", "scale",	CHILD_POLICY_EMPTY);
		addElement("error", "scale",	CHILD_POLICY_EMPTY);
		addElement("offset", "Calibration",	CHILD_POLICY_ALL);
		addElement("value", "offset",	CHILD_POLICY_EMPTY);
		addElement("error", "offset",	CHILD_POLICY_EMPTY);
		
		//Band -> Statistics
		addElement("Statistics", "Band",	CHILD_POLICY_ALL);
		//TODO: Add
	}
	
	public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
		//		 XXX
		return false;
	}

}
