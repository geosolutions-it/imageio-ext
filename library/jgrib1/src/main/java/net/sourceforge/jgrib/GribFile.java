/*
 * GribFile.java  1.0  01/01/2001
 *
 * (C) Benjamin Stark
 * Updated Kjell R�ang, 18/03/2002
 * Updated Richard D. Gonzalez 7 Dec 02
 * @author simone giannecchini
 */
package net.sourceforge.jgrib;

import it.geosolutions.factory.NotSupportedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import net.sourceforge.jgrib.factory.GribGDSFactory;

/**
 * A class that represents a GRIB file. It consists of a number of records which
 * are represented by <tt>GribRecord</tt> objects.
 * 
 * @author Simone Giannecchini
 * @author Benjamin Stark
 * @author Peter Gylling 2002-04-16
 * @version 1.0
 */
public final class GribFile {
	
	public enum AccessType{
		R,
		
		RW,
		
		RWS,
		
		RWD;
	}
	
	private Object originatingSource=null;
	
	/**Default number of records.*/
	public final static int DEF_RECORDS=25;
	
	/** Logger for the GribFile class. */
	private final static Logger LOGGER = Logger.getLogger(GribFile.class.toString());

	/** Input stream */
	private ImageInputStream inStream = null;

	/** List of all the records for this grib file */
	private List<GribRecord> recordList = new ArrayList<GribRecord>(DEF_RECORDS);

	/** Number of records for this grib file. */
	private int recordCount = 0;


	/**
	 * GribFile constructor that accepts as an input a file. It checks if the
	 * exists and in case it doesn't it throws a file not found exception. This
	 * method uses an internal memory mapped file.
	 * 
	 * @param file
	 *            File The file to read from.
	 * 
	 * @throws FileNotFoundException
	 *             In case the provided file does not exist.
	 * @throws IOException
	 *             In case an IOException is thrown somewhere in the code.
	 */
	GribFile(File file) throws FileNotFoundException,
			IOException {
		GribFileUtilities.ensureNotNull("file", file);
		GribFileUtilities.checkFileReadable(file);

		// we are reading from a file
		this.originatingSource = file;
		this.inStream = ImageIO.createImageInputStream(file);
		

	}

	// *** constructors *******************************************************

	/**
	 * Constructs a <code>GribFile</code> object from a file.
	 * 
	 * @param filename
	 *            name of the GRIB file
	 * 
	 * @throws FileNotFoundException
	 *             If file can not be found
	 * @throws IOException
	 *             if file can not be opened etc.
	 * @throws NotSupportedException
	 *             DOCUMENT ME!
	 * @throws NoValidGribException
	 *             if file is no valid GRIB file
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	GribFile(final String string)
			throws FileNotFoundException, IOException {
		GribFileUtilities.ensureNotNull("string", string);

		// initialization
		final File file = new File(string);
		GribFileUtilities.checkFileReadable(file);

		this.originatingSource = file;
		this.inStream = ImageIO.createImageInputStream(file);

	}

	/**
	 * Constructs a <code>GribFile</code> object from an input stream.
	 * 
	 * @param stream
	 *            The inpust stream to read from.
	 * 
	 * @throws IOException
	 *             An IO error occurred somewhere.
	 * @throws NotSupportedException
	 * @throws NoValidGribException
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	GribFile(final InputStream stream) throws IOException {
		GribFileUtilities.ensureNotNull("stream", stream);
		this.originatingSource = stream;
		this.inStream = ImageIO.createImageInputStream(stream);
	}

	/**
	 * This constructor of <code>GribFile</code> is responsible for
	 * interoperability with imageio library. When an object of a subtype of
	 * ImageInputStream will be used at construction time we will be able this
	 * way to use it directly in order to read from the underlying source. In
	 * all the other cases we will decide on the fly which is the best class to
	 * use basing this decision on the type of object provided as input source.
	 * 
	 * @param inStream
	 *            Stream to read from.
	 */
	GribFile(final ImageInputStream inStream) {
		GribFileUtilities.ensureNotNull("stream", inStream);

		this.inStream = inStream;
		this.originatingSource = inStream;

	}

