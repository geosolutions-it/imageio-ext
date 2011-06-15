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
 * This class is designed to represent a Channel Definition Box of JPEG JP2 file
 * format. A Channel Definition Box has a length, and a fixed type of "cdef".
 * Its content defines the type of the image channels: color channel, alpha
 * channel or premultiplied alpha channel.
 *
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
public class ChannelDefinitionBoxMetadataNode extends BaseJP2KBoxMetadataNode {

    private short numChannels;

    private String numberOfChannelDefinitions;

    private String[] channels;

    private String[] types;

    private String[] associations;

    public ChannelDefinitionBoxMetadataNode(final ChannelDefinitionBox box) {
        super(box);
        numChannels = box.getNum();
        final short[] channel = box.getChannel();
        final short[] assoc = box.getAssociation();
        final int[] type = box.getTypes();
        IIOMetadataNode child = new IIOMetadataNode("NumberOfChannelDefinition");
        child.setUserObject(new Short(numChannels));
        numberOfChannelDefinitions = Short.toString(numChannels);
        channels = new String[numChannels];
        types = new String[numChannels];
        associations = new String[numChannels];
        child.setNodeValue(numberOfChannelDefinitions);
        appendChild(child);

        child = new IIOMetadataNode("Definitions");
        appendChild(child);

        for (int i = 0; i < numChannels; i++) {
            IIOMetadataNode child1 = new IIOMetadataNode("ChannelNumber");
            child1.setUserObject(new Short(channel[i]));
            channels[i] = Short.toString(channel[i]);
            child1.setNodeValue(channels[i]);
            child.appendChild(child1);

            child1 = new IIOMetadataNode("ChannelType");
            child1.setUserObject(new Integer(type[i]));
            types[i] = Integer.toString(type[i]);
            child1.setNodeValue(types[i]);
            child.appendChild(child1);

            child1 = new IIOMetadataNode("Association");
            child1.setUserObject(new Short(assoc[i]));
            associations[i] = Short.toString(assoc[i]);
            child1.setNodeValue(associations[i]);
            child.appendChild(child1);
        }
    }

    public String getChannel(final int index) {
        if (index > numChannels - 1)
            throw new IllegalArgumentException("Number of channel descriptions is " + numChannels);
        return channels[index];
    }

    public String getType(final int index) {
        if (index > numChannels - 1)
            throw new IllegalArgumentException("Number of channel descriptions is " + numChannels);
        return types[index];
    }

    public String getAssociation(final int index) {
        if (index > numChannels - 1)
            throw new IllegalArgumentException("Number of channel descriptions is " + numChannels);
        return associations[index];
    }

    public String getNumberOfChannelDefinitions() {
        return numberOfChannelDefinitions;
    }
}
