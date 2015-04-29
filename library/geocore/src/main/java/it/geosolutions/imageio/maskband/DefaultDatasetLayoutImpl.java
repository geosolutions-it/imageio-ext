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
 * Default implementation for {@link DatasetLayout} interface setting default values for interface methods.
 * 
 * @author Nicola Lagomarsini GeoSolutions S.A.S.
 */
public class DefaultDatasetLayoutImpl implements DatasetLayout {

    public int getNumInternalOverviews() {
        return 0;
    }

    public int getNumExternalOverviews() {
        return 0;
    }

    public int getNumExternalMaskOverviews() {
        return 0;
    }

    public int getNumInternalMasks() {
        return 0;
    }

    public int getNumExternalMasks() {
        return 0;
    }

    public int getInternalOverviewImageIndex(int overviewIndex) {
        return overviewIndex;
    }

    public int getInternalMaskImageIndex(int maskIndex) {
        return maskIndex;
    }

    public File getExternalMasks() {
        return null;
    }

    public File getExternalOverviews() {
        return null;
    }

    public File getExternalMaskOverviews() {
        return null;
    }
}
