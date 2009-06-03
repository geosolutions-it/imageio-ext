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
package net.sourceforge.jgrib.factory;

import it.geosolutions.factory.FactoryRegistry;
import it.geosolutions.factory.NotSupportedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

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
		final int[] data = new int[6];
		data[0] = in.read();
		data[1] = in.read();
		data[2] = in.read();
		data[3] = in.read();
		data[4] = in.read();
		data[5] = in.read();

		// octet 6 (grid type - see table 6)
		final int grid_type = data[5];
		GribGDSFactorySpi gdf = null;
		final FactoryRegistry registry = new FactoryRegistry(Arrays.asList(new Class[] { GribGDSFactorySpi.class }));
		Iterator gdfi = registry.getServiceProviders(GribGDSFactorySpi.class);
		/* FactoryFinder.factories(GribGDSFactorySpi.class, null); */

		while (gdfi.hasNext()) {
			gdf = (GribGDSFactorySpi) gdfi.next();

			if (gdf.canProduce(grid_type)) {
				break;
			}

			gdf = null;
		}

		if (gdf == null) {
			throw new UnsupportedOperationException(new StringBuffer(
					"GribGDSFactory: GRiB type ").append(grid_type).append(
					" not supported yet").toString());
		}

		GribRecordGDS gribGDSGrid = gdf.createGridGDS(in, data);
		return gribGDSGrid;

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
		GribGDSFactorySpi gdf = null;
		FactoryRegistry registry = new FactoryRegistry(Arrays.asList(new Class[] { GribGDSFactorySpi.class }));
		final Iterator gdfi = registry.getServiceProviders(GribGDSFactorySpi.class);
		while (gdfi.hasNext()) {
			gdf = (GribGDSFactorySpi) gdfi.next();

			if (gdf.canProduce(grid_type)) {
				break;
			}

			gdf = null;
		}

		if (gdf == null) {
			throw new UnsupportedOperationException(new StringBuffer(
					"GribGDSFactory: GRiB type ").append(grid_type).append(
					" not supported yet").toString());
		}

		return gdf.createGridGDS(grid_type);

	}
}
