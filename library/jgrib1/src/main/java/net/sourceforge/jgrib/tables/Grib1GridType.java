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
public final  class Grib1GridType {
    static private final HashMap<Integer,String> gridTypes = new HashMap<Integer,String>();
    static private int[][] ranges4Reserved = null;

    /**
     * statically loads all the centers into the hash map
     */
    static {
        Grib1GridType.gridTypes.put(0,
            "Latitude/Longitude Grid - Equidistant Cylindrical or Plate Carree projection");
        Grib1GridType.gridTypes.put(1, "Mercator Projection Grid");
        Grib1GridType.gridTypes.put(2, "Gnomonic Projection Grid");
        Grib1GridType.gridTypes.put(3,
            "Lambert Conformal, secant or tangent, conical or bipolar (normal or oblique) projection");
        Grib1GridType.gridTypes.put(4,
            "Gaussian Latitude/Longitude");
        Grib1GridType.gridTypes.put(5,
            "Polar Stereographic projection Grid");
        Grib1GridType.gridTypes.put(6,
            "Universal Transverse Mercator (UTM) projection");
        Grib1GridType.gridTypes.put(7,
            "Simple polyconic projection");
        Grib1GridType.gridTypes.put(8,
            "Albers equal-area, secant or tangent, conic or bi-polar, projection");
        Grib1GridType.gridTypes.put(9,
            "Miller's cylindrical projection");
        Grib1GridType.gridTypes.put(10,
            "Rotated latitude/longitude grid");
        Grib1GridType.gridTypes.put(13,
            "Oblique Lambert conformal, secant or tangent, conical or bipolar, projection");
        Grib1GridType.gridTypes.put(14,
            "Rotated Gaussian latitude/longitude grid");
        Grib1GridType.gridTypes.put(20,
            "Stretched latitude/longitude grid");
        Grib1GridType.gridTypes.put(24,
            "Stretched Gaussian latitude/longitude grid");
        Grib1GridType.gridTypes.put(30,
            "Stretched and rotated latitude/longitude grids");
        Grib1GridType.gridTypes.put(34,
            "Stretched and rotated Gaussian latitude/longitude grids");
        Grib1GridType.gridTypes.put(50,
            "Spherical Harmonic Coefficients");
        Grib1GridType.gridTypes.put(60,
            "Rotated spherical harmonic coefficients");
        Grib1GridType.gridTypes.put(70,
            "Stretched spherical harmonics");
        Grib1GridType.gridTypes.put(80,
            "Stretched and rotated spherical harmonic coefficients");
        Grib1GridType.gridTypes.put(90,
            "Space view perspective or orthographic");
    }

    /**
     * getOriginatingCenter, looks if there exists an originating center with
     * the uspplied key otherwise it returns null.
     *
     * @param key int
     *
     * @return DOCUMENT ME!
     */
    public static String gridTypes(int key) {
        if (checkReserved(key)) {
            return "reserved";
        }

        if (Grib1GridType.gridTypes.containsKey(key)) {
            return (String) Grib1GridType.gridTypes.get(key);
        }

        return null;
    }

    /**
     * checkReserved
     *
     * @param key int
     *
     * @return boolean
     */
    private static boolean checkReserved(int key) {
        for (int i = 0; i < Grib1GridType.ranges4Reserved.length; i++) {
            if ((key >= Grib1GridType.ranges4Reserved[i][0])
                    && (key <= Grib1GridType.ranges4Reserved[i][1])) {
                return true;
            }
        }

        return false;
    }

    /**
     * getAll, returns a collection made of all the values we have put inside
     * this object.
     *
     * @return DOCUMENT ME!
     */
    public static Map<Integer,String> getAll() {
        Map<Integer,String> clone = new HashMap<Integer,String>(Grib1GridType.gridTypes);

        for (int i = 0; i < Grib1GridType.ranges4Reserved.length; i++) {
            for (int j = Grib1GridType.ranges4Reserved[i][0];
                    j <= Grib1GridType.ranges4Reserved[i][1]; j++) {
                clone.put(j, "reserved");
            }
        }

        return new HashMap<Integer, String>(Grib1GridType.gridTypes);
    }
}
;
