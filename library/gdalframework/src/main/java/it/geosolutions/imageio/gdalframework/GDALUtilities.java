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
package it.geosolutions.imageio.gdalframework;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * Utility class providing a set of static utility methods
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class GDALUtilities {
    /**
     * Simple placeholder for Strings representing GDAL metadata domains.
     */
    public final static class GDALMetadataDomain {

        public final static String IMAGESTRUCTURE = "IMAGE_STRUCTURE";

        public final static String SUBDATASETS = "SUBDATASETS";

        public final static String DEFAULT = "";

        protected final static String DEFAULT_KEY_MAP = "DEF";

        public final static String XML_PREFIX = "xml:";
    }


    public static final String STANDARD_METADATA_NAME = IIOMetadataFormatImpl.standardMetadataFormatName;

    public final static String newLine = System.getProperty("line.separator");
    /**
     * System property name to customize the max supported size of a GDAL In
     * Memory Raster Dataset to be created before using the createCopy method
     */
    public final static String GDALMEMORYRASTER_MAXSIZE_KEY = "it.geosolutions.gdalmemoryrastermaxsize";

    /**
     * Simple placeholder for information about a driver's capabilities.
     */
    public enum DriverCreateCapabilities {
        /** {@link Driver} supports up to create. */
        CREATE,

        /** {@link Driver} supports up to create copy. */
        CREATE_COPY,

        /** {@link Driver} supports up to read only. */
        READ_ONLY;
    }


	/**
	 * An auxiliary simple class containing only contants which are used to
	 * handle text building and visualization
	 * 
	 * @author Daniele Romagnoli
	 * 
	 */
	public enum MetadataChoice {
	    ONLY_IMAGE_METADATA,
	
	    ONLY_STREAM_METADATA,
	
	    STREAM_AND_IMAGE_METADATA,
	    
	    PROJECT_AND_GEOTRANSF,
	
	    EVERYTHING;
	}


    private static final String CPL_DEBUG = "CPL_DEBUG";

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.gdalframework");

    /** is gdal available on this machine?. */
    private static boolean available;

    private static boolean init = false;

    /**
     * This {@link Map} link each driver with its writing capabilities.
     * 
     */
    private final static Map<String,DriverCreateCapabilities> driversWritingCapabilities = Collections.synchronizedMap(new HashMap<String,DriverCreateCapabilities>());

    /** private constructor to prevent instantiation */
    private GDALUtilities() {
    }

    /**
     * Get a boolean value from a specified value. Return <code>true</code> if
     * the value is one of "ON","TRUE","YES" (values aren't case sensitive).
     * 
     * Return <code>false</code> otherwise. A null value is handled as false.
     */
    private static boolean getAsBoolean(final String value) {
        if (value != null && value.trim().length() > 0) {
            if (value.equalsIgnoreCase("ON") || value.equalsIgnoreCase("TRUE")
                    || value.equalsIgnoreCase("YES"))
                return true;
        }
        return false;
    }

    /**
     * Simply provides to retrieve the corresponding <code>GDALDataType</code>
     * for the specified <code>dataBufferType</code>
     * 
     * @param dataBufferType
     *                the <code>DataBuffer</code> type for which we need to
     *                retrieve the proper <code>GDALDataType</code>
     * 
     * @return the proper <code>GDALDataType</code>
     */
    public static int retrieveGDALDataBufferType(final int dataBufferType) {
        switch (dataBufferType) {
        case DataBuffer.TYPE_BYTE:
            return gdalconstConstants.GDT_Byte;
        case DataBuffer.TYPE_USHORT:
            return gdalconstConstants.GDT_UInt16;
        case DataBuffer.TYPE_SHORT:
            return gdalconstConstants.GDT_Int16;
        case DataBuffer.TYPE_INT:
            return gdalconstConstants.GDT_Int32;
        case DataBuffer.TYPE_FLOAT:
            return gdalconstConstants.GDT_Float32;
        case DataBuffer.TYPE_DOUBLE:
            return gdalconstConstants.GDT_Float64;
        default:
            return gdalconstConstants.GDT_Unknown;
        }
    }

    /**
     * Returns the maximum amount of memory available for GDAL caching
     * mechanism.
     * 
     * @return the maximum amount in bytes of memory available.
     */
    public static int getCacheMax() {
        return gdal.GetCacheMax();
    }

    /**
     * Returns the amount of GDAL cache used.
     * 
     * @return the amount (bytes) of memory currently in use by the GDAL memory
     *         caching mechanism.
     */
    public static int getCacheUsed() {
        return gdal.GetCacheUsed();
    }

    @SuppressWarnings("unchecked")
	public static List getJDKImageReaderWriterSPI(ServiceRegistry registry,
            String formatName, boolean isReader) {

        IIORegistry iioRegistry = (IIORegistry) registry;

        Class spiClass;
        if (isReader)
            spiClass = ImageReaderSpi.class;
        else
            spiClass = ImageWriterSpi.class;

        Iterator iter = iioRegistry.getServiceProviders(spiClass, true); // useOrdering

        String formatNames[];
        ImageReaderWriterSpi provider;

        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            provider = (ImageReaderWriterSpi) iter.next();

            // Get the formatNames supported by this Spi
            formatNames = provider.getFormatNames();
            final int length = formatNames.length;
            for (int i = 0; i < length; i++) {
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
     * Sets the GDAL Max Cache Size. You can do this only if caching is enabled.
     * 
     * @param maxCacheSize
     */
    public static void setCacheMax(int maxCacheSize) {
        gdal.SetCacheMax(maxCacheSize);
    }

    /**
     * Allows to enable/disable GDAL caching mechanism.
     * 
     * @param useCaching
     *                <code>true</code> to enable GDAL caching.
     *                <code>false</code> to disable GDAL caching.
     */
    public static void setGdalCaching(boolean useCaching) {
        final String sOption = useCaching ? "YES" : "NO";
        gdal.SetConfigOption("GDAL_FORCE_CACHING", sOption);
    }

    /**
     * Allows to enable/disable GDAL Persistable Auxiliary Metadata.
     * 
     * @param usePAM
     *                <code>true</code> to enable GDAL PAM. <code>false</code>
     *                to disable GDAL PAM.
     */
    public static void setGdalPAM(boolean usePAM) {
        final String sOption = usePAM ? "YES" : "NO";
        gdal.SetConfigOption("GDAL_PAM_ENABLED", sOption);
    }

    /**
     * Acquires a {@link Dataset} and return it, given the name of the Dataset
     * source and the desired access type
     * 
     * @param name
     *                of the dataset source to be accessed (usually, a File
     *                name).
     * @param accessType
     * @return the acquired {@link Dataset}
     */
    public static Dataset acquireDataSet(final String name,final int accessType) {
        if (!isGDALAvailable()) {
            return null;
        }
        if(name == null) {
            throw new IllegalArgumentException("Provided parameter is null:name");
        }
        return gdal.Open(name, accessType);

    }

    /**
     * Returns any metadata related to the specified image. The SUBDATASETS
     * domain is not returned since it is related to the whole stream instead of
     * a single image.
     * 
     * @param dataSetName
     *                the name of the dataset for which we need to retrieve
     *                imageMetadata
     * 
     * @return a <code>List</code> containing any metadata found.
     */
    public static List<String> getGDALImageMetadata(String dataSetName) {
        final Dataset ds = acquireDataSet(dataSetName, gdalconst.GA_ReadOnly);
        final List<String> gdalImageMetadata;
        if (ds != null) {
            try {
                gdalImageMetadata = ds.GetMetadata_List("");
            } finally {
                // Closing the dataset
                closeDataSet(ds);
            }
        } else {
            gdalImageMetadata = null;
        }
        return gdalImageMetadata;
    }

    /**
     * Closes the given {@link Dataset}.
     * 
     * @param ds
     *                {@link Dataset} to close.
     */
    public static void closeDataSet(Dataset ds) {
        if (ds == null) {
            throw new NullPointerException("The provided dataset is null");
        }
        try {
            ds.delete();
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns <code>true</code> if a driver for the specific format is
     * available. <code>false</code> otherwise.<BR>
     * It is worth to point out that a successful loading of the native library
     * is not sufficient to grant the support for a specific format. We should
     * also check if the proper driver is available.
     * 
     * @return <code>true</code> if a driver for the specific format is
     *         available. <code>false</code> otherwise.<BR>
     */
    public static boolean isDriverAvailable(final String driverName) {
        if (!isGDALAvailable()) {
            return false;
        }    
        Driver driver = null;
        
        try {
        	driver = gdal.GetDriverByName(driverName);
        } catch (UnsatisfiedLinkError e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
            	LOGGER.warning("Failed to get the specified GDAL Driver: " + driverName + 
                        "\nCause: " + e.toString() + "\nThis is not a problem unless you " +
                        "need to use the specified GDAL plugin. It won't be enabled");
            }
            return false;
        } 
        if (driver == null) {
            return false;
        }
        return true;
    }

    /**
     * Tells us about the capabilities for a GDAL driver.
     * 
     * @param driverName
     *                name of the {@link Driver} we want to get info about.
     * @return {@link GDALUtilities.DriverCreateCapabilities#CREATE} in case the
     *         driver supports creation of dataset,
     *         {@link GDALUtilities.DriverCreateCapabilities#CREATE_COPY} in
     *         case the driver supports only create copy and eventually
     *         {@link GDALUtilities.DriverCreateCapabilities#READ_ONLY} for
     *         read-only drivers.
     * @throws IllegalArgumentException
     *                 in case the specified driver name is <code>null</code>
     *                 or a Driver for the specified name is unavailable.
     */
    public static DriverCreateCapabilities formatWritingCapabilities(final String driverName) {
        if (driverName == null)
            throw new IllegalArgumentException(
                    "The provided driver name is null");
        loadGDAL();
        synchronized (driversWritingCapabilities) {
            if (driversWritingCapabilities.containsKey(driverName))
                return (driversWritingCapabilities.get(driverName));
            final Driver driver = gdal.GetDriverByName(driverName);
            try {
	            if (driver == null)
	                throw new IllegalArgumentException(
	                        "A Driver with the specified name is unavailable. "
	                                + "Check the specified name or be sure this "
	                                + "Driver is supported");
	            // parse metadata
	            final Map metadata = driver.GetMetadata_Dict("");
	            final String create = (String) metadata.get("DCAP_CREATE");
	            final String createCopy = (String) metadata.get("DCAP_CREATECOPY");
	            final boolean createSupported = create != null&& create.equalsIgnoreCase("yes");
	            final boolean createCopySupported = createCopy != null&& createCopy.equalsIgnoreCase("yes");
	            DriverCreateCapabilities retVal;
	            if (createSupported) {
	                driversWritingCapabilities.put(driverName, GDALUtilities.DriverCreateCapabilities.CREATE);
	                return GDALUtilities.DriverCreateCapabilities.CREATE;
	            } else if (createCopySupported) {
	                driversWritingCapabilities.put(driverName, GDALUtilities.DriverCreateCapabilities.CREATE_COPY);
	                return GDALUtilities.DriverCreateCapabilities.CREATE_COPY;
	            } else {
	                driversWritingCapabilities.put(driverName, GDALUtilities.DriverCreateCapabilities.READ_ONLY);
	                return GDALUtilities.DriverCreateCapabilities.READ_ONLY;
	            }
            } finally {
            	if (driver != null){
    	    		try{
                        // Closing the driver
    	    			driver.delete();
            		}catch (Throwable e) {
    					if(LOGGER.isLoggable(Level.FINEST))
    						LOGGER.log(Level.FINEST,e.getLocalizedMessage(),e);
    				}
    	    	}
            }
        }
    }

    /**
     * Returns the value of a specific metadata item related to the stream. As
     * an instance, it may be used to find the name or the description of a
     * specific subdataset.
     * 
     * @param metadataName
     *                the name of the specified metadata item.
     * @param datasetName
     * @return the value of the required metadata item.
     */
    public static String getStreamMetadataItem(String metadataName,
            String datasetName) {
        return getMetadataItem(getGDALStreamMetadata(datasetName), metadataName);
    }

    /**
     * Returns the value of a specific metadata item contained in the metadata
     * given as first input parameter
     * 
     * @param gdalImageMetadata
     *                the required metadata <code>List</code>
     *                (gdalStreamMetadata or gdalImageMetadata)
     * @param metadataName
     *                the name of the specified metadata item
     * @return the value of the specified metadata item
     */
    public static String getMetadataItem(List imageMetadata, String metadataName) {
        final Iterator it = imageMetadata.iterator();
        // Metadata items scan
        while (it.hasNext()) {
            String s = (String) it.next();
            int indexOfEqualSymbol = s.indexOf('=');
            String sName = s.substring(0, indexOfEqualSymbol);
            if (sName.equals(metadataName))
                return s.substring(indexOfEqualSymbol + 1, s.length());
        }
        return null;
    }

    /**
     * Returns any metadata which is not related to a specific image. The actual
     * implementation provide to return only the SUBDATASETS domain metadata but
     * it may be changed in future.
     * 
     * @param datasetName
     * 
     * @return a <code>List</code> containing metadata related to the stream.
     */
    public static List getGDALStreamMetadata(String datasetName) {
        Dataset ds = null;
        try {
            ds = acquireDataSet(datasetName, gdalconst.GA_ReadOnly);
            return ds.GetMetadata_List("SUBDATASETS");
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
            return null;
        } finally {
            if (ds != null) {
                try {
                    // Closing the dataset
                    GDALUtilities.closeDataSet(ds);
                } catch (Throwable e) {
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Set the value of a specific attribute of a specific
     * <code>IIOMetadataNode</code>
     * 
     * @param name
     *                The name of the attribute which need to be set
     * @param val
     *                The value we want to set
     * @param node
     *                The <code>IIOMetadataNode</code> having the attribute we
     *                are going to set
     * @param attributeType
     *                The type of the attribute we are going to set
     */
    public static void setNodeAttribute(String name, Object val,
            IIOMetadataNode node, int attributeType) {

        try {
            if (val != null && val instanceof String) {
                final String value = (String) val;
                if (value != null && value.length() > 0) {
                    switch (attributeType) {
                    case IIOMetadataFormat.DATATYPE_DOUBLE:
                        // check that we can parse it
                        Double.parseDouble(value);
                        break;
                    case IIOMetadataFormat.DATATYPE_FLOAT:
                        // check that we can parse it
                        Float.parseFloat(value);
                        break;
                    case IIOMetadataFormat.DATATYPE_INTEGER:
                        // check that we can parse it
                        Integer.parseInt(value);
                        break;
                    case IIOMetadataFormat.DATATYPE_BOOLEAN:
                        // check that we can parse it
                        Boolean.valueOf(value);
                        break;
                    case IIOMetadataFormat.DATATYPE_STRING:
                        // do nothing
                        break;
                    default:
                        // prevent the code from setting the value
                        throw new RuntimeException();
                    }
                    node.setAttribute(name, value);
                    return;
                }
            }
        } catch (NumberFormatException nfe) {
            LOGGER
                    .fine("The specified value has not been successfully parsed: "
                            + (String) val);
        }
        // DEFAULT VALUE FOR ATTRIBUTES IS ""
        node.setAttribute(name, "");
    }

    /**
     * The default tile size. This default tile size can be overridden with a
     * call to {@link JAI#setDefaultTileSize}.
     */
    public static final Dimension DEFAULT_TILE_SIZE = new Dimension(512, 512);

    /**
     * The minimums tile size.
     */
    public static final int MIN_TILE_SIZE = 256;

    /**
     * Line separator String. "\r\n" or "\r" or "\n" depending on the Operating
     * System
     */
    final static String NEWLINE = System.getProperty("line.separator");

    /**
     * Suggests a tile size for the specified image size. On input, {@code size}
     * is the image's size. On output, it is the tile size. This method write
     * the result directly in the supplied object and returns {@code size} for
     * convenience.
     * <p>
     */
    public static Dimension toTileSize(final Dimension size) {
        Dimension defaultSize = JAI.getDefaultTileSize();
        if (defaultSize == null) {
            defaultSize = DEFAULT_TILE_SIZE;
        }
        size.height = defaultSize.height;
        size.width = defaultSize.width;
        return size;
    }

    /**
     * Returns <code>true</code> if the GDAL native library has been loaded.
     * <code>false</code> otherwise.
     * 
     * @return <code>true</code> only if the GDAL native library has been
     *         loaded.
     */
    public static boolean isGDALAvailable() {
        loadGDAL();
        return available;
    }

    /**
     * Forces loading of GDAL libs.
     */
    public static void loadGDAL() {
        if (init == false) {
            synchronized (LOGGER) {
                if (init) {
                    return;
                }
                try {
                    try {
                        // GDAL version >= 2.3.0
                        System.loadLibrary("gdalalljni");
                    } catch (UnsatisfiedLinkError e1) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE,"Failed to load the GDAL native libs from \"gdalalljni\". " +
                                    "Falling back to \"gdaljni\".\n" +
                                    e1.toString());
                        }
                        System.loadLibrary("gdaljni");
                    }
                    gdal.AllRegister();
                    final String versionInfo = gdal.VersionInfo("RELEASE_NAME");
                    if (versionInfo != null && versionInfo.trim().length() > 0) {
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.info("GDAL Native Library loaded (version: " + versionInfo + ")");
                    }

                    if (LOGGER.isLoggable(Level.FINE)) {
                        int driverCount = gdal.GetDriverCount();
                        List<String> drivers = new ArrayList<>();
                        for (int i = 0; i < driverCount; i++) {
                            Driver driver = gdal.GetDriver(i);
                            drivers.add(driver.getShortName());
                            driver.delete();
                        }
                        LOGGER.fine("Formats available in GDAL (not all exposed by imageio-ext): " +  drivers);
                    }

                    // //
                    //
                    // Setting error messages handler.
                    //
                    // //
                    final String cplDebug = System.getProperty(CPL_DEBUG);
                    final boolean showErrors = getAsBoolean(cplDebug);
                    if (!showErrors) {
                        gdal.PushErrorHandler("CPLQuietErrorHandler");
                    }
                    GDALUtilities.available = true;
                } catch (UnsatisfiedLinkError e2) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning("Failed to load the GDAL native libs. This is not a problem "
                                    + "unless you need to use the GDAL plugins: they won't be enabled.\n" 
                                    + e2.toString());
                    }
                    GDALUtilities.available = false;
                } finally {
                    init = true;
                }
            }
        }
    }

    /**
     * Builds a proper <code>ColorModel</code> for a specified
     * <code>SampleModel</code>
     * 
     * @param sampleModel
     *                the sampleModel to be used as reference.
     * @return a proper <code>ColorModel</code> for the input
     *         <code>SampleModel</code>
     */
    public static ColorModel buildColorModel(final SampleModel sampleModel) {
        ColorSpace cs = null;
        ColorModel colorModel = null;
        final int buffer_type = sampleModel.getDataType();
        final int numBands = sampleModel.getNumBands();
        if (numBands > 1) {
            //
            // Number of Bands > 1.
            // ImageUtil.createColorModel provides to Creates a
            // ColorModel that may be used with the specified
            // SampleModel
            //
            colorModel = ImageUtil.createColorModel(sampleModel);
            if (colorModel == null) {
                LOGGER.severe("No ColorModels found");
            }
        } else if ((buffer_type == DataBuffer.TYPE_BYTE)
                || (buffer_type == DataBuffer.TYPE_USHORT)
                || (buffer_type == DataBuffer.TYPE_INT)
                || (buffer_type == DataBuffer.TYPE_FLOAT)
                || (buffer_type == DataBuffer.TYPE_DOUBLE)) {

            // Just one band. Using the built-in Gray Scale Color Space
            cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            colorModel = RasterFactory.createComponentColorModel(buffer_type, // dataType
                    cs, // color space
                    false, // has alpha
                    false, // is alphaPremultiplied
                    Transparency.OPAQUE); // transparency
        } else {
            if (buffer_type == DataBuffer.TYPE_SHORT) {
                // Just one band. Using the built-in Gray Scale Color
                // Space
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                colorModel = new ComponentColorModel(cs, false, false,
                        Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
            }
        }
        return colorModel;
    }

	//
	// Provides to retrieve projections from the provided {@lik RenderedImage}
	// and return the String containing properly formatted text.
	//
	public static String buildCRSProperties(RenderedImage ri, final int index) {
	    final Object imageReader = ri.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
            StringBuffer sb = new StringBuffer("CRS Information:").append(newLine);
            if (imageReader != null && imageReader instanceof ImageReader){
                    final GDALImageReader reader = (GDALImageReader) imageReader;
        	    final String projection = reader.getProjection(index);
        	    if (!projection.equals(""))
        	        sb.append("Projections:").append(projection).append(newLine);
        	
        	    // Retrieving GeoTransformation Information
        	    final double[] geoTransformations = reader.getGeoTransform(index);
        	    if (geoTransformations != null) {
        	        sb.append("Geo Transformation:").append(newLine);
        	        sb
        	                .append("Origin = (")
        	                .append(Double.toString(geoTransformations[0]))
        	                .append(",")
        	                .append(Double.toString(geoTransformations[3]))
        	                .append(")")
        	                .append(newLine)
        	                .append("Pixel Size = (")
        	                .append(Double.toString(geoTransformations[1]))
        	                .append(",")
        	                .append(Double.toString(geoTransformations[5]))
        	                .append(")")
        	                .append(newLine)
        	                .append(newLine)
        	                .append(
        	                        "---------- Affine GeoTransformation Coefficients ----------")
        	                .append(newLine);
        	        for (int i = 0; i < 6; i++)
        	            sb.append("adfTransformCoeff[").append(i).append("]=").append(
        	                    Double.toString(geoTransformations[i])).append(newLine);
        	    }
        	
        	    // Retrieving Ground Control Points Information
        	    final int gcpCount = reader.getGCPCount(index);
        	    if (gcpCount != 0) {
        	        sb.append(newLine).append("Ground Control Points:").append(newLine)
        	                .append("Projections:").append(newLine).append(
        	                        reader.getGCPProjection(index)).append(newLine);
        	
        	        final List gcps = reader.getGCPs(index);
        	
        	        int size = gcps.size();
        	        for (int i = 0; i < size; i++)
        	            sb.append("GCP ").append(i + 1).append(gcps.get(i)).append(
        	                    newLine);
        	    }
            }
            return sb.toString();
	}

	//
	// Provides to retrieve metadata from the provided
	// <code>RenderedImage</code>}
	// and return the String containing properly formatted text.
	//	 
	public static String buildMetadataText(RenderedImage ri,
	        final MetadataChoice metadataFields, final int index) {
	    try {
	        final String newLine = System.getProperty("line.separator");
	        final Object imageReader = ri.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
	        StringBuffer sb = new StringBuffer("");
	        if (imageReader != null && imageReader instanceof ImageReader){
        	        final GDALImageReader reader = (GDALImageReader) imageReader;
        	        switch (metadataFields) {
        	        case ONLY_IMAGE_METADATA:
        	        case EVERYTHING:
        	            sb.append(getImageMetadata(reader, index));
        	            break;
        	        case ONLY_STREAM_METADATA:
        	            sb.append(getStreamMetadata(reader));
        	            break;
        	        case STREAM_AND_IMAGE_METADATA:
        	            sb.append(getImageMetadata(reader, index)).append(newLine)
        	                    .append(getStreamMetadata(reader));
        	            break;
        	        }
	        }
	        return sb.toString();
	    } catch (Exception e) {
	        if (LOGGER.isLoggable(Level.WARNING))
	            LOGGER.warning(e.getLocalizedMessage());
	        return "";
	    }
	}

	//
	// returns a String containing metadata from the provided reader
	//
	// TODO: change with the new ImageIO - Metadata Capabilities
	public static String getImageMetadata(GDALImageReader reader,
	        final int index) {
	    final GDALCommonIIOImageMetadata mt = reader.getDatasetMetadata(index);
	    final List metadata = GDALUtilities.getGDALImageMetadata(mt.getDatasetName());
	    if (metadata != null) {
	        final int size = metadata.size();
	        StringBuffer sb = new StringBuffer("Image Metadata:")
	                .append(newLine);
	        for (int i = 0; i < size; i++)
	            sb.append(metadata.get(i)).append(newLine);
	        return sb.toString();
	    }
	    return "Image Metadata not found";
	}

	//
	// returns a String containing stream metadata from the provided reader
	//
	public static String getStreamMetadata(GDALImageReader reader)
	        throws IOException {
	    final GDALCommonIIOImageMetadata mt = reader.getDatasetMetadata(reader.getNumImages(true) - 1);
	    final List metadata = GDALUtilities.getGDALStreamMetadata(mt.getDatasetName());
	    if (metadata != null) {
	        final int size = metadata.size();
	        StringBuffer sb = new StringBuffer("Stream Metadata:")
	                .append(newLine);
	        for (int i = 0; i < size; i++)
	            sb.append(metadata.get(i)).append(newLine);
	        return sb.toString();
	    }
	    return "Stream Metadata not found";
	}
	
//	public static void shutdown(){
//		cache.invalidateAll();
//	}
}
