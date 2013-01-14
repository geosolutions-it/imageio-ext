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
package it.geosolutions.imageio.plugins.nitronitf;

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam.Compression;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam.ProgressionOrder;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageWriteParam;

import nitf.Field;
import nitf.FieldType;
import nitf.NITFException;
import nitf.TRE;

/**
 * Utility class storing default values and constants, as well as a set of methods to do fields setting,
 * field validation and compression parameters management.
 *  
 * @author Daniele Romagnoli, GeoSolutions SaS
 *
 */
public class NITFUtilities {

    final static int DEFAULT_TILE_WIDTH = 1024;

    final static int DEFAULT_TILE_HEIGHT = 1024;
    
    final static double BPPPB[] = new double[] { 0.03125, 0.0625, 0.125, 0.25, 0.5, 0.6,
        0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.5, 1.7, 2.0, 2.3, 3.5, 3.9, 0 };
    
    final static double BPPPB_15_1[] = new double[] { 0.0125, 0.025, 0.0375, 0.05, 0.0625, 0.075,
            0.1, 0.2, 0.3, 0.4, 0.41, 0.42, 0.43, 0.44, 0.45, 0.475, 0.5, 0.52, 1d/1.875d };
    
    final static double BPPPB_19[] = new double[] { 0.03125, 0.0625, 0.125, 0.25, 0.5, 0.6,
        0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.5, 1.7, 2.0, 2.3, 3.5, 3.9 };

    public enum WriteCompression {
        UNCOMPRESSED {
            @Override
            double[] getBitRates() {
                return null;
            }

            @Override
            int getQualityLayers() {
                return 1;
            }

            @Override
            Compression getCompression() {
                return Compression.UNDEFINED;
            }
        }, NPJE_VL {
            @Override
            double[] getBitRates() {
                return BPPPB_19;
            }

            @Override
            int getQualityLayers() {
                return BPPPB_19.length;
            }
            
            @Override
            Compression getCompression() {
                return Compression.LOSSY;
            }
        }, EPJE_VL {
            @Override
            double[] getBitRates() {
                return BPPPB_19;
            }

            @Override
            int getQualityLayers() {
                return BPPPB_19.length;
            }
            
            @Override
            Compression getCompression() {
                return Compression.LOSSY;
            }
        }, NPJE_NL {
            @Override
            double[] getBitRates() {
                return BPPPB;
            }

            @Override
            int getQualityLayers() {
                return BPPPB.length;
            }
            
            @Override
            Compression getCompression() {
                return Compression.NUMERICALLY_LOSSLESS;
            }
        }, EPJE_NL {
            @Override
            double[] getBitRates() {
                return BPPPB;
            }

            @Override
            int getQualityLayers() {
                return BPPPB.length;
            }
            
            @Override
            Compression getCompression() {
                return Compression.NUMERICALLY_LOSSLESS;
            }
        }, RATIO_15_1 {
            @Override
            double[] getBitRates() {
                return BPPPB_15_1;
            }

            @Override
            int getQualityLayers() {
                return BPPPB_15_1.length;
            }
            
            @Override
            Compression getCompression() {
                return Compression.LOSSY;
            }
        };

        
        abstract double [] getBitRates();
        
        abstract int getQualityLayers();
        
        abstract Compression getCompression();
    }

    /**
     * Constants
     */
    public static class Consts {

        /**
         * Private constructor... Consts can't be instantiated
         */
        private Consts() {
            
        }
        
        public final static String NONE = "N";
        
        public final static String ZERO = "0";
        
        public final static String ONE = "1";
        
        public final static String EIGHT = "8";
        
        public final static String DEFAULT_SECURITY_CLASSIFICATION_SYSTEM = "US";

        public final static String DEFAULT_SECURITY_CLASSIFICATION = "U";

        public final static int DEFAULT_ENCRYPTED = 0;
        
        public final static String DEFAULT_FILE_HEADER = "NITF";

        public final static String DEFAULT_FILE_VERSION = "02.10";

        public final static String DEFAULT_SYSTEM_TYPE = "BF01";
        
        public final static String DEFAULT_PVTYPE = "INT";
        
        public final static String DEFAULT_PJUST = "R";
        
        public final static String DEFAULT_IMODE = "B";
        
        public final static String COMPRESSION_JP2 = "C8";
        
        public final static String COMPRESSION_NONE = "NC";
        
        public final static String COMPRESSION_V039 = "V039";
        
        public final static String COMPRESSION_L005 = "L005";
        
    }

    private static final Logger LOGGER = Logger.getLogger("nitf.imageio.NITFUtilities");

