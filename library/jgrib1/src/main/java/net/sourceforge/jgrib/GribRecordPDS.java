/*
 *    Geotools2 - OpenSource mapping toolkit
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
/**
 * GribRecordPDS.java  1.1  01/01/2001 (C) Benjamin Stark Modified by Richard
 * D. Gonzalez - changes: Parameters use external tables, so program does not
 * have to be modified to add support for new tables.
 *
 * @see the GribPDSParameter, GribPDSParamTable, and GribPDSLevel classes.
 *      Added another time field to differentiate between base time and
 *      forecast time. Started handling Subcenters (though not completed yet)
 *      Added code to handle time offsets (thanks Hans)
 */
package net.sourceforge.jgrib;

import it.geosolutions.io.output.MathUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.imageio.stream.ImageInputStream;

import net.sourceforge.jgrib.tables.GribPDSParamTable;
import net.sourceforge.jgrib.tables.GribPDSParameter;


/**
 * A class representing the product definition section (PDS) of a GRIB record.
 *
 * @author Benjamin Stark
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @version 1.0
 */
public final class GribRecordPDS {
    /** Length in bytes of this PDS. */
    private int length;

    /** Exponent of decimal scale. */
    private int decscale;

    /** ID of grid type. */
    private int grid_id; // no pre-definied grids supported yet.

    /** True, if GDS exists. */
    private boolean gds_exists;

    /** True, if BMS exists. */
    private boolean bmsExists;

    /** The parameter as defined in the Parameter Table */
    private GribPDSParameter parameter;

    /**
     * Class containing the information about the level.  This helps to
     * actually use the data, otherwise the string for level will have to be
     * parsed.
     */
    private GribPDSLevel level;

    /** Model Run/Analysis/Reference time. */
    private Calendar baseTime;

    /**
     * Forecast time. Also used as starting time when times represent a period
     */
    private Calendar forecastTime;

    /** Ending time when times represent a period */
    private Calendar forecastTime2;

    /**
     * Strings used in building a string to represent the time(s) for this PDS
     * See the decoder for octet 21 to get an understanding
     */
    private String timeRange = null;
    private String connector = null;

    /**
     * Parameter Table Version number, currently 3 for international exchange.
     */
    private int table_version;

    /** Identification of center e.g. 88 for Oslo */
    private int center_id;

    /** Forecast time unit. */
    private int forecastTimeUnit;

    /** Period of time 1. */
    private int P1;

    /** Period of time 2. */
    private int P2;

    /** Time rabnge indicator. */
    private int timeRangeIndicator;

    // rdg - added the following:

    /** Identification of subcenter */
    private int subcenter_id;

    /** Identification of Generating Process */
    private int process_id;

    /**
     * rdg - moved the Parameter table information and functionality into a
     * class. See GribPDSParamTable class for details.
     */
    private GribPDSParamTable parameter_table;
    
    private double numberIncludedInAverage = 0.0;
    private double numberMissingFromAverage = 0.0;

    // *** constructors *******************************************************

    /**
     * Constructs a <tt>GribRecordPDS</tt> object from a bit input stream.
     *
     * @param in bit input stream with PDS content
     *
     * @throws IOException if stream can not be opened etc.
     */
    public GribRecordPDS(final ImageInputStream in) throws IOException {
        // octet 1-3 (length of section)
        this.length = MathUtils.uint3(in.read(), in.read(), in.read());

        // read rest of section
        final int temp = this.length - 3;
        final int[] data = new int[temp];

        for (int i = 0; i < temp; i++)
            data[i] = in.read(); //same as read unsigned

        // Paramter table octet 4
        this.table_version = data[0];

        // Center  octet 5
        this.center_id = data[1];

        // Center  octet 26 - out of order, but needed now
        this.subcenter_id = data[22];

        // Generating Process - See Table A
        this.process_id = data[2];

        // octet 7 (id of grid type) - not supported yet
        this.grid_id = data[3];

        // octet 8 (flag for presence of GDS and BMS)
        this.gds_exists = (data[4] & 128) == 128;
        this.bmsExists = (data[4] & 64) == 64;

        // octet 9 (parameter and unit)
        // Before getting parameter table values, must get the appropriate table
        // for this center, subcenter (not yet implemented) and parameter table.
        this.parameter_table = GribPDSParamTable.getParameterTable(center_id, subcenter_id, table_version);

        // octet 9 (parameter and unit)
        this.parameter = parameter_table.getParameter(data[5]);

        // octets 10-12 (level)
        this.level = new GribPDSLevel(data[6], data[7], data[8]);

        // octets 13-17 (base time of forecast)
        this.baseTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        this.baseTime.clear();
        this.baseTime.set((data[9] + (100 * (data[21] - 1))), data[10] - 1,data[11], data[12], data[13],0);

        // octet 18 see table 4 for its meaning
        this.forecastTimeUnit = data[14];

        //octets 19 and 20
        this.P1 = data[15];
        this.P2 = data[16];

        // octet 21 (time range indicator) see table 5 for its meaning
        //TODO implementing more row than implemented now.
        this.timeRangeIndicator = data[17];
        computeForecasts(data[9], data[10], data[11], data[12], data[13],data[21]);

        // octets 27-28 (decimal scale factor)
        this.decscale = MathUtils.int2(data[23], data[24]);
    }

