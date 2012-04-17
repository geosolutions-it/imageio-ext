/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2k.box;

import javax.imageio.metadata.IIOMetadataNode;

/**
 * This class is defined to represent a ComponentMappingBoxMetadataNode of JPEG JP2 file
 * format. 
 */
@SuppressWarnings("serial")
public class ComponentMappingBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private int nComponents;

    /** The data elements. */
    private String[] component;

    private String[] type;

    private String[] map;

    public ComponentMappingBoxMetadataNode(final ComponentMappingBox box) {
        super(box);
        final short[] comps = box.getComponent();
        final byte[] types = box.getComponentType();
        final byte[] assoc = box.getComponentAssociation();
        nComponents = comps.length;
        component = new String[nComponents];
        type = new String[nComponents];
        map = new String[nComponents];

        for (int i = 0; i < nComponents; i++) {
            IIOMetadataNode child = new IIOMetadataNode("Component");
            child.setUserObject(new Short(comps[i]));
            component[i] = Short.toString(comps[i]);
            child.setNodeValue(component[i]);
            appendChild(child);

            child = new IIOMetadataNode("ComponentType");
            child.setUserObject(new Byte(types[i]));
            type[i] = Byte.toString(types[i]);
            child.setNodeValue(type[i]);
            appendChild(child);

            child = new IIOMetadataNode("ComponentAssociation");
            map[i] = Byte.toString(assoc[i]);
            child.setUserObject(new Byte(assoc[i]));
            child.setNodeValue(map[i]);
            appendChild(child);
        }
    }

    public String getComponentType(final int index) {
        if (index > nComponents - 1)
            throw new IllegalArgumentException("Number of Component is "
                    + nComponents);
        return type[index];
    }

    public String getComponentAssociation(final int index) {
        if (index > nComponents - 1)
            throw new IllegalArgumentException("Number of Component is "
                    + nComponents);
        return map[index];
    }

    public String getComponent(final int index) {
        if (index > nComponents - 1)
            throw new IllegalArgumentException("Number of Component is "
                    + nComponents);
        return component[index];
    }
}
