package it.geosolutions.imageio.plugins.jp2k;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to check if kakadu was loaded in this JVM.
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public class KakaduUtilities {

	private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.jp2k");
	
    private static boolean init = false;

    /** is kakadu available on this machine?. */
    private static boolean available;

    static {
        loadKakadu();
    }

    /**
     * Checks if kakadu was loaded or not.
     * 
     */
	public synchronized static void loadKakadu() {
        if (init == false)
            init = true;
        else
            return;
        try {
            System.loadLibrary("kdu_jni");
            available = true;
        } catch (UnsatisfiedLinkError e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(new StringBuffer("Native library load failed.")
                        .append(e.toString()).toString());
            available = false;
        }
		
	}
    /**
     * Returns <code>true</code> if the GDAL native library has been loaded.
     * <code>false</code> otherwise.
     * 
     * @return <code>true</code> only if the GDAL native library has been
     *         loaded.
     */
    public static boolean isKakaduAvailable() {
    	loadKakadu();
        return available;
    }
    
}
