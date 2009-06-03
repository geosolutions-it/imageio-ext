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
import net.sourceforge.jgrib.tables.GribPDSParameter;


/**
 * Class holding information about the parameter for a certain grib cube.
 *
 * @author Simone Giannecchini.
 * 
 */
final class GribCube4DParameter extends GribCubeMeasurableObject {
    /**
     *
     */
    public GribCube4DParameter() {
        super();
    }

    /**
     * This method is responsible for checking the compatibility between the provided record 
     * and this record.
     * Specifically it tests to see if name, short name ans unit of measure are equals (case insensitive);
     *
     * @param record
     *
     * @return True for compatibility, false otherwise.
     */
    public boolean isCompatible(final GribRecord record) {
    	//we still have to initialize this cube.
        if ((this.UoM == null) || (this.description == null)
                || (this.shortName == null)) {
            return true;
        }

        //getting the parameter
        final GribPDSParameter parameter = record.getPDS().getParameter();

        //checking for equality
        if (parameter.getName().equalsIgnoreCase(this.getShortName())
                && parameter.getDescription().equalsIgnoreCase(this
                    .getDescription())
                && parameter.getUnit().equalsIgnoreCase(this.getUoM())) {
            return true;
        }

        return false;
    }

    /**
     * When adding a new record to this cube this method is responsible for
     * updating the information about the parameter.
     * It could be empty since for the moment it does pretty niothing.
     *
     * @param record Record being added.
     *
     * @return True if everything goes fine, false otherwise.	
     */
    public boolean add(final GribRecord record) {
        //		if(!this.isCompatible(record))
        //			return false;
    	//only the first time we set the parameter params
    	if(getDescription()==null){
	        final GribPDSParameter param = record.getPDS().getParameter();
	        this.setDescription(param.getDescription());
	        this.setShortName(param.getName());
	        this.setUoM(param.getUnit());
    	}
        return true;
    }
}
