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
package it.geosolutions.imageio.plugins.arcgrid.raster;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageReader;
import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageWriter;
import it.geosolutions.imageio.utilities.StringToDouble;

import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Class used to handle an ASCII GRASS format source.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
final class GrassAsciiGridRaster extends AsciiGridRaster {

	/** Column number tag in the header file */
	public static final String COLS = "COLS:";

	/** Row number tag in the header file */
	public static final String ROWS = "ROWS:";

	/** x corner coordinate tag in the header file */
	public static final String NORTH = "NORTH:";

	/** y corner coordinate tag in the header file */
	public static final String SOUTH = "SOUTH:";

	/** y corner coordinate tag in the header file */
	public static final String EAST = "EAST:";

	/** y corner coordinate tag in the header file */
	public static final String WEST = "WEST:";

	/** GRASS ascii grids uses the * character to indicate no data values. */
	public static final String NO_DATA_MARKER = "*";

	/**
	 * Creates a new instance of GrassAsciiGridRaster.
	 * 
	 * @param iis
	 *            ImageInputStream needed to read the raster.
	 */
	public GrassAsciiGridRaster(ImageInputStream iis) {
		super(iis);
	}

	/**
	 * Creates a new instance of GrassAsciiGridRaster.
	 * 
	 * @param iis
	 *            ImageInputStream needed to read the raster.
	 */
	public GrassAsciiGridRaster(ImageInputStream iis,
			AsciiGridsImageReader reader) {
		super(iis, reader);
	}

	/**
	 * Creates a new instance of GrassAsciiGridRaster.
	 * 
	 * @param ios
	 *            ImageOutputStream needed to write the raster.
	 */
	public GrassAsciiGridRaster(ImageOutputStream ios) {
		super(ios);
	}
	
	/**
	 * Creates a new instance of GrassAsciiGridRaster.
	 * 
	 * @param ios
	 *            ImageOutputStream needed to write the raster.
	 */
	public GrassAsciiGridRaster(ImageOutputStream ios,
			AsciiGridsImageWriter writer) {
		super(ios, writer);
	}


	/**
	 * Parses the header for the known properties.
	 * 
	 * @throws IOException
	 *             for reading errors
	 */
	public void parseHeader() throws IOException {
		// ////////////////////////////////////////////////////////////////////
		// 
		// This is the GRASS ASCII Grid Format
		// NORTH: XX
		// SOUTH: XX
		// EAST: XX
		// WEST: XX
		// ROWS: XX
		// COLS: XX
		// XX XX XX XX XX XX... (DATA VALUES)
		// 
		// ////////////////////////////////////////////////////////////////////
		double north = 0;
		double south = 0;
		double east = 0;
		double west = 0;
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.info("Header Parsed: ");

		boolean keepParsing = true;
		imageIS.mark();

		int requiredFields = 0;
		String sKey = null;
		int maxNumBytes = 100;
		double value = 0.0;
		// if in the header there is a field (like ncols, nrows) not followed by
		// numbers, parseInt or parseDouble throws a NumberFormatException

		// /////////////////////////////////////////////////////////////////////
		//
		// Parsing the header
		//
		// /////////////////////////////////////////////////////////////////////
		final StringToDouble doubleConverter = StringToDouble.acquire();
		while (keepParsing) {
			// //
			//
			// Get a key
			//
			// //
			sKey = getKey(imageIS, maxNumBytes, 6, (byte) 58);
			if (sKey == null | sKey.length() == 0 || sKey == "")
				break;

			// //
			//
			// Get corresponding value
			//
			// //
			value = getValue(imageIS, maxNumBytes, 150, doubleConverter);
			if (Double.isNaN(value) || Double.isNaN(value))
				break;

			if (NORTH.equalsIgnoreCase(sKey)) {
				north = value;
				requiredFields++;

			} else if (SOUTH.equalsIgnoreCase(sKey)) {
				south = value;
				requiredFields++;

			} else if (EAST.equalsIgnoreCase(sKey)) {
				east = value;
				requiredFields++;

			} else if (WEST.equalsIgnoreCase(sKey)) {
				west = value;
				requiredFields++;

			} else if (ROWS.equalsIgnoreCase(sKey)) {
				nRows = (int) value;
				requiredFields++;

			} else if (COLS.equalsIgnoreCase(sKey)) {
				nCols = (int) value;
				keepParsing = false;
				requiredFields++;

			} else {
				keepParsing = false;
			}
		}
		StringToDouble.release(doubleConverter);
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking if any required header field has been found
		//
		// /////////////////////////////////////////////////////////////////////
		if (requiredFields != 6) {
			// The Header is not compliant with the ArcInfo ASCII format.
			// Before checking if the Header is compliant to GRASS format, i
			// need to reset the stream
			imageIS.reset();
			throw new IOException(
					"This file is not a valid ESRI ascii grid file.");
		} else {
			// /////////////////////////////////////////////////////////////////////
			//
			// Looking for the data
			//
			// /////////////////////////////////////////////////////////////////////

			// //
			//
			// We found NO_DATA. Hence look for the start of data and save
			// the stream position
			//
			// //
			byte b;
			int limit = 0;
			while (true) {
				b = (byte) (imageIS.read() & 0xff);
				if (++limit > 100) {
					imageIS.reset();
					throw new IOException(
							"This file is not a valid ESRI ascii grid file.");
				}

				// //
				//
				// Is it something to use or not.
				//
				// //
				if (b == -1) {
					imageIS.reset();
					throw new IOException(
							"This file is not a valid ESRI ascii grid file.");
				}

				// //
				//
				// Eat spaces, tabs carriage return, line feeds.
				//
				// //
				if (b == 32 || b == 9 || b == 10 || b == 13)
					continue;

				// only digits, * + and - are allowed
				if ((b >= 48 && b <= 57) || b == 43 || b == 45 || b == 42) {
					dataStartAt = imageIS.getStreamPosition() - 1;
					break;
				}
			}
		}
		// Restoring stream position
		imageIS.reset();
		imageIS.mark();
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.info("\tnCols:" + nCols);
			LOGGER.info("\tnRows:" + nRows);
		}

