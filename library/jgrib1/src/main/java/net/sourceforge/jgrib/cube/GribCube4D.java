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
package net.sourceforge.jgrib.cube;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.tables.GribPDSLevel;
import net.sourceforge.jgrib.util.GribRecordComparator;

/**
 * Implementation of a 4D hypercube. It represent a single band of a
 * <code>Grib5DCube</code>. The different bands are spotted by using the
 * parameter name.
 * 
 * @author Simone Giannecchini
 * @see GribCube5D
 * @todo rethink about the time compatibility between a record and this cube.
 */
public final class GribCube4D {
	/** Object used to describe the grid structure for this cube. */
	private GribCube4DGrid grid = new GribCube4DGrid();

	/** Object used to describe the levels for this cube. */
	private GribCube4DLevelRange levels = new GribCube4DLevelRange();

	/** Object used to describe the parameter for this cube. */
	private GribCube4DParameter parameter = new GribCube4DParameter();

	/** Object used to describe the time range for this cube. */
	private GribCube4DTimeRange timeRange = new GribCube4DTimeRange();

	/**
	 * SortedSet used to store the 4D cube of records. SortedSet used to store
	 * the 4D cube of records. The records are ordered with the time axis
	 * varying slower.
	 */
	private SortedSet recordSet = new TreeSet(new GribRecordComparator());

	private int numberOfRecords;

	/**
	 * 
	 */
	public GribCube4D() {
	}

	/**
	 * This method tells me if this cube is suitable for the provided record,
	 * which means that the parameter is compatible, the grid is compatible, the
	 * level is compatible, the base time is compatible.
	 * 
	 * @param record
	 *            The record to check for the compatibility.
	 * 
	 * @return True in case there is strong compatibility, false otherwise.
	 */
	public boolean isCompatible(final GribRecord record) {
		if (grid.isCompatible(record) && levels.isCompatible(record)
				&& parameter.isCompatible(record)
				&& timeRange.isCompatible(record)) {
			return true;
		}

		return false;
	}

	/**
	 * This method tells me if this cube is compatible with the provided record,
	 * which means that the the grid is compatible, the level is compatible, the
	 * base time is compatible but parameter is not compatible. This test tell
	 * me if the provided record can reside in the same 5D cube of this 4D cube.
	 * 
	 * @param record
	 *            The record to check for the weak compatibility.
	 * 
	 * @return True in case there is weak compatibility, false otherwise.
	 */
	public boolean isWeakCompatible(final GribRecord record) {
		if (grid.isCompatible(record) && levels.isCompatible(record)
				&& timeRange.isCompatible(record)) {
			return true;
		}

		return false;
	}

	/**
	 * This method is responsible for adding the provided record to this cube.
	 * In case the record is successfully added the return value is true,
	 * otherwise it is false.
	 * 
	 * @param record
	 * 
	 * @return
	 */
	public boolean add(final GribRecord record) {
		if (!isCompatible(record)) {
			return false;
		}

		if (recordSet.contains(record)) {
			return true;
		}

		// updating parameters
		if (!this.levels.add(record) || !this.parameter.add(record)
				|| !this.timeRange.add(record) || !this.grid.add(record)) {
			return false;
		}

		// adding to the list
		recordSet.add(record);
		this.numberOfRecords++;
		return true;
	}

	/**
	 * This method retrieves the short name for the parameter of this cube.
	 * 
	 * @return Short name for the parameter of this cube.
	 */
	public String getParameterSName() {
		return this.parameter.getShortName();
	}

	public boolean increasingUp() {
		return levels.isIncreasingUp();
	}

	/**
	 * This method returns an iterator for the underlying recordset,
	 * 
	 * @return An iterator to browse the underlying recordset.
	 */
	public Iterator iterator() {
		return this.recordSet.iterator();
	}

	public GregorianCalendar getFirstForecastTime() {
		return this.timeRange.getFirstForecast();
	}

	public int getNumberOfRecords() {
		return this.numberOfRecords;
	}

	public GregorianCalendar getLastForecastTime() {
		return this.timeRange.getLastForecast();
	}

	public boolean hasNumericLevel() {
		return this.levels.isNumeric();
	}

	public float getMinLevel() {
		return this.levels.getMin();
	}

	public float getMaxLevel() {
		return this.levels.getMax();
	}

