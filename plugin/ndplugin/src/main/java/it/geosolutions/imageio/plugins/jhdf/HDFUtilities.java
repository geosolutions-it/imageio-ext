package it.geosolutions.imageio.plugins.jhdf;

import java.awt.image.DataBuffer;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Datatype;

public class HDFUtilities {

	/**
	 * Given a HDF Attribute, builds a String containing comma separated
	 * values related to the attribute. Some Attribute may have a int 
	 * array as value.  
	 * 
	 * @param att
	 * 			a HDF <code>Attribute</code>.
	 * @return
	 * 			the built <code>String</code>
	 */
	
	public static String buildAttributeString(Attribute att) {

		//TODO: Add more type handler
		final Datatype dataType = att.getType();
		final int attribTypeClass = dataType.getDatatypeClass();
		final int attribTypeSize = dataType.getDatatypeSize();
		Object valuesList = att.getValue();
		String attribValue = "";
		if (valuesList != null) {

			int i = 0;
			final StringBuffer sb = new StringBuffer();
			switch (attribTypeClass) {
			case Datatype.CLASS_ARRAY:

				break;
			case Datatype.CLASS_BITFIELD:

				break;
			case Datatype.CLASS_CHAR:
				final String[] strValues = (String[]) valuesList;
				final int numValues = strValues.length;
				for (; i < numValues - 1; i++) {
					sb.append(strValues[i]).append(",");
				}
				sb.append(strValues[i]);
				break;
			case Datatype.CLASS_FLOAT:
				switch (attribTypeSize){
				case 4://32 bit floating point
					final float[] fValues = (float[]) valuesList;
					final int fNumValues = fValues.length;
					for (; i < fNumValues - 1; i++) {
						sb.append(fValues[i]).append(",");
					}
					sb.append(fValues[i]);
					break;
				case 8://64 bit floating point
					final double[] dValues = (double[]) valuesList;
					final int dNumValues = dValues.length;
					for (; i < dNumValues - 1; i++) {
						sb.append(dValues[i]).append(",");
					}
					sb.append(dValues[i]);
					break;
				}
				break;
			case Datatype.CLASS_INTEGER:
				switch (attribTypeSize){
				case 2://16 bit integers 
					final short[] sValues = (short[]) valuesList;
					final int sNumValues = sValues.length;
					for (; i < sNumValues - 1; i++) {
						sb.append(sValues[i]).append(",");
					}
					sb.append(sValues[i]);
					break;
				case 4://32 bit integers 
					final int[] iValues = (int[]) valuesList;
					final int iNumValues = iValues.length;
					for (; i < iNumValues - 1; i++) {
						sb.append(iValues[i]).append(",");
					}
					sb.append(iValues[i]);
					break;
				case 8://64 bit integers
					final long[] lValues = (long[]) valuesList;
					final int lNumValues = lValues.length;
					for (; i < lNumValues - 1; i++) {
						sb.append(lValues[i]).append(",");
					}
					sb.append(lValues[i]);
					break;
				}
				break;
			case Datatype.CLASS_STRING:

				break;
			}
			attribValue=sb.toString();
		}
		return attribValue;
	}
	
	/**
	 * Given a HDF datatype, returns a proper DataBuffer type depending on 
	 * the datatype size and the datatype class.
	 * 
	 * @param datatype
	 * 			the input datatype 
	 * @return the proper buffer type
	 */
	public static int getBufferTypeFromDataType(Datatype datatype) {
		int buffer_type=0;
		final int dataTypeClass = datatype.getDatatypeClass();
		final int dataTypeSize = datatype.getDatatypeSize();
		final boolean isUnsigned = datatype.isUnsigned();
		if (dataTypeClass == Datatype.CLASS_INTEGER) {
			if (dataTypeSize == 1)
				buffer_type = DataBuffer.TYPE_BYTE;
			else if (dataTypeSize == 2) {
				if (isUnsigned)
					buffer_type = DataBuffer.TYPE_USHORT;
				else
					buffer_type = DataBuffer.TYPE_SHORT;
			} else if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_INT;
		} else if (dataTypeClass == Datatype.CLASS_FLOAT)
			if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_FLOAT;
			else if (dataTypeSize == 8)
				buffer_type = DataBuffer.TYPE_DOUBLE;
		return buffer_type;
	}
}
