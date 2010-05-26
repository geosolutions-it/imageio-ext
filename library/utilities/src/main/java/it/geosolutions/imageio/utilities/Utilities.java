/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le D�veloppement
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
package it.geosolutions.imageio.utilities;

import java.io.File;
import java.net.URL;

import javax.media.jai.JAI;

/**
 * Simple class for utility methods.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Martin Desruisseaux
 * 
 */
public final class Utilities {

	/**
	 * @deprecated Use {@link ImageIOUtilities#MAX_SUBSAMPLING_FACTOR} instead
	 */
	private static final int MAX_SUBSAMPLING_FACTOR = ImageIOUtilities.MAX_SUBSAMPLING_FACTOR;
	
	/**
	 * @deprecated Use {@link ImageIOUtilities#MAX_LEVELS} instead
	 */
	private static final int MAX_LEVELS = ImageIOUtilities.MAX_LEVELS;
	
    private Utilities() {

    }

    /**
	 * An array of strings containing only white spaces. Strings' lengths are
	 * equal to their index + 1 in the {@code spacesFactory} array. For example,
	 * {@code spacesFactory[4]} contains a string of length 5. Strings are
	 * constructed only when first needed.
	 * @deprecated Use {@link ImageIOUtilities#spacesFactory} instead
	 */
	private static final String[] spacesFactory = ImageIOUtilities.spacesFactory;

    /**
	 * Convenience method for testing two objects for equality. One or both
	 * objects may be null.
	 * @deprecated Use {@link ImageIOUtilities#equals(Object,Object)} instead
	 */
	public static boolean equals(final Object object1, final Object object2) {
		return ImageIOUtilities.equals(object1, object2);
	}

    /**
	 * Returns {@code true} if the two specified objects implements exactly the
	 * same set of interfaces. Only interfaces assignable to {@code base} are
	 * compared. Declaration order doesn't matter. For example in ISO 19111,
	 * different interfaces exist for different coordinate system geometries ({@code CartesianCS},
	 * {@code PolarCS}, etc.).
	 * @deprecated Use {@link ImageIOUtilities#sameInterfaces(Class<?>,Class<?>,Class<?>)} instead
	 */
	public static boolean sameInterfaces(final Class<?> object1,
	        final Class<?> object2, final Class<?> base) {
				return ImageIOUtilities.sameInterfaces(object1, object2, base);
			}

    /**
	 * Returns a string of the specified length filled with white spaces. This
	 * method tries to return a pre-allocated string if possible.
	 * 
	 * @param length
	 *                The string length. Negative values are clamped to 0.
	 * @return A string of length {@code length} filled with white spaces.
	 * @deprecated Use {@link ImageIOUtilities#spaces(int)} instead
	 */
	public static String spaces(int length) {
		return ImageIOUtilities.spaces(length);
	}

    /**
	 * Returns a short class name for the specified class. This method will omit
	 * the package name. For example, it will return "String" instead of
	 * "java.lang.String" for a {@link String} object. It will also name array
	 * according Java language usage, for example "double[]" instead of "[D".
	 * 
	 * @param classe
	 *                The object class (may be {@code null}).
	 * @return A short class name for the specified object.
	 * 
	 * @todo Consider replacing by {@link Class#getSimpleName} when we will be
	 *       allowed to compile for J2SE 1.5.
	 * @deprecated Use {@link ImageIOUtilities#getShortName(Class<?>)} instead
	 */
	public static String getShortName(Class<?> classe) {
		return ImageIOUtilities.getShortName(classe);
	}

    /**
	 * Takes a URL and converts it to a File. The attempts to deal with 
	 * Windows UNC format specific problems, specifically files located
	 * on network shares and different drives.
	 * 
	 * If the URL.getAuthority() returns null or is empty, then only the
	 * url's path property is used to construct the file. Otherwise, the
	 * authority is prefixed before the path.
	 * 
	 * It is assumed that url.getProtocol returns "file".
	 * 
	 * Authority is the drive or network share the file is located on.
	 * Such as "C:", "E:", "\\fooServer"
	 * 
	 * @param url a URL object that uses protocol "file"
	 * @return a File that corresponds to the URL's location
	 * @deprecated Use {@link ImageIOUtilities#urlToFile(URL)} instead
	 */
	public static File urlToFile(URL url) {
		return ImageIOUtilities.urlToFile(url);
	}
    
