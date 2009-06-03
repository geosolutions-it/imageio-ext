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
/**
 * GribGDSLambert.java  1.0  10/01/2002 based on GribRecordGDS (C) Benjamin
 * Stark Heavily modified by Richard D. Gonzalez to conform to GribGDSFactory
 * implementation - 4 Sep 02 Implements GDS Table D for Lambert grid
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
 * A class that represents the Grid Definition Section (GDS) of a GRIB record
 * using the Lambert Conformal projection.
 * 
 * @author Capt Richard D. Gonzalez
 * @author Alesio Fabiani
 * @author Simone Giannecchini
 * @version 2.0 Modified 4 Sep 02 to be constructed by GribGDSFactory
 */
public final class GribGDSLambert extends GribRecordGDS {
	
	
	public final static int LAMBERT_GRID_TYPE=3;
	/* start of attributes unique to the Lambert GDS */

	/** Projection Center Flag. */
	protected int proj_center;

	/**
	 * Latin 1 - The first latitude from pole at which secant cone cuts the
	 * sperical earth. See Note 8 of ON388.
	 */
	protected double grid_latin1;

	/**
	 * Latin 2 - The second latitude from pole at which secant cone cuts the
	 * sperical earth. See Note 8 of ON388.
	 */
	protected double grid_latin2;

	/** latitude of south pole. */
	protected double grid_latsp;

	/** longitude of south pole. */
	protected double grid_lonsp;

	/**
	 * starting x value using this projection. This is NOT a lat or lon, but a
	 * grid position in this projection
	 */
	protected double grid_startx;

	/**
	 * starting y value using this projection. This is NOT a lat or lon, but a
	 * grid position in this projection
	 */
	protected double grid_starty;

	/** Variables used in calculating the projection - see prepProjection */
	double f;

	/** Variables used in calculating the projection - see prepProjection */
	double rhoRef;

