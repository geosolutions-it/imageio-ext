/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.plugins.netcdf;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;

import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.VariableIF;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

/**
 * Set of NetCDF utility methods.
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
public class NetCDFUtilities {

	public static class KeyValuePair implements Map.Entry<String, String> {
		
		public KeyValuePair(final String key, final String value){
			this.key = key;
			this.value = value;
		}
			
		private String key;
		private String value;

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		private boolean equal(Object a, Object b) {
			return a == b || a != null && a.equals(b);
		}

		public boolean equals(Object o) {
			return o instanceof KeyValuePair
					&& equal(((KeyValuePair) o).key, key)
					&& equal(((KeyValuePair) o).value, value);
		}

		private static int hashCode(Object a) {
			return a == null ? 42 : a.hashCode();
		}

		public int hashCode() {
			return hashCode(key) * 3 + hashCode(value);
		}

		public String toString() {
			return "(" + key + "," + value + ")";
		}

		public String setValue(String value) {
			this.value = value;
			return value;

		}
	}
	
    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger.getLogger(NetCDFUtilities.class.toString());
    
    private NetCDFUtilities() {

    }    

    public final static String LOWER_LEFT_LONGITUDE = "lower_left_longitude";

    public final static String LOWER_LEFT_LATITUDE = "lower_left_latitude";

    public final static String UPPER_RIGHT_LONGITUDE = "upper_right_longitude";

    public final static String UPPER_RIGHT_LATITUDE = "upper_right_latitude";

    public static final String COORDSYS = "latLonCoordSys";
    
    public final static String LATITUDE = "latitude";

    public final static String LAT = "lat";

    public final static String LONGITUDE = "longitude";

    public final static String LON = "lon";

    public final static String DEPTH = "depth";

    public final static String ZETA = "z";
    
	private static final String BOUNDS = "bounds";
    
    public final static String HEIGHT = "height";

    public final static String TIME = "time";
    
    public final static String COORDINATE_AXIS_TYPE = "_CoordinateAxisType";
    
    public static final String POSITIVE = "positive";
    
    public static final String UNITS = "units";
    
    public static final String NAME = "name";
    
    public static final String LONG_NAME = "long_name";
    
    
//    private final static HashMap TSS_OAG_ACCEPTED = new HashMap();
//
//    private final static HashMap TSS_PE_ACCEPTED = new HashMap();
//
//    static {
//        String[] values = new String[] { "temp", "temperr", "tempmean", "salt",
//                "salterr", "saltmean", "dynht", "dynhterr", "dynhtmean" };
//        int nValues = values.length;
//        for (int i = 0; i < nValues; i++) {
//            TSS_OAG_ACCEPTED.put(values[i], null);
//        }
//        values = new String[] { "temp", "temperr", "tempmean", "salt",
//                "salterr", "saltmean", "dynht", "dynhterr", "dynhtmean",
//                "vtot", "vclin", "NO3", "CELLNO3", "zoo", "NH4", "detritus",
//                "CHL", "CELLNH4", "NH4pr", "NH3pr", "zgrphy", "u", "v" };
//        nValues = values.length;
//        for (int i = 0; i < nValues; i++) {
//            TSS_PE_ACCEPTED.put(values[i], null);
//        }
//    }

    public static enum CheckType {
        /*OAG, PE_MODEL,*/ NONE, UNSET
    }

    /**
     * The dimension <strong>relative to the rank</strong> in {@link #variable}
     * to use as image width. The actual dimension is
     * {@code variable.getRank() - X_DIMENSION}. Is hard-coded because the loop
     * in the {@code read} method expects this order.
     */
    public static final int X_DIMENSION = 1;

    /**
     * The dimension <strong>relative to the rank</strong> in {@link #variable}
     * to use as image height. The actual dimension is
     * {@code variable.getRank() - Y_DIMENSION}. Is hard-coded because the loop
     * in the {@code read} method expects this order.
     */
    public static final int Y_DIMENSION = 2;

    /**
     * The default dimension <strong>relative to the rank</strong> in
     * {@link #variable} to use as Z dimension. The actual dimension is
     * {@code variable.getRank() - Z_DIMENSION}.
     * <p>
     */
    public static final int Z_DIMENSION = 3;

    /**
     * The data type to accept in images. Used for automatic detection of which
     * variables to assign to images.
     */
    public static final Set<DataType> VALID_TYPES = new HashSet<DataType>(12);

	

    static {
        VALID_TYPES.add(DataType.BOOLEAN);
        VALID_TYPES.add(DataType.BYTE);
        VALID_TYPES.add(DataType.SHORT);
        VALID_TYPES.add(DataType.INT);
        VALID_TYPES.add(DataType.LONG);
        VALID_TYPES.add(DataType.FLOAT);
        VALID_TYPES.add(DataType.DOUBLE);
    }

    /**
     * Utility method to retrieve the t-index of a Variable descriptor stored on
     * {@link NetCDFImageReader} NetCDF Flat Reader {@link HashMap} indexMap.
     * 
     * @param var
     *                {@link Variable}
     * @param range
     *                {@link Range}
     * @param imageIndex
     *                {@link int}
     * 
     * @return t-index {@link int} -1 if variable rank > 4
     */
    public static int getTIndex(Variable var, Range range, int imageIndex) {
        final int rank = var.getRank();

        if (rank > 2) {
            if (rank == 3) {
                return (imageIndex - range.first());
            } else {
                // return (imageIndex - range.first()) % var.getDimension(rank -
                // (Z_DIMENSION + 1)).getLength();
                return (int) Math.ceil((imageIndex - range.first())
                        / getZDimensionLength(var));
            }
        }

        return -1;
    }

    private static int getZDimensionLength(Variable var) {
        final int rank = var.getRank();
        if (rank > 2) {
            return var.getDimension(rank - Z_DIMENSION).getLength();
        }
        // TODO: Should I avoid use this method in case of 2D Variables?
        return 0;
    }

    // public static int getTIndex(NetCDFImageReader reader, int imageIndex, ) {
    // Range range = reader.getRange(imageIndex);
    // if (range != null) {
    // Variable variable = reader.getVariable(imageIndex);
    // if (range != null && variable != null)
    // return getTIndex(variable, range, imageIndex);
    // }
    // return -1;
    // }
    //
    // public static int getZIndex(NetCDFImageReader reader, int imageIndex) {
    // Range range = reader.getRange(imageIndex);
    // if (range != null) {
    // Variable variable = reader.getVariable(imageIndex);
    // if (range != null && variable != null)
    // return getZIndex(variable, range, imageIndex);
    // }
    // return -1;
    // }

    /**
     * Utility method to retrieve the z-index of a Variable descriptor stored on
     * {@link NetCDFImageReader} NetCDF Flat Reader {@link HashMap} indexMap.
     * 
     * @param var
     *                {@link Variable}
     * @param range
     *                {@link Range}
     * @param imageIndex
     *                {@link int}
     * 
     * @return z-index {@link int} -1 if variable rank &lt; 3
     */
    public static int getZIndex(Variable var, Range range, int imageIndex) {
        final int rank = var.getRank();

        if (rank > 2) {
            if (rank == 3) {
                return (imageIndex - range.first());
            } else {
                // return (int) Math.ceil((imageIndex - range.first()) /
                // var.getDimension(rank - (Z_DIMENSION + 1)).getLength());
                return (imageIndex - range.first()) % getZDimensionLength(var);
            }
        }

        return -1;
    }

    /**
     * Returns the data type which most closely represents the "raw" internal
     * data of the variable. This is the value returned by the default
     * implementation of {@link NetcdfImageReader#getRawDataType}.
     * 
     * @param variable
     *                The variable.
     * @return The data type, or {@link DataBuffer#TYPE_UNDEFINED} if unknown.
     * 
     * @see NetcdfImageReader#getRawDataType
     */
    public static int getRawDataType(final VariableIF variable) {
        VariableDS ds = (VariableDS) variable;
        final DataType type = ds.getOriginalDataType();
        if (DataType.BOOLEAN.equals(type) || DataType.BYTE.equals(type)) {
            return DataBuffer.TYPE_BYTE;
        }
        if (DataType.CHAR.equals(type)) {
            return DataBuffer.TYPE_USHORT;
        }
        if (DataType.SHORT.equals(type)) {
            return variable.isUnsigned() ? DataBuffer.TYPE_USHORT
                    : DataBuffer.TYPE_SHORT;
        }
        if (DataType.INT.equals(type)) {
            return DataBuffer.TYPE_INT;
        }
        if (DataType.FLOAT.equals(type)) {
            return DataBuffer.TYPE_FLOAT;
        }
        if (DataType.LONG.equals(type) || DataType.DOUBLE.equals(type)) {
            return DataBuffer.TYPE_DOUBLE;
        }
        return DataBuffer.TYPE_UNDEFINED;
    }

    public static String getAttributesAsString(Attribute attr) {
        return getAttributesAsString(attr, false);
    }
    
    public static String getAttributesAsString(final Variable var, final String attributeName) {
        String value = "";
        if (var != null){
        	Attribute attribute = var.findAttribute(attributeName);
        	if (attribute != null)
        		value = getAttributesAsString(attribute, false); 
        	
        }
    	return value;
    }
    
    public static Number getAttributesAsNumber(final Variable var, final String attributeName) {
        Number value = null;
        if (var != null){
        	Attribute attribute = var.findAttribute(attributeName);
        	if (attribute != null)
        		value = attribute.getNumericValue(); 
        	
        }
    	return value;
    }

    /**
     * Return the value of a NetCDF {@code Attribute} instance as a
     * {@code String}. The {@code isUnsigned} parameter allow to handle byte
     * attributes as unsigned, in order to represent values in the range
     * [0,255].
     */
    public static String getAttributesAsString(Attribute attr,
            final boolean isUnsigned) {
        String values[] = null;
        if (attr != null) {
            final int nValues = attr.getLength();
            values = new String[nValues];
            final DataType datatype = attr.getDataType();

            // TODO: Improve the unsigned management
            if (datatype == DataType.BYTE) {
                if (isUnsigned)
                    for (int i = 0; i < nValues; i++) {
                        byte val = attr.getNumericValue(i).byteValue();
                        int myByte = (0x000000FF & ((int) val));
                        short anUnsignedByte = (short) myByte;
                        values[i] = Short.toString(anUnsignedByte);
                    }
                else {
                    for (int i = 0; i < nValues; i++) {
                        byte val = attr.getNumericValue(i).byteValue();
                        values[i] = Byte.toString(val);
                    }
                }
            } else if (datatype == DataType.SHORT) {
                for (int i = 0; i < nValues; i++) {
                    short val = attr.getNumericValue(i).shortValue();
                    values[i] = Short.toString(val);
                }
            } else if (datatype == DataType.INT) {
                for (int i = 0; i < nValues; i++) {
                    int val = attr.getNumericValue(i).intValue();
                    values[i] = Integer.toString(val);
                }
            } else if (datatype == DataType.LONG) {
                for (int i = 0; i < nValues; i++) {
                    long val = attr.getNumericValue(i).longValue();
                    values[i] = Long.toString(val);
                }
            } else if (datatype == DataType.DOUBLE) {
                for (int i = 0; i < nValues; i++) {
                    double val = attr.getNumericValue(i).doubleValue();
                    values[i] = Double.toString(val);
                }
            } else if (datatype == DataType.FLOAT) {
                for (int i = 0; i < nValues; i++) {
                    float val = attr.getNumericValue(i).floatValue();
                    values[i] = Float.toString(val);
                }

            } else if (datatype == DataType.STRING) {
                for (int i = 0; i < nValues; i++) {
                    values[i] = attr.getStringValue(i);
                }
            } else {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning("Unhandled Attribute datatype "
                            + attr.getDataType().getClassType().toString());
            }
        }
        String value = "";
        if (values != null) {
            StringBuffer sb = new StringBuffer();
            int j = 0;
            for (; j < values.length - 1; j++) {
                sb.append(values[j]).append(",");
            }
            sb.append(values[j]);
            value = sb.toString();
        }
        return value;
    }

    public static class ProjAttribs {
        public final static String PROJECT_TO_IMAGE_AFFINE = "proj_to_image_affine";

        public final static String PROJECT_ORIGIN_LATITUDE = "proj_origin_latitude";

        public final static String PROJECT_ORIGIN_LONGITUDE = "proj_origin_longitude";

        public final static String EARTH_FLATTENING = "earth_flattening";

        public final static String EQUATORIAL_RADIUS = "equatorial_radius";

        public final static String STANDARD_PARALLEL_1 = "std_parallel_1";

        private ProjAttribs() {

        }
    }

    public static class DatasetAttribs {
        public final static String VALID_RANGE = "valid_range";

        public final static String VALID_MIN = "valid_min";

        public final static String VALID_MAX = "valid_max";

        public final static String LONG_NAME = "long_name";

        public final static String FILL_VALUE = "_FillValue";
        
        public final static String MISSING_VALUE = "missing_value";

        public final static String SCALE_FACTOR = "scale_factor";

        // public final static String SCALE_FACTOR_ERR = "scale_factor_err";

        public final static String ADD_OFFSET = "add_offset";

        // public final static String ADD_OFFSET_ERR = "add_offset_err";
        public final static String UNITS = "units";

        private DatasetAttribs() {

        }
    }

    /**
     * NetCDF files may contains a wide set of variables. Some of them are
     * unuseful for our purposes. The method returns {@code true} if the
     * specified variable is accepted.
     */
    public static boolean isVariableAccepted(final Variable var,
            final CheckType checkType) {
        if (var instanceof CoordinateAxis1D) {
            return false;
        } else
            return isVariableAccepted(var.getName(), checkType);
    }

    /**
     * NetCDF files may contains a wide set of variables. Some of them are
     * unuseful for our purposes. The method returns {@code true} if the
     * specified variable is accepted.
     */
    public static boolean isVariableAccepted(final String name,
            final CheckType checkType) {
        if (checkType == CheckType.NONE) {
            if (name.equalsIgnoreCase(LATITUDE)
                    || name.equalsIgnoreCase(LONGITUDE)
                    || name.equalsIgnoreCase(LON) 
                    || name.equalsIgnoreCase(LAT)
                    || name.equalsIgnoreCase(TIME)
                    || name.equalsIgnoreCase(DEPTH)
                    || name.equalsIgnoreCase(ZETA)
                    || name.equalsIgnoreCase(HEIGHT)
                    || name.toLowerCase().contains(COORDSYS.toLowerCase())
                    || name.endsWith(BOUNDS))
                
                return false;
            else
                return true;
        } 
//        else if (checkType == CheckType.OAG)
//            return TSS_OAG_ACCEPTED.containsKey(name);
//        else if (checkType == CheckType.PE_MODEL)
//            return TSS_PE_ACCEPTED.containsKey(name);
        return true;

    }

    /**
     * Returns a {@code NetcdfDataset} given an input object
     * 
     * @param input
     *                the input object (usually a {@code File}, a
     *                {@code String} or a {@code FileImageInputStreamExt).
     * @return {@code NetcdfDataset} in case of success.
     * @throws IOException
     *                 if some error occur while opening the dataset.
     * @throws {@link IllegalArgumentException}
     *                 in case the specified input is a directory
     */
    public static NetcdfDataset getDataset(Object input) throws IOException {
        NetcdfDataset dataset = null;
        if (input instanceof File) {
            if (!((File) input).isDirectory())
                dataset = NetcdfDataset.openDataset(((File) input).getPath());
            else
                throw new IllegalArgumentException("Error occurred during NetCDF file reading: The input file is a Directory.");
        } else if (input instanceof String) {
            File file = new File((String) input);
            if (!file.isDirectory())
                dataset = NetcdfDataset.openDataset(file.getPath());
            else
                throw new IllegalArgumentException( "Error occurred during NetCDF file reading: The input file is a Directory.");
        } else if (input instanceof URL) {
            final URL tempURL = (URL) input;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                File file = URLImageInputStreamSpi.urlToFile(tempURL);
                if (!file.isDirectory())
                    dataset = NetcdfDataset.openDataset(file.getPath());
                else
                    throw new IllegalArgumentException( "Error occurred during NetCDF file reading: The input file is a Directory.");
            }
        }

        else if (input instanceof FileImageInputStreamExt) {
            File file = ((FileImageInputStreamExt) input).getFile();
            if (!file.isDirectory())
                dataset = NetcdfDataset.openDataset(file.getPath());
            else
                throw new IllegalArgumentException("Error occurred during NetCDF file reading: The input file is a Directory.");
        }
        return dataset;
    }

    /**
     * Depending on the type of model/netcdf file, we will check for the
     * presence of some variables rather than some others. The method returns
     * the type of check on which we need to leverage to restrict the set of
     * interesting variables. The method will check for some
     * KEY/FLAGS/ATTRIBUTES within the input dataset in order to define the
     * proper check type to be performed.
     * 
     * @param dataset
     *                the input dataset.
     * @return the proper {@link CheckType} to be performed on the specified
     *         dataset.
     */
    public static CheckType getCheckType(NetcdfDataset dataset) {
        CheckType ct = CheckType.UNSET;
        if (dataset != null) {
            ct = CheckType.NONE;
//            Attribute attribute = dataset.findGlobalAttribute("type");
//            if (attribute != null) {
//                String value = attribute.getStringValue();
//                if (value.length() <= 3 && value.contains("OA"))
//                    ct = CheckType.OAG;
//                else if (value.contains("PE MODEL"))
//                    ct = CheckType.PE_MODEL;
//            }
        }
        return ct;
    }
    
    /**
     * Return a global attribute as a {@code String}. The required global
     * attribute is specified by name
     * 
     * @param attributeName
     *                the name of the required attribute.
     * @return the value of the required attribute. Returns an empty String in
     *         case the required attribute is not found.
     */
    public static String getGlobalAttributeAsString(final NetcdfDataset dataset, final String attributeName) {
        String attributeValue = "";
        if (dataset != null) {
        	final Attribute attrib = dataset.findGlobalAttribute(attributeName);
//        	final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
//            if (globalAttributes != null && !globalAttributes.isEmpty()) {
//                for (Attribute attrib: globalAttributes){
                    if (attrib.getName().equals(attributeName)) {
                        attributeValue = NetCDFUtilities
                                .getAttributesAsString(attrib);
//                        break;
                    }
//                }
//            }
        }
        return attributeValue;
    }

    public static KeyValuePair getGlobalAttribute(final NetcdfDataset dataset, final int attributeIndex) throws IOException {
    	KeyValuePair attributePair = null;
        if (dataset != null) {
        	final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
            if (globalAttributes != null && !globalAttributes.isEmpty()) {
            	final Attribute attribute = (Attribute) globalAttributes
                        .get(attributeIndex);
                if (attribute != null) {
                    attributePair = new KeyValuePair(attribute.getName(),
                    		NetCDFUtilities.getAttributesAsString(attribute));
                }
            }
        }
        return attributePair;
    }

	public static KeyValuePair getAttribute(final Variable var, 
			final int attributeIndex) {
		KeyValuePair attributePair = null;
		if (var != null){
			final List<Attribute> attributes = var.getAttributes();
		    if (attributes != null && !attributes.isEmpty()) {
		    	final Attribute attribute = (Attribute) attributes
		                .get(attributeIndex);
		        if (attribute != null) {
		            attributePair = new KeyValuePair(attribute.getName(),
		                    NetCDFUtilities.getAttributesAsString(attribute));
		        }
		    }
		}
	    return attributePair;
	}
}
