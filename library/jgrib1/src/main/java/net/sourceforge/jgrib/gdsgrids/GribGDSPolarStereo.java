/**
 * GribGDSPolarStereo.java  1.0  01/01/2001 based on GribRecordGDS (C) Benjamin
 * Stark Heavily modified by Capt Richard D. Gonzalez to conform to
 * GribGDSFactory implementation - 4 Sep 02 Implements GDS Table D for Polar
 * Stereo grid
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
 * A class that represents the grid definition section (GDS) of a GRIB record.
 * 
 * @author Benjamin Stark
 * @author Capt Richard D. Gonzalez
 * @version 2.0 Modified 4 Sep 02 to be constructed by GribGDSFactory - Richard
 *          D. Gonzalez
 */
public final class GribGDSPolarStereo extends GribRecordGDS {
	
	public final static int POLAR_STEREO_GRID_TYPE=5;
	/* start of attributes unique to the Polar Stereo GDS */

	/** Projection Center Flag. */
	protected int grid_proj_center;

	/**
	 * starting x value using this projection. This is not a Longitude, but an x
	 * value based on the projection
	 */
	protected double grid_startx;

	/**
	 * starting y value using this projection. This is not a Latitude, but a y
	 * value based on the projection
	 */
	protected double grid_starty;

	/** Central Scale Factor. Assumed 1.0 */
	protected final double SCALE_FACTOR = 1.0;

	/** Longitude of Center - assumed 60 N or 60 S based on note 2 of table D */
	protected double grid_center_lat = 60.0;

	// *** constructors *******************************************************

	/**
	 * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
	 * 
	 * @param in
	 *            bit input stream with GDS content
	 * @param header -
	 *            int array with first six octets of the GDS
	 * 
	 * @throws IOException
	 *             if stream can not be opened etc.
	 * @throws NoValidGribException
	 *             if stream contains no valid GRIB file
	 * @throws NotSupportedException
	 *             DOCUMENT ME!
	 */
	public GribGDSPolarStereo(ImageInputStream in, int[] header)
			throws IOException {
		super(header);

		if (this.grid_type != 5) {
			throw new IllegalArgumentException(
					"GribGDSPolarStereo: grid_type is not "
							+ "Polar Stereo (read grid type " + grid_type
							+ " needed 5)");
		}

		// read in the Grid Description (see Table D) of the GDS
		final int temp = this.length - header.length;
		final int[] data = new int[temp];

		for (int i = 0; i < temp; i++)
			data[i] = in.read();

		// octets 7-8 (Nx - number of points along x-axis)
		this.grid_nx = MathUtils.uint2(data[0], data[1]);

		// octets 9-10 (Ny - number of points along y-axis)
		this.grid_ny = MathUtils.uint2(data[2], data[3]);

		// octets 11-13 (La1 - latitude of first grid point)
		this.grid_lat1 = MathUtils.int3(data[4], data[5], data[6]) / 1000.0;

		// octets 14-16 (Lo1 - longitude of first grid point)
		this.grid_lon1 = MathUtils.int3(data[7], data[8], data[9]) / 1000.0;

		// octet 17 (resolution and component flags). See Table 7
		this.grid_mode = data[10];

		// octets 18-20 (Lov - Orientation of the grid - east lon parallel to y
		// axis)
		this.grid_lon2 = MathUtils.int3(data[11], data[12], data[13]) / 1000.0;

		// octets 21-23 (Dx - the X-direction grid length) See Note 2 of Table D
		this.grid_dx = MathUtils.int3(data[14], data[15], data[16]);

		// octets 24-26 (Dy - the Y-direction grid length) See Note 2 of Table D
		this.grid_dy = MathUtils.uint3(data[17], data[18], data[19]);

		// octets 27 (Projection Center flag) See Note 5 of Table D
		this.grid_proj_center = data[20];

		if ((grid_proj_center & 128) == 128) { // if bit 1 set to 1, SP is on
												// proj plane
			grid_center_lat = -60.0;
		}

		// octet 28 (Scanning mode) See Table 8
		this.grid_scan = data[21];

		if ((this.grid_scan & 63) != 0) {
			throw new UnsupportedOperationException(
					"GribRecordGDS: This scanning mode (" + this.grid_scan
							+ ") is not supported.");
		}

		// rdg = table 8 shows -i if bit set
		if ((this.grid_scan & 128) != 0) {
			this.grid_dx = -this.grid_dx;
		}

		// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
		if ((this.grid_scan & 64) != 64) {
			this.grid_dy = -this.grid_dy;
		}

		// if ((this.grid_scan & 64) != 0) this.grid_dy = -this.grid_dy;
		// octets 29-32 are reserved
		prepProjection();
	}

