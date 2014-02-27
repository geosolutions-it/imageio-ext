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
package it.geosolutions.imageio.plugins.png;

import java.awt.image.IndexColorModel;

import ar.com.hjg.pngj.IImageLine;

/**
 * The bridge between images and PNG scanlines 
 * 
 * @author Andrea Aime - GeoSolutions
 */
public interface ScanlineProvider extends IImageLine {

    /**
     * Image width
     * 
     * @return
     */
    int getWidth();
    
    /**
     * Image height
     * @return
     */
    int getHeight();
    
    /**
     * The bit depth of this image, 1, 2, 4, 8 or 16
     * @return
     */
    public byte getBitDepth();
    
    /**
     * The number of byte[] elements in the scaline
     * @return
     */
    public int getScanlineLength();
    
    /**
     * The next scanline, or throws an exception if we got past the end of the image
     * 
     * @return
     */
    void next(byte[] scaline, int offset, int length);
    
    /**
     * Returns the palette for this image, or null if the image does not have one 
     * @return
     */
    IndexColorModel getPalette();
}
