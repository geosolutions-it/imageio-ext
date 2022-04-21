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

import it.geosolutions.imageio.registry.RegisterablePlugin;
import it.geosolutions.imageio.registry.ImageIOEXTRegistry;

import java.util.Iterator;
import java.util.Set;


/**
 * A Compression SPI, reporting supported compressionType and priority.
 * Subclasses can set priorities so you can have multiple SPIs for the same compression type
 * and decide which one should be used based on priority.
 */
public abstract class AbstractCompressionSpi implements CompressionPrioritySpi, RegisterablePlugin {

    protected boolean initialized;

    // SPI Default priority
    protected int priority = 50;

    /** Return the {@link CompressionType} supported by this SPI */
    protected abstract Set<CompressionType> getSupportedCompressions();

    /** simple method checking if the compression is supported by this SPI */
    protected void checkCompression(CompressionType compressionType) {
        if (!getSupportedCompressions().contains(compressionType)) {
            throw new IllegalArgumentException("Unsupported Compression Type: " +
                    compressionType + " Not in range: " + getSupportedCompressions().toString());
        }
    };

    /** Return the priority for this SPI */
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        initialized = this.priority == priority;
        this.priority = priority;
    }

    /**
     * There might be special conditions for which the SPI is not enabled, i.e. a missing
     * dependency native lib
     */
    public boolean isEnabled() {
        return true;
    }

    public void onRegistration(ImageIOEXTRegistry imageIOEXTRegistry, Class<?> aClass) {
        if (initialized) {
            return;
        }

        initialized = true;
        if (!isEnabled()) {
            imageIOEXTRegistry.deregisterSPI(this);
            return;
        }

        Iterator<?> spis = imageIOEXTRegistry.getSPIs(aClass, false);
        while (spis.hasNext()) {
            Object spi = spis.next();
            if (spi != this && spi instanceof AbstractCompressionSpi) {
                AbstractCompressionSpi cpspi = (AbstractCompressionSpi) spi;
                int compare = cpspi.getPriority();
                if (getPriority() > compare) {
                    imageIOEXTRegistry.setOrder((Class) aClass, this, spi);
                } else {
                    imageIOEXTRegistry.setOrder((Class) aClass, spi, this);
                }
            }
        }
    }

    public void onDeregistration(ImageIOEXTRegistry imageIOEXTRegistry, Class<?> aClass) {
        // do nothing
    }

}
