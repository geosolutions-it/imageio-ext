/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.swan.raster;

import it.geoSolutions.jiioExt.swan.SwanHeaderDocument;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.Datasets;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.General;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.Raster;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.General.Envelope;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.General.Tau;
import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.General.Envelope.RasterSpace.Enum;
import it.geosolutions.imageio.plugins.swan.SwanImageReader;
import it.geosolutions.imageio.plugins.swan.utility.UomConverter;
import it.geosolutions.imageio.plugins.swan.utility.ValueConverter;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.TileFactory;
import javax.units.Unit;

import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.sun.media.jai.codecimpl.util.RasterFactory;
/**
 * Class representing a SWAN data Raster.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class SwanRaster {
	
	private final static String[] BI_COMPONENT_QUANTITIES = new String[] {
			"WIND", "FORCE", "TRANSP" };

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.swan.raster");

	public final static DateTimeFormatter dtf = ISODateTimeFormat
			.basicDateTimeNoMillis();

	// each output value stored within the swan file is composed of a
	// fixed number of chars. Such a number also depends on the quantity of
	// decimal digits which are used to represent the floating point value.
	// However, the representation of any number ALWAYS contains atleast 8
	// chars.

	/** The number of formatting chars. */
	private final static int constFormattingCharsNum = 8;

	/**
	 * 
	 * <pre>
	 *     Any value is represented as a sequence of N chars:
	 *     
	 *     2 space chars (if positive values) or a space + a '-' char (if negative)
	 *     2 chars: = ['0'] + ['.']
	 *     D chars: the decimal digits.
	 *     4 chars: ['E'] + ['+' or '-'] + 2 chars representing the exponent power
	 *     -----------------------------------------------------------------------
	 *     
	 *     So, the constant number of formatting chars is 8.
	 *     As an instance, a file containing values with 4 decimal digits should
	 *     have the following content (starting just after the word 'HERE:')
	 *     START HERE:  0.1111E+03  0.1234E-05  0.2222E+01 -0.5678E+02 -0.1234E-07
	 *     			  
	 * </pre>
	 * 
	 * It is worth to point out that, assuming that a positive value contains 2
	 * heading spaces (also the first value contained in the file) while a
	 * negative value contains a heading space followed by the minus char ('-') ,
	 * data values are NOT space separated.
	 * 
	 */

	/** The number of decimal digits, retrieved from the header */
	private int decimalDigitsNum = -1;

	/** The total number of chars used to represent a value */
	private int charsForValue; // decimalDigistNum + fixedFormattingCharsNum

	/**
	 * The total number of chars used to represent a single forecast for a
	 * single quantity
	 */
	private int datasetForecastSize = -1;

	/** the number of rows of the raster */
	private int nRows = -1;

	/** the number of cols of the raster */
	private int nCols = -1;

	/** the number of forecasts */
	private int nTaus = -1;

	/** the number of quantities contained in the input source */
	private int nDatasets = -1;

	protected Datasets datasets = null;

	/** Envelope properties */
	/** X lower left */
	private double xll = -1;

	/** Y lower left */
	private double yll = -1;

	/** X upper right */
	private double xur = -1;

	/** Y upper right */
	private double yur = -1;

	/** ImageInputStream used to read the source that contain data */
	private ImageInputStream imageIS = null;

	/** ImageInputStream used to read the header file */
	private ImageInputStream headerIS = null;

	/** the number of chars to represent a new line */
	private int newLineChars;

	/** the number representing no Valid Data */
	private double noDataValue = Double.NaN;

	/** String Array containing dataset names (Swan Output quantities) */
	private String[] datasetNames = null;

	protected SwanImageReader reader;

	/** the number of images contained in the input source */
	private int nImages = -1;

	/** The zone name related to this raster */
	private String zone;
	
	private String rasterSpace = null;

	private int tauTime = -1;

	private Unit tauUom = null;

	private GregorianCalendar baseTime = null;

	public SwanRaster(ImageInputStream iis) throws FileNotFoundException,
			IOException {
		// Set the imageInputStream
		imageIS = iis;

		final File swanFile = ((FileImageInputStreamExtImpl) imageIS).getFile();

		// Retrieving the related header File
		final String fileName = swanFile.getAbsolutePath();
		final String headerFileName = new StringBuffer(fileName.substring(0,
				fileName.lastIndexOf("."))).append(".swh").toString();
		final File headerFile = new File(headerFileName);

		headerIS = new FileImageInputStreamExtImpl(headerFile);
	}

	public SwanRaster(ImageInputStream iis, SwanImageReader reader)
			throws FileNotFoundException, IOException {
		this(iis);
		this.reader = reader;
	}

	public int getNCols() {
		return nCols;
	}

	public int getNRows() {
		return nRows;
	}

	/**
	 * Parse the related Header and provide to set proper fields.
	 * 
	 * @throws IOException
	 * @throws XmlException
	 */
	public void parseHeader() throws IOException, XmlException {
		headerIS.mark();

		final File headerFile = ((FileImageInputStreamExtImpl) headerIS)
				.getFile();

		final SwanHeaderDocument swanHeaderDoc = SwanHeaderDocument.Factory
				.parse(headerFile);
		final SwanHeader header = swanHeaderDoc.getSwanHeader();

		// Setting general properties
		final General generalSection = header.getGeneral();

		nDatasets = generalSection.getDatasetNumber();
		nTaus = generalSection.getTauNumber();
		nImages = nTaus * nDatasets;
		zone = generalSection.getZone();

		final String baseTimeString = generalSection.getBaseTime();
		final DateTime dt = dtf.parseDateTime(baseTimeString);
		baseTime = dt.toGregorianCalendar();

		final Tau tauProperty = generalSection.getTau();
		tauTime = tauProperty.getTime();
		final String uom = tauProperty.getUnitOfMeasure();

		tauUom = UomConverter.getUnit(uom);

		// setting envelope properties
		final Envelope envelopeProperty = generalSection.getEnvelope();
		xll = envelopeProperty.getXll();
		yll = envelopeProperty.getYll();
		xur = envelopeProperty.getXur();
		yur = envelopeProperty.getYur();

		final Enum rasterSpaceXML = envelopeProperty.getRasterSpace();
		if (rasterSpaceXML != null)
			rasterSpace = envelopeProperty.getRasterSpace().toString();

		// getting datasets
		datasets = header.getDatasets();
		datasetNames = generalSection.getDatasetNames().getDatasetNameArray();

		// setting raster properties
		final Raster rasterSection = header.getRaster();
		nCols = rasterSection.getColumns().intValue();
		nRows = rasterSection.getRows().intValue();
		decimalDigitsNum = rasterSection.getPrecision();

		charsForValue = decimalDigitsNum + constFormattingCharsNum;

		newLineChars = findNewLineCharsNumber();
		datasetForecastSize = (((charsForValue * nCols) + newLineChars) * nRows);

		headerIS.reset();
		checkFields();
	}

	private void checkFields() throws IOException {
		// TODO: Add more properties checks?
		final String errorHeader = "Invalid Header file:\n";
		StringBuffer sb = new StringBuffer(errorHeader);
		if (nCols <= 0)
			sb.append("Columns=").append(nCols).append("\n");
		if (nRows <= 0)
			sb.append("Rows=").append(nRows).append("\n");
		if (nDatasets <= 0)
			sb.append("Datasets=").append(nDatasets).append("\n");
		if (decimalDigitsNum <= 0)
			sb.append("Precision=").append(nDatasets).append("\n");
		if (rasterSpace == null)
			sb.append("Invalid Raster Space \n");
		if (tauTime <= 0)
			sb.append("Tau Time=").append(tauTime).append("\n");
		if (baseTime == null)
			sb.append("null Base Time");
		if (tauUom == null)
			sb.append("null tau Unit Of Measure");
		final String errorString = sb.toString();
		if (!errorString.equalsIgnoreCase(errorHeader)) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, errorString);
			throw new IOException("The header file is incomplete.");
		}

	}

	/**
	 * A simple method returning the number of chars for the newLine sequence.
	 * Some OS use a simple LF (Line Feed ), while some others use the sequence
	 * CR (Carriage Return) + LF. By this way, different executions of SWAN on
	 * different OS may produce output files having different newLine sequences.
	 * When reading data, in order to jump to useful values, by skipping the
	 * proper number of bytes, I need to know the number of newLine chars.
	 * 
	 * @return the number of chars in the file representing a newLine sequence
	 * @throws IOException
	 */
	private int findNewLineCharsNumber() throws IOException {
		imageIS.mark();
		int nChars = -1;

		// Go to the first new line sequence
		final long firstNewLineOccurrence = nCols * charsForValue;
		imageIS.seek(firstNewLineOccurrence);
		byte b[] = new byte[2];
		final int byteRead = imageIS.read(b, 0, 2);
		if (byteRead >= 1) {
			if (b[0] == 10)
				nChars = 1; // Line Feed (LF)
			else if (b[0] == 13 && b[1] == 10)
				nChars = 2; // CR (Carriage Return) + LF
		}
		// Resetting the stream
		imageIS.reset();
		return nChars;

	}

	/**
	 * Simply returns the number of Images
	 */
	public int getNImages() {
		return nImages;
	}

	public BufferedImage readRaster(final long imageStartAt,
			ImageReadParam param, final boolean isBicomponent)
			throws IOException {
		final WritableRaster raster;
		final int nBands = isBicomponent ? 2 : 1;

		int dstWidth = -1;
		int dstHeight = -1;
		int srcRegionWidth = -1;
		int srcRegionHeight = -1;
		int srcRegionXOffset = -1;
		int srcRegionYOffset = -1;
		int xSubsamplingFactor = -1;
		int ySubsamplingFactor = -1;
		boolean doSubsampling = false;

		// //////////////////////////////////////////////////////////////////////
		//
		// STEP 1
		//
		// Retrieving Information about Source Region and doing
		// additional intialization operations.
		//
		// /////////////////////////////////////////////////////////////////////
		Rectangle srcRegion = param.getSourceRegion();
		if (srcRegion != null) {
			srcRegionWidth = (int) srcRegion.getWidth();
			srcRegionHeight = (int) srcRegion.getHeight();
			srcRegionXOffset = (int) srcRegion.getX();
			srcRegionYOffset = (int) srcRegion.getY();

			// //
			//
			// Minimum correction for wrong source regions
			//
			// When you do subsampling or source subsetting it might happen that
			// the given source region in the read param is uncorrect, which
			// means it can be or a bit larger than the original file or can
			// begin a bit before original limits.
			//
			// We got to be prepared to handle such case in order to avoid
			// generating ArrayIndexOutOFboundsException later in the code.
			//
			// //
			if (srcRegionXOffset < 0)
				srcRegionXOffset = 0;
			if (srcRegionYOffset < 0)
				srcRegionYOffset = 0;
			if ((srcRegionXOffset + srcRegionWidth) > nCols) {
				srcRegionWidth = nCols - srcRegionXOffset;
			}
			// initializing destWidth
			dstWidth = srcRegionWidth;

			if ((srcRegionYOffset + srcRegionHeight) > nRows) {
				srcRegionHeight = nRows - srcRegionYOffset;
			}
			// initializing dstHeight
			dstHeight = srcRegionHeight;

		} else {
			// Source Region not specified.
			// Assuming Source Region Dimension equal to Source Image Dimension
			dstWidth = nCols;
			dstHeight = nRows;
			srcRegionXOffset = srcRegionYOffset = 0;
			srcRegionWidth = nCols;
			srcRegionHeight = nRows;
		}

		// SubSampling variables initialization
		xSubsamplingFactor = param.getSourceXSubsampling();
		ySubsamplingFactor = param.getSourceYSubsampling();
		if ((xSubsamplingFactor > nCols) || (ySubsamplingFactor > nRows)) {
			throw new IOException(
					"The subSamplingFactor cannot be greater than image size!");
		}
		if (xSubsamplingFactor > 1 || ySubsamplingFactor > 1)
			doSubsampling = true;
		// ////////////////////////////////////////////////////////////////////////////
		//
		// I'm loading data to create a Raster needed for a Tile.
		// Thus, if the samples needed for the tile are not located immediatly
		// after the header, I need to find (and count) a defined number of
		// whitespaces (a withespace could be one of
		// {' ' , '\n' , '\r' , '\t' , "\r\n"})
		//
		// ////////////////////////////////////////////////////////////////////////////

		// //
		//
		// Updating the destination size in compliance with
		// the subSampling parameters
		//
		// //
		dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
		dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

		// //
		// 
		// defining some useful constants needed to skip data
		//
		// //

		// The length (in chars) of a data row (included the new line chars)
		final long rowSizeChars = (nCols * charsForValue) + newLineChars;

		// When Source Region Y Offset is not zero, we need to skip several
		// rows. The following constant defines the number of chars to throw
		// away before to find the first valid row (subsampling will be applied
		// afterwards).
		final long charsToThrowAwayBeforeFirstUsefulRow = (rowSizeChars * srcRegionYOffset);

		// When Source Region X offset is not zero, we need to skip some data of
		// the row, prior to find useful samples. The following constant defines
		// the number of chars to throw away before to find the first valid
		// column (subsampling will be applied afterwards)
		final long charsToThrowAwayBeforeFirstUsefulColumn = srcRegionXOffset
				* charsForValue;

		// The following constant defines the number of chars to throw away
		// after I filled a raster row.
		final long charsBeforeNewRow = newLineChars
				+ (nCols - (srcRegionWidth + srcRegionXOffset)) * charsForValue;

		final double noData = noDataValue;
//		final int[] banks = new int[nBands];
//		final int[] offsets = new int[nBands];
//		for (int band = 0; band < nBands; band++) {
//			banks[band] = band;
//			offsets[band] = 0;
//		}

		// SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_FLOAT,
		// dstWidth, dstHeight,
		// dstWidth, banks, offsets);
		// ColorModel cm = SwanImageReader.retrieveColorModel(sm);
		// raster = RasterFactory.createWritableRaster(sm, null);

		final TileFactory factory = (TileFactory) JAI.getDefaultInstance()
				.getRenderingHint(JAI.KEY_TILE_FACTORY);
		if (factory != null)
			raster = factory.createTile(RasterFactory.createBandedSampleModel(
					java.awt.image.DataBuffer.TYPE_FLOAT, dstWidth, dstHeight,
					nBands), null);
		else
			raster = RasterFactory.createBandedRaster(
					java.awt.image.DataBuffer.TYPE_FLOAT, dstWidth, dstHeight,
					nBands, null);
		SampleModel sm = raster.getSampleModel();
		ColorModel cm = SwanImageReader.retrieveColorModel(sm);
		
		// /////////////////////////////////////////////////////////////////////
		//
		// STEP 2
		//
		// Preliminar seek of the first useful data section
		//
		// /////////////////////////////////////////////////////////////////////

		imageIS.mark();

		// Seeking the stream position where to find data for the required
		// imageIndex
		imageIS.seek(imageStartAt);

		for (int band = 0; band < nBands; band++) {

			// Skipping useless rows if Source Region Offset Y is not zero.
			if (srcRegionYOffset != 0)
				imageIS.skipBytes(charsToThrowAwayBeforeFirstUsefulRow);

			// setting raster scan indexes
			int rasterX = 0;
			int rasterY = 0;

			double value = -1;

			// TUNING: using a final local variable instead of a class field.
			final int readLength = charsForValue;

			long toSkip = -1; // Represents the number of bytes need to be
			// skipped
			long skipped = -1; // will contains the number of bytes which has
			// been
			// skipped after a skipBytes operation

			final byte b[] = new byte[readLength];

			// /////////////////////////////////////////////////////////////////////
			//
			// STEP 3
			//
			// Data loading
			//
			// /////////////////////////////////////////////////////////////////////

			for (int r = 0; r < srcRegionHeight; r++) {

				// rows scan
				if (doSubsampling && (r % ySubsamplingFactor != 0)) {
					// skipping row
					toSkip = rowSizeChars;
					skipped = imageIS.skipBytes(toSkip);
					while (skipped != toSkip) {
						toSkip = toSkip - skipped;
						skipped = imageIS.skipBytes(toSkip);
					}
				} else {
					// going to the x-start of the source region
					if (srcRegionXOffset != 0) {
						// Skip the first srcRegionXOffset values
						// This operation may require more skipping steps
						// (Although
						// this should be an unusual case)
						toSkip = charsToThrowAwayBeforeFirstUsefulColumn;
						skipped = imageIS.skipBytes(toSkip);
						while (skipped != toSkip) {
							toSkip = toSkip - skipped;
							skipped = imageIS.skipBytes(toSkip);
						}
					}

					for (int c = 0; c < srcRegionWidth; c++) {
						if (doSubsampling && (c % xSubsamplingFactor != 0)) {
							// skipping column
							imageIS.skipBytes(readLength);

						} else {
							// reading the value
							imageIS.read(b, 0, readLength);

							// converting data read to a float value
							value = ValueConverter
									.getValue(b, decimalDigitsNum);

							// setting sample
							rasterY = r / ySubsamplingFactor;
							rasterX = c / xSubsamplingFactor;
							raster.setSample(rasterX, rasterY, band,
									(float) value);
						}

					}
					// skipping to the start of the new row of data.
					// This operation may require more skipping steps (Although
					// this should be an unusual case )
					toSkip = charsBeforeNewRow;
					skipped = imageIS.skipBytes(toSkip);
					while (skipped != toSkip) {
						toSkip = toSkip - skipped;
						skipped = imageIS.skipBytes(toSkip);
					}
				}
			}
		}
		// resetting the stream
		imageIS.reset();
		return new BufferedImage(cm,raster,false,null);
	}

	public int getDatasetForecastSize() {
		return datasetForecastSize;
	}

	public ImageInputStream getHeaderIS() {
		return headerIS;
	}

	public int getNDatasets() {
		return nDatasets;
	}

	public int getNTaus() {
		return nTaus;
	}

	public double getXll() {
		return xll;
	}

	public double getXur() {
		return xur;
	}

	public double getYll() {
		return yll;
	}

	public double getYur() {
		return yur;
	}

	public int getDecimalDigitsNum() {
		return decimalDigitsNum;
	}

	public Datasets getDatasets() {
		return datasets;
	}

	public String getRasterSpace() {
		return rasterSpace;
	}

	public int getTauTime() {
		return tauTime;
	}

	public Unit getTauUom() {
		return tauUom;
	}

	public String[] getDatasetNames() {
		return datasetNames;
	}

	public GregorianCalendar getBaseTime() {
		return baseTime;
	}

	/**
	 * Returns the index of the quantity, given an absolute index.
	 * 
	 * <pre>
	 * 	As an instance, a source file having 5 forecasts for 6 quantities 
	 * 	supports 30 different absolute indexes. 
	 *  The absolute index 0 refers to the first forecast of the first 
	 *  quantity. The absolute index 16	refers to the 3RD forecast of the 5TH 
	 *  quantity.
	 *  
	 *  16 = (6*2) + 4
	 *   |      |    |
	 *   |      |    \ ---- 4 = 5TH quantity (indexes start from zero)
	 *   |		|
	 *   |      \---------- 2 = 3RD forecast (indexes start from zero)
	 *   |		
	 *   \----------------- Absolute image Index
	 * </pre>
	 * 
	 * @param imageIndex
	 *            absolute index
	 * 
	 * @return the index related to the quantity which the imageIndex refers to
	 * 
	 */

	private int getQuantityIndexFromImageIndex(int imageIndex) {
		return imageIndex % nDatasets;
	}

	/**
	 * returns <code>true</code> if the quantity related the input imageIndex
	 * is bicomponent (As an instance, WIND, FORCE,...)
	 * 
	 * @param imageIndex
	 * @return
	 */

	public boolean isBiComponentQuantity(int imageIndex) {
		// getting the quantity name
		final String quantityName = datasetNames[getQuantityIndexFromImageIndex(imageIndex)];

		// check if this is a biComponent quantity
		final int biComponentsQuantitiesNum = BI_COMPONENT_QUANTITIES.length;
		for (int j = 0; j < biComponentsQuantitiesNum; j++) {
			if (quantityName.equals(BI_COMPONENT_QUANTITIES[j])) {
				return true;
			}
		}
		return false;
	}

	public int getIndexFromQuantityName(String quantityName) {
		final int datasetsNum = nDatasets;
		for (int i = 0; i < datasetsNum; i++)
			if (quantityName.equals(datasetNames[i]))
				return i;
		return -1;
	}

	public String getZone() {
		return zone;
	}

}
