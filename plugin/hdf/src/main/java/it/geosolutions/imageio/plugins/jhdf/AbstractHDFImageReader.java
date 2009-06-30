/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.hdf.object.h4.H4File;
import it.geosolutions.hdf.object.h4.H4SDS;
import it.geosolutions.hdf.object.h4.H4SDSCollection;
import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

public abstract class AbstractHDFImageReader extends BaseImageReader {

	public static final String SEPARATOR = "$_$";
	
    protected final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jhdf");

    /** set it to <code>true</code> when initialization has been performed */
    private boolean isInitialized = false;

    /** The originating <code>File</code> */
    private File originatingFile = null;

    /**
     * A <code>Map</code> having all the <code>Dataset</code>s contained
     * within the source
     */
    protected Map<String, H4SDS> subDatasetsMap;

    /** The collection containing SDSs */
    private H4SDSCollection h4SDSCollection = null;

    protected synchronized H4SDSCollection getH4SDSCollection() {
        return h4SDSCollection;
    }

    /**
     * Returns a subDataset given a subDatasetIndex
     * 
     * @param subDatasetIndex
     *                The index of the required subDataset
     * @return the required subDataset
     */
    protected H4SDS retrieveDataset(int subDatasetIndex) {
        Set<String> set = subDatasetsMap.keySet();
        Iterator<String> it = set.iterator();
        for (int j = 0; j < subDatasetIndex; j++)
            it.next();
        return subDatasetsMap.get(it.next());
    }

    /**
     * Additional initialization for a specific HDF "Profile". Depending on the
     * HDF data producer, the originating file has a proper data/metadata
     * structure. For this reason, a specific initialization should be
     * implemented for each different HDF "Profile". As an instance, the
     * Automated Processing System (APS) produces HDF files having a different
     * structure with respect to the HDF structure of a file produced by TIROS
     * Operational Vertical Sounder (TOVS).
     * 
     * @throws Exception
     */
    protected abstract void initializeProfile() throws IOException;

    /** The originating H4File */
    private H4File h4file = null;

    protected AbstractHDFImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {

        // ////////////////////////////////////////////////////////////////////
        //
        // Reset the state of this reader
        //
        // Prior to set a new input, I need to do a pre-emptive reset in order
        // to clear any value-object related to the previous input.
        // ////////////////////////////////////////////////////////////////////

        // TODO: Add URL & String support.
        if (originatingFile != null)
            reset();
        try {

            // TODO: Check this
            // if (input instanceof URI) {
            // input = ((URI) input).toURL();
            // }
            if (originatingFile == null) {

                if (input instanceof File) {
                    originatingFile = (File) input;
                } else if (input instanceof String) {
                    originatingFile = new File((String) input);

                } else if (input instanceof URL) {
                    final URL tempURL = (URL) input;
                    if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                        originatingFile = URLImageInputStreamSpi
                                .urlToFile(tempURL);
                    }
                } else if (input instanceof FileImageInputStreamExt) {
                    originatingFile = ((FileImageInputStreamExt) input)
                            .getFile();
                }
            }
            super.setInput(input, seekForwardOnly, ignoreMetadata);
            initialize();
        } catch (IOException e) {
            throw new IllegalArgumentException("Not a Valid Input", e);
        }
    }

    public void setInput(Object input, boolean seekForwardOnly) {
        this.setInput(input, seekForwardOnly, true);
    }

    public void setInput(Object input) {
        this.setInput(input, true, true);
    }

    /**
     * Simple initialization method
     */
    protected synchronized void initialize() throws IOException {
        if (!isInitialized) {
            if (originatingFile == null)
                throw new IOException(
                        "Unable to Initialize data. Provided Input is not valid");
            final String fileName = originatingFile.getAbsolutePath();
            h4file = new H4File(fileName);
            h4SDSCollection = h4file.getH4SdsCollection();
            if (h4SDSCollection == null)
                throw new IOException(
                        "The provided file does not contain any SDS object!");

            // initialize information specific to this profile
            initializeProfile();
            isInitialized = true;
        }
    }

    public synchronized void dispose() {
        super.dispose();
        try {
            if (originatingFile != null) {
                originatingFile = null;
                isInitialized = false;
                h4file.dispose();
                h4file = null;
            }
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,
                        "Error occurred while disposing reader");
        }
    }

    public File getOriginatingFile() {
        return originatingFile;
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        // TODO message and/or implement
        throw new UnsupportedOperationException();
    }

    protected H4File getH4file() {
        return h4file;
    }

    public synchronized void reset() {
        super.setInput(null, false, false);
        dispose();
    }
}
