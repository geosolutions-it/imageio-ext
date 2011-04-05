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
 * Class used to handle an ASCII ArcGrid format source.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
final class EsriAsciiGridRaster extends AsciiGridRaster {

	public static final String NO_DATA_MARKER = "-9999";

	/** Column number tag in the header file */
	public static final String NCOLS = "NCOLS";

	/** Row number tag in the header file */
	public static final String NROWS = "NROWS";

	/** xll corner coordinate tag in the header file */
	public static final String XLLCORNER = "XLLCORNER";

	/** yll corner coordinate tag in the header file */
	public static final String YLLCORNER = "YLLCORNER";

	/** xll center coordinate tag in the header file */
	public static final String XLLCENTER = "XLLCENTER";

	/** yll center coordinate tag in the header file */
	public static final String YLLCENTER = "YLLCENTER";

	/** cell size tag in the header file */
	public static final String CELLSIZE = "CELLSIZE";

	/** no data tag in the header file */
	public static final String NODATA_VALUE = "NODATA_VALUE";

	private String noDataMarker;

	/**
	 * Creates a new instance of EsriAsciiGridRaster.
	 * 
	 * @param iis
	 *            ImageInputStream needed to read the raster.
	 */
	public EsriAsciiGridRaster(ImageInputStream iis) {
		super(iis);
	}

	/**
	 * Creates a new instance of EsriAsciiGridRaster.
	 * 
	 * @param ios
	 *            ImageOutputStream needed to write the raster.
	 */
	public EsriAsciiGridRaster(ImageOutputStream ios) {
		super(ios);
	}

	/**
	 * Creates a new instance of EsriAsciiGridRaster.
	 * 
	 * @param ios
	 *            ImageOutputStream needed to write the raster.
	 */
	public EsriAsciiGridRaster(ImageOutputStream ios,
			AsciiGridsImageWriter writer) {
		super(ios, writer);
	}

	/**
	 * Creates a new instance of EsriAsciiGridRaster.
	 * 
	 * @param iis
	 *            ImageInputStream needed to read the raster.
	 */
	public EsriAsciiGridRaster(ImageInputStream iis,
			AsciiGridsImageReader reader) {
		super(iis, reader);
	}