    /**
     * GribRecordPDS constructor. This method allows the user to build a
     * GribRecordPDS from scratch. GribFile class is responsible for
     * presenting tbale to the user in oder to make choices about the various
     * ids.
     *
     * @param paramTableVersion DOCUMENT ME!
     * @param centerID DOCUMENT ME!
     * @param generatingProcessID DOCUMENT ME!
     * @param gridID DOCUMENT ME!
     * @param GDS DOCUMENT ME!
     * @param BMS DOCUMENT ME!
     * @param paramID DOCUMENT ME!
     * @param levelID DOCUMENT ME!
     * @param levelValue1 DOCUMENT ME!
     * @param levelValue2 DOCUMENT ME!
     * @param referenceTime DOCUMENT ME!
     * @param forecastTimeUnitID DOCUMENT ME!
     * @param P1 DOCUMENT ME!
     * @param P2 DOCUMENT ME!
     * @param timeRangeID DOCUMENT ME!
     * @param includedInAverage DOCUMENT ME!
     * @param missingFromAverage DOCUMENT ME!
     * @param subCenterID DOCUMENT ME!
     * @param decimalScaleFactor DOCUMENT ME!
     */
    public GribRecordPDS(int paramTableVersion, //currently 3 for internationa exhange (2 is still accepted)
        int centerID, //code table 0
        int generatingProcessID, //allocated by originating center
        int gridID, boolean GDS, boolean BMS, int paramID, //code table 2
        int levelID, double levelValue1, double levelValue2,
        Calendar referenceTime, int forecastTimeUnitID, int P1, int P2,
        int timeRangeID, int includedInAverage, int missingFromAverage,
        int subCenterID, int decimalScaleFactor) {
    	
    	
        this.length = 28; //for the moment is always like this, no extensions!
        this.table_version = paramTableVersion;
        this.center_id = centerID;
        this.numberIncludedInAverage = includedInAverage;
        this.numberMissingFromAverage = missingFromAverage;

        //building a base time from scratch
        this.baseTime = new GregorianCalendar();
        this.baseTime.setTimeZone(TimeZone.getTimeZone("Europe/Greenwich"));
        this.baseTime.set(referenceTime.get(Calendar.YEAR),
            referenceTime.get(Calendar.MONTH),
            referenceTime.get(Calendar.DAY_OF_MONTH),
            referenceTime.get(Calendar.HOUR_OF_DAY),
            referenceTime.get(Calendar.MINUTE));
        this.forecastTimeUnit = forecastTimeUnitID;
        this.P1 = P1;
        this.P2 = P2;
        this.timeRangeIndicator = timeRangeID;

        int century = computeCentury();
        this.computeForecasts(this.computeYearOfCentury(),
            referenceTime.get(Calendar.MONTH) + 1,
            referenceTime.get(Calendar.DAY_OF_MONTH),
            referenceTime.get(Calendar.HOUR_OF_DAY),
            referenceTime.get(Calendar.MINUTE), century);

        this.bmsExists = BMS;
        this.gds_exists = GDS;

        this.grid_id = gridID & 0x000000ff;
        this.process_id = generatingProcessID;
        this.level = new GribPDSLevel(levelID, levelValue1, levelValue2);

        // Before getting parameter table values, must get the appropriate table
        // for this center, subcenter (not yet implemented) and parameter table.
        this.parameter_table = GribPDSParamTable.getParameterTable(center_id,
                subcenter_id, table_version);

        // octet 9 (parameter and unit)
        this.parameter = parameter_table.getParameter(paramID);
        this.decscale = decimalScaleFactor;

        this.subcenter_id = subCenterID;
    }

