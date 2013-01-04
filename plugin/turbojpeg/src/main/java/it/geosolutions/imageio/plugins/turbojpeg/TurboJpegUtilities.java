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

import org.libjpegturbo.turbojpeg.TJ;

/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 * 
 *         Class containing some methods ported from the TurboJPEG C code as well as lib availability check.
 * 
 */
public class TurboJpegUtilities {

    private static final Logger LOGGER = Logger.getLogger(TurboJpegUtilities.class.getName());

    public static final String FLAGS_PROPERTY = "it.geosolutions.imageio.plugins.turbojpeg.flags";

    private static boolean isAvailable;

    private static boolean isInitialized = false;

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
                load();
                isAvailable = true;

            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("Failed to load the TurboJpeg native libs."
                            + " This is not a problem, but the TurboJpeg encoder won't be available: " + t.toString());
                }
            } finally {
                isInitialized = true;
            }
        }

    }

    public static final String LIBNAME = "turbojpeg";
    
    static void load() {
        try {            
            System.loadLibrary(LIBNAME); // If this method is called more than once with the same library name, the second and subsequent calls are ignored.
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("TurboJPEG library loaded ("+LIBNAME+")");
            }
        } catch (java.lang.UnsatisfiedLinkError e) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.indexOf("mac") >= 0) {
                System.load("/usr/lib/libturbojpeg.jnilib");
            } else {
                throw e;
            }
        }
    }

    public static int getTurboJpegFlag(final String key) {
        if (key != null) {
            if (key.equalsIgnoreCase("FLAG_ACCURATEDCT")) {
                return TJ.FLAG_ACCURATEDCT;
            } else if (key.equalsIgnoreCase("FLAG_BOTTOMUP")) {
                return TJ.FLAG_BOTTOMUP;
            } else if (key.equalsIgnoreCase("FLAG_FASTDCT")) {
                return TJ.FLAG_FASTDCT;
            } else if (key.equalsIgnoreCase("FLAG_FASTUPSAMPLE")) {
                return TJ.FLAG_FASTUPSAMPLE;
            } else if (key.equalsIgnoreCase("FLAG_FORCEMMX")) {
                return TJ.FLAG_FORCEMMX;
            } else if (key.equalsIgnoreCase("FLAG_FORCESSE")) {
                return TJ.FLAG_FORCESSE;
            } else if (key.equalsIgnoreCase("FLAG_FORCESSE2")) {
                return TJ.FLAG_FORCESSE2;
            } else if (key.equalsIgnoreCase("FLAG_FORCESSE3")) {
                return TJ.FLAG_FORCESSE3;
            }
        }
        throw new IllegalArgumentException("Unsupported flag");
    }

    public static String getTurboJpegFlagAsString(final int key) {
        switch (key) {
        case TJ.FLAG_ACCURATEDCT:
            return "FLAG_ACCURATEDCT";
        case TJ.FLAG_BOTTOMUP:
            return "FLAG_BOTTOMUP";
        case TJ.FLAG_FASTDCT:
            return "FLAG_FASTDCT";
        case TJ.FLAG_FASTUPSAMPLE:
            return "FLAG_FASTUPSAMPLE";
        case TJ.FLAG_FORCEMMX:
            return "FLAG_FORCEMMX";
        case TJ.FLAG_FORCESSE:
            return "FLAG_FORCESSE";
        case TJ.FLAG_FORCESSE2:
            return "FLAG_FORCESSE2";
        case TJ.FLAG_FORCESSE3:
            return "FLAG_FORCESSE3";
        }
        throw new IllegalArgumentException("Unsupported flag");
    }
}
