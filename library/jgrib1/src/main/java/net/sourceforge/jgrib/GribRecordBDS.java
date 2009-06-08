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
 * GribRecordBDS.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 */
package net.sourceforge.jgrib;

import it.geosolutions.factory.NotSupportedException;
import it.geosolutions.io.input.BitInputStream;
import it.geosolutions.io.output.BitOutputStream;
import it.geosolutions.io.output.MathUtils;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.stream.ImageInputStream;
import javax.media.jai.DataBufferDouble;
import javax.media.jai.RasterFactory;

/**
 * A class representing the binary data section (BDS) of a GRIB record.
 * 
 * @author Benjamin Stark
 * @author Simone Giannecchini
 * @version 1.0
 */
public final class GribRecordBDS {
	/** Constant value for an undefined grid value. */
	public static final double UNDEFINED = Double.NaN;

	/** Length in bytes of this BDS. */
	private int length = 0;

	/** Binary scale factor. */
	private int binscale = 0;

	/** Reference value, the base for all parameter values. */
	private double referenceValue = 0.0;

	/** Number of bits per value. */
	private int numbits = 0;

	/** Raster containing the values for this grib record. */
	private WritableRaster values = null;

	/** Minimal parameter value in grid. */
	private double minvalue = Float.POSITIVE_INFINITY;

	/** Number of valid values after applying the optional bit map mask. */
	private int numValidValues = 0;

	/** Maximal parameter value in grid. */
	private double maxvalue = Float.NEGATIVE_INFINITY;

	/**
	 * rdg - added this to prevent a divide by zero error if variable data empty
	 * Indicates whether the BMS is represented by a single value - Octet 12 is
	 * empty, and the data is represented by the reference value.
	 */
	private boolean isConstant = false;

	/** Decimal scale factor for this record. */
	private int decimalScale = 0;

	/**
	 * Number of unused bit at the end of this BDS. Number of unused bit at the
	 * end of this BDS. In the Grib version 1 specification about BDS, it is
	 * stated that the data stream is zero filled to an even number of octets.
	 */
	private int unusedBits = 0;

	/** Reference to the GDS for this record. */
	private GribRecordGDS gds = null;

	/** Reference to the BMS for this record, in case one exists. */
	private GribRecordBMS bms = null;

	/** Reference to the stram linked to the underlying source. */
	private ImageInputStream inStream;

	/** Size of the part of the stream containing the compressed data. */
	private int size;

	/** Position in the stream at which the compressed data starts. */
	private long pos;

	/** Logger. */
	private final static Logger LOGGER = Logger.getLogger(GribRecordBDS.class.toString());

	/**
	 * GribRecordBDS constructor to be used when creating a GRIB from scratch.
	 * In such a case
	 * 
	 * @param decimalScale
	 *            int >=0.
	 * @param DatumPointBitLength
	 *            int If 0 we will use vairbale length.
	 * @param Data
	 *            Raster matrix of data.
	 * @param isConstant
	 *            DOCUMENT ME!
	 * @param max
	 *            DOCUMENT ME!
	 * @param min
	 *            DOCUMENT ME!
	 * @param numValidValues
	 *            DOCUMENT ME!
	 * @param gds
	 *            DOCUMENT ME!
	 * @param bms
	 *            DOCUMENT ME!
	 * 
	 * @throws NoValidGribException
	 */
	GribRecordBDS(final int DecimalScale, final int DatumPointBitLength,
			final double[] Data, final boolean isConstant, final double max,
			final double min, final int numValidValues,
			final GribRecordGDS gds, final boolean[] bms) {
		this.gds = gds;

		if (bms != null) {
			this.bms = new GribRecordBMS(bms);
		}

		this.decimalScale = DecimalScale;
		this.numbits = DatumPointBitLength;

		final int W = this.gds.getGridNX();
		final int H = this.gds.getGridNY();
		double[] rasterBuffer = new double[W * H];

		for (int i = 0; i < Data.length; i++) {
			final int[] xy = getPointFromIndex(i);
			rasterBuffer[xy[0] + (xy[1] * W)] = (double) Data[i];
		}

		// converting everything to a raster
		final DataBuffer db = new javax.media.jai.DataBufferDouble(rasterBuffer,W * H);
		this.values = RasterFactory.createBandedRaster(db, W, H, W,new int[] { 0 }, new int[] { 0 }, null);

		// this.length = 11; //length will never be shorter than this.
		this.isConstant = isConstant;
		this.maxvalue = max;
		this.minvalue = min;
		this.numValidValues = numValidValues;
		this.fillFields();
	}

