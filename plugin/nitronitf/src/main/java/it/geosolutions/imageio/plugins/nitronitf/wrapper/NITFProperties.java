/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageio.plugins.nitronitf.wrapper;

import java.util.List;

/**
 * A NITF Properties holder class storing all the fields and TREs values, segments and headers to be
 * used to write a NITF file. 
 * 
 * @todo Setup streamMetadata and imageMetadata objects to replace this entity. 
 * @author Daniele Romagnoli, GeoSolutions s.a.s.
 *
 */
public class NITFProperties {

    /** 
     * The object wrapping the MainHeader 
     */
    private HeaderWrapper header;

    private ShapeFileWrapper shape;

    /** 
     * The list of objects wrapping the TextSegments 
     */
    private List<TextWrapper> textsWrapper;

    /** 
     * The list of objects wrapping the ImageSegments  
     */
    private List<ImageWrapper> imagesWrapper;

    // private Map<String, Map<String, String>> extensionsMap;
    //
    // public Map<String, Map<String, String>> getExtensionsMap() {
    // return extensionsMap;
    // }
    //
    // public void setExtensionsMap(Map<String, Map<String, String>> extensionsMap) {
    // this.extensionsMap = extensionsMap;
    // }

    public HeaderWrapper getHeader() {
        return header;
    }

    public void setHeader(HeaderWrapper header) {
        this.header = header;
    }

    public ShapeFileWrapper getShape() {
        return shape;
    }

    public void setShape(ShapeFileWrapper shape) {
        this.shape = shape;
    }

    /**
     * Get the underlying text wrapper {@code List}. Note that this getter 
     * returns the list itself, not a copy. 
     * Therefore any change to the referred {@code List} will directly 
     * modify the underlying object. 
     * @return the underlying {@code List} of {@link TextWrapper} objects.
     */
    public List<TextWrapper> getTextsWrapper() {
        return textsWrapper;
    }

    public void setTextsWrapper(List<TextWrapper> textsWrapper) {
        this.textsWrapper = textsWrapper;
    }

    /**
     * Get the underlying images wrapper {@code List}. Note that this getter 
     * returns the list itself, not a copy. 
     * Therefore any change to the referred {@code List} will directly 
     * modify the underlying object. 
     * @return the underlying {@code List} of {@link ImageWrapper} objects.
     */
    public List<ImageWrapper> getImagesWrapper() {
        return imagesWrapper;
    }

    public void setImagesWrapper(List<ImageWrapper> imagesWrapper) {
        this.imagesWrapper = imagesWrapper;
    }

}
