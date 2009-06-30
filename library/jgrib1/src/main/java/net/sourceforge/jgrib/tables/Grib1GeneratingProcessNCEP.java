/*
 *    JGriB1 - OpenSource Java library for GriB files edition 1
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package net.sourceforge.jgrib.tables;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
public final class Grib1GeneratingProcessNCEP {
    static private Map<Integer,String> generatingProcessNCEP = new HashMap<Integer,String>();
	// static private int Maximum = 220;
	// static private int Minimum = 2;

    /**
     * statically loads all the centers into the hash map
     */
    static {
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(02,
            "Ultra Violet Index Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(03,
            "NCEP/ARL Transport and Dispersion Model1");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(05,
            "Satellite Derived Precipitation and temperatures, from IR (See PDS Octet 41... for specific satellite ID)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(10,
            "Global Wind-Wave Forecast Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(19,
            "Limited-area Fine Mesh (LFM) analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(25,
            "Snow Cover Analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(30,
            "Forecaster generated field");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(31,
            "Value added post processed field");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(39,
            "Nested Grid forecast Model (NGM)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(42,
            "Global Optimum Interpolation Analysis (GOI) from GFS model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(43,
            "Global Optimum Interpolation Analysis (GOI) from \"Final\" run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(44,
            "Sea Surface Temperature Analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(45,
            "Coastal Ocean Circulation Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(49,
            "Ozone Analysis from TIROS Observations ");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(52,
            "Ozone Analysis from Nimbus 7 Observations ");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(53,
            "LFM-Fourth Order Forecast Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(64,
            "Regional Optimum Interpolation Analysis (ROI)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(68,
            "80 wave triangular, 18-layer Spectral model from GFS model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(69,
            "80 wave triangular, 18 layer Spectral model from \"Medium Range Forecast\" run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(70,
            "Quasi-Lagrangian Hurricane Model (QLM)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(73,
            "Fog Forecast model - Ocean Prod. Center");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(74,
            "Gulf of Mexico Wind/Wave");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(75,
            "Gulf of Alaska Wind/Wave");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(76,
            "Bias corrected Medium Range Forecast");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(77,
            "126 wave triangular, 28 layer Spectral model from GFS model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(78,
            "126 wave triangular, 28 layer Spectral model from \"Medium Range Forecast\" run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(79,
            "Backup from the previous run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(80,
            "62 wave triangular, 28 layer Spectral model from \"Medium Range Forecast\" run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(81,
            "Spectral Statistical Interpolation (SSI) analysis from  GFS model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(82,
            "Spectral Statistical Interpolation (SSI) analysis from \"Final\" run.");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(83,
            "No longer used");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(84,
            "MESO ETA Model (currently 12 km)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(85,
            "No longer used");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(86,
            "RUC Model, from Forecast Systems Lab (isentropic; scale: 60km at 40N)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(87,
            "CAC Ensemble Forecasts from Spectral (ENSMB)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(88,
            "NOAA Wave Watch III (NWW3) Ocean Wave Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(89,
            "Non-hydrostatic Meso Model (NMM) Currently 8 km)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(90,
            "62 wave triangular, 28 layer spectral model extension of the \"Medium Range Forecast\" run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(91,
            "62 wave triangular, 28 layer spectral model extension of the GFS model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(92,
            "62 wave triangular, 28 layer spectral model run from the \"Medium Range Forecast\" final analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(93,
            "62 wave triangular, 28 layer spectral model run from the T62 GDAS analysis of the \"Medium Range Forecast\" run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(94,
            "T170/L42 Global Spectral Model from MRF run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(95,
            "T126/L42 Global Spectral Model from MRF run");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(96,
            "Global Forecast System Model (formerly known as the Aviation) T254 - Forecast hours 00-84 T170 - Forecast hours 87-180 T126 - Forecast hours 192 - 384");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(98,
            "Climate Forecast System Model -- Atmospheric model (GFS) coupled to a multi level ocean model .   Currently GFS spectral model at T62, 64 levels coupled to 40 level MOM3 ocean model.");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(100,
            "RUC Surface Analysis (scale: 60km at 40N)101	RUC Surface Analysis (scale: 40km at 40N)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(105,
            "RUC Model from FSL (isentropic; scale: 20km at 40N)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(110,
            "ETA Model - 15km version");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(111,
            "Eta model, generic resolution (Used in SREF processing)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(112,
            "WRF-NMM model, generic resolution (Used in various runs) NMM=Nondydrostatic Mesoscale Model (NCEP)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(113,
            "Products from NCEP SREF processing");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(115,
            "Downscaled GFS from Eta eXtension");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(116,
            "WRF-EM model, generic resolution (Used in various runs) EM - Eulerian Mass-core (NCAR - aka Advanced Research WRF)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(120,
            "Ice Concentration Analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(121,
            "Western North Atlantic Regional Wave Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(122,
            "Alaska Waters Regional Wave Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(123,
            "North Atlantic Hurricane Wave Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(124,
            "Eastern North Pacific Regional Wave Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(125,
            "North Pacific Hurricane Wave Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(126,
            "Sea Ice Forecast Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(127,
            "Lake Ice Forecast Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(128,
            "Global Ocean Forecast Model");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(129,
            "Global Ocean Data Analysis System (GODAS)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(130,
            "Merge of fields from the RUC, Eta, and Spectral Model ");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(140,
            "North American Regional Reanalysis (NARR)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(141,
            "Land Data Assimilation and Forecast System");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(150,
            "NWS River Forecast System (NWSRFS)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(151,
            "NWS Flash Flood Guidance System (NWSFFGS)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(152,
            "WSR-88D Stage II Precipitation Analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(153,
            "WSR-88D Stage III Precipitation Analysis");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(180,
            "Quantitative Precipitation Forecast generated by NCEP");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(181,
            "River Forecast Center Quantitative Precipitation Forecast mosaic generated by NCEP");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(182,
            "River Forecast Center Quantitative Precipitation estimate mosaic generated by NCEP");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(183,
            "NDFD product generated by NCEP/HPC");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(190,
            "National Convective Weather Diagnostic generated by NCEP/AWC");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(191,
            "Current Icing Potential automated product genterated by NCEP/AW");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(192,
            "Analysis product from NCEP/AWC");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(193,
            "Forecast product from NCEP/AWC");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(195,
            "Climate Data Assimilation System 2 (CDAS2)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(196,
            "Climate Data Assimilation System 2 (CDAS2) - used for regeneration runs");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(197,
            "Climate Data Assimilation System (CDAS)");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(198,
            "Climate Data Assimilation System (CDAS) - used for regeneration runs");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(200,
            "CPC Manual Forecast Product");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(201,
            "CPC Automated Product");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(210,
            "EPA Air Quality Forecast - Currently North East US domain");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(211,
            "EPA Air Quality Forecast - Currently Eastern US domain");
        Grib1GeneratingProcessNCEP.generatingProcessNCEP.put(220,
            "NCEP/OPC automated product");
    }

    /**
     * getOriginatingCenter, looks if there exists an originating center with
     * the uspplied key otherwise it returns null.
     *
     * @param key int
     *
     * @return DOCUMENT ME!
     */
    public static String generatingProcessNCEP(int key) {
        if (Grib1GeneratingProcessNCEP.generatingProcessNCEP.containsKey(
                    key)) {
            return Grib1GeneratingProcessNCEP.generatingProcessNCEP
            .get(key);
        }

        //if(key<=Grib1GeneratingProcessNCEP.Maximum&& key>=Grib1GeneratingProcessNCEP.Minimum);
        // return "reserved";
        return null;
    }

    /**
     * getAll, returns a collection made of all the values we have put inside
     * this object.
     *
     * @return DOCUMENT ME!
     */
    public static Map<Integer, String> getAll() {
        return new HashMap<Integer, String> (Grib1GeneratingProcessNCEP.generatingProcessNCEP);
    }
}
;
