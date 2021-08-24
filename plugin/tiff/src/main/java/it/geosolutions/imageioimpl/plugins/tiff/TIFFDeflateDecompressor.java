/*
 * $RCSfile: TIFFDeflateDecompressor.java,v $
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
 *    http://java.net/projects/imageio-ext/
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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.IIOException;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFDecompressor;

public class TIFFDeflateDecompressor extends TIFFDecompressor {

    private static final boolean DEBUG = false;

    Inflater inflater = null;
    int predictor;

    public TIFFDeflateDecompressor(int predictor) throws IIOException {
        inflater = new Inflater();

        if (predictor != BaselineTIFFTagSet.PREDICTOR_NONE &&
            predictor != BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING &&
            predictor != BaselineTIFFTagSet.PREDICTOR_FLOATING_POINT) {
            throw new IIOException("Illegal value for Predictor in TIFF file");
        }

        if(DEBUG) {
            System.out.println("Using horizontal differencing predictor");
        }

        this.predictor = predictor;
    }

    public synchronized void decodeRaw(byte[] b,
                                       int dstOffset,
                                       int bitsPerPixel,
                                       int scanlineStride) throws IOException {

        PredictorDecompressor predictorDecompressor = new PredictorDecompressor(
                predictor, bitsPerSample, sampleFormat, samplesPerPixel, stream.getByteOrder());
        predictorDecompressor.validate();

        // Seek to current tile data offset.
        stream.seek(offset);

        // Read the deflated data.
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

        // Set the input to the Inflater.
        inflater.setInput(srcData);

        // Inflate the data.
        try {
            inflater.inflate(buf, bufOffset, bytesPerRow*srcHeight);
        } catch(DataFormatException dfe) {
            throw new IIOException(I18N.getString("TIFFDeflateDecompressor0"),
                                   dfe);
        }

        // Reset the Inflater.
        inflater.reset();
        predictorDecompressor.decompress(buf, bufOffset, dstOffset, srcHeight, srcWidth, bytesPerRow);

        if(bytesPerRow != scanlineStride) {
            if(DEBUG) {
                System.out.println("bytesPerRow != scanlineStride");
            }
            int off = 0;
            for (int y = 0; y < srcHeight; y++) {
                System.arraycopy(buf, off, b, dstOffset, bytesPerRow);
                off += bytesPerRow;
                dstOffset += scanlineStride;
            }
        }
    }
}
