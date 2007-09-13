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

/**
 * Several GDAL format drivers allow to specify a set of options during the
 * creation of a file. <code>GDALCreateOption</code> class allows to represent
 * these creation options (properties as its name, its value,...) and it also
 * provides a set of methods to perform validity checks when users specify the
 * values for a create option.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini
 */
public class GDALCreateOption {

	/**
	 * A String representing the name of a "GDAL specific format driver"'s
	 * supported create option.
	 */
	private String optionName;

	/**
	 * Values for create option need to be specified to GDAL as strings.
	 * <code>representedValueType</code> allows to understand what kind of
	 * data type the string is representing. This is needed for value coherency
	 * checks. As an instance, a create option accepting only one of "YES/NO"
	 * values can't accept a value of 99, while a create option accepting an
	 * integer value belonging [1-100] can't accept an "ENABLED" String value.
	 * 
	 * The actually supported values for this field are listed below as a
	 * <code>public final static int</code> items list.
	 */

	private int representedValueType;

	/**
	 * supported <code>representedValueType</code> values.
	 */

	public final static int TYPE_INT = 50;

	public final static int TYPE_FLOAT = 51;

	public final static int TYPE_CHAR = 58;

	public final static int TYPE_STRING = 59;

	/**
	 * <code>validityCheckType</code> allows to specify some constraints on a
	 * create option supported values. Some create options only accept a value
	 * from a limited set, like as an instance, ["YES","NO"]. Some others accept
	 * a value which need to belong to a specified numeric RANGE.
	 * 
	 * The actually supported values for this field are listed below as a
	 * <code>public final static int</code> items list.
	 */
	private int validityCheckType;

	/**
	 * supported validity check types
	 */
	public final static int VALIDITYCHECKTYPE_VALUE = 0;

	public final static int VALIDITYCHECKTYPE_ONEOF = 1;

	public final static int VALIDITYCHECKTYPE_COMBINATIONOF = 2;

	public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED = 10;

	public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_LEFTEXCLUDED = 11;

	public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_RIGHTEXCLUDED = 12;

	public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESEXCLUDED = 19;

	public final static int VALIDITYCHECKTYPE_VALUE_BELOWERTHAN = 21;

	public final static int VALIDITYCHECKTYPE_VALUE_BELOWEROREQUALTHAN = 22;

	public final static int VALIDITYCHECKTYPE_VALUE_GREATEROREQUALTHAN = 23;

	public final static int VALIDITYCHECKTYPE_VALUE_GREATERTHAN = 24;

	public final static int VALIDITYCHECKTYPE_STRING_SYNTAX = 100;

	public final static int VALIDITYCHECKTYPE_NONE = 9999;

	/**
	 * <code>validityValues</code> is an array of String representing the
	 * control values will be used during value coherency checks in compliance
	 * with <code>validityCheckType</code> field. <br />
	 * 
	 * If validityCheckType is <code>VALIDITYCHECKTYPE_VALUE</code>, you need
	 * to set the only supported value.<br />
	 * 
	 * If validityCheckType is <code>VALIDITYCHECKTYPE_ONEOF</code> or
	 * <code>VALIDITYCHECKTYPE_COMBINATIONOF</code>, you need to fill that
	 * array with all supported values.<br />
	 * 
	 * If validityCheckType is
	 * <code>VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_XXX</code>, you need to
	 * set the two elements representing the extremes of that range.
	 * 
	 * If validityCheckType is one of <br />
	 * <code>VALIDITYCHECKTYPE_VALUE_BELOWERTHAN</code>,
	 * <code>VALIDITYCHECKTYPE_VALUE_BELOWEROREQUALTHAN</code>,
	 * <code>VALIDITYCHECKTYPE_VALUE_GREATEROREQUALTHAN</code>,
	 * <code>VALIDITYCHECKTYPE_VALUE_GREATERTHAN</code><br />
	 * you need to set the reference value.
	 */
	private String[] validityValues;

	/**
	 * simply establishes if the create option was set.
	 */
	private boolean set;

	/**
	 * A <code>String</code> representing the specified value for the create
	 * option.
	 */
	private String value;

	private String defaultValue; // Actually not used

