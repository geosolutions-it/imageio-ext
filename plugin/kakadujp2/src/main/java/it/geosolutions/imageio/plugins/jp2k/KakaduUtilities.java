/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2k");

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
     * Returns <code>true</code> if the Kakadu native library has been loaded.
     * <code>false</code> otherwise.
     * 
     * @return <code>true</code> only if the Kakadu native library has been
     *         loaded.
     */
    public static boolean isKakaduAvailable() {
        loadKakadu();
        return available;
    }
}
