/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.util;

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReadParam;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

import kdu_jni.KduException;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_message_formatter;

/**
 * Class with utility methods.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class KakaduUtilities {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.util");

    /** is Kakadu available on this machine?. */
    private static boolean available;

    private static boolean init = false;

    private KakaduUtilities() {

    }

    /**
     * Find the optimal subsampling factor, given a specified subsampling factor
     * as input parameter, as well as the number of DWT levels which may be
     * discarded. Let iSS be the input subsampling factor and let L be the
     * number of available source DWT levels.
     * 
     * The optimal subsampling factor is oSS = 2^level, where: level is not
     * greater than L, and oSS is not greater than iSS.
     * 
     * @param sourceDWTLevels
     *                the number of DWT levels in the source image
     * @param newSubSamplingFactor
     *                the specified subsampling factor for which we need to find
     *                an optimal subsampling factor
     * @return an int array containing the optimalSubSamplingFactor as first
     *         element, and the number of levels to be discarded as second
     *         element
     */
    public static int[] findOptimalResolutionInfo(final int sourceDWTLevels,
            final int newSubSamplingFactor) {
        // Within a loop, using a local variable instead of an instance field
        // is preferred to improve performances.
        final int levels = sourceDWTLevels;
        int optimalSubSamplingFactor = 1;

        // finding the available subsampling factors from the number of
        // resolution levels
        int discardLevels = 0;
        for (int level = 0; level < levels; level++) {
            // double the subSamplingFactor until it is lower than the
            // input subSamplingFactor
            if (optimalSubSamplingFactor < newSubSamplingFactor)
                optimalSubSamplingFactor = 1 << level;
            // if the calculated subSamplingFactor is greater than the input
            // subSamplingFactor, we need to step back by halving it.
            else if (optimalSubSamplingFactor > newSubSamplingFactor) {
                optimalSubSamplingFactor = optimalSubSamplingFactor >> 1;
                break;
            } else if (optimalSubSamplingFactor == newSubSamplingFactor) {
                break;
            }
        }
        int decreasingSSF = optimalSubSamplingFactor;
        for (discardLevels = 0; discardLevels < levels && decreasingSSF > 1; discardLevels++) {
            decreasingSSF = decreasingSSF >> 1;
        }

        return new int[] { optimalSubSamplingFactor, discardLevels };
    }

    /**
     * Initializing kakadu messages as stated in the KduRender.java example
     */
    public static void initializeKakaduMessagesManagement() {
        try {

            // ////
            // Customize error and warning services
            // ////

            // Non-throwing message printer
            Kdu_sysout_message sysout = new Kdu_sysout_message(false);

            // Exception-throwing message printer
            Kdu_sysout_message syserr = new Kdu_sysout_message(true);

            // /////
            // Initialize formatted message printer
            // ////

            // Non-throwing printer
            Kdu_message_formatter pretty_sysout = new Kdu_message_formatter(
                    sysout);
            // Throwing printer
            Kdu_message_formatter pretty_syserr = new Kdu_message_formatter(
                    syserr);
            Kdu_global.Kdu_customize_warnings(pretty_sysout);
            Kdu_global.Kdu_customize_errors(pretty_syserr);

        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during creation of key objects! ",
                    e);
        }
    }

    public static List<ImageReaderWriterSpi> getJDKImageReaderWriterSPI(
            ServiceRegistry registry, String formatName, boolean isReader) {

        if (registry == null || !(registry instanceof IIORegistry))
            throw new IllegalArgumentException("Illegal registry provided");

        IIORegistry iioRegistry = (IIORegistry) registry;
        Class<? extends ImageReaderWriterSpi> spiClass;
        if (isReader)
            spiClass = ImageReaderSpi.class;
        else
            spiClass = ImageWriterSpi.class;

        final Iterator<? extends ImageReaderWriterSpi> iter = iioRegistry
                .getServiceProviders(spiClass, true); // useOrdering
        final ArrayList<ImageReaderWriterSpi> list = new ArrayList<ImageReaderWriterSpi>();
        while (iter.hasNext()) {
            final ImageReaderWriterSpi provider = (ImageReaderWriterSpi) iter
                    .next();

            // Get the formatNames supported by this Spi
            final String[] formatNames = provider.getFormatNames();
            for (int i = 0; i < formatNames.length; i++) {
                if (formatNames[i].equalsIgnoreCase(formatName)) {
                    // Must be a JDK provided ImageReader/ImageWriter
                    list.add(provider);
                    break;
                }
            }
        }
        return list;
    }

    /**
     * Transforms the provided <code>BufferedImage</code> and returns a new
     * one in compliance with the required destination bimage properties,
     * adopting the specified interpolation algorithm
     * 
     * @param cm
     *                the <code>ColorModel</code> to be used in the warping
     * @param bi
     *                the original BufferedImage
     * @param destinationRegion.width
     *                the required destination image width
     * @param destinationRegion.height
     *                the required destination image height
     * @param interpolationType
     *                the specified interpolation type
     * @return a <code>BufferedImage</code> having size =
     *         destinationRegion.width*destinationRegion.height which is the
     *         result of the WarpAffineresu.
     */
    public static BufferedImage subsampleImage(ColorModel cm, BufferedImage bi,
            final int destinationWidth, final int destinationHeight,
            final int interpolationType) {

        final WritableRaster raster = cm.createCompatibleWritableRaster(
                destinationWidth, destinationHeight);

        final BufferedImage finalImage = new BufferedImage(cm, raster, false,
                null);
        final Graphics2D gc2D = finalImage.createGraphics();
        gc2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        gc2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        gc2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        gc2D
                .setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        interpolationType == JP2KKakaduImageReadParam.INTERPOLATION_NEAREST ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                                : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gc2D.drawImage(bi, 0, 0, destinationWidth, destinationHeight, 0, 0, bi
                .getWidth(), bi.getHeight(), null);
        gc2D.dispose();
        bi.flush();
        bi = null;

        return finalImage;
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

    /**
     * Forces loading of Kakadu libs.
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
}
