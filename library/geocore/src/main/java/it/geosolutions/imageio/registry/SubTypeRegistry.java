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
package it.geosolutions.imageio.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SubTypeRegistry {
    ImageIOEXTRegistry registry;
    Class<?> subType;

    public SubTypeRegistry(ImageIOEXTRegistry registry, Class<?> subType) {
        this.registry = registry;
        this.subType = subType;
    }

    final PartiallyOrderedSet<Object> partiallyOrderedSet = new PartiallyOrderedSet();
    final Map<Class<?>, Object> map = new HashMap();

    public synchronized boolean registerSPI(Object spi) {
        Object provider = map.get(spi.getClass());
        boolean exists = provider != null;
        if (exists) {
            deregisterSPI(provider);
        }

        map.put(spi.getClass(), spi);
        partiallyOrderedSet.add(spi);
        if (spi instanceof RegisterablePlugin) {
            RegisterablePlugin plugin = (RegisterablePlugin) spi;
            plugin.onRegistration(registry, subType);
        }
        return !exists;
    }

    public synchronized boolean deregisterSPI(Object spi) {
        Object plugin = map.get(spi.getClass());
        if (spi == plugin) {
            map.remove(spi.getClass());
            partiallyOrderedSet.remove(spi);
            if (spi instanceof RegisterablePlugin) {
                RegisterablePlugin rs = (RegisterablePlugin) spi;
                rs.onDeregistration(registry, subType);
            }
            return true;
        }
        return false;
    }

    public boolean isRegistered(Object spi) {
        Object plugin = map.get(spi.getClass());
        return plugin == spi;
    }

    public synchronized boolean setOrder(Object firstSpi, Object secondSpi) {
        return partiallyOrderedSet.setOrder(firstSpi, secondSpi);
    }

    public synchronized boolean unsetOrder(Object firstSpi, Object secondSpi) {
        return partiallyOrderedSet.clearOrder(firstSpi, secondSpi);
    }

    public synchronized Iterator<Object> getSPIs(boolean sorting) {
        return sorting ? partiallyOrderedSet.iterator() : map.values().iterator();
    }

    public synchronized void clear() {
        Iterator iter = map.values().iterator();

        while (true) {
            Object provider;
            do {
                if (!iter.hasNext()) {
                    partiallyOrderedSet.clear();
                    return;
                }
                provider = iter.next();
                iter.remove();
            } while (!(provider instanceof RegisterablePlugin));
        }
    }

    public synchronized void finalize() {
        clear();
    }
}
