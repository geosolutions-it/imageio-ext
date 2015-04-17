/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2015, GeoSolutions
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
package it.geosolutions.imageio.maskband;

import java.io.File;

/**
 * Interface defining methods for accessing Internal Image structure, like overviews, masks and so on
 * 
 * @author Nicola Lagomarsini GeoSolutions S.A.S.
 */
public interface DatasetLayout {

    /**
     * Returns the number of internal Image overviews
     * 
     * @return an Integer indicating how many overviews are present
     */
    public int getNumInternalOverviews();

    /**
     * Returns the number of external Image overviews
     * 
     * @return an Integer indicating how many overviews are present
     */
    public int getNumExternalOverviews();

    /**
     * Returns the number of external mask overviews
     * 
     * @return an Integer indicating how many overviews are present
     */
    public int getNumExternalMaskOverviews();

    /**
     * Returns the total number of internal Image masks. Notice that If masks are more than one, the others are considered as overviews of the first
     * mask
     * 
     * @return an Integer indicating how many masks are present
     */
    public int getNumInternalMasks();

    /**
     * Returns the total number of externals Image masks. Notice that If masks are more than one, the others are considered as overviews of the first
     * mask
     * 
     * @return an Integer indicating how many external masks are present
     */
    public int getNumExternalMasks();

    /**
     * Returns the Overview index associated to the input image index defined. This is helpful when we have overviews and masks and we are unable to
     * distinguish them. If no overwiew is present or the overview index is greater than the maximum index, -1 will be returned.
     * 
     * @param overviewIndex Integer defining an image overview index (0 means the native image)
     * @return The Overview index related to the imageIndex defined
     */
    public int getInternalOverviewImageIndex(int overviewIndex);

    /**
     * Returns the Mask index associated to the input image index defined. This is helpful when we have overviews and masks and we are unable to
     * distinguish them. If no mask is present or the mask index is greater than the maximum index, -1 will be returned.
     * 
     * @param maskIndex Integer defining an image mask index (0 means the native image resolution)
     * @return The Overview index related to the imageIndex defined
     */
    public int getInternalMaskImageIndex(int maskIndex);

    /**
     * This methods returns a File containing external masks associated to input Image, or <code>null</code> if not present.
     * 
     * @return a {@link File} containing external masks associated to an input {@link File}
     */
    public File getExternalMasks();

    /**
     * This methods returns a File containing external overviews associated to input Image, or <code>null</code> if not present.
     * 
     * @return a {@link File} containing external overviews associated to an input {@link File}
     */
    public File getExternalOverviews();

    /**
     * This methods returns a File containing external overviews associated to external Image masks, or <code>null</code> if not present.
     * 
     * @return a {@link File} containing external mask overviews associated to an input {@link File}
     */
    public File getExternalMaskOverviews();
}
