/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFChunkInfo;
import ncsa.hdf.hdflib.HDFCompInfo;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a HDF Scientific Dataset.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4SDS extends H4Variable implements IHObject {

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    /** predefined attributes */
    public static String PREDEF_ATTR_LONG_NAME = "long_name";

    public static String PREDEF_ATTR_UNITS = "units";

    public static String PREDEF_ATTR_FORMAT = "format";

    public static String PREDEF_ATTR_CALIBRATED_NT = "calibrated_nt";

    public static String PREDEF_ATTR_SCALE_FACTOR = "scale_factor";

    public static String PREDEF_ATTR_SCALE_FACTOR_ERR = "scale_factor_err";

    public static String PREDEF_ATTR_ADD_OFFSET = "add_offset";

    public static String PREDEF_ATTR_ADD_OFFSET_ERR = "add_offset_err";

    public static String PREDEF_ATTR_FILL_VALUE = "_FillValue";

    public static String PREDEF_ATTR_COORDINATE_SYSTEM = "cordsys";

    public static String PREDEF_ATTR_VALID_RANGE_MIN = "valid_min";

    public static String PREDEF_ATTR_VALID_RANGE_MAX = "valid_max";

    public static String PREDEF_ATTR_VALID_RANGE = "valid_range";

    // private int[] mutex = new int[] { 1 };

    /**
     * The index of this SDS within the source File.
     */
    private int index;

    /**
     * The dimension sizes of this SDS.
     */
    private int[] dimSizes;

    /**
     * The chunk sizes of this SDS.
     */
    private int[] chunkSizes;

    /**
     * The rank of this SDS
     */
    private int rank = -1;

    /**
     * the reference of this SDS
     */
    private H4ReferencedObject reference = null;

    /**
     * The list of data object label annotations related to this SDS. <br>
     */
    private List labelAnnotations = null;

    /**
     * The number of data object label annotations related to this SDS. <br>
     */
    private int nLabels = -1;

    /**
     * The list of data object description annotations related to this SDS. <br>
     */
    private List descAnnotations = null;

    /**
     * The number of data object description annotations related to this SDS.
     * <br>
     */
    private int nDescriptions = -1;

    // /**
    // * the number of 2D datasets contained within this SDS.<BR>
    // * As an instance, a 3D SDS, having size 5*800*600 has datasetSize=5 since
    // * we can access to 5 different 2D-datasets
    // *
    // * @uml.property name="datasetSize"
    // */
    // private int datasetSize;

    /**
     * the datatype of this sds
     */
    private int datatype = HDFConstants.FAIL;

    /**
     * The list of dimensions related to this SDS
     * 
     * @uml.associationEnd inverse="h4sds:it.geosolutions.hdf.object.h4.H4Dimension"
     */
    private List dimensions;

    /**
     * A boolean flag which tells whether the SDS has been opened.
     */
    private boolean isOpened;

    /**
     * The {@link H4SDSCollection} to which this {@link H4SDS} belongs.
     * 
     * @uml.associationEnd inverse="sdsList:it.geosolutions.hdf.object.h4.H4SDSCollection"
     */
    private H4SDSCollection h4SDSCollectionOwner = null;

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////
    /**
     * getter of <code>dimSizes</code>
     * 
     * @return the dimension sizes of this SDS.
     */
    public int[] getDimSizes() {
        return dimSizes;
    }

    /**
     * getter of <code>chunkSizes</code>
     * 
     * @return the chunk sizes of this SDS.
     */
    public int[] getChunkSizes() {
        return chunkSizes;
    }

    /**
     * @return the reference of this SDS.
     */
    int getReference() {
        return reference.getReference();
    }

    /**
     * getter of <code>rank</code>
     * 
     * @return the rank of this SDS
     */
    public int getRank() {
        return rank;
    }

    /**
     * getter of <code>index</code>
     * 
     * @return the index of this SDS within the source File
     */
    public int getIndex() {
        return index;
    }

    // /**
    // * getter of <code>datasetSize</code>
    // *
    // * @return the number of 2D datasets contained within this SDS
    // * @uml.property name="datasetSize"
    // */
    // public int getDatasetSize() {
    // return datasetSize;
    // }

    /**
     * getter of <code>datatype</code>
     * 
     * @return the datatype of this sds
     */
    public int getDatatype() {
        return datatype;
    }

    /**
     * getter of <code>nDescriptions</code>
     * 
     * @return number of data object description annotations related to this
     *         SDS.
     */
    public synchronized int getNDescriptions() {
        // synchronized (mutex) {
        if (nDescriptions == -1)
            try {
                getAnnotations(HDFConstants.AN_DATA_DESC);
            } catch (HDFException e) {
                nDescriptions = 0;
            }
        return nDescriptions;
        // }
    }

    /**
     * getter of <code>nLabels</code>
     * 
     * @return the number of data object label annotations related to this SDS.
     */
    public synchronized int getNLabels() {
        // synchronized (mutex) {
        if (nLabels == -1)
            try {
                getAnnotations(HDFConstants.AN_DATA_LABEL);
            } catch (HDFException e) {
                nLabels = 0;
            }
        return nLabels;
        // }
    }

    /**
     * getter of <code>h4SDSCollectionOwner</code>
     * 
     * @return the {@link H4SDSCollection} to which this {@link H4SDS} belongs.
     */
    H4SDSCollection getH4SDSCollectionOwner() {
        return h4SDSCollectionOwner;
    }

    // /**
    // * Main Constructor which builds a new <code>H4SDS</code> given its index
    // * within the HDF source.<BR>
    // *
    // * It is worth to point out that we need to check if the just built
    // * <code>H4SDS</code> is a dimension scale using the proper method
    // * {@link H4SDS#isDimensionScale()}. If it is not a dimension scale, then
    // * we need to initialize the <code>H4SDS</code> by calling the
    // * {@link H4SDS#init()} method.
    // *
    // * @param h4SdsCollection
    // * the parent collection
    // * @param sdsIndex
    // * the index of the SDS within the HDF source
    // *
    // */
    // public H4SDS(H4SDSCollection h4SdsCollection, final int sdsIndex) {
    // h4SDSCollectionOwner = h4SdsCollection;
    // final int interfaceID = h4SDSCollectionOwner.getIdentifier();
    // try {
    // index = sdsIndex;
    // identifier = HDFLibrary.SDselect(interfaceID, sdsIndex);
    // isOpened = true;
    // if (identifier == HDFConstants.FAIL) {
    // throw new RuntimeException(
    // "Error while creating a new H4SDS: invalid identifier");
    // }
    //
    // } catch (HDFException e) {
    // throw new RuntimeException("Error while creating a new H4SDS", e);
    // }
    // }

    private H4SDS(H4SDSCollection h4SdsCollection, int index, int identifier)
            throws HDFException {
        h4SDSCollectionOwner = h4SdsCollection;
        this.index = index;
        this.identifier = identifier;
        init();
        isOpened = true;
    }

    /**
     * Attempts to build a new {@link H4SDS} given its index within the file. A
     * new {@link H4SDS} is returned only if the underlying SDS does not
     * represents a dimension scale. Otherwise, <code>null</code> will
     * returned.
     * 
     * @param h4SDSCollection
     *                the collection owner.
     * @param index
     *                the index of the required SDS within the file
     * @return a new {@link H4SDS} if the underlying SDS does not represents a
     *         dimension scale. <code>null</code> otherwise.
     * @throws HDFException
     */
    protected static H4SDS buildH4SDS(H4SDSCollection h4SDSCollection,
            final int index) throws HDFException {
        H4SDS sds = null;
        final int interfaceID = h4SDSCollection.getIdentifier();
        if (interfaceID == HDFConstants.FAIL) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, "undefined SDInterface identifier ");
            return sds;
        }
        try {
            final int identifier = HDFLibrary.SDselect(interfaceID, index);
            if (identifier != HDFConstants.FAIL) {
                if (!HDFLibrary.SDiscoordvar(identifier)) {
                    sds = new H4SDS(h4SDSCollection, index, identifier);
                } else
                    HDFLibrary.SDendaccess(identifier);
                // if (sds != null)
                // sds.open();
            } else {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, "undefined SD identifier ");
            }

        } catch (HDFException e) {
            throw new RuntimeException("Error while creating a new H4SDS", e);
        }
        return sds;
    }

    // /**
    // * Open this <code>H4SDS</code><BR>
    // * it is worth to point out that the identifier of this sds is always the
    // * same when referring to the same SDS Interface ID.
    // */
    // public synchronized void open() {
    // if (!isOpened) {
    // if (identifier != HDFConstants.FAIL) {
    // try {
    // final int newIdentifier = HDFLibrary.SDselect(
    // h4SDSCollectionOwner.getIdentifier(), index);
    // if (newIdentifier != identifier) {
    // if (LOGGER.isLoggable(Level.WARNING))
    // LOGGER.log(Level.WARNING,
    // "SDS identifier has changed");
    // }
    // isOpened = true;
    // init();
    // } catch (HDFException e) {
    // throw new RuntimeException("Error while opening the H4SDS",
    // e);
    // }
    // }
    // }
    // }

    /**
     * Initializes the {@link H4SDS} fields, such as dimension sizes, rank,
     * reference and so on.
     * 
     * @throws HDFException
     */
    private void init() throws HDFException {
        // checks if already initalized
        if (rank != -1)
            return;

        // //
        //
        // get basic info like name, size etc... and print it out
        //
        // //
        final int sdInfo[] = { 0, 0, 0 };
        final int datasetDimSizes[] = new int[HDFConstants.MAX_VAR_DIMS];
        String datasetName[] = { "" };

        if (identifier != HDFConstants.FAIL
                && HDFLibrary.SDgetinfo(identifier, datasetName,
                        datasetDimSizes, sdInfo)) {

            reference = new H4ReferencedObject(HDFLibrary.SDidtoref(identifier));
            name = datasetName[0];
            rank = sdInfo[0];
            dimSizes = new int[rank];
            // datasetSize = 1;
            for (int j = 0; j < rank; j++) {
                dimSizes[j] = datasetDimSizes[j];
                // when rank > 2, X and Y are the last 2 coordinates. As an
                // instance, for a 3D subdatasets, 3rd dimension has index 0.
                // if (j < rank - 2)
                // datasetSize *= datasetDimSizes[j];
            }
            sdInfo[1] = sdInfo[1] & (~HDFConstants.DFNT_LITEND);
            datatype = sdInfo[1];
            numAttributes = sdInfo[2];
            initH4();
            dimensions = new ArrayList(rank);

            HDFChunkInfo chunkInfo = new HDFChunkInfo();
            final int[] cflag = { HDFConstants.HDF_NONE };
            boolean status = HDFLibrary.SDgetchunkinfo(identifier, chunkInfo,
                    cflag);
            if (status) {
                if (cflag[0] == HDFConstants.HDF_NONE)
                    chunkSizes = null;
                else {
                    chunkSizes = new int[rank];
                    for (int k = 0; k < rank; k++)
                        chunkSizes[k] = chunkInfo.chunk_lengths[k];
                }
            }
        } else {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER
                        .log(
                                Level.WARNING,
                                "Error initializing SDS: "
                                        + (identifier != HDFConstants.FAIL ? "\n SDgetInfo returned false"
                                                : "identifier = -1"));
        }
    }

    protected void finalize() throws Throwable {
        dispose();
    }

    /**
     * close this {@link H4SDS} and its owned {@link AbstractHObject}s and
     * dispose allocated objects.
     */
    public synchronized void dispose() {
        if (dimensions != null) {
            final int dimSizes = dimensions.size();
            if (dimSizes != 0) {
                for (int i = 0; i < dimSizes; i++) {
                    H4Dimension dim = (H4Dimension) dimensions.get(i);
                    dim.dispose();
                }
            }
            dimensions.clear();
            dimensions = null;
        }
        if (descAnnotations != null) {
            for (int i = 0; i < nDescriptions; i++) {
                H4Annotation ann = (H4Annotation) descAnnotations.get(i);
                ann.close();
            }
            descAnnotations.clear();
            descAnnotations = null;
        }
        if (labelAnnotations != null) {
            for (int i = 0; i < nLabels; i++) {
                H4Annotation ann = (H4Annotation) labelAnnotations.get(i);
                ann.close();
            }
            labelAnnotations.clear();
            labelAnnotations = null;
        }

        // Disposing objects hold by H4DecoratedObject superclass
        super.dispose();
        close();
        identifier = HDFConstants.FAIL;
    }

    /**
     * Terminate access to this SDS.
     */
    public synchronized void close() {
        // ----------------
        // OLD STRATEGY:
        // ----------------
        // During the H4SDSCollection initialization, all SDSs are opened,
        // initialized and immediatly closed. When a SDS is required, the
        // H4SDSCollection re-open access to the specific SDS. When a
        // H4SDSCollection is closed, it attempts to close all H4SDSs by
        // calling the close method of each of them. Only the really opened
        // H4SDSs need to be closed.
        if (isOpened) {
            try {
                if (identifier != HDFConstants.FAIL) {
                    boolean closed = HDFLibrary.SDendaccess(identifier);
                    if (!closed) {
                        if (LOGGER.isLoggable(Level.WARNING))
                            LOGGER.log(Level.WARNING,
                                    "Unable to close access to the specified SDS having identifier:"
                                            + identifier);
                    }
                    isOpened = false;
                }
            } catch (HDFException e) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING,
                            "Error closing access to the specified SDS"
                                    + identifier);
            }
        }
    }

    /**
     * Read all data from the current SDS.
     * 
     * @return an array of the proper type containing read data
     * @throws HDFException
     */
    public Object read() throws HDFException {
        final int start[] = new int[rank];
        final int stride[] = new int[rank];
        final int size[] = new int[rank];

        for (int i = 0; i < rank; i++) {
            start[i] = 0;
            stride[i] = 1;
            size[i] = dimSizes[i];
        }
        return read(start, stride, size);
    }

    /**
     * Read a required zone of data, given a set of input parameters.
     * 
     * @param selectedStart
     *                int array indicating the start point of each dimension
     * @param selectedStride
     *                int array indicating the required stride of each dimension
     * @param selectedSizes
     *                int array indicating the required size along each
     *                dimension
     * @return an array of the proper type containing read data
     * @throws HDFException
     */
    public Object read(final int selectedStart[], final int selectedStride[],
            final int selectedSizes[]) throws HDFException {

        Object theData = null;
        if (identifier == HDFConstants.FAIL) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Undefined identifier for the read operation"
                                + identifier);
            return theData;
        }
        int datasize = 1;
        int[] size = new int[rank];
        int[] start = new int[rank];
        int[] stride = new int[rank];
        for (int i = 0; i < rank; i++) {
            final int dimSize = dimSizes[i];
            final int sSize = selectedSizes[i];
            final int sStart = selectedStart[i];
            final int sStride = selectedStride[i];
            if (sSize < 0
                    || sStart < 0
                    || sStart + sSize > dimSize
                    || sStride <= 0
                    || sStride > dimSize
                    || (sStride > 1 && ((sStart + (sSize-1) * sStride)) > dimSize)) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            "wrong read parameters: selectedSize = " + sSize
                                    + " selectedStart = " + sStart
                                    + " selectedStride = " + sStride);
                    return theData;
                }
            }

            size[i] = sSize;
            start[i] = sStart;
            stride[i] = sStride;
            datasize *= sSize;

        }

        if (LOGGER.isLoggable(Level.FINE)) {
            StringBuffer sb = new StringBuffer();
            sb.append("READING: \nSelected size = ");
            for (int i = 0; i < rank; i++)
                sb.append(selectedSizes[i]).append(" ");
            sb.append("\nSelected stride = ");
            for (int i = 0; i < rank; i++)
                sb.append(selectedStride[i]).append(" ");
            sb.append("\nSelected start = ");
            for (int i = 0; i < rank; i++)
                sb.append(selectedStart[i]).append(" ");
            LOGGER.log(Level.FINE, sb.toString());
        }

        // allocate the required data array where data read will be stored.
        theData = H4DatatypeUtilities.allocateArray(datatype, datasize);
        // //
        //
        // Read operation
        //
        // //
        if (theData != null) {
            HDFLibrary.SDreaddata(identifier, start, stride, size, theData);
        }

        return theData;
    }

    /**
     * Returns compression info related to this SDS
     * 
     * @return the type of compression.
     * @throws HDFException
     */
    public int getCompression() throws HDFException {
        final HDFCompInfo compInfo = new HDFCompInfo();
        HDFLibrary.SDgetcompress(identifier, compInfo);
        return compInfo.ctype;
    }

    /**
     * Returns a <code>List</code> containing available dimensions for this
     * SDS.
     * 
     * @return a <code>List</code> of {@link H4Dimension}s
     */
    public synchronized List getDimensions() throws HDFException {
        if (dimensions.size() == 0) {
            for (int i = 0; i < rank; i++) {
                // Adding dimensions if needed
                H4Dimension dimension = new H4Dimension(this, i);
                dimensions.add(i, dimension);
            }
        }
        return dimensions;
    }

    /**
     * returns a specific dimension of this SDS, given its index.
     * 
     * @param index
     *                the index of the required dimension
     * @return the {@link H4Dimension} related to the specified index.
     * @throws HDFException
     */
    public H4Dimension getDimension(final int index) throws HDFException {
        if (index > rank || index < 0)
            throw new IndexOutOfBoundsException(
                    "Specified index is not valid. It should be greater than zero and belower than rank which is "
                            + rank);
        H4Dimension dim = (H4Dimension) getDimensions().get(index);
        return dim;
    }

    /**
     * Returns a <code>List</code> of annotations available for this SDS,
     * given the required type of annotations.
     * 
     * @param annotationType
     *                the required annotation type. One of:<BR>
     *                <code>HDFConstants.AN_DATA_LABEL</code> for label
     *                annotations<BR>
     *                <code>HDFConstants.AN_DATA_DESC</code> for description
     *                annotations
     * @return the <code>List</code> of annotations available for this SDS
     * @throws HDFException
     */
    public synchronized List getAnnotations(final int annotationType)
            throws HDFException {

        H4AnnotationManager annotationManager = h4SDSCollectionOwner
                .getH4File().getH4AnnotationManager();
        short shortRef = (short) reference.getReference();
        switch (annotationType) {
        case HDFConstants.AN_DATA_LABEL:
            if (nLabels == -1) {
                // Searching data object label annotations related to this
                // SDS using the new TAG.
                List listLabels = annotationManager.getH4Annotations(
                        HDFConstants.AN_DATA_LABEL,
                        (short) HDFConstants.DFTAG_NDG, shortRef);
                if (listLabels == null) {
                    // Searching data object label annotations related to
                    // this SDS using the obsolete TAG.
                    listLabels = annotationManager.getH4Annotations(
                            HDFConstants.AN_DATA_LABEL,
                            (short) HDFConstants.DFTAG_SDG, shortRef);
                }
                if (listLabels == null || listLabels.size() == 0) {
                    nLabels = 0;
                } else
                    nLabels = listLabels.size();
                labelAnnotations = listLabels;
            }
            return labelAnnotations;
        case HDFConstants.AN_DATA_DESC:
            if (nDescriptions == -1) {
                // Searching data object label annotations related to this
                // SDS using the new TAG.
                List listDescriptions = annotationManager.getH4Annotations(
                        HDFConstants.AN_DATA_DESC,
                        (short) HDFConstants.DFTAG_NDG, shortRef);
                if (listDescriptions == null) {
                    // Searching data object label annotations related to
                    // this SDS using the obsolete TAG.
                    listDescriptions = annotationManager.getH4Annotations(
                            HDFConstants.AN_DATA_DESC,
                            (short) HDFConstants.DFTAG_SDG, shortRef);
                }
                if (listDescriptions == null || listDescriptions.size() == 0) {
                    nDescriptions = 0;
                } else
                    nDescriptions = listDescriptions.size();
                descAnnotations = listDescriptions;
            }
            return descAnnotations;
        default:
            return null;
        }
    }
}
