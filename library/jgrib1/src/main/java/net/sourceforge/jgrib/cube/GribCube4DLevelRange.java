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
import net.sourceforge.jgrib.tables.GribPDSLevel;

/**
 * DOCUMENT ME!
 * 
 * @author Simone Giannecchini
 */
final class GribCube4DLevelRange extends GribCubeMeasurableObject {

	/**
	 * This fields tells me whehter or not this level is numeric.
	 * 
	 * True if numeric values are used for this level (e.g. 1000 mb) False if
	 * level doesn't use values (e.g. surface). Basically indicates whether you
	 * will be able to get a value for this level.
	 * 
	 */
	protected boolean isNumeric = false;

	/** Min value for the level. */
	protected float min = Float.POSITIVE_INFINITY;

	/** MaxValue for the level set. */
	protected float max = Float.NEGATIVE_INFINITY;

	/** Number of levels for this cube. */
	protected int numberOfLevels = 0;

	/** Direction of increment for the z level. */
	protected boolean increasingUp = true;

	/**
	 * 
	 */
	public GribCube4DLevelRange() {
	}

	/**
	 * This method is used to check if a record is compatible with this cube. In
	 * particular this method check whether or not the level of the provided
	 * record is compatible with this cube.
	 * 
	 * @param record
	 *            Record to check for compatibility.
	 * 
	 * @return True for compatible, false for not compatible.
	 */
	public boolean isCompatible(final GribRecord record) {
		if ((this.UoM == null) || (this.description == null)
				|| (this.shortName == null)) {
			return true;
		}

		// getting the parameter
		final GribPDSLevel level = record.getPDS().getLevel();

		// checking for equality
		if (level.getName().equalsIgnoreCase(this.getShortName())
		// && level.getDesc().equalsIgnoreCase(this.getDescription())
				&& level.getUnits().equalsIgnoreCase(this.getUoM())
				&& level.getIsNumeric() == isNumeric()
				&& level.getIsIncreasingUp() == increasingUp) {
			return true;
		}

		return false;
	}

	/**
	 * This method is used to ipadate the levels for this cube in case we add a
	 * record to this cube.
	 * 
	 * @param record
	 * @return
	 */
	public boolean add(final GribRecord record) {
		// compatibility
		// if(!isCompatible(record))
		// return false;
		// getting the parameter to check if we already added it
		final GribPDSLevel level = record.getPDS().getLevel();
		final float minVal = level.getValue1();
		// final float maxVal = level.getValue2();

		isNumeric = level.getIsNumeric();
		increasingUp = level.getIsIncreasingUp();
		// setting min and max
		if (isNumeric()) {
			if (increasingUp) {
				if (!Float.isNaN(minVal) && (minVal > max)) {
					this.max = minVal;
				}

				if (!Float.isNaN(minVal) && minVal < min) {
					this.min = minVal;
				}
			} else {

				if (Float.isInfinite(max)
						|| (!Float.isNaN(minVal) && (minVal < max))) {
					this.max = minVal;
				}

				if (Float.isInfinite(min)
						|| (!Float.isNaN(minVal) && minVal > min)) {
					this.min = minVal;
				}
			}
			this.numberOfLevels++;
		}
		// only the first time we ran into a compatible level we set this fields
		if (getDescription() == null) {
			// this.setDescription(level.getDesc());
			this.setShortName(level.getName());
			this.setUoM(level.getUnits());

		}

		return true;
	}

	public float getMax() {
		return max;
	}

	public float getMin() {
		return min;
	}

	public int getNumberOfLevels() {
		return numberOfLevels;
	}

	public boolean isNumeric() {
		return isNumeric;
	}

	public boolean isIncreasingUp() {
		return increasingUp;
	}
}
