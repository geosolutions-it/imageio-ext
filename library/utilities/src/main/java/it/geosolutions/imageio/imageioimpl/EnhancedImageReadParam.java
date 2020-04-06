/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        http://java.net/projects/imageio-ext/
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
package it.geosolutions.imageio.imageioimpl;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

public class EnhancedImageReadParam extends ImageReadParam implements Cloneable{

    // the bands parameter define the order and which bands should be returned
    // with respect to standard ImageReadParam's sourceBands and destinationBands
    // it allows duplicated entries
    protected int[] bands;

    protected Rectangle destinationRegion;

    public Rectangle getDestinationRegion() {
        return destinationRegion;
    }

    public void setDestinationRegion(final Rectangle destinationRegion) {
        this.destinationRegion = (Rectangle) destinationRegion.clone();
    }

    public int[] getBands() {
        return bands;
    }

    public void setBands(int[] bands) {
        this.bands = bands;
    }

    /**
     * Performs a narrow clone of this {@link EnhancedImageReadParam}.
     * 
     * @param param the {@link EnhancedImageReadParam} instance containing the clone.
     * @return the narrow clone of this {@link ImageReadParam}.
     */
    protected Object narrowClone(EnhancedImageReadParam param) {
        param.setDestination(this.getDestination());
        int[] destBands = this.getDestinationBands();
        if (destBands != null)
            param.setDestinationBands((int[]) destBands.clone());
        int[] bands = this.getBands();
        if (bands != null)
            param.setBands((int[]) bands.clone());
        Point p = this.getDestinationOffset();
        if (p != null) {
            param.setDestinationOffset((Point) p.clone());
        }

        if (this.getDestinationType() != null)
            param.setDestinationType(this.getDestinationType());
        int[] srcBands = this.getSourceBands();
        if (srcBands != null)
            param.setSourceBands((int[]) srcBands.clone());

        param.setSourceProgressivePasses(this.getSourceMinProgressivePass(),
                this.getSourceNumProgressivePasses());
        Rectangle srcRegion = this.getSourceRegion();
        if (srcRegion != null) {
            param.setSourceRegion((Rectangle) srcRegion.clone());
        }

        param.setSourceSubsampling(this.getSourceXSubsampling(), this
                .getSourceYSubsampling(), this.getSubsamplingXOffset(), this
                .getSubsamplingYOffset());
        param.setController(this.getController());
        Dimension d = this.getSourceRenderSize();
        if (d != null) {
            param.setSourceRenderSize((Dimension) d.clone());
        }
        
        Rectangle destinationRegion = this.getDestinationRegion();
        if (destinationRegion != null) {
            param.setDestinationRegion((Rectangle) destinationRegion.clone());
        }
        return param;
    }

    public Object clone() throws CloneNotSupportedException {
        EnhancedImageReadParam param = new EnhancedImageReadParam();
        return narrowClone(param);
    }
}
