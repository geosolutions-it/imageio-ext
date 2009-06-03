package net.sourceforge.jgrib.tables;

import java.util.HashMap;
import java.util.Map;

/**
 * <p><strong>Title:</strong>Grib1TimeRangeIndicator</p>
 *
 * <p><strong>Description:</strong> This class is an helper class that implements
 * <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table5.html"> table 5</a> of grib edition 1.
 * </p>
 *
 * <p>It is worth to point out the following excerpt taken from the above mentioned web page:</p>
 *
 * <p>
 * <strong>NOTES:</strong>
 * <ol>
 * <li> For analysis products, or the first of a series of analysis products, the reference time (octets 13 to 17) indicates the valid time.
 * <li> For forecast products, or the first of a series of forecast products, the reference time indicates the valid time of the analysis upon which the (first) forecast is based.
 * <li> Initialized analysis products are allocated numbers distinct from those allocated to uninitialized analysis products.
 * <li> A value of 10 allows the period of a forecast to be extended over two octets; this accommodates extended range forecasts.
 * <li> Where products or a series of products are averaged or accumulated, the number involved is to be represented in octets 22-23 of Section 1, while any number missing is to be represented in octet 24.
 * <li> Forecasts of the accumulation or difference of some quantity (e.g. quantitative precipitation forecasts, indicated by values of 4 or 5 in octet 21, have a product valid time given by the reference time + P2; the period of accumulation, or difference, can be calculated as P2 - P1.
 * </ol>
 * </p>
 *
 * <p>
 * A few examples may help to clarify the use of Table 5:
 * <ul>
 * <li>For analysis products P1 is zero and the time range indicator is also zero; for initialized products (sometimes called "zero hour forecasts") P1 is zero, but octet 21 is set to 1.
 * <li>For forecasts, typically, P1 contains the number of hours of the forecast (the unit indicator given in octet 18 would be 1) and octet 21 contains a zero.
 * </ul>
 * .......
 *
 * <p>Copyright (c)<A HREF="mailto:simboss_ml@tiscali.it">Simone Giannecchini</a>  <2005</p>
 *
 *
 *
 * @author <A HREF="mailto:simboss_ml@tiscali.it">Simone Giannecchini</a>
 * @version 1.0
 */
public final class Grib1TimeRangeIndicator {
    static private Map<Integer,String> timeRangeIndicator = new HashMap<Integer,String>();
    static private int[][] ranges4Reserved = null;

