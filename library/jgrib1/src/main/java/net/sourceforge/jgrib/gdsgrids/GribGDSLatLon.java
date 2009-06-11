/*
 * GribGDSLatLon.java  1.0  10/15/2002
 *
 * @author Capt Richard D. Gonzalez
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
 * with a Lat/Lon grid projection.
 *
 * @author Richard Gonzalez based heavily on the original GribRecordGDS
 * @author simone giannecchini
 * @version 1.1
 */
public final class GribGDSLatLon extends GribRecordGDS {

	public final static int LATLON_GRID_TYPE=0;
    /**
     * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream. See
     * Table D of NCEP Office Note 388 for details
     *
     * @param in bit input stream with GDS content
     * @param header DOCUMENT ME!
     *
     * @throws IOException if stream can not be opened etc.
     * @throws NoValidGribException if stream contains no valid GRIB file
     * @throws NotSupportedException DOCUMENT ME!
     */
    public GribGDSLatLon(final ImageInputStream in,final  int[] header)
        throws IOException {
        super(header);

        if (this.grid_type != 0) {
            throw new IllegalArgumentException("GribGDSLatLon: grid_type is not "
                + "Latitude/Longitude (read grid type " + grid_type
                + ", needed 0)");
        }

        final byte[] data = new byte[this.length - header.length];
        in.read(data);

        // octets 7-8 (number of points along a parallel)
        this.grid_nx = MathUtils.uint2(0xff & data[0], 0xff & data[1]);

        // octets 9-10 (number of points along a meridian)
        this.grid_ny = MathUtils.uint2(0xff & data[2], 0xff & data[3]);

        // octets 11-13 (latitude of first grid point)
        this.grid_lat1 = MathUtils.int3(0xff & data[4], 0xff & data[5],
                0xff & data[6]) / 1000.0;

        // octets 14-16 (longitude of first grid point)
        this.grid_lon1 = MathUtils.int3(0xff & data[7], 0xff & data[8],
                0xff & data[9]) / 1000.0;

        // octet 17 (resolution and component flags -> 128 == increments given.)
        this.grid_mode = 0xff & data[10];

        if ((this.grid_mode != 128) && (this.grid_mode != 0)) {
            throw new UnsupportedOperationException(
                "GribGDSLatLon: No other component flag than 128 "
                + "(increments given) or 0 (not given) supported. "
                + "Current is: " + this.grid_mode);
        }

        // octets 18-20 (latitude of last grid point)
        this.grid_lat2 = MathUtils.int3(0xff & data[11], 0xff & data[12],
                0xff & data[13]) / 1000.0;

        // octets 21-23 (longitude of last grid point)
        this.grid_lon2 = MathUtils.int3(0xff & data[14], 0xff & data[15],
                0xff & data[16]) / 1000.0;

        // increments given
        if (this.grid_mode == 128) {
            // octets 24-25 (x increment)
            this.grid_dx = MathUtils.uint2(0xff & data[17], 0xff & data[18]) / 1000.0;

            // octets 26-27 (y increment)
            this.grid_dy = MathUtils.uint2(0xff & data[19], 0xff & data[20]) / 1000.0;

            //         if ((this.grid_scan & 64) != 0) this.grid_dy = -this.grid_dy;
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
    }

    /**
     * GribGDSLatLon
     */
    public GribGDSLatLon() {
    }

    // *** public methods **************************************************************
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

        if (!(obj instanceof GribRecordGDS))
            return false;

        GribRecordGDS gds = (GribRecordGDS) obj;

        if (grid_type != gds.getGridType()) {
            return false;
        }

        if (grid_mode != gds.getGridMode()) {
            return false;
        }

        if (grid_scan != gds.getGridScanmode()) {
            return false;
        }

        if (grid_nx != gds.getGridNX()) {
            return false;
        }

        if (grid_ny != gds.getGridNY()) {
            return false;
        }

        if (grid_dx != gds.getGridDX()) {
            return false;
        }

        if (grid_dy != gds.getGridDY()) {
            return false;
        }

        if (grid_lat1 != gds.getGridLat1()) {
            return false;
        }

        if (grid_lat2 != gds.getGridLat2()) {
            return false;
        }

        if (grid_latsp != gds.getGridLatSP()) {
            return false;
        }

        if (grid_lon1 != gds.getGridLon1()) {
            return false;
        }

        if (grid_lon2 != gds.getGridLon2()) {
            return false;
        }

        if (grid_lonsp != gds.getGridLonSP()) {
            return false;
        }

        if (grid_rotang != gds.getGridRotAngle()) {
            return false;
        }

        if (isAdiacent_i_Or_j() != gds.isAdiacent_i_Or_j()) {
            return false;
        }

        return true;
    }

