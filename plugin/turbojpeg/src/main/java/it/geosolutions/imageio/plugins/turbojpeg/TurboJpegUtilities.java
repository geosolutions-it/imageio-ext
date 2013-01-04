/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageio.plugins.turbojpeg;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bridj.BridJ;

/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 * 
 *         Class containing some methods ported from the TurboJPEG C code as well as lib availability check.
 * 
 */
public class TurboJpegUtilities {
    
    private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.turbojpeg.TurboJpegUtilities");
    
    private static boolean isAvailable;
    
    private static boolean isInitialized;
    
    public static boolean isTurboJpegAvailable() {
        loadTurboJpeg();
        return isAvailable;
    }
    
    public static void loadTurboJpeg() {
        if (isInitialized) {
            return;
        }
        synchronized (LOGGER) {
            if (isInitialized) {
                return;
            }
            try {
                isAvailable = BridJ.getNativeLibrary("turbojpeg") != null;
                
            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.WARNING)){
                    LOGGER.warning("Failed to load the TurboJpeg native libs. This is not a problem unless you need to " +
                    		"use the TurboJpeg encoder: it won't be available" + t.toString());
                }
            } finally {
                isInitialized = true;
            }
        }

    }
    
//    static {
//        loadTurboJpeg();
//    }
    
    /** --------------------------------------------
     *  Constants and method ported from the C code
     *  --------------------------------------------
     */
    static final int H_SAMP_FACTOR[] = new int[] { 1, 2, 2, 1 };

    static final int V_SAMP_FACTOR[] = new int[] { 1, 1, 2, 1 };

    static final int PIXEL_SIZE[] = new int[] { 3, 3, 3, 1 };
    
    public static int bufSizeYuv(int width, int height, int jpegSubsamp) {
        int retval = 0;
        int pw = pad(width, H_SAMP_FACTOR[jpegSubsamp]);
        int ph = pad(height, V_SAMP_FACTOR[jpegSubsamp]);
        int cw = pw / H_SAMP_FACTOR[jpegSubsamp];
        int ch = ph / V_SAMP_FACTOR[jpegSubsamp];
        retval = pad(pw, 4) * ph + (jpegSubsamp == TurboJpegLibrary.TJ_GRAYSCALE ? 0 : pad(cw, 4) * ch * 2);
        // Extra size added. got some VM crashes on very small images without some extra space
        // Moreover, on c code available on trunk, this extra space has been added too.
        return retval + 2048; 
    }

    public static int bufSize(final int width, final int height) {
        // Note the "+ 2048" at the end of the computation. It has been copied from the original
        // c code to fix a potential issue. It could be the same we are encountering in the
        // Yuv version.
        return ((width + 15) & (~15)) * ((height + 15) & (~15)) * 6 + 2048; 
    }

    public final static int pad(int v, int p) {
        return ((v + (p) - 1) & (~((p) - 1)));
    }

}
