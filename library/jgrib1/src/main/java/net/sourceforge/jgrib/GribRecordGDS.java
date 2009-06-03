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
 * GribRecordGDS.java  1.0  01/01/2001
 *                     2.0  04 Sep 02
 *
 * (C) Benjamin Stark
 */
package net.sourceforge.jgrib;

import it.geosolutions.io.output.MathUtils;

import java.io.IOException;
import java.io.OutputStream;

import net.sourceforge.jgrib.factory.GribGDSFactorySpi;


/**
 * A class that represents the grid definition section (GDS) of a GRIB record.
 *
 * @author Benjamin Stark
 * @author Capt Richard D. Gonzalez, USAF
 * @author Simone Giannecchini
 * @version 2.0 4 Sep 02 - Modified to be implemented using GribGDSFactory class. This class is used to store the first 32 octets of the GDS, which are common, or similar, in all GDS types. Sometimes names vary slightly in Table D, but functionality is similar, e.g. Grid type     Octet    Id Lat/Lon       7-8      Ni - Number of points along a latitude circle Lambert       7-8      Nx - Number of points along x-axis Other times, functionality is different, e.g. Lat/Lon      18-20     La2 - latitude of grid point Lambert      18-20     Lov - the orientation of the grid However, all sets have at least 32 octets.  Those 32 are stored here, and the differences are resolved in the child classes, and therefore, all attributes are set from the Child classes. The names of the attributes are the same JGrib originally used , for simplicity and continuity.  The fact that some grids use a different number of octets for doubles is irrelevant, as the conversion is stored, not the octets. The child classes should call the proper setters and getters. The class retains every bit of the original functionality, so it can continue to be used in legacy programs (still limited to grid_type 0 and 10). New users should not create instances of this class directly (in fact, it should be changed to an abstract class - it's on the to do list), but use the GribGDS factory instead, and add new child classes (e.g. GribGDSXxxx) as needed for additional grid_types.
 */
public abstract class GribRecordGDS extends GribGDSFactorySpi {
    /** Length in bytes of this section. */
	//default 32 octets (GridLatLon)
    protected int length = 32; 
    
    /** Adiacent rows? */
    protected boolean adiacent_i;

    /** Type of grid (See table 6) */
    protected int grid_type;

    /** Number of grid columns. (Also Ni) */
    protected int grid_nx;

    /** Number of grid rows. (Also Nj) */
    protected int grid_ny;

    /** Latitude of grid start point. */
    protected double grid_lat1;

    /** Longitude of grid start point. */
    protected double grid_lon1;

    /** Mode of grid (See table 7) only 128 supported == increments given) */
    protected int grid_mode = 128;

    /** Latitude of grid end point. */
    protected double grid_lat2;

    /** Longitude of grid end point. */
    protected double grid_lon2;

    /** x-distance between two grid points can be delta-Lon or delta x. */
    protected double grid_dx;

    /** y-distance of two grid points can be delta-Lat or delta y. */
    protected double grid_dy;

    /** Scanning mode (See table 8). */
    protected int grid_scan;

    // rdg - the remaining coordinates are not common to all types, and as such
    //    should be removed.  They are left here (temporarily) for continuity.
    //    These should be implemented in a GribGDSxxxx child class.

    /** y-coordinate/latitude of south pole of a rotated lat/lon grid. */
    protected double grid_latsp;

    /** x-coordinate/longitude of south pole of a rotated lat/lon grid. */
    protected double grid_lonsp;

    /** Rotation angle of rotated lat/lon grid. */
    protected double grid_rotang;

    /** Radius of earth used in calculating projections */
    public final double EARTH_RADIUS = 6367470; // per table 7 - assumes spheroid

    // *** constructors *******************************************************

    /**
     * New constructor created for child classes.
     *
     * @param header - integer array of header data (octets 1-6) read in
     *        GribGDSFactory exceptions are thrown in children and passed up
     */
    public GribRecordGDS(int[] header) {
        // octets 1-3 (GDS section length)
        this.length = MathUtils.uint3(header[0], header[1], header[2]);

        // octet 4 (number of vertical coordinate parameters) and
        // octet 5 (octet location of vertical coordinate parameters
        // not implemented yet
        // octet 6 (grid type)
        this.grid_type = header[5];
    }

