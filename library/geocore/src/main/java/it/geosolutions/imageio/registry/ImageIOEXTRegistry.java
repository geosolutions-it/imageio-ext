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

import java.util.*;

/**
 * A Registry for Plugins
 */
public class ImageIOEXTRegistry {

    private void checkSubType(SubTypeRegistry reg) {
        if (reg == null) {
            throw new IllegalArgumentException("subtype not found!");
        }
    }

    void ensureNotNull (Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " == null!");
        }
    }


    private <T> void checkSpis(T firstSpi, T secondSpi) {
        if (firstSpi == null || secondSpi == null) {
            throw new IllegalArgumentException("spi is null!");
        }
        if (firstSpi == secondSpi) {
            throw new IllegalArgumentException("spis are the same!");
        }
    }

    private Map<Class<?>, SubTypeRegistry> subTypesMap = new HashMap();

    public ImageIOEXTRegistry(Iterator<Class<?>> subTypes) {
        ensureNotNull(subTypes, "subTypes");
        while(subTypes.hasNext()) {
            Class<?> subType = (Class)subTypes.next();
            SubTypeRegistry reg = new SubTypeRegistry(this, subType);
            subTypesMap.put(subType, reg);
        }
    }

    public void registerSPI(Object spi) {
        ensureNotNull(spi, "spi");
        Iterator regs = this.getSubRegistries(spi);
        while(regs.hasNext()) {
            SubTypeRegistry reg = (SubTypeRegistry)regs.next();
            reg.registerSPI(spi);
        }
    }

    public boolean isRegistered(Object spi) {
        ensureNotNull(spi, "spi");
        Iterator regs = getSubRegistries(spi);

        SubTypeRegistry reg;
        do {
            if (!regs.hasNext()) {
                return false;
            }
            reg = (SubTypeRegistry)regs.next();
        } while(!reg.isRegistered(spi));

        return true;
    }

    public <T> Iterator<T> getSPIs(Class<T> subtype, boolean sorting) {
        SubTypeRegistry reg = subTypesMap.get(subtype);
        checkSubType(reg);
        return (Iterator<T>) reg.getSPIs(sorting);
    }

    public <T> boolean setOrder(Class<T> subtype, T firstSpi, T secondSpi) {
        checkSpis(firstSpi, secondSpi);
        SubTypeRegistry reg = subTypesMap.get(subtype);
        checkSubType(reg);
        return reg.isRegistered(firstSpi) && reg.isRegistered(secondSpi) ? reg.setOrder(firstSpi, secondSpi) : false;
    }

    public <T> boolean unsetOrder(Class<T> subtype, T firstSpi, T secondSpi) {
        checkSpis(firstSpi, secondSpi);
        SubTypeRegistry reg = subTypesMap.get(subtype);
        checkSubType(reg);
        return reg.isRegistered(firstSpi) && reg.isRegistered(secondSpi) ? reg.unsetOrder(firstSpi, secondSpi) : false;
    }

    public void deregisterSPI(Object spi) {
        ensureNotNull(spi, "spi");
        Iterator regs = getSubRegistries(spi);

        while(regs.hasNext()) {
            SubTypeRegistry reg = (SubTypeRegistry)regs.next();
            reg.deregisterSPI(spi);
        }
    }

    public void deregisterAll(Class<?> subtype) {
        SubTypeRegistry reg = subTypesMap.get(subtype);
        checkSubType(reg);
        reg.clear();
    }

    public void deregisterAll() {
        Iterator iter = subTypesMap.values().iterator();

        while(iter.hasNext()) {
            SubTypeRegistry reg = (SubTypeRegistry)iter.next();
            reg.clear();
        }
    }

    public Iterator<Class<?>> getSubTypes() {
        return subTypesMap.keySet().iterator();
    }

    private Iterator<SubTypeRegistry> getSubRegistries(Object spi) {
        List<SubTypeRegistry> l = new ArrayList();
        Iterator iter = getSubTypes();

        while(iter.hasNext()) {
            Class<?> c = (Class)iter.next();
            if (c.isAssignableFrom(spi.getClass())) {
                l.add(subTypesMap.get(c));
            }
        }

        return l.iterator();
    }

    public void finalize() throws Throwable {
        this.deregisterAll();
        super.finalize();
    }

}
