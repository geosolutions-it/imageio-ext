/*
 * Created on Aug 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.jgrib.cube;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.jgrib.GribRecord;

/**
 * This class implements the so-called 5D hypercube as we can read from a
 * dataset of grib file version 1. A 5D hypercube is used to represent with a
 * single object what could come from a set of grib files. It ims a t
 * representing the variation of multiple observable phenomenons over a certain
 * grographic bounding box, at different height/depth levels, over a certain
 * number of time dwells. It can be represented as a function from R^4 (x,y,z,t)
 * to R^N where N is the number of sampled phenomenons. As an instance we can
 * think about a forecast for temperature, pressure and humidity over a
 * rectangular area, which comes with 20 different z-levels from 0 metres to 1
 * km of height, and with 20 different taus where the period is 6 hours starting
 * at some mm/gg/yyyy.
 * 
 * This is exactly an <code>GribCube5D</code>.
 * 
 * In this implementation a <code>GribCube5D</code> is made up by a certain
 * number of <code>GribCube4D</code> where the distinction between different
 * 4D hyercubes is done over the parameter name, i.e. temperature rather than
 * pressure
 * 
 * @see net.sourceforge.jgrib.cube.GribCube4D .
 * 
 * @author Simone Giannecchini
 * 
 */
public final class GribCube5D {

	/**
	 * Map used to hold the different 4D cubes for this 5D hypercube. The key is
	 * the parameter name.
	 */
	private Map Cubes4DList = new HashMap();

	/**
	 * Default constructor.
	 */
	public GribCube5D() {

	}

	/**
	 * Returns a 4D cube whose parameter name is the one provided. In case no
	 * such a parameter exists in this cube we return null.
	 * 
	 * @param parameterName
	 *            The paramter name to look for.
	 * @return The cube4D for this parameter or null.
	 */
	public GribCube4D get4DCube(final String parameterName) {
		// check to see if we have that parameter
		if (this.Cubes4DList.containsKey(parameterName))
			return (GribCube4D) this.Cubes4DList.get(parameterName);
		return null;

	}

	/**
	 * Adding 5D <code>GribRecord</code> to this 5D hypercube. The record can
	 * be added only if it is compatible. In case it is, if a 4D hypercube able
	 * to contain it is found, the record is added there, otherwise a new 4D
	 * hypercube is created on purpose for this record.
	 * 
	 * 
	 * @param record
	 *            Record to add to this hypercube.
	 * @return True if the record is added, false otherwise.
	 * @see GribCube5D#isCompatible(final GribRecord record).
	 */
	public boolean add(final GribRecord record) {

		// is it compatible?
		if (!this.isCompatible(record))
			return false;

		// getting the parameter name to look for a cube which will accept it
		final String parameterName = record.getPDS().getParameter().getName();

		// is the list empty?
		if (!this.Cubes4DList.isEmpty()) {

			// let's check the name to see if we can add it to one of the 4D
			// record we hold
			if (this.Cubes4DList.containsKey(parameterName)) {
				// we have such a paramter name, let's see if the corresponding
				// record is
				// compatible
				final GribCube4D cube = (GribCube4D) this.Cubes4DList
						.get(parameterName);
				if (cube.add(record))
					return true;

			}

			/**
			 * we did not add this record because the parameter name is new.
			 * next step is to check if this 4D cube is compatible with this
			 * record therefore we can add another 4D cube to this 5D cube for
			 * the newly created parameter
			 */

		}
		/**
		 * either the record is compatible but the parameter name is new or this
		 * cube is empty. Result is that we create a new 4D cube and we insert
		 * it here. create a new 4D cube with this record
		 */
		final GribCube4D newCube = new GribCube4D();
		if (!newCube.add(record))
			return false;

		// add to this 5D cube
		this.Cubes4DList.put(parameterName, newCube);

		return true;

	}

	/**
	 * Checking the compatibility between the provided record and this 5D
	 * hypercube. The record is said to be compatible when it is located in a
	 * compatible 4D hypercube which means same geographical area, same
	 * basetime, same type of level.
	 * 
	 * @param record
	 *            The record to check for compatibility.
	 * @return True if compatible, false otherwise.
	 * @see GribCube4D#isWeakCompatible(GribRecord).
	 */
	public boolean isCompatible(final GribRecord record) {
		// is the list empty? We do not have any problems.
		if (this.Cubes4DList.isEmpty())
			return true;

		/**
		 * let's look if we have a compatible 4D cube get the first 4D cube and
		 * check the hypercube
		 */
		final Iterator it = Cubes4DList.values().iterator();
		final GribCube4D firstCube = (GribCube4D) it.next();

		// check if this cube is weak compatible with the provided record
		if (!firstCube.isWeakCompatible(record)) {

			// the record was not compatible at all
			// we cannot add it
			return false;

		}
		return true;
	}

	/**
	 * Purpose of this method is giving the user the possibility to access all
	 * the keys of the GribCube4D this 5D cube holds. Keys are parameters names.
	 * 
	 * @return Set containing all the parameters names.
	 */
	public Set getParametersNames() {
		return Cubes4DList.keySet();
	}

	/**
	 * Retrieves all the 4D cubes held in this 5D cubes
	 * 
	 * @return The collection of 4D cubes.
	 */
	public Collection getCubes4D() {
		return this.Cubes4DList.values();
	}

	/**
	 * Number fo 4D cubes in this 5D cube.
	 * 
	 * @return int Number fo 4D cubes in this 5D cube.
	 */
	public int getCube4DCount() {
		return this.Cubes4DList.size();
	}

	/**
	 * Debugging method it retrieves a textual description for this 5D
	 * hypercube.
	 */
	public String toString() {
		final Iterator it = Cubes4DList.values().iterator();
		final StringBuffer buff = new StringBuffer();

		while (it.hasNext())
			buff.append(((GribCube4D) it.next()).toString()).append("\n");

		return buff.toString();
	}

}