	/**
	 * GribFile default constructor.
	 */
	GribFile() {
	}

	/**
	 * Constructs a <code>GribFile</code> object from a URL.
	 * 
	 * @param url
	 *            DOCUMENT ME!
	 * 
	 * @throws IOException
	 * @throws NotSupportedException
	 * @throws NoValidGribException
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	GribFile(final URL url)
			throws IOException {
		GribFileUtilities.ensureNotNull("url", url);
		
		if (url.getProtocol().compareToIgnoreCase("file") == 0) {
			/**
			 * We are reading from a file
			 */

			// we are reading from a file
			this.inStream = ImageIO.createImageInputStream(new File(URLDecoder
					.decode(url.getFile(), "UTF-8")));

		} else {
			/** We are not reading from a file. */
			final InputStream in = url.openStream();

			if (in == null) {
				throw new IllegalArgumentException(
						"GribFile::GribFile(final URL urlToReadFrom):the provided input stream is null!");
			}

			this.inStream = ImageIO.createImageInputStream(in);

		}
		originatingSource = url;
	}

	/**
	 * Parse a GribFile.
	 * 
	 * @throws IOException
	 *             if stream can not be opened etc.
	 * @throws NotSupportedException
	 *             DOCUMENT ME!
	 * @throws NoValidGribException
	 *             if stream does not contain a valid GRIB file
	 */
	public void parseGribFile() throws IOException {
		
		
		
		
		// the indicator section of a record
		GribRecordIS is = null;
		GribRecordPDS pds = null;
		GribRecordGDS gds = null;
		GribRecordBMS bms = null;
		GribRecordBDS bds = null;
		GribRecord gr = null;

		/** Looking for a new GriB1 record by looking for a 'GRIB' string. */
		int totalBytes = 0;
		int recordCount = 0;

		while (GribFileUtilities.seekHeader(inStream)) {
			// System.out.println(recordCount);

			/**
			 * IS
			 */
			is = new GribRecordIS(inStream);

			// total length of the grib file
			totalBytes = is.getGribLength();

			// Remove IS length
			totalBytes -= is.getLength();

			/**
			 * PDS
			 */
			pds = new GribRecordPDS(inStream); // read Product Definition
			// Section

			// Remove PDS length
			totalBytes -= pds.getLength();

			/**
			 * GDS
			 */
			if (pds.gdsExists()) {
				gds = GribGDSFactory.getGDS(inStream);

				// Remove GDS length
				// what remains is the length of the bds
				totalBytes -= gds.getLength();

				/**
				 * 
				 * BMS
				 * 
				 */
				final boolean BMS_exists = pds.bmsExists();

				if (BMS_exists) {
					bms = new GribRecordBMS(inStream);
				}

				/**
				 * 
				 * BDS
				 * 
				 */
				if (BMS_exists) {
					bds = new GribRecordBDS(inStream, pds.getDecimalScale(),
							gds, bms);
				} else {
					bds = new GribRecordBDS(inStream, pds.getDecimalScale(),
							gds, null);
				}

				/**
				 * 
				 * ES
				 * 
				 */
				// looking for end section in this buffer
				// we could avoid this in order to improve performances since
				// it does nothing!
				if (GribFileUtilities.seekFooter(inStream)) {
					// we got a record
					recordCount++;

					// creating a new record
					gr = new GribRecord(is, pds, gds, bds, bms);
					this.recordList.add(gr);;

				} else {
					throw new IllegalArgumentException(
							"GribFile::Parse:End of file string '7777' not found!");
				}
			} else {
				/**
				 * Not having a GDS is not an error condition, at least
				 * generally speaking, but we require it therefore we have to
				 * warn the user about the fact that it is missed. After sending
				 * the warn we just skip the remaining byte until the next grib
				 * record.
				 */
				LOGGER
						.warning("GribRecord::ParseGribFile: No GDS included, skipping this record.");

				// Skip this record
				inStream.skipBytes(totalBytes);
			}
		}

		// we did not found any GriB records in this grib file
		if (recordCount == 0) {
			throw new IOException(
					"GribFile::Parse:No grib file or non valid grib file supplied!");
		}


		// setting number of records
		this.recordCount = recordCount;
	}

	/**
	 * Get a specific GRIB record of this GRIB file as <tt>GribRecord</tt>
	 * object.
	 * 
	 * @param i
	 *            number of GRIB record, first record is number 1
	 * 
	 * @return GRIB record object
	 * 
	 * @throws NoSuchElementException
	 *             if record number does not exist
	 * @throws IndexOutOfBoundsException
	 *             if JGrib doesn't yet support the operation
	 */
	public GribRecord getRecord(int i) throws NoSuchElementException,
			IndexOutOfBoundsException {
		// check first the index
		if (i > this.recordCount) {
			throw new IndexOutOfBoundsException(
					"GribRecord::getRecord(int i):index out of bound!");
		}

		// check to see if we need to load the BDS
		return (GribRecord) this.recordList.get(i - 1);
	}

	/**
	 * Get the number of records this GRIB file contains.
	 * 
	 * @return number of records in this GRIB file
	 */
	public int getRecordCount() {
		return this.recordCount;
	}