    /** is NITF native lib available on this machine?. */
    private static boolean available;

    private static boolean init = false;

    /**
     * Returns <code>true</code> if the NITF native library has been loaded. <code>false</code>
     * otherwise.
     * 
     * @return <code>true</code> only if the NITF native library has been loaded.
     */
    public static boolean isNITFAvailable() {
        loadNITF();
        return available;
    }

    /**
     * Forces loading of NITF libs.
     */
    public static void loadNITF() {
        if (init == false) {
            synchronized (LOGGER) {
                if (init) {
                    return;
                }
                try {
                    System.loadLibrary("nitf.jni-c");
                    available = true;
                } catch (UnsatisfiedLinkError e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        StringBuilder sb = new StringBuilder(
                                "Failed to load the NITF native libs. This is not a problem unless you need to use the NITF plugins: they won't be enabled.")
                                .append(e.toString());
                        LOGGER.warning(sb.toString());
                    }
                    available = false;
                } finally {
                    init = true;
                }
            }
        }
    }

    /**
     * Setup a proper set of JP2K writing parameters depending on the type of requested compression.
     * 
     * @param kakaduWriter a writer instance to get default write parameters.
     * @param compression the type of {@link WriteCompression} desired
     * @param isMulti whether the image to be compressed is multi band or pancromatic.
     * @return a {@link JP2KKakaduImageWriteParam} instance with proper writing parameters.
     */
    public static JP2KKakaduImageWriteParam getCompressionParam(
            final JP2KKakaduImageWriter kakaduWriter, 
            final WriteCompression compression,
            final boolean isMulti) {
        final JP2KKakaduImageWriteParam param = (JP2KKakaduImageWriteParam) kakaduWriter
                .getDefaultWriteParam();
        param.setsProfile(1);
        if (compression.getCompression() == Compression.NUMERICALLY_LOSSLESS) {
//            if (!isMulti) {
//                param.setqGuard(2);
//            }
        } 
        
        param.setQualityLayers(compression.getQualityLayers());
        param.setQualityLayersBitRates(compression.getBitRates());
        param.setCompression(compression.getCompression());
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCLevels(5);
        if (compression.toString().startsWith("EPJE")) {
            param.setcOrder(ProgressionOrder.RLCP);
            // param.setOrgT_parts("R");
        } else {
            param.setcOrder(ProgressionOrder.LRCP);
            // param.setOrgT_parts("L");
        }

        param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        param.setTiling(DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT, 0, 0);

        param.setOrgGen_plt(true);
        param.setOrgGen_tlm(1);
        param.setWriteCodeStreamOnly(true);
        return param;
    }

    /**
     * Set a field in the specified TRE 
     * @param tre the TRE to be set
     * @param fieldName the name of the field to be set 
     * @param fieldValue the value to be set for that field
     * @param doValidation {@code true} to check if the provided value is compliant 
     *          with the field properties (length and type).
     * @throws NITFException
     */
    public static void setTREField(
            final TRE tre, 
            final String fieldName, 
            final String fieldValue,
            final boolean doValidation) throws NITFException {
        final Field field = tre.getField(fieldName);
        if (field == null) {
            throw new IllegalArgumentException("The specified field " + fieldName
                    + " doesn't exist in the specified TRE " + tre.getTag());
        }
        if (doValidation) {
            validateField(fieldName, field, fieldValue);
        }
        field.setData(fieldValue);
    }
    
    /**
     * Set a field in the specified TRE 
     * @param tre the TRE to be set
     * @param fieldName the name of the field to be set 
     * @param fieldValue the value to be set for that field
     * @throws NITFException
     * 
     * @see {@link #setTREField(TRE, String, String, boolean)}
     */
    public static void setTREFieldDirect(
            final TRE tre, 
            final String fieldName, 
            final String fieldValue) throws NITFException {
        tre.setField(fieldName, fieldValue);
    }

    /**
     * Perform field validation. Validation is made by checking the field value is respecting the field type
     * and its length doesn't exceed the field length. Specifying a value having length less than the the field 
     * length is allowed, the NITRO encoder will do the proper padding.  
     * 
     * @param fieldName the name of the field to be set. This parameter is only used for logging/exceptions purposes.
     * @param field the field to be set.
     * @param fieldValue the value to be set for that field.
     */
    private static void validateField(final String fieldName, final Field field, final String fieldValue) {
        if (fieldValue == null || fieldValue.length() == 0) {
            throw new IllegalArgumentException("The value specified for field " + fieldName + " is " + (fieldValue == null ? "null" : "empty"));
        }
        validateField(fieldName, field, fieldValue.getBytes());
    }
    
    /**
     * Perform field validation. Validation is made by checking the field value is respecting the field type
     * and its length doesn't exceed the field length. Specifying a value having length less than the the field 
     * length is allowed, the NITRO encoder will do the proper padding.  
     * 
     * @param fieldName the name of the field to be set. This parameter is only used for logging/exceptions purposes.
     * @param field the field to be set.
     * @param fieldValue the value to be set for that field.
     */
    private static void validateField(final String fieldName, final Field field, final byte[] fieldValue) {
        if (fieldValue == null || fieldValue.length == 0) {
            throw new IllegalArgumentException("The value specified for field " + fieldName + " is " + (fieldValue == null ? "null" : "empty"));
        }
        final long length = field.getLength();
        final int valueLength = fieldValue.length;
        if (valueLength > length) {
            throw new IllegalArgumentException("The specified field " + fieldName + " size is "
                    + length + " whilst the specified value is " + valueLength + " bytes length");
        }
        if (!isValid(field, fieldValue)) {
            throw new IllegalArgumentException("The value specified for the field " + fieldName
                    + " doesn't respect the field's type " + field.getType());
        }

    }

    /**
     * Check whether the specified field value is compliant with the field definition in terms of
     * field length and field type. As an instance, setting a field value made of letters for a
     * field having numeric type, will not pass the check which will return false.
     * 
     * @param field the Field to be set
     * @param fieldValue the value to be set.
     * 
     * @return {@code true} in case of successfull validation
     */
    private static boolean isValid(final Field field, final byte[] fieldValue) {
        FieldType type = field.getType();

        // Check for numeric fields
        if (type.equals(FieldType.NITF_BCS_N)) {
            for (byte b : fieldValue) {
                if (!((b <= 0x39 && b >= 0x30) || (b == 0x2B || b == 0x2D || b == 0x2E || b == 0x2F))) {
                    return false;
                }
            }
        }

        // Check for alphanumeric fields
        if (type.equals(FieldType.NITF_BCS_A)) {
            for (byte b : fieldValue) {
                if (!(b <= 0x7E && b >= 0x20)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Set the specified fieldName, represented by the specified Field, with the provided fieldValue
     * Note that the fieldName parameter is only used for logging/exceptions purposes.
     * 
     * @param fieldName the name to be shown in case some exception occurs
     * @param field the field to be set
     * @param fieldValue the value, as a {@code String}, to be assigned to that field
     */
    public static void setField(final String fieldName, final Field field, final String fieldValue) {
        setField(fieldName, field, fieldValue, true);
    }

    /**
     * Set the specified fieldName, represented by the specified Field, with the provided fieldValue
     * Note that the fieldName parameter is only used for logging/exceptions purposes.
     * 
     * @param fieldName the name to be shown in case some exception occurs
     * @param field the field to be set
     * @param fieldValue the value, as a {@code byte[]}, to be assigned to that field
     */
    public static void setField(final String fieldName, final Field field, final byte[] fieldValue) {
        setField(fieldName, field, fieldValue, true);
    }

    /**
     * Set the specified fieldName, represented by the specified Field, with the provided fieldValue
     * Note that the fieldName parameter is only used for logging/exceptions purposes. Validation of
     * the provided value is performed depending on the values of the {@code doValidation} param.
     * 
     * @param fieldName the name to be shown in case some exception occurs
     * @param field the field to be set
     * @param fieldValue the value, as a {@code String}, to be assigned to that field
     * @param doValidation if {@code true} check the fieldValue is compliant with the field
     *        properties
     */
    public static void setField(final String fieldName, final Field field, final String fieldValue,
            final boolean doValidation) {
        if (doValidation) {
            validateField(fieldName, field, fieldValue);
        }
        field.setData(fieldValue);
    }
    
    /**
     * Set the specified fieldName, represented by the specified Field, with the provided fieldValue
     * Note that the fieldName parameter is only used for logging/exceptions purposes. Validation of
     * the provided value is performed depending on the values of the {@code doValidation} param.
     * 
     * @param fieldName the name to be shown in case some exception occurs
     * @param field the field to be set
     * @param fieldValue the value, as a {@code String}, to be assigned to that field
     * @param doValidation if {@code true} check the fieldValue is compliant with the field
     *        properties
     */
    public static void setField(final String fieldName, final Field field, final byte[] fieldValue,
            final boolean doValidation) {
        if (doValidation) {
            validateField(fieldName, field, fieldValue);
        }
        field.setRawData(fieldValue);
    }

}
