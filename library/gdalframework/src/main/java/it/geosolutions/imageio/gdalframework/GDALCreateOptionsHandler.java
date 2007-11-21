/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.gdalframework;

import java.util.Vector;

/**
 * Abstract class which allows to properly handle the set of "format specific"
 * create options. Each Image I/O plugin exploiting a GDAL driver which supports
 * create options, should extend this class and define the proper
 * <code>GDALCreateOptionsHandler</code> constructor.<BR>
 * 
 * To write the extended <code>GDALCreateOptionsHandler</code> constructor you
 * need to instantiate the <code>createOptions</code> array with the number of
 * supported create options. Then, you need to set the proper fields of each
 * {@link GDALCreateOption} using the constructor as shown in the example listed
 * below.<BR>
 * <BR>
 * ... <BR>
 * <BR>
 * Firstly: set the validityValues for the create option. See
 * <code>GDALCreateOption</code> source code for more information about
 * <code>validityValues</code> and others fields. <BR>
 * <BR>
 * <code>final String nameOfCreateOptionValidityValues[] = new String[N];</code><BR>
 * <code>nameOfCreateOptionValidityValues[0] = "FIRST VALUE";</code><BR>
 * <code>nameOfCreateOptionValidityValues[1] = "SECOND VALUE";</code><BR>
 * <code>...</code><BR>
 * <code>nameOfCreateOptionValidityValues[N-1] = "LAST VALUE";</code><BR>
 * <BR>
 * Then, create a new <code>GDALCreateOption</code> setting the
 * <code>optionName</code>, the <code>validityCheckType</code>, the
 * <code>validityValues</code> array and the <code>representedType</code>.<BR>
 * <BR>
 * <code>createOptions[i] = new GDALCreateOption( "CREATEOPTIONNAME",</code><BR>
 * <code>GDALCreateOption.VALIDITYCHECKTYPE_XXXX, nameOfCreateOptionValidityValues, GDALCreateOption.TYPE_XXXX);</code><BR>
 * <BR>
 * <BR>
 * <BR>
 * PRACTICAL EXAMPLE: Suppose we are setting a Quality Create options which
 * accepts integer values belonging the range [1,100]<BR>
 * <code>final String qualityValues[] = new String[2];</code>
 * <code>qualityValues[0] = "1";</code><BR>
 * <code>qualityValues[1] = "100";</code><BR>
 * <code>...</code><BR>
 * <code>createOptions[0]=new GDALCreateOption("Quality", <BR>
 * GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,<BR>
 * qualityValues, GDALCreateOption.TYPE_INT);</code>
 * 
 * Available information about create options properties can be found at <a
 * href="http://www.gdal.org/formats_list.html"> GDAL Supported formats list</a>.
 * Look at the proper format page to retrieve names and values.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 */
public abstract class GDALCreateOptionsHandler {

	/**
	 * NOTE: ------------------------------------------------------------------
	 * When extending this class for different formats, you need to respect
	 * case-sensitiveness of create Options when setting <code>optionName</code>
	 * field.
	 */

	protected GDALCreateOption[] createOptions;

	/**
	 * Implement this method returning the number of the create options
	 * supported by the specific format driver.
	 * 
	 * @return the number of supported create options
	 */
	protected abstract int getCreateOptionsNumber();

	/**
	 * Provides to return a <code>Vector</code> containing <code>String</code>s
	 * representing all specified create options we need to give to the writer
	 * when it call GDAL's create/createCopy method.
	 */
	public Vector getCreateOptions() {
		int specifiedCreateOptions = 0;
		final int createOptionsArrayLenght = createOptions.length;

		// TODO: Checks the best approach to use.

		// ////
		// 
		// approach 1
		//
		// ////

		// Is this a Faster Approach?
		// This approach allocate more int than required but reduces
		// the loop count and the array scan.
		final int usefulCreateOptions[] = new int[createOptionsArrayLenght];

		// Scanning createOptions searching for set items
		for (int i = 0, j = 0; i < createOptionsArrayLenght; i++) {
			if (createOptions[i].isSet()) {
				specifiedCreateOptions++;
				// annotating the position of the set option
				usefulCreateOptions[j] = i;
				j++;
			}
		}
		if (specifiedCreateOptions == 0)
			return null;
		Vector optionVector = new Vector(specifiedCreateOptions);
		int selectedOption = 0;
		for (int i = 0; i < specifiedCreateOptions; i++) {
			StringBuffer sb = new StringBuffer("");

			// retrieving the index of the next set option
			selectedOption = usefulCreateOptions[i];
			sb.append(createOptions[selectedOption].getOptionName())
					.append("=").append(
							createOptions[selectedOption].getValue());

			optionVector.add((String) sb.toString());
		}

		// ////
		// 
		// approach 2
		//
		// ////

		// for (int i = 0; i < createOptionsArrayLenght; i++) {
		// if (createOptions[i].isSet())
		// specifiedCreateOptions++;
		// }
		// Vector optionVector = new Vector(specifiedCreateOptions);
		// for (int i = 0; i < createOptionsArrayLenght; i++) {
		// if (createOptions[i].isSet()) {
		// StringBuffer sb = new StringBuffer("");
		// sb.append(createOptions[i].getOptionName())
		// .append("=").append(
		// createOptions[i].getValue());
		// // TODO: Check this?
		// optionVector.add((String) sb.toString());
		// }
		// }
		return optionVector;
	}

	/**
	 * Set the value of the create option identified by <code>optionName</code>
	 * to <code>optionValue</code>
	 * 
	 * @param optionName
	 *            name of the create option we want to set.
	 * @param optionValue
	 *            value for the specified create option.
	 */
	public void setCreateOption(final String optionName,
			final String optionValue) {
		final int createOptionIndex = findCreateOption(optionName);
		createOptions[createOptionIndex].setValue(optionValue);
	}

	/**
	 * Set the value of the create option identified by <code>optionName</code>
	 * to <code>optionValue</code>
	 * 
	 * @param optionName
	 *            name of the create option we want to set.
	 * @param optionValue
	 *            value for the specified create option.
	 */
	public void setCreateOption(final String optionName, final int optionValue) {
		setCreateOption(optionName, Integer.toString(optionValue));
	}

	/**
	 * Set the value of the create option identified by <code>optionName</code>
	 * to <code>optionValue</code>
	 * 
	 * @param optionName
	 *            name of the create option we want to set.
	 * @param optionValue
	 *            value for the specified create option.
	 */
	public void setCreateOption(final String optionName, final float optionValue) {
		setCreateOption(optionName, Float.toString(optionValue));
	}

	/**
	 * Given the name of a create option, returns the index of the
	 * {@link GDALCreateOption}s array where this option is stored.
	 * 
	 * @param optionName
	 *            the name of the required create option
	 * @return the index in the array of {@link GDALCreateOption}s where this
	 *         option is stored.
	 */
	private int findCreateOption(final String optionName) {
		final int createOptionsNumber = getCreateOptionsNumber();
		for (int i = 0; i < createOptionsNumber; i++)
			if (createOptions[i].getOptionName().equals(optionName))
				return i;
		return -1;
	}

}
