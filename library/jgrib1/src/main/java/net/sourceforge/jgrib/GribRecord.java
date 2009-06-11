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
/**
 * GribRecord.java  1.0  01/01/2001 (C) Benjamin Stark Updated Kjell R�ang,
 * 18/03/2002 Simone Giannecchini
 */
package net.sourceforge.jgrib;

import it.geosolutions.factory.NotSupportedException;

import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Comparator;

import javax.media.jai.RasterFactory;

import net.sourceforge.jgrib.util.GribRecordComparator;

/**
 * A class representing a single GRIB record. A record consists of five
 * sections: indicator section (IS), product definition section (PDS), grid
 * definition section (GDS), bitmap section (BMS) and binary data section (BDS).
 * The sections can be obtained using the getIS, getPDS, ... methods.
 * 
 * <p>
 * </p>
 * 
 * @author Benjamin Stark
 * @version 1.0
 */
public final class GribRecord implements Comparable<GribRecord> {
	/** The indicator section. */
	private GribRecordIS is;

	/** The product definition section. */
	private GribRecordPDS pds;

	/** The grid definition section. */
	private GribRecordGDS gds;

	/** The bitmap section. */
	private GribRecordBMS bms;

	/** The binary data section. */
	private GribRecordBDS bds;

	/**
	 * GribRecord, default constructor.
	 */
	public GribRecord() {
	}

	/**
	 * This function creates a GribRecord by providing all the sections needed
	 * to da that.
	 * 
	 * @param is
	 *            Indicator Section.
	 * @param pds
	 *            Product Description Section.
	 * @param gds
	 *            Grid Description Section.
	 * @param bds
	 *            Binary Description Section.
	 * @param bms
	 *            Binary Description Section.
	 */
	public GribRecord(
			final GribRecordIS is, 
			final GribRecordPDS pds,
			final GribRecordGDS gds, 
			final GribRecordBDS bds,
			final GribRecordBMS bms) {
		this.is = is;
		this.pds = pds;
		this.gds = gds;
		this.bds = bds;
		this.bms = bms;
	}

	/**
	 * Get the byte length of this GRIB record.
	 * 
	 * @return length in bytes of GRIB record
	 */
	final public int getLength() {
		return this.is.getGribLength();
	}

	/**
	 * Get the indicator section of this GRIB record.
	 * 
	 * @return indicator section object
	 */
	final public GribRecordIS getIS() {
		return this.is;
	}

	/**
	 * Get the product definition section of this GRIB record.
	 * 
	 * @return product definition section object
	 */
	final public GribRecordPDS getPDS() {
		return this.pds;
	}

	/**
	 * Get the grid definition section of this GRIB record.
	 * 
	 * @return grid definition section object
	 */
	final public GribRecordGDS getGDS() {
		return this.gds;
	}

	/**
	 * Get the bitmap section of this GRIB record.
	 * 
	 * @return bitmap section object
	 */
	final public GribRecordBMS getBMS() {
		return this.bms;
	}

	/**
	 * Get the binary data section of this GRIB record.
	 * 
	 * @return binary data section object
	 */
	final public GribRecordBDS getBDS() {
		return bds;
	}

	/**
	 * Get grid coordinates in longitude/latitude
	 * 
	 * @return longitide/latituide as doubles
	 */
	final public double[] getGridCoords() {
		return gds.getGridCoords();
	}

	/**
	 * Get data/parameter values as a writable raster of float.
	 * 
	 * @return array of parameter values
	 * 
	 * @throws NoValidGribException
	 * @throws IOException
	 */
	public WritableRaster getValues() throws IOException {
		if (!(bds.getIsConstant())) {
			return bds.getValues();
		}

		final int W = gds.getGridNX();
		final int H = gds.getGridNY();
		final int gridSize = W * H;
		final float[] values = new float[gridSize];
		final double ref = bds.getReferenceValue();

		for (int i = 0; i < gridSize; i++) {
			values[i] = (float) ref;
		}

		final DataBuffer db = new javax.media.jai.DataBufferFloat(values, W * H);

		return RasterFactory.createBandedRaster(db, W, H, W, new int[] { 0 },
				new int[] { 0 }, null);
	}

	/**
	 * Get a single value from the BDS using i/x, j/y index. Retrieves using a
	 * row major indexing.
	 * 
	 * @param i
	 *            DOCUMENT ME!
	 * @param j
	 *            DOCUMENT ME!
	 * 
	 * @return array of parameter values
	 * 
	 * @throws NoValidGribException
	 *             DOCUMENT ME!
	 * @throws IOException
	 */
	public double getValue(final int i, final int j)
			throws IOException {
		// deferred loading
		// checkBDS();
		if ((i >= 0) && (i < gds.getGridNX()) && (j >= 0)
				&& (j < gds.getGridNY())) {
			final int[] xy = GribFileUtilities.getPointFromIndex((gds.getGridNX() * j) + i,gds);

			return bds.getValues().getSampleDouble(xy[0], xy[1], 0);
		}

		throw new IllegalArgumentException("GribRecord:  Array index out of bounds");
	}

	/**
	 * Get the parameter type of this GRIB record.
	 * 
	 * @return name of parameter
	 */
	final public String getType() {
		return this.pds.getType();
	}

	/**
	 * Get a more detailed description of the parameter.
	 * 
	 * @return description of parameter
	 */
	final public String getDescription() {
		return this.pds.getDescription();
	}

	/**
	 * Get the unit for the parameter.
	 * 
	 * @return name of unit
	 */
	final public String getUnit() {
		return this.pds.getUnit();
	}