    /**
	 * Given a pair of xSubsamplingFactor (xSSF) and ySubsamplingFactor (ySFF), 
	 * look for a subsampling factor (SSF) in case xSSF != ySSF or they are not
	 * powers of 2.
	 * In case xSSF == ySSF == 2^N, the method return 0 (No optimal subsampling factor found).
	 * 
	 * @param xSubsamplingFactor
	 * @param ySubsamplingFactor
	 * @return 
	 * @deprecated Use {@link ImageIOUtilities#getSubSamplingFactor2(int,int)} instead
	 */
	public static int getSubSamplingFactor2(final int xSubsamplingFactor, final int ySubsamplingFactor) {
		return ImageIOUtilities.getSubSamplingFactor2(xSubsamplingFactor,
				ySubsamplingFactor);
	}
	
	/**
	 * @deprecated Use {@link ImageIOUtilities#findOptimalSubSampling(int)} instead
	 */
	private static int findOptimalSubSampling(final int newSubSamplingFactor) {
		return ImageIOUtilities.findOptimalSubSampling(newSubSamplingFactor);
	}
    
    /**
	 * Returns a short class name for the specified object. This method will
	 * omit the package name. For example, it will return "String" instead of
	 * "java.lang.String" for a {@link String} object.
	 * 
	 * @param object
	 *                The object (may be {@code null}).
	 * @return A short class name for the specified object.
	 * @deprecated Use {@link ImageIOUtilities#getShortClassName(Object)} instead
	 */
	public static String getShortClassName(final Object object) {
		return ImageIOUtilities.getShortClassName(object);
	}
    
    /**
	 * @deprecated Use {@link ImageIOUtilities#adjustAttributeName(String)} instead
	 */
	public static String adjustAttributeName(final String attributeName){
		return ImageIOUtilities.adjustAttributeName(attributeName);
	}
    
    /**
	 * Allows or disallow native acceleration for the specified operation on the given JAI instance.
	 * By default, JAI uses hardware accelerated methods when available. For example, it make use of
	 * MMX instructions on Intel processors. Unluckily, some native method crash the Java Virtual
	 * Machine under some circumstances. For example on JAI 1.1.2, the {@code "Affine"} operation on
	 * an image with float data type, bilinear interpolation and an {@link javax.media.jai.ImageLayout}
	 * rendering hint cause an exception in medialib native code. Disabling the native acceleration
	 * (i.e using the pure Java version) is a convenient workaround until Sun fix the bug.
	 * <p>
	 * <strong>Implementation note:</strong> the current implementation assumes that factories for
	 * native implementations are declared in the {@code com.sun.media.jai.mlib} package, while
	 * factories for pure java implementations are declared in the {@code com.sun.media.jai.opimage}
	 * package. It work for Sun's 1.1.2 implementation, but may change in future versions. If this
	 * method doesn't recognize the package, it does nothing.
	 *
	 * @param operation The operation name (e.g. {@code "Affine"}).
	 * @param allowed {@code false} to disallow native acceleration.
	 * @param jai The instance of {@link JAI} we are going to work on. This argument can be
	 *        omitted for the {@linkplain JAI#getDefaultInstance default JAI instance}.
	 *
	 * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854">JAI bug report 4906854</a>
	 * @deprecated Use {@link ImageIOUtilities#setNativeAccelerationAllowed(String,boolean,JAI)} instead
	 */
	public synchronized static void setNativeAccelerationAllowed(final String operation,
	                                                             final boolean  allowed,
	                                                             final JAI jai)
	{
		ImageIOUtilities.setNativeAccelerationAllowed(operation, allowed, jai);
	}

    /**
	 * Allows or disallow native acceleration for the specified operation on the
	 * {@linkplain JAI#getDefaultInstance default JAI instance}. This method is
	 * a shortcut for <code>{@linkplain #setNativeAccelerationAllowed(String,boolean,JAI)
	 * setNativeAccelerationAllowed}(operation, allowed, JAI.getDefaultInstance())</code>.
	 *
	 * @see #setNativeAccelerationAllowed(String, boolean, JAI)
	 * @deprecated Use {@link ImageIOUtilities#setNativeAccelerationAllowed(String,boolean)} instead
	 */
	public static void setNativeAccelerationAllowed(final String operation, final boolean allowed) {
		ImageIOUtilities.setNativeAccelerationAllowed(operation, allowed);
	}
    
    /**
	 * @deprecated Use {@link ImageIOUtilities#checkNotNull(Object,String)} instead
	 */
	public final static void checkNotNull (final Object checkMe, final String message){
		ImageIOUtilities.checkNotNull(checkMe, message);
	}
}