    final private void computeForecasts(final int year,final  int month,final  int day,final  int hour,
        int minute, int century) {
        int offset = 0;
        int offset2 = 0;

        /**
         * In the following cases we need to multiply the offsets by the value
         * we get here.
         */
        switch (forecastTimeUnit) {
        case 10: //3 hours
            forecastTimeUnit *= 3;
            P2 *= 3;
            forecastTimeUnit = 1;

            break;

        case 11: // 6 hours
            forecastTimeUnit *= 6;
            P2 *= 6;
            forecastTimeUnit = 1;

            break;

        case 12: // 12 hours
            forecastTimeUnit *= 12;
            P2 *= 12;
            forecastTimeUnit = 1;

            break;
        }

        switch (timeRangeIndicator) {
        case 0:

            /**
             * Forecast product valid for reference time + P1 (P1>0) or
             * Uninitialized analysis product for reference time (P1=0). or
             * Image product for reference time (P1=0)
             */
            offset = P1;
            offset2 = 0;

            break;

        case 1:

            /**
             * analysis product - valid at reference time
             */
            offset = 0;
            offset2 = 0;

            break;

        case 2:

            /**
             * Product with a valid time ranging between reference time + P1
             * and reference time + P2
             */
            timeRange = "product valid from ";
            connector = " to ";
            offset = P1;
            offset2 = P2;

            break;

        case 3:

            /**
             * Average (reference time + P1 to reference time + P2)
             */
            timeRange = "product is an average between ";
            connector = " and ";
            offset = P1;
            offset2 = P2;

            break;

        case 4:

            /**
             * Accumulation (reference time + P1 to reference time + P2)
             * product considered valid at reference time + P2
             */
            timeRange = "product is an accumulation from ";
            connector = " to ";
            offset = P1;
            offset2 = P2;

            break;

        case 5:

            /**
             * Difference(reference time + P2 minus reference time + P1)
             * product considered valid at reference time + P2
             */
            timeRange = "product is the difference of ";
            connector = " minus ";
            offset = P2;
            offset2 = forecastTimeUnit;

            break;

        case 6:

            /**
             * Average (reference time - P1 to reference time - P2)
             */
            timeRange = "product is an average from ";
            connector = " to ";
            offset = -P1;
            offset2 = -P2;

            break;

        case 7:

            /**
             * Average(reference time - P1 to reference time + P2)
             */
            timeRange = "product is an average from ";
            connector = " to ";
            offset = -P1;
            offset2 = P2;

            break;

        case 10:

            /**
             * P1 occupies octets 19 and 20; product valid at reference time +
             * P1
             */
            offset = MathUtils.uint2(P1, P2);

            break;

        default:

            // no reason to crash here - just notify that the time is not discernible
            //            throw new NotSupportedException("GribRecordPDS: Time " +
            //                  "Range Indicator " +timeRangeIndicator + " is not yet supported");
            System.err.println("GribRecordPDS: Time Range Indicator "
                + forecastTimeUnit
                + " is not yet supported - continuing, but time "
                + "of data is not valid");
        }

        // prep for adding the offset - get the base values
        int minute1 = minute;
        int minute2 = minute;
        int hour1 = hour;
        int hour2 = hour;
        int day1 = day;
        int day2 = day;
        int month1 = month;
        int month2 = month;
        int year1 = year;
        int year2 = year;

        // octet 18 (again) - this time adding offset to get forecast/valid time
        switch (forecastTimeUnit) {
        case 0:
            minute1 += offset;
            minute2 += offset2;

            break; // minute

        case 1:
            hour1 += offset;
            hour2 += offset2;

            break; // hour

        case 2:
            day1 += offset;
            day2 += offset2;

            break; // day

        case 3:
            month1 += offset;
            month2 += offset2;

            break; // month

        case 4:
            year1 += offset;
            year2 += offset2;

            break; // year

        default:

            // no reason to crash here either - just report and continue
            //            throw new NotSupportedException("GribRecordPDS: Forecast time unit " +
            //                  "> year not supported yet.");
            System.err.println("GribRecordPDS: Forecast time unit, index of "
                + forecastTimeUnit
                + ", is not yet supported - continuing, but time "
                + "of data is not valid");
        }

        // octets 13-17 (time of forecast)
        this.forecastTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        this.forecastTime.clear();
        this.forecastTime.set((year1 + (100 * (century - 1))), month1 - 1,
            day1, hour1, minute1,0);

        this.forecastTime2 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        this.forecastTime2.clear();
        this.forecastTime2.set((year2 + (100 * (century - 1))), month2 - 1,
            day2, hour2, minute2,0);

        // GMT timestamp: zone offset to GMT is 0
        //  this.forecastTime.set(Calendar.ZONE_OFFSET, 0);
        // this.forecastTime2.set(Calendar.ZONE_OFFSET, 0);
        // rdg - adjusted for DST - don't know if this affects everywhere or if
        //       Calendar can figure out where DST is implemented. Find out at end of Oct.
        // GMT timestamp: DST offset to GMT is 0
        this.forecastTime.set(Calendar.DST_OFFSET, 0);
        this.forecastTime2.set(Calendar.DST_OFFSET, 0);
    }