	/**
	 * GribRecordBDS constructor to be used when creating a GRIB from scratch.
	 * In such a case
	 * 
	 * @param decimalScale
	 *            int >=0.
	 * @param datumPointBitLength
	 *            int If 0 we will use vairbale length.
	 * @param data
	 *            Raster matrix of data.
	 * @param isConstant
	 *            DOCUMENT ME!
	 * @param max
	 *            DOCUMENT ME!
	 * @param min
	 *            DOCUMENT ME!
	 * @param numValidValues
	 *            DOCUMENT ME!
	 * @param gds
	 *            DOCUMENT ME!
	 * @param bms
	 *            DOCUMENT ME!
	 */
	GribRecordBDS(final int decimalScale, final int datumPointBitLength,
			final WritableRaster data, final boolean isConstant,
			final double max, final double min, final int numValidValues,
			final GribRecordGDS gds, final boolean[] bms) {
		this.gds = gds;

		if (bms != null) {
			this.bms = new GribRecordBMS(bms);
		}

		this.decimalScale = decimalScale;
		this.numbits = datumPointBitLength;
		this.values = data;

		// this.length = 11; //length will never be shorter than this.
		this.isConstant = isConstant;
		assert (this.isConstant&&numbits==0)||(!this.isConstant&&numbits>0);
		this.maxvalue = max;
		this.minvalue = min;
		this.numValidValues = numValidValues;
		this.fillFields();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param inStream
	 * @param decimalscale
	 *            DOCUMENT ME!
	 * @param gds
	 *            DOCUMENT ME!
	 * @param bms
	 *            DOCUMENT ME!
	 * 
	 * @throws IOException
	 * @throws NotSupportedException
	 */
	public GribRecordBDS(final ImageInputStream inStream,
			final int decimalscale, final GribRecordGDS gds,
			final GribRecordBMS bms) throws IOException {
		this.gds = gds;
		this.bms = bms;
		this.inStream = inStream;

		/**
		 * reading BDS header and saving pos and size
		 */

		// octets 1-3 (section length)
		this.length = MathUtils.uint3(inStream.read(), inStream.read(),inStream.read());

		// octet 4, 1st half (packing flag) see table 11
		final int fourthByte = inStream.read();

		if ((fourthByte & 240) != 0) {
			throw new UnsupportedOperationException("GribRecordBDS: No other flag "
					+ "(octet 4, 1st half) than 0 (= simple packed doubles as "
					+ "grid point data) supported yet in BDS section.");
		}

		// octet 4, 2nd half (number of unused bits at end of this section)
		this.unusedBits = fourthByte & 15;

		// octets 5-6 (binary scale factor)
		this.binscale = MathUtils.int2(inStream.read(), inStream.read());

		// decimal scale from PDS
		this.decimalScale = decimalscale;

		// octets 7-10 (reference point = minimum value)
		this.referenceValue = MathUtils.IBM2FLoat(inStream.read(), inStream.read(),inStream.read(), inStream.read());

		// octet 11 (number of bits per value)
		this.numbits = inStream.read();
		if(numbits==0)
			isConstant=true;

		// pos and size, skipping positions in order to not read data now
		this.pos = inStream.getStreamPosition();
		this.size = this.length - 11;
		inStream.skipBytes(size);
	}



	/**
	 * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream. A
	 * bit map indicates the grid points where no parameter value is defined.
	 * TODO better support for table 11
	 * 
	 * 
	 * @throws IOException
	 * @todo TODO XXX optimize me by using the {@link ImageInputStream} directly
	 */
	private void parseBDS() throws  IOException {


		final int W = this.gds.getGridNX();
		final int H = this.gds.getGridNY();
		final double[] rasterBuffer = new double[W * H];

		// preparing the scaling factors
		final double ref = (Math.pow(10.0, -decimalScale) * this.referenceValue);
		final double scale = (Math.pow(10.0, -decimalScale) * Math.pow(2.0,this.binscale));

		// temp variables used during parsing
		int[] xy = null;
		double val = 0.0;

		BitInputStream in=null;
		if (!isConstant) {
			//seek 
			this.inStream.seek(this.pos);
			
			//read data
			final byte[] buffer = new byte[this.size];
			inStream.read(buffer);
			in = new BitInputStream(new ByteArrayInputStream(buffer));

		}
		else
		{
			//
			this.numValidValues=1;
			this.maxvalue=this.minvalue=this.referenceValue;
		}
		
		if (bms != null) {
			/** DO WE HAVE A BMS TO TAKE CARE OF? */
			final boolean[] bitmap = bms.getBitmap();
			final int length = bitmap.length;

			for (int i = 0; i < length; i++) {
				xy = getPointFromIndex(i);

				if (bitmap[i]) {

					if (!isConstant) {

						// valid data
						this.numValidValues++;
						
						val = ref + (scale * in.readUBits(this.numbits));
						rasterBuffer[xy[0] + (xy[1] * W)] = (double) val;

						if (val > this.maxvalue) {
							this.maxvalue = val;
						}

						if (val < this.minvalue) {
							this.minvalue = val;
						}
					} else { // rdg - added this to handle a constant valued
						// parameter
						val = ref;
						rasterBuffer[xy[0] + (xy[1] * W)] = (double) val;
					}
				} else {
					rasterBuffer[xy[0] + (xy[1] * W)] = (double) GribRecordBDS.UNDEFINED;
				}
			}
		} else {
			//BMS not present
			if (!isConstant) {
				this.numValidValues = (((this.length - 11) * 8) - this.unusedBits)/ this.numbits;

				for (int i = 0; i < this.numValidValues; i++) {
					xy = getPointFromIndex(i);
					val = ref + (scale * in.readUBits(this.numbits));
					rasterBuffer[xy[0] + (xy[1] * W)] = (double) val;

					if (val > this.maxvalue) {
						this.maxvalue = val;
					}

					if (val < this.minvalue) {
						this.minvalue = val;
					}
				}
			} else { 
				// constant valued - same min and max
				this.maxvalue = ref;
				this.minvalue = ref;
				this.numValidValues = 1; // rivedi
			}

			
		}

		// creating the raster
		final DataBuffer db = new DataBufferDouble(rasterBuffer,W * H);
		this.values = RasterFactory.createBandedRaster(db, W, H, W,new int[] { 0 }, new int[] { 0 }, null);

		// clean up
		if(in!=null)
			in.close();
	}

	/**
	 * Get the length in bytes of this section.
	 * 
	 * @return length in bytes of this section
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Get the binary scale factor.
	 * 
	 * @return binary scale factor
	 */
	public int getBinaryScale() {
		return this.binscale;
	}

	/**
	 * Get whether this BDS is single valued
	 * 
	 * @return isConstant
	 */
	public boolean getIsConstant() {
		return this.isConstant;
	}

	/**
	 * Get the reference value all data values are based on.
	 * 
	 * @return reference value
	 */
	public double getReferenceValue() {
		return this.referenceValue;
	}

	/**
	 * Get number of bits used per parameter value.
	 * 
	 * @return number of bits used per parameter value
	 */
	public int getNumBits() {
		return this.numbits;
	}

	/**
	 * Get data/parameter values as an array of double.
	 * 
	 * @return array of parameter values
	 * 
	 * @throws NoValidGribException
	 * @throws IOException
	 */
	public WritableRaster getValues() throws  IOException {
		if (this.values == null) {
			this.parseBDS();
		}

		return getValues(new Rectangle(0, 0, this.gds.getGridNX(), this.gds
				.getGridNY()));
	}

	public WritableRaster getValues(Rectangle2D roi)
			throws  IOException {
		if (this.values == null) {
			this.parseBDS();
		}

		if ((roi.getX() >= 0) && (roi.getX() <= this.gds.getGridNX())
				&& (roi.getY() >= 0) && (roi.getY() <= this.gds.getGridNY())
				&& (roi.getWidth() <= this.gds.getGridNX())
				&& (roi.getHeight() <= this.gds.getGridNY())) {
			return this.values.createWritableChild((int) roi.getX(), (int) roi
					.getY(), (int) roi.getWidth(), (int) roi.getHeight(),
					(int) roi.getX(), (int) roi.getY(), new int[] { 0 });
		}

		throw new IllegalArgumentException(
				"GribRecordBDS: Region of Interest out of bounds");
	}

	final public WritableRaster copyValues() throws 
			IOException {
		if (this.values == null) {
			this.parseBDS();
		}

		return copyValues(new Rectangle(0, 0, this.gds.getGridNX(), this.gds.getGridNY()));
	}

	public WritableRaster copyValues(final Rectangle2D roi)
			throws IOException {
		if (this.values == null) {
			this.parseBDS();
		}

		if ((roi.getX() >= 0) && (roi.getX() <= this.gds.getGridNX())
				&& (roi.getY() >= 0) && (roi.getY() <= this.gds.getGridNY())
				&& (roi.getWidth() <= this.gds.getGridNX())
				&& (roi.getHeight() <= this.gds.getGridNY())) {
			double[] rasterBuffer = new double[(int) (roi.getWidth() * roi.getHeight())];
			rasterBuffer = this.values.getSamples((int) roi.getX(), (int) roi.getY(), (int) roi.getWidth(), (int) roi.getHeight(), 0,rasterBuffer);

			final DataBuffer db = new javax.media.jai.DataBufferDouble(rasterBuffer, (int) (roi.getWidth() * roi.getHeight()));

			return RasterFactory.createBandedRaster(db, (int) roi.getWidth(),(int) roi.getHeight(), (int) roi.getWidth(),new int[] { 0 }, new int[] { 0 }, null);
		}

		throw new IllegalArgumentException(
				"GribRecordBDS: Region of Interest out of bounds");
	}

	/**
	 * Get data/parameter value as a double given the GRIB Matrix Index.
	 * 
	 * @param index
	 *            int
	 * 
	 * @return double value from raster
	 * 
	 * @throws NoValidGribException
	 *             DOCUMENT ME!
	 */
	public int[] getPointFromIndex(final int index)  {
		if ((index >= 0)
				&& (index < (this.gds.getGridNX() * this.gds.getGridNY()))) {
			// checking dimensions
			int rowIndex = 0;
			int columnIndex = 0;

			final int W = this.gds.getGridNX();
			final int H = this.gds.getGridNY();
			final boolean adiacent_i = this.gds.isAdiacent_i_Or_j();
			final boolean plus_i = this.gds.getGridDX() > 0;
			final boolean plus_j = this.gds.getGridDY() > 0;

			if (adiacent_i) {
				columnIndex = (plus_i ? (index % W) : ((W - 1) - (index % W)));
				rowIndex = (int) ((!plus_j) ? (Math.ceil(index / W)): ((H - 1) - Math.ceil(index / W)));
			} else {
				columnIndex = (int) (plus_i ? (Math.ceil(index / H)): ((W - 1) - (Math.ceil(index / H))));
				rowIndex = (plus_j ? (index % H) : ((H - 1) - (index % H)));
			}

			return new int[] { columnIndex, rowIndex };
		}

		throw new IllegalArgumentException(
				"GribRecordBDS::getPointFromIndex:Array index out of bounds");
	}

	/**
	 * Get minimum value
	 * 
	 * @return mimimum value
	 */
	public double getMinValue() {
		return minvalue;
	}

	/**
	 * Get maximum value
	 * 
	 * @return maximum value
	 */
	public double getMaxValue() {
		return maxvalue;
	}

	/**
	 * Get a string representation of this BDS.
	 * 
	 * @return string representation of this BDS
	 */
	public String toString() {
		return "    BDS section:" + '\n' + "        min/max value: "
				+ this.minvalue + " " + this.maxvalue + "\n"
				+ "        ref. value: " + this.referenceValue + "\n"
				+ "        is a constant: " + this.isConstant + "\n"
				+ "        bin. scale: " + this.binscale + "\n"
				+ "        num bits: " + this.numbits;
	}

	/**
	 * Method to help filling all fields. When using the external constructor a
	 * lot of fields need to be computed by complex algorithms. This is the
	 * place to do that.
	 */
	private void fillFields() {
		// decimal scaling factor
		final double decScaling = Math.pow(10.0, this.decimalScale);


		/**
		 * FIRST STEP, find the minimum of the scaled values if we do not
		 * have a constant value for the grid
		 */
		double min = this.minvalue * decScaling;
		double max = this.maxvalue * decScaling;

		// setting the reference value
		this.referenceValue = min;

		// how many bits do we need?
		// we are representing an unsigned integer number here
		// we do not need any strange technique.
		int bitsNumber = this.setBitsNumber(max);

		this.setBinaryScale(bitsNumber);

		// padding
		// total bits used
		this.computeUnusedBits();

		// now the overall length
		this.setLength();
	}

	final private void setLength() {
		
		// length itself, octet 4, binary
		// scale, reference value, datum
		// poinbits number
		this.length = 3 + 1 + 2 + 4 + 1; 
		
		//adding valid values
		this.length += (int) Math.ceil((numValidValues * this.numbits) / 8.0);

		//
		if ((this.length % 2) == 1) {
			this.length++;
		}
	}

	/**
	 * Serialization of BDS section with emphasis on data packing.
	 * 
	 * @param out
	 *            OutputStream
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void writeTo(final OutputStream out) throws IOException {
		/* STEP 1 processing output data */

		// buffering output
		final ByteArrayOutputStream dataByteArray = new ByteArrayOutputStream();

		try {
			// packing data into the file
			// calculating reference vale, binary scale and bitsnum
			this.packData(dataByteArray);
		} catch (Exception e) {
			throw new IOException("GribRecordBDS::writeTo:" + e.getMessage());
		}

		// STEP 2 Evaluating byte 4, E, R and bitsnumber
		final ByteArrayOutputStream ParamsByteArray = new ByteArrayOutputStream();
		this.SerializeParams(ParamsByteArray);

		// STEP 3 length of this section
		final ByteArrayOutputStream LengthByteArray = new ByteArrayOutputStream();
		this.fillLength(LengthByteArray);

		// STEP 4 writing everything!!!
		out.write(LengthByteArray.toByteArray(),0,LengthByteArray.size());
		out.write(ParamsByteArray.toByteArray(),0,ParamsByteArray.size());
		out.write(dataByteArray.toByteArray(),0,dataByteArray.size());

	}

