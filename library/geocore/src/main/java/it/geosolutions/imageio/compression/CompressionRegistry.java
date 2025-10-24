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

import it.geosolutions.imageio.registry.ImageIOEXTRegistry;
import java.util.*;

/** A Registry for CompressorSpi, DecompressorSpi registering SPIs found on the classpath */
public class CompressionRegistry extends ImageIOEXTRegistry {

    private static final List<Class<?>> INITIAL_TYPES = new ArrayList(2);

    private static final CompressionRegistry DEFAULT_INSTANCE;

    public CompressionRegistry() {
        super(INITIAL_TYPES.iterator());
        registerApplicationClasspathSpis();
    }

    static {
        INITIAL_TYPES.add(CompressorSpi.class);
        INITIAL_TYPES.add(DecompressorSpi.class);
        DEFAULT_INSTANCE = new CompressionRegistry();
    }

    public static CompressionRegistry getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public void registerApplicationClasspathSpis() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Iterator categories = this.getSubTypes();

        while (categories.hasNext()) {
            Class _class = (Class) categories.next();
            Iterator iterator = ServiceLoader.load(_class, cl).iterator();

            while (iterator.hasNext()) {
                try {
                    CompressionPrioritySpi spi = (CompressionPrioritySpi) iterator.next();
                    this.registerSPI(spi);
                } catch (ServiceConfigurationError sce) {
                    if (System.getSecurityManager() == null) {
                        throw sce;
                    }
                }
            }
        }
    }
}
