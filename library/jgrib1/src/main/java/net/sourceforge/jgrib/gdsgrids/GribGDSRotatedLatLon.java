/*
 * GribGDSRotatedLatLon.java  1.0  28/10/2004
 *
 * Simone Giannecchini
 */
package net.sourceforge.jgrib.gdsgrids;

import it.geosolutions.io.output.MathUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.imageio.stream.ImageInputStream;

import net.sourceforge.jgrib.GribRecordGDS;


/**
 * A class that represents the grid definition section (GDS) of a GRIB record
 * with a Rotated Lat/Lon grid projection.
 *
 * @author  Simone Giannecchini (simboss@tiscali.it)
 * based heavily on the original GribRecordGDS
 *
 * @version 1.0
 */
public final class GribGDSRotatedLatLon extends GribRecordGDS {
	
	public final static int ROTATED_LATLON_GRID_TYPE=10;
    // Attributes for Lat/Lon grid not included in GribRecordGDS

    /**
     * y-coordinate/latitude of south pole of stretching.
     */
    protected double grid_latspst;

    /**
     * x-coordinate/longitude of south pole of stretching.
     */
    protected double grid_lonspst;

    /**
     * Stretching factor rotated lat/lon grid.
     */
    protected double grid_stretchfact;
    private double original_grid_lon1;
    private double original_grid_lat1;
    private double original_grid_lon2;
    private double original_grid_lat2;

    /**
     * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
     *
     * See Table D of NCEP Office Note 388 for details
     *
     * @param in bit input stream with GDS content
     *
     * @throws IOException           if stream can not be opened etc.
     * @throws NoValidGribException  if stream contains no valid GRIB file
     */
    public GribGDSRotatedLatLon(final ImageInputStream in, int[] header)
        throws IOException
    {
        super(header);

        if (this.grid_type != 10) {
            throw new IllegalArgumentException(
                "GribGDSRotatedLatLon: grid_type is not " +
                "Rotated Latitude/Longitude (read grid type " + grid_type +
                ", needed 10)");
        }

        final int temp=this.length - header.length;
        final int[] data = new int[temp];
        for(int i=0;i<temp;i++)
            data[i]=in.read();

        // octets 7-8 (number of points along a parallel)
        this.grid_nx = MathUtils.uint2(data[0], data[1]);

        // octets 9-10 (number of points along a meridian)
        this.grid_ny = MathUtils.uint2(data[2], data[3]);

        // octets 11-13 (latitude of first grid point)
        this.grid_lat1 = MathUtils.int3(data[4], data[5], data[6]) / 1000.0;

        // octets 14-16 (longitude of first grid point)
        this.grid_lon1 = MathUtils.int3(data[7], data[8], data[9]) / 1000.0;

        // octet 17 (resolution and component flags -> 128 == increments given.)
        this.grid_mode = data[10];

        if ((this.grid_mode != 128) && (this.grid_mode != 0)) {
//            throw new NotSupportedException(
//                "GribGDSRotatedLatLon: No other component flag than 128 "
//                + "(increments given) or 0 (not given) supported. "
//                + "Current is: " + this.grid_mode);
        }

        // octets 18-20 (latitude of last grid point)
        this.grid_lat2 = MathUtils.int3(data[11], data[12], data[13]) / 1000.0;

        // octets 21-23 (longitude of last grid point)
        this.grid_lon2 = MathUtils.int3(data[14], data[15], data[16]) / 1000.0;

        // increments given
        if (this.grid_mode == 128) {
            // octets 24-25 (x increment)
            this.grid_dx = MathUtils.uint2(data[17], data[18]) / 1000.0;

            // octets 26-27 (y increment)
            this.grid_dy = MathUtils.uint2(data[19], data[20]) / 1000.0;
        } else {
            // calculate increments
            this.grid_dx = (this.grid_lon2 - this.grid_lon1) / this.grid_nx;
            this.grid_dy = (this.grid_lat2 - this.grid_lat1) / this.grid_ny;
        }

        // octet 28 (point scanning mode - See table 8)
        this.grid_scan = 0xff & data[21];

        if ((this.grid_scan & 0x20) == 1) {
            //j adiacent
            this.adiacent_i = false;
        } else {
            this.adiacent_i = true;
        }

        if ((this.grid_scan & 128) != 0) {
            this.grid_dx = -this.grid_dx;
        }

        // rdg - changed to != 64 here because table 8 shows -j if bit NOT set
        if ((this.grid_scan & 64) != 64) {
            this.grid_dy = -this.grid_dy;
        }

        /*rotation*/
        // octets 33-35 (lat of s.pole)
        this.grid_latsp = MathUtils.int3(data[26], data[27], data[28]) / 1000.0;

        // octets 36-38 (lon of s.pole)
        this.grid_lonsp = MathUtils.int3(data[29], data[30], data[31]) / 1000.0;

        // octets 39-42 (angle of rotation)
        this.grid_rotang =
            MathUtils.int4(data[32], data[33], data[34], data[35]) / 1000.0;

        /*stretched*/
        // octets 43-45 (lat of s.pole str.)
        this.grid_latspst = MathUtils.int3(data[36], data[37], data[38]) / 1000.0;

        // octets 46-48 (lon of s.pole str.)
        this.grid_lonspst = MathUtils.int3(data[39], data[40], data[41]) / 1000.0;

        // octets 49-52 (stretch factor)
        this.grid_stretchfact = MathUtils.int4(data[42], data[43], data[44],
                data[45]) / 1000.0;

        // preparing Projection
        prepProjection();
    }