	/**
	 * Constructor for a CreateOption.
	 * 
	 * @param optionName
	 *            The name of the create option
	 * @param validityCheckType
	 *            The <code>validityCheckType</code> for the create option
	 *            (see supported validity types)
	 * @param validityValues
	 *            The array of validity values for the create option.
	 * @param representedValueType
	 *            The type of value the create option is representing.
	 */
	public GDALCreateOption(final String optionName,
			final int validityCheckType, final String[] validityValues,
			final int representedValueType) {
		this.optionName = optionName;
		this.validityCheckType = validityCheckType;
		this.validityValues = validityValues;
		this.representedValueType = representedValueType;
		value = "";
	}

	/**
	 * returns the default value of the create option.
	 * 
	 * @return the default value of the create option.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * returns the name of the create option.
	 * 
	 * @return the name of the create option.
	 */
	public String getOptionName() {
		return optionName;
	}

	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

	/**
	 * returns <code>true</code> if the create option was set.
	 * 
	 * @return <code>true</code> if the create option was set.
	 */
	public boolean isSet() {
		return set;
	}

	/**
	 * returns the validty check type for the create option.
	 * 
	 * @return returns the validty check type for the create option.
	 */
	public int getValidityCheckType() {
		return validityCheckType;
	}

	public void setValidityCheckType(int validityType) {
		this.validityCheckType = validityType;
	}

	/**
	 * returns the array containing the validty values for the create option.
	 * 
	 * @return returns the array containing the validty values for this create
	 *         option.
	 */
	public String[] getValidityValues() {
		return validityValues;
	}

	public void setValidityValues(String[] validityValues) {
		this.validityValues = validityValues;
	}

	/**
	 * returns the set value of the create option.
	 * 
	 * @return the set value of the create option.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the specified value for the create option.
	 * 
	 * @param value
	 *            the value to set for the create option.
	 */
	public void setValue(String value) {
		// Checking if the specified value is compliant with the constraints
		// of this create option.
		if (checkValidity(value)) {
			this.value = value;
			this.set = true;
		} else {
			// TODO: More understandable error messages (parametrized)
			StringBuffer sb = new StringBuffer(
					"Error while setting value for create option ''").append(
					optionName).append("''");
			throw new IllegalArgumentException(sb.toString());
		}
	}

	/**
	 * Checks if the provided value is acceptable for this create option. As an
	 * instance, for a create option which accepts values belonging the range
	 * [1-100], the value ''ENABLED'' is obviously unapcetted and the method
	 * returns <code>false</code>
	 * 
	 * @param checkingValue
	 *            the value need to be checked.
	 * @return <code>true</code> if the specified value is compliant with this
	 *         create option.
	 */
	private boolean checkValidity(String checkingValue) {
		switch (validityCheckType) {
		// No validity check is required for this option
		case VALIDITYCHECKTYPE_NONE:
			return true;
			// The specified value must be the only one value, supported by this
			// option
		case VALIDITYCHECKTYPE_VALUE:
			return checkValueIs(checkingValue);

			// The specified value must be only one of a pre-established set.
		case VALIDITYCHECKTYPE_ONEOF:
			return checkOneOf(checkingValue);

		case VALIDITYCHECKTYPE_COMBINATIONOF:
			return checkCombinationOf(checkingValue);

			// The specified value must belonge to a pre-established range.
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESEXCLUDED:
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED:
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_LEFTEXCLUDED:
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_RIGHTEXCLUDED:
			switch (representedValueType) {
			case TYPE_INT:
				return checkIntRange(checkingValue);
			case TYPE_FLOAT:
				return checkFloatRange(checkingValue);
			}
			break;

		// The specified value must be compared to a reference value.
		case VALIDITYCHECKTYPE_VALUE_BELOWEROREQUALTHAN:
		case VALIDITYCHECKTYPE_VALUE_BELOWERTHAN:
		case VALIDITYCHECKTYPE_VALUE_GREATEROREQUALTHAN:
		case VALIDITYCHECKTYPE_VALUE_GREATERTHAN:
			switch (representedValueType) {
			case TYPE_INT:
				return checkIntValue(checkingValue);
			case TYPE_FLOAT:
				return checkFloatValue(checkingValue);
			}
			break;

		// The specified value is a String which must respect a pre-established
		// syntax
		case VALIDITYCHECKTYPE_STRING_SYNTAX:
			return checkStringSyntaxCompliance(checkingValue);
		}
		return false;
	}

