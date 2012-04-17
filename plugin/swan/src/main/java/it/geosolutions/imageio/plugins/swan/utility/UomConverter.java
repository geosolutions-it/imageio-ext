/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.swan.utility;

import javax.units.*;

/**
 * This class simply provide to get a proper Unit for an input unit of measure
 * specified as a <code>String</code>
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class UomConverter {
	
	//TODO: join two packages working with UoM 
	//into a single one (SWANplugin + Jaguar SWAN node)
	
//	public final static Unit METERS_PER_SECOND = SI.METER.divide(SI.SECOND);
	public final static Unit METERS_PER_SECOND = javax.units.TransformedUnit.valueOf("m/s");
	
	public final static Unit SQUARE_METERS_PER_SECOND = javax.units.TransformedUnit.valueOf("m^2/s");
	
	public final static Unit CUBIC_METERS_PER_SECOND = javax.units.TransformedUnit.valueOf("m^3/s");
	
	public final static Unit NEWTON_PER_SQUARE_METERS = javax.units.TransformedUnit.valueOf("N/m^2");
	
	public final static Unit WATT_PER_METERS = javax.units.TransformedUnit.valueOf("W/m");

	public final static Unit WATT_PER_SQUARE_METERS = javax.units.TransformedUnit.valueOf("W/m^2");
	
	public static Unit getUnit(final String uomString) {
		if (uomString.equalsIgnoreCase("1"))
			return Unit.ONE;
		if (uomString.equalsIgnoreCase(SI.SECOND.toString()))
			return SI.SECOND;
		else if (uomString.equalsIgnoreCase(SI.METER.toString()))
			return SI.METER;
		else if (uomString.equalsIgnoreCase(NonSI.HOUR.toString()))
			return NonSI.HOUR;
		else if (uomString.equalsIgnoreCase(NonSI.MINUTE.toString()))
			return NonSI.MINUTE;
		else if (uomString.equalsIgnoreCase(NonSI.DEGREE_ANGLE.toString()))
			return NonSI.DEGREE_ANGLE;
		else if (uomString.equalsIgnoreCase(METERS_PER_SECOND.toString()))
			return METERS_PER_SECOND;
		else if (uomString.equalsIgnoreCase(SQUARE_METERS_PER_SECOND.toString()))
			return SQUARE_METERS_PER_SECOND;
		else if (uomString.equalsIgnoreCase(CUBIC_METERS_PER_SECOND.toString()))
			return CUBIC_METERS_PER_SECOND;
		else if (uomString.equalsIgnoreCase(NEWTON_PER_SQUARE_METERS.toString()))
			return NEWTON_PER_SQUARE_METERS;
		else if (uomString.equalsIgnoreCase(WATT_PER_METERS.toString()))
			return WATT_PER_METERS;
		else if (uomString.equalsIgnoreCase(WATT_PER_SQUARE_METERS.toString()))
			return WATT_PER_SQUARE_METERS;
		return null;
	}
}