	/**
	 * Get the level (height or pressure) description.
	 * 
	 * @return description of level
	 */
	public String getLevel() {
		return this.pds.getDescription();
	}

	/**
	 * Get the base time of this GRIB record.
	 * 
	 * @return analysis or forecast time
	 */
	public Calendar getTime() {
		return this.pds.getGMTBaseTime();
	}

	/**
	 * Get a string representation of this GRIB record.
	 * 
	 * @return string representation of this GRIB record
	 */
	public String toString() {
		// combine string representations of subsections
		return "GRIB record:\n" + this.is + "\n" + this.pds + "\n"
				+ (this.pds.gdsExists() ? (this.gds.toString() + "\n") : "")
				+ (this.pds.bmsExists() ? (this.bms.toString() + "\n") : "")
				+ this.bds;
	}

	/**
	 * writeTo
	 * 
	 * @param out
	 *            OutputStream
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void writeTo(final OutputStream out) throws IOException {
		// BDS
		final ByteArrayOutputStream BDS = new ByteArrayOutputStream();
		this.bds.writeTo(BDS);

		// PDS
		final ByteArrayOutputStream PDS = new ByteArrayOutputStream(pds.getLength());
		this.pds.writeTo(PDS);

		// BMS
		ByteArrayOutputStream BMS = null;

		if (this.bms != null) {
			BMS = new ByteArrayOutputStream(bms.getLength());
			this.bms.writeTo(BMS);
		}

		// GDS
		final ByteArrayOutputStream GDS = new ByteArrayOutputStream(gds.length);
		this.gds.writeTo(GDS);

		// IS
		this.is.setLength(pds.getLength(), gds.getLength(),
				((this.bms == null) ? 0 : this.bms.getLength()), bds
						.getLength());
		final ByteArrayOutputStream IS = new ByteArrayOutputStream(is.getGribLength());
		this.is.writeTo(IS);

		// ES
		final ByteArrayOutputStream ES = new ByteArrayOutputStream();
		final GribRecordES es = new GribRecordES();
		es.writeTo(ES);

		// gather all of them in the right order
		out.write(IS.toByteArray(), 0, IS.size());
		out.write(PDS.toByteArray(), 0, PDS.size());
		out.write(GDS.toByteArray(), 0, GDS.size());
		if (this.bms != null) {
			out.write(BMS.toByteArray(), 0, BMS.size());
		}
		out.write(BDS.toByteArray(), 0, BDS.size());
		out.write(ES.toByteArray(), 0, ES.size());

	}

	public boolean equals(Object obj) {
		if (!(obj instanceof GribRecord)) {
			return false;
		}

		if (this == obj) {
			// Same object
			return true;
		}

		GribRecord rec = (GribRecord) obj;
		boolean bmsEq = true;

		if (((bms == null) & (rec.bms != null))
				|| ((bms != null) & (rec.bms == null))) {
			bmsEq = false;
		}

		if (bms != null) {
			bmsEq = bms.equals(rec.bms);
		}

		return this.pds.equals(rec.pds) & this.gds.equals(rec.gds) & bmsEq
				& this.bds.equals(rec.bds) & is.equals(rec.is);
	}


	/**
	 * 
	 * @param decimalScale
	 * @param datumPointLength
	 * @param raster
	 * @param isConstant
	 * @param max
	 * @param min
	 * @param d 
	 * @throws IOException
	 */
	public void setBDS(final GribRecordBDS bds) throws IOException {

		this.bds = bds;

	} 
	

	/**
	 * setBMS
	 * 
	 * @param bms
	 *            boolean[]
	 */
	public void setBMS(final GribRecordBMS bms) {
		this.bms = bms;
	}

	/**
	 * Set the GDS for this record. This method should be used when adding a new
	 * record on a newly created grib file.
	 * 
	 * @param gds
	 *            GribRecordGDS The GDS record to assign to this record.
	 */
	public void setGDS(final GribRecordGDS gds) {
		this.gds = gds;
	}

	/**
	 * Set the PDs for the current record. This method should be used when
	 * adding a new record on a newly created grib file.
	 * 
	 * @param paramTableVersion
	 * @param centerID
	 * @param generatingProcessID
	 * @param gridID
	 * @param GDS
	 * @param BMS
	 * @param paramID
	 * @param levelID
	 * @param levelValue1
	 * @param levelValue2
	 * @param referenceTime
	 * @param forecastTimeUnitID
	 * @param P1
	 * @param P2
	 * @param timeRangeID
	 * @param includedInAvrage
	 * @param missingFromAverage
	 * @param subCenterID
	 * @param decimalScaleFactor
	 */
	public void setPDS(final GribRecordPDS pds) {
		// calling the constructor
		this.pds = pds;
	}

	/**
	 * Creates and set the IS for this record. This method should be used when
	 * creating a new record, in order to set the IS for it. It should be used
	 * only when adding a new record to a newly created grib file.
	 * 
	 * @param edition
	 *            int Edition number for this grib record. (We currently support
	 *            1)
	 * @param PDS
	 *            int PDS length.
	 * @param GDS
	 *            int GDS length.
	 * @param BMS
	 *            int BMS length.
	 * @param BDS
	 *            int BDS length.
	 * @throws NotSupportedException
	 */
	public void setIS(final GribRecordIS is)  {
		this.is =is;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final GribRecord record) {
		final Comparator<GribRecord> recordComparator = new GribRecordComparator();
		return recordComparator.compare(this, record);

	}
}
