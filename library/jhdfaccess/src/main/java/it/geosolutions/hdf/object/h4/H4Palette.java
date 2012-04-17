/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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
package it.geosolutions.hdf.object.h4;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a Palette.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class H4Palette extends AbstractHObject implements IHObject {

    /** TODO: need to add locking as used in H4SDS objects */
    
    /** Logger. */
    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");
    
    /**
     * the datavalues of this palette. They will be loaded only when required.
     */
    private byte[] values = null;

    /**
     * the datatype of this palette
     */
    private int datatype;

    /**
     * The index of this palette within the parent image.
     */
    private int index;

    /**
     * The number of entries of this palette.
     */

    private int numEntries;

    /**
     * the interlace Mode associated to this palette <BR>
     * Available values are:<BR>
     * 
     * HDFConstants.MFGR_INTERLACE_PIXEL<BR>
     * HDFConstants.MFGR_INTERLACE_LINE<BR>
     * HDFConstants.MFGR_INTERLACE_COMPONENT<BR>
     */
    private int interlaceMode;

    /**
     * the reference of this palette
     */
    private H4ReferencedObject reference;

    /**
     * the number of components of this palette
     */
    private int numComponents;

    // ////////////////////////////////////////////////////////////////////////
    //
    // SET of Getters
    // 
    // ////////////////////////////////////////////////////////////////////////
    /**
     * getter of <code>index</code>
     * 
     * @return the index of this palette within the parent image.
     */
    public int getIndex() {
        return index;
    }

    /**
     * getter of <code>reference</code>
     * 
     * @return the reference of this palette
     */
    int getReference() {
        return reference.getReference();
    }

    /**
     * getter of <code>numComponents</code>
     * 
     * @return the number of components of this palette
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * getter of <code>datatype</code>
     * 
     * @return the datatype of this palette
     */
    public int getDatatype() {
        return datatype;
    }

    /**
     * getter of <code>interlaceMode</code>
     * 
     * @return the interlace Mode associated to this palette
     */
    public int getInterlaceMode() {
        return interlaceMode;
    }

    /**
     * getter of <code>numEntries</code>
     * 
     * @return the number of entries of this palette.
     */
    public int getNumEntries() {
        return numEntries;
    }

    /**
     * Constructor which builds a new <code>H4Palette</code> given its index
     * in the image.<BR>
     * 
     * @param image
     *                the parent image
     * @param index
     *                the index of the required palette
     * @throws IllegalArgumentException
     *                 in case of wrong specified input parameters or in case
     *                 some initialization fails due to wrong identifiers or
     *                 related errors.
     */
    public H4Palette(H4GRImage image, final int index) {
        if (image == null)
            throw new IllegalArgumentException("Null grImage provided");
        if (index < 0)
            throw new IllegalArgumentException("Invalid dimension index");
        final int grID = image.getIdentifier();
        if (grID == HDFConstants.FAIL)
            throw new IllegalArgumentException("Invalid grImage identifier");
        try {
            int identifier = HDFLibrary.GRgetlutid(grID, index);
            if (identifier != HDFConstants.FAIL) {
                setIdentifier(identifier);
                int lutInfo[] = new int[] { 0, 0, 0, 0 };

                // Getting palette information
                HDFLibrary.GRgetlutinfo(identifier, lutInfo);
                reference = new H4ReferencedObject(HDFLibrary
                        .GRluttoref(identifier));
                numComponents = lutInfo[0];

                // palette datatype
                datatype = lutInfo[1] & (~HDFConstants.DFNT_LITEND);

                // palette interlaceMode
                interlaceMode = lutInfo[2];

                // palette num entries
                numEntries = lutInfo[3];
            } else {
                // throw an exception
                throw new IllegalStateException("Unable to find lut identifier");
            }

        } catch (HDFException e) {
            throw new IllegalStateException(
                    "HDFException occurred while creating a new H4Palette", e);
        }
    }

    /**
     * return a byte array containing palette values.
     * 
     * @return the values of the palette.
     * @throws HDFException
     */
    public synchronized byte[] getValues() throws HDFException {
        if (values == null) {
            HDFLibrary.GRreqlutil(getIdentifier(), interlaceMode);
            values = new byte[3 * numEntries];
            HDFLibrary.GRreadlut(getIdentifier(), values);
        }
        return (byte[])values.clone();
    }

    /**
     * Method inherited from {@link AbstractHObject}.
     */
    public synchronized void dispose() {
       final int identifier = getIdentifier();
       if (identifier!=HDFConstants.FAIL){
           if (LOGGER.isLoggable(Level.FINE))
               LOGGER.log(Level.FINE, "disposing Palette with ID = "
                       + identifier);
           values = null;
           numEntries=-1;
           interlaceMode=-1;
           reference=null;
       }
        
        super.dispose();
    }
}
