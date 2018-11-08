/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2018, GeoSolutions
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
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;

/**
 * A scanline provider that copy data from the buffered image into the scanline 
 * by skipping some bytes due to pixelStride not equal to number of bands
 * (There might be some bandSelect happening)
 * 
 * @author Daniele Romagnoli - GeoSolutions
 */
public final class RasterByteSingleBandSkippingBytesProvider extends AbstractScanlineProvider {

    final static int[] PIXEL_STRIDES = new int[]{1};

    int pixelStride;

    final byte[] bytes;

    int[] bandOffsets;

    int numBands;

    public RasterByteSingleBandSkippingBytesProvider(Raster raster) {
        super(raster, 8, raster.getWidth() * computePixelStride(raster, PIXEL_STRIDES));
        PixelInterleavedSampleModel sm = (PixelInterleavedSampleModel) raster.getSampleModel();
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        this.pixelStride = sm.getPixelStride();
        this.numBands = sm.getNumBands();
        this.bandOffsets = sm.getBandOffsets();
    }

    public void next(final byte[] scanline, final int offset, final int length) {
        if (this.currentRow == height) {
            throw new IllegalStateException("All scanlines have been read already");
        }

        int bytesIdx = cursor.next();
        if (numBands == pixelStride) {
            System.arraycopy(bytes, bytesIdx, scanline, offset, length);
        } else {
            int i = offset;
            final int max = offset + length;
            while (i < max) {
                for (int j = 0; j < numBands; j++) {
                    scanline[i + j] = bytes[bytesIdx + bandOffsets[j]];
                }
                bytesIdx += pixelStride;
                i += numBands;
            }
        }
        currentRow++;
    }

}
