/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
package it.geosolutions.resources;

// Collections and arrays
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * A set of miscellaneous methods.
 *
 * @since 2.0
 * @source $URL: http://svn.geotools.org/geotools/branches/2.3.x/module/referencing/src/org/geotools/resources/Utilities.java $
 * @version $Id: Utilities.java 1041 2007-01-22 16:59:43Z simboss $
 * @author Martin Desruisseaux
 */
public final class Utilities {
    /**
     * An array of strings containing only white spaces. Strings' lengths are equal to their
     * index + 1 in the {@code spacesFactory} array. For example, {@code spacesFactory[4]}
     * contains a string of length 5. Strings are constructed only when first needed.
     */
    private static final String[] spacesFactory = new String[20];

    /**
     * Forbid object creation.
     */
    private Utilities() {
    }

    /**
     * Convenience method for testing two objects for equality. One or both objects may be null.
     */
    public static boolean equals(final Object object1, final Object object2) {
        return (object1==object2) || (object1!=null && object1.equals(object2));
    }

    /**
     * Returns {@code true} if the two specified objects implements exactly the same set of
     * interfaces. Only interfaces assignable to {@code base} are compared. Declaration order
     * doesn't matter. For example in ISO 19111, different interfaces exist for different coordinate
     * system geometries ({@code CartesianCS}, {@code PolarCS}, etc.). We can check if two
     * CS implementations has the same geometry with the following code:
     *
     * <blockquote><code>
     * if (sameInterfaces(cs1, cs2, {@linkplain org.opengis.referencing.cs.CoordinateSystem}.class))
     * </code></blockquote>
     */
    public static boolean sameInterfaces(final Class object1,
                                         final Class object2,
                                         final Class  base)
    {
        if (object1 == object2) {
            return true;
        }
        if (object1==null || object2==null) {
            return false;
        }
        final Class[] c1 = object1.getInterfaces();
        final Class[] c2 = object2.getInterfaces();
        /*
         * Trim all interfaces that are not assignable to 'base' in the 'c2' array.
         * Doing this once will avoid to redo the same test many time in the inner
         * loops j=[0..n].
         */
        int n = 0;
        for (int i=0; i<c2.length; i++) {
            final Class c = c2[i];
            if (base.isAssignableFrom(c)) {
                c2[n++] = c;
            }
        }
        /*
         * For each interface assignable to 'base' in the 'c1' array, check if
         * this interface exists also in the 'c2' array. Order doesn't matter.
         */
compare:for (int i=0; i<c1.length; i++) {
            final Class c = c1[i];
            if (base.isAssignableFrom(c)) {
                for (int j=0; j<n; j++) {
                    if (c.equals(c2[j])) {
                        System.arraycopy(c2, j+1, c2, j, --n-j);
                        continue compare;
                    }
                }
                return false; // Interface not found in 'c2'.
            }
        }
        return n==0; // If n>0, at least one interface was not found in 'c1'.
    }

    /**
     * Returns a string of the specified length filled with white spaces.
     * This method tries to return a pre-allocated string if possible.
     *
     * @param  length The string length. Negative values are clamped to 0.
     * @return A string of length {@code length} filled with white spaces.
     */
    public static String spaces(int length) {
        // No need to synchronize.  In the unlikely event of two threads
        // calling this method at the same time and the two calls creating a
        // new string, the String.intern() call will take care of
        // canonicalizing the strings.
        final int last = spacesFactory.length-1;
        if (length<0) length=0;
        if (length <= last) {
            if (spacesFactory[length]==null) {
                if (spacesFactory[last]==null) {
                    char[] blancs = new char[last];
                    Arrays.fill(blancs, ' ');
                    spacesFactory[last]=new String(blancs).intern();
                }
                spacesFactory[length] = spacesFactory[last].substring(0,length).intern();
            }
            return spacesFactory[length];
        } else {
            char[] blancs = new char[length];
            Arrays.fill(blancs, ' ');
            return new String(blancs);
        }
    }

    /**
     * Returns a short class name for the specified class. This method will
     * omit the package name.  For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object. It will also name
     * array according Java language usage,  for example "double[]" instead
     * of "[D".
     *
     * @param  classe The object class (may be {@code null}).
     * @return A short class name for the specified object.
     *
     * @todo Consider replacing by {@link Class#getSimpleName} when we will
     *       be allowed to compile for J2SE 1.5.
     */
    public static String getShortName(Class classe) {
        if (classe == null) {
            return "<*>";
        }
        int dimension = 0;
        Class el;
        while ((el = classe.getComponentType()) != null) {
            classe = el;
            dimension++;
        }
        String name = classe.getName();
        final int lower = name.lastIndexOf('.');
        final int upper = name.length();
        name = name.substring(lower+1, upper).replace('$','.');
        if (dimension != 0) {
            StringBuffer buffer = new StringBuffer(name);
            do {
                buffer.append("[]");
            } while (--dimension != 0);
            name = buffer.toString();
        }
        return name;
    }

    /**
     * Returns a short class name for the specified object. This method will
     * omit the package name. For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object.
     *
     * @param  object The object (may be {@code null}).
     * @return A short class name for the specified object.
     */
    public static String getShortClassName(final Object object) {
        return getShortName(object!=null ? object.getClass() : null);
    }

    /**
     * Invoked when an unexpected error occurs.
     *
     * @param paquet  The package where the error occurred. This information
     *                may be used to fetch an appropriate {@link Logger} for
     *                logging the error.
     * @param classe  The class name where the error occurred.
     * @param method  The method name where the error occurred.
     * @param error   The error.
     */
    public static void unexpectedException(final String   paquet,
                                           final String   classe,
                                           final String   method,
                                           final Throwable error)
    {
        final LogRecord record = getLogRecord(error);
        record.setSourceClassName (classe);
        record.setSourceMethodName(method);
        record.setThrown          (error);
        Logger.getLogger(paquet).log(record);
    }

    /**
     * Invoked when a recoverable error occurs. This exception is similar to
     * {@link #unexpectedException unexpectedException} except that it doesn't
     * log the stack trace and uses a lower logging level.
     *
     * @param paquet  The package where the error occurred. This information
     *                may be used to fetch an appropriate {@link Logger} for
     *                logging the error.
     * @param classe  The class name where the error occurred.
     * @param method  The method name where the error occurred.
     * @param error   The error.
     */
    public static void recoverableException(final String   paquet,
                                            final String   classe,
                                            final String   method,
                                            final Throwable error)
    {
        final LogRecord record = getLogRecord(error);
        record.setLevel(Level.FINER);
        record.setSourceClassName (classe);
        record.setSourceMethodName(method);
        Logger.getLogger(paquet).log(record);
    }

    /**
     * Returns a log record for the specified exception.
     */
    static LogRecord getLogRecord(final Throwable error) {
        final StringBuffer buffer = new StringBuffer(getShortClassName(error));
        final String message = error.getLocalizedMessage();
        if (message != null) {
            buffer.append(": ");
            buffer.append(message);
        }
        return new LogRecord(Level.WARNING, buffer.toString());
    }
}
