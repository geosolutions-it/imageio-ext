package net.sourceforge.jgrib.tables;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: Grib1ForecastTimeUnit</p>
 *
 * <p>Description: this class provides the user with the table 4 in a
 * useful way</p>
 *
 * <p>Copyright: Copyright (c) 2005 Simone Giannecchini</p>
 *
 *
 * @author Simone Giannecchini 
 * @version 1.0
 */
public final  class Grib1ForecastTimeUnit {
    private static int lowerReserved = 13;
    private static int UpperReserved = 253;

    /**Hash map which will contain forecast time units.
     *
     */
    static private Map<Integer,String> ForecastTimeUnits = new HashMap<Integer,String>();

    /**
     * statically loads all the centers into the hash map
     */
    static {
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(0), "Minute");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(1), "Hour");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(2), "Day");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(3), "Month");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(4), "Year");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(5),
            "Decade(10 years)");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(6),
            "Normal(30 years)");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(7), "Century");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(10), "3 hours");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(11), "6 hours");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(12), "12 hours");
        Grib1ForecastTimeUnit.ForecastTimeUnits.put(Integer.valueOf(254), "seconds");
    }

    /**
     * getOriginatingCenter, looks if there exists an originating center
     * with the uspplied key otherwise it returns null.
     *
     * @param key int
     */
    public static String getForecastTimeUnit(int key) {
        if ((key <= 253) && (key >= 13)) {
            return "reserved";
        }

        if (Grib1ForecastTimeUnit.ForecastTimeUnits.containsKey(
                Integer.valueOf(key))) {
            return Grib1ForecastTimeUnit.ForecastTimeUnits.get(Integer.valueOf(
                    key));
        }

        return null;
    }

    /**
     * getAll, returns a collection made of all the values we have put inside this object.
     * We use a map to
     */
    public static Map<Integer, String> getAll() {
        Map<Integer, String> clone =  new HashMap<Integer, String>(Grib1ForecastTimeUnit.ForecastTimeUnits);

        for (int i = Grib1ForecastTimeUnit.lowerReserved;
            i <= Grib1ForecastTimeUnit.UpperReserved; i++) {
            clone.put(Integer.valueOf(i), "reserved");
        }

        return clone;
    }
}