	/**
	 * fillLength Writes the length of this section to an output stream.
	 * 
	 * @param lengthByteArray
	 *            ByteArrayOutputStream
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	private void fillLength(final ByteArrayOutputStream lengthByteArray)
			throws IOException {
		lengthByteArray.write(MathUtils.bitVector2ByteVector(this.length, 24));
	}

	/**
	 * SerializeParams
	 * 
	 * @param paramsByteArray
	 *            ByteArrayOutputStream
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	private void SerializeParams(ByteArrayOutputStream paramsByteArray)
			throws IOException {
		// octet 4 (1 byte)
		paramsByteArray.write((byte) this.unusedBits);

		// writing E (2 bytes)
		// we write sign and right after module of binary scale
		final byte[] binScale = MathUtils.bitVector2ByteVector(Math
				.abs(this.binscale), 16);

		if (this.binscale < 0) {
			binScale[0] |= 0x80;
		}

		// setting binaryScale sign bit
		paramsByteArray.write(binScale);

		// writing R (4 bytes)
		paramsByteArray.write(MathUtils.Float2IBM(this.referenceValue));

		// writing bitsnumber (1 byte)
		paramsByteArray.write((byte) this.numbits);
	}

	/**
	 * packData Pack the data we got into a bytearray with poroper filling.
	 * 
	 * @param dataByteArray
	 *            ByteArrayOutputStream
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws NoValidGribException
	 */
	private void packData(ByteArrayOutputStream dataByteArray)
			throws IOException {
		if (this.values == null) {
			return;
		}

		final int length = this.gds.getGridNX() * this.gds.getGridNY();
		final BitOutputStream bitOut = new BitOutputStream(dataByteArray);

		/**
		 * We have to loop through all data in order to evaluate reference value
		 * and numbits. First we scan all data to evaluate the maximum and
		 * minimum values of Yi10^D. Second we set Reference value to this
		 * minimum, we check if the number of bits given for the datum point is
		 * enough otherwise we evaluate E. Third we scan again all data in order
		 * to evaluate round[ [ (Yi10^D)- Min(Yi10^D) ] / [ 2^E ] ]
		 */

		// decimal scaling factor
		final double decScaling = Math.pow(10.0, this.decimalScale);

		/**
		 * DO WE HAVE A GRID WITH THE SAME VALUE ALL OVER?
		 */
		if (this.isConstant) {
			/**
			 * CONSTANT VALUE GRID
			 */

			// length already set
			// setting reference value
			assert this.referenceValue==this.maxvalue;
			assert this.referenceValue==this.minvalue;
			this.referenceValue = this.maxvalue;
			

			/**
			 * WRITING EVERYTHING
			 */

			// length (octets 1-3)
			bitOut.write(this.length, 24);

			// octet 4 everything is zero
			bitOut.write((byte) 0);

			// E
			bitOut.write(0, 16);

			// R
			bitOut.write(MathUtils.Float2IBM(this.referenceValue));

			// octet 11 set to 0
			bitOut.write((byte) 0);
		} else {
			// we need to evaluate real data and to write them
			// using the specified number of bits
			double a;
			double val;
			int b;
			int[] xy = null;

			if (this.binscale != 0) {
				for (int i = 0; i < length; i++) {
					xy = getPointFromIndex(i);
					val = this.values.getSampleDouble(xy[0], xy[1], 0); // rasterBuffer[xy[0]
					// +
					// xy[1]*W];

					if (Double.isNaN(val)) {
						// writing NOTHING when reaching a msked bit
						continue;
					}

					a = ((val * decScaling) - this.referenceValue)
							/ MathUtils.exp2(this.binscale);
					b = (int) Math.round(a);
					bitOut.write(b, this.numbits);
				}
			} else {
				for (int i = 0; i < length; i++) {
					xy = getPointFromIndex(i);
					val = this.values.getSampleDouble(xy[0], xy[1], 0); // rasterBuffer[xy[0]
					// +
					// xy[1]*W];

					if (Double.isNaN(val)) {
						// writing NOTHING when reaching a msked bit
						continue;
					}

					a = ((val * decScaling) - this.referenceValue);
					b = (int) Math.round(a);
					bitOut.write(b, this.numbits);
				}
			}
		}

		// padding
		// total bits used
		// this.computeUnusedBits();
		// padding if necessary
		for (int i = 0; i < unusedBits; i++) {
			bitOut.write(false);
		}

		// closing the bit oriented stream
		bitOut.close();
	}