	/**
	 * This method is responsible for the subsetting of an original 4D cube over
	 * a time span and a level range. The object returned is a new 4D cube which
	 * contains only the records satisfying the conditions provided by the
	 * users, in case such records exist. In case no record satisfied the
	 * provided conditions an empty cube is returned.
	 * 
	 * @param startTimeGMT
	 *            Lower temporal limit, GMT time.
	 * @param endTimeGMT
	 *            Upper temporal limit, GMT time.
	 * @param minLevel
	 * @param maxLevel
	 * 
	 * @return
	 */
	public GribCube4D subset(final GregorianCalendar startTimeGMT,
			final GregorianCalendar endTimeGMT, final Float minLevel,
			final Float maxLevel) {
		GribCube4D newCube = null;

		/**
		 * 
		 * ERROR CONDITIONS.
		 * <ol>
		 * <li>wrong order for time stamps </li>
		 * <li>start time after last forecast</li>
		 * <li> end time before first forecast </li>
		 * <li> wrong order for layers </li>
		 * <li> min level after last level 6>max level before fist level</li>
		 * </ol>
		 * 
		 */
		// error conditions 1
		final boolean lowerTime = startTimeGMT != null;
		final boolean upperTime = endTimeGMT != null;
		final boolean lowerLevel = minLevel != null;
		final boolean upperLevel = maxLevel != null;

		if (lowerTime && upperTime && endTimeGMT.before(startTimeGMT)) {
			return newCube;
		}

		// error conditions 2 and 3
		if ((upperTime && endTimeGMT.before(this.getFirstForecastTime()))
				|| (lowerTime && startTimeGMT.after(this.getLastForecastTime()))) {
			return newCube;
		}

		if (hasNumericLevel()) {
			// error condition 4
			if (lowerLevel
					&& upperLevel
					&& (increasingUp() ? (minLevel.floatValue() > maxLevel
							.floatValue()) : (minLevel.floatValue() > maxLevel
							.floatValue()))

			) {
				return newCube;
			}
			// error conditions 5 and 6
			final boolean incresingUp = increasingUp();
			if ((lowerLevel && (((incresingUp) ? minLevel.floatValue() > this
					.getMaxLevel() : minLevel.floatValue() < this.getMaxLevel())))
					|| (upperLevel && (((incresingUp) ? maxLevel.floatValue() < this
							.getMinLevel()
							: maxLevel.floatValue() > this.getMinLevel()))

					))
				return newCube;
		}

		/**
		 * 
		 * 
		 * Core processing in order to select only the records in the ranges.
		 * 
		 */
		// we might still have problems, like no records that fit the
		// query. In such a case we just return null.
		final Iterator it = this.recordSet.iterator();
		GribRecord record = null;
		GribRecordPDS pds = null;
		GregorianCalendar forecastTime = null;
		GribPDSLevel level = null;
		float value1 = 0.0f;
		while (it.hasNext()) {
			// getting a record
			record = (GribRecord) it.next();

			// getting pds
			pds = record.getPDS();

			// getting forecast time
			forecastTime = (GregorianCalendar) pds.getGMTForecastTime();

			// getting the level
			level = pds.getLevel();

			// checking level
			if (level.getIsNumeric()) {
				// lower
				value1 = level.getValue1();
				if (lowerLevel && value1 < minLevel.floatValue())
					continue;
				// upper
				if (upperLevel && value1 > maxLevel.floatValue())
					continue;
			}

			// checking forecast time
			if (lowerTime && forecastTime.before(startTimeGMT))
				continue;
			if (upperTime && forecastTime.after(endTimeGMT))
				break;

			// the record is good!
			if (newCube == null)
				newCube = new GribCube4D();
			newCube.add(record);
		}

		return newCube;
	}

	/**
	 * 
	 * @return
	 */
	public int getRank() {
		if (getNumberOfRecords() > 0) {
			int rank = 2;// xy
			if (getFirstForecastTime().before(getLastForecastTime()))
				rank++;// t
			if (hasNumericLevel() && getMaxLevel() > getMinLevel())
				rank++;// z
		}
		return 0;

	}

	/**
	 * 
	 */
	public String toString() {
		final Iterator it = recordSet.iterator();
		final StringBuffer buff = new StringBuffer();

		while (it.hasNext())
			buff.append(((GribRecord) it.next()).toString()).append("\n");

		return buff.toString();
	}
}