    private int computeCentury() {
        //octet 25 century of initial reference time

        /**
         * from
         * http://www-lehre.inf.uos.de/~fbstark/diplom/docs/data/GRIB/pds.html
         * note 6 To specify year 2000, octet 13 of the section (Year of the
         * century) shall contain a value equal to 100 and octet 25 of the
         * section (Century of reference time data) shall contain a value
         * equal to 20. To specify year 2001, octet 13 of the section shall
         * contain a value equal to 1 and octet 25 of the section shall
         * contain a value equal to 21 (by International Convention, the date
         * of 1 January 2000 is the first day of the hundredth year of the
         * twentieth century and the date of 1 January 2001 is the first day
         * of the first year of the twenty-first century); it is to be noted
         * also that year 2000 is a leap year and the 29 February 2000 exists.
         */
        return ((int) Math.ceil((double) this.baseTime.get(Calendar.YEAR) / 100))& 0x000000ff;
    }

    /**
     * Get the byte length of this section.
     *
     * @return length in bytes of this section
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Check if GDS exists.
     *
     * @return true, if GDS exists
     */
    public boolean gdsExists() {
        return this.gds_exists;
    }

    /**
     * Check if BMS exists.
     *
     * @return true, if BMS exists
     */
    public boolean bmsExists() {
        return this.bmsExists;
    }

    /**
     * Get the exponent of the decimal scale used for all data values.
     *
     * @return exponent of decimal scale
     */
    public int getDecimalScale() {
        return this.decscale;
    }

    /**
     * Get the type of the parameter.
     *
     * @return type of parameter
     */
    public String getType() {
        return this.parameter.getName();
    }

    /**
     * Get a descritpion of the parameter.
     *
     * @return descritpion of parameter
     */
    public String getDescription() {
        return this.parameter.getDescription();
    }

    /**
     * Get the name of the unit of the parameter.
     *
     * @return name of the unit of the parameter
     */
    public String getUnit() {
        return this.parameter.getUnit();
    }

    /**
     * Get the level of the forecast/analysis.
     *
     * @return level (height or pressure)
     */
    public GribPDSLevel getLevel() {
        return this.level;
    }

    // rdg - added the following getters for level information though they are
    //       just convenience methods.  You could do the same by getting the
    //       GribPDSLevel (with getPDSLevel) then calling its methods directly

    /**
     * Get the name for the type of level for this forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public String getLevelName() {
        return this.level.getName();
    }

    /**
     * Get the long description for this level of the forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public String getLevelDesc() {
        return this.level.getDesc();
    }

    /**
     * Get the units for the level of the forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public String getLevelUnits() {
        return this.level.getUnits();
    }

    /**
     * Get the numeric value for this level.
     *
     * @return name of level (height or pressure)
     */
    public double getLevelValue() {
        return this.level.getValue1();
    }

    /**
     * Get value 2 (if it exists) for this level.
     *
     * @return name of level (height or pressure)
     */
    public double getLevelValue2() {
        return this.level.getValue2();
    }

    /**
     * Get the level of the forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public GribPDSLevel getPDSLevel() {
        return this.level;
    }

    /**
     * Get the Parameter Table that defines this parameter.
     *
     * @return GribPDSParamTable containing parameter table that defined this
     *         parameter
     */
    public GribPDSParamTable getParamTable() {
        return this.parameter_table;
    }

    /**
     * Get the base (analysis) time of the forecast in local time zone.
     *
     * @return date and time
     */
    public Calendar getLocalBaseTime() {
        return this.baseTime;
    }

