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

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class providing access to HDF annotations. It is worth to point out that it
 * does not internally store any instance of {@link H4Annotation} but simply
 * allows to build them when required.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
class H4AnnotationManager extends AbstractHObject implements IHObject {

    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    /**
     * The number of file label annotations <br>
     */
    private int nFileLabels = -1;

    /**
     * The number of file description annotations <br>
     */
    private int nFileDescriptions = -1;

    /**
     * The number of total data object label annotations <br>
     */
    private int nDataObjectLabels = -1;

    /**
     * The number of total data object description annotations <br>
     */
    private int nDataObjectDescriptions = -1;

    /**
     * the {@link H4File} to which this collection is attached
     * 
     * @uml.associationEnd inverse="H4AnnotationManager:it.geosolutions.hdf.object.h4.H4File"
     */
    private H4File h4File;

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////

    /**
     * getter of <code>nDataObjectDescriptions</code>
     * 
     * @return Returns the nDataObjectDescriptions.
     */
    public int getNDataObjectDescriptions() {
        return nDataObjectDescriptions;
    }

    /**
     * getter of <code>nDataObjectLabels</code>
     * 
     * @return Returns the nDataObjectLabels.
     */
    public int getNDataObjectLabels() {
        return nDataObjectLabels;
    }

    /**
     * getter of <code>nFileDescriptions</code>
     * 
     * @return Returns the nFileDescriptions.
     */
    public int getNFileDescriptions() {
        return nFileDescriptions;
    }

    /**
     * getter of <code>nFileLabels</code>
     * 
     * @return Returns the nFileLabels.
     */
    public int getNFileLabels() {
        return nFileLabels;
    }

    /**
     * getter of <code>h4File</code>
     * 
     * @return the {@link H4File} to which this collection is attached
     */
    H4File getH4File() {
        return h4File;
    }

    /**
     * Main constructor which builds a <code>H4AnnotationManager</code> given
     * an input {@link H4File}.
     * 
     * @param h4file
     *                the input {@link H4File}
     * @throws IllegalArgumentException
     *                 in case of wrong specified input parameters or
     *                 identifiers.
     * @throws IllegalStateException
     *                 in case of problems getting a valid identifier for the
     *                 Annotation APIs
     */
    public H4AnnotationManager(H4File h4file) {
        h4File = h4file;
        if (h4file == null)
            throw new IllegalArgumentException("Null file provided");
        final int fileID = h4File.getIdentifier();
        if (fileID == HDFConstants.FAIL)
            throw new IllegalArgumentException("Invalid file identifier");
        try {
            // Open the Annotation Interface and set its identifier
            int identifier = HDFLibrary.ANstart(fileID);
            if (identifier != HDFConstants.FAIL) {
                setIdentifier(identifier);
                // Retrieve basic annotations properties.
                int annotationsInfo[] = new int[] { 0, 0, 0, 0 };
                HDFLibrary.ANfileinfo(identifier, annotationsInfo);
                nFileLabels = annotationsInfo[0];
                nFileDescriptions = annotationsInfo[1];
                nDataObjectLabels = annotationsInfo[2];
                nDataObjectDescriptions = annotationsInfo[3];
            } else {
                throw new IllegalStateException(
                        "Unable to find annotation in the provided file");
            }

        } catch (HDFException e) {
            throw new IllegalStateException(
                    "HDFException occurred while accessing to annotation routines ",
                    e);
        }
    }