	/** Variables used in calculating the projection - see prepProjection */
	double n;

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
	 */
	public GribGDSLambert(ImageInputStream in, int[] header)
			throws IOException {
		super(header);

		if (this.grid_type != 3) {
			throw new IllegalArgumentException("GribGDSLambert: grid_type is not "
					+ "Lambert Conformal (read grid type " + grid_type
					+ " needed 3)");
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
		this.proj_center = data[20];

		// octet 28 (Scanning mode) See Table 8
		this.grid_scan = data[21];

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

		// if ((this.grid_scan & 64) != 0) this.grid_dy = -this.grid_dy;
		// octets 29-31 (Latin1 - first lat where secant cone cuts spherical
		// earth)
		this.grid_latin1 = MathUtils.int3(data[22], data[23], data[24]) / 1000.0;

		// octets 32-34 (Latin2 - second lat where secant cone cuts spherical
		// earth)
		this.grid_latin2 = MathUtils.int3(data[25], data[26], data[27]) / 1000.0;

		// octets 35-37 (lat of southern pole)
		this.grid_latsp = MathUtils.int3(data[28], data[29], data[30]) / 1000.0;

		// octets 36-38 (lon of southern pole)
		this.grid_lonsp = MathUtils.int3(data[31], data[32], data[33]) / 1000.0;

		// calculate what you can about the projection from what we have
		prepProjection();
	}

	/**
	 * GribGDSLambert
	 */
	public GribGDSLambert() {
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
		if (!(obj instanceof GribGDSLambert)) {
			return false;
		}

		if (this == obj) {
			// Same object
			return true;
		}

        if (!(obj instanceof GribGDSLambert))
            return false;
        
		GribGDSLambert gds = (GribGDSLambert) obj;

		if (grid_type != gds.grid_type) {
			return false;
		}

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

		if (proj_center != gds.proj_center) {
			if (grid_scan != gds.grid_scan) {
				return false;
			}
		}

		if (grid_latin1 != gds.grid_latin1) {
			return false;
		}

		if (grid_latin2 != gds.grid_latin2) {
			return false;
		}

		if (grid_latsp != gds.grid_latsp) {
			return false;
		}

		if (grid_lonsp != gds.grid_lonsp) {
			return false;
		}

		return true;
	}

	/**
	 * Get orientation of the grid
	 * 
	 * @return east longitude value of meridian parallel to y axis.
	 */
	public double getGridLov() {
		return this.grid_lon2;
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
	 * Get Projection Center flag - see note 5 of Table D.
	 * 
	 * @return Projection Center flag
	 */
	public int getProjCenter() {
		return this.proj_center;
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
	 * Get first latitude from the pole at which cone cuts spherical earth - see
	 * note 8 of Table D
	 * 
	 * @return latitude of south pole
	 */
	public double getGridLatin1() {
		return this.grid_latin1;
	}

	/**
	 * Get second latitude from the pole at which cone cuts spherical earth -
	 * see note 8 of Table D
	 * 
	 * @return latitude of south pole
	 */
	public double getGridLatin2() {
		return this.grid_latin2;
	}

	/**
	 * Get latitude of south pole.
	 * 
	 * @return latitude of south pole
	 */
	public double getGridLatSP() {
		return this.grid_latsp;
	}

	/**
	 * Get longitude of south pole of a rotated latitude/longitude grid.
	 * 
	 * @return longitude of south pole
	 */
	public double getGridLonSP() {
		return this.grid_lonsp;
	}

	/**
	 * Get starting x value for this grid - THIS IS NOT A LONGITUDE, but an x
	 * value calculated for this specific projection, based on an origin of
	 * latin1, lov.
	 * 
	 * @return x grid value of first point of this grid.
	 */
	public double getStartX() {
		return this.grid_startx;
	}

	/**
	 * Get starting y value for this grid - THIS IS NOT A LATITUDE, but an y
	 * value calculated for this specific projection, based on an origin of
	 * latin1, lov.
	 * 
	 * @return y grid value of first point of this grid.
	 */
	public double getStartY() {
		return this.grid_starty;
	}

	public void setGridLatin1(double grid_latin1) {
		this.grid_latin1 = grid_latin1;
	}

	public void setGridLatin2(double grid_latin2) {
		this.grid_latin2 = grid_latin2;
	}

	public void setProjCenter(int proj_center) {
		this.proj_center = proj_center;
	}

	public double getRhoRef() {
		return rhoRef;
	}

	public void setRhoRef(double rhoRef) {
		this.rhoRef = rhoRef;
	}

	/**
	 * Get all longitide coordinates
	 * 
	 * @return longtitude as double
	 */

	// doesn't work yet - need to create a projToLL method and convert each
	// point
	/*
	 * public double[] getXLons() { // alloc double[] coords = new
	 * double[grid_nx]; int k = 0; for (int x = 0; x < grid_nx; x++) { double
	 * longi = grid_lon1 + x * grid_dx; // move x-coordinates to the range
	 * -180..180 if (longi >= 180.0) longi = longi - 360.0; if (longi < -180.0)
	 * longi = longi + 360.0; coords[k++] = longi; } return coords; }
	 */

	/**
	 * Get all x Axis grid coordinates
	 * 
	 * @return array of coordinates in the LC projection for the x axis. These
	 *         are the LC coordinates that equate to the Longitudes along the x
	 *         axis. rdg - modified to return in km, vice m
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
	 * Get y Axis grid coordinates
	 * 
	 * @return latitude as double
	 */

	// doesn't work yet - need to create a projToLL method and convert each
	// point
	/*
	 * public double[] getYLats() { // alloc double[] coords = new
	 * double[grid_ny]; int k = 0; for (int y = 0; y < grid_ny; y++) { double
	 * lati = grid_lat1 + y * grid_dy; // if (lati > 90.0 || lati < -90.0) //
	 * System.err.println("GribGDSLambert: latitude out of range (-90 to 90)."); //
	 * coords[k++] = lati; } return coords; }
	 */

	/**
	 * Get all y Axis grid coordinates
	 * 
	 * @return array of coordinates in the LC projection for the y axis. These
	 *         are the LC coordinates that equate to the Latitudes along the y
	 *         axis. rdg - converted to return km vice m
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
	 * the Lat1 and Lon1 for this grid adapted from J.P. Snyder, Map Projections -
	 * A Working Manual, U.S. Geological Survey Professional Paper 1395, 1987
	 * Maintained his symbols, so the code matches his work. Somewhat hard to
	 * follow, if interested, suggest looking up quick reference at
	 * http://mathworld.wolfram.com/LambertConformalConicProjection.html Origin
	 * is where Lov intersects Latin1. Note: In GRIB table D, the first standard
	 * parallel (Latin1) is the one closest to the pole. In the mathematical
	 * formulas, the first standard parallel is the one closest to the equator.
	 * Therefore, the math looks backwards here, but it isn't.
	 */
	private void prepProjection() {
		double rho;
		double theta;
//		double pi2;
		double pi4;
		double latin1r;
		double latin2r;

//		pi2 = Math.PI / 2;
		pi4 = Math.PI / 4;
		latin1r = Math.toRadians(grid_latin1);
		latin2r = Math.toRadians(grid_latin2);

		// compute the common parameters
        if (latin1r != latin2r) {
		n = Math.log(Math.cos(latin1r) / Math.cos(latin2r))
				/ Math.log(Math.tan(pi4 + (latin2r / 2))
						/ Math.tan(pi4 + (latin1r / 2)));
        } else {
            n = Math.sin(latin1r);
        }
		f = (Math.cos(latin1r) * Math.pow(Math.tan(pi4 + (latin1r / 2)), n))
				/ n;
		rho = EARTH_RADIUS * f
				* Math.pow(Math.tan(pi4 + (Math.toRadians(grid_lat1) / 2)), -n);
		rhoRef = EARTH_RADIUS
				* f
				* Math.pow(Math.tan(pi4 + (Math.toRadians(grid_latin1) / 2)),
						-n);

		// compute the starting x and starting y coordinates for this projection
		// the grid_lon2 here is the lov - the reference longitude
		theta = n * Math.toRadians(grid_lon1 - grid_lon2);
		grid_startx = rho * Math.sin(theta);
		grid_starty = rhoRef - (rho * Math.cos(theta));
	}

	/**
	 * Get grid coordinates in latitude/longitude adapted from J.P. Snyder, Map
	 * Projections - A Working Manual, U.S. Geological Survey Professional Paper
	 * 1395, 1987 Maintained his symbols, so the code matches his work. Somewhat
	 * hard to follow, if interested, suggest looking up quick reference at
	 * http://mathworld.wolfram.com/LambertConformalConicProjection.html Origin
	 * is where Lov intersects Latin1. Note: In GRIB table D, the first standard
	 * parallel (Latin1) is the one closest to the pole. In the mathematical
	 * formulas, the first standard parallel is the one closest to the equator.
	 * Therefore, the math looks backwards here, but it isn't.
	 * 
	 * @return latitide/longitude as doubles
	 */
	public double[] getGridCoords() {
		double rho;
		double theta;
		double pi2;
//		double pi4;
//		double latin1r;
//		double latin2r;
		double lat;
		double lon;
		double x;
		double y;

		double[] coords;

		pi2 = Math.PI / 2;
//		pi4 = Math.PI / 4;
//		latin1r = Math.toRadians(grid_latin1);
//		latin2r = Math.toRadians(grid_latin2);

		// need space for a lat and lon for each grid point
		coords = new double[grid_ny * grid_nx * 2];

		// compute the lat and lon for each grid point
		// note - grid points are NOT the indices of the arrays, they are
		// computed
		// from the projection
		int k = 0;

		for (int j = 0; j < grid_ny; j++) {
			y = grid_starty + (grid_dy * j);

			for (int i = 0; i < grid_nx; i++) {
				x = grid_startx + (grid_dx * i);

				theta = Math.atan(x / (rhoRef - y));
				rho = Math.sqrt(Math.pow(x, 2.0) + Math.pow((rhoRef - y), 2.0));

				if (n < 0) {
					rho = -rho;
				}

				lon = grid_lon2 + Math.toDegrees(theta / n);
				lat = Math.toDegrees((2.0 * Math.atan(Math.pow(
						(EARTH_RADIUS * f) / rho, 1 / n)))
						- pi2);

				// move x-coordinates to the range -180..180
				if (lon >= 180.0) {
					lon = lon - 360.0;
				}

				if (lon < -180.0) {
					lon = lon + 360.0;
				}

				if ((lat > 90.0) || (lat < -90.0)) {
					System.err
							.println("GribGDSLambert: latitude out of range (-90 to 90).");
				}

				// coords[grid_nx * y + x] =
				coords[k++] = lon;
				coords[k++] = lat;
			}
		}

		return coords;
	}

    public double[] getGridLonLatEnvelope() {
        double rho;
        double theta;
        double pi2;
        double lat;
        double lon;
        double x;
        double y;

        double[] indexes;
        double[] coords;

        pi2 = Math.PI / 2;

        // need space for a lat and lon for each grid point
        indexes = new double[4];
        coords = new double[4];

        if (grid_dx > 0) {
            indexes[0] = grid_startx;
            indexes[2] = grid_startx + (grid_nx * grid_dx);
        } else {
            indexes[0] = grid_startx + (grid_nx * grid_dx);
            indexes[2] = grid_startx;
        }

        if (grid_dy > 0) {
            indexes[1] = grid_starty;
            indexes[3] = grid_starty + (grid_ny * grid_dy);
        } else {
            indexes[1] = grid_starty + (grid_ny * grid_dy);
            indexes[3] = grid_starty;
        }

        int k = 0;
        
        for (int i = 0; i < 2; i++) {
            y = indexes[(i*2)+1];
            x = indexes[i*2];
            
            theta = Math.atan(x / (rhoRef - y));
            rho = Math.sqrt(Math.pow(x, 2.0) + Math.pow((rhoRef - y), 2.0));
            
            if (n < 0) {
                rho = -rho;
            }
            
            lon = grid_lon2 + Math.toDegrees(theta / n);
            lat = Math.toDegrees((2.0 * Math.atan(Math.pow(
                    (EARTH_RADIUS * f) / rho, 1 / n)))
                    - pi2);
            
            // move x-coordinates to the range -180..180
            if (lon >= 180.0) {
                lon = lon - 360.0;
            }
            
            if (lon < -180.0) {
                lon = lon + 360.0;
            }
            
            if ((lat > 90.0) || (lat < -90.0)) {
                System.err
                .println("GribGDSLambert: latitude out of range (-90 to 90).");
            }
            
            // coords[grid_nx * y + x] =
            coords[k++] = lon;
            coords[k++] = lat;
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

		str += "  Lambert Conformal Grid";

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
			str += "       Adjacent points in i direction are consecutive \n      ";
		}

		str += " The first latitude from pole at which the secant cone";
		str += (" cuts the spherical earth: " + this.grid_latin1 + "\n      ");
		str += " The second latitude from pole at which the secant cone";
		str += (" cuts the spherical earth: " + this.grid_latin2 + "\n      ");
		str += (" Latitude of the southern pole: " + this.grid_latsp + "\n      ");
		str += (" Longitude of the southern pole: " + this.grid_lonsp);

		return str;
	}

	public void writeTo(OutputStream out) throws IOException {
        //length
        out.write(MathUtils.signedInt2Bytes(42, 3));

        //NV
        out.write(new byte[] { 0 });

        //PV or PL
        out.write(new byte[] { 0 });

        //data representation type (octet 6) is 3
        out.write(new byte[] { 3 });

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

		// octets 18-20 (Lov - Orientation of the grid - east lon parallel to y
		// axis)
        int lon2 = (int) Math.round(this.getGridLon2() * 1000);
        out.write(MathUtils.signedInt2Bytes(lon2, 3));

        //Di
        int di = (int) (Math.abs(Math.round(this.getGridDX() * 1000)));
        out.write(MathUtils.signedInt2Bytes(di, 3));

        //Dj
        int dj = (int) (Math.abs(Math.round(this.getGridDY() * 1000)));
        out.write(MathUtils.signedInt2Bytes(dj, 3));

        // Proj Center
        out.write(new byte[] { (byte) this.getProjCenter() });
        
        //scanning mode
        out.write(new byte[] { (byte) this.getGridScanmode() });
        
		// octets 29-31 (Latin1 - first lat where secant cone cuts spherical
		// earth)
        int latin1 = (int) Math.round(this.getGridLatin1() * 1000);
        out.write(MathUtils.signedInt2Bytes(latin1, 3));

		// octets 32-34 (Latin2 - second lat where secant cone cuts spherical
		// earth)
        int latin2 = (int) Math.round(this.getGridLatin2() * 1000);
        out.write(MathUtils.signedInt2Bytes(latin2, 3));

		// octets 35-37 (lat of southern pole)
        int latsp = (int) Math.round(this.getGridLatSP() * 1000);
        out.write(MathUtils.signedInt2Bytes(latsp, 3));

		// octets 38-40 (lon of southern pole)
        int lonsp = (int) Math.round(this.getGridLonSP() * 1000);
        out.write(MathUtils.signedInt2Bytes(lonsp, 3));
        
        // write the rest
        byte unused[] = new byte[2];
        if (unused.length > 0)
            out.write(unused);
	}

    public String getName() {
        return "Grid GDS Lambert Conformal (type 3)";
    }

    public Set<Integer> getSupportedTypes() {
        return Collections.singleton(3);
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canProduce(int GDSType) {
        return GDSType ==3;
    }

    public GribRecordGDS createGridGDS(int GDSType) {
        return new GribGDSLambert();
    }

    public GribRecordGDS createGridGDS(ImageInputStream in, int[] data) throws IOException {
        return new GribGDSLambert(in, data);
    }

    public Map getImplementationHints() {
        return Collections.emptyMap();
    }
}