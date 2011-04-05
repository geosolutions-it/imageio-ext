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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.plugins.jp2k.box.BoxUtilities;

import java.lang.ref.SoftReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.tree.DefaultMutableTreeNode;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jp2_input_box;
import kdu_jni.Jp2_locator;
import kdu_jni.KduException;

/**
 * {@link JP2KBox} which is loaded lazily, which means only when the content is
 * needed.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
class LazyJP2KBox extends DefaultMutableTreeNode implements JP2KBoxMetadata {

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
        return builder.toString();
    }

    /**
     * 
     */
    private static final long serialVersionUID = -3905954214836933636L;

    private final static Logger LOGGER = Logger.getLogger("LazyJP2Box");

    private final Jp2_locator locator;

    private int type;

    private SoftReference<? extends JP2KBox> boxRef;

    private String filename;

    /**
     * Build a new {@link LazyJP2KBox}.
     * 
     * @param filename
     *                the input filename
     * @param type
     *                the box type
     * @param locator
     *                the JP2 locator referring this JP2K Box.
     */
    LazyJP2KBox(final String filename, final int type,
            final Jp2_locator locator) {
        this.filename = filename;
        this.locator = locator;
        this.type = type;
    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JP2KBox#getContent()
     */
    public byte[] getContent() {
        // superbox don't return any content
        if (!isLeaf())
            return null;
        // paranoid check
        if (BoxUtilities.SUPERBOX_NAMES.contains(BoxUtilities.getTypeString(type)))
            return null;
        final JP2KBox originalBox = loadBox();
        return originalBox.getContent();
    }

    private synchronized JP2KBox loadBox() {
        JP2KBox retVal = this.boxRef == null ? null : this.boxRef.get();
        if (retVal == null) {
            final Jp2_family_src familySource = new Jp2_family_src();
            final Jp2_input_box box = new Jp2_input_box();
            try {
                familySource.Open(filename);

                // //
                //
                // Get the required Jp2_input_box using the locator
                //  
                // //
                box.Open(familySource, locator);
                retVal = BoxUtilities.createBox(type,
                        BoxUtilities.getContent(box));
                this.boxRef = new SoftReference<JP2KBox>(retVal);

            } catch (KduException e) {
                throw new RuntimeException(
                        "Error caused by a Kakadu exception during Box management! ",
                        e);
            } finally {
                // clean up
                try {
                    familySource.Close();
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.FINEST))
                        LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                }

                try {
                    if (box != null)
                        box.Close();
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.FINEST))
                        LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                }
            }
        }
        return retVal;

    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JP2KBox#getExtraLength()
     */
    public long getExtraLength() {
        final JP2KBox originalBox = loadBox();
        return originalBox.getExtraLength();
    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JP2KBox#getLength()
     */
    public int getLength() {
        final JP2KBox originalBox = loadBox();
        return originalBox.getLength();
    }

    /**
     * @see it.geosolutions.imageio.plugins.jp2k.box.JP2KBox#getType()
     */
    public int getType() {
        return type;
    }

    JP2KBox getOriginalBox() {
        return loadBox();
    }

    public IIOMetadataNode getNativeNode() {
        //Delegate the nativeNode creation to the original box
        return ((JP2KBoxMetadata)getOriginalBox()).getNativeNode();
    }

    /**
     * Utility method to return the original box related to an input Box.
     * @param box
     * @return
     */
    static JP2KBox getAsOriginalBox(JP2KBox box) {
        JP2KBox returnedBox = null;
        if (box instanceof LazyJP2KBox)
            returnedBox = (JP2KBox) ((LazyJP2KBox) box).getOriginalBox();
        else if (box instanceof JP2KBox)
            returnedBox = (JP2KBox) box;
        else
            throw new IllegalArgumentException("Not a valid JP2Box");
        return returnedBox;
    }

    @Override
    public Object clone() {
//        LazyJP2KBox newBox = (LazyJP2KBox)super.clone();
        Jp2_locator locator = new Jp2_locator();
        try {
            final long filePos = this.locator.Get_file_pos();
            locator.Set_file_pos(filePos);
//            newBox.filename = filename;
//            newBox.type = type;
//            newBox.locator = locator;
//            newBox.boxRef = null;
//            return newBox;
            return new LazyJP2KBox(filename, type, locator);
        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during Box management! ",
                    e);
        }
    }
}