    /**
     * Get the time of the forecast in local time zone.
     *
     * @return date and time
     */
    public Calendar getLocalForecastTime() {
        return this.forecastTime;
    }

    /**
     * Get the parameter for this pds.
     *
     * @return date and time
     */
    public GribPDSParameter getParameter() {
        return this.parameter;
    }

    /**
     * Get the base (analysis) time of the forecast in GMT.
     *
     * @return date and time
     */
    public Calendar getGMTBaseTime() {
        return baseTime; 
    }

    /**
     * Get the time of the forecast.
     *
     * @return date and time
     */
    public Calendar getGMTForecastTime() {
        return forecastTime; 
    }

    /**
     * Get a string representation of this PDS.
     *
     * @return string representation of this PDS
     */
    public String toString() {
        return headerToString() + "        center: " + this.center_id + "\n"
        + "        subcenter: " + this.subcenter_id + "\n"
        + "        process id: " + this.process_id + "\n" +  "        "
        + this.level // now formatted in GribPDSLevel
        + "        "+this.parameter // now formatted in GribPDSLevel
        + "        dec.scale: " + this.decscale
        + (this.gds_exists ? "\n        GDS exists" : "")
        + (this.bmsExists ? "\n        BMS exists" : "");
    }

    /**
     * Get a string representation of this Header information for this PDS.
     *
     * @return string representation of the Header for this PDS
     */
    public String headerToString() {
        String time1 = this.forecastTime.get(Calendar.DAY_OF_MONTH) + "."
            + (this.forecastTime.get(Calendar.MONTH) + 1) + "."
            + this.forecastTime.get(Calendar.YEAR) + "  "
            + this.forecastTime.get(Calendar.HOUR_OF_DAY) + ":"
            + this.forecastTime.get(Calendar.MINUTE);
        String time2 = this.forecastTime2.get(Calendar.DAY_OF_MONTH) + "."
            + (this.forecastTime2.get(Calendar.MONTH) + 1) + "."
            + this.forecastTime2.get(Calendar.YEAR) + "  "
            + this.forecastTime2.get(Calendar.HOUR_OF_DAY) + ":"
            + this.forecastTime2.get(Calendar.MINUTE);
        String timeStr;

        if (timeRange == null) {
            timeStr = "time: " + time1;
        } else {
            timeStr = timeRange + time1 + connector + time2;
        }

        return "    PDS header:" + '\n' + "        center: " + this.center_id
        + "\n" + "        subcenter: " + this.subcenter_id + "\n"
        + "        table: " + this.table_version + "\n" + "        grid_id: "
        + this.grid_id + "\n" + "        " + timeStr + " (dd.mm.yyyy hh:mm) \n";
    }

