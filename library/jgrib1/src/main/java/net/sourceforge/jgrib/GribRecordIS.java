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
 * GribRecordIS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 *
 * Updated by Capt Richard D. Gonzalez 16 Sep 02
 */
package net.sourceforge.jgrib;

import it.geosolutions.factory.NotSupportedException;
import it.geosolutions.io.output.MathUtils;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageInputStream;

/**
 * A class that represents the indicator section (IS) of a GRIB record.
 * 
 * @author Benjamin Stark
 * @author Richard D. Gonzalez - modified to indicate support of GRIB EDITION 1
 *         only
 * @author Simone Giannecchini
 * @version 1.1
 */
public final class GribRecordIS {

	/** Init string for Grib files.*/
	private final static byte[] GRIB_INIT= new byte[]{(byte) 'G',(byte) 'R',(byte) 'I',(byte) 'B'};
	

	/** Edition of GRIB specification used. */
	private final static int EDITION = 1;


	/** Length in bytes of GRIB record. */
	private int gribLength;

	/**
	 * Length in bytes of IS section. Section gribLength differs between GRIB
	 * editions 1 and 2 Currently only GRIB EDITION 1 supported - gribLength is 8
	 * octets/bytes.
	 */
	private int length = 8;

	/**
	 * GribRecordIS. This constructor is used to build the IS section from
	 * scratch using the other section. This choice has been since the IS
	 * contains the gribLength of the overall record, therefore it needs to know the
	 * gribLength of all the other sections of the record itself.
	 * 
	 * @param EDITION
	 *            int Edition of the grib record (MUST be 1)
	 * @throws NotSupportedException
	 */
	public GribRecordIS(final int edition) {
		if (1 != edition)
			throw new UnsupportedOperationException(
					"GribRecordIS:GribRecordIS(final int EDITION)::Only EDITION 1 supported by this library!");
	}
	
	public GribRecordIS(final int edition,final int PDS, final int GDS, final int BMS,
			final int BDS) {
		if (1 != edition)
			throw new UnsupportedOperationException(
					"GribRecordIS:GribRecordIS(final int EDITION)::Only EDITION 1 supported by this library!");
		setLength(PDS, GDS, BMS, BDS);
	}

	/**
	 * Constructs a <tt>GribRecordIS</tt> object from an image input stream
	 * which can link to a file or a strem.
	 * 
	 * @param in
	 *            Image input stream with IS content
	 * 
	 * @throws NotSupportedException
	 *             In case the grib EDITION field is different from 1.
	 * @throws IOException
	 *             If the stream can not be opened etc.
	 */
	public GribRecordIS(final ImageInputStream in)
			throws IOException {
		// checks 
		GribFileUtilities.ensureNotNull("inStream", in);
		
		// gribLength of GRIB record
		this.gribLength = MathUtils.uint3(in.read(), in.read(), in.read());

		// EDITION of GRIB specification
		final int edition = in.read();

		if (edition == 1) {
			this.length = 8;
		} else {
			throw new UnsupportedOperationException(
					"GribRecordIS::GribRecordIS(ImageInputStream in):GRIB EDITION "+ edition + " is not supported");
		}
	}

	/**
	 * Get the byte gribLength of this GRIB record.
	 * 
	 * @return gribLength in bytes of GRIB record
	 */
	public int getGribLength() {
		return this.gribLength;
	}

	/**
	 * Get the byte gribLength of the IS section.
	 * 
	 * @return gribLength in bytes of IS section
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Get the EDITION of the GRIB specification used.
	 * 
	 * @return EDITION number of GRIB specification
	 */
	public int getGribEdition() {
		return EDITION;
	}

	/**
	 * Get a string representation of this IS.
	 * 
	 * @return string representation of this IS
	 */
	public String toString() {
		return "    IS section:" + '\n' + "        Grib Edition "
				+ EDITION + '\n' + "        gribLength: " + this.gribLength
				+ " bytes";
	}

	/**
	 * writeTo(OutputStream out) Write the indicator section to
	 * 
	 * @param out
	 *            Output stream to write to.
	 * 
	 * @throws IOException
	 *             A low level I/O error occurred.
	 */
	public void writeTo(final OutputStream out) throws IOException {
		GribFileUtilities.ensureNotNull("out", out);
		
		// writing first 4 octets
		out.write(GRIB_INIT);

		// gribLength of this message (3 octets)
		out.write(MathUtils.bitVector2ByteVector(this.gribLength, 24));

		// EDITION (1 octet)
		out.write((byte) EDITION);
	}

	/**
	 * This method is used to set the gribLength of the Indicato Section for the
	 * current grib record. This
	 * 
	 * @param PDS
	 *            int PDS gribLength.
	 * @param GDS
	 *            int GDS gribLength.
	 * @param BMS
	 *            int BMS gribLength.
	 * @param BDS
	 *            int BDS gribLength.
	 */
	public void setLength(final int PDS, final int GDS, final int BMS,
			final int BDS) {
		this.gribLength = PDS + BDS + GDS + BMS + 8 + 4; // 8 octests for IS and
		// 4 for '7777'
	}

	public boolean equals(final Object obj) {
		if (!(obj instanceof GribRecordIS)) {
			return false;
		}

		if (this == obj) {
			// Same object
			return true;
		}

		final GribRecordIS is = (GribRecordIS) obj;

		//EDITION WILL SURELY BE 1, SO WE WON'T CHECK IT EXPLICITLY
		
		if (gribLength != is.gribLength) {
			return false;
		}

		return true;
	}
}
