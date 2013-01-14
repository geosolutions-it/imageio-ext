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

import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities;
import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities.WriteCompression;

import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;


/**
 * Wrapper class related to an ImageSegment of a NITF file.
 * 
 * @author Daniele Romagnoli, GeoSolutions s.a.s.
 */
public class ImageWrapper extends IdentifiableNITFObjectWrapper {

    /**
     * Image Band wrapper, storing the ISUBCAT, IREP fields of a Band Info
     */
    public static class ImageBand {

        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public String getRepresentation() {
            return representation;
        }

        public void setRepresentation(String representation) {
            this.representation = representation;
        }

        public ImageBand(String subCategory, String representation) {
            this.subCategory = subCategory;
            this.representation = representation;
        }
        
        public ImageBand() {
            
        }

        String subCategory;

        String representation;
    }

    /**
     * Supported ICORDS values
     */
    public enum ICords {
        G, N, S
    }

    /**
     * Supported IREP values
     */
    public enum Representation {
        MONO, MULTI, RGB, NODISPLY
    }

    /**
     * Supported ICAT values
     */
    public enum Category {
        VIS, MS, CLOUD
    }

    /**
     * TODO: once the mapping framework is available, we should deal with metadata objects
     */
    public ImageWrapper() {
        super();
    }

    /**
     * The underlying renderedImage to be stored within the related ImageSegment
     */
    private RenderedImage image;

    /**
     * The {@link WriteCompression} to be used to compress the image in the ImageSegment
     */
    private WriteCompression compression;

    /**
     * The ISORCE field of the ImageSubHeader
     */
    private String source;

    /**
     * The ICOM fields of the ImageSubHeader
     */
    private List<String> comments;

    /**
     * The Image Representation to be used for the IREP filed of the ImageSubHeader
     */
    private Representation representation;

    /**
     * The Image Category to be used for the ICAT filed of the ImageSubHeader
     */
    private Category imageCategory;

    private String imageCoordinateSystem;
    
    private String imageMagnification;

    private String igeolo;
    
    private String pixelJustification = NITFUtilities.Consts.DEFAULT_PJUST;
    
    private ImageBand[] bands;

    private Map<String, Map<String, String>> tres;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public RenderedImage getImage() {
        return image;
    }

    public void setImage(RenderedImage image) {
        this.image = image;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public WriteCompression getCompression() {
        return compression;
    }

    public void setCompression(WriteCompression compression) {
        this.compression = compression;
    }

    public Representation getRepresentation() {
        return representation;
    }

    public void setRepresentation(Representation representation) {
        this.representation = representation;
    }

    public Category getImageCategory() {
        return imageCategory;
    }

    public void setImageCategory(Category imageCategory) {
        this.imageCategory = imageCategory;
    }

    public String getImageCoordinateSystem() {
        return imageCoordinateSystem;
    }

    public void setImageCoordinateSystem(String imageCoordinateSystem) {
        this.imageCoordinateSystem = imageCoordinateSystem;
    }

    public String getImageMagnification() {
        return imageMagnification;
    }

    public void setImageMagnification(String imageMagnification) {
        this.imageMagnification = imageMagnification;
    }

    public String getIgeolo() {
        return igeolo;
    }

    public void setIgeolo(String igeolo) {
        this.igeolo = igeolo;
    }

    public String getPixelJustification() {
        return pixelJustification;
    }

    public void setPixelJustification(String pixelJustification) {
        this.pixelJustification = pixelJustification;
    }

    public ImageBand[] getBands() {
        return bands;
    }

    public void setBands(ImageBand[] bands) {
        this.bands = bands;
    }

    public Map<String, Map<String, String>> getTres() {
        return tres;
    }

    public void setTres(Map<String, Map<String, String>> tres) {
        this.tres = tres;
    }

}
