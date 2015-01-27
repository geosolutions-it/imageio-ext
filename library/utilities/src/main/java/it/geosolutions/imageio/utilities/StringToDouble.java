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
package it.geosolutions.imageio.utilities;

import java.lang.ref.SoftReference;

/**
 * This class is responsible for converting a sequence of chars into a a double
 * number.
 * 
 * <p>
 * It is a utility class uses by the {@link AsciiGridRaster} class for
 * converting the input {@link String} into numbers. This class is highly
 * optimized for performances reasons.
 * 
 * <p>
 * This class is not thread safe!!!
 * 
 * 
 * <p>
 * <strong>Usage</strong>
 * <p>
 * 
 * <pre>
 *        	final StringToDouble converter = new StringToDouble();
 *          // If I need to load 10 samples, I need to count 9 spaces
 *        	while (...) {
 *        			// /////////////////////////////////////////////////////////////////
 *        			// 
 *        			// Read the char
 *        			//
 *        			// /////////////////////////////////////////////////////////////////       
 *        			ch = read();
 *        		
 *        
 *        			// /////////////////////////////////////////////////////////////////
 *        			// 
 *        			// Push the char into the converter
 *        			//
 *        			// /////////////////////////////////////////////////////////////////
 *        			if (converter.pushChar(ch)) {
 *        
 *        				// /////////////////////////////////////////////////////////////////
 *        				// 
 *        				// Get the new value
 *        				//
 *        				// /////////////////////////////////////////////////////////////////
 *        				value = converter.getValue(); 
 *        
 *        				// /////////////////////////////////////////////////////////////////
 *        				// 
 *        				// Consume the new value
 *        				//
 *        				// /////////////////////////////////////////////////////////////////
 *        				...
 *        				...
 *        				...
 *        
 *        			} else {
 *        				// /////////////////////////////////////////////////////////////////
 *        				// 
 *        				// Let's check the EOF.
 *        				//
 *        				// /////////////////////////////////////////////////////////////////
 *        				if (converter.isEof()) {
 *        					...
 *        					...
 *        					...
 *        				}
 *        			}
 *        		}
 * </pre>
 * 
 * <p>
 * It is worth to point out that when using this class it would be great to add
 * to the JVM the hint to augment the lifetime of {@link SoftReference} objects
 * since the underlying pool is based on them.
 * <p>
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class StringToDouble {
//	/**
//	 * Default number of {@link StringToDouble} object to keep in the pool.
//	 * 
//	 * <p>
//	 * I use this value also when enlarging the pool.
//	 */
//	private static final int CONVERTER_NUM = 50;
//
//	/**
//	 * Static pool of double converters.
//	 */
//	static List pool;
//
//	static {
//		pool = Collections.synchronizedList(new ArrayList(CONVERTER_NUM));
//		enlargePool(CONVERTER_NUM);
//	}
//
//	private static void enlargePool(final int num) {
//		for (int i = 0; i < num; i++)
//			pool.add(new SoftReference(new StringToDouble()));
//	}

	// variables for arithmetic operations
	private double value = 0.0;

	private int prevCh = -1;

	private boolean eof = false;

	private final StringBuilder builder = new StringBuilder();

	/**
	 * Constructor.
	 * 
	 */
	private StringToDouble() {

	}

	/**
	 * Resets the converter.
	 * 
	 * <p>
	 * This method should be called each time a new value is consumed by using
	 * {@link StringToDouble#compute()} method.
	 * 
	 */
	public void reset() {
		// Resetting Values
		value = 0.0;
		eof = false;
		prevCh = -1;
		builder.setLength(0);

	}

	/**
	 * Pushes a new character to this converter.
	 * 
	 * <p>
	 * The converter parses the char and check if we have a new value. The value
	 * is not computed unless requested for performance reasons. Often it is
	 * needed to throw away values that are not needed because of subsampling or
	 * the like, hence it is a waste of resource explicitly computing them.
	 * 
	 * <p>
	 * It is worth to point out that after getting a false value it is crucial
	 * to check the {@link StringToDouble#isEof()} to see if we reached to eof
	 * condition.
	 * 
	 * @param newChar
	 *            to parse.
	 * @return true if there is a value to get, false otherwise.
	 * @see {@link StringToDouble#isEof()}
	 */
	public boolean pushChar(final int newChar) {
		boolean retVal = false;
		// check if we read a white space or similar
		if ((newChar != 32) && (newChar != 10) && (newChar != 13)
				&& (newChar != 9) && (newChar != 0)) {
			if ((prevCh == 32) // ' '
					|| (prevCh == 10) // '\r'
					|| (prevCh == 13) // '\n'
					|| (prevCh == 9) // '\t'
					|| (prevCh == 0)) {
				// //
				//
				// End of white spaces. I need to convert read bytes
				// in a double value and I set it as a sample of the
				// raster, if subsampling allows it.
				//
				// //

				// we found a value
				retVal = true;
				value = compute();
				reset();

			}

			// //
			//
			// Analysis of current byte for next value
			//
			// //
			switch (newChar) {
			case 48: // '0'
			case 49: // '1'
			case 50: // '2'
			case 51: // '3'
			case 52: // '4'
			case 53: // '5'
			case 54: // '6'
			case 55: // '7'
			case 56: // '8'
			case 57: // '9'
				builder.append(newChar - 48);
				retVal = true;
				break;

			case 44: // ',' generated by ArcGIS in some environments
			case 46: // '.'
				builder.append('.');
				retVal = true;
				break;

			case 45: // '-'
				builder.append('-');
				retVal = true;
				break;

			case 43: // '+'
				builder.append('+');
				retVal = true;
				break;

			case 42: // '*' NoData in GRASS Format
				retVal = true;
				value = Double.NaN;
				break;

			case 69: // 'E'
			case 101: // 'e'
				builder.append('E');
				retVal = true;
				break;

			case -1:
				retVal = true;
				eof = true;
				break;

			default:
				throw new NumberFormatException(new StringBuilder(
						"Invalid data value was found. ASCII CODE : ").append(
						newChar).toString());
			}

		}
		prevCh = newChar; // store this byte for some checks
		return retVal;
	}

	/**
	 * Returns a value, if any was advertised by the
	 * {@link StringToDouble#pushChar(int)} method.
	 * 
	 * @return
	 * 
	 * @return the computed value;
	 */
	public double compute() {
		if (!Double.isNaN(value)) {
			value = Double.parseDouble(builder.toString());
			builder.setLength(0);
		}
		return value;
	}

	/**
	 * Did we find an EOF?
	 * 
	 */
	boolean isEof() {
		return eof;
	}

	/**
	 * Retrieves a poole {@link StringToDouble} object.
	 * 
	 */
	public static StringToDouble acquire() {
//		synchronized (pool) {
//			SoftReference r;
//			Object o;
//			while (pool.size() > 0) {
//				r = (SoftReference) pool.remove(0);
//				o = r.get();
//				if (o != null) {
//					StringToDouble stf = (StringToDouble) o;
//					stf.reset();
//					return stf;
//				}
//
//			}
//			// we did not find any
//			enlargePool(CONVERTER_NUM - 1);
			return new StringToDouble();
//		}
	}

	/**
	 * Reacquire a pooled {@link StringToDouble}.
	 * 
	 * @param c
	 */
	public static void release(StringToDouble c) {
		c.builder.setLength(0);
		c.builder.trimToSize();
//		synchronized (pool) {
//			pool.add(new SoftReference(c));
//		}
	}
}
