/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.util;

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReadParam;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

import kdu_jni.Jp2_channels;
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

    public static final double DOUBLE_TOLERANCE = 1E-6;

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.util");

    /** is Kakadu available on this machine?. */
    private static boolean available;

    private static boolean init = false;

    public static final double BIT_TO_BYTE_FACTOR = 0.125;

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
        for (int level = 0; level < levels + 1; level++) {
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

        AffineTransform at = new AffineTransform(destinationWidth / (float) bi.getWidth(), 0,
                0, destinationHeight / (float) bi.getHeight(), 0, 0);
        int interpolation = interpolationType == JP2KKakaduImageReadParam.INTERPOLATION_NEAREST
                ? AffineTransformOp.TYPE_NEAREST_NEIGHBOR : AffineTransformOp.TYPE_BILINEAR;
        AffineTransformOp op = new AffineTransformOp(at, interpolation);
        return op.filter(bi, null);
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
            String nativeVersion = getKakaduNativeLibVersion();
            String jniVersion = getKakaduJniLibVersion();
            if (jniVersion.equalsIgnoreCase(nativeVersion)) {
                LOGGER.info(String.format("Kakadu native library: %s, JNI library: %s", nativeVersion, jniVersion));
            } else {
                LOGGER.warning(String.format("Kakadu JNI/native version mismatch: native library: %s, JNI library: %s",
                        nativeVersion, jniVersion));
            }
        } catch (UnsatisfiedLinkError e) {
            if (LOGGER.isLoggable(Level.WARNING)){
            	 LOGGER.warning("Failed to load the Kakadu native libs. This is not a problem unless you need to use the Kakadu plugin: it won't be enabled. " + e.toString());
            }
            available = false;
        }
    }
    
    /**
     * Compute the source region and destination dimensions taking any parameter
     * settings into account.
     */
    public static void computeRegions(final Rectangle sourceBounds,
            Dimension destSize, ImageWriteParam param) {
        int periodX = 1;
        int periodY = 1;
        if (param != null) {
            final int[] sourceBands = param.getSourceBands();
            if (sourceBands != null
                    && (sourceBands.length != 1 || sourceBands[0] != 0)) {
                throw new IllegalArgumentException("Cannot sub-band image!");
                // TODO: Actually, sourceBands is ignored!!
            }

            // ////////////////////////////////////////////////////////////////
            //
            // Get source region and subsampling settings
            //
            // ////////////////////////////////////////////////////////////////
            Rectangle sourceRegion = param.getSourceRegion();
            if (sourceRegion != null) {
                // Clip to actual image bounds
                sourceRegion = sourceRegion.intersection(sourceBounds);
                sourceBounds.setBounds(sourceRegion);
            }

            // Get subsampling factors
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();

            // Adjust for subsampling offsets
            int gridX = param.getSubsamplingXOffset();
            int gridY = param.getSubsamplingYOffset();
            sourceBounds.x += gridX;
            sourceBounds.y += gridY;
            sourceBounds.width -= gridX;
            sourceBounds.height -= gridY;
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // Compute output dimensions
        //
        // ////////////////////////////////////////////////////////////////////
        destSize.setSize((sourceBounds.width + periodX - 1) / periodX,
                (sourceBounds.height + periodY - 1) / periodY);
        if (destSize.width <= 0 || destSize.height <= 0) {
            throw new IllegalArgumentException("Empty source region!");
        }
    }

    public static boolean notEqual(double value, double reference) {
        return (Math.abs(value - reference) > KakaduUtilities.DOUBLE_TOLERANCE); 
    }

    /**
     * Helper that reads a "terminated" string applying some work arounds for invalid data.
     * Assumes all the bytes should be used, skips the eventual last terminator, and turns
     * all ones in the middle in newlines, see also https://trac.osgeo.org/gdal/ticket/5760
     *
     * @param contents
     * @return
     */
    public static String readTerminatedString(byte[] contents) {
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < contents.length; i++) {
            byte c = contents[i];
            if ((c == 0 || c == -1)) {
                if (i == contents.length - 1) {
                    break; 
                } else {
                    c = '\n';
                }
            }
            builder.append((char) c);
        }

        return builder.toString();
    }

    /**
     * @return version returned by JNI Kakadu library in the format
     *         {@code v<major>.<minor>.<patch>}, should not differ from the
     *         {@link #getKakaduNativeLibVersion() native library version} to ensure
     *         compatibility between the java bindings and the native library
     */
    public static String getKakaduJniLibVersion() {
        String v = Kdu_global.KDU_CORE_VERSION;
        return v;
    }

    /**
     * @return Major version returned by the JNI Kakadu library (e.g. from
     *         {@code v7.10.6} returns {@code 7})
     */
    public static int getKakaduJniMajorVersion() {
        String versionString = getKakaduJniLibVersion();
        return parseMajorVersion(versionString);
    }

    /**
     * @return version returned by the native Kakadu library in the format
     *         {@code v<major>.<minor>.<patch>}, should not differ from the
     *         {@link #getKakaduJniLibVersion() JNI library version} to ensure
     *         compatibility between the java bindings and the native library
     */
    public static String getKakaduNativeLibVersion() {
        String kduCoreVersion;
        try {
            kduCoreVersion = Kdu_global.Kdu_get_core_version();
        } catch (KduException e) {
            throw new RuntimeException("Error querying kakadu version ", e);
        }
        return kduCoreVersion;
    }
    
    /**
     * Gets the major version from a kakadu version string (e.g. from
     * {@code v7.10.6} returns {@code 7})
     */
    private static int parseMajorVersion(String versionString) {
        String majorString = versionString.substring(0, versionString.indexOf('.'));
        majorString = majorString.replaceAll("v", "");
        int majorVersion = Integer.parseInt(majorString);
        return majorVersion;
    }

    /**
     * Prepares the channel for RGB writing handling the binary incompatibility
     * between versions of the JNI library in
     * {@link Jp2_channels#Set_colour_mapping}.
     * <p>
     * {@link Jp2_channels#Set_colour_mapping} in library version up to {@code v7.x}
     * has three arguments
     * ({@code int _colour_idx, int _codestream_component, int _lut_idx}). From
     * {@code v7.x} this method has two additional arguments:
     * ({@code int _codestream_idx, int _data_format}).
     * <p>
     * So far that's the only binary incompatibility found when dealing with Kakadu
     * prior to 7.x and 7.x+. When/if more incompatibilities are found (probably
     * with newer library versions in the future), a different approach may be
     * needed to handle them.
     * <p>
     * For the time being, this method is used by {@link JP2KKakaduImageWriter}, and
     * uses reflection to invoke {@code Jp2_channels.Set_colour_mapping(...)} with
     * the correct number of arguments based on the
     * {@link #getKakaduJniMajorVersion() major version} of the JNI library being
     * used.
     * 
     * @param channels the channels object to initialize for RGB writing
     * @throws KduException     propagated from the invocation of
     *                          {@link Jp2_channels#Set_colour_mapping}
     * @throws RuntimeException if an error occurs reflectivelly acquiring the
     *                          {@code Set_colour_mapping} method or invoking it.
     */
    public static void initializeRGBChannels(Jp2_channels channels) throws KduException {
        final int numColours = 3;
        channels.Init(numColours);
        // channels.Set_colour_mapping(0, 0, 0);
        // channels.Set_colour_mapping(1, 0, 1);
        // channels.Set_colour_mapping(2, 0, 2);
        Method setColourMapping;
        Object[] args1;
        Object[] args2;
        Object[] args3;
        final int major = getKakaduJniMajorVersion();
        try {
            if (major < 7) {
                setColourMapping = Jp2_channels.class.getMethod("Set_colour_mapping", int.class, int.class, int.class);
                //int _colour_idx, int _codestream_component, int _lut_idx
                args1 = new Object[] { 0, 0, 0 };
                args2 = new Object[] { 1, 0, 1 };
                args3 = new Object[] { 2, 0, 2 };
            } else {
                setColourMapping = Jp2_channels.class.getMethod("Set_colour_mapping", int.class, int.class, int.class,
                        int.class, int.class);
               //int _colour_idx, int _codestream_component, int _lut_idx, int _codestream_idx, int _data_format
                final int _codestream_idx = 0;
                final int _data_format = 0;
                args1 = new Object[] { 0, 0, 0, _codestream_idx, _data_format };
                args2 = new Object[] { 1, 0, 1, _codestream_idx, _data_format };
                args3 = new Object[] { 2, 0, 2, _codestream_idx, _data_format };
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Unable to acquire method Jp2_channels.Set_colour_mapping reflectively", e);
        }
        try {
            setColourMapping.invoke(channels, args1);
            setColourMapping.invoke(channels, args2);
            setColourMapping.invoke(channels, args3);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Error calling Jp2_channels.Set_colour_mapping(...)", e);
        }
    }
}
