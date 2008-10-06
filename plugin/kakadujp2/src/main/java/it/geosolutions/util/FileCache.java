package it.geosolutions.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A simple class which, given an input <coded>URL</code> relying to a remote
 * <code>File</code>, provides to return a temporary <code>File</code>
 * containing data loaded from that location. Furthermore, it provides to cache
 * temporary files ((with a LRU-technic), in order to avoid future data
 * re-loading from the same remote location.
 * 
 * It is worth to point out that, if the input <code>URL</code> relys to a
 * local <code>File</code>, no caching occurs and the class simply returns
 * that file.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini
 * @see LRULinkedHashMap
 * 
 * 
 * @todo thread safety
 * @todo possibility to remove the created temp files.
 * @todo possibily to control the behaviour of the {@link LRULinkedHashMap}
 * @todo make the thing static and share the files between different threads
 */
public class FileCache {

    /** {@link Logger} for this class. */
    protected final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.util");

    // private final static File tempDirectory;
    // static {
    // // get the location of the temporary directory
    // final String tempDirPath = System.getProperty("java.io.tmpdir");
    // if (tempDirPath != null) {
    // final File tempDir = new File(tempDirPath);
    // if (!tempDir.exists()) {
    // // the temp dir does not exists,
    // // let's try to create it.
    // final boolean result = tempDir.mkdir();
    // if (!result || !tempDir.exists())
    // throw new IllegalArgumentException();
    // }
    // // the temp dir exists let's try to explicitly create a file
    // final File testFile = new File(tempDir, "test");
    // try {
    // if (!testFile.createNewFile() || !testFile.exists())
    // throw new IllegalArgumentException(
    // "Unable to create temporary files. Check your rights.");
    // } catch (IOException e) {
    // final IllegalStateException ex = new IllegalStateException(
    // "Unable to create temporary files. Check your rights.");
    // ex.initCause(e);
    // throw ex;
    // }
    // tempDirectory = tempDir;
    //
    // } else {
    // // use the default behaviour and create uknown temp files
    // tempDirectory = null;
    // // test the usual methods of getting a temp file
    // File testFile;
    // try {
    // testFile = File.createTempFile("test", "test");
    // if (!testFile.createNewFile() || !testFile.exists())
    // throw new IllegalArgumentException(
    // "Unable to create temporary files. Check your rights.");
    // } catch (IOException e) {
    // if (LOGGER.isLoggable(Level.SEVERE))
    // LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
    // final IllegalStateException ex = new IllegalStateException(
    // "Unable to create temporary files. Check your rights.");
    // ex.initCause(e);
    // throw ex;
    // }
    //
    // }
    //
    // }

    /** the inner map containing couples [<code>URL</code>, <code>File</code>] */
    protected final Map map = new LRULinkedHashMap();

    /**
     * Default size for the buffer that we can use to eventually download data
     * from a remote URI.
     */
    final public static int DEFAULT_BUFFER_SIZE = 8 * 1024;

    /**
     * Holds the chose size of the buffer used to eventually download remote
     * resources.
     * 
     * @see #DEFAULT_BUFFER_SIZE
     */
    protected int bufferSize;

    /**
     * Default constructor for a {@link FileCache} object.
     * 
     * <p>
     * This constructor uses a default size for the underlying cache which
     * corresponds to the value of {@link #DEFAULT_BUFFER_SIZE}.
     * 
     */
    public FileCache() {
        bufferSize = DEFAULT_BUFFER_SIZE;
    }

    /**
     * Constructor with bufferSize setting.
     * 
     * @param bufferSize
     *                the size of the Byte's buffer which will be used to read
     *                data from the <code>InputStream</code> related to remote
     *                object.
     * 
     */
    public FileCache(final int bufferSize) {
        // //
        //
        // check input values
        //
        // //
        if (bufferSize <= 0)
            throw new IllegalArgumentException("The specified buffer size "
                    + bufferSize + " is invalid!");
        this.bufferSize = bufferSize;
    }

    /**
     * Provides a <code>File<code> containing data coming from the 
     * specified input <code>URL</code> which may rely on a remote location or
     * a local <code>File</code>.
     *  
     * <p>
     * This method checks the provided {@link URL} in order to see to what it 
     * points and then it tries to retreve a valid file for it.
     * If the {@link URL} point to an http(s) or (s)ftp resource this method 
     * opens up a connection and then dump the content of the remote resource 
     * into a temporary file, which is then cached and returned.
     * 
     * 
     * <p>
     * In case we cannot safely create a file for the provided {@link URL} <code>null</code>
     * might be returned, hence, <strong>always</strong> check the return value of this method.
     * 
     * @param url
     * 			an <code>URL<code> from which we need to get the <code>File<code>
     * @return the <code>File</code> related to the specified <code>URL<code>
     * @throws FileNotFoundException
     * @throws IOException
     */
    public File getFile(URL url) throws FileNotFoundException, IOException {
        if (url == null)
            throw new NullPointerException("the provided input is null!");
        final File tempFile;

        // /////////////////////////////////////////////////////////////////////
        //
        // Preliminary checks
        //
        // /////////////////////////////////////////////////////////////////////
        // input URL may rely to a local File. In this case, no caching
        // mechanism occurs and we simply returns that file.
        if (url.getProtocol().compareToIgnoreCase("file") == 0) {
            tempFile = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            if (!tempFile.exists())
                return null;
            return tempFile;
        }

        // Checking if a temporary file already exists for the specified URL
        if (map.containsKey(url)) {
            tempFile = (File) map.get(url);

            // security check
            if (!tempFile.exists())
                return null;
            return tempFile;

        }
        // /////////////////////////////////////////////////////////////////
        //
        // The specified URL is not contained in the map.
        // (even if the same URL was previously provided as input of
        // this method, the map should have removed it (LRU technic)).
        //
        // /////////////////////////////////////////////////////////////////

        // //
        //
        // Creating a tempFile into the system-dependent default
        // temporary-file directory
        //
        // //
        tempFile = File.createTempFile("cached", ".tmp", null);

        // //
        //
        // getting an InputStream from the connection to the
        // object referred to by the URL
        //
        // //
        final InputStream is = url.openConnection().getInputStream();

        // //
        //
        // Preparing a FileOutputStream where to write all data
        // we will read by the InputStream
        //
        // //
        final BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(tempFile));
        final byte b[] = new byte[bufferSize];
        int num = 0;

        // "read from InputStream -> write to FileOutputStream"
        // operation
        while ((num = is.read(b)) > 0) {
            os.write(b, 0, num);
        }

        // closing streams and flushing the outputStream
        os.flush();
        is.close();
        os.close();

        // putting the couple [URL, File] in the map.
        map.put(url, tempFile);

        // returning the tempFile
        return tempFile;
    }

    /**
     * Clears up the internal cache of temporary files used to avoid downloading
     * the same file over and over from the web.
     * 
     * 
     * TODO: deleting temporary files??
     */
    public void clear() {
        // Set set = map.keySet();
        // Iterator iter = set.iterator();
        // File file;
        // // Cleaning HashMap
        // while (iter.hasNext()) {
        // file = (File) map.get(iter.next());
        // file.delete();
        // }
        map.clear();

    }

}
