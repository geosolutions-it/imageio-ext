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
package it.geosolutions.hdf.object.h4;

import java.awt.image.DataBuffer;

import ncsa.hdf.hdflib.HDFConstants;

/**
 * Utility abstract class for retrieving datatype information and building
 * properly typed and properly sized data array where to load data values.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4DatatypeUtilities {

	// TODO: Should I change the datatype from int to this class?
	private H4DatatypeUtilities() {

	}

	/**
	 * Builds a properly typed and properly sized array to store a specific
	 * amount of data, given the type of data and its size.
	 * 
	 * @param datatype
	 *            the datatype of the data which will be stored in the array.
	 * @param size
	 *            the size of the required array
	 * @return the allocated array
	 */
	public static Object allocateArray(final int datatype, final int size) {
		if (size <= 0)
			return null;

		// //
		// 
		// Allocating a buffer of the required type and size.
		// 
		// //
		Object data = null;

		switch (datatype) {

		// Byte array
		case HDFConstants.DFNT_CHAR:
		case HDFConstants.DFNT_UCHAR8:
		case HDFConstants.DFNT_UINT8:
		case HDFConstants.DFNT_INT8:
			data = new byte[size];
			break;

		// short array
		case HDFConstants.DFNT_INT16:
		case HDFConstants.DFNT_UINT16:
			data = new short[size];
			break;

		// int array
		case HDFConstants.DFNT_INT32:
		case HDFConstants.DFNT_UINT32:
			data = new int[size];
			break;

		// long array
		case HDFConstants.DFNT_INT64:
		case HDFConstants.DFNT_UINT64:
			data = new long[size];
			break;

		// float array
		case HDFConstants.DFNT_FLOAT32:
			data = new float[size];
			break;

		// double array
		case HDFConstants.DFNT_FLOAT64:
			data = new double[size];
			break;

		// unrecognized datatype!!
		default:
			data = null;
			break;
		}

		return data;
	}

	/**
	 * Returns the size (in bytes) of a given datatype
	 * 
	 * @param datatype
	 *            the input datatype
	 * @return the size (in bytes) of a given datatype
	 */
	public static int getDataTypeSize(final int datatype) {
		switch (datatype) {
		case HDFConstants.DFNT_CHAR:
		case HDFConstants.DFNT_UCHAR8:
		case HDFConstants.DFNT_INT8:
		case HDFConstants.DFNT_UINT8:
			return 1;
		case HDFConstants.DFNT_INT16:
		case HDFConstants.DFNT_UINT16:
			return 2;
		case HDFConstants.DFNT_INT32:
		case HDFConstants.DFNT_UINT32:
		case HDFConstants.DFNT_FLOAT32:
			return 4;
		case HDFConstants.DFNT_INT64:
		case HDFConstants.DFNT_UINT64:
		case HDFConstants.DFNT_FLOAT64:
			return 8;
		default:
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if the provided datatype is unsigned;
	 * <code>false</code> otherwise.
	 * 
	 * @param datatype
	 *            the given datatype
	 * @return <code>true</code> if the provided datatype is unsigned;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean isUnsigned(final int datatype) {
		boolean unsigned = false;
		switch (datatype) {
		case HDFConstants.DFNT_UCHAR8:
		case HDFConstants.DFNT_UINT8:
		case HDFConstants.DFNT_UINT16:
		case HDFConstants.DFNT_UINT32:
		case HDFConstants.DFNT_UINT64:
			unsigned = true;
			break;
		}
		return unsigned;
	}

	/**
	 * Given a HDF datatype, returns a proper databuffer type depending on the
	 * datatype properties.
	 * 
	 * @param datatype
	 *            the input datatype
	 * @return the proper databuffer type
	 */
	public static int getBufferTypeFromDataType(final int datatype) {
		int bufferType = DataBuffer.TYPE_UNDEFINED;

		switch (datatype) {
		case HDFConstants.DFNT_INT8:
		case HDFConstants.DFNT_UINT8:
		case HDFConstants.DFNT_CHAR8:
		case HDFConstants.DFNT_UCHAR:
			bufferType = DataBuffer.TYPE_BYTE;
			break;
		case HDFConstants.DFNT_INT16:
			bufferType = DataBuffer.TYPE_SHORT;
			break;
		case HDFConstants.DFNT_UINT16:
			bufferType = DataBuffer.TYPE_USHORT;
			break;
		case HDFConstants.DFNT_INT32:
		case HDFConstants.DFNT_UINT32:
			bufferType = DataBuffer.TYPE_INT;
			break;
		case HDFConstants.DFNT_FLOAT32:
			bufferType = DataBuffer.TYPE_FLOAT;
			break;
		case HDFConstants.DFNT_FLOAT64:
			bufferType = DataBuffer.TYPE_DOUBLE;
			break;
		// TODO: Handle more cases??

		}
		return bufferType;
	}

	/**
	 * Return values contained in the provided data buffer of the specified
	 * datatype, as <code>String</code>.
	 * 
	 * @param datatype
	 *            the data type of values
	 * @param buf
	 *            a buffer containing data values of a specific type
	 */
	public static String getValuesAsString(int datatype, Object buf) {
		StringBuffer sb = new StringBuffer();
		if (datatype == HDFConstants.DFNT_FLOAT32
				|| datatype == HDFConstants.DFNT_FLOAT) {
			float[] ff = (float[]) buf;
			final int size = ff.length;
			for (int i = 0; i < size; i++) {
				sb.append((ff[i])).append(" ");
			}
		} else if (datatype == HDFConstants.DFNT_DOUBLE
				|| datatype == HDFConstants.DFNT_FLOAT64) {
			double[] dd = (double[]) buf;
			final int size = dd.length;
			for (int i = 0; i < size; i++) {
				sb.append((dd[i])).append(" ");
			}
		} else if (datatype == HDFConstants.DFNT_INT8
				|| datatype == HDFConstants.DFNT_UINT8) {
			byte[] bb = (byte[]) buf;
			final int size = bb.length;
			for (int i = 0; i < size; i++) {
				sb.append((bb[i])).append(" ");
			}
		} else if (datatype == HDFConstants.DFNT_INT16
				|| datatype == HDFConstants.DFNT_UINT16) {
			short[] ss = (short[]) buf;
			final int size = ss.length;
			for (int i = 0; i < size; i++) {
				sb.append((ss[i])).append(" ");
			}
		} else if (datatype == HDFConstants.DFNT_INT32
				|| datatype == HDFConstants.DFNT_UINT32) {
			int[] ii = (int[]) buf;
			final int size = ii.length;
			for (int i = 0; i < size; i++) {
				sb.append((ii[i])).append(" ");
			}
		} else if (datatype == HDFConstants.DFNT_CHAR
				|| datatype == HDFConstants.DFNT_UCHAR8) {

			byte[] bb = (byte[]) buf;
			final int size = bb.length;
			sb = new StringBuffer(size);
			for (int i = 0; i < size && bb[i] != 0; i++) {
				sb.append(new String(bb, i, 1));
			}
		}
		return sb.toString();
	}
}
