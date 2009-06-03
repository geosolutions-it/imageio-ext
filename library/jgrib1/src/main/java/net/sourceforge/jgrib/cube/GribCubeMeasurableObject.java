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

/**
 * This abstract class is used as a superclass for the section
 * of the Grib files that have common descriptive properties which
 * can be reconducted to what I call a Measurable object, an object with
 * an associated UoM.
 *
 * @author Simone Giannecchini 
 */
abstract class GribCubeMeasurableObject {
    protected String shortName = null;
    protected String description = null;
    protected String UoM = null;

    /**
     *
     */
    public GribCubeMeasurableObject() {
    }
    
    /**Retrieves the description for this object.*/
    public String getDescription() {
        return description;
    }

    /**Sets the description for this object.*/
    public void setDescription(String description) {
        this.description = description;
    }

    /**Retrieves the short name for this object.*/
    public String getShortName() {
        return shortName;
    }

    /**Sets the short name for this object.*/
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**Retrieves the unit of measure for this object.*/
    public String getUoM() {
        return UoM;
    }

    /**Sets the unit of measure for this object.*/
    public void setUoM(String uoM) {
        UoM = uoM;
    }
}