	/**
	 * Checks if the specified value equals the only one value supported by this
	 * option.
	 * 
	 * @param checkingValue
	 *            the value need to be checked.
	 * @return <code>true</code> if the specified value is accepted.
	 */
	private boolean checkValueIs(String checkingValue) {
		if (checkingValue.equals(validityValues[0]))
			return true;
		return false;
	}

	// TODO: Maybe, we need to change some aspects of the SyntaxCompliance
	// check.

	/**
	 * Checks if the specified <code>String</code> respects the syntax of the
	 * value which the create option accepts.<BR />
	 * 
	 * How to intialize the <code>validityValues</code> array when setting
	 * <code>VALIDITYCHECKTYPE_STRING_SYNTAX</code> as
	 * <code>validityCheckType</code>? <BR />
	 * 
	 * You need to build <code>validityValues</code> array with 1
	 * <code>String</code>.<BR />
	 * 
	 * <code>validityValues[0]</code> need to be set with a
	 * <code>String</code> containing the tokens which separate the items in
	 * the String syntax.<BR />
	 * 
	 * Let us provide a simple example to clarify these explainations.<BR />
	 * Suppose a driver allows to specify the 'DATE' create option which has the
	 * syntax "D/M/Y-H:M:S".<BR />
	 * 
	 * In such a case, you need to do: validityValues[0]="//-::"
	 * 
	 * @param checkingValue
	 *            the string which syntax need to be checked.
	 * 
	 * @return <code>true</code> if the specified value respects the syntax.
	 */
	private boolean checkStringSyntaxCompliance(String checkingValue) {

		final int expectedTokensNumber = validityValues[0].length();
		char token[] = validityValues[0].toCharArray();
		final int checkingStringLength = checkingValue.length();
		int tokenPoisitions[] = new int[expectedTokensNumber];
		int tokenPosition = -1;
		int tokensFound = 0;
		int firstTokenPosition = 0;
		for (int i = 0; i < expectedTokensNumber; i++) {
			tokenPosition = checkingValue.indexOf(token[i], tokenPosition + 1);

			// It is worth to point out that tokens separate values.
			// So, if the first char of the checked String is a token, the
			// syntax was not respected, since at least the first char should be
			// a value instead of a token.
			if (i == 0) {
				firstTokenPosition = tokenPosition;
				// if (firstTokenPosition == 0)
				// return false;
			}

			// each time I find a token, increase the number of found tokens.
			if (tokenPosition != -1)
				tokensFound++;
			else
				break;
			tokenPoisitions[i] = tokenPosition;
		}
		// At the end of the loop we need to check 3 cases:
		// 1st) the number of tokens found can't be belower than the number of
		// expected tokens.
		if (tokensFound < expectedTokensNumber)
			return false;

		// 2nd) token cannot be adjacent.
		for (int i = 0; i < expectedTokensNumber - 1; i++)
			if (tokenPoisitions[i] == (tokenPoisitions[i + 1] - 1))
				return false;
		// 3rd) the last char of the checked string should be a value instead of
		// a token.
		if (tokenPoisitions[expectedTokensNumber - 1] == checkingStringLength)
			return false;
		return true;
	}

	/**
	 * Check if the integer value represented by the input <code>String</code>
	 * is compliant with the range of the accepted values for the create option
	 * 
	 * @param checkingValue
	 *            the string representing the integer value need to be checked.
	 * @return <code>true</code> if the specified value is compliant with the
	 *         range of the accepted values.
	 */
	private boolean checkIntRange(String checkingValue) {
		final int sx = Integer.parseInt(validityValues[0]);
		final int dx = Integer.parseInt(validityValues[1]);
		final int parsedValue = Integer.parseInt(checkingValue);

		switch (validityCheckType) {
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED:
			if (parsedValue >= sx && parsedValue <= dx)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_LEFTEXCLUDED:
			if (parsedValue > sx && parsedValue <= dx)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_RIGHTEXCLUDED:
			if (parsedValue >= sx && parsedValue < dx)
				return true;
			break;
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESEXCLUDED:
			if (parsedValue > sx && parsedValue < dx)
				return true;
			break;
		}
		return false;
	}

