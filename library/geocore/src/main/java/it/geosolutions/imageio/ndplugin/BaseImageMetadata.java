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
package it.geosolutions.imageio.ndplugin;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;

import org.w3c.dom.Node;

/**
 * A basic abstract class containing common metadata such as basic raster
 * properties
 * 
 * TODO  @TODO: Add UOM management (Maybe, it is more appropriate to do on the upper
 *        class).
 */
public abstract class BaseImageMetadata extends CoreCommonImageMetadata {

    public final static String ATTRIBUTES_NODE = "Attributes";

    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger.getLogger(BaseImageMetadata.class.toString());

    protected final BaseImageReader imageReader;

    private int imageIndex;

    private Node commonNativeTree;

    /**
     * <code>BaseImageMetadata</code> constructor.
     * 
     * @param reader
     *                the reader used to obtain metadata.
     * @param name
     *                the name to be set for the dataset represented by this
     *                common metadata object.
     */
    protected BaseImageMetadata(BaseImageReader reader, int imageIndex) {
        super(false, nativeMetadataFormatName, nativeMetadataFormatClassName,null, null);
    	if (reader == null)
            throw new NullPointerException("The provided reader is null");
        
        this.imageIndex = imageIndex;
        imageReader = reader;

        // //
        //
        // Initializing member if needed
        //
        // //
        try {
            setMembers(imageReader);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING,"Error setting metadata \n"+ e.getLocalizedMessage(),e);
        }
    }

    protected void setMembers(final BaseImageReader imageReader) throws IOException {
        // Retrieving raster properties
        setWidth(imageReader.getWidth(imageIndex));
        setHeight(imageReader.getHeight(imageIndex));
        setTileWidth(imageReader.getTileWidth(imageIndex));
        setTileHeight(imageReader.getTileHeight(imageIndex));

        // TODO: Should I expose a getNumBands method instead of leveraging
        // on imageTypeSpecifier?
        Iterator<ImageTypeSpecifier> it = imageReader.getImageTypes(imageIndex);
        if (it != null && it.hasNext()) {
            final ImageTypeSpecifier its = it.next();
            setNumBands(its.getSampleModel().getNumBands());
        }
    }

    protected int getImageIndex() {
        return imageIndex;
    }

    @Override
    protected synchronized Node createCommonNativeTree() {
        if (this.commonNativeTree == null)
            commonNativeTree = super.createCommonNativeTree();
        return commonNativeTree;

    }

	public BaseImageReader getImageReader() {
		return imageReader;
	}
}