    /**
     * rdg - added an equals method here
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(final Object obj) {
        if (!(obj instanceof GribRecordPDS)) {
            return false;
        }

        if (this == obj) {
            // Same object
            return true;
        }

        final GribRecordPDS pds = (GribRecordPDS) obj;

        if (grid_id != pds.grid_id) {
            return false;
        }

        if ((baseTime.get(Calendar.YEAR) != pds.baseTime.get(Calendar.YEAR))
                || (baseTime.get(Calendar.MONTH) != pds.baseTime.get(
                    Calendar.MONTH))
                || (baseTime.get(Calendar.DAY_OF_MONTH) != pds.baseTime.get(
                    Calendar.DAY_OF_MONTH))
                || (baseTime.get(Calendar.HOUR_OF_DAY) != pds.baseTime.get(
                    Calendar.HOUR_OF_DAY))
                || (baseTime.get(Calendar.MINUTE) != pds.baseTime.get(
                    Calendar.MINUTE))) {
            return false;
        }

        if ((forecastTime.get(Calendar.YEAR) != pds.forecastTime.get(
                    Calendar.YEAR))
                || (forecastTime.get(Calendar.MONTH) != pds.forecastTime.get(
                    Calendar.MONTH))
                || (forecastTime.get(Calendar.DAY_OF_MONTH) != pds.forecastTime
                .get(Calendar.DAY_OF_MONTH))
                || (forecastTime.get(Calendar.HOUR_OF_DAY) != pds.forecastTime
                .get(Calendar.HOUR_OF_DAY))
                || (forecastTime.get(Calendar.MINUTE) != pds.forecastTime.get(
                    Calendar.MINUTE))) {
            return false;
        }

        if (center_id != pds.center_id) {
            return false;
        }

        if (subcenter_id != pds.subcenter_id) {
            return false;
        }

        if (table_version != pds.table_version) {
            return false;
        }

        if (decscale != pds.decscale) {
            return false;
        }

        if (length != pds.length) {
            return false;
        }

        if (!(parameter.equals(pds.getParameter()))) {
            return false;
        }

        if (!(level.equals(pds.getPDSLevel()))) {
            return false;
        }

        return true;
    }

    /**
     * rdg - added this method to be used in a comparator for sorting while
     * extracting records. Not currently used in the JGrib library, but is
     * used in a library I'm using that uses JGrib. Compares numerous features
     * from the PDS information to sort according to a time, level,
     * level-type, y-axis, x-axis order
     *
     * @param pds - the GribRecordGDS to compare to
     *
     * @return - -1 if pds is "less than" this, 0 if equal, 1 if pds is
     *         "greater than" this.
     */
    public int compare(final GribRecordPDS pds) {
        int check;

        if (this.equals(pds)) {
            return 0;
        }

        // not equal, so either less than or greater than.
        // check if pds is less; if not, then pds is greater
        if (grid_id > pds.grid_id) {
            return -1;
        }

        if (baseTime.getTime().getTime() > pds.baseTime.getTime().getTime()) {
            return -1;
        }

        if (forecastTime.getTime().getTime() > pds.forecastTime.getTime()
                                                                   .getTime()) {
            return -1;
        }

        if (forecastTime2.getTime().getTime() > pds.forecastTime2.getTime()
                                                                     .getTime()) {
            return -1;
        }

        if (center_id > pds.center_id) {
            return -1;
        }

        if (subcenter_id > pds.subcenter_id) {
            return -1;
        }

        if (table_version > pds.table_version) {
            return -1;
        }

        if (decscale > pds.decscale) {
            return -1;
        }

        if (length > pds.length) {
            return -1;
        }

        check = parameter.compare(pds.getParameter());

        if (check < 0) {
            return -1;
        }

        check = level.compare(pds.getPDSLevel());

        if (check < 0) {
            return -1;
        }

        // if here, then something must be greater than something else - doesn't matter what
        return 1;
    }

    /**
     * writeTo Writes the current section to an output stream as requested by
     * the specifications on grib files.
     *
     * @param out OutputStream
     *
     * @throws IOException DOCUMENT ME!
     */
    public void writeTo(final OutputStream out) throws IOException {
        //length (28 decimal in 3 octets)
        out.write(new byte[] { 0 });
        out.write(new byte[] { 0 });
        out.write(new byte[] { 28 });

        //table version
        out.write(new byte[] { (byte) this.table_version });

        //center id
        out.write(new byte[] { (byte) this.center_id });

        //generating process
        out.write(new byte[] { (byte) this.process_id });

        //grid id (octet 7)
        out.write(new byte[] { (byte) this.grid_id });

        //GDS and BMS
        byte val = 0;

        val |= ((this.gds_exists ? 1 : 0) << 7);
        val |= ((this.bmsExists ? 1 : 0) << 6);
        out.write(new byte[] { val });

        //parameter id
        out.write(new byte[] { (byte) this.parameter.getNumber() });

        //level
        out.write(new byte[] { (byte) this.level.getIndex() });

        //level value


        final ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();

        this.level.writeTo(outByteArray);
        out.write(outByteArray.toByteArray(),0,outByteArray.size());

        //reference time
        final int yearOfCentury = computeYearOfCentury();

        out.write(new byte[] {
                (byte) yearOfCentury,
                (byte) (this.baseTime.get(Calendar.MONTH) + 1), //month is zero based
            (byte) this.baseTime.get(Calendar.DAY_OF_MONTH),
                (byte) this.baseTime.get(Calendar.HOUR_OF_DAY),
                (byte) this.baseTime.get(Calendar.MINUTE)
            });

        //forecast time unit
        out.write(new byte[] { (byte) this.forecastTimeUnit });

        //p1
        out.write(new byte[] { (byte) this.P1 });

        //p2
        out.write(new byte[] { (byte) this.P2 });

        //time range indicator
        out.write(new byte[] { (byte) this.timeRangeIndicator });

        //octet2 22 and 23
        out.write(new byte[] { 0, 0 });

        //octet 24
        out.write(new byte[] { (byte) 0 });

        //octet 25 century of initial reference time
        /**
         * from
         * http://www-lehre.inf.uos.de/~fbstark/diplom/docs/data/GRIB/pds.html
         * note 6 To specify year 2000, octet 13 of the section (Year of the
         * century) shall contain a value equal to 100 and octet 25 of the
         * section (Century of reference time data) shall contain a value
         * equal to 20. To specify year 2001, octet 13 of the section shall
         * contain a value equal to 1 and octet 25 of the section shall
         * contain a value equal to 21 (by International Convention, the date
         * of 1 January 2000 is the first day of the hundredth year of the
         * twentieth century and the date of 1 January 2001 is the first day
         * of the first year of the twenty-first century); it is to be noted
         * also that year 2000 is a leap year and the 29 February 2000 exists.
         */
        out.write(new byte[] {
                (byte) (Math.ceil(
                    (double) this.baseTime.get(Calendar.YEAR) / 100))
            });

        //subcenter id
        out.write(new byte[] { (byte) this.subcenter_id });

        //decimal scale value
        short dec = (short) Math.abs(this.decscale);

        if (this.decscale < 0) { //sign bit
            dec |= (1 << 15);
        }

        out.write(new byte[] { (byte) ((dec >>> 8)), (byte) (dec & 255) });
    }

