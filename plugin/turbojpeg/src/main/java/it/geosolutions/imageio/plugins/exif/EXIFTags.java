/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.exif;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.EXIFParentTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.EXIFTIFFTagSet;

public class EXIFTags {
    
    public static final int COPYRIGHT = BaselineTIFFTagSet.TAG_COPYRIGHT;

    public static final int EXIF_IFD_POINTER = EXIFParentTIFFTagSet.TAG_EXIF_IFD_POINTER;

    public static final int USER_COMMENT = EXIFTIFFTagSet.TAG_USER_COMMENT;
    
    private EXIFTags() {
        
    }
    
    public enum Type{
        BASELINE, EXIF
        //TODO more may be added in the future, like GPS, ...
    }
}
