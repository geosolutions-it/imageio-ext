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
package net.sourceforge.jgrib.util;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Class implementing a comparator for Gregorian Calendar objects
 * 
 * @author Simone Giannecchini.
 *
 */
public final class CalendarComparator implements Comparator<Calendar> {
    /**
     * Method required to implement Comparator. If obj1 is less than obj2,
     * return -1, if equal, return 0, else return 1
     *
     * @param obj1 First object.
     * @param obj2 Second object.
     *
     * @return 0 if the two objects holds the same date time, -1 if the second is more recent,
     * 1 if the first one is more recent.
     */
    public int compare(final Calendar calendar1, final Calendar calendar2) {
        // quick check to see if they're the same PDSLevel
        if (calendar1 == calendar2) {
            return 0;
        }

        if (calendar1.after(calendar2)) {
            return 1;
        } else if (calendar1.before(calendar2)) {
            return -1;
        }

        return 0;

        //
        //        //getting fields
        //        int year1 = calendar1.get(Calendar.YEAR);
        //
        //        //getting fields
        //        int year2 = calendar2.get(Calendar.YEAR);
        //        int month1 = calendar1.get(Calendar.MONTH);
        //        int month2 = calendar2.get(Calendar.MONTH);
        //        int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
        //        int day2 = calendar2.get(Calendar.DAY_OF_MONTH);
        //        int hour1 = calendar1.get(Calendar.HOUR_OF_DAY);
        //        int hour2 = calendar2.get(Calendar.HOUR_OF_DAY);
        //        int minute1 = calendar1.get(Calendar.MINUTE);
        //        int minute2 = calendar2.get(Calendar.MINUTE);
        //
        //        if (year1 < year2) {
        //            return -1;
        //        } else if (year1 > year2) {
        //            return 1;
        //        }
        //
        //        //same year
        //        if (month1 < month2) {
        //            return -1;
        //        } else if (month1 > month2) {
        //            return 1;
        //        }
        //
        //        //same year and month
        //        if (day1 < day2) {
        //            return -1;
        //        } else if (day1 > day2) {
        //            return 1;
        //        }
        //
        //        //same year month and day
        //        if (hour1 < hour2) {
        //            return -1;
        //        } else if (hour1 > hour2) {
        //            return 1;
        //        }
        //
        //        //let's check the minute
        //        if (minute1 < minute2) {
        //            return -1;
        //        } else if (minute1 > minute2) {
        //            return 1;
        //        }
    } // end of method compare
}