    /**
     * Compute year of century following GriB1 rules.
     *
     * @return int
     */
    private int computeYearOfCentury() {
        /**
         * from
         * http://www-lehre.inf.uos.de/~fbstark/diplom/docs/data/GRIB/pds.html
         * note 6 To specify year 2000, octet 13 of the section (Year of the
         * century) shall contain a value equal to 100 and octet 25 of the
         * section (Century of reference time data) shall contain a value
         * equal to 20. To specify year 2001, octet 13 of the section shall
         * contain a value equal to 1 and octet 25 of the section shall
         * contain a value equal to 21 (by International Convention, the date
         * of 1 January 2000 is the first day of the hundredth year of the
         * twentieth century and the date of 1 January 2001 is the first day
         * of the first year of the twenty-first century); it is to be noted
         * also that year 2000 is a leap year and the 29 February 2000 exists.
         */

        //reference time
        int yearOfCentury = this.baseTime.get(Calendar.YEAR) % 100;
        if (yearOfCentury == 0) {
            yearOfCentury = 100;
        }

        return yearOfCentury;
    }

    /**
     * getOriginatingCenterID
     *
     * @return DOCUMENT ME!
     */
    public int getOriginatingCenterID() {
        return this.center_id;
    }

    /**
     * getSubcenterID
     *
     * @return DOCUMENT ME!
     */
    public int getSubcenterID() {
        return this.subcenter_id;
    }

    /**
     * getGeneratingProcessID
     *
     * @return DOCUMENT ME!
     */
    public int getGeneratingProcessID() {
        return this.process_id;
    }

    /**
     * getP1
     *
     * @return DOCUMENT ME!
     */
    public int getP1() {
        return this.P1;
    }

    /**
     * getP2
     *
     * @return DOCUMENT ME!
     */
    public int getP2() {
        return this.P2;
    }

    /**
     * getTimeRangeIndicator
     *
     * @return DOCUMENT ME!
     */
    public int getTimeRangeIndicator() {
        return this.timeRangeIndicator;
    }

    /**
     * getForecastTimeUnit
     *
     * @return DOCUMENT ME!
     */
    public int getForecastTimeUnit() {
        return this.forecastTimeUnit;
    }

    /**
     * getNumberIncludedInAverage
     *
     * @return DOCUMENT ME!
     */
    public double getNumberIncludedInAverage() {
        return this.numberIncludedInAverage;
    }

    /**
     * getNumberMissingFromAverage
     *
     * @return DOCUMENT ME!
     */
    public double getNumberMissingFromAverage() {
        return this.numberMissingFromAverage;
    }

    /**
     * getGMTForecastTime2
     *
     * @return DOCUMENT ME!
     */
    public Calendar getGMTForecastTime2() {
        return this.forecastTime2;
    }

    /**
     * getGridID
     *
     * @return DOCUMENT ME!
     */
    public int getGridID() {
        return this.grid_id;
    }

    /**
     * getTableVersion() We need this method because the version of the used
     * table may be diferent from the one requested in case we do not have it.
     *
     * @return DOCUMENT ME!
     */
    public int getTableVersion() {
        return this.table_version;
    }
}
