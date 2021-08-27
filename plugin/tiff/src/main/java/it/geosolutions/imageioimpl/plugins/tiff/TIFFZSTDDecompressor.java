/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2021, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import io.airlift.compress.zstd.ZstdDecompressor;
import it.geosolutions.imageio.plugins.tiff.TIFFDecompressor;

import java.io.IOException;

/**
 * Decompressor for ZSTD compression
 */
public class TIFFZSTDDecompressor extends TIFFDecompressor {

    private final int predictor;
    ZstdDecompressor zstdDecompressor = new ZstdDecompressor();

    public TIFFZSTDDecompressor(int predictor) {
        this.predictor = predictor;
    }

    @Override
    public void decodeRaw(byte[] b, int dstOffset, int bitsPerPixel, int scanlineStride) throws IOException {
        PredictorDecompressor predictorDecompressor = new PredictorDecompressor(
                predictor, bitsPerSample, sampleFormat, samplesPerPixel, stream.getByteOrder());
        predictorDecompressor.validate();

        stream.seek(offset);
        byte[] srcData = new byte[byteCount];
        stream.readFully(srcData);

        int bytesPerRow = (srcWidth*bitsPerPixel + 7)/8;
        byte[] buf;
        int bufOffset;
        if(bytesPerRow == scanlineStride) {
            buf = b;
            bufOffset = dstOffset;
        } else {
            buf = new byte[bytesPerRow*srcHeight];
            bufOffset = 0;
        }

        zstdDecompressor.decompress(srcData, 0, byteCount, buf, bufOffset, bytesPerRow*srcHeight);
        predictorDecompressor.decompress(buf, bufOffset, dstOffset, srcHeight, srcWidth, bytesPerRow);

        if (bytesPerRow != scanlineStride) {
            int off = 0;
            for (int y = 0; y < srcHeight; y++) {
                System.arraycopy(buf, off, b, dstOffset, bytesPerRow);
                off += bytesPerRow;
                dstOffset += scanlineStride;
            }
        }
    }
}
