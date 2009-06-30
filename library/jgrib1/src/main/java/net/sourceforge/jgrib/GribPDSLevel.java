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

package net.sourceforge.jgrib;

import it.geosolutions.io.output.MathUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 */
public final class GribPDSLevel {

	/** Logger for the GribFile class. */
	private final static Logger LOGGER = Logger.getLogger(GribPDSLevel.class.toString());
	
    /**
     * Index number from table 3 - can be used for comparison even if the
     * description of the level changes
     */
    private int index;

    /** Name of the vertical coordinate/level */
    private String name = null;

    /**
     * Value of PDS octet10 if separate from 11, otherwise value from
     * octet10&11
     */
    private double value1 = Double.NaN;

    /** Value of PDS octet11 */
    private double value2 = Double.NaN;

    /**
     * Stores a short name of the level - same as the string "level" in the
     * original GribRecordPDS implementation
     */
    private String level = "";

    /** Stores a descriptive name of the level GribRecordPDS implementation */
    private String description = "";

    /**
     * Stores the name of the level - same as the string "level" in the
     * original GribRecordPDS implementation
     */
    private String units = "";

    /**
     * Stores whether this is (usually) a vertical coordinate for a single
     * layer (e.g. surface, tropopause level) or multiple layers (e.g. hPa, m
     * AGL) Aids in deciding whether to build 2D or 3D grids from the data
     */
    private boolean isSingleLayer = true;

    /**
     * Indicates whether the vertical coordinate increases with height. e.g.
     * false for pressure and sigma, true for height above ground or if
     * unknown
     */
    private boolean isIncreasingUp = true;

    /**
     * True if a numeric values are used for this level (e.g. 1000 mb) False if
     * level doesn't use values (e.g. surface). Basically indicates whether
     * you will be able to get a value for this level.
     */
    private boolean isNumeric = false;


