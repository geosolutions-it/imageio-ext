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
package net.sourceforge.jgrib.tables;

/**
 * Title:        JGrib Description:  Class which represents a parameter from a
 * PDS parameter table Copyright:    Copyright (c) 2002 Company:      U.S. Air
 * Force
 *
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 */
public final class GribPDSParameter {
    protected int number;
    protected String name;
    protected String description;
    protected String unit;

    public GribPDSParameter() {
        this.number = 0;
        this.name = "UNDEF";
        this.description = "undefined";
        this.unit = "undefined";
    }

    public GribPDSParameter(final int aNum, final String aName,
        final String aDesc, final String aUnit) {
        this.number = aNum;
        this.name = aName;
        this.description = aDesc;
        this.unit = aUnit;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public String toString() {
        return "  Parameter description\n        "+number + ":" + name + ":" + description + "[" + unit + "]";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GribPDSParameter)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GribPDSParameter param = (GribPDSParameter) obj;

        if (name != param.name) {
            return false;
        }

        if (number != param.number) {
            return false;
        }

        if (description != param.description) {
            return false;
        }

        if (unit != param.unit) {
            return false;
        }

        return true;
    }

    /**
     * rdg - added this method to be used in a comparator for sorting while
     * extracting records. Not currently used in the JGrib library, but is
     * used in a library I'm using that uses JGrib.
     *
     * @param param - the GribPDSParameter to compare to
     *
     * @return - -1 if level is "less than" this, 0 if equal, 1 if level is
     *         "greater than" this.
     */
    public int compare(GribPDSParameter param) {
        if (this.equals(param)) {
            return 0;
        }

        // check if param is less than this
        // really only one thing to compare because parameter table sets info
        // compare tables in GribRecordPDS
        if (number > param.number) {
            return -1;
        }

        return 1;
    }
}