	/**
	 * Parses the header for the known properties.
	 * 
	 * @throws IOException
	 *             for reading errors
	 */
	public void parseHeader() throws IOException {
		// /////////////////////////////////////////////////////////////////////
		//
		// This is the ArcInfo ASCII Grid Format
		// nrows XX
		// ncols XX
		// xllcorner | xllcenter XX
		// yllcorner | yllcenter XX
		// cellsize XX
		// NODATA_value XX (Optional)
		// XX XX XX XX... (DATA VALUES)
		//
		// /////////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.info("Header Parsed: ");

		boolean keepParsing = true;
		imageIS.mark();

		int requiredFields = 0;
		String sKey = null;
		boolean cornerInitialized = false;
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
			// Get a key, no special chars here.
			//
			// //
			sKey = getKey(imageIS, maxNumBytes, 12, (byte) 255);
			if (sKey == null | sKey.length() == 0 || sKey == "")
				break;

			// //
			//
			// Get corresponding value
			//
			// //
			value = getValue(imageIS, 300, 150, doubleConverter);
			if (Double.isNaN(value) || Double.isNaN(value))
				break;

			if (NCOLS.equalsIgnoreCase(sKey)) {
				nCols = (int) value;
				requiredFields++;

			} else if (NROWS.equalsIgnoreCase(sKey)) {
				nRows = (int) value;
				requiredFields++;

			} else if (XLLCORNER.equalsIgnoreCase(sKey)) {
				xllCellCoordinate = value;
				if (!cornerInitialized) {
					isCorner = true;
					cornerInitialized = true;
				}
				requiredFields++;

			} else if (YLLCORNER.equalsIgnoreCase(sKey)) {
				yllCellCoordinate = value;
				requiredFields++;

			} else if (XLLCENTER.equalsIgnoreCase(sKey)) {
				xllCellCoordinate = value;
				if (!cornerInitialized) {
					isCorner = false;
					cornerInitialized = true;
				}
				requiredFields++;

			} else if (YLLCENTER.equalsIgnoreCase(sKey)) {
				yllCellCoordinate = value;
				requiredFields++;

			} else if (CELLSIZE.equalsIgnoreCase(sKey)) {
				cellSizeX = cellSizeY = value;
				requiredFields++;

			} else if (NODATA_VALUE.equalsIgnoreCase(sKey)) {

				noData = value;
				requiredFields++;
				keepParsing = false;
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
		if (requiredFields < 5) {
			// The Header is not compliant with the ArcInfo ASCII format.
			// Before checking if the Header is compliant to GRASS format, i
			// need to reset the stream
			imageIS.reset();
			throw new IOException(
					"This file is not a valid ESRI ascii grid file.");
		} else {
			// ////////////////////////////////////////////////////////////////
			//
			// Looking for the data
			//
			// ////////////////////////////////////////////////////////////////
			if (requiredFields == 6) {
				// ///
				//
				// We found NO_DATA. Hence look for the start of data and save
				// the stream position
				//
				// ///
				byte b;
				int limit = 0;
				while (true) {
					b = (byte) (imageIS.read() & 0xff);
					if (++limit > 100) {
						imageIS.reset();
						throw new IOException(
								"This file is not a valid ESRI ascii grid file.");

					}

					// /////////////////////////////////////////////////////////////////
					//
					// Is it something to use or not.
					//
					// /////////////////////////////////////////////////////////////////
					if (b == -1) {
						imageIS.reset();
						throw new IOException(
								"This file is not a valid ESRI ascii grid file.");

					}

					// ///////////////////////////////////////////////////////////////////
					//
					// Eat spaces, tabs carriage return, line feeds.
					//
					// ///////////////////////////////////////////////////////////////////
					if (b == 32 || b == 9 || b == 10 || b == 13) {
						continue;
					}
					// only digits and + or - are allowed
					if ((b >= 48 && b <= 57) || b == 43 | b == 45) {
						dataStartAt = imageIS.getStreamPosition() - 1;
						break;
					}
				}
			} else if (requiredFields == 5) {
				// ///
				//
				// We dound only 5 fields and we did not found a no data which
				// is optional, then let's see if the actual char is a digit
				// which might mean the start of the body (we cannot be 100%
				// sure though).
				//
				// ///
				imageIS.seek(imageIS.getStreamPosition() - 1);
				byte b = (byte) (imageIS.read() & 0xff);
				// only digits and + or - are allowed
				if ((b >= 48 && b <= 57) || b == 43 | b == 45)
					dataStartAt = imageIS.getStreamPosition() - 1;
				else {
					imageIS.reset();
					throw new IOException(
							"This file is not a valid ESRI ascii grid file.");

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
	}

	/**
	 * This method provides the header writing
	 * 
	 * @param columns
	 *            A String representing the number of columns
	 * @param rows
	 *            A String representing the number of rows
	 * @param xll
	 *            A String representing the xllCellCoordinate of the Bounding
	 *            Box
	 * @param yll
	 *            A String representing the yllCellCoordinate of the Bounding
	 *            Box
	 * @param cellsizeX
	 *            A String representing the x size of the grid cell
	 * @param cellsizeY
	 *            A String representing the Y size of the grid cell
	 * @param rasterSpaceType
	 *            A string representing if xll is xllCorner or xllCenter
	 * @param noDataValue
	 *            A String representing the optional NoData value
	 * @throws IOException
	 *             if a writing error occurs
	 */
	public void writeHeader(String columns, String rows, String xll,
			String yll, String cellsizeX, String cellsizeY,
			String rasterSpaceType, String noDataValue) throws IOException {
		imageOS.writeBytes(new StringBuffer(NCOLS).append(" ").append(columns)
				.append(newline).toString());
		imageOS.writeBytes(new StringBuffer(NROWS).append(" ").append(rows)
				.append(newline).toString());

		if (rasterSpaceType.equalsIgnoreCase("pixelIsPoint")) {
			imageOS.writeBytes(new StringBuffer(XLLCENTER).append(" ").append(
					xll).append(newline).toString());
			imageOS.writeBytes(new StringBuffer(YLLCENTER).append(" ").append(
					yll).append(newline).toString());
		} else {
			imageOS.writeBytes(new StringBuffer(XLLCORNER).append(" ").append(
					xll).append(newline).toString());
			imageOS.writeBytes(new StringBuffer(YLLCORNER).append(" ").append(
					yll).append(newline).toString());
		}

		imageOS.writeBytes(new StringBuffer(CELLSIZE).append(" ").append(
				cellsizeX).append(newline).toString());

		// remember the no data value is optional
		if (noDataValue != null) {

			// we need to extend the reader to read Nan and Inf
			imageOS
					.writeBytes(new StringBuffer(NODATA_VALUE)
							.append(" ")
							.append(
									noDataValue.equalsIgnoreCase("nan") ? NO_DATA_MARKER
											: noDataValue).append(newline)
							.toString());
			// setting the no data value
			noData = Double.parseDouble(noDataValue);
		}
	}

	/**
	 * This method returns the noDataMarker returns the noDataMarker
	 */
	public String getNoDataMarker() {
		if (noDataMarker == null) {
			noDataMarker = !Double.isNaN(noData) ? Double.toString(noData)
					: NO_DATA_MARKER;
		}
		return noDataMarker;
	}

	@Override
	public AsciiGridRasterType getRasterType() {
		return AsciiGridRasterType.ESRI;
	}
}
