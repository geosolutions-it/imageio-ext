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
package it.geosolutions.imageio.plugins.png;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

/**
 * A scanline provider optimized for Raster objects containing a 8bit gray and alpha bands
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteGrayAlphaProvider extends AbstractScanlineProvider {

    final static int[] PIXEL_STRIDES = new int[]{2};

    final byte[] bytes;

    boolean alphaFirst;

    int[] bandOffsets;

    int pixelStride;

    int numBands;

    public RasterByteGrayAlphaProvider(Raster raster) {
        super(raster, 8, raster.getWidth() * computePixelStride(raster, PIXEL_STRIDES));
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        this.bandOffsets = sm.getBandOffsets();
        this.numBands = sm.getNumBands();
        this.pixelStride = sm.getPixelStride();
        this.alphaFirst = bandOffsets[0] != 0;
    }

    public void next(final byte[] row, final int offset, final int length) {
        int bytesIdx = cursor.next();
        if (!alphaFirst && (numBands == pixelStride)) {
            System.arraycopy(bytes, bytesIdx, row, offset, length);
        } else {
            int i = offset;
            final int max = offset + length;
            while (i < max) {
                for (int j = 0; j < numBands; j++) {
                    row[i + j] = bytes[bytesIdx + bandOffsets[j]];
                }
                bytesIdx += pixelStride;
                i += numBands;
            }
        }
    }

}
