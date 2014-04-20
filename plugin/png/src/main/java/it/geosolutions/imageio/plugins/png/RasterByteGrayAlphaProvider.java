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

    final byte[] bytes;

    boolean alphaFirst;

    public RasterByteGrayAlphaProvider(Raster raster) {
        super(raster, 8, raster.getWidth() * 2);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        int[] bandOffsets = ((ComponentSampleModel) raster.getSampleModel()).getBandOffsets();
        this.alphaFirst = bandOffsets[0] != 0;
    }

    
    public void next(final byte[] row, final int offset, final int length) {
        int bytesIdx = cursor.next();
        if (alphaFirst) {
            int i = offset;
            final int max = offset + length;
            while (i < max) {
                final byte a = bytes[bytesIdx++];
                final byte g = bytes[bytesIdx++];
                row[i++] = g;
                row[i++] = a;
            }
        } else {
            System.arraycopy(bytes, bytesIdx, row, offset, length);
        }
    }

}
