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
    static private HashMap originatingCentersmap = new HashMap();

    /**
     * statically loads all the centers into the hash map
     */
    static {
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(1),
            "Melbourne (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(2),
            "Melbourne (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(4),
            "Moscow (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(5),
            "Moscow (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(7),
            "US National Weather Service - NCEP (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(8),
            "US National Weather Service - NWSTG (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(9),
            "US National Weather Service - Other (WMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(10),
            "Cairo (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(12),
            "Dakar (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(14),
            "Nairobi (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(16),
            "Atananarivo (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(18),
            "Tunis-Casablanca (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(20),
            "Las Palmas (RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(21),
            "Algiers (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(22),
            "Lagos (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(26),
            "Khabarovsk (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(28),
            "New Delhi (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(30),
            "Novosibirsk (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(32),
            "Tashkent (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(33),
            "Jeddah (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(34),
            "Japanese Meteorological Agency - Tokyo (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(36),
            "Bankok");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(37),
            "Ulan Bator");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(38),
            "Beijing (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(40),
            "Seoul");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(41),
            "Buenos Aires (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(43),
            "Brasilia (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(45),
            "Santiago");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(46),
            "Brasilian Space Agency - INPE");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(51),
            "Miami (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(52),
            "National Hurricane Center, Miami");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(53),
            "Canadian Meteorological Service - Montreal (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(54),
            "Canadian Meteorological Service - Montreal (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(55),
            "San Francisco");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(57),
            "U.S. Air Force - Global Weather Center");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(58),
            "US Navy - Fleet Numerical Oceanography Center");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(59),
            "NOAA Forecast Systems Lab, Boulder CO");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(60),
            "National Center for Atmospheric Research (NCAR), Boulder, CO");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(64),
            "Honolulu");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(65),
            "Darwin (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(67),
            "Melbourne (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(69),
            "Wellington (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(74),
            "U.K. Met Office - Bracknell");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(76),
            "Moscow (RSMC/RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(78),
            "Offenbach (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(80),
            "Rome (RSMC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(82),
            "Norrkoping");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(85),
            "French Weather Service - Toulouse");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(86),
            "Helsinki");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(87),
            "Belgrade");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(88),
            "Oslo");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(89),
            "Prague");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(90),
            "Episkopi");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(91),
            "Ankara");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(92),
            "Frankfurt/Main (RAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(93),
            "London (WAFC)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(94),
            "Copenhagen");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(95),
            "Rota");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(96),
            "Athens");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(97),
            "European Space Agency (ESA)");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(98),
            "European Center for Medium-Range Weather Forecasts - Reading");
        Grib1OriginatingCenters.originatingCentersmap.put(new Integer(99),
            "DeBilt, Netherlands");
    }

    /**
     * getOriginatingCenter, looks if there exists an originating center
     * with the uspplied key otherwise it returns null.
     *
     * @param key int
     */
    public static String getOriginatingCenter(int key) {
        if (Grib1OriginatingCenters.originatingCentersmap.containsKey(
                new Integer(key))) {
            return (String) Grib1OriginatingCenters.originatingCentersmap.get(new Integer(
                    key));
        }

        return null;
    }

    /**
     * getAll, returns a collection made of all the values we have put inside this object.
     */
    public static Map getAll() {
        return (Map) Grib1OriginatingCenters.originatingCentersmap.clone();
    }
}