    /**
     * Constructor.  Creates a GribPDSLevel based on octets 10-12 of the PDS.
     * Implements tables 3 and 3a.
     *
     * @param pds10 part 1 of level code
     * @param pds11 part 2 of level code
     * @param pds12 part 3 of level code
     */
    public GribPDSLevel(final int pds10, final int pds11, final int pds12) {
        int pds1112 = (pds11 << 8) | pds12;

        this.index = pds10;

        switch (index) {
        case 1:
            name ="SFC";
            description = level = "Ground or water surface";

            break;

        case 2:
            name ="CBL";
            description = level = "Cloud base level";

            break;

        case 3:
            name ="CTL";
            description = level = "Cloud top level";

            break;

        case 4:
            name = "0DEG";
            description = level = "Level of 0 deg (C) isotherm";

            break;

        case 5:
            name = "ADCL";
            description = level = "Level of adiabatic condensation lifted from the surface";

            break;

        case 6:
            name ="MWSL";
            description = level = "Maximum wind level";

            break;

        case 7:
            name ="TRO";
            description = level = "Tropopause";

            break;

        case 8:
            name = "NTAT";
            description = level = "Nominal top of atmosphere";

            break;

        case 9:
            name = "SEAB";
            description = level = "Sea bottom";

            break;

        case 20:
            name = "TMPL";
            value1 = pds1112;
            units = "K";
            isNumeric = true;
            level = "Isothermal level";
            description = "Isothermal level at " + (value1 / 100) + units;
            break;

        case 100:
            name = "ISBL";
            value1 = pds1112;
            units = "hPa";
            isNumeric = true;
            isIncreasingUp = false;
            isSingleLayer = false;
            level = "isobaric level";
            description = "pressure at " + value1 + " " + units;

            break;

        case 101:
            name = "ISBY";
            value1 = pds11 * 10; // convert from kPa to hPa - who uses kPa???
            value2 = pds12 * 10;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two isobaric levels";
            description = "layer between " + value1 + " and " + value2 + " "
                + units;

            break;

        case 102:
            name = "MSL";
            isNumeric = true;
            value1=0;
            value2=0;
            description = level = "mean sea level";
            

            break;

        case 103:
            name = "GPML";
            value1 = pds1112;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level ="Specified altitude above MSL";
            description = value1 + " m above mean sea level";

            break;

        case 104:
            name = "GPMY";
            value1 = (pds11 * 100); // convert hm to m
            value2 = (pds12 * 100);
            units = "m";
            isNumeric = true;
            isSingleLayer = false;            
            level ="Layer between two altitudes above MSL";
            description = "Layer between " + pds11 + " and " + pds12
                + " m above mean sea level";

            break;

        case 105:
            name ="HTGL";
            value1 = pds1112;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level =  "specified height level above ground";
            description = value1 + " m above ground";

            break;

        case 106:
            name = "HTGY";
            value1 = (pds11 * 100); // convert hm to m
            value2 = (pds12 * 100);
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level ="layer between two specified height levels above ground";
            description = "Layer between " + value1 + " and " + value2
                + " m above ground";

            break;

        case 107:
            name = "SIGL";
            value1 = (pds1112 / 10000.0f);
            level = "sigma level";
            units = "sigma";
            isNumeric = true;
            isSingleLayer = false;
            isIncreasingUp = false;
            description = "sigma = " + value1;

            break;

        case 108:
            name = "SIGY";
            value1 = (pds11 / 100.0f);
            value2 = (pds12 / 100.0f);
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two sigma levels";
            description = "Layer between sigma levels " + value1 + " and "
                + value2;

            break;

        case 109:
            name ="HYBL";
            value1 = pds1112;
            isNumeric = true;
            isSingleLayer = false;
            level = "Hybrid level";
            description = "hybrid level " + value1;

            break;

        case 110:
            name = "HYBY";
            value1 = pds11;
            value2 = pds12;
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two hybrid levels";
            description = "Layer between hybrid levels " + value1 + " and "
                + value2;

            break;

        case 111:
            name = "DBLL";
            value1 = pds1112;
            units = "cm";
            isNumeric = true;
            isSingleLayer = false;
            level ="depth below land surface";
            description = value1 + " " + units;

            break;

        case 112:
            name ="DBLY";
            value1 = pds11;
            value2 = pds12;
            units = "cm";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two depths below land surface";
            description = "Layer between " + value1 + " and " + value2
                + " cm below land surface";

            break;

        case 113:
            name = "THEL";
            value1 = pds1112;
            units = "K";
            isNumeric = true;
            isSingleLayer = false;
            level = "isentropic (theta) level";
            description = value1 + " K";

            break;

        case 114:
            name = "THEY";
            value1 = (pds11 + 475);
            value2 = (pds12 + 475);
            units = "K";
            isNumeric = true;
            isSingleLayer = false;
            level="layer between two isentropic levels";
            description = "Layer between " + value1 + " and " + value2 + " K";

            break;

        case 115:
            name ="SPDL";
            value1 = pds1112;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level =  "level at specified pressure difference from ground to level";
            description = value1 + " hPa";

            break;

        case 116:
            name = "SPDY";
            value1 = pds11;
            value2 = pds12;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level = "Layer between pressure differences from ground to levels";
            description = "Layer between pressure differences from ground: "
                + value1 + " and " + value2 + " K";

            break;

        case 125:
            name = "HGLH";
            value1 = pds1112;
            units = "cm";
            isNumeric = true;
            isSingleLayer = false;
            level = "Height above ground (high precision)";
            description = value1 + " " + units + " above ground";

            break;
        case 126:
            name = "ISBP";
            value1 = pds1112;
            units = "Pa";
            isNumeric = true;
            isSingleLayer = false;
            level = "isobaric level";
            description = value1 + " " + units + " above ground";

            break;

        case 128:
            name = "SGYH";
            value1 = pds11;
            value2=pds12;
            units = "";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two sigma levels";
            description = "layer between two sigma levels "+value1 + " and "+ value2;

            break;  
            
            
        case 141:
            name = "IBYM";
            value1 = pds11;
            value2=pds12;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two isobaric surfaces";
            description = "layer between two isobaric surfaces "+value1 + "hPa and "+ value2 + " hPa";

            break;  
        case 160:
            name = "DBSL";
            value1 = pds1112;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level = "Depth below sea level";
            description = pds1112 + " m below sea level";

            break;

        case 200:
            name ="EATM";
            this.value1=0;
            this.value2=0;            
            description = level = "entire atmosphere layer";

            break;

        case 201:
            name = "EOCN";
            this.value1=0;
            this.value2=0;
            description = level = "entire ocean layer";
            break;

        case 204:
            name ="HTFL"; 
            description = level = "Highest tropospheric freezing level";

            break;
        case 206:
            name = "GCBL";
            description = level = "Grid scale cloud bottom level";

            break;
        case 207:
            name = "GCTL";
            description = level = "Grid scale cloud top level";

            break;     
        case 209:
            name = "BCBL";
            description = level = "Boundary layer cloud bottom level";

            break;       
        case 210:
            name = "BCTL";
            description = level = "Boundary layer cloud top level";

            break;    
        case 211:
            name = "BCY";
            description = level = "Boundary layer cloud layer";

            break;  
        case 212:
            name = "LCBL";
            description = level = "Low cloud bottom level";

            break;          
        case 213:
            name = "LCTL";
            description = level = "Low cloud top level";

            break;            
        case 214:
        	name = "LCY";
            name = description = level = "Low Cloud Layer";

            break;
        case 222:
        	name = "MCBL";
            description = level = "Middle cloud bottom level";

            break;
        case 223:
        	name = "MCTL";
            description = level = "Middle cloud top level";

            break; 
            
        case 224:
            name = "MCY";
            description = level = "Middle Cloud Layer";

            break;
        case 232:
        	name = "HCBL";
            description = level = "High cloud bottom level";

            break; 
        case 233:
        	name = "HCTL";
            description = level = "High cloud top level";

            break;            
        case 234:
            name ="HCY";
            description = level = "High Cloud Layer";

            break;

        case 235:
            name ="OITL";
            description = level = "Ocean Isotherm Level (1/10 deg C)	";
            break;
            
        case 236:
            name ="OLYR";
            value1 = pds11;
            value2 = pds12;
            units = "M";
            isNumeric = true;
            isSingleLayer = false;
            level = value1 + units + " - " + value2 + units;
            description = "Layer between two depths below ocean surface: "
                + value1 + " and " + value2 + " M";
            break;

        case 237:
            name ="OBML";
            description = level = "Bottom of Ocean Mixed Layer (m)";
            break;

        case 238:
            name ="OBIL";
            description = level = "Bottom of Ocean Isothermal Layer (m)";
            break;

        case 242:
            name ="CCBL";
            description = level = "Convective cloud bottom level";
            break;
            
        case 243:
            name ="CCTL";
            description = level = "Convective cloud top level";
            break;
        case 244:
            name ="CCY";
            description = level = "Convective cloud layer";
            break;
        case 245:
            name ="LLTW";
            description = level = "Lowest level of the wet bulb zero";
            break;
        case 246:
            name ="MTHE";
            description = level = "Maximum equivalent potential temperature level";
            break;
        case 247:
            name ="EHLT";
            description = level = "Equilibrium level";
            break;
        case 248:
            name ="SCBL";
            description = level = "Shallow convective cloud bottom level";
            break;
        case 249:
            name ="SCTL";
            description = level = "Shallow convective cloud top level";
            break;
        case 251:
            name ="DCBL";
            description = level = "Deep convective cloud bottom level";
            break;
        case 252:
            name ="DCTL";
            description = level = "Deep convective cloud top level";
            break;
        default:
            name = description = "undefined level";
            units = "undefined units";
            if(LOGGER.isLoggable(Level.WARNING))
            	LOGGER.warning("GribPDSLevel: Table 3 level " + index + " is not implemented yet");

            break;
        }
    }