//
//	public void setOutput(Object output) {
//		if (output == null) {
//			throw new IllegalArgumentException(
//					"GribFile::setOutput(Object output):Provided argument is null");
//		}
//
//		if (output instanceof String) {
//		}
//
//		if (output instanceof File) {
//		}
//
//		if (output instanceof RandomAccessFile) {
//		}
//
//		if (output instanceof String) {
//		}
//
//		if (output instanceof OutputStream) {
//		}
//
//		if (output instanceof ImageOutputStream) {
//		}
//
//		// if we get here throw an exception
//		throw new IllegalArgumentException(
//				"GribFile::setOutput(Object output):Provided argument is of a wrong type");
//	}

	/**
	 * writeTo
	 * 
	 * @param out
	 *            OutputStream
	 * 
	 * @throws NoSuchElementException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public void writeTo(final OutputStream out) throws NoSuchElementException,
			IOException {
		final int length = this.getRecordCount();

		for (int i = 1; i <= length; i++) {
			this.getRecord(i).writeTo(out);
		}

		out.flush();
		out.close();
	}

	/**
	 * DO NOT USE THIS ONE
	 * 
	 * @param obj
	 *            Object
	 * 
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		try {
			if (!(obj instanceof GribFile)) {
				return false;
			}

			if (this == obj) {
				// Same object
				return true;
			}

			GribFile file = (GribFile) obj;
			final int i1 = file.getRecordCount();
			final int i2 = getRecordCount();

			if (i1 != i2) {
				return false;
			}

			for (int j = 1; j <= i1; j++) {
				if (!file.getRecord(j).equals(this.getRecord(j))) {
					System.out.println("Failed comparing Record num: " + j
							+ "\n");
					System.out.println(this.getRecord(j));
					System.out.println(file.getRecord(j));
					return false;
				}
			}
		} catch (Exception e) {
			// if any exception occurs
			return false;
		}

		return true;
	}

	/**
	 * clean, cleans the GribFile from anything it contains. We might use this
	 * method to clean a file before adding new records
	 */
	public void clean() {
		if (this.recordList.size() != 0) {
			this.recordList.clear();
		}
	}

	public void addRecord(final GribRecord rec) {
		this.addRecord(rec, recordCount);
	}

	/**
	 * addRecord
	 * 
	 * @param rec
	 *            GribRecord
	 * @param pos
	 *            DOCUMENT ME!
	 * 
	 * @throws IndexOutOfBoundsException
	 *             DOCUMENT ME!
	 */
	public void addRecord(final GribRecord rec, int pos) {
		if (rec != null) {
			// we are adding a record
			if (pos < 0) {
				throw new IndexOutOfBoundsException(
						"GribFile::addRecord(GribRecord rec, int pos):index out of bound!");
			}

			this.recordList.add(pos, rec);
			recordCount++;
		}
	}

	/**
	 * This method is responsible for removing a record from the record map. A
	 * GribFile is made of a certain number of records.
	 * 
	 * @param pos
	 *            int The position of the record to be removed
	 * 
	 * @return boolean True if the record was found and remove, false otherwise.
	 */
	public boolean removeRecord(int pos) {
		if (pos > this.recordCount) {
			this.recordList.remove(pos - 1);
			return true;
		}

		return false;
	}


	/**
	 * The goal of this method is to check the provided object to see if it can
	 * be decoded as a GribFile. The avalaible options for the input object are:
	 * <li>
	 * <ul>
	 * A File object.
	 * <ul>
	 * A String object which contain a path to a file or a URL.
	 * <ul>
	 * A URL object.
	 * <ul>
	 * An object of an ImageInputStream's subclass.
	 * <li>
	 * 
	 * The check for compatibility is pretty simple at this stage in order to
	 * keep things as simple as possible. We just look for the opening string
	 * 'GRIB' and the closing string '777' which have to come in ordered pairs
	 * (see the GRIB edition 2 specification on the web).
	 * 
	 * @param input
	 *            The provided object to be checked for decodeability.
	 * @return True if decodeable, false otherwise.
	 */
	public static boolean canDecodeInput(final Object input) {
		//check input
		GribFileUtilities.ensureNotNull("input", input);
		
		int pivot = 0;
		ImageInputStream stream = null;
		
		try{
			// file
			if (input instanceof File){
				final File inputFile= (File)input;
				GribFileUtilities.checkFileReadable(inputFile);
				stream = ImageIO.createImageInputStream(inputFile);
			}
			else {
				if (input instanceof String) {
					// is it a file path?
					try {
						final File inputFile = new File((String) input);
						GribFileUtilities.checkFileReadable(inputFile);
						stream = ImageIO.createImageInputStream(inputFile);
					} catch (Throwable e) {
						// is it an url?
						final URL url2Try = new URL((String) input);
						stream = GribFileUtilities.checkURL(url2Try);
	
					}
				} else {
					// URL
					if (input instanceof URL)
						stream = GribFileUtilities.checkURL((URL) input);
					else
						if(input instanceof ImageInputStream)
							stream=(ImageInputStream) input;
					
				}
			}
			// were we able to create an input stream
			if(stream==null)
				return false;
			
			// checking if we have GRIB and 7777 for each file
			stream.mark();
			while (GribFileUtilities.seekHeader(stream)) {
				pivot++;
				if (!GribFileUtilities.seekFooter(stream))
					return false;
				pivot++;
			}
			stream.reset();
			return (pivot == 0) ? false : ((pivot % 2 != 0) ? false : true);
		}catch (Throwable e) {
			if(LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE,"Unable to decoded grib file",e);
		}
		finally{
			if(stream!=null&& !(input instanceof ImageInputStream))
				try{
					stream.close();
				}
				catch (Throwable e) {
					if(LOGGER.isLoggable(Level.FINE))
						LOGGER.log(Level.FINE,"Unable to close input stream",e);
				}
		}
	return false;

	}

	public static GribFile open(final Object source, final AccessType accessType) throws FileNotFoundException, IOException{

		
		switch (accessType) {
		case R:
			// check input source, cannot be null
			GribFileUtilities.ensureNotNull("source", source);
			if(canDecodeInput(source))
			{
				if(source instanceof File){
					return new GribFile((File)source);
				}
				if(source instanceof URL){
					return new GribFile((URL)source);
				}
				if(source instanceof String){
					return new GribFile((String)source);
				}
				if(source instanceof InputStream){
					return new GribFile((InputStream)source);
				}	
				if(source instanceof ImageInputStream){
					return new GribFile((ImageInputStream)source);
				}					
			}
			return null;
		case RW:
			return new GribFile();
		default:
			throw new UnsupportedOperationException("XXX");
		}
		
	}
	/**
	 * Release all the used resource for this grib file.
	 * 
	 */
	public void dispose() {
		clean();
		this.recordList=null;
		if(inStream!=null)
		{
			try{
				inStream.flush();
			}
			catch (Throwable e) {
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
			try {
				inStream.close();
			} catch (IOException e) {
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			}
		}


	}
	/**
	 * @return  the originatingSource
	 * @uml.property  name="originatingSource"
	 */
	public Object getOriginatingSource() {
		return originatingSource;
	}
}
