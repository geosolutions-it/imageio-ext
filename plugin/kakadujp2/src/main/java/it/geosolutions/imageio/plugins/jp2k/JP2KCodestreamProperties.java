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

import java.awt.image.ColorModel;
import java.awt.image.SampleModel;

/**
 * Placeholder for a wide set of codestream properties.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
class JP2KCodestreamProperties {
    
    /** Number of components of the source. */
    private int numComponents;

    private int dataBufferType = -1;

    /** the bitDepth */
    private int maxBitDepth;

    /** the whole image width */
    private int width;

    /** the whole image height */
    private int height;

    /** the tile image width */
    private int tileWidth;

    /** the tile image height */
    private int tileHeight;

    /** sample model for the whole image */
    private SampleModel sampleModel = null;

    /** color model */
    private ColorModel colorModel = null;

    /** max number of available quality layers */
    private int maxAvailableQualityLayers = -1;

    /** The source resolution levels. */
    private int sourceDWTLevels;

    /** It is simply 2^sourceDWTLevels */
    private int maxSupportedSubSamplingFactor;

    private boolean isSigned;

    private int[] bitsPerComponent;

    private int[] componentIndexes;
    
    protected JP2KCodestreamProperties(){
        
    }

    public int getNumComponents() {
        return numComponents;
    }

    public void setNumComponents(int components) {
        numComponents = components;
    }

    public int getDataBufferType() {
        return dataBufferType;
    }

    public void setDataBufferType(int dataBufferType) {
        this.dataBufferType = dataBufferType;
    }

    public int getMaxBitDepth() {
        return maxBitDepth;
    }

    public void setMaxBitDepth(int maxBitDepth) {
        this.maxBitDepth = maxBitDepth;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public SampleModel getSampleModel() {
        return sampleModel;
    }

    public void setSampleModel(SampleModel sm) {
        this.sampleModel = sm;
    }

    public ColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(ColorModel cm) {
        this.colorModel = cm;
    }

    public int getMaxAvailableQualityLayers() {
        return maxAvailableQualityLayers;
    }

    public void setMaxAvailableQualityLayers(int maxAvailableQualityLayers) {
        this.maxAvailableQualityLayers = maxAvailableQualityLayers;
    }

    public int getSourceDWTLevels() {
        return sourceDWTLevels;
    }

    public void setSourceDWTLevels(int sourceDWTLevels) {
        this.sourceDWTLevels = sourceDWTLevels;
    }

    public int getMaxSupportedSubSamplingFactor() {
        return maxSupportedSubSamplingFactor;
    }

    public void setMaxSupportedSubSamplingFactor(int maxSupportedSubSamplingFactor) {
        this.maxSupportedSubSamplingFactor = maxSupportedSubSamplingFactor;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean isSigned) {
        this.isSigned = isSigned;
    }

    public int[] getBitsPerComponent() {
        return bitsPerComponent!=null?bitsPerComponent.clone():bitsPerComponent;
    }

    public void setBitsPerComponent(int[] bitsPerComponent) {
        this.bitsPerComponent = bitsPerComponent;
    }

    public int[] getComponentIndexes() {
        return componentIndexes!=null?componentIndexes.clone():componentIndexes;
    }

    public void setComponentIndexes(int[] componentIndexes) {
        this.componentIndexes = componentIndexes;
    }
    
}
