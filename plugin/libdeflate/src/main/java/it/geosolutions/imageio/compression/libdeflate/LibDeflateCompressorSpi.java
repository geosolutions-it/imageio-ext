/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    https://www.geosolutionsgroup.com/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
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
package it.geosolutions.imageio.compression.libdeflate;

import it.geosolutions.imageio.compression.AbstractCompressorSpi;
import it.geosolutions.imageio.compression.CompressionType;
import it.geosolutions.imageio.compression.Compressor;
import it.geosolutions.imageio.compression.CompressorSpi;
import me.steinborn.libdeflate.Libdeflate;

import java.util.Collections;
import java.util.Set;

/**
 * Compressor SPI based on libdeflate java library
 */
public class LibDeflateCompressorSpi extends AbstractCompressorSpi implements CompressorSpi {

    final static int DEFAULT_PRIORITY = 80;

    private final static int DEFAUL_MAX_LEVEL = 8;

    private final static int DEFAUL_MIN_LEVEL = 1;

    public final static int getDefaultPriority() {
        return DEFAULT_PRIORITY;
    }

    public final static int getDefaultMaxLevel() {
        return DEFAUL_MAX_LEVEL;
    }

    public final static int getDefaultMinLevel() {
        return DEFAUL_MIN_LEVEL;
    }
    public LibDeflateCompressorSpi() {
        super();
        minLevel = DEFAUL_MIN_LEVEL;
        priority = DEFAULT_PRIORITY;
        // During some initial tests, libdeflate was slow when using deflate level 9, with
        // respect to standard deflate
        maxLevel = DEFAUL_MAX_LEVEL;
    }

    static Set<CompressionType> SUPPORTED_TYPES = Collections.singleton(CompressionType.DEFLATE);

    @Override
    public Set<CompressionType> getSupportedCompressions() {
        return SUPPORTED_TYPES;
    }

    @Override
    public Compressor createCompressor(int level, CompressionType compressionType) {
        checkCompression(compressionType);
        return new LibDeflateCompressor(level);
    }

    @Override
    public boolean isEnabled() {
        return Libdeflate.isAvailable();
    }

}
