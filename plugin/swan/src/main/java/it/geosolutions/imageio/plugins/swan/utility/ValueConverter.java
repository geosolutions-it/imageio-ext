/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.swan.utility;

/**
 * This abstract class provides to compute a double value, given its
 * representation as a byte sequence
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini
 * 
 */
public abstract class ValueConverter {

	public static double getValue(byte[] buffer, int decimalDigitsNum) {
		final int nChars = buffer.length;
		double value = 0.0f;
		// buffer[0] is always a space
		// buffer[1] may be a space (' ') or a minus sign ('-')
		// buffer[2] is always a zero.
		// buffer[3] is always a dot char: '.'
		// from buffer[4] to buffer[nChars-5] we have decimal digits.
		// buffer[nChars-4] is always an ('E') char.
		// buffer[nChars-3] may be a plus or a minus sign ('+' or '-')
		// from buffer[nChars-2] to buffer[nChars-1] we have exponent digits.

		// remind the ascii table:
		// space = 32
		// '+' = 43
		// '-' = 45
		// '.' = 46
		// '0' = 48
		// '1' = 49
		// ...
		// '9' = 57
		// 'E' = 69
		// 'e' = 101

		// Do a preliminar consistency check

		if (buffer[0] != 32 || ((buffer[1] != 32) && (buffer[1] != 45))
				|| (buffer[2] != 48)
				|| ((buffer[nChars - 4] != 69) && (buffer[nChars - 4] != 101))
				|| ((buffer[nChars - 3] != 43) && (buffer[nChars - 3] != 45))) {
			throw new NumberFormatException("An Illegal number was found:\n");
			// TODO: produce a better error explaination
		}

		double multiplier = 0.1;

		// build the decimal value
		for (int i = 0; i < decimalDigitsNum; i++) {
			value += (buffer[4 + i] - 48) * (multiplier);
			multiplier /= 10.0;
		}

		// find the exponent
		double exponent = ((buffer[nChars - 1] - 48))
				+ ((buffer[nChars - 2] - 48) * 10);

		// set the proper exponent sign
		if (buffer[nChars - 3] == 45)
			exponent *= -1;

		// compute the value (V*10^Exp)
		value *= Math.pow(10.0, exponent);

		// set the proper exponent sign
		if (buffer[1] == 45)
			value *= -1;

		// return the value
		return value;
	}

}
