package it.geosolutions.imageio.plugins;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class CoverageMetadataFormat extends IIOMetadataFormatImpl {

	protected CoverageMetadataFormat() {
		super(CoverageMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);

		addElement("CoverageMetaData",
				CoverageMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);
		addElement("name", "CoverageMetaData", CHILD_POLICY_EMPTY);
		addElement("description", "CoverageMetaData", CHILD_POLICY_EMPTY);
		addElement("discoveryMetaData", "CoverageMetaData", CHILD_POLICY_SOME);

		// For the moment: CHILD_POLICY_EMPTY
		addElement("ISO19139", "discoveryMetaData", CHILD_POLICY_EMPTY);

		addElement("coverageDescriptions", "CoverageMetaData",
				CHILD_POLICY_REPEAT);

		addElement("GridCoverage", "coverageDescriptions", CHILD_POLICY_SOME);
		addElement("name", "GridCoverage", CHILD_POLICY_EMPTY);
		addElement("description", "GridCoverage", CHILD_POLICY_EMPTY);
		addElement("overviews", "GridCoverage", CHILD_POLICY_REPEAT);
		addAttribute("overviews", "numLevels", DATATYPE_INTEGER, true, "1");
		addAttribute("overviews", "scaleFactor", DATATYPE_FLOAT, true, null);
		// TODO: review the datatype of the overviews type
		addAttribute("overviews", "type", DATATYPE_STRING, false, null);

		addElement("Overview", "overviews", CHILD_POLICY_ALL);
		addElement("imageIndex", "Overview", CHILD_POLICY_EMPTY);
		addElement("levelID", "Overview", CHILD_POLICY_EMPTY);
		addElement("GridGeometry", "Overview", CHILD_POLICY_ALL);
		// TODO: TBD GridGeometry childrens

		addElement("spatialDomain", "GridCoverage", CHILD_POLICY_ALL);
		addAttribute("spatialDomain", "srsName", DATATYPE_STRING, true, null);
		addElement("RectifiedGrid", "spatialDomain", CHILD_POLICY_ALL);

		addElement("limits", "RectifiedGrid", CHILD_POLICY_ALL);
		addElement("GridEnvelope", "limits", CHILD_POLICY_ALL);
		addElement("low", "GridEnvelope", CHILD_POLICY_EMPTY);
		addElement("high", "GridEnvelope", CHILD_POLICY_EMPTY);

		addElement("origin", "RectifiedGrid", CHILD_POLICY_ALL);
		addElement("Point", "origin", CHILD_POLICY_ALL);
		addElement("descriptions", "Point", CHILD_POLICY_EMPTY);
		addElement("coordinates", "Point", CHILD_POLICY_EMPTY);

		addElement("offsetVectors", "RectifiedGrid", CHILD_POLICY_REPEAT);

		addElement("temporalDomain", "GridCoverage", CHILD_POLICY_CHOICE);
		addAttribute("temporalDomain", "format", DATATYPE_STRING, true,
				"ISO8601");
		addElement("timePosition", "temporalDomain", CHILD_POLICY_EMPTY);
		addElement("timePeriod", "temporalDomain", CHILD_POLICY_SOME);

		addElement("beginPosition", "timePeriod", CHILD_POLICY_EMPTY);
		addElement("endPosition", "timePeriod", CHILD_POLICY_EMPTY);
		addElement("timeResolution", "timePeriod", CHILD_POLICY_EMPTY);

		addElement("verticalDomain", "GridCoverage", CHILD_POLICY_CHOICE);
		addAttribute("verticalDomain", "srsName", DATATYPE_STRING, true, null);
		addElement("singleValue", "verticalDomain", CHILD_POLICY_EMPTY);
		addElement("verticalRange", "verticalDomain", CHILD_POLICY_SOME);

		addElement("min", "verticalRange", CHILD_POLICY_EMPTY);
		addElement("max", "verticalRange", CHILD_POLICY_EMPTY);
		addElement("res", "verticalRange", CHILD_POLICY_EMPTY);

		addElement("axis", "GridCoverage", CHILD_POLICY_ALL);
		addElement("name", "axis", CHILD_POLICY_EMPTY);
		addElement("description", "axis", CHILD_POLICY_EMPTY);
		addElement("nullValue", "axis", CHILD_POLICY_EMPTY);
		addElement("range", "axis", CHILD_POLICY_EMPTY);
		addElement("bandKeys", "axis", CHILD_POLICY_REPEAT);
		addElement("BandKey", "bandKeys", CHILD_POLICY_EMPTY);

	}

	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {
		// TODO Auto-generated method stub
		return false;
	}

}
