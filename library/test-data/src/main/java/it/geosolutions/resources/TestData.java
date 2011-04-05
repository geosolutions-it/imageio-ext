/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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
package it.geosolutions.resources;

import it.geosolutions.imageio.utilities.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ----------------------------- NOTE -----------------------------
 * This class contains a modified version of the geotools TestData 
 * class.
 * ----------------------------------------------------------------
 * 
 * Provides access to {@code test-data} directories associated with JUnit tests.
 * <p>
 * We have chosen "{@code test-data}" to follow the javadoc "{@code doc-files}" convention
 * of ensuring that data directories don't look anything like normal java packages.
 * <p>
 * Example:
 * <pre>
 * class MyClass {
 *     public void example() {
 *         Image testImage = new ImageIcon(TestData.url(this, "test.png")).getImage();
 *         Reader reader = TestData.openReader(this, "script.xml");
 *         // ... do some process
 *         reader.close();
 *     }
 * }
 * </pre>
 * Where the directory structure goes as bellow:
 * <ul>
 *   <li>{@code MyClass.java}<li>
 *   <li>{@code test-data/test.png}</li>
 *   <li>{@code test-data/script.xml}</li>
 * </ul>
 * <p>
 * By convention you should try and locate {@code test-data} near the JUnit test
 * cases that uses it. If you need an access to shared test data, import the
 * {@link org.geotools.TestData} class from the {@code sample-module} instead
 * of this one.
 *
 * @since 2.0
 * @version $Id: TestData.java 1041 2007-01-22 16:59:43Z simboss $
 * @author James McGill
 * @author Simone Giannecchini, GeoSolutions
 * @author Martin Desruisseaux
 *
 * @tutorial http://www.geotools.org/display/GEOT/5.8+Test+Data
 */

public class TestData implements Runnable {
    /**
     * The test data directory.
     */
    private static final String DIRECTORY = "test-data";

    /**
     * Encoding of URL path.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The {@linkplain System#getProperty(String) system property} key for more extensive test
     * suite. The value for this key is returned by the {@link #isExtensiveTest} method. Some
     * test suites will perform more extensive test coverage if this property is set to
     * {@code true}. The value for this property is typically defined on the command line as a
     * <code>-D{@value}=true</code> option at Java or Maven starting time.
     */
    public static final String EXTENSIVE_TEST_KEY = "test.extensive";

    /**
     * The {@linkplain System#getProperty(String) system property} key for interactive tests. 
     * The value for this key is returned by the {@link #isInteractiveTest} method. Some
     * test suites will show windows with maps and other artifacts related to testing 
     * if this property is set to {@code true}. 
     * The value for this property is typically defined on the command line as a
     * <code>-D{@value}=true</code> option at Java or Maven starting time.
     */
    public static final String INTERACTIVE_TEST_KEY = "test.interactive";

    /**
     * The files to delete at shutdown time. {@link File#deleteOnExit} alone doesn't seem
     * sufficient since it will preserve any overwritten files.
     */
    private static final LinkedList<Deletable> toDelete = new LinkedList<Deletable>();

    /**
     * Register the thread to be automatically executed at shutdown time.
     * This thread will delete all temporary files registered in {@link #toDelete}.
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new TestData(), "Test data cleaner"));
    }

    /**
     * Do not allow instantiation of this class, except for extending it.
     */
    protected TestData() {
    }

