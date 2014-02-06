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

import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

/**
 * A scanline provider that can copy 1-1 data from the buffered image into the scanline without
 * performing any kind of transformation
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteSingleBandProvider extends AbstractScanlineProvider {

    final byte[] bytes;

    public RasterByteSingleBandProvider(Raster raster, int bitDepth, int scanlineLength) {
        super(raster, bitDepth, scanlineLength);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
    }

    public RasterByteSingleBandProvider(Raster raster, int bitDepth, int scanlineLength,
            IndexColorModel palette) {
        super(raster, bitDepth, scanlineLength, palette);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
    }

    
    public void next(final byte[] scanline, final int offset, final int length) {
        if (this.currentRow == height) {
            throw new IllegalStateException("All scanlines have been read already");
        }

        final int next = cursor.next();
        System.arraycopy(bytes, next, scanline, offset, length);
        currentRow++;
    }

}
