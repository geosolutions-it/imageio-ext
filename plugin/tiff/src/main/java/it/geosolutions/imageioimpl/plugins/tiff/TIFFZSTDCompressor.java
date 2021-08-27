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


import io.airlift.compress.zstd.ZstdCompressor;
import it.geosolutions.imageio.plugins.tiff.PrivateTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFCompressor;

import javax.imageio.ImageWriteParam;
import java.io.IOException;

/**
 * Compressor for ZSTD compression.
 */
public class TIFFZSTDCompressor extends TIFFCompressor {

    ZstdCompressor compressor = new ZstdCompressor();
    int predictor;
    ImageWriteParam param;

    public TIFFZSTDCompressor(ImageWriteParam param, int predictor) {
        super("ZSTD", PrivateTIFFTagSet.COMPRESSION_ZSTD, true);
        this.param = param;
        this.predictor = predictor;
        // Currently the java ZSTD library only support compression level 3
    }

    public int encode(byte[] b, int off,
                      int width, int height,
                      int[] bitsPerSample,
                      int scanlineStride) throws IOException {

        int inputSize = height*scanlineStride;
        int maxOutputLenght = compressor.maxCompressedLength(inputSize);

        byte[] compData = new byte[maxOutputLenght];

        int numCompressedBytes = compressor.compress(b, off, inputSize, compData, 0, maxOutputLenght);
        stream.write(compData, 0, numCompressedBytes);

        return numCompressedBytes;
    }
}
