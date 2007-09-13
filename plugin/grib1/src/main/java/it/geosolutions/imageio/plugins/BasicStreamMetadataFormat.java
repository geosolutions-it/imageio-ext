package it.geosolutions.imageio.plugins;


import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class BasicStreamMetadataFormat extends IIOMetadataFormatImpl {

	protected BasicStreamMetadataFormat() {
		super(BasicStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);

		// root -> BasicStreamMetaData
		// 			|
		// 			\---> format
		// 			\---> description
		// 			\---> numImages
		// 			\---> images
		// 					|
		// 					\--->Image[0]
		// 					|		|
		// 					|		\--->imageIndex
		//					|		\--->srcURI
		//					\--->Image[1]
   		// 							|
   		// 							\--->imageIndex
   		//							\--->srcURI

		addElement("BasicStreamMetaData",
				BasicStreamMetadata.nativeMetadataFormatName,
				CHILD_POLICY_ALL);
		addElement("format", "BasicStreamMetaData",	CHILD_POLICY_EMPTY);
		addElement("description", "BasicStreamMetaData",	CHILD_POLICY_EMPTY);
		addElement("numImages", "BasicStreamMetaData",	CHILD_POLICY_EMPTY);
		addElement("images", "BasicStreamMetaData",	CHILD_POLICY_REPEAT);
		addElement("Image", "Images",	CHILD_POLICY_ALL);
		addElement("imageIndex", "Image",	CHILD_POLICY_EMPTY);
		addElement("srcURI", "Image",	CHILD_POLICY_EMPTY);
	}

	public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
		// TODO Auto-generated method stub
		return false;
	}

}