    /**
     * statically loads all the centers into the hash map
     */
    static {
        Grib1TimeRangeIndicator.timeRangeIndicator.put(0,
            "Forecast product valid for reference time + P1 (P1>0) or Uninitialized analysis product for reference time (P1=0) or Image product for reference time (P1=0)");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(1,
            "Initialized analysis product for reference time (P1=0).");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(2,
            "Product with a valid time ranging between reference time + P1 and reference time + P2");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(3,
            "Average (reference time + P1 to reference time + P2)");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(4,
            "Accumulation (reference time + P1 to reference time + P2) product considered valid at reference time + P2");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(5,
            "Difference (reference time + P2 minus reference time + P1) product considered valid at reference time + P2");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(6,
            "Average (reference time - P1 to reference time - P2)");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(7,
            "Average (reference time - P1 to reference time + P2)");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(10,
            "P1 occupies octets 19 and 20; product valid at reference time + P1");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(51,
            "Climatological Mean Value: multiple year averages of quantities which are themselves means over some period of time (P2) less than a year. The reference time (R) indicates the date and time of the start of a period of time, given by R to R + P2, over which a mean is formed; N indicates the number of such period-means that are averaged together to form the climatological value, assuming that the N period-mean fields are separated by one year. The reference time indicates the start of the N-year climatology. N is given in octets 22-23 of the PDS. If P1 = 0 then the data averaged in the basic interval P2 are assumed to be continuous, i.e., all available data are simply averaged together. If P1 = 1 (the units of time - octet 18, code table 4 - are not relevant here) then the data averaged together in the basic interval P2 are valid only at the time (hour, minute) given in the reference time, for all the days included in the P2 period. The units of P2 are given by the contents of octet 18 and Table 4.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(113,
            "Average of N forecasts (or initialized analyses); each product has forecast period of P1 (P1=0 for initialized analyses); products have reference times at intervals of P2, beginning at the given reference time.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(114,
            "Accumulation of N forecasts (or initialized analyses); each product has forecast period of P1 (P1=0 for initialized analyses); products have reference times at intervals of P2, beginning at the given reference time.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(115,
            "Average of N forecasts, all with the same reference time; the first has a forecast period of P1, the remaining forecasts follow at intervals of P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(116,
            "Accumulation of N forecasts, all with the same reference time; the first has a forecast period of P1, the remaining follow at intervals of P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(117,
            "Average of N forecasts, the first has a period of P1, the subsequent ones have forecast periods reduced from the previous one by an interval of P2; the reference time for the first is given in octets 13-17, the subsequent ones have reference times increased from the previous one by an interval of P2. Thus all the forecasts have the same valid time, given by the initial reference time + P1.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(118,
            "Temporal variance, or covariance, of N initialized analyses; each product has forecast period P1=0; products have reference times at intervals of P2, beginning at the given reference time.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(119,
            "Standard deviation of N forecasts, all with the same reference time with respect to time average of forecasts; the first forecast has a forecast period of P1, the remaining forecasts follow at intervals of P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(123,
            "Average of N uninitialized analyses, starting at the reference time, at intervals of P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(124,
            "Accumulation of N uninitialized analyses, starting at the reference time, at intervals of P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(125,
            "Standard deviation of N forecasts, all with the same reference time with respect to time average of the time tendency of forecasts; the first forecast has a forecast period of P1, the remaining forecasts follow at intervals of P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(128,
            "Average of daily forecast accumulations. P1 = start of accumulation period. P2 = end of accumulation period. Reference time is the start time of the first forecast, other forecasts are at 1 day intervals. Number in Ave = number of days in average.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(129,
            "Average of successive forecast accumulations. P1 = start of accumulation period. P2 = end of accumulation period. Reference time is the start time of the first forecast, other forecasts at (P2 - P1) intervals. Number in Ave = number of forecasts used");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(130,
            "Average of daily forecast averages. P1 = start of averaging period. P2 = end of averaging period. Reference time is the start time of the first forecast, other forecasts are at 1 day intervals. Number in Ave = number of days in average");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(131,
            "Average of successive forecast averages. P1 = start of averaging period. P2 = end of averaging period. Reference time is the start time of the first forecast, other forecasts at (P2 - P1) intervals. Number in Ave = number of forecasts used");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(132,
            "Climatological Average of N analyses, each a year apart, starting from initial time R and for the period from R+P1 to R+P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(133,
            "Climatological Average of N forecasts, each a year apart, starting from initial time R and for the period from R+P1 to R+P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(134,
            "Climatological Root Mean Square difference between N forecasts and their verifying analyses, each a year apart, starting with initial time R and for the period from R+P1 to R+P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(135,
            "Climatological Standard Deviation of N forecasts from the mean of the same N forecasts, for forecasts one year apart. The first forecast starts wtih initial time R and is for the period from R+P1 to R+P2.");
        Grib1TimeRangeIndicator.timeRangeIndicator.put(136,
            "Climatological Standard Deviation of N analyses from the mean of the same N analyses, for analyses one year apart. The first analyses is valid for  period R+P1 to R+P2.");
        Grib1TimeRangeIndicator.ranges4Reserved = new int[][] {
                { 8, 9 },
                { 11, 50 },
                { 51, 112 },
                { 120, 122 },
                { 126, 127 },
                { 137, 254 }
            };
    }

    /**
       * getOriginatingCenter, looks if there exists an originating center
       * with the uspplied key otherwise it returns null.
       *
       * @param key int
       */
    public static String TimeRangeIndicator(int key) {
        if (checkReserved(key)) {
            return "reserved";
        }

        if (Grib1TimeRangeIndicator.timeRangeIndicator.containsKey(key)) {
            return (String) Grib1TimeRangeIndicator.timeRangeIndicator.get(key);
        }

        return null;
    }

    /**
     * checkReserved
     *
     * @param key int
     * @return boolean
     */
    private static boolean checkReserved(int key) {
        for (int i = 0; i < Grib1TimeRangeIndicator.ranges4Reserved.length;i++) {
            if ((key >= Grib1TimeRangeIndicator.ranges4Reserved[i][0])&& (key <= Grib1TimeRangeIndicator.ranges4Reserved[i][1])) {
                return true;
            }
        }

        return false;
    }

    /**
       * getAll, returns a collection made of all the values we have put inside this object.
       */
    public static Map<Integer,String> getAll() {
        Map<Integer,String> clone = new HashMap<Integer, String>(Grib1TimeRangeIndicator.timeRangeIndicator);

        for (int i = 0; i < Grib1TimeRangeIndicator.ranges4Reserved.length;i++) {
            for (int j = Grib1TimeRangeIndicator.ranges4Reserved[i][0];
                j <= Grib1TimeRangeIndicator.ranges4Reserved[i][1]; j++) {
                clone.put(j, "reserved");
            }
        }

        return new HashMap<Integer, String>(Grib1TimeRangeIndicator.timeRangeIndicator);
    }
}
;
