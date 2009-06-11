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

import net.sourceforge.jgrib.GribPDSLevel;

public final class GribPDSLevelComparator implements Comparator<GribPDSLevel> {
	/**
	 * Method required to implement Comparator. If obj1 is less than obj2,
	 * return -1, if equal, return 0, else return 1
	 * 
	 * @param obj1
	 *            DOCUMENT ME!
	 * @param obj2
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int compare(GribPDSLevel level1, GribPDSLevel level2) {
		// quick check to see if they're the same PDSLevel
		if (level1 == level2) {
			return 0;
		}

		double z1;
		double z2;
//		String levelType1;
//		String levelType2;
//		int check;


		// compare the levels
		if (level1.getIndex() < level2.getIndex()) {
			return -1;
		}

		if (level1.getIndex() > level2.getIndex()) {
			return 1; // if neither, then equal; continue
		}

		// compare the z levels
		z1 = level1.getValue1();
		z2 = level2.getValue1();

		// if the levels are supposed to decrease with height, reverse
		// comparator
		if (!(level1.getIsIncreasingUp())) {
			z1 = -z1;
			z2 = -z2;
		}

		if (z1 < z2) {
			return -1;
		}

		if (z1 > z2) {
			return 1; // if neither, then equal; continue
		}

		z1 = level1.getValue2();
		z2 = level2.getValue2();

		// if the levels are supposed to decrease with height, reverse
		// comparator
		if (!(level1.getIsIncreasingUp())) {
			z1 = -z1;
			z2 = -z2;
		}

		if (z1 < z2) {
			return -1;
		}

		if (z1 > z2) {
			return 1; // last check, if neither, then equal
		}

		return 0;
	} // end of method compare
}