    public GribPDSLevel(final int pds10, final double value1, final double value2) {
        this.index = pds10;

        switch (index) {
        case 1:
            name ="SFC";
            description = level = "Ground or water surface";

            break;

        case 2:
            name ="CBL";
            description = level = "Cloud base level";

            break;

        case 3:
            name ="CTL";
            description = level = "Cloud top level";

            break;

        case 4:
            name = "0DEG";
            description = level = "Level of 0 deg (C) isotherm";

            break;

        case 5:
            name = "ADCL";
            description = level = "Level of adiabatic condensation lifted from the surface";

            break;

        case 6:
            name ="MWSL";
            description = level = "Maximum wind level";

            break;

        case 7:
            name ="TRO";
            description = level = "Tropopause";

            break;

        case 8:
            name = "NTAT";
            description = level = "Nominal top of atmosphere";

            break;

        case 9:
            name = "SEAB";
            description = level = "Sea bottom";

            break;
        case 20:
            name = "TMPL";
            this.value1 = value1;
            units = "K";
            isNumeric = true;
            level = "Isothermal level";
            description = "Isothermal level at " + (value1 / 100) + units;
            break;

        case 100:
            name = "ISBL";
            this.value1 = value1;
            units = "hPa";
            isNumeric = true;
            isIncreasingUp = false;
            isSingleLayer = false;
            level = "isobaric level";
            description = "pressure at " + value1 + " " + units;

            break;

        case 101:
            name = "ISBY";
            this.value1 = value1 ; // convert from kPa to hPa - who uses kPa???
            this.value2 = value2;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two isobaric levels";
            description = "layer between " + value1 + " and " + value2 + " "
                + units;

            break;

        case 102:
            name = "MSL";
            isNumeric = true;
            this.value1=0;
            this.value2=0;
            description = level = "mean sea level";
            

            break;

        case 103:
            name = "GPML";
            this.value1 = value1;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level ="Specified altitude above MSL";
            description = value1 + " m above mean sea level";

            break;

        case 104:
            name = "GPMY";
            this.value1 = value1; // convert hm to m
            this.value2 = value2;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;            
            level ="Layer between two altitudes above MSL";
            description = "Layer between " + value1 + " and " + value2
                + " m above mean sea level";

            break;

        case 105:
            name ="HTGL";
            this.value1 = value1;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level =  "specified height level above ground";
            description = this.value1 + " m above ground";

            break;

        case 106:
            name = "HTGY";
            this.value1 = value1; // convert hm to m
            this.value2 = value2;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level ="layer between two specified height levels above ground";
            description = "Layer between " + this.value1 + " and " + value2
                + " m above ground";

            break;

        case 107:
            name = "SIGL";
            this.value1 = value1;
            level = "sigma level";
            units = "sigma";
            isNumeric = true;
            isSingleLayer = false;
            isIncreasingUp = false;
            description = "sigma = " + this.value1;

            break;

        case 108:
            name = "SIGY";
            this.value1 = value1;
            this.value2 = value2;
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two sigma levels";
            description = "Layer between sigma levels " + this.value1 + " and "
                + value2;

            break;

        case 109:
            name ="HYBL";
            this.value1 = value1;
            isNumeric = true;
            isSingleLayer = false;
            level = "Hybrid level";
            description = "hybrid level " + this.value1;

            break;

        case 110:
            name = "HYBY";
            this.value1 = value1;
            this.value2 = value2;
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two hybrid levels";
            description = "Layer between hybrid levels " + this.value1 + " and "
                + value2;

            break;

        case 111:
            name = "DBLL";
            this.value1 = value1;
            units = "cm";
            isNumeric = true;
            isSingleLayer = false;
            level ="depth below land surface";
            description = this.value1 + " " + units;

            break;

        case 112:
            name ="DBLY";
            this.value1 = value1;
            this.value2 = value2;
            units = "cm";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two depths below land surface";
            description = "Layer between " + this.value1 + " and " + value2
                + " cm below land surface";

            break;

        case 113:
            name = "THEL";
            this.value1 = value1;
            units = "K";
            isNumeric = true;
            isSingleLayer = false;
            level = "isentropic (theta) level";
            description = this.value1 + " K";

            break;

        case 114:
            name = "THEY";
            this.value1 = value1 ;
            this.value2 = value2  ;
            units = "K";
            isNumeric = true;
            isSingleLayer = false;
            level="layer between two isentropic levels";
            description = "Layer between " + this.value1 + " and " + value2 + " K";

            break;

        case 115:
            name ="SPDL";
            this.value1 = value1;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level =  "level at specified pressure difference from ground to level";
            description = this.value1 + " hPa";

            break;

        case 116:
            name = "SPDY";
            this.value1 = value1;
            this.value2 = value2;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level = "Layer between pressure differences from ground to levels";
            description = "Layer between pressure differences from ground: "
                + this.value1 + " and " + value2 + " K";

            break;

        case 125:
            name = "HGLH";
            this.value1 = value1;
            units = "cm";
            isNumeric = true;
            isSingleLayer = false;
            level = "Height above ground (high precision)";
            description = this.value1 + " " + units + " above ground";

            break;
        case 126:
            name = "ISBP";
            this.value1 = value1;
            units = "Pa";
            isNumeric = true;
            isSingleLayer = false;
            level = "isobaric level";
            description = this.value1 + " " + units + " above ground";

            break;

        case 128:
            name = "SGYH";
            this.value1 = value1;
            this.value2=value2;
            units = "";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two sigma levels";
            description = "layer between two sigma levels "+this.value1 + " and "+ value2;

            break;  
            
            
        case 141:
            name = "IBYM";
            this.value1 = value1;
            this.value2=value2;
            units = "hPa";
            isNumeric = true;
            isSingleLayer = false;
            level = "layer between two isobaric surfaces";
            description = "layer between two isobaric surfaces "+this.value1 + "hPa and "+ value2 + " hPa";

            break;  
        case 160:
            name = "DBSL";
            this.value1 = value1;
            units = "m";
            isNumeric = true;
            isSingleLayer = false;
            level = "Depth below sea level";
            description = value1 + " m below sea level";

            break;

        case 200:
            name ="EATM";
            this.value1=0;
            this.value2=0;            
            description = level = "entire atmosphere layer";

            break;

        case 201:
            name = "EOCN";
            this.value1=0;
            this.value2=0;
            description = level = "entire ocean layer";
            break;

        case 204:
            name ="HTFL"; 
            description = level = "Highest tropospheric freezing level";

            break;
        case 206:
            name = "GCBL";
            description = level = "Grid scale cloud bottom level";

            break;
        case 207:
            name = "GCTL";
            description = level = "Grid scale cloud top level";

            break;     
        case 209:
            name = "BCBL";
            description = level = "Boundary layer cloud bottom level";

            break;       
        case 210:
            name = "BCTL";
            description = level = "Boundary layer cloud top level";

            break;    
        case 211:
            name = "BCY";
            description = level = "Boundary layer cloud layer";

            break;  
        case 212:
            name = "LCBL";
            description = level = "Low cloud bottom level";

            break;          
        case 213:
            name = "LCTL";
            description = level = "Low cloud top level";

            break;            
        case 214:
        	name = "LCY";
            name = description = level = "Low Cloud Layer";

            break;
        case 222:
        	name = "MCBL";
            description = level = "Middle cloud bottom level";

            break;
        case 223:
        	name = "MCTL";
            description = level = "Middle cloud top level";

            break; 
            
        case 224:
            name = "MCY";
            description = level = "Middle Cloud Layer";

            break;
        case 232:
        	name = "HCBL";
            description = level = "High cloud bottom level";

            break; 
        case 233:
        	name = "HCTL";
            description = level = "High cloud top level";

            break;            
        case 234:
            name ="HCY";
            description = level = "High Cloud Layer";

            break;

        case 235:
            name ="OITL";
            description = level = "Ocean Isotherm Level (1/10 deg C)	";
            break;
            
        case 236:
            name ="OLYR";
            this.value1 = value1;
            this.value2 = value2;
            units = "M";
            isNumeric = true;
            isSingleLayer = false;
            level = this.value1 + units + " - " + value2 + units;
            description = "Layer between two depths below ocean surface: "
                + this.value1 + " and " + this.value2 + " M";
            break;

        case 237:
            name ="OBML";
            description = level = "Bottom of Ocean Mixed Layer (m)";
            break;

        case 238:
            name ="OBIL";
            description = level = "Bottom of Ocean Isothermal Layer (m)";
            break;

        case 242:
            name ="CCBL";
            description = level = "Convective cloud bottom level";
            break;
            
        case 243:
            name ="CCTL";
            description = level = "Convective cloud top level";
            break;
        case 244:
            name ="CCY";
            description = level = "Convective cloud layer";
            break;
        case 245:
            name ="LLTW";
            description = level = "Lowest level of the wet bulb zero";
            break;
        case 246:
            name ="MTHE";
            description = level = "Maximum equivalent potential temperature level";
            break;
        case 247:
            name ="EHLT";
            description = level = "Equilibrium level";
            break;
        case 248:
            name ="SCBL";
            description = level = "Shallow convective cloud bottom level";
            break;
        case 249:
            name ="SCTL";
            description = level = "Shallow convective cloud top level";
            break;
        case 251:
            name ="DCBL";
            description = level = "Deep convective cloud bottom level";
            break;
        case 252:
            name ="DCTL";
            description = level = "Deep convective cloud top level";
            break;
        default:
            name = description = "undefined level";
            units = "undefined units";
            if(LOGGER.isLoggable(Level.WARNING))
            	LOGGER.warning("GribPDSLevel: Table 3 level " + index + " is not implemented yet");

            break;
        }
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getDesc() {
        return description;
    }

    public String getUnits() {
        return units;
    }

    public double getValue1() {
        return value1;
    }

    public double getValue2() {
        return value2;
    }

    public boolean getIsNumeric() {
        return isNumeric;
    }

    public boolean getIsIncreasingUp() {
        return isIncreasingUp;
    }

    public boolean getIsSingleLayer() {
        return isSingleLayer;
    }

    /**
     * Formats the class for output
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return "  Level:" + '\n' + "        level id: " + this.index
        + "\n" + "        name: " + this.name + "\n" + "        description: "
        + this.description + "\n" + "        units: " + this.units + "\n"
        + "        short descr: " + this.level + "\n"
        + "        increasing up?: " + this.isIncreasingUp + "\n"
        + "        single layer?: " + this.isSingleLayer + "\n"
        + "        numeric?: " + this.isNumeric + "\n"
        + "        value1: " + this.value1 + "\n" + "        value2: "
        + this.value2 + "\n";
    }

    /**
     * rdg - added equals method didn't check everything as most are set in the
     * constructor
     *
     * @param obj DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(final Object obj) {
        if (!(obj instanceof GribPDSLevel)) {
            return false;
        }

        // quick check to see if same object
        if (this == obj) {
            return true;
        }

        final GribPDSLevel lvl = (GribPDSLevel) obj;

        if (index != lvl.getIndex()) {
            return false;
        }

        if (Double.isNaN(value1) != Double.isNaN(lvl.getValue1())) {
            return false;
        }

        if (!Double.isNaN(value1) && (value1 != lvl.getValue1())) {
            return false;
        }

        if (Double.isNaN(value2) != Double.isNaN(lvl.getValue2())) {
            return false;
        }

        if (!Double.isNaN(value2) && (value2 != lvl.getValue2())) {
            return false;
        }

        return true;
    }

    /**
     * rdg - added this method to be used in a comparator for sorting while
     * extracting records.
     *
     * @param level - the GribRecordGDS to compare to
     *
     * @return - -1 if level is "less than" this, 0 if equal, 1 if level is
     *         "greater than" this.
     */
    public int compare(final GribPDSLevel level) {
        if (this.equals(level)) {
            return 0;
        }

        // check if level is less than this
        if (index > level.getIndex()) {
            return -1;
        }

        if (value1 > level.getValue1()) {
            return -1;
        }

        if (value2 > level.getValue2()) {
            return -1;
        }

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
    public void writeTo(OutputStream out) throws IOException {
        switch (index) {
        case 1:

            //name = description = level = "Ground or water surface";
            out.write(new byte[] { 0, 0 });

            break;

        case 2:

            //name = description = level = "Cloud base level";
            out.write(new byte[] { 0, 0 });

            break;

        case 3:

            // name = description = level = "Cloud top level";
            out.write(new byte[] { 0, 0 });

            break;

        case 4:

            // name = description = level = "Level of 0 deg (C) isotherm";
            out.write(new byte[] { 0, 0 });

            break;

        case 5:

            //name = description = level =
            //  "Level of adiabatic condensation lifted from the surface";
            out.write(new byte[] { 0, 0 });

            break;

        case 6:

            //name = description = level = "Maximum wind level";
            out.write(new byte[] { 0, 0 });

            break;

        case 7:

            //name = description = level = "Tropopause";
            out.write(new byte[] { 0, 0 });

            break;

        case 8:

            //name = description = level = "Nominal top of atmosphere";
            out.write(new byte[] { 0, 0 });

            break;

        case 9:

            //name = description = level = "Sea bottom";
            out.write(new byte[] { 0, 0 });

            break;

        case 20:

            //i am dealing with a single value to be written in two octets
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 100:

            //i am dealing with a single value to be written in two octets
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 101:

            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value1 / 10.0), 1));
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value2 / 10.0), 1));

            break;

        case 102:

            //name = description = level = "mean sea level";
            out.write(new byte[] { 0, 0 });

            break;

        case 103:

            //name = "Specified altitude above MSL";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 104:

            //name = "Layer between two altitudes above MSL";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value1 / 100.0), 1));
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value2 / 100.0), 1));

            break;

        case 105:

            //name = "specified height level above ground";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 106:

            //name = "layer between two specified height levels above ground";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value1 / 100.0), 1));
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value2 / 100.0), 1));

            break;

        case 107:
            name = "sigma level";
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value1 * 10000.0f), 2));

            break;

        case 108:

            //name = "layer between two sigma levels";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value1 * 100.0), 1));
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value2 * 100.0), 1));

            break;

        case 109:

            //name = "Hybrid level";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 110:

            //name = "layer between two hybrid levels";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 1));
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value2), 1));

            break;

        case 111:

            //name = "depth below land surface";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 112:

            //name = "layer between two depths below land surface";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 1));
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value2), 1));

            break;

        case 113:

            //name = "isentropic (theta) level";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 114:

            //name = "layer between two isentropic levels";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value1 - 475), 1));
            out.write(MathUtils.signedInt2Bytes(
                    (int) Math.round(value2 - 475), 1));

            break;

        case 115:

            //name =                    "level at specified pressure difference from ground to level";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 116:

            //name = "Layer between pressure differences from ground to levels";
            //i am dealing with two values to be written in two octets
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 1));
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value2), 1));

            break;

        case 125:

            //name = "Height above ground (high precision)";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 160:

            //name = "Depth below sea level";
            out.write(MathUtils.signedInt2Bytes((int) Math.round(value1), 2));

            break;

        case 200:

            //name = description = level = "entire atmosphere layer";
            out.write(new byte[] { 0, 0 });

            break;

        case 201:

            //name = description = level = "entire ocean layer";
            out.write(new byte[] { 0, 0 });

            break;

        case 204:

            //name = description = level = "Highest tropospheric freezing level";
            out.write(new byte[] { 0, 0 });

            break;

        case 214:

            //name = description = level = "Low Cloud Layer";
            out.write(new byte[] { 0, 0 });

            break;

        case 224:

            //name = description = level = "Middle Cloud Layer";
            out.write(new byte[] { 0, 0 });

            break;

        case 234:

            //name = description = level = "High Cloud Layer";
            out.write(new byte[] { 0, 0 });

            break;

        default:

            //name = description = "undefined level";
            out.write(new byte[] { 0, 0 });

            break;
        }
    }

    public int hashCode() {
        int result = 17;
        result += description.hashCode();
        result += new Integer(index).hashCode();
        result += level.hashCode();
        result += new Integer(index).hashCode();
        result += new Double(value1).hashCode();
        result += new Double(value2).hashCode();

        return result;
    }
}