	final private void computeUnusedBits() {
		this.unusedBits = 0;

		final int totalBits = numValidValues * this.numbits;

		// computing byte used to reach an integer number of bytes.
		int byteUsed = (int) Math.ceil(totalBits / 8.0);

		if ((byteUsed % 2) == 0) {
			byteUsed++;
			this.unusedBits = 8;
		}

		int partialUnusedBits = totalBits- ((int) Math.floor(totalBits / 8.0) * 8);

		if (partialUnusedBits > 0) {
			this.unusedBits += (8 - partialUnusedBits);
		}
	}

	private int setBitsNumber(final double max) {
		// how many bits do we need?
		// we are representing an unsigned integer number here
		// we do not need any strange technique.
		final double val = Math.floor(max * 1000000.0 - this.referenceValue* 1000000.0) / 1000000.0;
		int bitsNumber = (int) Math.ceil(MathUtils.log2(val));

		// take into account case when (Math.round(Max -this.refvalue)) gives
		// a value which is a power of 2
		bitsNumber += (((val) == Math.pow(2.0, bitsNumber)) ? 1 : 0);

		// int bitsNumber = bitCount((int) Math.round(Max - this.refvalue));
		return  (bitsNumber > 0) ? bitsNumber : 0;
	}

	private void setBinaryScale(final int bitsNumber) {
		if (this.numbits == 0) { // VARIABLE NUMBER OF BITS
			this.numbits = bitsNumber;
			this.binscale = 0;
		} else {
			this.binscale = bitsNumber - this.numbits;
		}
	}

