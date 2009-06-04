package net.sourceforge.jgrib.tables;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: Grib1OriginatingCenters</p>
 *
 * <p>Description: this class is used to provide a simple value to pick an originating
 * center when crating a grib file edition 1</p>
 *
 * <p>Copyright: Copyright (c) 2005 Simone Giannecchini</p>
 *
 *
 * @author Simone Giannecchini (simboss_ml@tiscali.it)
 * @version 1.0
 */
public final class Grib1OriginatingCenters {
    /**Hash map which will contain originating centers and kes.
     *
     */
    static private Map<Integer,String> originatingCentersmap = new HashMap<Integer,String>();

    /**
     * statically loads all the centers into the hash map
     */
    static {
        Grib1OriginatingCenters.originatingCentersmap.put(1,"Melbourne (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(2,"Melbourne (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(3,"Melbourne (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(4,"Moscow (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(5,"Moscow (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(7,"US National Weather Service - NCEP (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(8,"US National Weather Service - NWSTG (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(9,"US National Weather Service - Other (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(10,"Cairo (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(12,"Dakar (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(14,
            "Nairobi (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(16,
            "Atananarivo (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(18,
            "Tunis-Casablanca (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(20,
            "Las Palmas (RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(21,
            "Algiers (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(22,
            "Lagos (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(26,
            "Khabarovsk (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(28,
            "New Delhi (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(30,
            "Novosibirsk (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(32,
            "Tashkent (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(33,
            "Jeddah (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(34,
            "Japanese Meteorological Agency - Tokyo (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(36,
            "Bankok");
        Grib1OriginatingCenters.originatingCentersmap.put(37,
            "Ulan Bator");
        Grib1OriginatingCenters.originatingCentersmap.put(38,
            "Beijing (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(40,
            "Seoul");
        Grib1OriginatingCenters.originatingCentersmap.put(41,
            "Buenos Aires (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(43,
            "Brasilia (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(45,
            "Santiago");
        Grib1OriginatingCenters.originatingCentersmap.put(46,
            "Brasilian Space Agency - INPE");
        Grib1OriginatingCenters.originatingCentersmap.put(51,
            "Miami (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(52,
            "National Hurricane Center, Miami");
        Grib1OriginatingCenters.originatingCentersmap.put(53,
            "Canadian Meteorological Service - Montreal (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(54,
            "Canadian Meteorological Service - Montreal (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(55,
            "San Francisco");
        Grib1OriginatingCenters.originatingCentersmap.put(57,
            "U.S. Air Force - Global Weather Center");
        Grib1OriginatingCenters.originatingCentersmap.put(58,
            "US Navy - Fleet Numerical Oceanography Center");
        Grib1OriginatingCenters.originatingCentersmap.put(59,
            "NOAA Forecast Systems Lab, Boulder CO");
        Grib1OriginatingCenters.originatingCentersmap.put(60,
            "National Center for Atmospheric Research (NCAR, Boulder, CO");
        Grib1OriginatingCenters.originatingCentersmap.put(64,
            "Honolulu");
        Grib1OriginatingCenters.originatingCentersmap.put(65,
            "Darwin (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(67,
            "Melbourne (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(69,
            "Wellington (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(74,
            "U.K. Met Office - Bracknell");
        Grib1OriginatingCenters.originatingCentersmap.put(76,
            "Moscow (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(78,
            "Offenbach (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(80,
            "Rome (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(82,
            "Norrkoping");
        Grib1OriginatingCenters.originatingCentersmap.put(85,
            "French Weather Service - Toulouse");
        Grib1OriginatingCenters.originatingCentersmap.put(86,
            "Helsinki");
        Grib1OriginatingCenters.originatingCentersmap.put(87,
            "Belgrade");
        Grib1OriginatingCenters.originatingCentersmap.put(88,
            "Oslo");
        Grib1OriginatingCenters.originatingCentersmap.put(89,
            "Prague");
        Grib1OriginatingCenters.originatingCentersmap.put(90,
            "Episkopi");
        Grib1OriginatingCenters.originatingCentersmap.put(91,
            "Ankara");
        Grib1OriginatingCenters.originatingCentersmap.put(92,
            "Frankfurt/Main (RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(93,
            "London (WAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(94,
            "Copenhagen");
        Grib1OriginatingCenters.originatingCentersmap.put(95,
            "Rota");
        Grib1OriginatingCenters.originatingCentersmap.put(96,
            "Athens");
        Grib1OriginatingCenters.originatingCentersmap.put(97,
            "European Space Agency (ESA)");
        Grib1OriginatingCenters.originatingCentersmap.put(98,
            "European Center for Medium-Range Weather Forecasts - Reading");
        Grib1OriginatingCenters.originatingCentersmap.put(99,
            "DeBilt, Netherlands");
    }

    /**
     * getOriginatingCenter, looks if there exists an originating center
     * with the uspplied key otherwise it returns null.
     *
     * @param key int
     */
    public static String getOriginatingCenter(int key) {
        if (Grib1OriginatingCenters.originatingCentersmap.containsKey(key)) {
            return (String) Grib1OriginatingCenters.originatingCentersmap.get(key);
        }

        return null;
    }

    /**
     * getAll, returns a collection made of all the values we have put inside this object.
     */
    public static Map<Integer,String> getAll() {
        return new HashMap<Integer, String>( Grib1OriginatingCenters.originatingCentersmap);
    }
}
