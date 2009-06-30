/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 *    JGriB1 - OpenSource Java library for GriB files edition 1
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/**
 * GribPDSParamTable.java  1.0  08/01/2002 Newly created, based on GribTables
 * class.  Moved Parameter Table specific functionality to this class.
 * Performs operations related to loading parameter tables stored in files.
 * Removed the embedded table as this limited functionality and made dynamic
 * changes impossible. Through a lookup table (see readParameterTableLookup)
 * all of the supported Parameter Tables are known.  An actual table is not
 * loaded until a parameter from that center/subcenter/table is loaded. For
 * now, the lookup table name is hard coded to ".\\tables\\tablelookup.lst"
 * rdg - Still need to finish implementing SubCenters
 */
package net.sourceforge.jgrib.tables;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 */
public final class GribPDSParamTable {

	private final static Logger LOGGER = Logger.getLogger(GribPDSParamTable.class.toString());

	private static final int NPARAMETERS = 256;

	/**
	 * Static Array with parameter tables used by the GRIB file (should only be
	 * one, but not actually limited to that - this allows GRIB files to be read
	 * that have more than one center's information in it) Added by Richard D.
	 * Gonzalez
	 */
	private final static GribPDSParamTable[] paramTables;

	static {
		final List<GribPDSParamTable> tables = new ArrayList<GribPDSParamTable>();
		try {
			initFromProperties(tables);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Unable to init the parameter tables", e);
		}
		
		if(tables.size()==0)
			throw new IllegalStateException("Unable to find any parameter tables!!!");
		else
			// Make table
			paramTables = (GribPDSParamTable[]) tables.toArray(new GribPDSParamTable[tables.size()]);
	}

	/** Directory where to look for parameter tables. */
	private static final String TABLE_DIRECTORY = "paramtables";

	/** Lookup file for parameter tables. */
	private static String TABLE_LIST = "tablelookup.lst";

	/** Identification of center e.g. 88 for Oslo. */
	protected int center_id;

	/**
	 * Identification of center defined sub-center - not fully implemented yet.
	 */
	protected int subcenter_id;

	/** Identification of parameter table version number */
	protected int table_number;

	/**
	 * Stores the name of the file containing this table - not opened unless
	 * required for lookup.
	 */
	protected String filename = null;

	/**
	 * URL store corresponding url of filename containint this table. URL store
	 * corresponding url of filename containint this table. Opened if required
	 * for lookup.
	 */
	protected URL url = null;

	/** Parameters - stores array of GribPDSParameter classes */
	protected GribPDSParameter[] parameters = null;

	private GribPDSParamTable() {
	}

	

	/**
	 * Load default tables from jar file (class path) Reads in the list of
	 * tables available and stores them. Does not actually open the parameter
	 * tables files, nor store the list of parameters, but just stores the file
	 * names of the parameter tables. Parameters for a table are read in when
	 * the table is requested (in the getParameterTable method). Currently
	 * hardcoded the file name to "tablelookup". May change to command line
	 * later, but would rather minimize command line inputs. Added by Tor
	 * C.Bekkvik todo: add method for appending more GRIBtables later
	 * 
	 * @param aTables
	 *            DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	private static void initFromJAR(final List<GribPDSParamTable> aTables) throws IOException {
		final ClassLoader cl = GribPDSParamTable.class.getClassLoader();

		final URL baseUrl = cl.getResource(TABLE_DIRECTORY);

		if (baseUrl == null) {
			return;
		}

		readTableEntries(baseUrl.toExternalForm(), aTables);
	}

	private static void initFromProperties(final List<GribPDSParamTable> aTables)
			throws IOException {
		final ClassLoader cl = GribPDSParamTable.class.getClassLoader();
		final URL baseUrl = cl.getResource("META-INF/table.properties");

		if (baseUrl == null) {
			initFromJAR(aTables);
			return;
		}

		final ResourceBundle myTables = ResourceBundle.getBundle("META-INF/table");
		String aBaseUrl = baseUrl.toExternalForm();
		aBaseUrl = aBaseUrl.substring(0, aBaseUrl.length()- "/META-INF/table.properties".length());

		try {
			TABLE_LIST = myTables.getString("tablelookup");
		} catch (MissingResourceException ex) {
			// default value
			TABLE_LIST = "tablelookup.lst";
		}

		readTableEntries(aBaseUrl, aTables);
	}

	private static void readTableEntries(final String aBaseUrl,
			final List<GribPDSParamTable> aTables) throws IOException {
		// Open file
		final InputStream is = new URL(aBaseUrl + "/META-INF/" +TABLE_DIRECTORY+"/"+ TABLE_LIST).openStream();
		final InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		String line;
		GribPDSParamTable table = null;
		String[] tableDefArr = null;

		while (((line = br.readLine()) != null) && (line.length() != 0)) {
			table = new GribPDSParamTable();
			tableDefArr = line.split(":");

			table.center_id = Integer.parseInt(tableDefArr[0].trim());
			table.subcenter_id = Integer.parseInt(tableDefArr[1].trim());
			table.table_number = Integer.parseInt(tableDefArr[2].trim());
			table.filename = tableDefArr[3].trim();
			table.url = new URL(aBaseUrl + "/META-INF/" +TABLE_DIRECTORY+"/"+ table.filename);

			aTables.add(table);
		}

		is.close();
	}

	/**
	 * Looks for the parameter table which matches the center, subcenter and
	 * table version from the tables array. If this is the first time asking for
	 * this table, then the parameters for this table have not been read in yet,
	 * so this is done as well.
	 * 
	 * @param center -
	 *            integer from PDS octet 5, representing Center.
	 * @param subcenter -
	 *            integer from PDS octet 26, representing Subcenter
	 * @param number -
	 *            integer from PDS octet 4, representing Parameter Table Version
	 * 
	 * @return GribPDSParamTable matching center, subcenter, and number
	 */
	public static GribPDSParamTable getParameterTable(final int center,
			final int subcenter, final int number) {
		GribPDSParamTable table = null;
		int i = 0;

		final int length = paramTables.length;
		for (; i < length; i++) {
			table = paramTables[i];
			if ((table.center_id == 7) && (table.subcenter_id == -1)
					&& (table.table_number == 3)) {
			}

			if ((center == table.center_id)
					&& ((table.subcenter_id == -1) || (subcenter == table.subcenter_id))
					&& (number == table.table_number)) {
				break;
			}
		}

		if (i == paramTables.length) {
			// currently using version 3 for international exchange
			// setting default table as in wgrib
			table = paramTables[0];

		}

		// now that this table is being used, check to see if the
		// parameters for this table have been read in yet.
		// If not, initialize table and read them in now.
		if (table.parameters == null) {
			table.parameters = new GribPDSParameter[NPARAMETERS];
			table.readParameterTable();
		}

		return table;
	}

