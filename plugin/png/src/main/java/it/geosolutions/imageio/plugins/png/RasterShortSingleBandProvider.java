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

import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

/**
 * A scanline provider optimized for a Raster with 16 bit gray pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterShortSingleBandProvider extends AbstractScanlineProvider {

    final short[] shorts;

    public RasterShortSingleBandProvider(Raster raster) {
        super(raster, 16, raster.getWidth() * 2);
        this.shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
    }
    
    public RasterShortSingleBandProvider(Raster raster, int bidDepth, int scanlineLength, IndexColorModel palette) {
        super(raster, bidDepth, scanlineLength, palette);
        this.shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
    }

    
    public void next(final byte[] scanline, final int offset, final int length) {
        int shortsIdx = cursor.next();
        int i = offset;
        int max = offset + length;
        while (i < max) {
            short gray = shorts[shortsIdx++];
            scanline[i++] = (byte) ((gray >> 8) & 0xFF);
            if(i < max) {
                scanline[i++] = (byte) (gray & 0xFF);
            }
        }
    }

}