    //    /**
    //     * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
    //     *
    //     * @param in bit input stream with GDS content
    //     *
    //     * @throws IOException if stream can not be opened etc.
    //     * @throws NoValidGribException if stream contains no valid GRIB file
    //     *
    //     * @deprecated - This class is on it's way to becoming abstract.  Use one
    //     *             of the child classes, and create it using the
    //     *             GribGDSFactory class
    //     */
    //    public GribRecordGDS(BitInputStream in)
    //        throws IOException, NoValidGribException {
    //        int[] data;
    //
    //        // octets 1-3 (Length of GDS)
    //        data = in.readUI8(3);
    //        this.length = MathUtils.uint3(data[0], data[1], data[2]);
    //
    //        // octets 4-5 not implemented yet
    //        data = in.readUI8(this.length - 3);
    //
    //        // octet 6 (grid type)
    //        this.grid_type = data[2];
    //
    //        if ((this.grid_type != 0) && (this.grid_type != 10)) {
    //            throw new NoValidGribException(
    //                "GribRecordGDS: Only supporting grid type 0 "
    //                + "(latlon grid) and 10 (rotated latlon grid).");
    //        }
    //
    //        data = in.readUI8(this.length - 4);
    //
    //        // octets 7-8 (number of points along a parallel)
    //        this.grid_nx = MathUtils.uint2(data[3], data[4]);
    //
    //        // octets 9-10 (number of points along a meridian)
    //        this.grid_ny = MathUtils.uint2(data[5], data[6]);
    //
    //        // octets 11-13 (latitude of first grid point)
    //        this.grid_lat1 = MathUtils.int3(data[7], data[8], data[9]) / 1000.0;
    //
    //        // octets 14-16 (longitude of first grid point)
    //        this.grid_lon1 = MathUtils.int3(data[10], data[11], data[12]) / 1000.0;
    //
    //        // octet 17 (resolution and component flags -> 128 == increments given.)
    //        this.grid_mode = data[13];
    //
    //        if ((this.grid_mode != 128) && (this.grid_mode != 0)) {
    //            throw new NoValidGribException(
    //                "GribRecordGDS: No other component flag than 128 "
    //                + "(increments given) or 0 (not given) supported. "
    //                + "Current is: " + this.grid_mode);
    //        }
    //
    //        // octets 18-20 (latitude of last grid point)
    //        this.grid_lat2 = MathUtils.int3(data[14], data[15], data[16]) / 1000.0;
    //
    //        // octets 21-23 (longitude of last grid point)
    //        this.grid_lon2 = MathUtils.int3(data[17], data[18], data[19]) / 1000.0;
    //
    //        // increments given
    //        if (this.grid_mode == 128) {
    //            // octets 24-25 (x increment)
    //            this.grid_dx = MathUtils.uint2(data[20], data[21]) / 1000.0;
    //
    //            // octets 26-27 (y increment)
    //            this.grid_dy = -MathUtils.uint2(data[22], data[23]) / 1000.0;
    //
    //            // octet 28 (point scanning mode)
    //            this.grid_scan = data[24];
    //
    //            if ((this.grid_scan & 0x20) == 1) {
    //                //j adiacent
    //                this.adiacent_i = false;
    //            } else {
    //                this.adiacent_i = true;
    //            }
    //
    //            if ((this.grid_scan & 128) != 0) {
    //                this.grid_dx = -this.grid_dx;
    //            }
    //
    //            // rdg - changed to != 64 here because table 8 shows -j if bit NOT set
    //            if ((this.grid_scan & 64) != 64) {
    //                this.grid_dy = -this.grid_dy;
    //            }
    //        } else {
    //            // calculate increments
    //            this.grid_dx = (this.grid_lon2 - this.grid_lon1) / (this.grid_nx
    //                - 1);
    //            this.grid_dy = (this.grid_lat2 - this.grid_lat1) / (this.grid_ny
    //                - 1);
    //        }
    //
    //        if (this.grid_type == 10) {
    //            // octets 33-35 (lat of s.pole)
    //            this.grid_latsp = MathUtils.int3(data[29], data[30], data[31]) / 1000.0;
    //
    //            // octets 36-38 (lon of s.pole)
    //            this.grid_lonsp = MathUtils.int3(data[32], data[33], data[34]) / 1000.0;
    //
    //            // octets 39-42 (angle of rotation)
    //            this.grid_rotang = MathUtils.int4(data[35], data[36], data[37],
    //                    data[38]) / 1000.0;
    //        }
    //    }

    /**
     * GribRecordGDS
     */
    public GribRecordGDS() {
    }

