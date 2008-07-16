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
import java.util.Collections;
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
public class H4SDS extends H4Variable implements IHObject, IH4Object {

    private AbstractH4Object objectWithAttributes; 
    
    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

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
    private boolean isOpen;

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
        if (nDescriptions == -1)
            try {
                getAnnotations(HDFConstants.AN_DATA_DESC);
            } catch (HDFException e) {
                nDescriptions = 0;
            }
        return nDescriptions;
    }

    /**
     * getter of <code>nLabels</code>
     * 
     * @return the number of data object label annotations related to this SDS.
     */
    public synchronized int getNLabels() {
        if (nLabels == -1)
            try {
                getAnnotations(HDFConstants.AN_DATA_LABEL);
            } catch (HDFException e) {
                nLabels = 0;
            }
        return nLabels;
    }

    /**
     * getter of <code>h4SDSCollectionOwner</code>
     * 
     * @return the {@link H4SDSCollection} to which this {@link H4SDS} belongs.
     */
    H4SDSCollection getH4SDSCollectionOwner() {
        return h4SDSCollectionOwner;
    }

    private H4SDS(H4SDSCollection h4SdsCollection, int index, int identifier)
            throws HDFException {
        h4SDSCollectionOwner = h4SdsCollection;
        this.index = index;
        setIdentifier(identifier);
        initialize();
        isOpen = true;
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
            } else {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, "undefined SD identifier ");
            }

        } catch (HDFException e) {
            throw new RuntimeException("Error while creating a new H4SDS", e);
        }
        return sds;
    }

    /**
     * Initializes the {@link H4SDS} fields, such as dimension sizes, rank,
     * reference and so on.
     * 
     * @throws HDFException
     */
    private void initialize() throws HDFException {

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

        int identifier = getIdentifier();
        if (identifier != HDFConstants.FAIL
                && HDFLibrary.SDgetinfo(identifier, datasetName,
                        datasetDimSizes, sdInfo)) {
            reference = new H4ReferencedObject(HDFLibrary.SDidtoref(identifier));
            setName(datasetName[0]);
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
            objectWithAttributes = new H4SDSFamilyObjectsAttributesManager(identifier, sdInfo[2]);
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
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during SDS finalization: "
                                + e.getLocalizedMessage());
        }
    }

    /**
     * close this {@link H4SDS} and its owned {@link AbstractHObject}s and
     * dispose allocated objects.
     */
    public synchronized void dispose() {
        final int identifier = getIdentifier();
        if (identifier != HDFConstants.FAIL) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "disposing SDS with ID = "
                        + identifier);
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
                    ann.dispose();
                }
                descAnnotations.clear();
                descAnnotations = null;
            }
            if (labelAnnotations != null) {
                for (int i = 0; i < nLabels; i++) {
                    H4Annotation ann = (H4Annotation) labelAnnotations.get(i);
                    ann.dispose();
                }
                labelAnnotations.clear();
                labelAnnotations = null;
            }
            if (objectWithAttributes!=null){
                objectWithAttributes.dispose();
                objectWithAttributes = null;
            }
            // Disposing objects hold by H4DecoratedObject superclass
            // ----------------
            // OLD STRATEGY:
            // ----------------
            // During the H4SDSCollection initialization, all SDSs are opened,
            // initialized and immediately closed. When a SDS is required, the
            // H4SDSCollection re-open access to the specific SDS. When a
            // H4SDSCollection is closed, it attempts to close all H4SDSs by
            // calling the close method of each of them. Only the really opened
            // H4SDSs need to be closed.
            if (isOpen) {
                try {
                    boolean closed = HDFLibrary.SDendaccess(identifier);
                    if (!closed) {
                        if (LOGGER.isLoggable(Level.WARNING))
                            LOGGER.log(Level.WARNING,
                                    "Unable to close access to the SDS with ID = "
                                            + identifier);
                    }
                } catch (HDFException e) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "Error closing access to the SDS with ID = "
                                        + identifier);
                }
            }
            isOpen = false;
        }
        super.dispose();
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
     * @return an array of the proper type containing read data or
     *         <code>null</code> in case of no data read.
     * @throws HDFException
     */
    public Object read(final int selectedStart[], final int selectedStride[],
            final int selectedSizes[]) throws HDFException {

        if (getIdentifier() == HDFConstants.FAIL) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Undefined identifier for the read operation"
                                + getIdentifier());
            return null;
        }
        if (selectedStart == null || selectedStride == null
                || selectedSizes == null || selectedStart.length != rank
                || selectedSizes.length != rank
                || selectedStride.length != rank) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER
                        .log(
                                Level.WARNING,
                                "Wrong input parameters. Check the selected start/size/stride parameters are not null and their rank is equal to the the SDS rank");
            return null;
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
                    || (sStride > 1 && ((sStart + (sSize - 1) * sStride)) > dimSize)) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            "wrong read parameters: selectedSize = " + sSize
                                    + " selectedStart = " + sStart
                                    + " selectedStride = " + sStride);
                    return null;
                }
            }

            size[i] = sSize;
            start[i] = sStart;
            stride[i] = sStride;
            datasize *= sSize;

        }
        Object theData = null;
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
        theData = H4Utilities.allocateArray(datatype, datasize);
        // //
        //
        // Read operation
        //
        // //
        if (theData != null) {
            HDFLibrary
                    .SDreaddata(getIdentifier(), start, stride, size, theData);
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
        HDFLibrary.SDgetcompress(getIdentifier(), compInfo);
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
     * Returns an unmodifiable <code>List</code> of annotations available for
     * this SDS, given the required type of annotations.
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
        List returnedAnnotations = null;
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
            if (nLabels > 0)
                returnedAnnotations = Collections
                        .unmodifiableList(labelAnnotations);
            break;
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
            if (nDescriptions > 0)
                returnedAnnotations = Collections
                        .unmodifiableList(descAnnotations);
            break;
        default:
            returnedAnnotations = null;
        }
        if (returnedAnnotations == null)
            returnedAnnotations = Collections.emptyList();
        return returnedAnnotations;
    }

    /**
     * @see {@link IH4ObjectWithAttributes#getAttribute(int)}
     */
    public H4Attribute getAttribute(int attributeIndex) throws HDFException {
        return objectWithAttributes.getAttribute(attributeIndex);
    }

    /**
     * @see {@link IH4ObjectWithAttributes#getAttribute(String)}
     */
    public H4Attribute getAttribute(String attributeName) throws HDFException {
        return objectWithAttributes.getAttribute(attributeName);
    }

    /**
     * @see {@link IH4ObjectWithAttributes#getNumAttributes()}
     */
    public int getNumAttributes() {
       return objectWithAttributes.getNumAttributes();
    }
}
