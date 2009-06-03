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
 * Created on Aug 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.jgrib.cube;

import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordGDS;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * DOCUMENT ME!
 * 
 * @author giannecchini TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
final class GribCube4DGrid {
	/** Envelope for this cube. */
	protected Rectangle2D envelope = null;

	/** Number of rows for this grid. */
	protected int numRows = -1;

	/** Number of columns for this grid. */
	protected int numColumns = -1;

	/**
	 * Grid type for this cube. It is the grid type in the Grib1 specification.
	 */
	protected int gridType = -1;

	/**
	 * 
	 */
	public GribCube4DGrid() {
	}

	/**
	 * This method is responsible for checking if the provided
	 * <code>GribRecord</code> is compatible for not with this 4D cube. In
	 * order to be compatible it needs to cover the same geographic area using
	 * the same CRS.
	 * 
	 * @param record
	 *            GribRecord to check for compatibility.
	 * @return True if there is compatibility false otherwise.
	 */
	public boolean isCompatible(final GribRecord record) {
		// not yet initialized
		if ((numColumns == -1) || (numRows == -1) || (gridType == -1)
				|| (envelope == null)) {
			return true;
		}

		// check for compatibility
		final GribRecordGDS grid = record.getGDS();
		final Rectangle2D env = buildEnvelope(grid);

		if ((numColumns != grid.getGridNX()) || (numRows != grid.getGridNY())
				|| (gridType != grid.getGridType()) || !envelope.equals(env)) {
			return false;
		}

		return true;
	}

	/**
	 * This method is resposnible for building an envelope (lon,lat) from a grid
	 * object taking into account versors' direction.
	 * 
	 * @param grid
	 *            GribRecordGDS to use for building the envelope.
	 * 
	 * @return A Rectange2D representing the built envelope.
	 */
	private Rectangle2D buildEnvelope(final GribRecordGDS grid) {
		Rectangle2D env = new Rectangle();
		double lon1 = grid.getGridLon1();
		double lon2 = grid.getGridLon2();
		double lat1 = grid.getGridLat1();
		double lat2 = grid.getGridLat2();
		double temp = 0.0;
		final double w = Math.abs(lon2 - lon1);
		final double h = Math.abs(lat2 - lat1);
		final boolean plus_i = grid.getGridDX() > 0;
		final boolean plus_j = grid.getGridDY() > 0;

		if (!plus_i) {
			temp = lon1;
			lon1 = lon2;
			lon2 = temp;
		}

		if (!plus_j) {
			temp = lat1;
			lat1 = lat2;
			lat2 = temp;
		}

		env.setFrame(lon1, lat2, w, h);
		return env;
	}

	/**
	 * Adding a <code>GribRecord</code>record to this <code>GribCube4DGrid</code>.
	 * 
	 * @param record
	 *            <code>GribRecord</code> to be added.
	 * 
	 * @return <code>True</code> if the record has been added,
	 *         <code>False</code> oterwise.
	 */
	public boolean add(final GribRecord record) {
		// if(!this.isCompatible(record))
		// return false;
		final GribRecordGDS gds = record.getGDS();

		if (this.envelope == null) {
			// never set this
			numColumns = gds.getGridNX();
			numRows = gds.getGridNY();
			gridType = gds.getGridType();
			envelope = buildEnvelope(gds);
		}
		return true;
	}
}
