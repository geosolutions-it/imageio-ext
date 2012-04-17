/*
 * $RCSfile: Box.java,v $
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
 * $Revision: 1.6 $
 * $Date: 2007/09/05 20:03:20 $
 * $State: Exp $
 */
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

import it.geosolutions.imageio.plugins.jp2k.JP2KBoxMetadata;

import java.lang.reflect.Method;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * This class is defined to create the box of JP2 file format. A box has a
 * length, a type, an optional extra length and its content. The subclasses
 * should explain the content information.
 * 
 * @uml.dependency supplier="it.geosolutions.imageio.plugins.jp2k.box.JP2KBox"
 */
public abstract class BaseJP2KBox extends DefaultMutableTreeNode implements
        JP2KBoxMetadata {

    private byte[] data;

    /**
     * @uml.property name="extraLength"
     */
    private long extraLength;

    /**
     * Box length, extra length, type and content data array
     * 
     * @uml.property name="length"
     */
    private int length;

    /**
     * @uml.property name="type"
     */
    private int type;

    /**
     * Constructs a <code>Box</code> instance using the provided the box type
     * and the box content in byte array format.
     * 
     * @param length
     *                The provided box length.
     * @param type
     *                The provided box type.
     * @param data
     *                The provided box content in a byte array.
     * 
     * @throws IllegalArgumentException
     *                 If the length of the content byte array is not length -
     *                 8.
     */
    public BaseJP2KBox(int length, int type, byte[] data) {
        this.type = type;
        setLength(length);
        setContent(data);
    }

    /**
     * Constructs a <code>Box</code> instance using the provided the box type,
     * the box extra length, and the box content in byte array format. In this
     * case, the length of the box is set to 1, which indicates the extra length
     * is meaningful.
     * 
     * @param length
     *                The provided box length.
     * @param type
     *                The provided box type.
     * @param extraLength
     *                The provided box extra length.
     * @param data
     *                The provided box content in a byte array.
     * 
     * @throws IllegalArgumentException
     *                 If the length of the content byte array is not extra
     *                 length - 16.
     */
    public BaseJP2KBox(int length, int type, long extraLength, byte[] data) {
        this.type = type;
        setLength(length);
        setContent(data);
    }

    /**
     * Constructs a Box from an "unknown" Node. This node has at least the
     * attribute "Type", and may have the attribute "Length", "ExtraLength" and
     * a child "Content". The child node content is a IIOMetaDataNode with a
     * byte[] user object.
     */
    public BaseJP2KBox(Node node) throws IIOInvalidTreeException {
        NodeList children = node.getChildNodes();

        String value = (String) BoxUtilities.getAttribute(node, "Type");
        type = BoxUtilities.getTypeInt(value);
        if (value == null || BoxUtilities.names.get(new Integer(type)) == null)
            throw new IIOInvalidTreeException("Type is not defined", node);

        value = (String) BoxUtilities.getAttribute(node, "Length");
        if (value != null)
            length = new Integer(value).intValue();

        value = (String) BoxUtilities.getAttribute(node, "ExtraLength");
        if (value != null)
            extraLength = new Long(value).longValue();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ("Content".equals(child.getNodeName())) {
                if (child instanceof IIOMetadataNode) {
                    IIOMetadataNode cnode = (IIOMetadataNode) child;
                    try {
                        data = (byte[]) cnode.getUserObject();
                    } catch (Exception e) {
                    }
                } else {
                    data = BoxUtilities.getByteArrayElementValue(child);
                }

                if (data == null) {
                    value = node.getNodeValue();
                    if (value != null)
                        data = value.getBytes();
                }
            }
        }

    }

    /**
     * Creates an <code>IIOMetadataNode</code> from this box. The format of
     * this node is defined in the XML dtd and xsd for the JP2 image file.
     * 
     * This method is designed for the types of boxes whose XML tree only has 2
     * levels.
     */
    protected IIOMetadataNode getNativeNodeForSimpleBox() {
        try {
            Method m = this.getClass().getMethod("getElementNames",
                    (Class[]) null);
            String[] elementNames = (String[]) m.invoke(null, (Object[]) null);

            IIOMetadataNode node = new IIOMetadataNode(BoxUtilities
                    .getName(getType()));
            setDefaultAttributes(node);
            for (int i = 0; i < elementNames.length; i++) {
                IIOMetadataNode child = new IIOMetadataNode(elementNames[i]);
                m = this.getClass().getMethod("get" + elementNames[i],
                        (Class[]) null);
                Object obj = m.invoke(this, (Object[]) null);
                child.setUserObject(obj);
                child.setNodeValue(ImageUtil.convertObjectToString(obj));
                node.appendChild(child);
            }
            return node;
        } catch (Exception e) {
            throw new IllegalArgumentException("Box0");
        }
    }

    /** Returns the box content in byte array. */
    public synchronized byte[] getContent() {
        if (data == null)
            data = compose();
        return data;
    }

    /**
     * Sets the box content. If the content length is not length -8 or extra
     * length - 16, IllegalArgumentException will be thrown.
     */
    private void setContent(byte[] data) {
        if (data != null
                && ((length == 1 && (extraLength - 16 != data.length)) || (length != 1 && length - 8 != data.length)))
            throw new IllegalArgumentException("Box2");
        this.data = data;
        if (data != null)
            parse(data);
    }

    /**
     * Parses the data elements from the byte array. The subclasses should
     * override this method and implement the proper behvaior.
     */
    abstract protected void parse(byte[] data);

    /**
     * Composes the content byte array from the data elements.
     */
    abstract protected byte[] compose();

    /**
     * Creates an <code>IIOMetadataNode</code> from this box. The format of
     * this node is defined in the XML dtd and xsd for the JP2 image file.
     */
    public IIOMetadataNode getNativeNode() {
        String name = BoxUtilities.getName(getType());
        if (name == null)
            name = "unknown";

        IIOMetadataNode node = new IIOMetadataNode(name);
        setDefaultAttributes(node);
        IIOMetadataNode child = new IIOMetadataNode("Content");
        child.setUserObject(data);
        child.setNodeValue(ImageUtil.convertObjectToString(data));
        node.appendChild(child);

        return node;
    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JPEG2000SimpleBox#getExtraLength()
     * @uml.property name="extraLength"
     */
    public long getExtraLength() {
        return extraLength;
    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JPEG2000SimpleBox#getLength()
     * @uml.property name="length"
     */
    public int getLength() {
        return length;
    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JPEG2000SimpleBox#getType()
     * @uml.property name="type"
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the default attributes, "Length", "Type", and "ExtraLength", to the
     * provided <code>IIOMetadataNode</code>.
     */
    protected void setDefaultAttributes(IIOMetadataNode node) {
        node.setAttribute("Length", Integer.toString(length));
        node.setAttribute("Type", BoxUtilities.getTypeString(type));

        if (length == 1) {
            node.setAttribute("ExtraLength", Long.toString(extraLength));
        }
    }

    /**
     * Sets the box extra length length to the provided value.
     * 
     * @uml.property name="extraLength"
     */
    public void setExtraLength(long extraLength) {
        if (length != 1)
            throw new IllegalArgumentException("Box1");
        this.extraLength = extraLength;
    }

    /**
     * Sets the box length to the provided value.
     * 
     * @uml.property name="length"
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @param type
     * @uml.property name="type"
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final String superString = super.toString();
        final StringBuilder builder = new StringBuilder(
                superString != null ? superString : "");
        builder.append("\n");
        builder.append("type:").append(type).append("\n");
        builder.append("box class:").append(BoxUtilities.getBoxClass(type))
                .append("\n");
        builder.append("type hex:").append(Integer.toHexString(type).toUpperCase()).append(
                "\n");
        builder.append("box name:").append(BoxUtilities.getBoxName(type))
                .append("\n");
        builder.append("length:").append(length).append("\n");
        builder.append("extralength:").append(extraLength).append("\n");
        return builder.toString();
    }
    
    public Object clone(){
        return BoxUtilities.createBox(type, data);
    }
}
