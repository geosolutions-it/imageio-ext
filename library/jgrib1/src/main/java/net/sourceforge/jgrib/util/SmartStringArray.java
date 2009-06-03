/*
 *    JGriB1 - OpenSource Java library for GriB files edition 1
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package net.sourceforge.jgrib.util;


/**
 * Class to handle addition of elements 
 * @author Kjell Røang, 18/03/2002
 * @author Simone Giannecchini
 */
public final class SmartStringArray {
	private static final int START = 64;

	int sp = 0; // "stack pointer" to keep track of position in the array

	private String[] array;

	private int growthSize;

	public SmartStringArray() {
		this(START);
	}

	public SmartStringArray(final int initialSize) {
		this(initialSize, (initialSize / 4));
	}

	public SmartStringArray(final int initialSize, final int growthSize) {
		this.growthSize = growthSize;
		array = new String[initialSize];
		
	}

	public SmartStringArray(final String[] anArr) {
		array = anArr;
		growthSize = START / 4;
		sp = array.length;
	}

	/**
	 * Reset stack pointer
	 */
	public void reset() {
		sp = 0;
	}

	/**
	 * Add one string
	 * 
	 * @param str
	 *            DOCUMENT ME!
	 */
	public void add(final String str) {
		if (sp >= array.length) // time to grow!
		{
			String[] tmpArray = new String[array.length + growthSize];

			System.arraycopy(array, 0, tmpArray, 0, array.length);
			array = tmpArray;
		}

		array[sp] = str;
		sp += 1;
	}

	/**
	 * Return normal array
	 * 
	 * @return DOCUMENT ME!
	 */
	public String[] toArray() {
		final String[] trimmedArray = new String[sp];

		System.arraycopy(array, 0, trimmedArray, 0, trimmedArray.length);

		return trimmedArray;
	}

	public int size() {
		return sp;
	}

	/**
	 * Split string in an array
	 * 
	 * @param token
	 *            DOCUMENT ME!
	 * @param string
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String[] split(final String token, final String string) {
		final SmartStringArray ssa = new SmartStringArray();

		int previousLoc = 0;
		int loc = string.indexOf(token, previousLoc);

		if (loc == -1) {
			// No token in string
			ssa.add(string);

			return (ssa.toArray());
		}

		final int strLength = string.length();
		final int tokenLength = token.length();

		do {

			ssa.add(string.substring(previousLoc, loc));
			previousLoc = (loc + tokenLength);
			loc = string.indexOf(token, previousLoc);
		} while ((loc != -1) && (previousLoc < strLength));


		ssa.add(string.substring(previousLoc));

		return (ssa.toArray());
	}

	/**
	 * Remove array elements with blanks and blanks inside a string Alter number
	 * of elements
	 * 
	 * @param inArr
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String[] removeBlanks(final String[] inArr) {
		// Count number of non blanks
		int nonBl = 0;
		int length = inArr.length;

		for (int i = 0; i < length; i++) {
			String inStr = inArr[i].trim();

			if (inStr.length() != 0) {
				nonBl++;
			}

			inArr[i] = inStr;
		}

		if (nonBl == inArr.length) {
			return inArr;
		}

		// Copy to new
		String[] outArr = new String[nonBl];
		length = nonBl;
		nonBl = 0;
		String inStr;
		for (int i = 0; i < length; i++) {
			inStr = inArr[i].trim();

			if (inStr.length() != 0) {
				outArr[nonBl] = inStr;
				nonBl++;
			}
		}

		return outArr;
	}

	/**
	 * Join some elements with a token in between
	 * 
	 * @param token
	 *            DOCUMENT ME!
	 * @param strings
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String join(final String token, final String[] strings) {
		final StringBuffer sb = new StringBuffer();

		final int length = strings.length;
		for (int x = 0; x < length; x++) {
			if (strings[x] != null) {
				sb.append(strings[x]);
			}

			if (x < (length - 1)) {
				sb.append(token);
			}
		}

		return (sb.toString());
	}

}
