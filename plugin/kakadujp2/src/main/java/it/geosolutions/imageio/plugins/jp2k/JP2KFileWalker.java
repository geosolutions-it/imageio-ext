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
import it.geosolutions.imageio.plugins.jp2k.box.JP2KFileBox;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jp2_input_box;
import kdu_jni.Jp2_locator;
import kdu_jni.KduException;

/**
 * Parses a JP2K File into a TreeModel of boxes.
 * 
 * <p>
 * Note that for the moment we are doing basic parsing which means that we try
 * to recognize as many boxes as possible but we uses the {@link Jp2_family_src}
 * object from kakadu. In the future we should allow this file walker to walk
 * through fragmented codestream as in general JPX files.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
class JP2KFileWalker {
    /** {@link Logger} for this {@link JP2KFileWalker}. */
    private final static Logger LOGGER = Logger.getLogger("JP2KFileWalker");

    /**
     * Tells me whether or not this {@link JP2KFileWalker} has been initialized
     * or not.
     */
    private boolean initialized;

    /**
     * Contains all the boxes this {@link JP2KFileWalker} has been able to
     * recognize, parsed in a tree model.
     */
    private DefaultTreeModel tree;

    /**
     * File name of the underlying jpeg2k source.
     */
    private String fileName;

    /**
     * Object used to parse the underlying file.
     */
    private Jp2_family_src familySource;

    /**
     * 
     */
    public JP2KFileWalker(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * Parses this file into a {@link TreeModel} object.
     */
    private void init() {
        if (this.initialized)
            return;

        // //
        //
        // create needed kakadu machinery
        //
        // //
        familySource = new Jp2_family_src();
        final Jp2_input_box inputBox = new Jp2_input_box();
        final Jp2_locator locator = new Jp2_locator();
        List<? extends Throwable> exceptions = Collections.emptyList();

        try {
            // open the family
            familySource.Open(fileName);

            // create the needed objects
            final JP2KFileBox box = new JP2KFileBox();
            this.tree = new DefaultTreeModel(box);
            
            // Add a listener: a new instance of a JP2KTreeController on the tree 
            final JP2KTreeController controller = new JP2KTreeController(this.tree);
            this.tree.addTreeModelListener(controller);

            // recursive parsing
            if (inputBox.Open(familySource, locator)) {
                parse(inputBox, box, 0);
            }
            
            controller.checkTreeConsistency();
            // clean up
            inputBox.Close();

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
                if (inputBox != null)
                    inputBox.Close();

            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.FINEST))
                    LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
            }
        }
        
        if (!exceptions.isEmpty()){
            // TODO Create a specific exception that wraps them all!
            throw new IllegalStateException("Tree Check failed");
        }
    }

    /**
     * Parses this box, his brothers as well as his children recursively.
     * 
     * @param inputBox
     *                the box to begin the parsing with.
     * @param parent
     *                the parent box to which attach this box as well as its
     *                brothers.
     * @param index
     *                the index at which this box should be attached in his
     *                father's children list.
     * @throws KduException
     *                 in case any problem with the low level machinery happens.
     */
    private void parse(final Jp2_input_box inputBox, final JP2KBox parent,
            int index) throws KduException {
        final Jp2_input_box childBox = new Jp2_input_box();
        try {

            // //
            //
            // get the info for this box
            //
            // //
            final int boxtype = (int) (0xffffffff & inputBox.Get_box_type());
            final String typeString = BoxUtilities.getTypeString(boxtype);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Found box " + typeString);
            }
            LazyJP2KBox currentBox = null;

            // //
            //
            // get the content of the box
            //
            // //
            if (BoxUtilities.boxNames.containsKey(boxtype)) {
                final Jp2_locator locator = inputBox.Get_locator();

                // created a lazily loaded box
                currentBox = new LazyJP2KBox(this.fileName, boxtype, locator);
                parent.insert(currentBox, index++);
                this.tree.nodesWereInserted(parent, new int[] { index - 1 });
            } else {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Box of type " + typeString + " cannot be handled by this file type reader");
                }
            }

            // //
            //
            // check children
            //
            // //
            // reopen box
            if (BoxUtilities.SUPERBOX_NAMES.contains(typeString)) {
                if (childBox.Open(inputBox)) {
                    if (childBox.Exists()) {
                        parse(childBox, currentBox, 0);
                    }
                    // close box
                    childBox.Close();
                }
            }

            // //
            //
            // check next
            //
            // //
            // close box
            inputBox.Close();
            if (inputBox.Open_next()) {
                if (inputBox.Exists()) {
                    parse(inputBox, parent, index);
                }

            }

        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during Box management! ",
                    e);
        } finally {
            // clean up
            try {
                if (childBox != null)
                    childBox.Close();

            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.FINEST))
                    LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
            }

            try {
                if (inputBox != null)
                    inputBox.Close();

            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.FINEST))
                    LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Retrieves a cached {@link TreeModel} instance which represent the
     * underlying jpeg2k file.
     * 
     * @return a cached {@link TreeModel} instance which represent the
     *         underlying jpeg2k file.
     */
    public synchronized TreeModel getJP2KBoxesTree() {
        init();
        return this.tree;
    }
}
