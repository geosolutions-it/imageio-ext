/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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
 * A scanline provider that packs more than one pixel per output byte
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteRepackSingleBandProvider extends AbstractScanlineProvider {

    final byte[] bytes;

    public RasterByteRepackSingleBandProvider(Raster raster, int bitDepth, int scanlineLength) {
        super(raster, bitDepth, scanlineLength,  8 / bitDepth);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
    }

    public RasterByteRepackSingleBandProvider(Raster raster, int bitDepth, int scanlineLength,
            IndexColorModel palette) {
        super(raster, bitDepth, scanlineLength, 8 / bitDepth, palette);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
    }

    
    public void next(final byte[] row, final int offset, final int length) {
        if (this.currentRow == height) {
            throw new IllegalStateException("All scanlines have been read already");
        }

        int pxIdx = cursor.next();
        final int pxLimit = pxIdx + width;
        int i = offset;
        final int max = offset + length;
        if (bitDepth == 4) {
            while (i < max) {
                final int low = bytes[pxIdx++];
                final int high = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                row[i++] = (byte) ((low << 4) | high);
            }
        } else if (bitDepth == 2) {
            while (i < max) {
                final int b1 = bytes[pxIdx++];
                final int b2 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b3 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b4 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                row[i++] = (byte) (b4 | (b3 << 2) | (b2 << 4) | (b1 << 6));
            }
        } else {
            while (i < max) {
                final int b1 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b2 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b3 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b4 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b5 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b6 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b7 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                final int b8 = pxIdx < pxLimit ? bytes[pxIdx++] : 0;
                row[i++] = (byte) (b8 | (b7 << 1) | (b6 << 2) | (b5 << 3) | (b4 << 4) | (b3 << 5)
                        | (b2 << 6) | (b1 << 7));
            }
        }

        currentRow++;
    }

}