    // *** public methods **************************************************************
    // rdg - the basic getters can remain here, but other functionality should
    //    be moved to the child GribGDSxxxx classes.  For now, overriding these
    //    methods will work just fine.
    public int hashCode() {
        int result = 17;

        result = (37 * result) + grid_nx;
        result = (37 * result) + grid_ny;

        int intLat1 = Float.floatToIntBits((float) grid_lat1);

        result = (37 * result) + intLat1;

        int intLon1 = Float.floatToIntBits((float) grid_lon1);

        result = (37 * result) + intLon1;

        return result;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GribRecordGDS)) {
            return false;
        }

        if (this == obj) {
            // Same object
            return true;
        }

        GribRecordGDS gds = (GribRecordGDS) obj;

        if (grid_type != gds.grid_type) {
            return false;
        }

        if (grid_mode != gds.grid_mode) {
            return false;
        }

        if (grid_scan != gds.grid_scan) {
            return false;
        }

        if (grid_nx != gds.grid_nx) {
            return false;
        }

        if (grid_ny != gds.grid_ny) {
            return false;
        }

        if (grid_dx != gds.grid_dx) {
            return false;
        }

        if (grid_dy != gds.grid_dy) {
            return false;
        }

        if (grid_lat1 != gds.grid_lat1) {
            return false;
        }

        if (grid_lat2 != gds.grid_lat2) {
            return false;
        }

        if (grid_latsp != gds.grid_latsp) {
            return false;
        }

        if (grid_lon1 != gds.grid_lon1) {
            return false;
        }

        if (grid_lon2 != gds.grid_lon2) {
            return false;
        }

        if (grid_lonsp != gds.grid_lonsp) {
            return false;
        }

        if (grid_rotang != gds.grid_rotang) {
            return false;
        }

        if (isAdiacent_i_Or_j() != gds.isAdiacent_i_Or_j()) {
            return false;
        }

        return true;
    }

    /**
     * rdg - added this method to be used in a comparator for sorting while
     * extracting records. Not currently used in the JGrib library, but is
     * used in a library I'm using that uses JGrib.
     *
     * @param gds - the GribRecordGDS to compare to
     *
     * @return - -1 if gds is "less than" this, 0 if equal, 1 if gds is
     *         "greater than" this.
     */
    public int compare(GribRecordGDS gds) {
        if (this.equals(gds)) {
            return 0;
        }

        // not equal, so either less than or greater than.
        // check if gds is less, if not, then gds is greater
        if (grid_type > gds.grid_type) {
            return -1;
        }

        if (grid_mode > gds.grid_mode) {
            return -1;
        }

        if (grid_scan > gds.grid_scan) {
            return -1;
        }

        if (grid_nx > gds.grid_nx) {
            return -1;
        }

        if (grid_ny > gds.grid_ny) {
            return -1;
        }

        if (grid_dx > gds.grid_dx) {
            return -1;
        }

        if (grid_dy > gds.grid_dy) {
            return -1;
        }

        if (grid_lat1 > gds.grid_lat1) {
            return -1;
        }

        if (grid_lat2 > gds.grid_lat2) {
            return -1;
        }

        if (grid_latsp > gds.grid_latsp) {
            return -1;
        }

        if (grid_lon1 > gds.grid_lon1) {
            return -1;
        }

        if (grid_lon2 > gds.grid_lon2) {
            return -1;
        }

        if (grid_lonsp > gds.grid_lonsp) {
            return -1;
        }

        if (grid_rotang > gds.grid_rotang) {
            return -1;
        }

        // if here, then something must be greater than something else - doesn't matter what
        return 1;
    }

    /**
     * Get length in bytes of this section.
     *
     * @return length in bytes of this section
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Get type of grid. <i>Only 0 (lat/lon grid) and 10 (rot lat/lon grid)
     * supported so far.</i>
     *
     * @return type of grid
     */
    public int getGridType() {
        return this.grid_type;
    }

    /**
     * Set type of grid.  This is type 0.
     *
     * @param grid_type DOCUMENT ME!
     */
    public void setGridType(int grid_type) {
        this.grid_type = grid_type;
    }

    /**
     * Get number of grid columns.
     *
     * @return number of grid columns
     */
    public int getGridNX() {
        return this.grid_nx;
    }

    /**
     * set number of grid columns.
     *
     * @param grid_nx DOCUMENT ME!
     */
    public void setGridNX(int grid_nx) {
        this.grid_nx = grid_nx;
    }

    /**
     * Get number of grid rows.
     *
     * @return number of grid rows.
     */
    public int getGridNY() {
        return this.grid_ny;
    }

    /**
     * set number of grid rows.
     *
     * @param grid_ny DOCUMENT ME!
     */
    public void setGridNY(int grid_ny) {
        this.grid_ny = grid_ny;
    }

    /**
     * Get y-coordinate/latitude of grid start point.
     *
     * @return y-coordinate/latitude of grid start point
     */
    public double getGridLat1() {
        return this.grid_lat1;
    }

    /**
     * set latitude of grid start point.
     *
     * @param grid_lat1 DOCUMENT ME!
     */
    public void setGridLat1(double grid_lat1) {
        this.grid_lat1 = grid_lat1;
    }

    /**
     * Get x-coordinate/longitude of grid start point.
     *
     * @return x-coordinate/longitude of grid start point
     */
    public double getGridLon1() {
        return this.grid_lon1;
    }

    /**
     * set longitude of grid start point.
     *
     * @param grid_lon1 DOCUMENT ME!
     */
    public void setGridLon1(double grid_lon1) {
        this.grid_lon1 = grid_lon1;
    }

    /**
     * Get grid mode. <i>Only 128 (increments given) supported so far.</i>
     *
     * @return grid mode
     */
    public int getGridMode() {
        return this.grid_mode;
    }

    public void setGridMode(int grid_mode) {
        this.grid_mode = grid_mode;
    }

    /**
     * Get y-coordinate/latitude of grid end point.
     *
     * @return y-coordinate/latitude of grid end point
     */
    public double getGridLat2() {
        return this.grid_lat2;
    }

    /**
     * set latitude of grid end point.
     *
     * @param grid_lat2 DOCUMENT ME!
     */
    public void setGridLat2(double grid_lat2) {
        this.grid_lat2 = grid_lat2;
    }

    /**
     * Get x-coordinate/longitude of grid end point.
     *
     * @return x-coordinate/longitude of grid end point
     */
    public double getGridLon2() {
        return this.grid_lon2;
    }

    /**
     * set longitude of grid end point.
     *
     * @param grid_lon2 DOCUMENT ME!
     */
    public void setGridLon2(double grid_lon2) {
        this.grid_lon2 = grid_lon2;
    }

    /**
     * Get x-increment/distance between two grid points.
     *
     * @return x-increment
     */
    public double getGridDX() {
        return this.grid_dx;
    }

    /**
     * Set delta-Lon between two grid points.
     *
     * @param grid_dx DOCUMENT ME!
     */
    public void setGridDX(double grid_dx) {
        this.grid_dx = grid_dx;
    }

    /**
     * Get y-increment/distance between two grid points.
     *
     * @return y-increment
     */
    public double getGridDY() {
        return this.grid_dy;
    }

    /**
     * Set delta-Lat between two grid points.
     *
     * @param grid_dy DOCUMENT ME!
     */
    public void setGridDY(double grid_dy) {
        this.grid_dy = grid_dy;
    }

    /**
     * Get scan mode (sign of increments). <i>Only 64, 128 and 192 supported so
     * far.</i>
     *
     * @return scan mode
     */
    public int getGridScanmode() {
        return this.grid_scan;
    }

    /**
     * Set scan mode (sign of increments). <i>Only 64, 128 and 192 supported so
     * far.</i>
     *
     * @param plus_i DOCUMENT ME!
     * @param plus_j DOCUMENT ME!
     * @param adiacents_i DOCUMENT ME!
     */
    public void setGridScanmode(boolean plus_i, boolean plus_j,
        boolean adiacents_i) {
        this.grid_scan = 0;

        if (!plus_i) {
            this.grid_scan |= (1 << 7);
        } else {
            this.grid_scan &= ~(1 << 7);
        }

        if (plus_j) {
            this.grid_scan |= (1 << 6);
        } else {
            this.grid_scan &= ~(1 << 6);
        }

        if (!adiacents_i) {
            this.grid_scan |= (1 << 5);
        } else {
            this.grid_scan &= ~(1 << 5);
        }

        this.grid_scan &= 0xe0;
        this.adiacent_i = adiacents_i;
    }

    /**
     * Get y-coordinate/latitude of south pole of a rotated latitude/longitude
     * grid.
     *
     * @return latitude of south pole
     */
    public double getGridLatSP() {
        return this.grid_latsp;
    }

    /**
     * set y-coordinate/latitude of south pole of a rotated latitude/longitude
     * grid.
     *
     * @param grid_latsp DOCUMENT ME!
     */
    public void setGridLatSP(double grid_latsp) {
        this.grid_latsp = grid_latsp;
    }

    /**
     * Get x-coordinate/longitude of south pole of a rotated latitude/longitude
     * grid.
     *
     * @return longitude of south pole
     */
    public double getGridLonSP() {
        return this.grid_lonsp;
    }

    /**
     * set x-coordinate/longitude of south pole of a rotated latitude/longitude
     * grid.
     *
     * @param grid_lonsp DOCUMENT ME!
     */
    public void setGridLonSP(double grid_lonsp) {
        this.grid_lonsp = grid_lonsp;
    }

    /**
     * Get grid rotation angle of a rotated latitude/longitude grid.
     *
     * @return rotation angle
     */
    public double getGridRotAngle() {
        return this.grid_rotang;
    }

    /**
     * Set grid rotation angle of a rotated latitude/longitude grid.
     *
     * @param grid_rotang DOCUMENT ME!
     */
    public void setGridRotAngle(double grid_rotang) {
        this.grid_rotang = grid_rotang;
    }

    /**
     * Get all longitide coordinates
     *
     * @return longtitude as double
     */

    /*  rdg - these should be implemented in the child classes, as they are not longer generic
       public double[] getXCoords()
       {
          // alloc
          double[] coords = new double[grid_nx];
          int k = 0;
          for (int x = 0; x < grid_nx; x++)
          {
             double longi = grid_lon1 + x * grid_dx;
             // move x-coordinates to the range -180..180
             if (longi >= 180.0) longi = longi - 360.0;
             if (longi < -180.0) longi = longi + 360.0;
             coords[k++] = longi;
          }
          return coords;
       }
     */
    /*  rdg - these should be implemented in the child classes, as they are not longer generic
       /**
     * Get all latitude coordinates
     * @returns latitude as double
     */
    /*   public double[] getYCoords()
       {
          // alloc
          double[] coords = new double[grid_ny];
          int k = 0;
          for (int y = 0; y < grid_ny; y++)
          {
             double lati = grid_lat1 + y * grid_dy;
             if (lati > 90.0 || lati < -90.0)
       System.err.println("GribRec: latitude out of range (-90 to 90).");
               coords[k++] = lati;
            }
            return coords;
         }
     */

    /**
     * Get grid coordinates in longitude/latitude
     *
     * @return longitide/latituide as doubles
     */
    public double[] getGridCoords() {
        // alloc
        double[] coords = new double[grid_ny * grid_nx * 2];

        // How to handle rotated grids ??
        int k = 0;

        for (int y = 0; y < grid_ny; y++) {
            for (int x = 0; x < grid_nx; x++) {
                double longi = grid_lon1 + (x * grid_dx);
                double lati = grid_lat1 + (y * grid_dy);

                // move x-coordinates to the range -180..180
                if (longi >= 180.0) {
                    longi = longi - 360.0;
                }

                if (longi < -180.0) {
                    longi = longi + 360.0;
                }

                if ((lati > 90.0) || (lati < -90.0)) {
                    System.err.println(
                        "GribRec: latitude out of range (-90 to 90).");
                }

                //coords[grid_nx * y + x] =
                coords[k++] = longi;
                coords[k++] = lati;
            }
        }

        return coords;
    }

    /**
     * Get a string representation of this GDS.
     *
     * @return string representation of this GDS
     */
    public String toString() {
        String str = "    GDS section:\n      ";

        if (this.grid_type == 0) {
            str += "  LatLon Grid";
        }

        if (this.grid_type == 10) {
            str += "  Rotated LatLon Grid";
        }

        str += ("  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ");
        str += ("  lon: " + this.grid_lon1 + " to " + this.grid_lon2);
        str += ("  (dx " + this.grid_dx + ")\n      ");
        str += ("  lat: " + this.grid_lat1 + " to " + this.grid_lat2);
        str += ("  (dy " + this.grid_dy + ")");

        if (this.grid_type == 10) {
            str += ("\n        south pole: lon " + this.grid_lonsp + " lat "
            + this.grid_latsp);
            str += ("\n        rot angle: " + this.grid_rotang);
        }

        return str;
    }

    /**
     * writeTo
     *
     * @param out OutputStream
     *
     * @throws IOException DOCUMENT ME!
     */
    public abstract void writeTo(OutputStream out) throws IOException;

    /**
     * isAdiacent_i_Or_j
     *
     * @return DOCUMENT ME!
     */
    public boolean isAdiacent_i_Or_j() {
        return this.adiacent_i;
    }

    /**
     * @param length The length to set.
     */
    public void setLength(int length) {
        this.length = length;
    }
}