		// Preparing data.
		xllCellCoordinate = west;
		yllCellCoordinate = south;
		isCorner = true;
		cellSizeY = (north - south) / nRows;
		cellSizeX = (east - west) / nCols;
	}

	/**
	 * Writes the header of a ASCII GRASS File
	 * 
	 * @param columnsString
	 *            A String representing the number of columns
	 * @param rowsString
	 *            A String representing the number of rows
	 * @param xllString
	 *            A String representing the xllCorner of the Bounding Box
	 * @param yllString
	 *            A String representing the yllCorner of the Bounding Box
	 * @param cellsizeString
	 *            A String representing the size of the grid cell
	 * @param rasterSpaceType
	 *            Not interesting in GRASS rasters
	 * @param noDataValue
	 *            Not interesting in GRASS rasters
	 * 
	 * @throws IOException
	 *             if a writing error occurs
	 */
	public void writeHeader(String columnsString, String rowsString,
			String xllString, String yllString, String cellsizeStringX,
			String cellsizeStringY, String rasterSpaceType, String noDataValue)
			throws IOException {

		// //
		//
		// Initializing numeric values
		//
		// //
		nCols = Integer.parseInt(columnsString);
		nRows = Integer.parseInt(rowsString);
		cellSizeX = Double.parseDouble(cellsizeStringX);
		cellSizeY = Double.parseDouble(cellsizeStringY);
		xllCellCoordinate = Double.parseDouble(xllString);
		yllCellCoordinate = Double.parseDouble(yllString);
		noData = Double.parseDouble(noDataValue);
		final double west = xllCellCoordinate;
		final double south = yllCellCoordinate;
		final double north = south + (cellSizeY * nRows);
		final double east = west + (cellSizeX * nCols);

		// //
		//
		// Writing Header information
		//
		// //
		imageOS.writeBytes(new StringBuffer(NORTH).append(" ").append(
				Double.toString(north)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(SOUTH).append(" ").append(
				Double.toString(south)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(EAST).append(" ").append(
				Double.toString(east)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(WEST).append(" ").append(
				Double.toString(west)).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(ROWS).append(" ")
				.append(rowsString).append(newline).toString());
		imageOS.writeBytes(new StringBuffer(COLS).append(" ").append(
				columnsString).append(newline).toString());
	}

	/**
	 * return the <code>String</code> representing a noData value. In GRASS
	 * format, NoDataMarker is always the '*' sign.
	 */
	public String getNoDataMarker() {
		return NO_DATA_MARKER;
	}

	@Override
	public AsciiGridRasterType getRasterType() {
		return AsciiGridRasterType.GRASS;
	}
}
