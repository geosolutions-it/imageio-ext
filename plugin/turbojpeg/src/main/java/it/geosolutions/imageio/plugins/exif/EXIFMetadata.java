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

import it.geosolutions.imageio.plugins.exif.EXIFTags.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniele Romagnoli, GeoSolutions SAS
 * 
 * Class representing EXIF entity in terms of list of baseline TIFF Tags and specific EXIF tags. 
 */
public class EXIFMetadata {
    
    Map<Type, List<TIFFTagWrapper>> tags;
    
//    /** Package private list of tags */
//    List<TIFFTagWrapper> baselineExifTags;
//    List<TIFFTagWrapper> exifTags;
//    
    /** 
     * In order to minimize inner checks, make sure that the elements in each list
     * are provided in ascending order, as requested by the EXIF specification. 
     * 
     * @param baselineExifTags a {@link List} containing baseline TIFF tags elements, 
     *  already sorted in ascending order.
     * @param exifTags a {@link List} containing EXIF TIFF tags elements, 
     *  already sorted in ascending order.  
     */
    public EXIFMetadata(
            List<TIFFTagWrapper> baselineExifTags,
            List<TIFFTagWrapper> exifTags) {
        tags = new HashMap<Type, List<TIFFTagWrapper>>();
        tags.put(Type.BASELINE, baselineExifTags);
        tags.put(Type.EXIF, exifTags);
    }
    
    /** 
     * In order to minimize inner checks, make sure that the elements in each list of the map
     * are provided in ascending order, as requested by the EXIF specification. 
     * 
     * @param tagsMap the map containing EXIF tags. The map won't be cloned
     */
    public EXIFMetadata(Map<Type, List<TIFFTagWrapper>> tagsMap) {
        this.tags = tagsMap;
    }
    
    /**
     * Set the specified TAG of the specified list, with the specified content.
     * The TAG needs to be already present within the list. No Tags will be added to the list 
     * if missing. The content set will also update the count value.
     *  
     * @param tagNumber the number of the tag to be updated.
     * @param content the content to be set for that tag.
     * @param tagType the type of TAGs list to be scanned. 
     */
    public void setTag(
            final int tagNumber, 
            final Object content, 
            final Type tagType){
        List<TIFFTagWrapper> list = getList(tagType);
        for (TIFFTagWrapper wrapper: list) {
            if (wrapper.getNumber() == tagNumber){
                wrapper.setContent(content); 
                
                // Update the count field on top of the content/prefix/suffix
                if (content instanceof byte[]){
                    int count = ((byte[]) content).length;
                    if (wrapper.getSuffix() != null){
                        count += wrapper.getSuffix().length;
                    }
                    if (wrapper.getPrefix() != null){
                        count += wrapper.getPrefix().length;
                    }
                    wrapper.setCount(count);
                }
                break;
            }
        }
    }
    
    /**
     * Return the proper tag list, depending on the specified {@link EXIFTagType}.
     * @param tagType an {@link EXIFTagType} specified the type of tag.
     * @return the specific tag list.
     */
    List<TIFFTagWrapper> getList(final Type tagType) {
        switch (tagType){
        case EXIF:
            return tags.get(Type.EXIF);
        case BASELINE:
            return tags.get(Type.BASELINE);
        }
        return null;
    }
}
