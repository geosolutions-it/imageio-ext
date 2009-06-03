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
/*
 *    JGriB1 - OpenSource Java library for GriB files edition 1
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
package net.sourceforge.jgrib.util;

import java.util.Comparator;
import java.util.GregorianCalendar;

import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.tables.GribPDSLevel;


public final class GribRecordComparator implements Comparator<GribRecord> {
    /**
     * Method required to implement Comparator. If obj1 is less than obj2,
     * return -1, if equal, return 0, else return 1
     *
     * @param obj1 DOCUMENT ME!
     * @param obj2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compare(final GribRecord gr1, GribRecord gr2) {
        // get the records  and all the needed values  
        final GribRecordPDS pds1=gr1.getPDS();
        final GribRecordPDS pds2=gr2.getPDS();
        final GribPDSLevel level1=pds1.getLevel();
        final GribPDSLevel level2=pds2.getLevel();        
        float z1 = level1.getValue1(),  z2 = level2.getValue1();
        final GregorianCalendar time1=(GregorianCalendar) pds1.getGMTForecastTime();
        final GregorianCalendar time2=(GregorianCalendar) pds2.getGMTForecastTime();        




        // quick check to see if they're the same record
        if (gr1 == gr2) 
            return 0;
        
        // compare the forecast times
        if(time1.before(time2))
        	return -1;
        else
        	if(time2.before(time1))
        		return 1;
        
        /**
         * 
         * same forecast time 1
         * let's compare the z levels
         * 
         */
        
        //are they numeric? If they differ in this field we cannot compare them.
        final boolean numeric1=level1.getIsNumeric();
        final boolean numeric2=level2.getIsNumeric();
        if(numeric1!=numeric2)
        	return -1;
        //both of them are not numeric they are equals.
        //I SUPPOSE YOU CHECK DIFFERENTLY IF THEY ARE COMPATIBLE!!!!
        if(!numeric1&&!numeric2)
        	return 0;
	    // if the levels are supposed to decrease with height, reverse comparator
	    if (!(level1.getIsIncreasingUp())) {
	       z1 = -z1;
	       z2 = -z2;
	    }
	    if (z1 < z2) return -1;
	    if (z1 > z2) return 1; 

	    // if not either, then equal and we continue
	    return 0;

 } // end of method compare
}