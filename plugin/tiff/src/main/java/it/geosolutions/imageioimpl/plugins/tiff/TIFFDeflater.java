/*
 * $RCSfile: TIFFDeflater.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 05:01:45 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2007 - 2009, GeoSolutions
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


import it.geosolutions.imageio.compression.CompressionType;
import it.geosolutions.imageio.compression.Compressor;
import it.geosolutions.imageio.compression.CompressionFinder;
import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFCompressor;

import java.io.IOException;
import java.util.zip.Deflater;
import javax.imageio.ImageWriteParam;

/**
 * Compressor superclass for Deflate and ZLib compression.
 */
public class TIFFDeflater extends TIFFCompressor {
    Compressor deflateCompressor;

    int predictor;

    public TIFFDeflater(String compressionType,
                        int compressionTagValue,
                        ImageWriteParam param, 
			int predictorValue) {
        super(compressionType, compressionTagValue, true);

	this.predictor = predictorValue;

        // Set the deflate level.
        int deflateLevel;
        if (param != null &&
           param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
            float quality = param.getCompressionQuality();
            deflateLevel = (int)(1 + 8*quality);
        } else {
            deflateLevel = Deflater.DEFAULT_COMPRESSION;
        }
        deflateCompressor = CompressionFinder.getCompressor(deflateLevel, CompressionType.DEFLATE);

    }

    public int encode(byte[] b, int off,
                      int width, int height,
                      int[] bitsPerSample,
                      int scanlineStride) throws IOException {

        int inputSize = height*scanlineStride;
        int blocks = (inputSize + 32767)/32768;

        // Worst case for Zlib deflate is input size + 5 bytes per 32k
        // block, plus 6 header bytes
        byte[] compData = new byte[inputSize + 5*blocks + 6];

        int numCompressedBytes = 0;
        if(predictor == BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING) {
            int samplesPerPixel = bitsPerSample.length;
            int bitsPerPixel = 0;
            for (int i = 0; i < samplesPerPixel; i++) {
                bitsPerPixel += bitsPerSample[i];
            }
            int bytesPerRow = (bitsPerPixel*width + 7)/8;
            byte[] rowBuf = new byte[bytesPerRow];

            int maxRow = height - 1;
            for(int i = 0; i < height; i++) {
                // Cannot modify b[] in place as it might be a data
                // array from the image being written so make a copy.
                System.arraycopy(b, off, rowBuf, 0, bytesPerRow);
                for(int j = bytesPerRow - 1; j >= samplesPerPixel; j--) {
                    rowBuf[j] -= rowBuf[j - samplesPerPixel];
                }

                int numBytes = 0;

                deflateCompressor.setInput(rowBuf);
                if (i == maxRow) {
                    deflateCompressor.finish();
                }
                while((numBytes = deflateCompressor.compress(
                        compData, 0, rowBuf.length,
                        numCompressedBytes, compData.length - numCompressedBytes)) != 0) {
                    numCompressedBytes += numBytes;
                }
                off += scanlineStride;
            }
        } else {
            deflateCompressor.setInput(b);
            deflateCompressor.finish();
            numCompressedBytes =
                    deflateCompressor.compress(compData, off, height*scanlineStride, 0, compData.length);
        }
        deflateCompressor.done();
        stream.write(compData, 0, numCompressedBytes);

        return numCompressedBytes;
    }
}
