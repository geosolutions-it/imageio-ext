package net.sourceforge.jgrib;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;


/**
 * <p>GribRecordES </p>
 *
 * <p>This class is simply an helper class to handle the end section '7777' of a grib file edition1 </p>
 *
 * <p>@author simone giannecchini </p>
 *
 * @version 1.0
 */
public final class  GribRecordES {
	
	private final int length=4;
	
	public int getLength() {
		return length;
	}

	/** Logger. */
	final static Logger LOGGER = Logger.getLogger(GribRecordES.class
			.toString());
	
    /**Serialization of BDS section with emphasis on data packing.
     *
     * @param out OutputStream
     */
    public void writeTo(final OutputStream out)
        throws IOException {
        //writes '7777' to the specified output
        out.write(new byte[] { 55, 55, 55, 55 });
    }
}
