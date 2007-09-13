package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.AbstractSpatioTemporalReader;
import it.geosolutions.imageio.plugins.CoverageMetadata;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

public class GRIB1CoverageMetadata extends CoverageMetadata {

	private AbstractSpatioTemporalReader reader;

	public GRIB1CoverageMetadata(GRIB1SpatioTemporalReader reader) {
		this.reader = reader;
	}

	/**
	 * Builds the coverageDescriptions metadata node
	 * 
	 * 
	 * TODO: add null checks!
	 * 
	 */
	protected IIOMetadataNode buildCoverageDescriptionsNode(
			IIOMetadataNode coverageDescriptionsNode) {

		// Map used to skip global indexes already handled
		Map handledIndexesMap = Collections.synchronizedMap(new TreeMap());

		String specifiedBands[] = null;
		// Getting all the globalIndexes involved in this coverageMetadata
		int[] globalIndexes = ((GRIB1SpatioTemporalReader) reader)
				.getGlobalIndexes(name, null, null, null, specifiedBands);

		// retrieving the mapper
		GRIB1FlatReaderMapper mapper = (GRIB1FlatReaderMapper) ((GRIB1SpatioTemporalReader) reader)
				.getMapper();

		// getting the globalIndex to SliceDescriptor Map as well as the
		// inverted map
		Map indexToSDMap = mapper.getIndexToSliceDescriptorMap();
		Map sdToIndexMap = mapper.getSliceDescriptorStringToIndexMap();

		// getting the number of all involved indexes
		final int numIndexes = globalIndexes.length;

		// Generating a node for each globalIndex
		for (int i = 0; i < numIndexes; i++) {
			IIOMetadataNode gridCoverageNode = new IIOMetadataNode(
					"GridCoverage");

			// getting the actually scanned index
			Integer actualIndex = Integer.valueOf(i);

			// Retrieving the SliceDescriptor related to this globalIndex
			GRIB1SliceDescriptor sd = (GRIB1SliceDescriptor) indexToSDMap
					.get(actualIndex);

			String parameterID = sd.getParameterID();

			// Getting the table version and parameter number of this parameter
			int[] param = GRIB1Utilities.getParamDescriptor(parameterID);

			// checking if this parameter has a single component or more.
			final int bandParam = GRIB1Utilities.checkMultiBandsParam(param);
			final String bandKeys[];
			final String mainParameterName = GRIB1Utilities
					.getMainParameterName(param[2], param[3]);
			final String parameterDescription = GRIB1Utilities
					.getMainParameterDescription(param[2], param[3]);
			final String axisDescription = GRIB1Utilities.getAxisDescription(
					param[2], param[3]);
			if (bandParam != GRIB1Utilities.SINGLE_COMPONENT_PARAM) {

				// Maybe the globalIndex related to a component of a
				// multiComponents parameter has been already encountered
				// Thus, I can proceed with the next global index.
				if (handledIndexesMap.containsKey(Integer.valueOf(i)))
					continue;

				// TODO Cache something in order to avoid unuseful scan in
				// future (as an instance, when need to groups images related to
				// different components

				// Getting the number related to the first component of this
				// parameter.
				final int firstBandParamNum = param[3] - bandParam;

				// Getting the number of components (>1) of this parameter.
				final int numComponents = GRIB1Utilities
						.getBandsNumberFromFirstParamNum(param[2],
								firstBandParamNum);

				// Preparing a Strings array for parameter components
				// todo: handle null values and more
				bandKeys = new String[numComponents];
				GRIB1SliceDescriptor foundSD = null;
				Integer globalIndex;

				// loop over parameter components
				for (int j = 0; j < numComponents; j++) {

					// The actual index may refer to the first component of the
					// parameter
					if (j == 0 && param[3] == firstBandParamNum) {
						globalIndex = actualIndex;
						foundSD = sd;

					} else {
						// I need to search globalIndex for ordered components

						// getting the parameter prefix
						final String prefix = new StringBuffer(Integer
								.toString(param[0])).append(":").append(
								Integer.toString(param[1])).append(":").append(
								Integer.toString(param[2])).append(":").append(
								Integer.toString(firstBandParamNum + j))
								.toString();
						final String sdDesc = sd.toString();
						final String suffix = sdDesc.substring(sdDesc
								.indexOf("_"), sdDesc.length());

						// building the String identifying the required
						// SliceDescriptor
						final String requiredSD = new StringBuffer(prefix)
								.append(suffix).toString();

						if (sdToIndexMap.containsKey(requiredSD)) {
							globalIndex = (Integer) sdToIndexMap
									.get(requiredSD);
							foundSD = (GRIB1SliceDescriptor) indexToSDMap
									.get(globalIndex);
						} else
							throw new IllegalArgumentException(
									"Needed component not found");

					}
					bandKeys[j] = foundSD.getParameterName();
					handledIndexesMap.put(globalIndex, null);
				}
			} else {
				bandKeys = new String[] { sd.getParameterName() };
			}

			// ////////////////////////////////////////////////////////////////
			//
			// Building Nodes
			//			 
			// ////////////////////////////////////////////////////////////////

			// TODO: Settings
			IIOMetadataNode nameNode = new IIOMetadataNode("name");
			nameNode.setNodeValue("SET GridCoverage NAME HERE");

			gridCoverageNode.appendChild(nameNode);

			IIOMetadataNode descriptionNode = new IIOMetadataNode("description");
			descriptionNode.setNodeValue("SET GridCoverage description HERE");

			gridCoverageNode.appendChild(descriptionNode);

			// TODO: settings overviews node

			// TODO: Settings spatial domain node

			// TODO: Settings temporal domain node
			// ////////////////////////////////////////////////////////////////
			// 
			// temporalDomain
			//
			// ////////////////////////////////////////////////////////////////

			final String iso8601temporalDomain = sd.getTemporalDomain();
			IIOMetadataNode temporalDomainNode = new IIOMetadataNode(
					"temporalDomain");
			if (!iso8601temporalDomain.contains("/")) {
				IIOMetadataNode timePositionNode = new IIOMetadataNode(
						"timePosition");
				timePositionNode.setNodeValue(iso8601temporalDomain);
				temporalDomainNode.appendChild(timePositionNode);
			} else {
				final String timeLimits[] = iso8601temporalDomain.split("/");
				IIOMetadataNode timePeriodNode = new IIOMetadataNode(
						"timePeriod");
				IIOMetadataNode beginPositionNode = new IIOMetadataNode(
						"beginPositionNode");
				beginPositionNode.setNodeValue(timeLimits[0]);
				IIOMetadataNode endPositionNode = new IIOMetadataNode(
						"endPositionNode");
				endPositionNode.setNodeValue(timeLimits[1]);
				timePeriodNode.appendChild(beginPositionNode);
				timePeriodNode.appendChild(endPositionNode);
				temporalDomainNode.appendChild(timePeriodNode);
			}

			gridCoverageNode.appendChild(temporalDomainNode);

			// ////////////////////////////////////////////////////////////////
			// 
			// verticalDomain
			//
			// ////////////////////////////////////////////////////////////////
			// TODO: Settings vertical domain node
			final float verticalLevel = sd.getZetaLevel();
			IIOMetadataNode verticalDomainNode = new IIOMetadataNode(
					"verticalDomain");

			IIOMetadataNode singleValueNode = new IIOMetadataNode("singleValue");

			singleValueNode.setNodeValue(Float.toString(verticalLevel));
			verticalDomainNode.appendChild(singleValueNode);
			gridCoverageNode.appendChild(verticalDomainNode);

			IIOMetadataNode axisNode = new IIOMetadataNode("axis");
			// Setting axis Node childs

			IIOMetadataNode axisNameNode = new IIOMetadataNode("name");
			axisNameNode.setNodeValue(mainParameterName);
			axisNode.appendChild(axisNameNode);

			IIOMetadataNode axisDescriptionNode = new IIOMetadataNode(
					"description");
			axisDescriptionNode.setNodeValue(parameterDescription);
			axisNode.appendChild(axisDescriptionNode);

			// TODO: setting somehow nullValues and range

			IIOMetadataNode axisBandKeysNode = new IIOMetadataNode("bandKeys");
			final int numBands = bandKeys.length;
			for (int bands = 0; bands < numBands; bands++) {
				IIOMetadataNode axisBandNode = new IIOMetadataNode("BandKey");
				axisBandNode.setNodeValue(bandKeys[bands]);
				axisBandKeysNode.appendChild(axisBandNode);
			}
			axisNode.appendChild(axisBandKeysNode);

			// appending axis Node
			gridCoverageNode.appendChild(axisNode);
			coverageDescriptionsNode.appendChild(gridCoverageNode);
		}

		return coverageDescriptionsNode;

	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		// TODO Auto-generated method stub
	}

	public void reset() {
		// TODO Auto-generated method stub
	}
}