	public boolean equals(Object obj) {
		try {
			if (!(obj instanceof GribRecordBDS)) {
				return false;
			}

			if (this == obj) {
				// Same object
				return true;
			}

			GribRecordBDS bds = (GribRecordBDS) obj;

			if (this.decimalScale != bds.decimalScale) {
				return false;
			}

			if (this.binscale != bds.binscale) {
				//return false;
			}

			if (this.getIsConstant() != bds.getIsConstant()) {
				return false;
			}

			if (this.getLength() != bds.getLength()) {
				return false;
			}

			if (this.getNumBits() != bds.getNumBits()) {
				return false;
			}

			if (Math.abs(bds.getReferenceValue() - this.getReferenceValue()) > 0.0001) {
				return false;
			}

			WritableRaster thisValues;
			WritableRaster values;

			try {
				thisValues = this.getValues();
				values = bds.getValues();
			} catch (Throwable e) {
				return false;
			}

			final int W = thisValues.getWidth();
			final int H = thisValues.getHeight();

			if ((thisValues.getWidth() != values.getWidth())
					|| (thisValues.getHeight() != values.getHeight())) {
				return false;
			}

			if (this.bms == null) {
				if (bds.bms != null) {
					return false;
				}
			} else if (bds.bms == null) {
				return false;
			}

			final double[] rasterBufferA = ((DataBufferDouble) thisValues.getDataBuffer()).getData();
			final double[] rasterBufferB = ((DataBufferDouble) values.getDataBuffer()).getData();

			for (int i = 0; i < W; i++) {
				for (int j = 0; j < H; j++) {
					if (bds.bms == null) {
						if (Math.abs(rasterBufferA[i + (j * W)]
								- rasterBufferB[i + (j * W)]) > 0.0001) {
							return false;
						}
					} else {
						if (bds.bms.getBitmap()[i] != bms.getBitmap()[i]) {
							return false;
						}

						if (bms.getBitmap()[i]) {
							if (Math.abs(rasterBufferA[i + (j * W)]
									- rasterBufferB[i + (j * W)]) > 0.0001) {
								return false;
							}
						}
					}
				}
			}

			return true;
		} catch (IOException e) {
			if(LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
			return false;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return
	 */
	public int getNumValidValues() {
		return this.numValidValues;
	}
}