    /**
     * Get a property as a boolean value. If the property can't be
     * fetch for security reason, then default to {@code false}.
     */
    private static boolean getBoolean(final String name) {
        try {
            return Boolean.getBoolean(name);
        } catch (SecurityException exception) {
            Logger.getLogger("org.geotools").warning(exception.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Returns {@code true} if {@value #EXTENSIVE_TEST_KEY} system property is set to
     * {@code true}. Test suites should check this value before to perform lengthly tests.
     */
    public static boolean isExtensiveTest() {
        return getBoolean(EXTENSIVE_TEST_KEY);
    }

    /**
     * Returns {@code true} if {@value #INTERACTIVE_TEST_KEY} system property is set to {@code true}.
     * Test suites should check this value before showing any kind of graphical window to the user.
     */
    public static boolean isInteractiveTest() {
        return getBoolean(INTERACTIVE_TEST_KEY);
    }

    /**
     * Locates named test-data resource for caller. <strong>Note:</strong> Consider using the
     * <code>{@link #url url}(caller, name)</code> method instead if the resource should always
     * exists.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  name resource name in {@code test-data} directory.
     * @return URL or {@code null} if the named test-data could not be found.
     *
     * @see #url
     */
    @SuppressWarnings("unchecked")
	public static URL getResource(final Object caller, String name) {
        if (name == null || (name=name.trim()).length() == 0) {
            name = DIRECTORY;
        } else {
            name = DIRECTORY + '/' + name;
        }
        if (caller != null) {
            final Class c = (caller instanceof Class) ? (Class) caller : caller.getClass();
            return c.getResource(name);
        } else {
            return Thread.currentThread().getContextClassLoader().getResource(name);
        }
    }

    /**
     * Access to <code>{@linkplain #getResource getResource}(caller, path)</code> as a non-null
     * {@link URL}. At the difference of {@code getResource}, this method throws an exception if
     * the resource is not found. This provides a more explicit explanation about the failure
     * reason than the infamous {@link NullPointerException}.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  path Path to file in {@code test-data}.
     * @return The URL to the {@code test-data} resource.
     * @throws FileNotFoundException if the resource is not found.
     *
     * @since 2.2
     */
    public static URL url(final Object caller, final String path) throws FileNotFoundException {
        final URL url = getResource(caller, path);
        if (url == null) {
            throw new FileNotFoundException("Could not locate test-data: " + path);
        }
        return url;
    }

    /**
     * Access to <code>{@linkplain #getResource getResource}(caller, path)</code> as a non-null
     * {@link File}. You can access the {@code test-data} directory with:
     *
     * <blockquote><pre>
     * TestData.file(MyClass.class, null);
     * </pre></blockquote>
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  path Path to file in {@code test-data}.
     * @return The file to the {@code test-data} resource.
     * @throws FileNotFoundException if the file is not found.
     * @throws IOException if the resource can't be fetched for an other reason.
     */
    public static File file(final Object caller, final String path) throws IOException {
        final URL url = url(caller, path);
        final File file = Utilities.urlToFile(url);
        if (!file.exists()) {
            throw new FileNotFoundException("Could not locate test-data: " + path);
        }
        return file;
    }

    /**
     * Creates a temporary file with the given name. The file will be created in the
     * {@code test-data} directory.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  name A base name for the temporary file.
     * @param  delOnExit if true, delete the temporary file on exit.
     * @return The temporary file in the {@code test-data} directory.
     * @throws IOException if the file can't be created.
     */
    public static File temp(final Object caller, final String name, final boolean delOnExit) throws IOException {
        final File testData = file(caller, null);
        final int split = name.lastIndexOf('.');
        final String prefix = (split < 0) ? name  : name.substring(0,split);
        final String suffix = (split < 0) ? "tmp" : name.substring(split+1);
        final File tmp = File.createTempFile(prefix, '.'+suffix, testData);
        if (delOnExit)
        	deleteOnExit(tmp);
        return tmp;
    }
    
    /**
     * Creates a temporary file with the given name. The file will be created in the
     * {@code test-data} directory and will be deleted on exit.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  name A base name for the temporary file.
     * @return The temporary file in the {@code test-data} directory.
     * @throws IOException if the file can't be created.
     */
    public static File temp(final Object caller, final String name) throws IOException {
    	return temp(caller,name,true);
    }

    /**
     * Provides a non-null {@link InputStream} for named test data.
     * It is the caller responsability to close this stream after usage.
     *
     * @param  caller Calling class or object used to locate {@code test-data}.
     * @param  name of test data to load.
     * @return The input stream.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @since 2.2
     */
    public static InputStream openStream(final Object caller, final String name)
            throws IOException
    {
        return new BufferedInputStream(url(caller, name).openStream());
    }

    /**
     * Provides a {@link BufferedReader} for named test data. The buffered reader is provided as
     * an {@link LineNumberReader} instance, which is useful for displaying line numbers where
     * error occur. It is the caller responsability to close this reader after usage.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name of test data to load.
     * @return The buffered reader.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @since 2.2
     */
    public static LineNumberReader openReader(final Object caller, final String name)
            throws IOException
    {
        return new LineNumberReader(new InputStreamReader(url(caller, name).openStream()));
    }

    /**
     * Provides a channel for named test data. It is the caller responsability to close this
     * chanel after usage.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name of test data to load.
     * @return The chanel.
     * @throws FileNotFoundException if the resource is not found.
     * @throws IOException if an error occurs during an input operation.
     *
     * @since 2.2
     */
    public static ReadableByteChannel openChannel(final Object caller, final String name)
            throws IOException
    {
        final URL url = url(caller, name);
        final File file = Utilities.urlToFile(url);
        if (file.exists()) {
            return new RandomAccessFile(file, "r").getChannel();
        }
        return Channels.newChannel(url.openStream());
    }

    /**
     * Unzip a file in the {@code test-data} directory. The zip file content is inflated in place,
     * i.e. inflated files are written in the same {@code test-data} directory. If a file to be
     * inflated already exists in the {@code test-data} directory, then the existing file is left
     * untouched and the corresponding ZIP entry is silently skipped. This approach avoid the
     * overhead of inflating the same files many time if this {@code unzipFile} method is invoked
     * before every tests.
     * <p>
     * Inflated files will be automatically {@linkplain File#deleteOnExit deleted on exit}
     * if and only if they have been modified. Callers don't need to worry about cleanup,
     * because the files are inflated in the {@code target/.../test-data} directory, which
     * is not versionned by SVN and is cleaned by Maven on {@code mvn clean} execution.
     *
     * @param  caller The class of the object associated with named data.
     * @param  name The file name to unzip in place.
     * @throws FileNotFoundException if the specified zip file is not found.
     * @throws IOException if an error occurs during an input or output operation.
     *
     * @since 2.2
     */
    public static void unzipFile(final Object caller, final String name) throws IOException {
        final File        file    = file(caller, name);
        final File        parent  = file.getParentFile().getAbsoluteFile();
        final ZipFile     zipFile = new ZipFile(file);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        final byte[]      buffer  = new byte[4096];
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            final File path = new File(parent, entry.getName());
            if (path.exists()) {
                continue;
            }
            final File directory = path.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            // Copy the file. Note: no need for a BufferedOutputStream,
            // since we are already using a buffer of type byte[4096].
            final InputStream  in  = zipFile.getInputStream(entry);
            final OutputStream out = new FileOutputStream(path);
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
            // Call 'deleteOnExit' only after after we closed the file,
            // because this method will save the modification time.
            deleteOnExit(path, false);
        }
        zipFile.close();
    }

    /**
     * Requests that the file or directory denoted by the specified
     * pathname be deleted when the virtual machine terminates.
     */
    protected static void deleteOnExit(final File file) {
        deleteOnExit(file, true);
    }

    /**
     * Requests that the file or directory denoted by the specified pathname be deleted
     * when the virtual machine terminates. This method can optionnaly delete the file
     * only if it has been modified, thus giving a chance for test suites to copy their
     * resources only once.
     * 
     * @param file The file to delete.
     * @param force If {@code true}, delete the file in all cases. If {@code false},
     *        delete the file if and only if it has been modified. The default value
     *        if {@code true}.
     *
     * @since 2.4
     */
    protected static void deleteOnExit(final File file, final boolean force) {
        if (force) {
            file.deleteOnExit();
        }
        final Deletable entry = new Deletable(file, force);
        synchronized (toDelete) {
            if (file.isFile()) {
                toDelete.addFirst(entry);
            } else {
                toDelete.addLast(entry);
            }
        }
    }

    /**
     * A file that may be deleted on JVM shutdown.
     */
    private static final class Deletable {
        /**
         * The file to delete.
         */
        private final File file;

        /**
         * The initial timestamp. Used in order to determine if the file has been modified.
         */
        private final long timestamp;

        /**
         * Constructs an entry for a file to be deleted.
         */
        public Deletable(final File file, final boolean force) {
            this.file = file;
            timestamp = force ? Long.MIN_VALUE : file.lastModified();
        }

        /**
         * Returns {@code true} if failure to delete this file can be ignored.
         */
        public boolean canIgnore() {
            return timestamp != Long.MIN_VALUE && file.isDirectory();
        }

        /**
         * Deletes this file, if modified. Returns {@code false} only
         * if the file should be deleted but the operation failed.
         */
        public boolean delete() {
            if (!file.exists() || file.lastModified() <= timestamp) {
                return true;
            }
            return file.delete();
        }

        /**
         * Returns the filepath.
         */
        public String toString() {
            return String.valueOf(file);
        }
    }

    /**
     * Deletes all temporary files. This method is invoked automatically at shutdown time and
     * should not be invoked directly. It is public only as an implementation side effect.
     */
    public void run() {
        int iteration = 5; // Maximum number of iterations
        synchronized (toDelete) {
            while (!toDelete.isEmpty()) {
                if (--iteration < 0) {
                    break;
                }
                /*
                 * Before to try to delete the files, invokes the finalizers in a hope to close
                 * any input streams that the user didn't explicitly closed. Leaving streams open
                 * seems to occurs way too often in our test suite...
                 */
                System.gc();
                System.runFinalization();
                for (final Iterator<Deletable> it=toDelete.iterator(); it.hasNext();) {
                    final Deletable f = it.next();
                    try {
                        if (f.delete()) {
                            it.remove();
                            continue;
                        }
                    } catch (SecurityException e) {
                        if (iteration == 0) {
                            System.err.print(e.getClass().getCanonicalName());
                            System.err.print(": ");
                        }
                    }
                    // Can't use logging, since logger are not available anymore at shutdown time.
                    if (iteration == 0 && !f.canIgnore()) {
                        System.err.print("Can't delete ");
                        System.err.println(f);
                    }
                }
            }
        }
    }
}