    /**
     * Get longitude coordinates converted to the range +/- 180
     *
     * @return longtitude as double
     */
    public double[] getXCoords() {
        return getXCoords(true);
    }

    /**
     * Get longitide coordinates
     *
     * @param convertTo180 DOCUMENT ME!
     *
     * @return longtitude as double
     */
    public double[] getXCoords(boolean convertTo180) {
        double[] coords = new double[grid_nx];

        int k = 0;

        for (int x = 0; x < grid_nx; x++) {
            double longi = grid_lon1 + (x * grid_dx);

            if (convertTo180) { // move x-coordinates to the range -180..180

                if (longi >= 180.0) {
                    longi = longi - 360.0;
                }

                if (longi < -180.0) {
                    longi = longi + 360.0;
                }
            } else { // handle wrapping at 360

                if (longi >= 360.0) {
                    longi = longi - 360.0;
                }
            }

            coords[k++] = longi;
        }

        return coords;
    }

    /**
     * Get all latitude coordinates
     *
     * @return latitude as double
     */
    public double[] getYCoords() {
        double[] coords = new double[grid_ny];

        int k = 0;

        for (int y = 0; y < grid_ny; y++) {
            double lati = grid_lat1 + (y * grid_dy);

            if ((lati > 90.0) || (lati < -90.0)) {
                System.err.println(
                    "GribGDSLatLon.getYCoords: latitude out of range (-90 to 90).");
            }

            coords[k++] = lati;
        }

        return coords;
    }

    /**
     * Get grid coordinates in longitude/latitude pairs Longitude is returned
     * in the range +/- 180 degrees
     *
     * @return longitide/latituide as doubles
     */
    public double[] getGridCoords() {
        double[] coords = new double[grid_ny * grid_nx * 2];

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
                        "GribGDSLatLon.getGridCoords: latitude out of range (-90 to 90).");
                }

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
     *
     * @todo include more information about this projection
     */
    public String toString() {
        String str = "    GDS section:\n      ";

        if (this.grid_type == 0) {
            str += "  LatLon Grid";
        }

        str += ("  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ");
        str += ("  lon: " + this.grid_lon1 + " to " + this.grid_lon2);
        str += ("  (dLon " + this.grid_dx + ")\n      ");
        str += ("  lat: " + this.grid_lat1 + " to " + this.grid_lat2);
        str += ("  (dLat " + this.grid_dy + ")");

        return str;
    }

    /**
     * writeTo writes this section to an output stream
     *
     * @param out String
     *
     * @throws IOException DOCUMENT ME!
     */
    public void writeTo(OutputStream out) throws IOException {
        //length is always 32
        out.write(new byte[] { 0, 0, 32 });

        //NV
        out.write(new byte[] { 0 });

        //PV or PL
        out.write(new byte[] { 0 });

        //data representation type (octet 6) is 0
        out.write(new byte[] { 0 });

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
    }

    public String getName() {
        return "Grid GDS Lat-Lon (type 0)";
    }

    public Set<Integer> getSupportedTypes() {
        return Collections.singleton(0);
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canProduce(int GDSType) {
        return GDSType == LATLON_GRID_TYPE;
    }

    public Map getImplementationHints() {
        return Collections.emptyMap();
    }
}