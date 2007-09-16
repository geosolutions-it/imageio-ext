package it.geosolutions.imageio.plugins;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @author Daniele Romagnoli
 */
public class StreamMetadataFormat extends IIOMetadataFormatImpl {

	protected StreamMetadataFormat() {
		super(StreamMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);
		
		addElement("StreamMetadata", StreamMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);
		addElement("coverageOffering", "StreamMetadata", CHILD_POLICY_REPEAT);
		
		addElement("Coverage","coverageOffering",CHILD_POLICY_ALL );
		addElement("name", "Coverage", CHILD_POLICY_EMPTY);
		addElement("description", "Coverage", CHILD_POLICY_EMPTY);
		addElement("boundedBy","Coverage", CHILD_POLICY_SOME);
		addElement("Envelope","boundedBy", CHILD_POLICY_ALL);
		addElement("lowerCorner","Envelope", CHILD_POLICY_EMPTY);
		addElement("upperCorner","Envelope", CHILD_POLICY_EMPTY);
		
		addElement("VerticalExtent", "boundedBy", CHILD_POLICY_CHOICE);
		addAttribute("VerticalExtent", "srsName", DATATYPE_STRING, true,null);
		addElement("values", "VerticalExtent", CHILD_POLICY_REPEAT);
		addElement("singleValue", "values", CHILD_POLICY_EMPTY);
		
		addElement("verticalRange", "VerticalExtent", CHILD_POLICY_SOME);
		addElement("min","verticalRange",CHILD_POLICY_EMPTY );
		addElement("max","verticalRange",CHILD_POLICY_EMPTY );
		addElement("res","verticalRange",CHILD_POLICY_EMPTY );
		
		addElement("TemporalExtent", "boundedBy", CHILD_POLICY_CHOICE);
		addAttribute("TemporalExtent",  "format", DATATYPE_STRING, true,"ISO8601");
		addElement("timePositions", "TemporalExtent", CHILD_POLICY_REPEAT);
		addElement("timePosition", "timePositions", CHILD_POLICY_EMPTY);
		
		addElement("timePeriod", "TemporalExtent", CHILD_POLICY_SOME);
		addElement("beginPosition","timePeriod",CHILD_POLICY_EMPTY );
		addElement("endPosition","timePeriod",CHILD_POLICY_EMPTY );
		addElement("timeResolution","timePeriod",CHILD_POLICY_EMPTY );
		
		addElement("axis","Coverage", CHILD_POLICY_ALL);
		addElement("name", "axis", CHILD_POLICY_EMPTY);
		addElement("description", "axis", CHILD_POLICY_EMPTY);
		addElement("bandKeys", "axis", CHILD_POLICY_REPEAT);
		addElement("BandKey", "bandKeys", CHILD_POLICY_EMPTY);
		
		addElement("Dictionary", "StreamMetadata", CHILD_POLICY_SOME);
		addElement("CRS", "Dictionary", CHILD_POLICY_EMPTY);
		addElement("verticalCRSs", "Dictionary", CHILD_POLICY_REPEAT);
		addElement("VerticalCRS", "verticalCRSs", CHILD_POLICY_EMPTY);
		addElement("unitOfMeasures", "Dictionary", CHILD_POLICY_REPEAT);
		addElement("UnitOfMeasure", "unitOfMeasures", CHILD_POLICY_EMPTY);
	}

	public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
		// TODO Auto-generated method stub
		return false;
	}


}