    /**
     * Builds and returns a <code>List</code> of {@link H4Annotation}s
     * available for a specific data object, given the required type of
     * annotation, and the TAG and reference of the data object.<BR>
     * If you are looking for file annotations, you have to use
     * {@link H4AnnotationManager#getH4Annotations(int)} instead.
     * 
     * @param annotationType
     *                the required type of annotation. Supported values are
     *                <code>HDFConstants.AN_DATA_DESC</code> for data object
     *                descriptions and <code>HDFConstants.AN_DATA_LABEL</code>
     *                for data object labels. File annotations will not
     *                retrieved by this method since specifying TAG and
     *                Reference does not have sense for file. Anyway, using this
     *                method for requesting file annotations will return
     *                <code>null</code>.
     * @param requiredTag
     *                the TAG of the required object.
     * @param requiredReference
     *                the reference of the required object.
     * @return a <code>List</code> of {@link H4Annotation}s.
     * @throws HDFException
     */
    List getH4Annotations(final int annotationType, final short requiredTag,
            final short requiredReference) throws HDFException {
        List annotations = null;
        switch (annotationType) {
        case HDFConstants.AN_DATA_DESC:
        case HDFConstants.AN_DATA_LABEL:
            final int numTag = HDFLibrary.ANnumann(getIdentifier(),
                    annotationType, requiredTag, requiredReference);
            if (numTag > 0) {
                final int annIDs[] = new int[numTag];
                HDFLibrary.ANannlist(getIdentifier(), annotationType,
                        requiredTag, requiredReference, annIDs);
                annotations = new ArrayList(numTag);
                for (int k = 0; k < numTag; k++) {
                    H4Annotation annotation = new H4Annotation(annIDs[k]);
                    annotations.add(k, annotation);
                }
            }
        }
        return annotations;
    }

    /**
     * Use this method to retrieve the <code>List</code> of
     * {@link H4Annotation}s available for the file represented by the
     * {@link H4File} owner of this <code>H4AnnotationManager</code>. If you
     * are looking for data object annotations, you have to use
     * {@link H4AnnotationManager#getH4Annotations(int, short, short)} instead,
     * since data object annotations are related to a specific Object identified
     * by a couple <TAG,reference>.
     * 
     * @param annotationType
     *                the required type of annotation. Supported values are
     *                <code>HDFConstants.AN_FILE_DESC</code> and
     *                <code>HDFConstants.AN_FILE_LABEL</code>. Anyway, using
     *                this method for requesting data object annotations will
     *                return <code>null</code>.
     * @return a <code>List</code> of {@link H4Annotation}s.
     * @throws HDFException
     */
    List getH4Annotations(final int annotationType) throws HDFException {
        List annotations = null;
        switch (annotationType) {
        case HDFConstants.AN_FILE_LABEL:
            if (nFileLabels != 0) {
                annotations = new ArrayList(nFileLabels);
                for (int i = 0; i < nFileLabels; i++) {
                    final int annID = HDFLibrary.ANselect(getIdentifier(), i,
                            HDFConstants.AN_FILE_LABEL);
                    H4Annotation annotation = new H4Annotation(annID);
                    annotations.add(i, annotation);
                }
            }
            break;
        case HDFConstants.AN_FILE_DESC:
            if (nFileDescriptions != 0) {
                annotations = new ArrayList(nFileDescriptions);
                for (int i = 0; i < nFileDescriptions; i++) {
                    final int annID = HDFLibrary.ANselect(getIdentifier(), i,
                            HDFConstants.AN_FILE_DESC);
                    H4Annotation annotation = new H4Annotation(annID);
                    annotations.add(i, annotation);
                }
            }
            break;

        case HDFConstants.AN_DATA_DESC:
        case HDFConstants.AN_DATA_LABEL:
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER
                        .log(Level.WARNING,
                                "Direct use of annotation manager only allows to get File annotations.");
            }
        }
        return annotations;
    }

    /**
     * End access to the underlying annotation routine interface.
     */
    public void dispose() {
        final int identifier = getIdentifier();
        if (identifier != HDFConstants.FAIL) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE,
                        "disposing annotation interface with ID = "
                                + identifier);
            try {
                boolean closed = HDFLibrary.ANend(identifier);
                if (!closed) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "Unable to close access to the annotation interface with ID = "
                                        + identifier);
                }
            } catch (HDFException e) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING,
                            "Error closing access to the annotation interface with ID = "
                                    + identifier);
            }
        }
        super.dispose();
    }
}
