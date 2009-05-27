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
package it.geosolutions.imageio.plugins.grib1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.tables.GribPDSParamTable;
import net.sourceforge.jgrib.tables.GribPDSParameter;

/**
 * Set of utility methods to handle all the issues involved with table versions,
 * parameter numbers, multibands (as an instance, U and V bands of the Wind
 * Velocity).
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
class GRIB1Utilities {

    public final static String DATE_SEPARATOR = "/";

    /**
     * A simple constant to indicate whether a Grib1 parameter is composed of a
     * single band
     */
    public final static int SINGLE_BAND_PARAM = -1;

    /**
     * Static array containing {@link GRIB1MultiBandsParam}s needed to
     * initialize mapping.
     */
    private static GRIB1MultiBandsParam PARAMSG[];

    private static GRIB1MultiBandsParam PARAMS2[];

    private static GRIB1MultiBandsParam PARAMS128[];

    private static GRIB1MultiBandsParam PARAMS129[];

    private static GRIB1MultiBandsParam PARAMS130[];

    // private static GRIB1MultiBandsParam PARAMS140[];

    /**
     * Utility class needed to represent GRIB1 parameters which are composed by
     * several bands.<BR>
     * As an instance, Wind Velocity is composed of U-band and W-band. Heat Net
     * Flux is composed of Latent and Sensible bands.
     * 
     * @author Daniele Romagnoli, GeoSolutions
     */
    private static class GRIB1MultiBandsParam {

        /** The shortName for this parameter */
        private String shortName;

        /** The parameter Number of the first band of this parameter */
        private int firstBandParamNum;

        /** The description of this parameter */
        private String description;

        public GRIB1MultiBandsParam(int firstBandParamNum, String description,
                String name) {
            this.firstBandParamNum = firstBandParamNum;
            this.description = description;
            this.shortName = name;
        }

        public String getShortName() {
            return shortName;
        }

        public String getDescription() {
            return description;
        }

        public int getFirstBandParamNum() {
            return firstBandParamNum;
        }
    }

    /** Map containing couples <paramNumber, GRIB1MultiBandsParam> */
    private final static Map<Integer, Map<Integer, GRIB1MultiBandsParam>> multiBandsParamMap = new TreeMap<Integer, Map<Integer, GRIB1MultiBandsParam>>();

    /**
     * Parallel Map containing couples <paramShortName, GRIB1MultiBandsParam>
     */
    private final static Map<Integer, Map<String, GRIB1MultiBandsParam>> multiBandsParamByName = new HashMap<Integer, Map<String, GRIB1MultiBandsParam>>();

    public static final String VALUES_SEPARATOR = " ";

    /**
     * Initializing multiband parameters<BR>
     * TODO: Add additional settings
     */
    static {
        // ////////////////////////////////////////////////////////////////////
        //
        // GENERAL Parameters
        //
        // ////////////////////////////////////////////////////////////////////
        GRIB1Utilities.PARAMSG = new GRIB1Utilities.GRIB1MultiBandsParam[] {
                new GRIB1MultiBandsParam(21, "Radar Spectra", "RDSP"),
                new GRIB1MultiBandsParam(28, "Wave Spectra", "WVSP"),
                new GRIB1MultiBandsParam(31, "Wind", "W"),
                new GRIB1MultiBandsParam(33, "Wind Velocity Components", "WGRD"),
                new GRIB1MultiBandsParam(45, "Vertical shear Components", "VSH"),
                new GRIB1MultiBandsParam(47, "Current", "C"),
                new GRIB1MultiBandsParam(49, "Current Components", "OGRD"),
                new GRIB1MultiBandsParam(73, "Cloud Cover", "CDC"),
                new GRIB1MultiBandsParam(93, "Ice Drift", "ICED"),
                new GRIB1MultiBandsParam(95, "Ice Drift Components", "ICE"),
                new GRIB1MultiBandsParam(111,
                        "Net wave radiation flux (surface)", "NWRS"),
                new GRIB1MultiBandsParam(113,
                        "Net wave radiation flux (top of atmosphere)", "NWRT"),
                new GRIB1MultiBandsParam(115, "Wave radiation flux", "WAVR"),
                new GRIB1MultiBandsParam(121, "Heat Net Flux", "HTFL"),
                new GRIB1MultiBandsParam(124, "Momentum flux Components", "FLX") };
        final int nParamsG = PARAMSG.length;

        final Map<Integer, GRIB1MultiBandsParam> paramGmap = new TreeMap<Integer, GRIB1MultiBandsParam>();
        final Map<String, GRIB1MultiBandsParam> paramGnamesMap = new HashMap<String, GRIB1MultiBandsParam>();
        for (int i = 0; i < nParamsG; i++) {
            paramGmap.put(PARAMSG[i].getFirstBandParamNum(), PARAMSG[i]);
            paramGnamesMap.put(PARAMSG[i].getShortName(), PARAMSG[i]);
        }
        GRIB1Utilities.multiBandsParamMap.put(0, paramGmap);
        GRIB1Utilities.multiBandsParamByName.put(0, paramGnamesMap);

        // ////////////////////////////////////////////////////////////////////
        //
        // TABLE Version 2
        //
        // ////////////////////////////////////////////////////////////////////
        GRIB1Utilities.PARAMS2 = new GRIB1Utilities.GRIB1MultiBandsParam[] {
                new GRIB1MultiBandsParam(160, "Clear Sky Solar Flux", "CSSF"),
                new GRIB1MultiBandsParam(162, "Clear Sky Long Wave Flux",
                        "CSLF"),
                new GRIB1MultiBandsParam(181, "Gradient of Log Pressure", "LPS"),
                new GRIB1MultiBandsParam(183, "Gradient of Height", "HGT"),
                new GRIB1MultiBandsParam(196, "Storm Motion", "STM"),
                new GRIB1MultiBandsParam(204, "Downward Wave Radiation Flux",
                        "DWRF"),
                new GRIB1MultiBandsParam(211, "Upward Wave Radiation Flux",
                        "UWRF"),
                new GRIB1MultiBandsParam(232, "Total Radiation Flux", "TRF") };
        final int nParams2 = PARAMS2.length;

        final Map<Integer, GRIB1MultiBandsParam> param2map = new TreeMap<Integer, GRIB1MultiBandsParam>();
        final Map<String, GRIB1MultiBandsParam> param2namesMap = new HashMap<String, GRIB1MultiBandsParam>();
        for (int i = 0; i < nParams2; i++) {
            param2map.put(PARAMS2[i].getFirstBandParamNum(), PARAMS2[i]);
            param2namesMap.put(PARAMS2[i].getShortName(), PARAMS2[i]);
        }
        GRIB1Utilities.multiBandsParamMap.put(2, param2map);
        GRIB1Utilities.multiBandsParamByName.put(2, param2namesMap);

        // //////////////////////////////////////////////////////////////////
        //
        // TABLE Version 128
        //
        // ////////////////////////////////////////////////////////////////////
        GRIB1Utilities.PARAMS128 = new GRIB1Utilities.GRIB1MultiBandsParam[] {
                new GRIB1MultiBandsParam(146, "Components of Ice Stress", "SXX"),
                new GRIB1MultiBandsParam(183, "Barotropic Velocity", "BARO") };
        final int nParams128 = PARAMS128.length;

        final Map<Integer, GRIB1MultiBandsParam> param128map = new TreeMap<Integer, GRIB1MultiBandsParam>();
        final Map<String, GRIB1MultiBandsParam> param128namesMap = new HashMap<String, GRIB1MultiBandsParam>();
        for (int i = 0; i < nParams128; i++) {
            param128map.put(PARAMS128[i].getFirstBandParamNum(), PARAMS128[i]);
            param128namesMap.put(PARAMS128[i].getShortName(), PARAMS128[i]);
        }
        GRIB1Utilities.multiBandsParamMap.put(128, param128map);
        GRIB1Utilities.multiBandsParamByName.put(128, param128namesMap);

        // //////////////////////////////////////////////////////////////////
        //
        // TABLE Version 129
        //
        // ////////////////////////////////////////////////////////////////////
        GRIB1Utilities.PARAMS129 = new GRIB1Utilities.GRIB1MultiBandsParam[] {
                new GRIB1MultiBandsParam(190,
                        "Scatterometer Estimated Wind Components", "SCT"),
                new GRIB1MultiBandsParam(203, "Velocity Variance", "VAR") };
        final int nParams129 = PARAMS129.length;

        final Map<Integer, GRIB1MultiBandsParam> param129map = new TreeMap<Integer, GRIB1MultiBandsParam>();
        final Map<String, GRIB1MultiBandsParam> param129namesMap = new HashMap<String, GRIB1MultiBandsParam>();
        for (int i = 0; i < nParams129; i++) {
            param129map.put(PARAMS129[i].getFirstBandParamNum(), PARAMS129[i]);
            param129namesMap.put(PARAMS129[i].getShortName(), PARAMS129[i]);
        }
        GRIB1Utilities.multiBandsParamMap.put(129, param129map);
        GRIB1Utilities.multiBandsParamByName.put(129, param129namesMap);

        // //////////////////////////////////////////////////////////////////
        //
        // TABLE Version 130
        //
        // ////////////////////////////////////////////////////////////////////
        GRIB1Utilities.PARAMS130 = new GRIB1Utilities.GRIB1MultiBandsParam[] {
                new GRIB1MultiBandsParam(166, "Visible Downward Solar Flux",
                        "VDSF"),
                new GRIB1MultiBandsParam(168, "Near IR Downward Solar Flux",
                        "NDSF"),
                new GRIB1MultiBandsParam(204, "Downward Wave Radiation Flux",
                        "DWRF"),
                new GRIB1MultiBandsParam(211, "Upward Wave Radiation Flux",
                        "UWRF") };
        final int nParams130 = PARAMS130.length;

        final Map<Integer, GRIB1MultiBandsParam> param130map = new TreeMap<Integer, GRIB1MultiBandsParam>();
        final Map<String, GRIB1MultiBandsParam> param130namesMap = new HashMap<String, GRIB1MultiBandsParam>();
        for (int i = 0; i < nParams130; i++) {
            param130map.put(PARAMS130[i].getFirstBandParamNum(), PARAMS130[i]);
            param130namesMap.put(PARAMS130[i].getShortName(), PARAMS130[i]);
        }
        GRIB1Utilities.multiBandsParamMap.put(130, param130map);
        GRIB1Utilities.multiBandsParamByName.put(130, param130namesMap);

        // TODO: Add more table versions.
    }

    /**
     * Simple class used to specify whether the Level of a Grib1 record provides
     * a numeric value or it represent a special level (like, as an instance,
     * Ground Surface)
     */
    public static class ZLEVELS {
        public final static String NUMERIC = "Numeric_";

        public final static String SPECIAL = "Special_";

        private ZLEVELS() {

        }
    }
    
    /**
     * Return the abbreviation for the specified level numeric Identifier.
     * @param levelID the numeric identifier of the requested level.
     * @return the proper abbreviation.
     */
    public static String getLevelAbbreviation(final int levelID){
        switch (levelID){
        case 1:
            return "SFC";
        case 2: 
            return "CBL";
        case 3: 
            return "CTL";
        case 4: 
            return "0DEG";
        case 5: 
            return "ACDL";
        case 6: 
            return "MWSL";
        case 7: 
            return "TRO";
        case 8: 
            return "NTAT";
        case 9: 
            return "SEAB";
        case 20: 
            return "TMPL";
        case 204: 
            return "HTFL";
        case 206: 
            return "GCBL";
        case 207: 
            return "GCTL";
        case 209: 
            return "BCBL";
        case 210: 
            return "BCTL";
        case 211: 
            return "BCY";
        case 212: 
            return "LCBL";
        case 213: 
            return "LCTL";
        case 214: 
            return "LCY";
        case 215: 
            return "CEIL";
        case 220: 
            return "PBLRI";
        case 222: 
            return "MCBL";
        case 223: 
            return "MCTL";
        case 224: 
            return "MCY";
        case 232: 
            return "HCBL";
        case 233: 
            return "HCTL";
        case 234: 
            return "HCY";
        case 235: 
            return "OITL";
        case 236: 
            return "OLYR";
        case 237: 
            return "OBML";
        case 238: 
            return "OBIL";
        case 239: 
            return "S26CY";
        case 240: 
            return "OMXL";
        case 241: 
            return "OSEQD";
        case 242: 
            return "CCBL";
        case 243: 
            return "CCTL";
        case 244: 
            return "CCY";
        case 245: 
            return "LLTW";
        case 246: 
            return "MTHE";
        case 247: 
            return "EHLT";
        case 248: 
            return "SCBL";
        case 249: 
            return "SCTL";
        case 251: 
            return "DCBL";
        case 252: 
            return "DCTL";
        case 253: 
            return "LBLSW";
        case 254: 
            return "HTLSW";
        default:
            return "UNDEFINED";
        }
    }
    
    /**
     * An auxiliary method which attempts to understand if the requested zeta
     * levels are numeric or symbolic ones and provides to return properly
     * formatted zeta Strings in the form {@link GRIB1SliceDescriptor}'s
     * related classes suppose to work with. Zeta levels strings referring to a
     * numeric level start with {@link ZLEVELS#NUMERIC} while zeta levels
     * strings referring to a special level start with {@link ZLEVELS#SPECIAL}
     * 
     * @param zeta
     *                zeta levels String which need to be converted
     * @return properly formatted Strings representing GRIB1 Zeta levels
     */
    public static String[] convertZetaLevelsStrings(final String[] zeta) {
        final int zetaNum = zeta.length;
        String[] newZeta = new String[zetaNum];
        // TODO: improve this method with a kind of "isNumeric�
        for (int i = 0; i < zetaNum; i++) {
            try {
                Double.parseDouble(zeta[i]);
                newZeta[i] = ZLEVELS.NUMERIC + zeta[i];
            } catch (NumberFormatException nfe) {
                newZeta[i] = ZLEVELS.SPECIAL + zeta[i];
            }
        }
        return newZeta;
    }

    /**
     * Method returning the bands number for the global parameter having
     * {@code paramNum} as its first band parameter. As an instance, the WIND
     * parameter has 2 bands, U-band with paramNum = 33 and V-band with paramNum =
     * 34. To know the number of bands of the WIND parameter, I need to query
     * this method by specifying 33 as firstBandParamNum.
     * 
     * @param tableVersion
     *                the version of the table
     * @param firstBandParamNum
     *                the parameter number of the first band of the global
     *                parameter
     * @return the number of bands for the parameter having
     *         {@code firstBandParamNum} as paramNum of its first band.
     */
    public static int getBandsNumberFromFirstParamNum(final int tableVersion,
            final int firstBandParamNum) {

        // TODO: leverage on any table defined (files *.tab)

        if (firstBandParamNum >= 0 && firstBandParamNum < 128) {
            // NOTE: paramNum belonging [0,127] always refer to the same
            // parameter, whatever tableVersion firstBandParamNum is
            // set.

            switch (firstBandParamNum) {
            case 21:
            case 31:
            case 33:
            case 45:
            case 47:
            case 49:
            case 93:
            case 95:
            case 111:
            case 113:
            case 115:
            case 121:
            case 124:
                return 2;
            case 28:
            case 73:
                return 3;
            default:
                return 1;
            }
        }

        switch (tableVersion) {
        case 2:
            switch (firstBandParamNum) {
            case 160:
            case 162:
            case 181:
            case 183:
            case 196:
            case 204:
            case 211:
            case 232:
                // TODO add more cases
                return 2;
            default:
                return 1;
            }

        case 128:
            switch (firstBandParamNum) {
            // TODO: Add more cases
            case 146:
                return 3;
            case 183:
                return 2;
            default:
                return 1;
            }

        case 129:
            switch (firstBandParamNum) {
            // TODO: Add more cases
            case 190:
            case 203:
                return 2;
            default:
                return 1;
            }

        case 130:
            switch (firstBandParamNum) {
            // TODO: Add more cases
            case 166:
            case 168:
            case 204:
            case 211:
                return 2;
            default:
                return 1;
            }

        default:
            return 1;
        }
    }

    /**
     * Given a tableVersion number and a param number, returns -1 if this couple
     * represents a parameter having a single band; N if it represents the
     * (N-1)TH band of a multibands parameters. <BR>
     * <BR>
     * 
     * @see <a
     *      href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html">ON388 -
     *      TABLE2 - Parameters & Units (PDS Octet 9)</a> for information about
     *      parameter table and table versions.
     * 
     * @param tableVersion
     *                the table version of this parameter.
     * @param paramNum
     *                the paramNum of this parameter.
     * @return -1 if this couple represents a parameter having a single band; N
     *         if it represents the (N-1)TH band of a multibands parameters.
     */
    public static int checkMultiBandsParam(final int tableVersion,
            final int paramNum) {

        if (paramNum >= 0 && paramNum < 128) {
            // NOTE: paramNum belonging [0,127] always refer to the same
            // parameter, whatever tableVersion firstBandParamNum is
            // set.

            switch (paramNum) {
            case 21:
            case 28:
            case 31:
            case 33:
            case 45:
            case 47:
            case 49:
            case 73:
            case 93:
            case 95:
            case 111:
            case 113:
            case 115:
            case 121:
            case 124:
                // TODO add more cases
                return 0;
            case 22:
            case 29:
            case 32:
            case 34:
            case 46:
            case 48:
            case 50:
            case 74:
            case 94:
            case 96:
            case 112:
            case 114:
            case 116:
            case 122:
            case 125:
                return 1;
            case 23:
            case 30:
            case 75:
                return 2;
            default:
                return SINGLE_BAND_PARAM;
            }
        }

        switch (tableVersion) {
        case 2:
            switch (paramNum) {
            case 160:
            case 162:
            case 181:
            case 183:
            case 196:
            case 204:
            case 211:
            case 232:
                // TODO add more cases
                return 0;
            case 161:
            case 163:
            case 182:
            case 184:
            case 197:
            case 205:
            case 212:
            case 233:
                return 1;
            default:
                return SINGLE_BAND_PARAM;
            }

        case 128:
            switch (paramNum) {
            // TODO: Add more cases
            case 146:
            case 183:
                return 0;
            case 147:
            case 184:
                return 1;
            case 148:
                return 2;
            default:
                return SINGLE_BAND_PARAM;
            }

        case 129:
            switch (paramNum) {
            // TODO: Add more cases
            case 190:
            case 203:
                return 0;
            case 191:
            case 204:
                return 1;
            default:
                return SINGLE_BAND_PARAM;
            }

        case 130:
            switch (paramNum) {
            // TODO: Add more cases
            case 166:
            case 168:
            case 204:
            case 211:
                return 0;
            case 167:
            case 169:
            case 205:
            case 212:
                return 1;
            default:
                return SINGLE_BAND_PARAM;
            }

        default:
            return SINGLE_BAND_PARAM;
        }
    }

    /**
     * Return the description of the parameter specified by its number and the
     * belonging table. In case the specified parameter is related to a band of
     * a parameter having several bands, then the global description will be
     * returned.
     * 
     * @param paramNum
     *                the number of the parameter
     * @param table
     *                The parameter Table
     * 
     * @return the description of the parameter specified by its descriptor
     *         elements
     */
    public static String getDescription(final int paramNum,
            GribPDSParamTable table) {
        return getDescription(paramNum, table, false);
    }

    /**
     * Return the name of the parameter specified by its number and the
     * belonging table. In case the specified parameter is related to a band of
     * a parameter having several bands, then the global name will be returned.
     * 
     * @param paramNum
     *                the number of the parameter
     * @param table
     *                The parameter Table
     * 
     * @return the name of the parameter specified by its descriptor elements
     */
    public static String getName(final int paramNum, GribPDSParamTable table) {
        return getName(paramNum, table, false);
    }

    /**
     * Return the name and the description of the parameter specified by its
     * number and the belonging table. In case the specified parameter is
     * related to a band of a parameter having several bands, then the global
     * name will be returned.
     * 
     * @param paramNum
     *                the number of the parameter
     * @param table
     *                The parameter Table
     * 
     * @return the name and the description of the parameter specified by its
     *         descriptor elements
     */
    public static String[] getNameAndDescription(final int paramNum,
            GribPDSParamTable table) {
        return getNameAndDescription(paramNum, table, false);
    }

    /**
     * Return the description of the parameter specified by its number and the
     * belonging table. In case the specified parameter is related to a band of
     * a parameter having several bands, then the global description will be
     * returned.
     * 
     * @param paramNum
     *                the number of the parameter
     * @param table
     *                The parameter Table
     * @param groupMultiBandsParams
     *                specify if we need to return information about the grouped
     *                parameters instead of the single parameter.
     * 
     * @return the description of the parameter specified by its descriptor
     *         elements
     */
    public static String getDescription(final int paramNum,
            GribPDSParamTable table, final boolean groupMultiBandsParams) {
        int versionNumber = table.getVersionNumber();
        if (paramNum >= 0 && paramNum < 128)
            versionNumber = 0;
        final int multiBandsParam = checkMultiBandsParam(versionNumber,
                paramNum);
        if (groupMultiBandsParams && multiBandsParam != SINGLE_BAND_PARAM) {
            Map<Integer, GRIB1MultiBandsParam> tableMap = multiBandsParamMap
                    .get(versionNumber);
            if (tableMap != null
                    && tableMap.containsKey(paramNum - multiBandsParam)) {
                GRIB1MultiBandsParam gribMultiBandsParam = tableMap
                        .get(paramNum - multiBandsParam);
                return gribMultiBandsParam.getDescription();
            } else
                return "";
        } else
            return table.getParameter(paramNum).getDescription();
    }

    /**
     * Return the name of the parameter specified by its number and the
     * belonging table. In case the specified parameter is related to a band of
     * a parameter having several bands, then the global name will be returned.
     * 
     * @param paramNum
     *                the number of the parameter
     * @param table
     *                The parameter Table
     * @param groupMultiBandsParams
     *                specify if we need to return information about the grouped
     *                parameters instead of the single parameter.
     * 
     * @return the name of the parameter specified by its descriptor elements
     */
    public static String getName(final int paramNum, GribPDSParamTable table,
            final boolean groupMultiBandsParams) {
        int versionNumber = table.getVersionNumber();
        if (paramNum >= 0 && paramNum < 128)
            versionNumber = 0;
        final int multiBandsParam = checkMultiBandsParam(versionNumber,
                paramNum);
        if (groupMultiBandsParams && multiBandsParam != SINGLE_BAND_PARAM) {
            Map<Integer, GRIB1MultiBandsParam> tableMap = multiBandsParamMap
                    .get(versionNumber);
            if (tableMap != null
                    && tableMap.containsKey(paramNum - multiBandsParam)) {
                GRIB1MultiBandsParam gribMultiBandsParam = tableMap
                        .get(paramNum - multiBandsParam);
                return gribMultiBandsParam.getShortName();
            } else
                return "";
        } else
            return table.getParameter(paramNum).getName();
    }

    /**
     * Return the name and the description of the parameter specified by its
     * number and the belonging table. In case the specified parameter is
     * related to a band of a parameter having several bands, then the global
     * name will be returned.
     * 
     * @param paramNum
     *                the number of the parameter
     * @param table
     *                The parameter Table
     * @param groupMultiBandsParams
     *                specify if we need to return information about the grouped
     *                parameters instead of the single parameter.
     * 
     * @return the name and the description of the parameter specified by its
     *         descriptor elements
     */
    public static String[] getNameAndDescription(final int paramNum,
            GribPDSParamTable table, final boolean groupMultiBandsParams) {
        String[] nameAndDescription = null;
        int versionNumber = table.getVersionNumber();
        if (paramNum >= 0 && paramNum < 128)
            versionNumber = 0;
        final int multiBandsParam = checkMultiBandsParam(versionNumber, paramNum); 
        if (groupMultiBandsParams && multiBandsParam != SINGLE_BAND_PARAM) {
            Map<Integer, GRIB1MultiBandsParam> tableMap = multiBandsParamMap
                    .get(versionNumber);
            if (tableMap != null && tableMap.containsKey(paramNum-multiBandsParam)) {
                GRIB1MultiBandsParam gribMultiBandsParam = tableMap
                        .get(paramNum-multiBandsParam);
                nameAndDescription = new String[2];
                nameAndDescription[0] = gribMultiBandsParam.getShortName();
                nameAndDescription[1] = gribMultiBandsParam.getDescription();
            }
        } else {
            nameAndDescription = new String[2];
            nameAndDescription[0] = table.getParameter(paramNum).getName();
            nameAndDescription[1] = table.getParameter(paramNum)
                    .getDescription();
        }
        return nameAndDescription;
    }

    /**
     * Return a {@code String} representing Time for the provided GribRecordPDS.
     * It may represent both an Instant or an Interval.
     * 
     * @param pds
     *                A {@code GribRecordPDS}
     * @return A {@code String} representing Time for the provided GribRecordPDS
     */
    public static String getTime(GribRecordPDS pds) {
        if (pds == null)
            throw new IllegalArgumentException("Provided GribRecordPDS is null");
        final int p2 = pds.getP2();
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final StringBuffer sb = new StringBuffer();
        final Calendar forecastTime1 = pds.getGMTForecastTime();
        final int timeRangeIndicator = pds.getTimeRangeIndicator();

        // timeRangeIndicator = 10 -> product valid at reference time + P1
        if (p2 != 0 && timeRangeIndicator != 10) {
            final Calendar forecastTime2 = pds.getGMTForecastTime2();
            if (forecastTime1.compareTo(forecastTime2) == 0) {
                sb.append(sdf.format(forecastTime1.getTime()));
            } else {
                // //
                // 4 = Accumulation,
                // 5 = Difference
                //
                // product considered valid at reference time + P2 ->
                // forecastTime2
                // //
                if (timeRangeIndicator != 4 && timeRangeIndicator != 5)
                    sb.append(sdf.format(forecastTime1.getTime())).append(
                            DATE_SEPARATOR);
                sb.append(sdf.format(forecastTime2.getTime()));
            }
        } else {
            sb.append(sdf.format(forecastTime1.getTime()));
        }
        return sb.toString();
    }

    public static int getTimeRangeIndicator(GribRecordPDS pds) {
        if (pds == null)
            throw new IllegalArgumentException("Provided GribRecordPDS is null");
        return pds.getTimeRangeIndicator();
    }

    /**
     * Given the name of a Coverage, return {@code true} if it is a multi bands
     * coverage.
     * 
     * @param coverageName
     *                the name of the coverage for which the check need to be
     *                performed
     * @return {@code true} in case this coverage is a multibands coverage (A
     *         typical example is a coverage containing WIND velocity).
     *         {@code false} otherwise.
     */
    public static boolean isMultiBands(String coverageName,
            GribPDSParamTable table) {
        int paramNum = getParamNum(coverageName, table);
        if (paramNum != -1) {
            if (checkMultiBandsParam(table.getVersionNumber(), paramNum) != SINGLE_BAND_PARAM)
                return true;
            else
                return false;
        }

        return false;
    }

    /**
     * Returns the parameter number of a specified grib1 parameter name. In case
     * the parameter name is related to a multi bands parameter, then the
     * parameter number of the first band will be returned.
     * 
     * @param paramName
     *                the name of the parameter.
     * @param table
     *                the table
     * @return the parameter number.
     */
    public static int getParamNum(String paramName, GribPDSParamTable table) {

        int versionNumber = table.getVersionNumber();
        // //
        //
        // Firstly, check in the General Map
        //
        // //
        final Map<String, GRIB1MultiBandsParam> generalMap = multiBandsParamByName
                .get(0);
        if (generalMap != null && generalMap.containsKey(paramName)) {
            return ((GRIB1MultiBandsParam) generalMap.get(paramName))
                    .getFirstBandParamNum();
        } else {

            // //
            //
            // As a second attempt, check in the Version Specific Map
            //
            // //
            final Map<String, GRIB1MultiBandsParam> tableMap = multiBandsParamByName
                    .get(versionNumber);
            if (tableMap != null && tableMap.containsKey(paramName)) {
                return ((GRIB1MultiBandsParam) tableMap.get(paramName))
                        .getFirstBandParamNum();
            }
        }

        // //
        //
        // As a last attempt, manually scan the table
        //
        // //
        for (int i = 0; i < 256; i++)
            if (paramName.equalsIgnoreCase(table.getParameter(i).getName()))
                return i;
        // TODO: Display some warning?
        return -1;
    }

    /**
     * Given a named coverage and the related table, return the real parameter
     * names
     * 
     * @param coverageName
     *                The Coverage Name
     * @param table
     *                The parameter Table
     * @return
     */
    public static String[] getParameterBandsNames(String coverageName,
            GribPDSParamTable table) {

        // TODO: Provide a full set cases
        String[] paramNames = null;
        final int tableVersion = table.getVersionNumber();

        int paramNum = getParamNum(coverageName, table);
        final int multiBandsParam = checkMultiBandsParam(tableVersion, paramNum);
        if (multiBandsParam == SINGLE_BAND_PARAM) {
            paramNames = new String[] { table.getParameter(paramNum).getName() };

        } else {
            final int firstParam = paramNum - multiBandsParam;
            final int bandNumbers = getBandsNumberFromFirstParamNum(
                    tableVersion, firstParam);
            paramNames = new String[bandNumbers];
            for (int k = 0; k < bandNumbers; k++) {
                paramNames[k] = table.getParameter(firstParam + k).getName();
            }
        }
        return paramNames;
    }

    /**
     * return a parameterDescriptor {@code String} given the parameterTable and
     * the parameter.
     * 
     * @param parameter
     *                the specified Parameter
     * @param table
     *                the specified {@link GribPDSParamTable}
     * @return a parameterDescriptor {@code String} given the parameterTable and
     *         the parameter.
     */
    public static String buildParameterDescriptor(GribPDSParameter parameter,
            GribPDSParamTable table) {
        return new StringBuffer(Integer.toString(table.getCenter_id())).append(
                ":").append(Integer.toString(table.getSubcenter_id())).append(
                ":").append(Integer.toString(table.getVersionNumber())).append(
                ":").append(Integer.toString(parameter.getNumber())).toString();
    }

    /**
     * Returns a {@link GribPDSParamTable} given a properly formatted
     * {@code String} containing specified Center ID, SubCenter ID and
     * TableVersion
     * 
     * @param parameterDescriptorString
     *                the originating parameter Descriptor containing Center ID,
     *                SubCenter ID and TableVersion of the related table
     * 
     * @return a {@link GribPDSParamTable} given a properly formatted
     *         {@code String} containing specified Center ID, SubCenter ID and
     *         TableVersion
     */
    public static GribPDSParamTable getParameterTable(
            String parameterDescriptorString) {
        String[] paramTable = parameterDescriptorString.split(":");
        return GribPDSParamTable.getParameterTable(Integer
                .parseInt(paramTable[0]), Integer.parseInt(paramTable[1]),
                Integer.parseInt(paramTable[2]));
    }

    /**
     * Given a parameterDescriptor {@code String}, return an array of int
     * parameters. parameterDescriptor has this form:<BR>
     * CenterID:SubcenterID:TableVersion:ParameterNumber
     */
    public static int[] getParamDescriptors(String parameterDescriptor) {
        String paramDesc[] = parameterDescriptor.split(":");
        int[] params = new int[] { Integer.parseInt(paramDesc[0]),
                Integer.parseInt(paramDesc[1]), Integer.parseInt(paramDesc[2]),
                Integer.parseInt(paramDesc[3]) };
        return params;
    }

    /**
     * Given a parameterDescriptor {@code String}, return the description of
     * the related parameter. parameterDescriptor has this form:<BR>
     * CenterID:SubcenterID:TableVersion:ParameterNumber
     */
    public static String getParameterDescription(String parameterDescriptor) {
        final int[] param = getParamDescriptors(parameterDescriptor);
        return getParameterTable(parameterDescriptor).getParameter(param[3])
                .getDescription();
    }

}
