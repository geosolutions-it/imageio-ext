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

import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 * A scanline provider optimized for rasters with int packed RGB or RGBA pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterIntABGRProvider extends AbstractScanlineProvider {

    final int[] pixels;

    final boolean bgrOrder;
    
    final boolean hasAlpha;

    public RasterIntABGRProvider(Raster raster, boolean hasAlpha) {
        super(raster, 8, raster.getWidth() * (hasAlpha ? 4 : 3));
        this.pixels = ((DataBufferInt) raster.getDataBuffer()).getData();
        this.hasAlpha = hasAlpha;
        if (hasAlpha) {
            bgrOrder = false;
        } else {
            int[] offsets = ((SinglePixelPackedSampleModel) raster.getSampleModel()).getBitOffsets();
            bgrOrder = offsets[0] != 0;
        }
    }

    
    public void next(final byte[] row, final int offset, final int length) {
        int pxIdx = cursor.next();
        int i = offset;
        final int max = offset + length;
        if (hasAlpha) {
            while (i < max) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 24) & 0xff);
            }
        } else if (bgrOrder) {
            while (i < max) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
            }
        } else {
            while (i < max) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color >> 16) & 0xff);
            }
        }
    }

}