    /**
     * GribGDSRotatedLatLon
     */
    public GribGDSRotatedLatLon() {
    }

    private void prepProjection() {
        double[] LL = rtll(this.grid_lon1, this.grid_lat1);
        double[] UR = rtll(this.grid_lon2, this.grid_lat2);

        this.original_grid_lon1 = this.grid_lon1;
        this.original_grid_lat1 = this.grid_lat1;

        this.original_grid_lon2 = this.grid_lon2;
        this.original_grid_lat2 = this.grid_lat2;

        this.grid_lon1 = LL[0];
        this.grid_lat1 = LL[1];

        this.grid_lon2 = UR[0];
        this.grid_lat2 = UR[0];
    }

    private double[] rtll(double lon, double lat) {
        double[] all = new double[2];

        double dtr = Math.PI / 180.;

        double pole_lat = 90.0 + this.grid_latsp;
        double pole_lon = this.grid_lonsp;

        double ctph0 = Math.cos(pole_lat * dtr);
        double stph0 = Math.sin(pole_lat * dtr);

        double stph = Math.sin(lat * dtr);
        double ctph = Math.cos(lat * dtr);
        double ctlm = Math.cos(lon * dtr);
        double stlm = Math.sin(lon * dtr);

        // aph=asin(stph0.*ctph.*ctlm+ctph0.*stph);
        double aph = Math.asin((stph0 * ctph * ctlm) + (ctph0 * stph));

        // cph=cos(aph);
        double cph = Math.cos(aph);

        // almd=tlm0d+asin(stlm.*ctph./cph)/dtr;
        all[0] = pole_lon + (Math.asin((stlm * ctph) / cph) / dtr);
        // aphd=aph/dtr;
        all[1] = aph / dtr;

        return all;
    }
    // *** public methods **************************************************************
    public int hashCode()
    {
        int result = 17;

        result = (37 * result) + grid_nx;
        result = (37 * result) + grid_ny;

        int intLat1 = Float.floatToIntBits((float) grid_lat1);

        result = (37 * result) + intLat1;

        int intLon1 = Float.floatToIntBits((float) grid_lon1);

        result = (37 * result) + intLon1;

        return result;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof GribRecordGDS)) {
            return false;
        }

        if (this == obj) {
            // Same object
            return true;
        }

        if (!(obj instanceof GribGDSRotatedLatLon))
            return false;

        GribGDSRotatedLatLon gds = (GribGDSRotatedLatLon) obj;

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

        if (grid_latspst != gds.grid_latspst) {
            return false;
        }
        
        if (grid_lonspst != gds.grid_lonspst) {
            return false;
        }
        
        if (grid_stretchfact != gds.grid_stretchfact) {
            return false;
        }
        
        return true;
    }

    /**
     * Get length in bytes of this section.
     *
     * @return length in bytes of this section
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Get type of grid.  This is type 0.
     *
     * @return type of grid
     */
    public int getGridType()
    {
        return grid_type;
    }

    /**
     * Get number of grid columns.
     *
     * @return number of grid columns
     */
    public int getGridNX()
    {
        return grid_nx;
    }

    /**
     * Get number of grid rows.
     *
     * @return number of grid rows.
     */
    public int getGridNY()
    {
        return grid_ny;
    }

    /**
     * Get latitude of grid start point.
     *
     * @return latitude of grid start point
     */
    public double getGridLat1()
    {
        return original_grid_lat1;
    }

    /**
     * Get longitude of grid start point.
     *
     * @return longitude of grid start point
     */
    public double getGridLon1()
    {
        return original_grid_lon1;
    }

    /**
     * Get grid mode. <i>Only 128 (increments given) supported so far.</i>
     *
     * @return grid mode
     */
    public int getGridMode()
    {
        return grid_mode;
    }

    /**
     * Get latitude of grid end point.
     *
     * @return latitude of grid end point
     */
    public double getGridLat2()
    {
        return original_grid_lat2;
    }

    /**
     * Get longitude of grid end point.
     *
     * @return longitude of grid end point
     */
    public double getGridLon2()
    {
        return original_grid_lon2;
    }

    /**
     * Get delta-Lon between two grid points.
     *
     * @return Lon increment
     */
    public double getGridDX()
    {
        return grid_dx;
    }

    /**
     * Get delta-Lat between two grid points.
     *
     * @return Lat increment
     */
    public double getGridDY()
    {
        return grid_dy;
    }

    /**
     * Get scan mode (sign of increments). <i>Only 64, 128 and 192 supported so far.</i>
     *
     * @return scan mode
     */
    public int getGridScanmode()
    {
        return grid_scan;
    }

    /**
     * Get longitide coordinates converted to the range +/- 180
     * @returns longtitude as double
     */
    //    public double[] getXCoords()
    //    {
    //        return getXCoords(true);
    //    }

    /**
     * Get longitide coordinates
     * @returns longtitude as double
     */
    //    public double[] getXCoords(boolean convertTo180)
    //    {
    //        double[] coords = new double[grid_nx];
    //
    //        int k = 0;
    //
    //        for (int x = 0; x < grid_nx; x++) {
    //            double longi = grid_lon1 + (x * grid_dx);
    //
    //            if (convertTo180) { // move x-coordinates to the range -180..180
    //
    //                if (longi >= 180.0) {
    //                    longi = longi - 360.0;
    //                }
    //
    //                if (longi < -180.0) {
    //                    longi = longi + 360.0;
    //                }
    //            } else { // handle wrapping at 360
    //
    //                if (longi >= 360.0) {
    //                    longi = longi - 360.0;
    //                }
    //            }
    //
    //            coords[k++] = longi;
    //        }
    //
    //        return coords;
    //    }

    /**
     * Get all latitude coordinates
     * @returns latitude as double
     */
    //    public double[] getYCoords()
    //    {
    //        double[] coords = new double[grid_ny];
    //
    //        int k = 0;
    //
    //        for (int y = 0; y < grid_ny; y++) {
    //            double lati = grid_lat1 + (y * grid_dy);
    //
    //            if ((lati > 90.0) || (lati < -90.0)) {
    //                System.err.println(
    //                    "GribGDSRotatedLatLon.getYCoords: latitude out of range (-90 to 90).");
    //            }
    //
    //            coords[k++] = lati;
    //        }
    //
    //        return coords;
    //    }

    /**
     * Get grid coordinates in longitude/latitude pairs
     * Longitude is returned in the range +/- 180 degrees
     * @returns longitide/latituide as doubles
     */
    public double[] getGridCoords() {
        double[] coords = new double[grid_ny * grid_nx * 2];

        int k = 0;

        for (int y = 0; y < grid_ny; y++) {
            for (int x = 0; x < grid_nx; x++) {
                double longi = original_grid_lon1 + (x * grid_dx);
                double lati = original_grid_lat1 + (y * grid_dy);

                // move x-coordinates to the range -180..180
                if (longi >= 180.0) {
                    longi = longi - 360.0;
                }

                if (longi < -180.0) {
                    longi = longi + 360.0;
                }

                if ((lati > 90.0) || (lati < -90.0)) {
                    System.err.println(
                        "GribGDSRotatedLatLon.getGridCoords: latitude out of range (-90 to 90).");
                }

                double[] real_lonLat = rtll(longi, lati);
                coords[k++] = real_lonLat[0];
                coords[k++] = real_lonLat[1];
            }
        }

        return coords;
    }

    /**
     * Get a string representation of this GDS.
     * @todo include more information about this projection
     * @return string representation of this GDS
     */
    public String toString() {
        String str = "    GDS section (" + this.length +"):\n      ";

        if (this.grid_type == 10) {
            str += "  RotatedLatLon Grid";
        }

        str += ("  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ");
        str += ("  lon: " + this.grid_lon1 + " to " + this.grid_lon2);
        str += ("  (dLon " + this.grid_dx + ")\n      ");
        str += ("  lat: " + this.grid_lat1 + " to " + this.grid_lat2);
        str += ("  (dLat " + this.grid_dy + ")\n      ");
        str += ("  rot_angle: " + this.grid_rotang + "\n      ");
        str += ("  str_fact: " + this.grid_stretchfact);

        return str;
    }

    /**
     * Get stretching factor.
     *
     * @return stretching factor.
     */
    public double getGridStretchingFactor()
    {
        return grid_stretchfact;
    }

    /**
     * Set stretching factor.
     *
     * @return stretching factor.
     */
    public void setGridStretchingFactor(double grid_strfact)
    {
        this.grid_stretchfact = grid_strfact;
    }

    /**
     * Get grid rotation angle of a rotated latitude/longitude grid.
     *
     * @return rotation angle
     */
    public double getGridRotAngle()
    {
        return this.grid_rotang;
    }

    /**
     * Get y-coordinate/latitude of south pole of stretching.
     *
     * @return latitude of south pole of stretching.
     */
    public double getGridLatSPST()
    {
        return this.grid_latspst;
    }

    /**
     * Get x-coordinate/longitude of south pole of of stretching.
     *
     * @return longitude of south pole of stretching.
     */
    public double getGridLonSPST()
    {
        return this.grid_lonspst;
    }

    /**
     * @param grid_latspst The grid_latspst to set.
     */
    public void setGridLatSPST(double grid_latspst) {
        this.grid_latspst = grid_latspst;
    }

    /**
     * @param grid_lonspst The grid_lonspst to set.
     */
    public void setGridLonSPST(double grid_lonspst) {
        this.grid_lonspst = grid_lonspst;
    }

    public void writeTo(OutputStream out) throws IOException
    {
        //length
        out.write(MathUtils.signedInt2Bytes(this.getLength(), 3));

        //NV
        out.write(new byte[] { 0 });

        //PV or PL
        out.write(new byte[] { 0 });

        //data representation type (octet 6) is 10
        out.write(new byte[] { 10 });

        //Ni
        out.write(new byte[] {
                (byte) (this.getGridNX() >> 8), (byte) this.getGridNX()
            });

        //Nj
        out.write(new byte[] {
                (byte) (this.getGridNY() >> 8), (byte) this.getGridNY()
            });

        //La1
        int lat1 = (int) Math.round(this.getGridLat1() * 1000);
        out.write(MathUtils.signedInt2Bytes(lat1, 3));

        //Lo1
        int lon1 = (int) Math.round(this.getGridLon1() * 1000);
        out.write(MathUtils.signedInt2Bytes(lon1, 3));

        //resolution flags
        out.write(new byte[] { (byte) this.getGridMode() });

        //La2
        int lat2 = (int) Math.round(this.getGridLat2() * 1000);
        out.write(MathUtils.signedInt2Bytes(lat2, 3));

        //Lo2
        int lon2 = (int) Math.round(this.getGridLon2() * 1000);
        out.write(MathUtils.signedInt2Bytes(lon2, 3));

        //Di
        int di = (int) (Math.abs(Math.round(this.getGridDX() * 1000)));
        out.write(MathUtils.signedInt2Bytes(di, 2));

        //Dj
        int dj = (int) (Math.abs(Math.round(this.getGridDY() * 1000)));
        out.write(MathUtils.signedInt2Bytes(dj, 2));

        //scanning mode
        out.write(new byte[] { (byte) this.getGridScanmode() });

        //reserved to 0
        out.write(new byte[] { 0, 0, 0, 0 });
        
        /*rotation*/
        // octets 33-35 (lat of s.pole)
        int latsp = (int) Math.round(this.getGridLatSP() * 1000);
        out.write(MathUtils.signedInt2Bytes(latsp, 3));

        // octets 36-38 (lon of s.pole)
        int lonsp = (int) Math.round(this.getGridLonSP() * 1000);
        out.write(MathUtils.signedInt2Bytes(lonsp, 3));

        // octets 39-42 (angle of rotation)
        int rotang = (int) Math.round(this.getGridRotAngle() * 1000);
        out.write(MathUtils.signedInt2Bytes(rotang, 4));

        /*stretched*/
        // octets 43-45 (lat of s.pole str.)
        int latspst = (int) Math.round(this.getGridLatSPST() * 1000);
        out.write(MathUtils.signedInt2Bytes(latspst, 3));

        // octets 46-48 (lon of s.pole str.)
        int lonspst = (int) Math.round(this.getGridLonSPST() * 1000); 
        out.write(MathUtils.signedInt2Bytes(lonspst, 3));

        // octets 49-52 (stretch factor)
        int stretchfact = (int) Math.round(this.getGridStretchingFactor() * 1000);
        out.write(MathUtils.signedInt2Bytes(stretchfact, 4));

        // write the rest
        byte unused[] = new byte[this.getLength() - 52];
        if (unused.length > 0)
            out.write(unused);
    }
    public String getName() {
        return "Grid GDS Rotated Lat-Lon (type 10)";
    }

    public Set<Integer> getSupportedTypes() {
        return Collections.singleton(10);
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canProduce(int GDSType) {
        return GDSType == 10;
    }

    public GribRecordGDS createGridGDS(int GDSType) throws IllegalArgumentException {
        return new GribGDSRotatedLatLon();
    }

    public GribRecordGDS createGridGDS(ImageInputStream in, int[] data) throws IOException {
        return new GribGDSRotatedLatLon(in, data);
    }

    public Map getImplementationHints() {
        return Collections.emptyMap();
    }
}