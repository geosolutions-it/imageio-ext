/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
 */
package it.geosolutions.imageio.plugins.swan;

import it.geosolutions.imageio.plugins.swan.raster.SwanRaster;

import javax.imageio.metadata.IIOMetadata;

public abstract class SwanBaseMetadata extends IIOMetadata {

	protected int decimalDigits=-1;

	protected int nRows = -1;

	protected int nCols = -1;

	protected double xll = -1;

	protected double yll = -1;

	protected double xur = -1;

	protected double yur = -1;

	protected String rasterSpace = null;

	final public static String[] rasterSpaceTypes = { "PixelIsPoint",
			"PixelIsArea" };

	public SwanBaseMetadata(boolean standardMetadataFormatSupported,
			String nativeMetadataFormatName,
			String nativeMetadataFormatClassName,
			String[] extraMetadataFormatNames,
			String[] extraMetadataFormatClassNames) {
		super(standardMetadataFormatSupported, nativeMetadataFormatName,
				nativeMetadataFormatClassName, extraMetadataFormatNames,
				extraMetadataFormatClassNames);
	}

	/**
	 * This method uses access methods of the inputRaster to determine values
	 * needed for metadata initialization
	 * 
	 * @param inputRaster
	 */
	public void initializeFromRaster(SwanRaster inputRaster) {
		if (inputRaster != null) {
			nRows = inputRaster.getNRows();
			nCols = inputRaster.getNCols();

			xll = inputRaster.getXll();
			yll = inputRaster.getYll();
			xur = inputRaster.getXur();
			yur = inputRaster.getYur();

			decimalDigits = inputRaster.getDecimalDigitsNum();
			rasterSpace = inputRaster.getRasterSpace();
		}
	}

	public void reset() {
		xll = yll = xur = yur = -1;
		nCols = nRows = -1;
		rasterSpace = "";
		decimalDigits = -1;
	}
}
