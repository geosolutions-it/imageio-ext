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
package it.geosolutions.imageio.ndplugin;

import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

/** 
 * A base ImageReader class
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public abstract class BaseImageReader extends ImageReader {

    private int numRasters = -1;

    public int getNumImages(final boolean allowSearch) throws IOException {
        return numRasters;
    }

    public void setNumImages(final int numImages) {
        if (this.numRasters == -1)
            this.numRasters = numImages;
    }

    protected BaseImageReader(final ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public void dispose() {
        numRasters = -1;
    }
    
    /**
     * Simple check of the specified image index. Valid indexes are belonging
     * the range [0 - numRasters]. In case this constraint is not respected, an
     * {@link IndexOutOfBoundsException} is thrown.
     * 
     * @param imageIndex
     *                the index to be checked
     * 
     * @throw {@link IndexOutOfBoundsException} in case the provided imageIndex
     *        is not in the range of supported ones.
     */
    public void checkImageIndex(final int imageIndex) {
        if (imageIndex < 0 || imageIndex >= numRasters) {
            throw new IndexOutOfBoundsException(
                    "Invalid imageIndex. It should " + (numRasters > 0 ? ("belong the range [0," + (numRasters - 1)) : "be 0"));
        }
    }
}