	/**
	 * Get the parameter with id <tt>id</tt>.
	 * 
	 * @param id
	 *            DOCUMENT ME!
	 * 
	 * @return description of the unit for the parameter
	 */
	public GribPDSParameter getParameter(int id) {
		// TODO for the moment returns an MISSING param. An XML file could be
		// attached here
		// where a User puts all the information needed.
		return (parameters[id & 0x000000ff] != null ? parameters[id & 0x000000ff]
				: parameters[255 & 0x000000ff]);
	}

	/**
	 * Get the tag/name of the parameter with id <tt>id</tt>.
	 * 
	 * @param id
	 *            DOCUMENT ME!
	 * 
	 * @return tag/name of the parameter
	 */
	public String getParameterTag(int id) {
		return parameters[id].getName();
	}

	/**
	 * Get a description for the parameter with id <tt>id</tt>.
	 * 
	 * @param id
	 *            DOCUMENT ME!
	 * 
	 * @return description for the parameter
	 */
	public String getParameterDescription(int id) {
		return parameters[id].getDescription();
	}

	/**
	 * Get a description for the unit with id <tt>id</tt>.
	 * 
	 * @param id
	 *            DOCUMENT ME!
	 * 
	 * @return description of the unit for the parameter
	 */
	public String getParameterUnit(int id) {
		return parameters[id].getUnit();
	}

	/**
	 * Read parameter table
	 */

	// public void readParameterTable(String aFileName)
	private void readParameterTable() // throws IOException
	{
		int center;
		int subcenter;
		int number;

		try {
			BufferedReader br;

			if (url != null) {
				InputStream is = url.openStream();
				InputStreamReader isr = new InputStreamReader(is);

				br = new BufferedReader(isr);
			} else {
				br = new BufferedReader(new FileReader("tables\\" + filename));
			}

			// Read first
			String line = br.readLine();
			String[] tableDefArr = line.split(":");

			center = Integer.parseInt(tableDefArr[1].trim());
			subcenter = Integer.parseInt(tableDefArr[2].trim());
			number = Integer.parseInt(tableDefArr[3].trim());

			if ((center != center_id) && (subcenter != subcenter_id)
					&& (number != table_number)) {
				throw new java.io.IOException(
						"parameter table header values do not "
								+ " match values in GRIB file.  Possible error in lookup table.");
			}

			// rdg - added the 0 line length check to cover the case of blank
			// lines at
			// the end of the parameter table file.
			GribPDSParameter parameter = null;

			while (((line = br.readLine()) != null) && (line.length() != 0)) {
				parameter = new GribPDSParameter();

				tableDefArr = line.split(":");
				parameter.number = Integer.parseInt(tableDefArr[0].trim());
				parameter.name = tableDefArr[1].trim();

				// check to see if unit defined, if not, parameter is undefined
				if (tableDefArr[2].indexOf('[') == -1) {
					// Undefined unit
					parameter.description = parameter.unit = tableDefArr[2]
							.trim();
				} else {
					String[] arr2 = tableDefArr[2].split("\\[" );

					parameter.description = arr2[0].trim();

					// Remove "]"
					parameter.unit = arr2[1].substring(0,
							arr2[1].lastIndexOf(']')).trim();
				}

				this.parameters[parameter.number] = parameter;
			}
		} catch (IOException ioError) {
			System.err.println("An error occurred in GribPDSParamTable while "
					+ "trying to open the parameter table " + filename + " : "
					+ ioError);
		}
	}

	public String toString() {
		String str;

		str = "-1:" + center_id + ":" + subcenter_id + ":" + table_number
				+ "\n";

		for (int i = 0; i < parameters.length; i++) {
			str += parameters[i].toString();
		}

		return str;
	}

	/**
	 * getVersionNumber
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getVersionNumber() {
		return this.table_number;
	}
    public int getCenter_id() {
        return center_id;
    }

    public int getSubcenter_id() {
        return subcenter_id;
    }
}
