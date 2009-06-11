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
/*
 * GribGDSFactory.java  1.0  10/01/2002
 *
 */
package net.sourceforge.jgrib.gdsgrids;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import net.sourceforge.jgrib.GribRecordGDS;

/**
 * GribGDSFactory determines the proper subclass of GribRecordGDS to create.
 * Extend GribRecordGDS to add a GDS type for your definition. Add types to the
 * switch statement to create an instance of your new type NOTE - only a few
 * types are supported so far
 * 
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 */
public class GribGDSFactory {
	private GribGDSFactory() {
	}

	/**
	 * Determines the Grid type and calls the appropriate constructor (if it
	 * exists)
	 * 
	 * @param in
	 *            bit input stream with GDS content
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             if stream can not be opened etc.
	 * @throws NoValidGribException
	 *             if stream contains no valid GRIB file
	 * @throws NotSupportedException
	 *             DOCUMENT ME!
	 */
	/**
	 * getGDS
	 * 
	 * @param in
	 *            DOCUMENT ME!
	 * 
	 * @return GribRecordGDS
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws NoValidGribException
	 *             DOCUMENT ME!
	 * @throws NotSupportedException
	 *             DOCUMENT ME!
	 */
	public static GribRecordGDS getGDS(final ImageInputStream in)throws IOException {

		// octets 1-6 give the common GDS data - before the Table D unique data
		final int[] header = new int[6];
		header[0] = in.read();
		header[1] = in.read();
		header[2] = in.read();
		header[3] = in.read();
		header[4] = in.read();
		header[5] = in.read();

		// octet 6 (grid type - see table 6)
		final int grid_type = header[5];
		switch (grid_type) {
		case GribGDSLambert.LAMBERT_GRID_TYPE:
			return new GribGDSLambert(in,header);
		case GribGDSLatLon.LATLON_GRID_TYPE:
			return new GribGDSLatLon(in,header);
		case GribGDSPolarStereo.POLAR_STEREO_GRID_TYPE:
			return new GribGDSPolarStereo(in,header);
		case GribGDSRotatedLatLon.ROTATED_LATLON_GRID_TYPE:
			return new GribGDSRotatedLatLon(in,header);
		default:
			throw new UnsupportedOperationException("Unable to create gsd for grid type "+grid_type);
		}

	}
		

	/**
	 * getGDS, used as a default constructor. This constructor is used to build
	 * gribgds without having a byte stream to provide them with. I am planning
	 * on using this when building a grib file from scratch, because in such a
	 * case we will not have data we need as a byte strean but rather we will
	 * have them as primitive types as requested by the grib specification.
	 * 
	 * @param grid_type
	 *            DOCUMENT ME!
	 * 
	 * @return GribRecordGDS
	 * 
	 * @throws NotSupportedException
	 *             DOCUMENT ME!
	 */
	public static GribRecordGDS getGDS(int grid_type) {
		switch (grid_type) {
		case GribGDSLambert.LAMBERT_GRID_TYPE:
			return new GribGDSLambert();
		case GribGDSLatLon.LATLON_GRID_TYPE:
			return new GribGDSLatLon();
		case GribGDSPolarStereo.POLAR_STEREO_GRID_TYPE:
			return new GribGDSPolarStereo();
		case GribGDSRotatedLatLon.ROTATED_LATLON_GRID_TYPE:
			return new GribGDSRotatedLatLon();
		default:
			throw new UnsupportedOperationException("Unable to create gsd for grid type "+grid_type);
		}

		

	}
}