	/**
	 * GribGDSPolarStereo
	 */
	public GribGDSPolarStereo() {
	}

	// *** public methods
	// **************************************************************
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
		if (!(obj instanceof GribGDSPolarStereo)) {
			return false;
		}

		if (this == obj) { // Same object

			return true;
		}

        if (!(obj instanceof GribGDSPolarStereo))
            return false;

		GribGDSPolarStereo gds = (GribGDSPolarStereo) obj;

		if (grid_nx != gds.grid_nx) {
			return false;
		}

		if (grid_ny != gds.grid_ny) {
			return false;
		}

		if (grid_lat1 != gds.grid_lat1) {
			return false;
		}

		if (grid_lon1 != gds.grid_lon1) {
			return false;
		}

		if (grid_mode != gds.grid_mode) {
			return false;
		}

		if (grid_lat2 != gds.grid_lat2) {
			return false;
		}

		if (grid_dx != gds.grid_dx) {
			return false;
		}

		if (grid_dy != gds.grid_dy) {
			return false;
		}

		if (grid_type != gds.grid_type) {
			return false;
		}

		if (grid_proj_center != gds.grid_proj_center) {
			return false;
		}

		if (grid_scan != gds.grid_scan) {
			return false;
		}

		return true;
	}

	/**
	 * Get projection center flag.
	 * 
	 * @return projection center flag
	 */
	public int getProjCenterFlag() {
		return this.grid_proj_center;
	}

	/**
	 * Get the Latitude of the circle where grid lengths are defined
	 * 
	 * @return grid_center_lat
	 */
	public double getGridCenterLat() {
		return this.grid_center_lat;
	}

	/**
	 * Get all longitide coordinates
	 * 
	 * @return longtitude as double
	 */
	public double[] getXCoords() {
		double[] xCoords = new double[grid_nx];

		double startx = grid_startx / 1000.0;
		double dx = grid_dx / 1000.0;

		for (int i = 0; i < grid_nx; i++) {
			double x = startx + (i * dx);

			xCoords[i] = x;
		}

		return xCoords;
	}

	/**
	 * Get all latitude coordinates
	 * 
	 * @return latitude as double
	 */
	public double[] getYCoords() {
		double[] yCoords = new double[grid_ny];

		double starty = grid_starty / 1000.0;
		double dy = grid_dy / 1000.0;

		for (int j = 0; j < grid_ny; j++) {
			double y = starty + (j * dy);

			yCoords[j] = y;
		}

		return yCoords;
	}

	/**
	 * Prep the projection and determine the starting x and y values based on
	 * Lat1 and Lon1 relative to the origin for this grid. adapted from J.P.
	 * Snyder, Map Projections - A Working Manual, U.S. Geological Survey
	 * Professional Paper 1395, 1987 Maintained his symbols, so the code matches
	 * his work. Somewhat hard to follow, if interested, suggest looking up
	 * quick reference at
	 * http://mathworld.wolfram.com/LambertConformalConicProjection.html Origin
	 * is where Lov intersects 60 degrees (from note 2 of Table D) north or
	 * south (determined by bit 1 of the Projection Center Flag). This assumes a
	 * central scale factor of 1.
	 */
	private void prepProjection() {
		double k;
//		double pi2;
//		double pi4;
		double cosLat1;
		double sinLat1;
		double cos60;
		double sin60;
		double dLonr;

		cosLat1 = Math.cos(Math.toRadians(grid_lat1));
		sinLat1 = Math.sin(Math.toRadians(grid_lat1));
		cos60 = Math.cos(Math.toRadians(grid_center_lat));
		sin60 = Math.sin(Math.toRadians(grid_center_lat));
		dLonr = Math.toRadians(grid_lon1 - grid_lon2); // lon2 is lov

		k = (2.0 * SCALE_FACTOR)
				/ (1 + (sin60 * sinLat1) + (cos60 * cosLat1 * Math.cos(dLonr)));
		grid_startx = EARTH_RADIUS * k * cosLat1 * Math.sin(dLonr);
		grid_starty = EARTH_RADIUS * k
				* ((cos60 * sinLat1) - (sin60 * cosLat1 * Math.cos(dLonr)));
	}

	/**
	 * Get grid coordinates in longitude/latitude adapted from J.P. Snyder, Map
	 * Projections - A Working Manual, U.S. Geological Survey Professional Paper
	 * 1395, 1987 Maintained his symbols, so the code matches his work. Somewhat
	 * hard to follow, if interested, suggest looking up quick reference at
	 * http://mathworld.wolfram.com/PolarStereoConicProjection.html assumes
	 * scale factor of 1.0 rdg - may not be correct yet - did not align with
	 * display software I was using, but they implemented using a center point,
	 * vice LOV
	 * 
	 * @return longitide/latitude as doubles
	 * 
	 * @todo verify projection implementation
	 */
	public double[] getGridCoords() {
		int count = 0;
		double rho;
		double c;
		double cosC;
		double sinC;
		double cos60;
		double sin60;
		double lon;
		double lat;
		double x;
		double y;
		double[] coords = new double[grid_nx * grid_ny * 2];

		cos60 = Math.cos(Math.toRadians(grid_center_lat));
		sin60 = Math.sin(Math.toRadians(grid_center_lat));

		for (int j = 0; j < grid_ny; j++) {
			y = grid_starty + (grid_dy * j);

			for (int i = 0; i < grid_nx; i++) {
				x = grid_startx + (grid_dx * i);

				rho = Math.sqrt((x * x) + (y * y));
				c = 2.0 * Math.atan(rho / (2.0 * EARTH_RADIUS * SCALE_FACTOR));
				cosC = Math.cos(Math.toRadians(c));
				sinC = Math.sin(Math.toRadians(c));

				lon = Math.asin((cosC * sin60) + ((y * sinC * cos60) / rho));
				lat = grid_lon2
						+ Math.atan((x * sinC)
								/ ((rho * cos60 * cosC) - (y * cos60 * sinC)));

				// move x-coordinates to the range -180..180
				if (lon >= 180.0) {
					lon = lon - 360.0;
				}

				if (lon < -180.0) {
					lon = lon + 360.0;
				}

				if ((lat > 90.0) || (lat < -90.0)) {
					System.err
							.println("GribGDSPolarStereo: latitude out of range (-90 to 90).");
				}

				coords[count++] = lon;
				coords[count++] = lat;
			}
		}

		return coords;
	}

	/**
	 * Get a string representation of this GDS.
	 * 
	 * @return string representation of this GDS
	 * 
	 * @todo - ensure this returns PS specific info - probably still a copy of
	 *       LC
	 */
	public String toString() {
		String str = "    GDS section:\n      ";

		str += "  Polar Stereo Grid";

		str += ("  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ");
		str += ("  1st point:  Lat: " + this.grid_lat1);
		str += ("  Lon: " + this.grid_lon1 + "\n      ");
		str += ("  Grid length: X-Direction  " + this.grid_dx + "m; ");
		str += (" Y-Direction: " + this.grid_dy + "m\n      ");
		str += "  Orientation - East longitude parallel to y-axis: ";
		str += (this.grid_lat2 + "\n      ");
		str += "  Resolution and Component Flags: \n      ";

		if ((this.grid_mode & 128) == 128) {
			str += "       Direction increments given \n      ";
		} else {
			str += "       Direction increments not given \n      ";
		}

		if ((this.grid_mode & 64) == 64) {
			str += ("       Earth assumed oblate spheroid 6378.16 km at equator, "
					+ " 6356.775 km at pole, f=1/297.0\n      ");
		} else {
			str += "       Earth assumed spherical with radius = 6367.47 km \n      ";
		}

		if ((this.grid_mode & 8) == 8) {
			str += "       u and v components are relative to the grid \n      ";
		} else {
			str += ("       u and v components are relative to easterly and "
					+ "northerly directions \n      ");
		}

		str += "  Scanning mode:  \n      ";

		if ((this.grid_scan & 128) == 128) {
			str += "       Points scan in the -i direction \n      ";
		} else {
			str += "       Points scan in the +i direction \n      ";
		}

		if ((this.grid_scan & 64) == 64) {
			str += "       Points scan in the +j direction \n      ";
		} else {
			str += "       Points scan in the -j direction \n      ";
		}

		if ((this.grid_scan & 32) == 32) {
			str += "       Adjacent points in j direction are consecutive \n      ";
		} else {
			str += "       Adjacent points in i direction are consecutive";
		}

		return str;
	}

	public void writeTo(OutputStream out) throws IOException {
	}

    public String getName() {
        return "Grid GDS Polar Stereographic (type 5)";
    }

    public Set<Integer> getSupportedTypes() {
        return Collections.singleton(5);
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canProduce(int GDSType) {
        return GDSType == 5;
    }

    public Map getImplementationHints() {
        return null;
    }
}