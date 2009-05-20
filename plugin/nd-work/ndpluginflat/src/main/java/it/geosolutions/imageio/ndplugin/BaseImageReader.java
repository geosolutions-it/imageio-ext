/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.ndplugin;

import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

public abstract class BaseImageReader extends ImageReader {

    private int numImages = -1;

    public int getNumImages(boolean allowSearch) throws IOException {
        return numImages;
    }

    protected void setNumImages(int numImages) {
        if (this.numImages == -1)
            this.numImages = numImages;
        // TODO: Throws an exception if overwriting numImages?
    }

    protected BaseImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public synchronized void dispose() {
        numImages = -1;
    }
    
    /**
     * Simple check of the specified image index. Valid indexes are belonging
     * the range [0 - numImages]. In case this constraint is not respected, an
     * {@link IndexOutOfBoundsException} is thrown.
     * 
     * @param imageIndex
     *                the index to be checked
     * 
     * @throw {@link IndexOutOfBoundsException} in case the provided imageIndex
     *        is not in the range of supported ones.
     */
    protected void checkImageIndex(final int imageIndex) {
        if (imageIndex < 0 || imageIndex >= numImages) {
            throw new IndexOutOfBoundsException(
                    "Invalid imageIndex. It should "
                            + (numImages > 0 ? ("belong the range [0," + (numImages - 1))
                                    : "be 0"));
        }
    }
}