	/**
	 * Check if the integer value represented by the input <code>String</code>
	 * is compliant with the reference value.
	 * 
	 * @param checkingValue
	 *            the string representing the integer value need to be checked
	 * @return <code>true</code> if the provided value is compliant with the
	 *         reference value
	 */
	private boolean checkIntValue(String checkingValue) {
		final int limit = Integer.parseInt(validityValues[0]);
		final int parsedValue = Integer.parseInt(checkingValue);

		switch (validityCheckType) {
		case VALIDITYCHECKTYPE_VALUE_BELOWEROREQUALTHAN:
			if (parsedValue <= limit)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_BELOWERTHAN:
			if (parsedValue < limit)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_GREATEROREQUALTHAN:
			if (parsedValue >= limit)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_GREATERTHAN:
			if (parsedValue > limit)
				return true;
			break;
		}
		return false;
	}

	/**
	 * Check if the float value represented by the input <code>String</code>
	 * is compliant with the range of the accepted values for the create option
	 * 
	 * @param checkingValue
	 *            the string representing the float value need to be checked.
	 * @return <code>true</code> if the specified value is compliant with the
	 *         range of the accepted values.
	 */
	private boolean checkFloatRange(String checkingValue) {
		final float sx = Float.parseFloat(validityValues[0]);
		final float dx = Float.parseFloat(validityValues[1]);
		final float parsedValue = Float.parseFloat(checkingValue);

		switch (validityCheckType) {
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED:
			if (parsedValue >= sx && parsedValue <= dx)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_LEFTEXCLUDED:
			if (parsedValue > sx && parsedValue <= dx)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_RIGHTEXCLUDED:
			if (parsedValue >= sx && parsedValue < dx)
				return true;
			break;
		case VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESEXCLUDED:
			if (parsedValue > sx && parsedValue < dx)
				return true;
			break;
		}
		return false;
	}

	/**
	 * Check if the float value represented by the input <code>String</code>
	 * is compliant with the reference value.
	 * 
	 * @param checkingValue
	 *            the string representing the float value need to be checked
	 * @return <code>true</code> if the provided value is compliant with the
	 *         reference value
	 */
	private boolean checkFloatValue(String checkingValue) {
		final float limit = Float.parseFloat(validityValues[0]);
		final float parsedValue = Float.parseFloat(checkingValue);

		switch (validityCheckType) {
		case VALIDITYCHECKTYPE_VALUE_BELOWEROREQUALTHAN:
			if (parsedValue <= limit)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_BELOWERTHAN:
			if (parsedValue < limit)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_GREATEROREQUALTHAN:
			if (parsedValue >= limit)
				return true;
			break;

		case VALIDITYCHECKTYPE_VALUE_GREATERTHAN:
			if (parsedValue > limit)
				return true;
			break;
		}
		return false;
	}

	/**
	 * Checks if the specified value is one of values supported by this option.
	 * 
	 * @param checkingValue
	 *            the value need to be checked.
	 * @return <code>true</code> if the specified value is accepted.
	 */
	private boolean checkOneOf(String checkingValue) {
		final int cases = validityValues.length;
		for (int i = 0; i < cases; i++) {
			if (checkingValue.equals(validityValues[i]))
				return true;
		}
		return false;

	}

	/**
	 * Checks if the specified values (separated by the "|" char) are supported
	 * by this option.
	 * 
	 * @param checkingValue
	 *            the values need to be checked.
	 * @return <code>true</code> if the specified value combination is
	 *         accepted.
	 */

	private boolean checkCombinationOf(String checkingValues) {
		// Retrieving the number of validity values
		final int numOfValidityValues = validityValues.length;

		// preparing the array containing the specified values
		// pipe is a reserved character. So we need to use "\\|"
		final String[] values = checkingValues.split("\\|");
		final int numSpecifiedValues = values.length;

		// preliminar check: no value repetitions are allowed.
		for (int l = 0; l < numSpecifiedValues; l++) {
			final String comparingString = values[l];
			for (int k = l + 1; k < numSpecifiedValues; k++) {
				if (values[k].equals(comparingString))
					return false;
			}
		}

		// main check: specified values are supported?
		boolean supported;

		// Outer loop: on specified values
		for (int i = 0; i < numSpecifiedValues; i++) {
			supported = false;
			// inner loop: on validity values
			for (int j = 0; j < numOfValidityValues; j++) {
				// If the specified value is supported, check the next specified
				// value by terminating the inner loop.
				if (values[i].equals(validityValues[j])) {
					supported = true;
					break;
				}
			}
			if (!supported)
				return false;
		}
		return true;
	}

	public int getRepresentedValueType() {
		return representedValueType;
	}

	public void setRepresentedValueType(int representedValueType) {
		this.representedValueType = representedValueType;
	}

}
