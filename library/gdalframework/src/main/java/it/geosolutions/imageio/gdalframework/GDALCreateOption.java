/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.gdalframework;

/**
 * Several GDAL format drivers allow to specify a set of options during the
 * creation of a file. {@link GDALCreateOption} class allows to represent these
 * creation options (properties as its name, its value,...) and it also provides
 * a set of methods to perform validity checks when users specify the values for
 * a create option.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class GDALCreateOption {

    /**
     * A String representing the name of a "GDAL specific format driver"'s
     * supported create option.
     */
    private String optionName;

    /**
     * Values for create option need to be specified to GDAL as strings.
     * <code>representedValueType</code> allows to understand what type of
     * data the string is representing. This is needed for value coherence
     * checks. As an instance, a create option accepting only one of "YES/NO"
     * values can't accept a value of 99, while a create option accepting an
     * integer value belonging [1-100] can't accept an "ENABLED" String value.
     * 
     * Some drivers could accept a create option without any value. The
     * existence of that create option in the create options list represents
     * itself the specific setting.
     * 
     * The actually supported values for this field are listed below as a
     * <code>public final static int</code> items list.
     */
    private int representedValueType;

    /**
     * ************************************************************************
     * 
     * supported <code>representedValueType</code> values.
     * 
     * ************************************************************************
     */
    /** Tag for Integer Type */
    public final static int TYPE_INT = 50;

    /** Tag for Floating Point Type */
    public final static int TYPE_FLOAT = 51;

    /** Tag for Create options without a value */
    public final static int TYPE_NONE = 55;

    /** Tag for Char Type */
    public final static int TYPE_CHAR = 58;

    /** Tag for String Type */
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
     * ************************************************************************
     * 
     * supported validity check types
     * 
     * ************************************************************************
     */
    /** Accepted value is a single one */
    public final static int VALIDITYCHECKTYPE_VALUE = 0;

    /** Accepted value is one of a set of predefined values */
    public final static int VALIDITYCHECKTYPE_ONEOF = 1;

    /**
     * Accepted values are a combination of values belonging a set of predefined
     * ones
     */
    public final static int VALIDITYCHECKTYPE_COMBINATIONOF = 2;

    /**
     * Accepted values are contained in a range, having the extremes included
     */
    public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED = 10;

    /**
     * Accepted values are contained in a range, having the left extreme
     * excluded.
     */
    public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_LEFTEXCLUDED = 11;

    /**
     * Accepted values are contained in a range, having the right extreme
     * excluded.
     */
    public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_RIGHTEXCLUDED = 12;

    /**
     * Accepted values are contained in a range, having the extremes excluded.
     */
    public final static int VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESEXCLUDED = 19;

    /** Accepted values are less than a predefined one */
    public final static int VALIDITYCHECKTYPE_VALUE_LESSTHAN = 21;

    /** Accepted values are less than or equal to a predefined one */
    public final static int VALIDITYCHECKTYPE_VALUE_LESSTHANOREQUALTO = 22;

    /** Accepted values are greater than or equal to a predefined one */
    public final static int VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO = 23;

    /** Accepted values are greater than a predefined one */
    public final static int VALIDITYCHECKTYPE_VALUE_GREATERTHAN = 24;

    /** Accepted values are multiple of a predefined one */
    public final static int VALIDITYCHECKTYPE_VALUE_MULTIPLEOF = 25;

    /** Accepted values are power of a predefined one */
    public final static int VALIDITYCHECKTYPE_VALUE_POWEROF = 26;

    /** Accepted values are strings which need to respect a predefined syntax */
    public final static int VALIDITYCHECKTYPE_STRING_SYNTAX = 100;

    /** Accepted values are anything */
    public final static int VALIDITYCHECKTYPE_NONE = 9999;

    /**
     * <code>validityValues</code> is an array of String representing the
     * control values which will be used during value coherence checks in
     * compliance with <code>validityCheckType</code> field. <BR>
     * 
     * If validityCheckType is <code>VALIDITYCHECKTYPE_VALUE</code>, you need
     * to set the only supported value.<BR>
     * 
     * If validityCheckType is <code>VALIDITYCHECKTYPE_ONEOF</code> or
     * <code>VALIDITYCHECKTYPE_COMBINATIONOF</code>, you need to fill that
     * array with all supported values.<BR>
     * 
     * If validityCheckType is
     * <code>VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_XXX</code>, you need to
     * set the two elements representing the extremes of that range.
     * 
     * If validityCheckType is one of <BR>
     * <code>VALIDITYCHECKTYPE_VALUE_LESSTHAN</code>,
     * <code>VALIDITYCHECKTYPE_VALUE_LESSTHANOREQUALTO</code>,
     * <code>VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO</code>,
     * <code>VALIDITYCHECKTYPE_VALUE_GREATERTHAN</code>
     * <code>VALIDITYCHECKTYPE_VALUE_POWEROF</code>
     * <code>VALIDITYCHECKTYPE_VALUE_MULTIPLEOF</code><BR>
     * you need to set the reference value.
     * 
     * If validityCheckType is <code>VALIDITYCHECKTYPE_NONE</code> no checks
     * are performed.
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
     * Constructor for a <code>GDALCreateOption</code>.
     * 
     * @param optionName
     *                The name of the create option
     * @param validityCheckType
     *                The <code>validityCheckType</code> for the create option
     *                (see supported validity types)
     * @param validityValues
     *                The array of validity values for the create option.
     * @param representedValueType
     *                The type of value the create option is representing.
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

    /**
     * set the default value of the create option.
     */
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

    /**
     * set the name of the create option.
     */
    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    /**
     * returns <code>true</code> if the create option has been set.
     * 
     * @return <code>true</code> if the create option has been set.
     */
    public boolean isSet() {
        return set;
    }

    /**
     * returns the validity check type for the create option.
     * 
     * @return returns the validity check type for the create option.
     */
    public int getValidityCheckType() {
        return validityCheckType;
    }

    /**
     * set the validity check type for the create option.
     */
    public void setValidityCheckType(int validityType) {
        this.validityCheckType = validityType;
    }

    /**
     * returns the array containing the validity values for the create option.
     * 
     * @return returns the array containing the validity values for this create
     *         option.
     */
    public String[] getValidityValues() {
        return validityValues;
    }

    /**
     * set the array containing the validity values for the create option.
     */
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
     *                the value to set for the create option.
     */
    public void setValue(String value) {
        // Checking if the specified value is complying with the constraints
        // of this create option.
        if (checkValidity(value)) {
            this.value = value;
            this.set = true;
        } else {
            // TODO: More understandable error messages (parameterized)
            StringBuffer sb = new StringBuffer(
                    "Error while setting value for create option ''").append(
                    optionName).append("''");
            throw new IllegalArgumentException(sb.toString());
        }
    }

    /**
     * Checks if the provided value is acceptable for this create option. As an
     * instance, for a create option which accepts values belonging the range
     * [1-100], the value ''ENABLED'' is obviously unaccepted and the method
     * returns <code>false</code>
     * 
     * @param checkingValue
     *                the value need to be checked.
     * @return <code>true</code> if the specified value is complying with this
     *         create option.
     */
    private boolean checkValidity(String checkingValue) {
        switch (validityCheckType) {
        // //
        // 
        // No validity check is required for this option
        //
        // //
        case VALIDITYCHECKTYPE_NONE:
            return true;
            // //
            // 
            // The specified value must be the only one value, supported by this
            // option
            //
            // //
        case VALIDITYCHECKTYPE_VALUE:
            return checkValueIs(checkingValue);
            // //
            // 
            // The specified value must be only one of a predefined set.
            //
            // //
        case VALIDITYCHECKTYPE_ONEOF:
            return checkOneOf(checkingValue);
            // //
            // 
            // The specified value must be one or more of a predefined set.
            //
            // //
        case VALIDITYCHECKTYPE_COMBINATIONOF:
            return checkCombinationOf(checkingValue);
            // //
            // 
            // The specified value must belong to a predefined range.
            //
            // //
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
        // //
        // 
        // The specified value must be compared to a reference value.
        //
        // //
        case VALIDITYCHECKTYPE_VALUE_LESSTHANOREQUALTO:
        case VALIDITYCHECKTYPE_VALUE_LESSTHAN:
        case VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO:
        case VALIDITYCHECKTYPE_VALUE_GREATERTHAN:
        case VALIDITYCHECKTYPE_VALUE_MULTIPLEOF:
        case VALIDITYCHECKTYPE_VALUE_POWEROF:
            switch (representedValueType) {
            case TYPE_INT:
                return checkIntValue(checkingValue);
            case TYPE_FLOAT:
                return checkFloatValue(checkingValue);
            }
            break;

        // //
        // 
        // The specified value is a String which must respect a predefined
        // syntax
        //
        // //
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
     *                the value need to be checked.
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
     * value which the create option accepts.<BR>
     * 
     * How to initialize the <code>validityValues</code> array when setting
     * <code>VALIDITYCHECKTYPE_STRING_SYNTAX</code> as
     * <code>validityCheckType</code>? <BR>
     * 
     * You need to build <code>validityValues</code> array with 1
     * <code>String</code>.<BR>
     * 
     * <code>validityValues[0]</code> need to be set with a
     * <code>String</code> containing the tokens which separate the items in
     * the String syntax.<BR>
     * 
     * Let us provide a simple example to clarify these explanations.<BR>
     * Suppose a driver allows to specify the 'DATE' create option which has the
     * syntax "D/M/Y-H:M:S".<BR>
     * 
     * In such a case, you need to do: validityValues[0]="//-::"
     * 
     * @param checkingValue
     *                the string which syntax need to be checked.
     * 
     * @return <code>true</code> if the specified value respects the syntax.
     */
    private boolean checkStringSyntaxCompliance(String checkingValue) {
        // TODO: This method should be improved (more powerful checks)
        final int expectedTokensNumber = validityValues[0].length();
        char token[] = validityValues[0].toCharArray();
        final int checkingStringLength = checkingValue.length();
        int tokenPoisitions[] = new int[expectedTokensNumber];
        int tokenPosition = -1;
        int tokensFound = 0;
        for (int i = 0; i < expectedTokensNumber; i++) {
            tokenPosition = checkingValue.indexOf(token[i], tokenPosition + 1);
            // each time I find a token, increase the number of found tokens.
            if (tokenPosition != -1)
                tokensFound++;
            else
                break;
            tokenPoisitions[i] = tokenPosition;
        }
        // At the end of the loop we need to check 3 cases:
        // 1st) the number of tokens found can't be less than the number of
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
     * is complying with the range of the accepted values for the create option
     * 
     * @param checkingValue
     *                the string representing the integer value need to be
     *                checked.
     * @return <code>true</code> if the specified value is complying with the
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
     * is complying with the reference value.
     * 
     * @param checkingValue
     *                the string representing the integer value need to be
     *                checked
     * @return <code>true</code> if the provided value is complying with the
     *         reference value
     */
    private boolean checkIntValue(String checkingValue) {
        final int reference = Integer.parseInt(validityValues[0]);
        final int parsedValue = Integer.parseInt(checkingValue);

        switch (validityCheckType) {
        case VALIDITYCHECKTYPE_VALUE_LESSTHANOREQUALTO:
            if (parsedValue <= reference)
                return true;
            break;

        case VALIDITYCHECKTYPE_VALUE_LESSTHAN:
            if (parsedValue < reference)
                return true;
            break;

        case VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO:
            if (parsedValue >= reference)
                return true;
            break;

        case VALIDITYCHECKTYPE_VALUE_GREATERTHAN:
            if (parsedValue > reference)
                return true;
            break;
        case VALIDITYCHECKTYPE_VALUE_POWEROF:
            int result = 1;
            while (result < Integer.MAX_VALUE) {
                result *= reference;
                if (result == parsedValue)
                    return true;
            }
            break;
        case VALIDITYCHECKTYPE_VALUE_MULTIPLEOF:
            if (parsedValue % reference == 0)
                return true;
            break;
        }
        return false;
    }

    /**
     * Check if the float value represented by the input <code>String</code>
     * is complying with the range of the accepted values for the create option
     * 
     * @param checkingValue
     *                the string representing the float value need to be
     *                checked.
     * @return <code>true</code> if the specified value is complying with the
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
     * is complying with the reference value.
     * 
     * @param checkingValue
     *                the string representing the float value need to be checked
     * @return <code>true</code> if the provided value is complying with the
     *         reference value
     */
    private boolean checkFloatValue(String checkingValue) {
        final float reference = Float.parseFloat(validityValues[0]);
        final float parsedValue = Float.parseFloat(checkingValue);

        switch (validityCheckType) {
        case VALIDITYCHECKTYPE_VALUE_LESSTHANOREQUALTO:
            if (parsedValue <= reference)
                return true;
            break;

        case VALIDITYCHECKTYPE_VALUE_LESSTHAN:
            if (parsedValue < reference)
                return true;
            break;

        case VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO:
            if (parsedValue >= reference)
                return true;
            break;

        case VALIDITYCHECKTYPE_VALUE_GREATERTHAN:
            if (parsedValue > reference)
                return true;
            break;
        case VALIDITYCHECKTYPE_VALUE_MULTIPLEOF:
            // TODO: can I apply this rule to floating numbers?
            if (parsedValue % reference == 0)
                return true;
            break;
        }
        return false;
    }

    /**
     * Checks if the specified value is one of values supported by this option.
     * 
     * @param checkingValue
     *                the value need to be checked.
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
     *                the values need to be checked.
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

        // preliminary check: no value repetitions are allowed.
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

    /**
     * return the type of the represented value
     * 
     * @return the type of the represented value
     */
    public int getRepresentedValueType() {
        return representedValueType;
    }

    /**
     * set the type of the represented value
     */
    public void setRepresentedValueType(final int representedValueType) {
        this.representedValueType = representedValueType;
    }

}
