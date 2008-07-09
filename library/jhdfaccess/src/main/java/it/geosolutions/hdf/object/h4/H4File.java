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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Main class needed to access any HDF source.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4File extends AbstractHObject implements IHObject {

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

//    /**
//     * Almost all HDF APIs require the preliminary open of the source file using
//     * the <code>HOpen</code> routine. However, the SD interface, used to
//     * access SDS dataset, does not uses this routine, but the
//     * <code>SDStart</code> one. This variable will become <code>true</code>
//     * when HOpen will be called.
//     */
//    private boolean hOpened = false;

    /**
     * The list of file label annotations related to this file.
     */
    private List labelAnnotations = null;

    /**
     * the number of file labels annotations related to this file.
     */
    private int nLabels = -1;

    /**
     * The list of file description annotations related to this file.
     */
    private List descAnnotations = null;

    /**
     * the number of file description annotations related to this file.
     */
    private int nDescriptions = -1;

    /**
     * the {@link H4AnnotationManager} instance of this {@link H4File}
     * 
     * @uml.associationEnd inverse="h4File:it.geosolutions.hdf.object.h4.H4AnnotationManager"
     */
    private H4AnnotationManager h4AnnotationManager = null;

    /**
     * the {@link H4SDSCollection} instance of this {@link H4File}
     * 
     * @uml.associationEnd inverse="h4File:it.geosolutions.hdf.object.h4.H4SDSCollection"
     */
    private H4SDSCollection h4SdsCollection = null;

    /**
     * the {@link H4GRImageCollection} instance of this {@link H4File}
     * 
     * @uml.associationEnd inverse="h4File:it.geosolutions.hdf.object.h4.H4GRImageCollection"
     */
    private H4GRImageCollection h4GRImageCollection = null;

    /**
     * the {@link H4VGroupCollection} instance of this {@link H4File}
     * 
     * @uml.associationEnd inverse="h4File:it.geosolutions.hdf.object.h4.H4VGroupCollection"
     */
    private H4VGroupCollection h4VGroupCollection = null;

    /**
     * The file path of HDF file referred by this {@link H4File} instance
     */
    private String filePath;

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////
    /**
     * getter of <code>h4AnnotationManager</code>
     * 
     * @return the {@link H4AnnotationManager} instance of this {@link H4File}
     */
    synchronized H4AnnotationManager getH4AnnotationManager() {
        if (h4AnnotationManager == null)
            h4AnnotationManager = new H4AnnotationManager(this);
        return h4AnnotationManager;
    }

    /**
     * getter of <code>h4GRImageCollection</code>
     * 
     * @return the {@link H4GRImageCollection} instance of this {@link H4File}
     */
    public synchronized H4GRImageCollection getH4GRImageCollection() {
        if (h4GRImageCollection == null)
            h4GRImageCollection = new H4GRImageCollection(this);
        return h4GRImageCollection;
    }

    /**
     * getter of <code>h4SdsCollection</code>
     * 
     * @return the {@link H4SDSCollection} instance of this {@link H4File}
     */
    public synchronized H4SDSCollection getH4SdsCollection() {
        if (h4SdsCollection == null)
            h4SdsCollection = new H4SDSCollection(this);
        return h4SdsCollection;
    }

    /**
     * getter of <code>h4VGroupCollection</code>
     * 
     * @return the {@link H4VGroupCollection} instance of this {@link H4File}
     */
    public synchronized H4VGroupCollection getH4VGroupCollection() {
        if (h4VGroupCollection == null)
            h4VGroupCollection = new H4VGroupCollection(this);
        return h4VGroupCollection;
    }

    /**
     * getter of <code>nDescriptions</code>
     * 
     * @return the number of file description annotations related to this File.
     */
    public synchronized int getNDescriptions() {
        if (nDescriptions == -1)
            try {
                getAnnotations(HDFConstants.AN_FILE_DESC);
            } catch (HDFException e) {
                nDescriptions = 0;
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING,
                            "Error getting file description annotations");

            }
        return nDescriptions;
    }

    /**
     * getter of <code>nLabels</code>
     * 
     * @return the number of file label annotations related to this File.
     */
    public synchronized int getNLabels() {
        if (nLabels == -1)
            try {
                getAnnotations(HDFConstants.AN_FILE_LABEL);
            } catch (HDFException e) {
                nLabels = 0;
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING,
                            "Error getting file label annotations");
            }
        return nLabels;
    }

    /**
     * getter of <code>filePath</code>
     * 
     * @return the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Constructor of a {@link H4File} which builds a new instance of
     * {@link H4File}, given a file path.
     * 
     * @param path
     *                the path of the specified file.
     */
    public H4File(final String path) {
        if (path == null)
            throw new IllegalArgumentException("The specified filePath is null");
        try {
            if (HDFLibrary.Hishdf(path))
                filePath = path;
            else
                throw new IllegalArgumentException(
                        "The specified file is not a valid HDF source " + path);

            final int identifier = HDFLibrary.Hopen(filePath);
            if (identifier != HDFConstants.FAIL)
                setIdentifier(identifier);
            else
                throw new IllegalStateException("Error while opening the file "
                        + filePath + " due to unable to find the file ID");
        } catch (HDFException e) {
            throw new IllegalStateException(
                    "Error while checking the provided file", e);
        }

    }

    /**
     * Close access to this H4File and try to close all other related
     * opened/accessed interfaces and items.
     */
    public synchronized void dispose() {
        final int identifier = getIdentifier();
        if (identifier != HDFConstants.FAIL)
            try {
                filePath = null;
                // //
                //
                // Closing all opened interfaces
                //
                // //

                if (h4GRImageCollection != null)
                    h4GRImageCollection.dispose();
                if (h4VGroupCollection != null)
                    h4VGroupCollection.dispose();
                if (h4SdsCollection != null)
                    h4SdsCollection.dispose();

                // closing annotation Interface
                if (h4AnnotationManager != null) {
                    // End access to file annotations
                    if (descAnnotations != null) {
                        final int annSize = descAnnotations.size();
                        if (annSize != 0) {
                            for (int i = 0; i < annSize; i++) {
                                H4Annotation annotation = (H4Annotation) descAnnotations
                                        .get(i);
                                annotation.dispose();
                            }
                        }
                    }
                    if (labelAnnotations != null) {
                        final int annSize = labelAnnotations.size();
                        if (annSize != 0) {
                            for (int i = 0; i < annSize; i++) {
                                H4Annotation annotation = (H4Annotation) labelAnnotations
                                        .get(i);
                                annotation.dispose();
                            }
                        }
                    }
                    h4AnnotationManager.dispose();
                }

                // //
                //
                // closing file
                //
                // //
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, "Closing File with ID = "
                            + identifier);
                boolean closed = HDFLibrary.Hclose(identifier);
                if (!closed) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING,
                                "Unable to close access to file with ID = "
                                        + identifier);
                }

            } catch (HDFException e) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, "Error closing access to file");
            }
        super.dispose();
    }

    /**
     * Returns an unmodifiable <code>List</code> of annotations available for
     * this file, given the required type of annotations.
     * 
     * @param annotationType
     *                the required annotation type. Supported values are
     *                <code>HDFConstants.AN_FILE_DESC</code> for description
     *                annotations and <code>HDFConstants.AN_FILE_LABEL</code>
     *                for label annotations. Anyway, using this method for
     *                requesting data object annotations will return
     *                <code>null</code>.
     * @return the <code>List</code> of annotations available for this File.
     *         An empty {@link List} if annoation type is incorrect.
     * @throws HDFException
     */
    public synchronized List getAnnotations(final int annotationType)
            throws HDFException {
        // Get access to the annotations interface
        List returnedAnnotations = null;
        getH4AnnotationManager();
        switch (annotationType) {
        case HDFConstants.AN_FILE_LABEL:
            if (nLabels == -1) {// Annotations not yet initialized.
                nLabels = h4AnnotationManager.getNFileLabels();
                labelAnnotations = h4AnnotationManager
                        .getH4Annotations(annotationType);

            }
            if (nLabels > 0)
                returnedAnnotations = Collections
                        .unmodifiableList(labelAnnotations);
            break;
        case HDFConstants.AN_FILE_DESC:
            if (nDescriptions == -1) {// Annotations not yet initialized.
                nDescriptions = h4AnnotationManager.getNFileDescriptions();
                descAnnotations = h4AnnotationManager
                        .getH4Annotations(annotationType);
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

    protected void finalize() throws Throwable {
        try {
            dispose();
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Catched exception during file finalization: "
                                + e.getLocalizedMessage());
        }
    }
}
