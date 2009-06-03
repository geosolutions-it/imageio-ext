/*
 *    JGriB1 - OpenSource Java library for GriB files edition 1
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * GribGDSFactorySpi.java  1.0  18/05/2006
 *
 */
package net.sourceforge.jgrib.factory;

import it.geosolutions.factory.AbstractFactory;

import java.io.IOException;
import java.util.Set;

import javax.imageio.stream.ImageInputStream;

import net.sourceforge.jgrib.GribRecordGDS;

public abstract class GribGDSFactorySpi extends AbstractFactory {

    /**
     * Returns a descriptive name for the factory instance.
     * 
     * @return a descriptive name for the factory instance
     * 
     * @uml.property name="name" multiplicity="(0 1)"
     */
    abstract protected String getName();

    /**
     * Returns a <code>java.util.Set&lt;Integer&gt;</code> of the GDS types the
     * GribGDSFactory factory can create are able to handle.
     * 
     * @return the Set of supported GDS types.
     * 
     * @uml.property name="supportedTypes" multiplicity="(0 1)"
     */
    abstract protected Set getSupportedTypes();

    /**
     * Checks if the GribGDSFactory instances this factory serves will be able
     * of working properly (e.g., external dependencies are in place). This
     * method should be used to avoid asking for producer instances if they
     * are likely to fail.
     *
     * @return wether this factory is able to produce producer instances.
     */
    abstract protected boolean isAvailable();

    /**
     * Returns wether the GribGDSFactory created by this factory can create
     * GDSs of the specified type.
     *
     * @param GDSType a GDS type int to check if this factory is able to
     *        handle.
     *
     * @return <code>true</code> if <code>GDSType</code> is an GDS
     *         supported by the GribGDSFactory this factory serves.
     */
    abstract protected boolean canProduce(int GDSType);

    /**
     * Creates and instance of a GribGDSFactory suitable to create GDS types
     *
     * @param GDSType the GDS of the desired grid
     *
     * @return a GribGDSFactory capable of creating grids of <code>type</code>
     *         specified.
     *
     * @throws Exception if <code>type</code> is not one of
     *         the GDS types this factory can create grids in.
     */
    abstract protected GribRecordGDS createGridGDS(int GDSType)
        throws IllegalArgumentException;

    abstract protected GribRecordGDS createGridGDS(ImageInputStream in, int[] data)
        throws IOException;
}