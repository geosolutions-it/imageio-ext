package net.sourceforge.jgrib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

class GribFileUtilities {

	/** Logger. */
	private final static Logger LOGGER = Logger.getLogger(GribFileUtilities.class.toString());
	
	static void checkFileReadable(final File file){
		ensureNotNull("file", file);
		if(!(file.isFile()&&file.canRead()&&file.exists()))
			// build message for file
			throw new IllegalArgumentException(createFileDescription(file));
		
	}

	/**
	 * @param file
	 * @return
	 */
	static String createFileDescription(final File file) {
		final StringBuilder builder= new StringBuilder();
		builder.append("Invalid input file").append("\n");
		builder.append("absolutePath:").append(file.getAbsolutePath());
		builder.append("canRead:").append(file.canRead());
		builder.append("isFile:").append(file.isFile());
		builder.append("isHidden:").append(file.isHidden());
		return builder.toString();
	}

	static void ensureNotNull(final String name, final Object object){
		if(object==null)
			throw new NullPointerException(name+ " cannot be null");
	}

	/**
	 * This is a small utility method used to build an ImageInputStream from a
	 * URL.
	 * 
	 * @param url
	 *            URL to be used in order to build an ImageInputStream.
	 * @return The built ImageInputStream.
	 * @throws IOException
	 *             In case an IOError occurs.
	 * @throws UnsupportedEncodingException
	 *             In case hte URL is encoded with an unsupported encoding.
	 * @throws FileNotFoundException
	 *             In case the url points to a file but the provided path is
	 *             invalid.
	 */
	static ImageInputStream checkURL(final URL url)
			throws FileNotFoundException, UnsupportedEncodingException,
			IOException {
		ensureNotNull("url", url);
		final String protocol = url.getProtocol();
		if (protocol.equalsIgnoreCase("file"))
			return new FileImageInputStream(new File(URLDecoder.decode(url.getFile(), "utf8")));
		else if (protocol.equalsIgnoreCase("http"))
			return new MemoryCacheImageInputStream(url.openStream());
		return null;
	}

	/**
	 * This method is responsible for seeking the header of a Grib record which
	 * starts with the string 'GRIB'.
	 * 
	 * @param in
	 *            Image input stream to read from.
	 * 
	 * @return True when an header is found, false otherwise.
	 */
	static boolean seekHeader(final ImageInputStream in) {
		ensureNotNull("in", in);
		
		// seek header
		int iterations = 0;
		try {
			int ui8 = 0;
			while (true) {
				// code must be "G" "R" "I" "B"
				ui8 = in.read();
				iterations++;
	
				// EOF?
				if (ui8 == -1) {
					break;
				}
	
				if ((ui8 == 'G') && (in.read() == 'R') && (in.read() == 'I')
						&& (in.read() == 'B')) {
					return true;
				}
				iterations += 3;
	
				// did we reach the maximum number of iterations.
				if (iterations >= GribRecord.MAXIMUM_SEARCH_SIZE)
					return false;
			}
		} catch (IOException ioe) {
			if(LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE,"Unable to seek ",ioe);
		}
	
		return false;
	}

	/**seekFooter, seeks the footer section of a grib file.
	 *
	 * Well, this method should be self explaining therefore I won't
	 * talk much abouot it!
	 *
	 * @param in BitInputStream
	 * @return boolean
	 */
	static boolean seekFooter(final ImageInputStream in) {
	    // seek header
	    int iterations = 0;
	    try {
	    	int ui8 =0;
	    	
	        while (true) {
	        	ui8=in.read();
	            iterations++;
	        	if(ui8<0){
	        		GribRecordES.LOGGER.warning("Found EOF while lookig for End Section marker");
	        		return true;
	        	}            		
	            // code must be 7 7 7 7 for End Section
	            if ((ui8 == 55) && (in.read() == 55) && (in.read() == 55)
	                && (in.read() == 55)) {
	                return true;
	            }
	
	
				if ((ui8 == 'G') && (in.read() == 'R') && (in.read() == 'I')
						&& (in.read() == 'B')) {
					return false;
				}
				iterations += 3;
	
				// did we reach the maximum number of iterations.
				if (iterations >= GribRecord.MAXIMUM_SEARCH_SIZE)
					return false;
	        }
	    }
	    catch (IOException ioe) {
	        // do nothing
	    }
	
	    return false;
	}

	/**
	 * Count the number of set bits in an int;
	 * 
	 * @param x
	 *            the int to have its bits counted
	 * @author Tim Tyler tt@iname.com
	 * @returns the number of bits set in x
	 */
	static int bitCount(int x) {
		int temp;
	
		temp = 0x55555555;
		x = (x & temp) + (x >>> 1 & temp);
		temp = 0x33333333;
		x = (x & temp) + (x >>> 2 & temp);
		temp = 0x07070707;
		x = (x & temp) + (x >>> 4 & temp);
		temp = 0x000F000F;
		x = (x & temp) + (x >>> 8 & temp);
	
		return (x & 0x1F) + (x >>> 16);
	}

}
