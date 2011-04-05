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

import javax.imageio.metadata.IIOInvalidTreeException;

import org.w3c.dom.Node;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 */
@SuppressWarnings("serial")
public class ReaderRequirementsBox extends BaseJP2KBox {

    public final static int BOX_TYPE = 0x72726571;

    public final static String NAME = "rreq";

    public static final String JP2K_MD_NAME = "JP2KReaderRequirementsBox";

    private byte maskLength;

    private int numberOfStandardFlags;
    
    private int numberOfVendorFlags;

    private int[] standardFlags;

    private long[] standardMasks;

    private long fullyUnderstandAspectMask;

    private long decodeCompletelyMask;
    
    /**
     * TODO: Add vendor features
     */

    /**
     * @param length
     * @param type
     * @param data
     */
    public ReaderRequirementsBox(int length, int type, byte[] data) {
        super(length, type, data);
    }

    /**
     * @param length
     * @param type
     * @param extraLength
     * @param data
     */
    public ReaderRequirementsBox(int length, int type, long extraLength,
            byte[] data) {
        super(length, type, extraLength, data);
    }

    public ReaderRequirementsBox(byte[] data) {
        super(8 + data.length, BOX_TYPE, data);
    }

    /**
     * @param node
     * @throws IIOInvalidTreeException
     */
    public ReaderRequirementsBox(Node node) throws IIOInvalidTreeException {
        super(node);
    }

    @Override
    protected byte[] compose() {
        return null;
    }

    @Override
    protected void parse(byte[] data) {
        maskLength = data[0];
        int nsfIndex = 1 + (maskLength * 2);
        // TODO: FIX ME! Use the proper mask size
        numberOfStandardFlags = (((data[nsfIndex] & 0xFF) << 8) | (data[nsfIndex + 1] & 0xFF));
        standardFlags = new int[numberOfStandardFlags];
        standardMasks = new long[numberOfStandardFlags];
        
        switch (maskLength){
        case 1:
            
        case 2:
            fullyUnderstandAspectMask = (((data[1] & 0xFF) << 8) | (data[2] & 0xFF));
            decodeCompletelyMask = (((data[3] & 0xFF) << 8) | (data[4] & 0xFF));
            nsfIndex += 2;
            for (int i = 0; i < numberOfStandardFlags; i++) {
                standardFlags[i] =  (((data[nsfIndex + (i * 2)] & 0xFF) << 8) | (data[nsfIndex
                        + (i * 2) + 1] & 0xFF));
            }
            nsfIndex += (numberOfStandardFlags * 2);

            for (int i = 0; i < numberOfStandardFlags; i++) {
                standardMasks[i] = (((data[nsfIndex + (i * 2)] & 0xFF) << 8) | (data[nsfIndex
                        + (i * 2) + 1] & 0xFF));
            }
            
            nsfIndex += (numberOfStandardFlags * maskLength);
            numberOfVendorFlags = (((data[nsfIndex] & 0xFF) << 8) | (data[nsfIndex + 1] & 0xFF));
            //TODO: Continue setting these fields             
            
            break;
        case 4:
            
        case 8:
        }
        
       
    }
}
