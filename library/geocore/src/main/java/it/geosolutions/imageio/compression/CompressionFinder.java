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
package it.geosolutions.imageio.compression;

import java.util.Iterator;

/**
 * The SPI finder, delegated to retrieve compressors and decompressors for a specific
 * CompressionType format. At the moment, only DEFLATE compression is supported.
 */
public class CompressionFinder {

    private static volatile CompressionRegistry compressionRegistry = CompressionRegistry.getDefaultInstance();

    public static void scanForPlugins() {
        compressionRegistry.registerApplicationClasspathSpis();
    }

    /**
     * Find the higher priority Compressor SPI for the requested {@link CompressionType}
     * supporting the specified compression level and instantiate a Compressor.
     */
    public static Compressor getCompressor(int level, CompressionType compressionType) {

        Iterator<CompressorSpi> iterator;
        try {
            iterator = compressionRegistry.getServiceProviders(CompressorSpi.class, true);
        } catch (IllegalArgumentException var6) {
            return null;
        }

        CompressorSpi selectedSPI = null;
        for (Iterator<CompressorSpi> it = iterator; it.hasNext(); ) {
            CompressorSpi spi = it.next();
            if (level <= spi.getMaxLevel() && level >= spi.getMinLevel()) {
                selectedSPI = spi;
                break;
            }
        }
        if (selectedSPI != null) {
            return selectedSPI.createCompressor(level, compressionType);
        }
        return null;
    }

    /**
     * Find the higher priority Decompressor SPI for the requested {@link CompressionType}
     * and instantiate a Decompressor
     */
    public static Decompressor getDecompressor(CompressionType compressionType) {

        Iterator<DecompressorSpi> iterator;
        try {
            iterator = compressionRegistry.getServiceProviders(DecompressorSpi.class, true);
        } catch (IllegalArgumentException var6) {
            return null;
        }

        if (iterator.hasNext()) {
            DecompressorSpi spi = iterator.next();
            return spi.createDecompressor(compressionType);
        }
        return null;
    }

}
